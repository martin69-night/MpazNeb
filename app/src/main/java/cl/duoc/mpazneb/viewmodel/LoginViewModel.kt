package cl.duoc.mpazneb.viewmodel

import android.app.Application
import cl.duoc.mpazneb.data.repository.UsuarioRepository
import cl.duoc.mpazneb.validacion.validarLogin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

/** Todo lo que muestra la pantalla de ingreso de adultos. */
data class LoginUiState(
    val alias: String = "",
    val clave: String = "",
    val errores: Map<String, String> = emptyMap(),
    /** true cuando el ingreso fue correcto: la pantalla navega al panel. */
    val ingresoCorrecto: Boolean = false
)

class LoginViewModel(application: Application) : BaseViewModel(application) {

    private val auth = UsuarioRepository(db)

    // Patrón del curso: MutableStateFlow privado (solo el ViewModel lo cambia)
    // y StateFlow público de solo lectura (la pantalla solo lo observa).
    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    // Al escribir se borra el error de ese campo.
    fun cambiarAlias(valor: String) = _uiState.update { it.copy(alias = valor, errores = it.errores - "alias" - "general") }
    fun cambiarClave(valor: String) = _uiState.update { it.copy(clave = valor, errores = it.errores - "clave" - "general") }

    fun ingresar() {
        val estado = _uiState.value
        // 1) Validación local: campos vacíos o muy cortos.
        val errores = validarLogin(estado.alias, estado.clave)
        _uiState.update { it.copy(errores = errores) }
        if (errores.isNotEmpty()) return

        // 2) Validación contra la base de datos: ¿existe el usuario y coincide la contraseña?
        executeOperation {
            val usuario = auth.iniciarSesion(estado.alias, estado.clave)
            _uiState.update {
                if (usuario == null) it.copy(errores = mapOf("general" to "El usuario o la contraseña no son correctos."))
                else it.copy(ingresoCorrecto = true)
            }
        }
    }
}
