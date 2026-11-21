package cl.duoc.mpazneb.viewmodel

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import cl.duoc.mpazneb.data.local.entity.ActividadEntity
import cl.duoc.mpazneb.data.local.entity.PreguntaEntity
import cl.duoc.mpazneb.data.repository.ActividadRepository
import cl.duoc.mpazneb.data.repository.Sesion
import cl.duoc.mpazneb.model.EstadoActividad
import cl.duoc.mpazneb.model.Perfil
import cl.duoc.mpazneb.ui.navigation.DetalleActividadRuta
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

data class DetalleUiState(val actividad: ActividadEntity? = null, val preguntas: List<PreguntaEntity> = emptyList())

class DetalleActividadViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : BaseViewModel(application) {

    private val repository = ActividadRepository(db)

    /** El id llega como argumento de la ruta. */
    val actividadId = savedStateHandle.toRoute<DetalleActividadRuta>().id

    /** Solo la docente / UTP puede editar; la asistente solo mira (regla del caso). */
    val esDocente = Sesion.usuario?.perfil == Perfil.DOCENTE

    val uiState: StateFlow<DetalleUiState> =
        combine(repository.observar(actividadId), repository.observarPreguntas(actividadId)) { actividad, preguntas ->
            DetalleUiState(actividad, preguntas)
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), DetalleUiState())

    /** Mensaje para el Snackbar (null = no hay mensaje). */
    val mensaje = MutableStateFlow<String?>(null)

    fun cambiarEstado(nuevo: EstadoActividad) {
        val actividad = uiState.value.actividad ?: return
        executeOperation { mensaje.value = repository.cambiarEstado(actividad, nuevo) }
    }

    fun eliminarPregunta(pregunta: PreguntaEntity) {
        executeOperation { repository.eliminarPregunta(pregunta) }
    }
}
