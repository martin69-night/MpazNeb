package cl.duoc.mpazneb.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.mpazneb.data.model.Feriado
import cl.duoc.mpazneb.data.repository.FeriadoRepository
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Los 3 estados posibles de la tarjeta de feriados. */
sealed interface FeriadosUiState {
    data object Cargando : FeriadosUiState
    data class Listo(val feriados: List<Feriado>) : FeriadosUiState
    data class Error(val mensaje: String) : FeriadosUiState
}

/**
 * ViewModel que mantiene el estado de los feriados obtenidos de la API EXTERNA (no usa Room ni el microservicio).
 * El repositorio llega por el constructor (con valor por defecto) para poder reemplazarlo en las pruebas.
 */
open class FeriadosViewModel(
    private val repository: FeriadoRepository = FeriadoRepository()
) : ViewModel() {

    // Flujo mutable que contiene el estado de la tarjeta
    protected val _uiState = MutableStateFlow<FeriadosUiState>(FeriadosUiState.Cargando)

    // Flujo público de solo lectura
    open val uiState: StateFlow<FeriadosUiState> = _uiState

    // Se llama automáticamente al iniciar
    init {
        fetchFeriados()
    }

    // Función que obtiene los datos en segundo plano
    open fun fetchFeriados() {
        _uiState.value = FeriadosUiState.Cargando
        viewModelScope.launch {
            _uiState.value = try {
                FeriadosUiState.Listo(repository.getProximosFeriados())
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                println("Error al obtener feriados: ${e.localizedMessage}")
                FeriadosUiState.Error("No se pudieron cargar los feriados. Revisa tu conexión a internet.")
            }
        }
    }
}
