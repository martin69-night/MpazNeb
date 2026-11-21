package cl.duoc.mpazneb.viewmodel

import android.app.Application
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.viewModelScope
import cl.duoc.mpazneb.data.local.entity.ActividadEntity
import cl.duoc.mpazneb.data.local.entity.EscuelaEntity
import cl.duoc.mpazneb.data.local.entity.EstudianteEntity
import cl.duoc.mpazneb.data.local.entity.UsuarioEntity
import cl.duoc.mpazneb.data.repository.ActividadRepository
import cl.duoc.mpazneb.data.repository.EscuelaRepository
import cl.duoc.mpazneb.data.repository.EstudianteRepository
import cl.duoc.mpazneb.data.repository.Sesion
import cl.duoc.mpazneb.data.repository.UsuarioRepository
import cl.duoc.mpazneb.model.Modulo
import cl.duoc.mpazneb.model.Perfil
import cl.duoc.mpazneb.nativo.Camara
import cl.duoc.mpazneb.nativo.Ubicacion
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/* ViewModels de las 4 pestañas del panel (y del panel mismo). Son cortos, por eso van juntos. */

/** El panel: quién ingresó y su perfil (la asistente no puede editar actividades). */
class PanelViewModel(application: Application) : BaseViewModel(application) {
    private val auth = UsuarioRepository(db)
    val alias: String = Sesion.usuario?.alias.orEmpty()
    val esDocente: Boolean = Sesion.usuario?.perfil == Perfil.DOCENTE
    fun cerrarSesion() = auth.cerrarSesion()
}

// ---------------- Pestaña Mi perfil (foto del docente) ----------------

class PerfilViewModel(application: Application) : BaseViewModel(application) {
    private val repository = UsuarioRepository(db)
    private val camara = Camara(application)

    /** El usuario con la sesión abierta, leído desde Room: al cambiar la foto, la pantalla se actualiza sola. */
    val usuario: StateFlow<UsuarioEntity?> = repository.observar(Sesion.usuario?.id ?: 0L)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), Sesion.usuario)

    /** CÁMARA: guarda la foto y su ruta en la tabla usuarios. */
    fun fotoTomada(foto: Bitmap) {
        val actual = usuario.value ?: return
        executeOperation { repository.cambiarFoto(actual, camara.guardarFoto(foto)) }
    }

    /** GALERÍA: copia la foto elegida (fuera del hilo principal, porque puede ser grande) y guarda su ruta. */
    fun fotoElegida(uri: Uri) {
        val actual = usuario.value ?: return
        executeOperation {
            val ruta = withContext(Dispatchers.IO) { camara.guardarDesdeGaleria(uri) }
            repository.cambiarFoto(actual, ruta)
        }
    }
}

// ---------------- Pestaña Actividades ----------------

/** Estado de la conexión con el microservicio (se muestra arriba de la lista). */
enum class Conexion { SINCRONIZANDO, CONECTADO, SIN_CONEXION }

data class ActividadesUiState(
    val actividades: List<ActividadEntity> = emptyList(),
    val modulo: Modulo? = null, // null = todos
    val curso: Int? = null,     // null = todos
    val conexion: Conexion = Conexion.SINCRONIZANDO
)

class ActividadesViewModel(application: Application) : BaseViewModel(application) {
    private val repository = ActividadRepository(db)
    private val modulo = MutableStateFlow<Modulo?>(null)
    private val curso = MutableStateFlow<Int?>(null)
    private val conexion = MutableStateFlow(Conexion.SINCRONIZANDO)

    /** Si cambia la tabla (Room), un filtro o la conexión, la lista se recalcula sola. */
    val uiState: StateFlow<ActividadesUiState> =
        combine(repository.observarTodas(), modulo, curso, conexion) { lista, m, c, estado ->
            ActividadesUiState(lista.filter { (m == null || it.modulo == m) && (c == null || it.curso == c) }, m, c, estado)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ActividadesUiState())

    init {
        sincronizar()
    }

    /**
     * MICROSERVICIO: trae las actividades y preguntas (GET) y actualiza Room.
     * Si no hay conexión, se siguen mostrando los datos guardados en el teléfono.
     */
    fun sincronizar() {
        conexion.value = Conexion.SINCRONIZANDO
        viewModelScope.launch {
            conexion.value = try {
                repository.sincronizar()
                Conexion.CONECTADO
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.w("MPAZ", "Sin conexión con el microservicio", e)
                Conexion.SIN_CONEXION
            }
        }
    }

    fun filtrarModulo(valor: Modulo?) { modulo.value = valor }
    fun filtrarCurso(valor: Int?) { curso.value = valor }
}

// ---------------- Pestaña Estudiantes ----------------

data class EstudiantesUiState(val estudiantes: List<EstudianteEntity> = emptyList(), val curso: Int? = null)

class EstudiantesViewModel(application: Application) : BaseViewModel(application) {
    private val repository = EstudianteRepository(db)
    private val curso = MutableStateFlow<Int?>(null)

    val uiState: StateFlow<EstudiantesUiState> =
        combine(repository.observarTodos(), curso) { lista, c ->
            EstudiantesUiState(lista.filter { c == null || it.curso == c }, c)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EstudiantesUiState())

    fun filtrarCurso(valor: Int?) { curso.value = valor }

    /** MICROSERVICIO estudiantes-service: al abrir la pestaña se traen los estudiantes (GET) y se guardan en Room. */
    init {
        viewModelScope.launch {
            try {
                repository.sincronizar()
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                android.util.Log.w("MPAZ", "Sin conexión con estudiantes-service", e)
            }
        }
    }
}

// ---------------- Pestaña Escuela (GPS) ----------------

data class EscuelaUiState(val escuela: EscuelaEntity = EscuelaEntity(), val buscando: Boolean = false, val mensaje: String? = null)

class EscuelaViewModel(application: Application) : BaseViewModel(application) {

    private val repository = EscuelaRepository(db)
    private val ubicacion = Ubicacion(application)

    private val local = MutableStateFlow(EscuelaUiState())

    val uiState: StateFlow<EscuelaUiState> =
        combine(repository.observar(), local) { escuela, l -> l.copy(escuela = escuela) }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), EscuelaUiState())

    /** GPS: guarda la ubicación actual como la ubicación de la escuela (el permiso ya fue concedido). */
    fun usarUbicacionActual() {
        if (!ubicacion.activada()) {
            local.update { it.copy(mensaje = "Activa la ubicación del dispositivo.") }
            return
        }
        local.update { it.copy(buscando = true, mensaje = null) }
        viewModelScope.launch {
            val lugar = ubicacion.obtener()
            if (lugar == null) {
                local.update { it.copy(buscando = false, mensaje = "No se pudo obtener la ubicación. Intenta de nuevo.") }
            } else {
                repository.guardarUbicacion(lugar.latitude, lugar.longitude)
                local.update { it.copy(buscando = false, mensaje = "Ubicación guardada ✓ (precisión ±${lugar.accuracy.toInt()} m)") }
            }
        }
    }

    fun permisoRechazado() = local.update { it.copy(mensaje = "Sin permiso de ubicación no se puede usar el GPS.") }

    fun cambiarRadio(metros: Int) {
        executeOperation { repository.cambiarRadio(metros) }
    }

    fun cambiarSoloEnEscuela(activo: Boolean) {
        executeOperation { repository.cambiarSoloEnEscuela(activo) }
    }
}
