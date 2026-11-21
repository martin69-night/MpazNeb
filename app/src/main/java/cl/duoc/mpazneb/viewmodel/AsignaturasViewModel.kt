package cl.duoc.mpazneb.viewmodel

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import cl.duoc.mpazneb.data.local.entity.EscuelaEntity
import cl.duoc.mpazneb.data.local.entity.EstudianteEntity
import cl.duoc.mpazneb.data.local.entity.UsuarioEntity
import cl.duoc.mpazneb.data.repository.ActividadRepository
import cl.duoc.mpazneb.data.repository.EscuelaRepository
import cl.duoc.mpazneb.data.repository.EstudianteRepository
import cl.duoc.mpazneb.data.repository.UsuarioRepository
import cl.duoc.mpazneb.model.AvanceAsignatura
import cl.duoc.mpazneb.model.Logro
import cl.duoc.mpazneb.model.Modulo
import cl.duoc.mpazneb.model.armarCamino
import cl.duoc.mpazneb.model.avanceAsignatura
import cl.duoc.mpazneb.model.calcularLogros
import cl.duoc.mpazneb.model.totalEstrellas
import cl.duoc.mpazneb.nativo.Ubicacion
import cl.duoc.mpazneb.ui.navigation.AsignaturasRuta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Resultado de revisar con el GPS si el estudiante está en la escuela. */
enum class EstadoUbicacion { PENDIENTE, BUSCANDO, DENTRO, FUERA, SIN_PERMISO, SIN_GPS }

/** Lo que cambia solo en esta pantalla (no viene de la base de datos): el resultado del GPS. */
private data class EstadoGps(
    val ubicacion: EstadoUbicacion = EstadoUbicacion.PENDIENTE,
    val distancia: Int? = null
)

/** Todo lo que muestra el menú del estudiante. */
data class AsignaturasUiState(
    val estudiante: EstudianteEntity? = null,
    /** Profesor/a del curso del estudiante (null si el curso no tiene uno asignado). */
    val docente: UsuarioEntity? = null,
    val asignaturas: List<AvanceAsignatura> = emptyList(),
    val estrellas: Int = 0,
    val logros: List<Logro> = emptyList(),
    val escuela: EscuelaEntity = EscuelaEntity(),
    val ubicacion: EstadoUbicacion = EstadoUbicacion.PENDIENTE,
    val distancia: Int? = null
) {
    /** Si la escuela exige estar dentro, solo se practica cuando el GPS lo confirma. */
    val puedePracticar: Boolean get() = !escuela.soloEnEscuela || ubicacion == EstadoUbicacion.DENTRO
}

class AsignaturasViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : BaseViewModel(application) {

    private val estudiantes = EstudianteRepository(db)
    private val actividades = ActividadRepository(db)
    private val usuarios = UsuarioRepository(db)
    private val escuela = EscuelaRepository(db)
    private val ubicacion = Ubicacion(application)

    val estudianteId = savedStateHandle.toRoute<AsignaturasRuta>().estudianteId
    private val gps = MutableStateFlow(EstadoGps())

    /** La configuración de la escuela (Room) junto con el resultado del GPS. */
    private val escuelaYGps = combine(escuela.observar(), gps) { config, g -> config to g }

    /**
     * combine junta 5 fuentes: el estudiante, su profesor/a, las actividades y sus resultados (Room)
     * más la escuela con el GPS. Si cualquiera cambia (por ejemplo, la docente cambia su foto
     * o el estudiante termina una actividad), el menú se recalcula solo.
     */
    val uiState: StateFlow<AsignaturasUiState> = combine(
        estudiantes.observar(estudianteId),
        usuarios.observarDocenteDe(estudianteId),
        actividades.observarTodas(),
        estudiantes.observarResultados(estudianteId),
        escuelaYGps
    ) { estudiante, docente, todas, resultados, (config, g) ->
        AsignaturasUiState(
            estudiante = estudiante,
            docente = docente,
            asignaturas = if (estudiante == null) emptyList() else Modulo.entries.map { modulo ->
                avanceAsignatura(modulo, armarCamino(todas, resultados, estudiante.curso, modulo))
            },
            estrellas = totalEstrellas(resultados),
            logros = calcularLogros(resultados, todas),
            escuela = config,
            ubicacion = g.ubicacion,
            distancia = g.distancia
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), AsignaturasUiState())

    fun permisoRechazado() = gps.update { it.copy(ubicacion = EstadoUbicacion.SIN_PERMISO) }

    /** GPS: se llama cuando el permiso de ubicación ya está concedido. */
    fun verificarUbicacion() {
        if (!ubicacion.activada()) {
            gps.update { it.copy(ubicacion = EstadoUbicacion.SIN_GPS) }
            return
        }
        gps.update { it.copy(ubicacion = EstadoUbicacion.BUSCANDO) }
        viewModelScope.launch {
            val lugar = ubicacion.obtener()
            val config = uiState.value.escuela
            val latEscuela = config.latitud
            val lonEscuela = config.longitud
            if (lugar == null || latEscuela == null || lonEscuela == null) {
                gps.update { it.copy(ubicacion = EstadoUbicacion.SIN_GPS) }
                return@launch
            }
            val metros = Ubicacion.distancia(lugar.latitude, lugar.longitude, latEscuela, lonEscuela)
            gps.update {
                it.copy(
                    ubicacion = if (metros <= config.radioMetros) EstadoUbicacion.DENTRO else EstadoUbicacion.FUERA,
                    distancia = metros
                )
            }
        }
    }
}
