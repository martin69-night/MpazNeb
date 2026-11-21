package cl.duoc.mpazneb.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Create
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mpazneb.model.etiquetaCurso
import cl.duoc.mpazneb.ui.components.Avatar
import cl.duoc.mpazneb.ui.components.BotonPrincipal
import cl.duoc.mpazneb.ui.components.CampoTexto
import cl.duoc.mpazneb.ui.components.Cargando
import cl.duoc.mpazneb.ui.components.DialogoCargando
import cl.duoc.mpazneb.ui.components.Estrellas
import cl.duoc.mpazneb.ui.components.TextoVacio
import cl.duoc.mpazneb.ui.theme.Naranjo
import cl.duoc.mpazneb.ui.theme.Verde
import cl.duoc.mpazneb.viewmodel.ProgresoViewModel
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val formatoFecha = SimpleDateFormat("dd-MM-yyyy HH:mm", Locale.forLanguageTag("es-CL"))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProgresoScreen(
    onVolver: () -> Unit,
    viewModel: ProgresoViewModel = viewModel()
) {
    val ui by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    DialogoCargando(isLoading)
    val observacion by viewModel.observacion.collectAsState()
    val errorObservacion by viewModel.errorObservacion.collectAsState()
    val guardada by viewModel.guardada.collectAsState()
    // Estado solo visual: qué intento está abierto para ver sus respuestas.
    var abierto by rememberSaveable { mutableStateOf<Long?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Progreso") },
                navigationIcon = { IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } }
            )
        }
    ) { padding ->
        val estudiante = ui.estudiante
        if (estudiante == null) {
            Cargando()
            return@Scaffold
        }
        LazyColumn(
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(padding)
        ) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Avatar(estudiante.alias, estudiante.fotoRuta, tamano = 72.dp)
                    Spacer(Modifier.width(16.dp))
                    Column {
                        Text(estudiante.alias, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
                        Text(etiquetaCurso(estudiante.curso))
                    }
                }
            }
            item {
                ElevatedCard(Modifier.fillMaxWidth()) {
                    Row(horizontalArrangement = Arrangement.SpaceEvenly, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Dato("Actividades", ui.actividadesTerminadas.toString())
                        Dato("Estrellas", "★ ${ui.estrellas}")
                        Dato("Intentos", ui.intentos.size.toString())
                    }
                }
            }
            item {
                Text(
                    "Información de apoyo para orientar la práctica. No es una calificación ni un diagnóstico.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            item { Text("Historial (toca un intento para ver sus respuestas)", style = MaterialTheme.typography.titleMedium) }
            if (ui.intentos.isEmpty()) item { TextoVacio("Todavía no ha hecho actividades.") }
            items(ui.intentos, key = { it.resultado.id }) { intento ->
                val r = intento.resultado
                Card(Modifier.fillMaxWidth().clickable { abierto = if (abierto == r.id) null else r.id }) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(intento.actividad, fontWeight = FontWeight.Bold)
                                Text("${formatoFecha.format(Date(r.fecha))} · ${r.aciertos} de ${r.total} correctas", style = MaterialTheme.typography.bodySmall)
                            }
                            Estrellas(r.estrellas())
                        }
                        // ANIMACIÓN: las respuestas se despliegan al tocar.
                        AnimatedVisibility(visible = abierto == r.id) {
                            Column(Modifier.padding(top = 8.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                intento.respuestas.forEach { resp ->
                                    Text(
                                        (if (resp.esCorrecta) "✓ " else "✗ ") + "${resp.enunciado} → ${resp.elegida}",
                                        color = if (resp.esCorrecta) Verde else Naranjo
                                    )
                                }
                            }
                        }
                    }
                }
            }
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Observación pedagógica (general y ficticia)", style = MaterialTheme.typography.titleMedium)
                    CampoTexto(observacion, viewModel::cambiarObservacion, "Observación", Icons.Filled.Create, errorObservacion, singleLine = false)
                    BotonPrincipal("Guardar observación", onClick = viewModel::guardarObservacion)
                    AnimatedVisibility(visible = guardada) { Text("Guardada en la base de datos ✓", color = Verde) }
                }
            }
        }
    }
}

@Composable
private fun Dato(titulo: String, valor: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(valor, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(titulo, style = MaterialTheme.typography.bodySmall)
    }
}
