package cl.duoc.mpazneb.viewmodel

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import cl.duoc.mpazneb.data.local.entity.PreguntaEntity
import cl.duoc.mpazneb.data.repository.ActividadRepository
import cl.duoc.mpazneb.ui.navigation.FormPreguntaRuta
import cl.duoc.mpazneb.validacion.validarPregunta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class FormPreguntaUiState(
    val enunciado: String = "",
    val alternativas: List<String> = listOf("", "", ""),
    val correcta: Int? = null,
    val retroalimentacion: String = "",
    val errores: Map<String, String> = emptyMap(),
    val esEdicion: Boolean = false,
    val guardado: Boolean = false
)

class FormPreguntaViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : BaseViewModel(application) {

    private val repository = ActividadRepository(db)

    private val ruta = savedStateHandle.toRoute<FormPreguntaRuta>()

    private val _uiState = MutableStateFlow(FormPreguntaUiState(esEdicion = ruta.preguntaId != null))
    val uiState: StateFlow<FormPreguntaUiState> = _uiState.asStateFlow()

    init {
        if (ruta.preguntaId != null) {
            viewModelScope.launch {
                val p = repository.obtenerPregunta(ruta.preguntaId) ?: return@launch
                _uiState.update {
                    it.copy(enunciado = p.enunciado, alternativas = p.alternativas(), correcta = p.correcta, retroalimentacion = p.retroalimentacion)
                }
            }
        }
    }

    fun actualizar(cambio: (FormPreguntaUiState) -> FormPreguntaUiState) {
        _uiState.update { anterior ->
            val nuevo = cambio(anterior)
            if (anterior.errores.isEmpty()) nuevo else nuevo.copy(errores = validar(nuevo))
        }
    }

    fun cambiarAlternativa(indice: Int, texto: String) = actualizar {
        it.copy(alternativas = it.alternativas.mapIndexed { i, actual -> if (i == indice) texto else actual })
    }

    fun guardar() {
        val estado = _uiState.value
        val errores = validar(estado)
        _uiState.update { it.copy(errores = errores) }
        if (errores.isNotEmpty()) return

        executeOperation {
            repository.guardarPregunta(
                PreguntaEntity(
                    id = ruta.preguntaId ?: 0L,
                    actividadId = ruta.actividadId,
                    enunciado = estado.enunciado.trim(),
                    alternativaA = estado.alternativas[0].trim(),
                    alternativaB = estado.alternativas[1].trim(),
                    alternativaC = estado.alternativas[2].trim(),
                    correcta = estado.correcta!!,
                    retroalimentacion = estado.retroalimentacion.trim()
                )
            )
            _uiState.update { it.copy(guardado = true) }
        }
    }

    private fun validar(s: FormPreguntaUiState) = validarPregunta(s.enunciado, s.alternativas, s.correcta, s.retroalimentacion)
}
