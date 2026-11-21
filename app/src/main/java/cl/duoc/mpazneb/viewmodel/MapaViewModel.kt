package cl.duoc.mpazneb.viewmodel

import android.app.Application
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import cl.duoc.mpazneb.data.local.entity.EstudianteEntity
import cl.duoc.mpazneb.data.repository.ActividadRepository
import cl.duoc.mpazneb.data.repository.EstudianteRepository
import cl.duoc.mpazneb.model.Modulo
import cl.duoc.mpazneb.model.Paso
import cl.duoc.mpazneb.model.armarCamino
import cl.duoc.mpazneb.ui.navigation.MapaRuta
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

/** El camino de una asignatura: todas sus actividades disponibles, en orden y con candado. */
data class MapaUiState(
    val estudiante: EstudianteEntity? = null,
    val camino: List<Paso> = emptyList()
) {
    /** Índice de la actividad que "le toca" (la primera desbloqueada sin terminar), o -1. */
    val siguiente: Int get() = camino.indexOfFirst { it.desbloqueada && !it.terminada }
    val terminadas: Int get() = camino.count { it.terminada }
}

class MapaViewModel(
    application: Application,
    savedStateHandle: SavedStateHandle
) : BaseViewModel(application) {

    private val estudiantes = EstudianteRepository(db)
    private val actividades = ActividadRepository(db)

    // La ruta trae el estudiante y la asignatura que tocó en el menú.
    private val ruta = savedStateHandle.toRoute<MapaRuta>()
    val estudianteId = ruta.estudianteId
    val modulo: Modulo = Modulo.valueOf(ruta.modulo)

    /**
     * combine junta 3 fuentes de Room: el estudiante, las actividades y sus resultados.
     * Al terminar una actividad se guarda un resultado y el camino se recalcula solo (se abre la siguiente).
     */
    val uiState: StateFlow<MapaUiState> = combine(
        estudiantes.observar(estudianteId),
        actividades.observarTodas(),
        estudiantes.observarResultados(estudianteId)
    ) { estudiante, todas, resultados ->
        MapaUiState(
            estudiante = estudiante,
            camino = if (estudiante == null) emptyList() else armarCamino(todas, resultados, estudiante.curso, modulo)
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), MapaUiState())
}
