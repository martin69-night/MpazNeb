package cl.duoc.mpazneb.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.mpazneb.data.local.AppDatabase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * Clase base de los ViewModels de la app (mismo patrón visto en clase con "My Tasks").
 * - Hereda de AndroidViewModel para tener el contexto de la aplicación (base de datos, cámara, GPS).
 * - Expone [isLoading] para mostrar el diálogo de carga mientras se escribe en la base de datos.
 * - [executeOperation] ejecuta una operación asíncrona con control de concurrencia y manejo de errores.
 */
abstract class BaseViewModel(application: Application) : AndroidViewModel(application) {

    /** Instancia ÚNICA de la base de datos Room (ver AppDatabase.obtener). */
    protected val db: AppDatabase = AppDatabase.obtener(application)

    // Estado mutable privado para controlar si hay una operación en curso
    private val _isLoading = MutableStateFlow(false)
    // StateFlow inmutable expuesto a la UI para observar el estado de carga
    val isLoading = _isLoading.asStateFlow()

    /**
     * Función de orden superior para ejecutar operaciones asíncronas con control de concurrencia
     * y manejo automático del estado de carga (isLoading).
     */
    protected fun executeOperation(operation: suspend () -> Unit) {
        viewModelScope.launch {
            // Guardia de concurrencia: si ya hay una operación en curso, se ignora la solicitud
            if (_isLoading.value) return@launch

            // Activa el indicador de carga
            _isLoading.value = true
            try {
                operation()
            } catch (e: Exception) {
                // Si algo falla al guardar, la app no se cierra: se registra el error en Logcat
                android.util.Log.e("MPAZ", "Error en operación de base de datos", e)
            } finally {
                // Garantiza que el indicador de carga se desactive al finalizar (éxito o error)
                _isLoading.value = false
            }
        }
    }
}
