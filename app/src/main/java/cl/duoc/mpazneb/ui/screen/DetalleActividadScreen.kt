package cl.duoc.mpazneb.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mpazneb.data.local.entity.PreguntaEntity
import cl.duoc.mpazneb.model.EstadoActividad
import cl.duoc.mpazneb.model.etiquetaCurso
import cl.duoc.mpazneb.ui.components.BotonPrincipal
import cl.duoc.mpazneb.ui.components.Cargando
import cl.duoc.mpazneb.ui.components.ChipEstado
import cl.duoc.mpazneb.ui.components.DialogoCargando
import cl.duoc.mpazneb.ui.components.FotoGuardada
import cl.duoc.mpazneb.ui.components.TextoVacio
import cl.duoc.mpazneb.ui.theme.Verde
import cl.duoc.mpazneb.viewmodel.DetalleActividadViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetalleActividadScreen(
    onVolver: () -> Unit,
    onEditar: (Long) -> Unit,
    onNuevaPregunta: (Long) -> Unit,
    onEditarPregunta: (actividadId: Long, preguntaId: Long) -> Unit,
    viewModel: DetalleActividadViewModel = viewModel()
) {
    val ui by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    DialogoCargando(isLoading)
    val mensaje by viewModel.mensaje.collectAsState()
    val snackbar = remember { SnackbarHostState() }
    // Estado solo visual: qué pregunta se quiere eliminar (para el diálogo de confirmación).
    var porEliminar by remember { mutableStateOf<PreguntaEntity?>(null) }

    LaunchedEffect(mensaje) {
        mensaje?.let { snackbar.showSnackbar(it); viewModel.mensaje.value = null }
    }

    // Cuando el microservicio confirma que se eliminó, se vuelve al panel.
    val eliminada by viewModel.eliminada.collectAsState()
    LaunchedEffect(eliminada) { if (eliminada) onVolver() }
    var confirmarEliminar by remember { mutableStateOf(false) }

    if (confirmarEliminar) {
        AlertDialog(
            onDismissRequest = { confirmarEliminar = false },
            title = { Text("¿Eliminar actividad?") },
            text = { Text("Se eliminará del microservicio junto con sus preguntas.") },
            confirmButton = { TextButton(onClick = { viewModel.eliminarActividad(); confirmarEliminar = false }) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = { confirmarEliminar = false }) { Text("Cancelar") } }
        )
    }

    porEliminar?.let { pregunta ->
        AlertDialog(
            onDismissRequest = { porEliminar = null },
            title = { Text("¿Eliminar pregunta?") },
            text = { Text(pregunta.enunciado) },
            confirmButton = { TextButton(onClick = { viewModel.eliminarPregunta(pregunta); porEliminar = null }) { Text("Eliminar") } },
            dismissButton = { TextButton(onClick = { porEliminar = null }) { Text("Cancelar") } }
        )
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle de actividad") },
                navigationIcon = { IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } },
                actions = {
                    if (viewModel.esDocente) IconButton(onClick = { onEditar(viewModel.actividadId) }) { Icon(Icons.Filled.Edit, "Editar") }
                }
            )
        },
        floatingActionButton = {
            if (viewModel.esDocente) {
                ExtendedFloatingActionButton(
                    onClick = { onNuevaPregunta(viewModel.actividadId) },
                    icon = { Icon(Icons.Filled.Add, null) },
                    text = { Text("Agregar pregunta") }
                )
            }
        },
        snackbarHost = { SnackbarHost(snackbar) }
    ) { padding ->
        val actividad = ui.actividad
        if (actividad == null) {
            Cargando()
            return@Scaffold
        }
        LazyColumn(
            contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(padding)
        ) {
            item {
                Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                    Text("${actividad.modulo.emoji} ${actividad.nombre}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                    Text("${actividad.modulo.etiqueta} · ${etiquetaCurso(actividad.curso)} · ${actividad.dificultad.etiqueta} · ${actividad.intentosPermitidos} intentos")
                    ChipEstado(actividad.estado.etiqueta, colorEstado(actividad.estado))
                    actividad.fotoRuta?.let { FotoGuardada(it) }
                    Text("Objetivo: ${actividad.objetivo}")
                    Text("Instrucciones: ${actividad.instrucciones}")
                    actividad.textoLectura?.let { Card(Modifier.fillMaxWidth()) { Text(it, modifier = Modifier.padding(16.dp)) } }
                }
            }
            if (viewModel.esDocente) {
                item {
                    // Solo las actividades "Disponibles" aparecen en el camino de los estudiantes.
                    if (actividad.estado == EstadoActividad.DISPONIBLE) {
                        OutlinedButton(onClick = { viewModel.cambiarEstado(EstadoActividad.DESACTIVADA) }, modifier = Modifier.fillMaxWidth()) {
                            Text("Desactivar (los estudiantes dejan de verla)")
                        }
                    } else {
                        BotonPrincipal("Dejar disponible para los estudiantes", onClick = { viewModel.cambiarEstado(EstadoActividad.DISPONIBLE) })
                    }
                }
                // Solo los borradores se pueden eliminar (regla también validada en el microservicio).
                if (actividad.estado == EstadoActividad.BORRADOR) {
                    item {
                        OutlinedButton(onClick = { confirmarEliminar = true }, modifier = Modifier.fillMaxWidth()) {
                            Icon(Icons.Filled.Delete, null, tint = MaterialTheme.colorScheme.error)
                            Text("  Eliminar actividad", color = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            }
            item { Text("Preguntas (${ui.preguntas.size})", style = MaterialTheme.typography.titleLarge) }
            if (ui.preguntas.isEmpty()) item { TextoVacio("Todavía no hay preguntas.") }
            itemsIndexed(ui.preguntas, key = { _, p -> p.id }) { i, pregunta ->
                ElevatedCard(Modifier.fillMaxWidth().animateItem()) {
                    Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("${i + 1}. ${pregunta.enunciado}", style = MaterialTheme.typography.titleMedium, modifier = Modifier.weight(1f))
                            if (viewModel.esDocente) {
                                IconButton(onClick = { onEditarPregunta(actividad.id, pregunta.id) }) { Icon(Icons.Filled.Edit, "Editar pregunta") }
                                IconButton(onClick = { porEliminar = pregunta }) {
                                    Icon(Icons.Filled.Delete, "Eliminar pregunta", tint = MaterialTheme.colorScheme.error)
                                }
                            }
                        }
                        pregunta.alternativas().forEachIndexed { j, alternativa ->
                            val esCorrecta = j == pregunta.correcta
                            Text(
                                "${'A' + j}) $alternativa" + if (esCorrecta) "  ✓" else "",
                                color = if (esCorrecta) Verde else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (esCorrecta) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                        Text("Retroalimentación: ${pregunta.retroalimentacion}", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
