package cl.duoc.mpazneb.viewmodel

import android.app.Application
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import cl.duoc.mpazneb.data.local.AppDatabase
import cl.duoc.mpazneb.data.remote.mensajeDeError
import kotlinx.coroutines.CancellationException
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
            } catch (e: CancellationException) {
                throw e // se salió de la pantalla: no es un error
            } catch (e: Exception) {
                // Si algo falla (microservicio apagado, error 409, etc.) la app no se cierra:
                // se registra en Logcat y se avisa a la persona con un mensaje claro.
                android.util.Log.e("MPAZ", "Error en la operación", e)
                Toast.makeText(getApplication(), mensajeDeError(e), Toast.LENGTH_LONG).show()
            } finally {
                // Garantiza que el indicador de carga se desactive al finalizar (éxito o error)
                _isLoading.value = false
            }
        }
    }
}
