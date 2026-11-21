package cl.duoc.mpazneb.ui.screen

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mpazneb.data.local.entity.UsuarioEntity
import cl.duoc.mpazneb.model.AvanceAsignatura
import cl.duoc.mpazneb.model.Logro
import cl.duoc.mpazneb.model.Modulo
import cl.duoc.mpazneb.model.etiquetaCurso
import cl.duoc.mpazneb.ui.components.Avatar
import cl.duoc.mpazneb.ui.components.Cargando
import cl.duoc.mpazneb.ui.components.rememberPedirUbicacion
import cl.duoc.mpazneb.ui.theme.Estrella
import cl.duoc.mpazneb.ui.theme.Naranjo
import cl.duoc.mpazneb.ui.theme.Verde
import cl.duoc.mpazneb.viewmodel.AsignaturasViewModel
import cl.duoc.mpazneb.viewmodel.EstadoUbicacion
import kotlinx.coroutines.launch

/**
 * Menú del estudiante después de ingresar con su PIN:
 * arriba su profesor/a con su foto, luego sus asignaturas con el avance de cada una y sus insignias.
 * Al tocar una asignatura se abre el camino con todas sus actividades disponibles.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AsignaturasScreen(
    onAsignatura: (estudianteId: Long, modulo: Modulo) -> Unit,
    onSalir: () -> Unit,
    viewModel: AsignaturasViewModel = viewModel()
) {
    val ui by viewModel.uiState.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // GPS: si la escuela exige practicar dentro, se piden los permisos y se revisa la ubicación.
    val pedirUbicacion = rememberPedirUbicacion { concedido ->
        if (concedido) viewModel.verificarUbicacion() else viewModel.permisoRechazado()
    }
    LaunchedEffect(ui.escuela.soloEnEscuela) {
        if (ui.escuela.soloEnEscuela && ui.ubicacion == EstadoUbicacion.PENDIENTE) pedirUbicacion()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Camino de Estrellas") },
                actions = { TextButton(onClick = onSalir) { Text("Salir") } }
            )
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        val estudiante = ui.estudiante
        if (estudiante == null) {
            Cargando()
            return@Scaffold
        }
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(16.dp)
        ) {
            // ---------- Profesor/a del curso ----------
            TarjetaDocente(ui.docente)

            // ---------- Saludo ----------
            Row(verticalAlignment = Alignment.CenterVertically) {
                Avatar(estudiante.alias, estudiante.fotoRuta, tamano = 64.dp)
                Spacer(Modifier.width(16.dp))
                Column(Modifier.weight(1f)) {
                    Text("¡Hola, ${estudiante.alias}!", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text(etiquetaCurso(estudiante.curso), color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("★ ${ui.estrellas}", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Estrella)
                    Text("estrellas", style = MaterialTheme.typography.labelSmall)
                }
            }

            // ---------- GPS ----------
            if (ui.escuela.soloEnEscuela) {
                TarjetaUbicacion(ui.ubicacion, ui.distancia, onReintentar = pedirUbicacion)
            }

            // ---------- Asignaturas ----------
            Text("Mis asignaturas", style = MaterialTheme.typography.titleLarge)
            ui.asignaturas.forEach { avance ->
                TarjetaAsignatura(avance) {
                    // Estructura de control: fuera de la escuela no se abre (si la escuela lo exige).
                    if (ui.puedePracticar) onAsignatura(estudiante.id, avance.modulo)
                    else scope.launch { snackbar.showSnackbar("Solo puedes practicar dentro de la escuela 🏫") }
                }
            }

            // ---------- Insignias ----------
            Text("Mis insignias", style = MaterialTheme.typography.titleLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
                ui.logros.forEach { TarjetaLogro(it) }
            }
        }
    }
}

/** Nombre y foto del profesor/a del curso. La foto la sube el docente desde su perfil. */
@Composable
private fun TarjetaDocente(docente: UsuarioEntity?) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
            if (docente == null) {
                Text("👩‍🏫 Tu curso todavía no tiene profesor/a asignado/a.", style = MaterialTheme.typography.bodyLarge)
            } else {
                Avatar(
                    alias = docente.nombre.substringAfterLast(' '),
                    fotoRuta = docente.fotoRuta,
                    tamano = 72.dp,
                    descripcion = "Foto de ${docente.nombre}"
                )
                Spacer(Modifier.width(16.dp))
                Column {
                    Text("Tu profesor/a", style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onPrimaryContainer)
                    Text(docente.nombre, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onPrimaryContainer)
                }
            }
        }
    }
}

/**
 * Tarjeta grande de una asignatura con su avance.
 * ANIMACIÓN: la barra de progreso se llena suavemente.
 */
@Composable
private fun TarjetaAsignatura(avance: AvanceAsignatura, onClick: () -> Unit) {
    val color = colorModulo(avance.modulo)
    val progreso by animateFloatAsState(avance.progreso, animationSpec = tween(800), label = "progresoAsignatura")
    Card(
        onClick = onClick,
        colors = CardDefaults.cardColors(containerColor = color),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(avance.modulo.emoji, fontSize = 40.sp)
                Spacer(Modifier.width(12.dp))
                Column(Modifier.weight(1f)) {
                    Text(avance.modulo.etiqueta, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White)
                    Text("${avance.terminadas} de ${avance.total} actividades", color = Color.White)
                }
                Text("★ ${avance.estrellas}", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
            }
            LinearProgressIndicator(
                progress = { progreso },
                color = Color.White,
                trackColor = Color.White.copy(alpha = 0.3f),
                modifier = Modifier.fillMaxWidth().height(8.dp)
            )
            val texto = when {
                avance.total == 0 -> "Pronto habrá actividades aquí."
                avance.siguiente == null -> "¡Terminaste todas! 🎉"
                else -> "Te toca: ${avance.siguiente}"
            }
            Text(texto, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun TarjetaUbicacion(estado: EstadoUbicacion, distancia: Int?, onReintentar: () -> Unit) {
    val (texto, color) = when (estado) {
        EstadoUbicacion.PENDIENTE, EstadoUbicacion.BUSCANDO -> "📍 Revisando si estás en la escuela…" to MaterialTheme.colorScheme.primary
        EstadoUbicacion.DENTRO -> "🏫 ¡Estás en la escuela! (a $distancia m)" to Verde
        EstadoUbicacion.FUERA -> "🏠 Estás fuera de la escuela (a $distancia m). Las actividades se abren en la escuela." to Naranjo
        EstadoUbicacion.SIN_PERMISO -> "📍 Necesitamos el permiso de ubicación para abrir las actividades." to Naranjo
        EstadoUbicacion.SIN_GPS -> "📍 No pudimos usar el GPS. Revisa que la ubicación esté activada." to Naranjo
    }
    Card(colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.12f)), border = BorderStroke(1.dp, color)) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
            Text(texto, modifier = Modifier.weight(1f))
            if (estado != EstadoUbicacion.DENTRO && estado != EstadoUbicacion.BUSCANDO) {
                TextButton(onClick = onReintentar) { Text("Revisar") }
            }
        }
    }
}

@Composable
private fun TarjetaLogro(logro: Logro) {
    Card(modifier = Modifier.width(120.dp).alpha(if (logro.ganado) 1f else 0.4f)) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(12.dp)) {
            Text(if (logro.ganado) logro.emoji else "❔", fontSize = 36.sp)
            Text(logro.nombre, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(Modifier.height(4.dp))
            Text(logro.descripcion, style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
        }
    }
}
