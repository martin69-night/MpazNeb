package cl.duoc.mpazneb.viewmodel

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import cl.duoc.mpazneb.data.local.entity.ActividadEntity
import cl.duoc.mpazneb.data.repository.ActividadRepository
import cl.duoc.mpazneb.model.Dificultad
import cl.duoc.mpazneb.model.EstadoActividad
import cl.duoc.mpazneb.model.Modulo
import cl.duoc.mpazneb.nativo.Camara
import cl.duoc.mpazneb.ui.navigation.FormActividadRuta
import cl.duoc.mpazneb.validacion.validarActividad
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FormActividadUiState(
    val nombre: String = "",
    val modulo: Modulo? = null,
    val curso: Int? = null,
    val dificultad: Dificultad? = null,
    val objetivo: String = "",
    val instrucciones: String = "",
    val intentos: String = "3",
    val textoLectura: String = "",
    val fotoRuta: String? = null,
    val errores: Map<String, String> = emptyMap(),
    val esEdicion: Boolean = false,
    val guardado: Boolean = false
)

class FormActividadViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : BaseViewModel(application) {

    private val repository = ActividadRepository(db)
    private val camara = Camara(application)

    private val idEdicion = savedStateHandle.toRoute<FormActividadRuta>().id
    private var estadoOriginal = EstadoActividad.BORRADOR

    private val _uiState = MutableStateFlow(FormActividadUiState(esEdicion = idEdicion != null))
    val uiState: StateFlow<FormActividadUiState> = _uiState.asStateFlow()

    init {
        // Edición: se leen los datos guardados en Room y se ponen en el formulario.
        if (idEdicion != null) {
            viewModelScope.launch {
                val a = repository.obtener(idEdicion) ?: return@launch
                estadoOriginal = a.estado
                _uiState.update {
                    it.copy(
                        nombre = a.nombre, modulo = a.modulo, curso = a.curso, dificultad = a.dificultad,
                        objetivo = a.objetivo, instrucciones = a.instrucciones, intentos = a.intentosPermitidos.toString(),
                        textoLectura = a.textoLectura.orEmpty(), fotoRuta = a.fotoRuta
                    )
                }
            }
        }
    }

    /**
     * Aplica un cambio del usuario (state hoisting: la pantalla no guarda el texto, lo guarda el ViewModel).
     * Si ya hubo errores, se valida en vivo para que el error desaparezca apenas el campo quede bien.
     */
    fun actualizar(cambio: (FormActividadUiState) -> FormActividadUiState) {
        _uiState.update { anterior ->
            val nuevo = cambio(anterior)
            if (anterior.errores.isEmpty()) nuevo else nuevo.copy(errores = validar(nuevo))
        }
    }

    /** CÁMARA: guarda la foto en el almacenamiento interno y su ruta en el formulario. */
    fun fotoTomada(foto: Bitmap) {
        val ruta = camara.guardarFoto(foto)
        _uiState.update { it.copy(fotoRuta = ruta) }
    }

    /**
     * Guardar tiene 3 pasos:
     * 1) validación local (campos vacíos, largos, rangos),
     * 2) validación contra la base de datos (el nombre no puede repetirse),
     * 3) guardar en Room y volver.
     */
    fun guardar() {
        val estado = _uiState.value
        val errores = validar(estado)
        _uiState.update { it.copy(errores = errores) }
        if (errores.isNotEmpty()) return

        executeOperation {
            if (repository.nombreRepetido(estado.nombre, idEdicion ?: 0L)) {
                _uiState.update { it.copy(errores = mapOf("nombre" to "Ya existe una actividad con ese nombre")) }
                return@executeOperation
            }
            repository.guardar(
                ActividadEntity(
                    id = idEdicion ?: 0L,
                    nombre = estado.nombre.trim(),
                    modulo = estado.modulo!!,
                    curso = estado.curso!!,
                    dificultad = estado.dificultad!!,
                    objetivo = estado.objetivo.trim(),
                    instrucciones = estado.instrucciones.trim(),
                    intentosPermitidos = estado.intentos.toInt(),
                    estado = estadoOriginal,
                    textoLectura = if (estado.modulo == Modulo.LECTURA) estado.textoLectura.trim() else null,
                    fotoRuta = estado.fotoRuta
                )
            )
            _uiState.update { it.copy(guardado = true) }
        }
    }

    private fun validar(s: FormActividadUiState) = validarActividad(
        s.nombre, s.modulo, s.curso, s.dificultad, s.objetivo, s.instrucciones, s.intentos,
        esLectura = s.modulo == Modulo.LECTURA, textoLectura = s.textoLectura
    )
}
