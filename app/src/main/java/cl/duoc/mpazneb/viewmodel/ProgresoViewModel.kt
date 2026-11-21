package cl.duoc.mpazneb.viewmodel

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import cl.duoc.mpazneb.data.local.entity.EstudianteEntity
import cl.duoc.mpazneb.data.local.entity.ResultadoEntity
import cl.duoc.mpazneb.data.repository.ActividadRepository
import cl.duoc.mpazneb.data.repository.EstudianteRepository
import cl.duoc.mpazneb.model.totalEstrellas
import cl.duoc.mpazneb.ui.navigation.ProgresoRuta
import cl.duoc.mpazneb.validacion.validarObservacion
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/** Una respuesta lista para mostrar. */
data class RespuestaVista(val enunciado: String, val elegida: String, val esCorrecta: Boolean)

/** Un intento terminado, con el nombre de la actividad y sus respuestas. */
data class IntentoVista(val resultado: ResultadoEntity, val actividad: String, val respuestas: List<RespuestaVista>)

data class ProgresoUiState(
    val estudiante: EstudianteEntity? = null,
    val intentos: List<IntentoVista> = emptyList(),
    val estrellas: Int = 0,
    val actividadesTerminadas: Int = 0
)

class ProgresoViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : BaseViewModel(application) {

    private val estudiantes = EstudianteRepository(db)
    private val actividades = ActividadRepository(db)

    private val estudianteId = savedStateHandle.toRoute<ProgresoRuta>().estudianteId

    /** Une 5 tablas (estudiante, resultados, respuestas, actividades y preguntas) para armar el historial. */
    val uiState: StateFlow<ProgresoUiState> = combine(
        estudiantes.observar(estudianteId),
        estudiantes.observarResultados(estudianteId),
        estudiantes.observarRespuestas(estudianteId),
        actividades.observarTodas(),
        actividades.observarTodasLasPreguntas()
    ) { estudiante, resultados, respuestas, todas, preguntas ->
        val nombreActividad = todas.associate { it.id to it.nombre }
        val preguntaPorId = preguntas.associateBy { it.id }
        val respuestasPorIntento = respuestas.groupBy { it.resultadoId }
        ProgresoUiState(
            estudiante = estudiante,
            intentos = resultados.map { r ->
                IntentoVista(
                    resultado = r,
                    actividad = nombreActividad[r.actividadId] ?: "Actividad eliminada",
                    respuestas = respuestasPorIntento[r.id].orEmpty().map { resp ->
                        val p = preguntaPorId[resp.preguntaId]
                        RespuestaVista(
                            enunciado = p?.enunciado ?: "Pregunta eliminada",
                            elegida = p?.alternativas()?.getOrNull(resp.elegida) ?: "-",
                            esCorrecta = resp.esCorrecta
                        )
                    }
                )
            },
            estrellas = totalEstrellas(resultados),
            actividadesTerminadas = resultados.map { it.actividadId }.toSet().size
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), ProgresoUiState())

    // ----- Formulario de observación pedagógica -----
    val observacion = MutableStateFlow("")
    val errorObservacion = MutableStateFlow<String?>(null)
    val guardada = MutableStateFlow(false)

    init {
        viewModelScope.launch { observacion.value = estudiantes.obtener(estudianteId)?.observacion.orEmpty() }
    }

    fun cambiarObservacion(texto: String) {
        observacion.value = texto
        errorObservacion.value = null
        guardada.value = false
    }

    /** Valida y guarda la observación en la tabla estudiantes. */
    fun guardarObservacion() {
        val error = validarObservacion(observacion.value)["observacion"]
        errorObservacion.value = error
        if (error != null) return
        executeOperation {
            estudiantes.guardarObservacion(estudianteId, observacion.value)
            guardada.value = true
        }
    }
}
