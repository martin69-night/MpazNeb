package cl.duoc.mpazneb.viewmodel

import android.app.Application
import android.graphics.Bitmap
import cl.duoc.mpazneb.data.repository.EstudianteRepository
import cl.duoc.mpazneb.nativo.Camara
import cl.duoc.mpazneb.validacion.validarEstudiante
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class FormEstudianteUiState(
    val alias: String = "",
    val curso: Int? = null,
    val pin: String = "",
    val confirmacion: String = "",
    val fotoRuta: String? = null,
    val errores: Map<String, String> = emptyMap(),
    val guardado: Boolean = false
)

class FormEstudianteViewModel(application: Application) : BaseViewModel(application) {

    private val repository = EstudianteRepository(db)
    private val camara = Camara(application)

    private val _uiState = MutableStateFlow(FormEstudianteUiState())
    val uiState: StateFlow<FormEstudianteUiState> = _uiState.asStateFlow()

    fun actualizar(cambio: (FormEstudianteUiState) -> FormEstudianteUiState) {
        _uiState.update { anterior ->
            val nuevo = cambio(anterior)
            if (anterior.errores.isEmpty()) nuevo else nuevo.copy(errores = validar(nuevo))
        }
    }

    /** El PIN solo acepta números y máximo 4. */
    fun cambiarPin(valor: String) = actualizar { it.copy(pin = valor.filter { c -> c.isDigit() }.take(4)) }
    fun cambiarConfirmacion(valor: String) = actualizar { it.copy(confirmacion = valor.filter { c -> c.isDigit() }.take(4)) }

    /** CÁMARA: foto del dibujo del personaje (no de la cara del niño). */
    fun fotoTomada(foto: Bitmap) {
        val ruta = camara.guardarFoto(foto)
        _uiState.update { it.copy(fotoRuta = ruta) }
    }

    /**
     * 1) Validación local (apodo, curso, PIN de 4 números y confirmación).
     * 2) Validación contra la base de datos: no puede haber dos estudiantes con el mismo apodo en un curso.
     * 3) Se guarda en Room (el PIN como hash).
     */
    fun guardar() {
        val estado = _uiState.value
        val errores = validar(estado)
        _uiState.update { it.copy(errores = errores) }
        if (errores.isNotEmpty()) return

        executeOperation {
            val curso = estado.curso!!
            if (repository.aliasRepetido(estado.alias, curso)) {
                _uiState.update { it.copy(errores = mapOf("alias" to "Ya hay un/a ${estado.alias.trim()} en ese curso. Usa otro apodo.")) }
                return@executeOperation
            }
            repository.crear(estado.alias, curso, estado.pin, estado.fotoRuta)
            _uiState.update { it.copy(guardado = true) }
        }
    }

    private fun validar(s: FormEstudianteUiState) = validarEstudiante(s.alias, s.curso, s.pin, s.confirmacion)
}
