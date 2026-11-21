package cl.duoc.mpazneb.ui.screen

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mpazneb.model.Modulo
import cl.duoc.mpazneb.model.Paso
import cl.duoc.mpazneb.ui.components.Estrellas
import cl.duoc.mpazneb.ui.components.TextoVacio
import cl.duoc.mpazneb.ui.theme.Bloqueado
import cl.duoc.mpazneb.ui.theme.ColorLectura
import cl.duoc.mpazneb.ui.theme.ColorMatematica
import cl.duoc.mpazneb.ui.theme.Verde
import cl.duoc.mpazneb.viewmodel.MapaViewModel
import kotlinx.coroutines.launch

fun colorModulo(modulo: Modulo): Color = if (modulo == Modulo.LECTURA) ColorLectura else ColorMatematica

/** Pantalla principal del estudiante: su camino de actividades, sus estrellas y sus insignias. */
/**
 * Camino de una asignatura: TODAS las actividades disponibles del curso, en orden de dificultad.
 * Solo se puede abrir la primera sin terminar; las siguientes muestran un candado hasta terminar la anterior.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MapaScreen(
    onJugar: (estudianteId: Long, actividadId: Long) -> Unit,
    onVolver: () -> Unit,
    viewModel: MapaViewModel = viewModel()
) {
    val ui by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val color = colorModulo(viewModel.modulo)

    /** Estructura de control: decide si se puede abrir la actividad o por qué no. */
    fun tocarPaso(paso: Paso) {
        val aviso = when {
            !paso.desbloqueada -> "Primero termina la actividad anterior 🔒"
            paso.sinIntentos -> "Ya usaste tus ${paso.actividad.intentosPermitidos} intentos. ¡Bien hecho!"
            else -> null
        }
        if (aviso == null) onJugar(viewModel.estudianteId, paso.actividad.id)
        else scope.launch { snackbar.showSnackbar(aviso) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("${viewModel.modulo.emoji} ${viewModel.modulo.etiqueta}") },
                navigationIcon = {
                    IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver a mis asignaturas") }
                }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(padding)
        ) {
            item {
                Text(
                    if (ui.camino.isEmpty()) "" else "Llevas ${ui.terminadas} de ${ui.camino.size} actividades",
                    style = MaterialTheme.typography.titleMedium,
                    color = color
                )
            }
            if (ui.estudiante != null && ui.camino.isEmpty()) {
                item { TextoVacio("Todavía no hay actividades de ${viewModel.modulo.etiqueta} para tu curso.") }
            }
            itemsIndexed(ui.camino, key = { _, paso -> paso.actividad.id }) { i, paso ->
                NodoCamino(
                    paso = paso,
                    indice = i,
                    esSiguiente = i == ui.siguiente,
                    color = color,
                    onClick = { tocarPaso(paso) },
                    modifier = Modifier.animateItem()
                )
            }
        }
    }
}

/**
 * Un punto del camino. Los puntos van en zigzag para que parezca un sendero.
 * ANIMACIÓN: la próxima actividad por hacer "late" para llamar la atención.
 */
@Composable
private fun NodoCamino(paso: Paso, indice: Int, esSiguiente: Boolean, color: Color, onClick: () -> Unit, modifier: Modifier) {
    val latido by rememberInfiniteTransition(label = "latido").animateFloat(
        initialValue = 1f,
        targetValue = if (esSiguiente) 1.12f else 1f,
        animationSpec = infiniteRepeatable(tween(700), RepeatMode.Reverse),
        label = "escalaNodo"
    )
    val colorCirculo = when {
        !paso.desbloqueada -> Bloqueado
        paso.terminada -> Verde
        else -> color
    }
    val simbolo = when {
        !paso.desbloqueada -> "🔒"
        paso.terminada -> "✓"
        else -> "${indice + 1}"
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(start = if (indice % 2 == 0) 0.dp else 64.dp) // zigzag
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(76.dp)
                .graphicsLayer { scaleX = latido; scaleY = latido }
                .clip(CircleShape)
                .background(colorCirculo)
        ) {
            Text(simbolo, fontSize = 30.sp, color = Color.White, fontWeight = FontWeight.Bold)
        }
        Spacer(Modifier.width(16.dp))
        Column {
            Text(paso.actividad.nombre, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(paso.actividad.dificultad.etiqueta, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Estrellas(paso.estrellas, tamano = 22)
            if (esSiguiente) Text("¡Te toca!", color = color, fontWeight = FontWeight.Bold)
        }
    }
}
