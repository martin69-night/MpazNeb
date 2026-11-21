package cl.duoc.mpazneb.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val Claro = lightColorScheme(
    primary = AzulNoche,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFDDE1FF),
    onPrimaryContainer = Color(0xFF00105C),
    secondary = Color(0xFF7A5900),
    secondaryContainer = Color(0xFFFFE08C),
    tertiary = Verde,
    background = Color(0xFFF8F7FF),
    surface = Color(0xFFF8F7FF)
)

private val Oscuro = darkColorScheme(
    primary = AzulClaro,
    secondary = Estrella,
    tertiary = Color(0xFF8BD6AE)
)

/** Esquinas bien redondeadas: se ven más amables para niños. */
private val Formas = Shapes(
    small = RoundedCornerShape(12.dp),
    medium = RoundedCornerShape(20.dp),
    large = RoundedCornerShape(28.dp)
)

@Composable
fun MpazTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = if (isSystemInDarkTheme()) Oscuro else Claro,
        typography = Typography,
        shapes = Formas,
        content = content
    )
}
