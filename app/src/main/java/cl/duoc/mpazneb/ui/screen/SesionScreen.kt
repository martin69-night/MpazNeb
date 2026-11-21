package cl.duoc.mpazneb.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mpazneb.ui.components.BotonPrincipal
import cl.duoc.mpazneb.ui.components.Cargando
import cl.duoc.mpazneb.ui.components.DialogoCargando
import cl.duoc.mpazneb.ui.components.FotoGuardada
import cl.duoc.mpazneb.ui.theme.Bloqueado
import cl.duoc.mpazneb.ui.theme.Estrella
import cl.duoc.mpazneb.ui.theme.Naranjo
import cl.duoc.mpazneb.ui.theme.Verde
import cl.duoc.mpazneb.viewmodel.Fase
import cl.duoc.mpazneb.viewmodel.SesionUiState
import cl.duoc.mpazneb.viewmodel.SesionViewModel
import kotlinx.coroutines.delay

/**
 * Una actividad del estudiante. Pensada para 1.º a 4.º básico: textos grandes, botones grandes,
 * barra de avance, confirmación antes de enviar y retroalimentación amable.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SesionScreen(
    onSalir: () -> Unit,
    viewModel: SesionViewModel = viewModel()
) {
    val ui by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    DialogoCargando(isLoading, "Guardando tus estrellas…")
    var confirmarSalida by remember { mutableStateOf(false) }

    // "Atrás" pide confirmación en vez de salir de golpe.
    BackHandler(enabled = ui.fase != Fase.RESUMEN) { confirmarSalida = true }

    if (confirmarSalida) {
        AlertDialog(
            onDismissRequest = { confirmarSalida = false },
            title = { Text("¿Salir de la actividad?") },
            text = { Text("Lo que respondiste en este intento no se guardará.") },
            confirmButton = { TextButton(onClick = onSalir) { Text("Salir") } },
            dismissButton = { TextButton(onClick = { confirmarSalida = false }) { Text("Seguir jugando") } }
        )
    }
    if (ui.confirmarEnvio) {
        AlertDialog(
            onDismissRequest = viewModel::cancelarEnvio,
            title = { Text("¿Enviar tus respuestas?") },
            text = { Text("Respondiste todas las preguntas. ¿Quieres terminar?") },
            confirmButton = { TextButton(onClick = viewModel::enviar) { Text("Sí, terminar") } },
            dismissButton = { TextButton(onClick = viewModel::cancelarEnvio) { Text("Todavía no") } }
        )
    }

    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = { Text(ui.actividad?.let { "${it.modulo.emoji} ${it.nombre}" } ?: "") },
                navigationIcon = {
                    IconButton(onClick = { if (ui.fase == Fase.RESUMEN) onSalir() else confirmarSalida = true }) {
                        Icon(Icons.Filled.Close, contentDescription = "Salir")
                    }
                }
            )
        }
    ) { padding ->
        if (ui.actividad == null || ui.preguntas.isEmpty()) {
            Cargando()
            return@Scaffold
        }
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // ANIMACIÓN: cambio suave entre instrucciones, preguntas y resumen.
            AnimatedContent(
                targetState = ui.fase,
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "fase",
                modifier = Modifier.widthIn(max = 720.dp)
            ) { fase ->
                when (fase) {
                    Fase.INSTRUCCIONES -> Instrucciones(ui, viewModel::comenzar)
                    Fase.PREGUNTAS -> Preguntas(ui, viewModel::seleccionar, viewModel::comprobar, viewModel::siguiente)
                    Fase.RESUMEN -> Resumen(ui, viewModel::reintentar, onSalir)
                }
            }
        }
    }
}

@Composable
private fun Instrucciones(ui: SesionUiState, onComenzar: () -> Unit) {
    val actividad = ui.actividad ?: return
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("¡Vamos, ${ui.estudiante?.alias.orEmpty()}!", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text("Intento ${ui.intento} de ${actividad.intentosPermitidos} · ${ui.preguntas.size} preguntas")
        actividad.fotoRuta?.let { FotoGuardada(it) }
        Card(Modifier.fillMaxWidth()) {
            Text(actividad.instrucciones, style = MaterialTheme.typography.titleLarge, modifier = Modifier.padding(16.dp))
        }
        actividad.textoLectura?.let { texto ->
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                Text(texto, fontSize = 22.sp, lineHeight = 32.sp, modifier = Modifier.padding(20.dp))
            }
        }
        BotonPrincipal("¡Comenzar!", onClick = onComenzar)
    }
}

@Composable
private fun Preguntas(ui: SesionUiState, onSeleccionar: (Int) -> Unit, onComprobar: () -> Unit, onSiguiente: () -> Unit) {
    // ANIMACIÓN: la barra de avance se mueve suavemente.
    val avance by animateFloatAsState((ui.indice + if (ui.comprobada) 1 else 0).toFloat() / ui.preguntas.size, label = "avance")

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Text("Pregunta ${ui.indice + 1} de ${ui.preguntas.size}", fontWeight = FontWeight.Bold)
        LinearProgressIndicator(
            progress = { avance },
            color = Estrella,
            modifier = Modifier.fillMaxWidth().height(14.dp).clip(RoundedCornerShape(50))
        )

        // En Lectura el texto sigue visible, para volver a leerlo.
        ui.actividad?.textoLectura?.let { Text(it, fontSize = 18.sp, color = MaterialTheme.colorScheme.onSurfaceVariant) }

        // ANIMACIÓN: cada pregunta nueva entra deslizándose desde la derecha.
        AnimatedContent(
            targetState = ui.indice,
            transitionSpec = { slideInHorizontally { it } togetherWith slideOutHorizontally { -it } },
            label = "pregunta"
        ) { indice ->
            val pregunta = ui.preguntas[indice]
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(pregunta.enunciado, fontSize = 28.sp, lineHeight = 36.sp, fontWeight = FontWeight.Bold)
                pregunta.alternativas().forEachIndexed { i, alternativa ->
                    Opcion(
                        letra = 'A' + i,
                        texto = alternativa,
                        elegida = ui.seleccion == i,
                        comprobada = ui.comprobada,
                        correcta = ui.esCorrecta,
                        onClick = { onSeleccionar(i) }
                    )
                }
                // ANIMACIÓN: la retroalimentación aparece con un rebote.
                AnimatedVisibility(visible = ui.comprobada, enter = scaleIn(spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn()) {
                    val color = if (ui.esCorrecta) Verde else Naranjo
                    Card(colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.15f)), modifier = Modifier.fillMaxWidth()) {
                        Column(Modifier.padding(16.dp)) {
                            // Respetuoso: nunca "incorrecto", sino "¡Casi! Revisemos juntos".
                            Text(if (ui.esCorrecta) "🎉 ¡Muy bien!" else "💡 ¡Casi! Revisemos juntos", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = color)
                            Text(pregunta.retroalimentacion, fontSize = 18.sp)
                        }
                    }
                }
            }
        }

        if (!ui.comprobada) {
            BotonPrincipal("Comprobar", onClick = onComprobar, habilitado = ui.seleccion != null)
        } else {
            BotonPrincipal(if (ui.esUltima) "Terminar" else "Siguiente →", onClick = onSiguiente)
        }
    }
}

/** Alternativa grande y táctil. ANIMACIÓN: su color cambia suavemente según el estado. */
@Composable
private fun Opcion(letra: Char, texto: String, elegida: Boolean, comprobada: Boolean, correcta: Boolean, onClick: () -> Unit) {
    val colorObjetivo = when {
        !elegida -> MaterialTheme.colorScheme.outline
        !comprobada -> MaterialTheme.colorScheme.primary
        correcta -> Verde
        else -> Naranjo
    }
    val color by animateColorAsState(colorObjetivo, label = "colorOpcion")
    val fondo = if (elegida) color.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surface
    Card(
        onClick = onClick,
        enabled = !comprobada,
        border = BorderStroke(if (elegida) 4.dp else 2.dp, color),
        colors = CardDefaults.cardColors(
            containerColor = fondo,
            disabledContainerColor = fondo,
            disabledContentColor = MaterialTheme.colorScheme.onSurface
        ),
        modifier = Modifier.fillMaxWidth().heightIn(min = 72.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            Text("$letra", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = color)
            Text("   $texto", fontSize = 24.sp)
        }
    }
}

@Composable
private fun Resumen(ui: SesionUiState, onReintentar: () -> Unit, onSalir: () -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
        Text(
            when (ui.estrellasGanadas) {
                3 -> "¡Excelente!"
                2 -> "¡Muy bien!"
                else -> "¡Buen esfuerzo!"
            },
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold
        )
        // ANIMACIÓN: las estrellas aparecen una por una con rebote.
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(3) { i -> EstrellaAnimada(ganada = i < ui.estrellasGanadas, retraso = i * 350L) }
        }
        Text("Respondiste bien ${ui.aciertos} de ${ui.preguntas.size} preguntas.", fontSize = 20.sp, textAlign = TextAlign.Center)
        Text(
            "Las estrellas son para practicar, no son una nota. ¡Cada intento te ayuda a aprender!",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        BotonPrincipal("Volver a mi camino", onClick = onSalir)
        if (ui.quedanIntentos) {
            OutlinedButton(onClick = onReintentar, modifier = Modifier.fillMaxWidth().heightIn(min = 56.dp)) { Text("Intentar de nuevo") }
        }
    }
}

@Composable
private fun EstrellaAnimada(ganada: Boolean, retraso: Long) {
    val escala = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        delay(retraso)
        escala.animateTo(1f, spring(dampingRatio = 0.35f, stiffness = Spring.StiffnessLow))
    }
    Text(
        "★",
        fontSize = 80.sp,
        color = if (ganada) Estrella else Bloqueado,
        modifier = Modifier.graphicsLayer { scaleX = escala.value; scaleY = escala.value }
    )
}
