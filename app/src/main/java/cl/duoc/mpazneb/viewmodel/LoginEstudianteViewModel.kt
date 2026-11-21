package cl.duoc.mpazneb.viewmodel

import android.app.Application
import androidx.lifecycle.viewModelScope
import cl.duoc.mpazneb.data.local.entity.EstudianteEntity
import cl.duoc.mpazneb.data.repository.EstudianteRepository
import cl.duoc.mpazneb.data.repository.UsuarioRepository
import cl.duoc.mpazneb.validacion.pinValido
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/** El ingreso del estudiante tiene 3 pasos, pensados para niños que recién aprenden a leer. */
enum class PasoIngreso { CURSO, ESTUDIANTE, PIN }

data class LoginEstudianteUiState(
    val paso: PasoIngreso = PasoIngreso.CURSO,
    val curso: Int? = null,
    /** Estudiantes del curso elegido (vienen de la base de datos). */
    val estudiantes: List<EstudianteEntity> = emptyList(),
    val elegido: EstudianteEntity? = null,
    val pin: String = "",
    val error: String? = null,
    /** Aumenta con cada PIN incorrecto: la pantalla lo usa para animar la "sacudida". */
    val fallos: Int = 0,
    /** Cuando no es null, la pantalla navega al mapa de ese estudiante. */
    val ingresoId: Long? = null
)

class LoginEstudianteViewModel(application: Application) : BaseViewModel(application) {

    private val estudiantes = EstudianteRepository(db)
    private val auth = UsuarioRepository(db)

    private val estado = MutableStateFlow(LoginEstudianteUiState())

    /** Se combina el estado de la pantalla con la tabla de estudiantes y se filtra por el curso elegido. */
    val uiState: StateFlow<LoginEstudianteUiState> =
        combine(estado, estudiantes.observarTodos()) { s, todos ->
            s.copy(estudiantes = todos.filter { it.curso == s.curso })
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), LoginEstudianteUiState())

    fun elegirCurso(curso: Int) = estado.update { it.copy(curso = curso, paso = PasoIngreso.ESTUDIANTE) }

    fun elegirEstudiante(estudiante: EstudianteEntity) =
        estado.update { it.copy(elegido = estudiante, pin = "", error = null, paso = PasoIngreso.PIN) }

    /** Vuelve al paso anterior. Devuelve false si ya estaba en el primero (la pantalla sale). */
    fun volver(): Boolean {
        val actual = estado.value.paso
        if (actual == PasoIngreso.CURSO) return false
        estado.update { it.copy(paso = if (actual == PasoIngreso.PIN) PasoIngreso.ESTUDIANTE else PasoIngreso.CURSO, pin = "", error = null) }
        return true
    }

    fun borrarDigito() = estado.update { it.copy(pin = it.pin.dropLast(1), error = null) }

    /** Cada número del teclado. Al completar 4, se valida solo. */
    fun presionarDigito(digito: Char) {
        val actual = estado.value
        if (actual.pin.length >= 4) return
        val nuevo = actual.pin + digito
        estado.update { it.copy(pin = nuevo, error = null) }
        if (nuevo.length == 4) verificar(nuevo)
    }

    private fun verificar(pin: String) {
        val estudiante = estado.value.elegido ?: return
        // 1) Validación local: 4 números.
        if (!pinValido(pin)) {
            estado.update { it.copy(error = "El PIN tiene 4 números", pin = "", fallos = it.fallos + 1) }
            return
        }
        // 2) Validación contra la base de datos: ¿coincide con el PIN guardado?
        executeOperation {
            if (auth.pinCorrecto(estudiante.id, pin)) {
                estado.update { it.copy(ingresoId = estudiante.id) }
            } else {
                estado.update { it.copy(error = "Ese no es tu PIN. ¡Inténtalo otra vez!", pin = "", fallos = it.fallos + 1) }
            }
        }
    }
}
