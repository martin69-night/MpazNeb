package cl.duoc.mpazneb.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.ThumbUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mpazneb.ui.components.BotonPrincipal
import cl.duoc.mpazneb.ui.components.CampoTexto
import cl.duoc.mpazneb.ui.components.DialogoCargando
import cl.duoc.mpazneb.viewmodel.FormPreguntaViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormPreguntaScreen(
    onVolver: () -> Unit,
    viewModel: FormPreguntaViewModel = viewModel()
) {
    val ui by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    DialogoCargando(isLoading)

    LaunchedEffect(ui.guardado) { if (ui.guardado) onVolver() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (ui.esEdicion) "Editar pregunta" else "Nueva pregunta") },
                navigationIcon = { IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } }
            )
        }
    ) { padding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            CampoTexto(ui.enunciado, { v -> viewModel.actualizar { it.copy(enunciado = v) } }, "Pregunta o ejercicio", Icons.Filled.Create, ui.errores["enunciado"], singleLine = false)

            Text("Alternativas (marca la correcta)", style = MaterialTheme.typography.labelLarge)
            ui.alternativas.forEachIndexed { i, texto ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    RadioButton(selected = ui.correcta == i, onClick = { viewModel.actualizar { it.copy(correcta = i) } })
                    CampoTexto(texto, { v -> viewModel.cambiarAlternativa(i, v) }, "Alternativa ${'A' + i}", Icons.AutoMirrored.Filled.List, ui.errores["alternativa$i"])
                }
            }
            listOfNotNull(ui.errores["alternativas"], ui.errores["correcta"]).forEach {
                Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
            }

            CampoTexto(
                ui.retroalimentacion, { v -> viewModel.actualizar { it.copy(retroalimentacion = v) } },
                "Retroalimentación (breve y amable)", Icons.Filled.ThumbUp, ui.errores["retroalimentacion"], singleLine = false
            )
            BotonPrincipal("Guardar pregunta", onClick = viewModel::guardar)
        }
    }
}
