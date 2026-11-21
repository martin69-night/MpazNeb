package cl.duoc.mpazneb.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mpazneb.model.CURSOS
import cl.duoc.mpazneb.model.Dificultad
import cl.duoc.mpazneb.model.Modulo
import cl.duoc.mpazneb.model.etiquetaCurso
import cl.duoc.mpazneb.ui.components.BotonCamara
import cl.duoc.mpazneb.ui.components.BotonPrincipal
import cl.duoc.mpazneb.ui.components.CampoTexto
import cl.duoc.mpazneb.ui.components.DialogoCargando
import cl.duoc.mpazneb.ui.components.FotoGuardada
import cl.duoc.mpazneb.ui.components.SelectorChips
import cl.duoc.mpazneb.viewmodel.FormActividadViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FormActividadScreen(
    onVolver: () -> Unit,
    viewModel: FormActividadViewModel = viewModel()
) {
    val ui by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    DialogoCargando(isLoading)

    LaunchedEffect(ui.guardado) { if (ui.guardado) onVolver() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (ui.esEdicion) "Editar actividad" else "Nueva actividad") },
                navigationIcon = { IconButton(onClick = onVolver) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } }
            )
        }
    ) { padding ->
        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.padding(padding).imePadding().verticalScroll(rememberScrollState()).padding(16.dp)
        ) {
            CampoTexto(ui.nombre, { v -> viewModel.actualizar { it.copy(nombre = v) } }, "Nombre", Icons.Filled.Create, ui.errores["nombre"])
            SelectorChips("Módulo", Modulo.entries, ui.modulo, { "${it.emoji} ${it.etiqueta}" }, { m -> viewModel.actualizar { it.copy(modulo = m) } }, ui.errores["modulo"])
            SelectorChips("Curso", CURSOS, ui.curso, ::etiquetaCurso, { c -> viewModel.actualizar { it.copy(curso = c) } }, ui.errores["curso"])
            SelectorChips("Dificultad (orden en el camino)", Dificultad.entries, ui.dificultad, { it.etiqueta }, { d -> viewModel.actualizar { it.copy(dificultad = d) } }, ui.errores["dificultad"])
            CampoTexto(ui.objetivo, { v -> viewModel.actualizar { it.copy(objetivo = v) } }, "Objetivo pedagógico", Icons.Filled.Star, ui.errores["objetivo"], singleLine = false)
            CampoTexto(ui.instrucciones, { v -> viewModel.actualizar { it.copy(instrucciones = v) } }, "Instrucciones (breves, para el estudiante)", Icons.Filled.Info, ui.errores["instrucciones"], singleLine = false)
            CampoTexto(
                ui.intentos, { v -> viewModel.actualizar { it.copy(intentos = v) } }, "Intentos permitidos (1 a 5)", Icons.Filled.Refresh, ui.errores["intentos"],
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
            )

            // ANIMACIÓN: el campo del texto aparece solo si el módulo es Lectura.
            AnimatedVisibility(visible = ui.modulo == Modulo.LECTURA) {
                CampoTexto(ui.textoLectura, { v -> viewModel.actualizar { it.copy(textoLectura = v) } }, "Texto de lectura", Icons.AutoMirrored.Filled.List, ui.errores["textoLectura"], singleLine = false)
            }

            // CÁMARA: foto del material impreso (guía, lámina, cuento). Opcional.
            ui.fotoRuta?.let { FotoGuardada(it) }
            BotonCamara(if (ui.fotoRuta == null) "Fotografiar el material" else "Tomar otra foto", onFoto = viewModel::fotoTomada)

            BotonPrincipal("Guardar", onClick = viewModel::guardar)
        }
    }
}
