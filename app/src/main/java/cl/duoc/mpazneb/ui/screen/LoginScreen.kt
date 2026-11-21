package cl.duoc.mpazneb.ui.screen

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mpazneb.data.local.CLAVE_DEMO
import cl.duoc.mpazneb.data.local.PIN_DEMO
import cl.duoc.mpazneb.ui.components.BotonPrincipal
import cl.duoc.mpazneb.ui.components.CampoTexto
import cl.duoc.mpazneb.ui.components.Estrellas
import cl.duoc.mpazneb.ui.theme.Estrella
import cl.duoc.mpazneb.viewmodel.LoginViewModel

@Composable
fun LoginScreen(
    onSoyEstudiante: () -> Unit,
    onIngresoAdulto: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    // collectAsState: cada vez que cambia el StateFlow, la pantalla se vuelve a dibujar.
    val ui by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(ui.ingresoCorrecto) { if (ui.ingresoCorrecto) onIngresoAdulto() }

    // ANIMACIÓN: la estrella del logo "late" suavemente.
    val escala by rememberInfiniteTransition(label = "logo").animateFloat(
        initialValue = 1f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(tween(800), RepeatMode.Reverse),
        label = "escalaLogo"
    )

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .safeDrawingPadding()
            .imePadding()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text("⭐", fontSize = 64.sp, modifier = Modifier.graphicsLayer { scaleX = escala; scaleY = escala })
        Text("Camino de Estrellas", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
        Text(
            "Lectura y Matemática para 1.º a 4.º básico\nEscuela Escritora Marcela Paz",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Column(Modifier.widthIn(max = 480.dp), verticalArrangement = Arrangement.spacedBy(16.dp)) {
            // Botón grande para los niños: sin escribir nada.
            Button(
                onClick = onSoyEstudiante,
                colors = ButtonDefaults.buttonColors(containerColor = Estrella, contentColor = MaterialTheme.colorScheme.onSurface),
                modifier = Modifier.fillMaxWidth().heightIn(min = 88.dp)
            ) {
                Text("🎒  Soy estudiante", fontSize = 26.sp, fontWeight = FontWeight.Bold)
            }

            Card(Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Docente o asistente de aula", style = MaterialTheme.typography.titleMedium)
                    CampoTexto(ui.alias, viewModel::cambiarAlias, "Usuario", Icons.Filled.Person, ui.errores["alias"])
                    CampoTexto(
                        ui.clave, viewModel::cambiarClave, "Contraseña", Icons.Filled.Lock, ui.errores["clave"],
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                        visualTransformation = PasswordVisualTransformation()
                    )
                    // ANIMACIÓN: el error de la base de datos aparece suavemente.
                    AnimatedVisibility(visible = "general" in ui.errores) {
                        Text(ui.errores["general"].orEmpty(), color = MaterialTheme.colorScheme.error)
                    }
                    BotonPrincipal("Ingresar", onClick = viewModel::ingresar, cargando = isLoading)
                }
            }

            Text(
                "Datos de prueba (ficticios): docente.demo (1.º) a docente4.demo (4.º) / asistente.demo · clave $CLAVE_DEMO · PIN estudiantes $PIN_DEMO",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
