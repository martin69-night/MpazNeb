package cl.duoc.mpazneb.ui.navigation

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.runtime.Composable
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import cl.duoc.mpazneb.ui.screen.AsignaturasScreen
import cl.duoc.mpazneb.ui.screen.DetalleActividadScreen
import cl.duoc.mpazneb.ui.screen.FormActividadScreen
import cl.duoc.mpazneb.ui.screen.FormEstudianteScreen
import cl.duoc.mpazneb.ui.screen.FormPreguntaScreen
import cl.duoc.mpazneb.ui.screen.LoginEstudianteScreen
import cl.duoc.mpazneb.ui.screen.LoginScreen
import cl.duoc.mpazneb.ui.screen.MapaScreen
import cl.duoc.mpazneb.ui.screen.PanelScreen
import cl.duoc.mpazneb.ui.screen.ProgresoScreen
import cl.duoc.mpazneb.ui.screen.SesionScreen
import kotlinx.serialization.Serializable

/* ---------- Rutas tipadas: el compilador revisa que se pasen los argumentos correctos ---------- */

@Serializable data object LoginRuta
@Serializable data object LoginEstudianteRuta
@Serializable data class AsignaturasRuta(val estudianteId: Long)
@Serializable data class MapaRuta(val estudianteId: Long, val modulo: String) // modulo = Modulo.name
@Serializable data class SesionRuta(val estudianteId: Long, val actividadId: Long)
@Serializable data object PanelRuta
@Serializable data class DetalleActividadRuta(val id: Long)
@Serializable data class FormActividadRuta(val id: Long? = null) // sin id = nueva
@Serializable data class FormPreguntaRuta(val actividadId: Long, val preguntaId: Long? = null)
@Serializable data object FormEstudianteRuta
@Serializable data class ProgresoRuta(val estudianteId: Long)

/**
 * Grafo de navegación de toda la app.
 * ANIMACIÓN: cada pantalla nueva entra deslizándose desde la derecha y al volver sale hacia la derecha.
 */
@Composable
fun MpazNavHost() {
    val nav = rememberNavController()

    NavHost(
        navController = nav,
        startDestination = LoginRuta,
        enterTransition = { slideInHorizontally(tween(300)) { it / 3 } + fadeIn(tween(300)) },
        exitTransition = { fadeOut(tween(150)) },
        popEnterTransition = { fadeIn(tween(300)) },
        popExitTransition = { slideOutHorizontally(tween(300)) { it / 3 } + fadeOut(tween(300)) }
    ) {
        composable<LoginRuta> {
            LoginScreen(
                onSoyEstudiante = { nav.navigate(LoginEstudianteRuta) },
                onIngresoAdulto = { nav.irSinVolver(PanelRuta) }
            )
        }

        // ----- Estudiante -----
        composable<LoginEstudianteRuta> {
            LoginEstudianteScreen(
                onVolver = { nav.volver() },
                onIngreso = { id -> nav.irSinVolver(AsignaturasRuta(id)) }
            )
        }
        composable<AsignaturasRuta> {
            AsignaturasScreen(
                onAsignatura = { estudianteId, modulo -> nav.navigate(MapaRuta(estudianteId, modulo.name)) },
                onSalir = { nav.irSinVolver(LoginRuta) }
            )
        }
        composable<MapaRuta> {
            MapaScreen(
                onJugar = { estudianteId, actividadId -> nav.navigate(SesionRuta(estudianteId, actividadId)) },
                onVolver = { nav.volver() }
            )
        }
        composable<SesionRuta> {
            SesionScreen(onSalir = { nav.volver() })
        }

        // ----- Docente / asistente -----
        composable<PanelRuta> {
            PanelScreen(
                onVerActividad = { id -> nav.navigate(DetalleActividadRuta(id)) },
                onNuevaActividad = { nav.navigate(FormActividadRuta()) },
                onVerEstudiante = { id -> nav.navigate(ProgresoRuta(id)) },
                onNuevoEstudiante = { nav.navigate(FormEstudianteRuta) },
                onSalir = { nav.irSinVolver(LoginRuta) }
            )
        }
        composable<DetalleActividadRuta> {
            DetalleActividadScreen(
                onVolver = { nav.volver() },
                onEditar = { id -> nav.navigate(FormActividadRuta(id)) },
                onNuevaPregunta = { id -> nav.navigate(FormPreguntaRuta(id)) },
                onEditarPregunta = { actividadId, preguntaId -> nav.navigate(FormPreguntaRuta(actividadId, preguntaId)) }
            )
        }
        composable<FormActividadRuta> { FormActividadScreen(onVolver = { nav.volver() }) }
        composable<FormPreguntaRuta> { FormPreguntaScreen(onVolver = { nav.volver() }) }
        composable<FormEstudianteRuta> { FormEstudianteScreen(onVolver = { nav.volver() }) }
        composable<ProgresoRuta> { ProgresoScreen(onVolver = { nav.volver() }) }
    }
}

/** Vuelve atrás solo si hay una pantalla anterior (evita quedar en blanco con doble toque). */
private fun NavController.volver() {
    if (previousBackStackEntry != null) popBackStack()
}

/** Navega borrando el historial: después de entrar o salir no se puede volver con "atrás". */
private fun NavController.irSinVolver(ruta: Any) {
    navigate(ruta) { popUpTo(graph.id) { inclusive = true } }
}
