package cl.duoc.mpazneb.viewmodel

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import cl.duoc.mpazneb.data.local.entity.ActividadEntity
import cl.duoc.mpazneb.data.local.entity.EstudianteEntity
import cl.duoc.mpazneb.data.local.entity.PreguntaEntity
import cl.duoc.mpazneb.data.local.entity.RespuestaEntity
import cl.duoc.mpazneb.data.repository.ActividadRepository
import cl.duoc.mpazneb.data.repository.EstudianteRepository
import cl.duoc.mpazneb.nativo.Notificador
import cl.duoc.mpazneb.ui.navigation.SesionRuta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Etapas de una actividad. */
enum class Fase { INSTRUCCIONES, PREGUNTAS, RESUMEN }

data class SesionUiState(
    val estudiante: EstudianteEntity? = null,
    val actividad: ActividadEntity? = null,
    val preguntas: List<PreguntaEntity> = emptyList(),
    val intento: Int = 1,
    val fase: Fase = Fase.INSTRUCCIONES,
    val indice: Int = 0,
    val seleccion: Int? = null,
    /** true después de "Comprobar": se muestra la retroalimentación. */
    val comprobada: Boolean = false,
    /** Respuestas de este intento. Se guardan TODAS juntas en Room al enviar. */
    val respuestas: List<RespuestaEntity> = emptyList(),
    val confirmarEnvio: Boolean = false,
    val estrellasGanadas: Int = 0
) {
    val preguntaActual: PreguntaEntity? get() = preguntas.getOrNull(indice)
    val esCorrecta: Boolean get() = seleccion == preguntaActual?.correcta
    val esUltima: Boolean get() = indice == preguntas.lastIndex
    val aciertos: Int get() = respuestas.count { it.esCorrecta }
    val quedanIntentos: Boolean get() = intento < (actividad?.intentosPermitidos ?: 0)
}

class SesionViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : BaseViewModel(application) {

    private val actividades = ActividadRepository(db)
    private val estudiantes = EstudianteRepository(db)
    private val notificador = Notificador(application)

    private val ruta = savedStateHandle.toRoute<SesionRuta>()

    private val _uiState = MutableStateFlow(SesionUiState())
    val uiState: StateFlow<SesionUiState> = _uiState.asStateFlow()

    init {
        // Se carga todo desde la base de datos al abrir la actividad.
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    estudiante = estudiantes.obtener(ruta.estudianteId),
                    actividad = actividades.obtener(ruta.actividadId),
                    preguntas = actividades.obtenerPreguntas(ruta.actividadId),
                    intento = estudiantes.intentosUsados(ruta.estudianteId, ruta.actividadId) + 1
                )
            }
        }
    }

    fun comenzar() = _uiState.update {
        it.copy(fase = Fase.PREGUNTAS, indice = 0, seleccion = null, comprobada = false, respuestas = emptyList())
    }

    fun seleccionar(opcion: Int) {
        if (!_uiState.value.comprobada) _uiState.update { it.copy(seleccion = opcion) }
    }

    /** Revisa la respuesta y la agrega a la lista del intento. Muestra la retroalimentación inmediata. */
    fun comprobar() {
        val estado = _uiState.value
        val pregunta = estado.preguntaActual ?: return
        val elegida = estado.seleccion ?: return
        if (estado.comprobada) return
        val respuesta = RespuestaEntity(resultadoId = 0, preguntaId = pregunta.id, elegida = elegida, esCorrecta = elegida == pregunta.correcta)
        _uiState.update { it.copy(comprobada = true, respuestas = it.respuestas + respuesta) }
    }

    /** Siguiente pregunta, o pide confirmación si era la última. */
    fun siguiente() = _uiState.update {
        if (it.esUltima) it.copy(confirmarEnvio = true) else it.copy(indice = it.indice + 1, seleccion = null, comprobada = false)
    }

    fun cancelarEnvio() = _uiState.update { it.copy(confirmarEnvio = false) }

    /** Guarda el intento en Room (resultado + cada respuesta) y avisa con una notificación. */
    fun enviar() {
        val estado = _uiState.value
        _uiState.update { it.copy(confirmarEnvio = false) }
        executeOperation {
            val resultado = estudiantes.registrarIntento(ruta.estudianteId, ruta.actividadId, estado.respuestas)
            notificador.actividadTerminada(estado.estudiante?.alias.orEmpty(), estado.actividad?.nombre.orEmpty(), resultado.estrellas())
            _uiState.update { it.copy(fase = Fase.RESUMEN, estrellasGanadas = resultado.estrellas()) }
        }
    }

    fun reintentar() {
        if (!_uiState.value.quedanIntentos) return
        _uiState.update { it.copy(intento = it.intento + 1, fase = Fase.INSTRUCCIONES) }
    }
}
