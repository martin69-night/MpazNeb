package cl.duoc.mpazneb.ui.screen

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalButton
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mpazneb.data.local.entity.EstudianteEntity
import cl.duoc.mpazneb.model.CURSOS
import cl.duoc.mpazneb.ui.components.Avatar
import cl.duoc.mpazneb.ui.components.TextoVacio
import cl.duoc.mpazneb.viewmodel.LoginEstudianteUiState
import cl.duoc.mpazneb.viewmodel.LoginEstudianteViewModel
import cl.duoc.mpazneb.viewmodel.PasoIngreso

private val coloresCurso = listOf(Color(0xFFEF5350), Color(0xFF42A5F5), Color(0xFF66BB6A), Color(0xFFFFA726))

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginEstudianteScreen(
    onVolver: () -> Unit,
    onIngreso: (Long) -> Unit,
    viewModel: LoginEstudianteViewModel = viewModel()
) {
    val ui by viewModel.uiState.collectAsState()

    LaunchedEffect(ui.ingresoId) { ui.ingresoId?.let(onIngreso) }

    // "Atrás" retrocede un paso; desde el primero vuelve al inicio.
    val atras = { if (!viewModel.volver()) onVolver() }
    BackHandler(onBack = atras)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Soy estudiante") },
                navigationIcon = { IconButton(onClick = atras) { Icon(Icons.AutoMirrored.Filled.ArrowBack, "Volver") } }
            )
        }
    ) { padding ->
        // ANIMACIÓN: cada paso entra deslizándose.
        AnimatedContent(
            targetState = ui.paso,
            transitionSpec = {
                val avanza = targetState.ordinal > initialState.ordinal
                slideInHorizontally { if (avanza) it else -it } togetherWith slideOutHorizontally { if (avanza) -it else it }
            },
            label = "pasoIngreso",
            modifier = Modifier.padding(padding).fillMaxSize()
        ) { paso ->
            when (paso) {
                PasoIngreso.CURSO -> PasoCurso(viewModel::elegirCurso)
                PasoIngreso.ESTUDIANTE -> PasoEstudiante(ui, viewModel::elegirEstudiante)
                PasoIngreso.PIN -> PasoPin(ui, viewModel::presionarDigito, viewModel::borrarDigito)
            }
        }
    }
}

@Composable
private fun PasoCurso(onCurso: (Int) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("¿En qué curso estás?", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.widthIn(max = 520.dp)
        ) {
            items(CURSOS) { curso ->
                Card(
                    onClick = { onCurso(curso) },
                    colors = CardDefaults.cardColors(containerColor = coloresCurso[curso - 1]),
                    modifier = Modifier.aspectRatio(1f)
                ) {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("$curso.º", fontSize = 56.sp, fontWeight = FontWeight.Bold, color = Color.White)
                            Text("básico", fontSize = 20.sp, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PasoEstudiante(ui: LoginEstudianteUiState, onElegir: (EstudianteEntity) -> Unit) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize().padding(24.dp)) {
        Text("¿Quién eres?", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(24.dp))
        if (ui.estudiantes.isEmpty()) TextoVacio("Todavía no hay estudiantes en este curso. Pídele ayuda a tu profe.")
        LazyVerticalGrid(
            columns = GridCells.Adaptive(130.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            contentPadding = PaddingValues(bottom = 24.dp),
            modifier = Modifier.weight(1f)
        ) {
            items(ui.estudiantes, key = { it.id }) { estudiante ->
                Card(onClick = { onElegir(estudiante) }) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                        Avatar(estudiante.alias, estudiante.fotoRuta, tamano = 88.dp)
                        Spacer(Modifier.height(8.dp))
                        Text(estudiante.alias, style = MaterialTheme.typography.titleLarge, textAlign = TextAlign.Center)
                    }
                }
            }
        }
    }
}

@Composable
private fun PasoPin(ui: LoginEstudianteUiState, onDigito: (Char) -> Unit, onBorrar: () -> Unit) {
    val estudiante = ui.elegido ?: return

    // ANIMACIÓN: los puntos del PIN se sacuden cuando el PIN es incorrecto.
    val sacudida = remember { Animatable(0f) }
    LaunchedEffect(ui.fallos) {
        if (ui.fallos > 0) {
            sacudida.animateTo(0f, keyframes {
                durationMillis = 400
                -24f at 50
                24f at 100
                -16f at 180
                16f at 260
                -6f at 330
            })
        }
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Avatar(estudiante.alias, estudiante.fotoRuta, tamano = 96.dp)
        Text("Hola, ${estudiante.alias}", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text("Escribe tu PIN secreto")
        Spacer(Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.graphicsLayer { translationX = sacudida.value }) {
            repeat(4) { i ->
                val lleno = i < ui.pin.length
                Box(
                    Modifier
                        .size(28.dp)
                        .clip(CircleShape)
                        .background(if (lleno) MaterialTheme.colorScheme.primary else Color.Transparent)
                        .border(3.dp, MaterialTheme.colorScheme.primary, CircleShape)
                )
            }
        }
        Text(ui.error.orEmpty(), color = MaterialTheme.colorScheme.error, modifier = Modifier.height(32.dp).padding(top = 8.dp))

        // Teclado numérico grande (más fácil que el teclado del sistema para 1.º básico).
        val teclas = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "", "0", "⌫")
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.widthIn(max = 320.dp)
        ) {
            items(teclas) { tecla ->
                if (tecla.isEmpty()) {
                    Spacer(Modifier.size(72.dp))
                } else {
                    FilledTonalButton(
                        onClick = { if (tecla == "⌫") onBorrar() else onDigito(tecla[0]) },
                        modifier = Modifier.height(72.dp)
                    ) { Text(tecla, fontSize = 28.sp) }
                }
            }
        }
    }
}
