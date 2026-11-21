package cl.duoc.mpazneb.ui.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mpazneb.model.CURSOS
import cl.duoc.mpazneb.model.etiquetaCurso
import cl.duoc.mpazneb.ui.components.Avatar
import cl.duoc.mpazneb.ui.components.BotonCamara
import cl.duoc.mpazneb.ui.components.BotonPrincipal
import cl.duoc.mpazneb.ui.components.CampoTexto
import cl.duoc.mpazneb.ui.components.DialogoCargando
import cl.duoc.mpazneb.ui.components.SelectorChips
import cl.duoc.mpazneb.viewmodel.FormEstudianteViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormEstudianteScreen(
    onVolver: () -> Unit,
    viewModel: FormEstudianteViewModel = viewModel()
) {
    val ui by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    DialogoCargando(isLoading)

    LaunchedEffect(ui.guardado) { if (ui.guardado) onVolver() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo estudiante") },
                navigationIcon = { IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } }
            )
        }
    ) { padding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            // Vista previa del avatar: se actualiza al escribir el apodo o tomar la foto.
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                Avatar(ui.alias.ifBlank { "?" }, ui.fotoRuta, tamano = 110.dp)
            }
            CampoTexto(ui.alias, { v -> viewModel.actualizar { it.copy(alias = v) } }, "Apodo (ficticio, ej: Luna)", Icons.Filled.Face, ui.errores["alias"])
            SelectorChips("Curso", CURSOS, ui.curso, ::etiquetaCurso, { c -> viewModel.actualizar { it.copy(curso = c) } }, ui.errores["curso"])
            CampoTexto(
                ui.pin, viewModel::cambiarPin, "PIN secreto (4 números)", Icons.Filled.Lock, ui.errores["pin"],
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation()
            )
            CampoTexto(
                ui.confirmacion, viewModel::cambiarConfirmacion, "Repite el PIN", Icons.Filled.Lock, ui.errores["confirmacion"],
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                visualTransformation = PasswordVisualTransformation()
            )

            // CÁMARA: el estudiante dibuja su personaje en papel y se fotografía el dibujo (nunca su cara).
            Text(
                "Avatar: pide al estudiante que dibuje su personaje en una hoja y fotografía el dibujo. Así no se guardan fotos de niños.",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            BotonCamara(if (ui.fotoRuta == null) "Fotografiar el dibujo" else "Tomar otra foto", onFoto = viewModel::fotoTomada)

            BotonPrincipal("Guardar estudiante", onClick = viewModel::guardar)
        }
    }
}
