package cl.duoc.mpazneb.ui.components

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.core.content.ContextCompat
import cl.duoc.mpazneb.ui.theme.Bloqueado
import cl.duoc.mpazneb.ui.theme.Estrella

/*
 * Componentes reutilizables. NINGUNO valida: solo muestran lo que les entrega el ViewModel.
 */

/** Campo de texto de todos los formularios: ícono, borde rojo + advertencia + mensaje si hay error. */
@Composable
fun CampoTexto(
    valor: String,
    onValorChange: (String) -> Unit,
    etiqueta: String,
    icono: ImageVector,
    error: String?,
    modifier: Modifier = Modifier,
    singleLine: Boolean = true,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    visualTransformation: VisualTransformation = VisualTransformation.None
) {
    OutlinedTextField(
        value = valor,
        onValueChange = onValorChange,
        label = { Text(etiqueta) },
        leadingIcon = { Icon(icono, contentDescription = null) },
        trailingIcon = if (error != null) {
            { Icon(Icons.Filled.Warning, contentDescription = "Error", tint = MaterialTheme.colorScheme.error) }
        } else null,
        isError = error != null,
        supportingText = if (error != null) {
            { Text(error) }
        } else null,
        singleLine = singleLine,
        minLines = if (singleLine) 1 else 3,
        keyboardOptions = keyboardOptions,
        visualTransformation = visualTransformation,
        modifier = modifier.fillMaxWidth()
    )
}

/** Fila de chips de Material 3 para elegir una opción. */
@Composable
fun <T> SelectorChips(
    titulo: String,
    opciones: List<T>,
    seleccion: T?,
    etiqueta: (T) -> String,
    onSeleccion: (T) -> Unit,
    error: String? = null
) {
    Column {
        Text(
            titulo,
            style = MaterialTheme.typography.labelLarge,
            color = if (error != null) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.horizontalScroll(rememberScrollState())) {
            opciones.forEach { opcion ->
                FilterChip(selected = opcion == seleccion, onClick = { onSeleccion(opcion) }, label = { Text(etiqueta(opcion)) })
            }
        }
        if (error != null) Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
    }
}

/**
 * Botón grande (56 dp, cómodo para niños).
 * ANIMACIÓN: se achica con rebote al presionarlo y muestra un indicador de carga si [cargando].
 */
@Composable
fun BotonPrincipal(
    texto: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    habilitado: Boolean = true,
    cargando: Boolean = false,
    colores: ButtonColors = ButtonDefaults.buttonColors()
) {
    val interaccion = remember { MutableInteractionSource() }
    val presionado by interaccion.collectIsPressedAsState()
    val escala by animateFloatAsState(
        targetValue = if (presionado) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "escalaBoton"
    )
    Button(
        onClick = onClick,
        enabled = habilitado && !cargando,
        interactionSource = interaccion,
        colors = colores,
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 56.dp)
            .graphicsLayer { scaleX = escala; scaleY = escala }
    ) {
        AnimatedContent(targetState = cargando, label = "cargaBoton") { estaCargando ->
            if (estaCargando) {
                CircularProgressIndicator(Modifier.size(24.dp), color = LocalContentColor.current, strokeWidth = 2.dp)
            } else {
                Text(texto, style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

/** Etiqueta de color para un estado. */
@Composable
fun ChipEstado(texto: String, color: Color) {
    Surface(shape = RoundedCornerShape(50), color = color.copy(alpha = 0.15f)) {
        Text(texto, color = color, style = MaterialTheme.typography.labelMedium, modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp))
    }
}

/** Estrellas de práctica: llenas en amarillo, vacías en gris. */
@Composable
fun Estrellas(cantidad: Int, tamano: Int = 20) {
    Row {
        repeat(3) { i ->
            Text("★", fontSize = tamano.sp, color = if (i < cantidad) Estrella else Bloqueado)
        }
    }
}

/** Muestra una foto guardada (tomada con la cámara), recortada y con bordes redondeados. */
@Composable
fun FotoGuardada(ruta: String, modifier: Modifier = Modifier, alto: Dp = 180.dp) {
    val imagen = remember(ruta) { BitmapFactory.decodeFile(ruta)?.asImageBitmap() } ?: return
    Image(
        bitmap = imagen,
        contentDescription = "Foto",
        contentScale = ContentScale.Crop,
        modifier = modifier
            .fillMaxWidth()
            .height(alto)
            .clip(RoundedCornerShape(20.dp))
    )
}

private val coloresAvatar = listOf(
    Color(0xFFEF5350), Color(0xFFAB47BC), Color(0xFF5C6BC0), Color(0xFF29B6F6),
    Color(0xFF26A69A), Color(0xFF9CCC65), Color(0xFFFFA726), Color(0xFF8D6E63)
)

/** Avatar: la foto guardada (dibujo del estudiante o foto del docente), o un círculo de color con la inicial. */
@Composable
fun Avatar(alias: String, fotoRuta: String?, tamano: Dp = 64.dp, descripcion: String = "Dibujo de $alias") {
    val imagen = remember(fotoRuta) { fotoRuta?.let { BitmapFactory.decodeFile(it)?.asImageBitmap() } }
    if (imagen != null) {
        Image(
            bitmap = imagen,
            contentDescription = descripcion,
            contentScale = ContentScale.Crop,
            modifier = Modifier.size(tamano).clip(CircleShape)
        )
    } else {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .size(tamano)
                .clip(CircleShape)
                .background(coloresAvatar[Math.floorMod(alias.hashCode(), coloresAvatar.size)])
        ) {
            Text(alias.take(1).uppercase(), color = Color.White, fontWeight = FontWeight.Bold, fontSize = (tamano.value / 2.2f).sp)
        }
    }
}

/**
 * RECURSO NATIVO: CÁMARA. Botón que:
 * 1) revisa si ya existe el permiso CAMERA; si no, lo pide en tiempo de ejecución,
 * 2) abre la cámara del sistema y entrega la foto (Bitmap) en [onFoto].
 */
@Composable
fun BotonCamara(texto: String, onFoto: (Bitmap) -> Unit) {
    val context = LocalContext.current
    var aviso by remember { mutableStateOf<String?>(null) }
    val tomarFoto = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { foto ->
        if (foto != null) onFoto(foto) // null = la persona canceló
    }
    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido ->
        if (concedido) tomarFoto.launch(null) else aviso = "Sin permiso de cámara no se puede tomar la foto."
    }
    Column {
        OutlinedButton(
            onClick = {
                val permitido = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
                if (permitido) tomarFoto.launch(null) else pedirPermiso.launch(Manifest.permission.CAMERA)
            },
            modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
        ) { Text("📷  $texto") }
        aviso?.let { Text(it, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall) }
    }
}

/**
 * Botón para elegir una foto de la galería con el selector de fotos del sistema.
 * No necesita permisos: el sistema solo entrega la foto que la persona eligió.
 */
@Composable
fun BotonGaleria(texto: String, onFoto: (Uri) -> Unit) {
    val elegir = rememberLauncherForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) onFoto(uri) // null = la persona canceló
    }
    OutlinedButton(
        onClick = { elegir.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)) },
        modifier = Modifier.fillMaxWidth().heightIn(min = 52.dp)
    ) { Text("🖼️  $texto") }
}

/**
 * RECURSO NATIVO: GPS. Devuelve una función que pide los permisos de ubicación
 * y avisa en [onResultado] si se concedió al menos uno.
 */
@Composable
fun rememberPedirUbicacion(onResultado: (Boolean) -> Unit): () -> Unit {
    val lanzador = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { resultado ->
        onResultado(resultado.values.any { it })
    }
    return { lanzador.launch(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION)) }
}

@Composable
fun Cargando() {
    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { CircularProgressIndicator() }
}

@Composable
fun TextoVacio(mensaje: String) {
    Text(
        mensaje,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.fillMaxWidth().padding(32.dp)
    )
}

/**
 * Diálogo modal de carga (mismo patrón visto en clase): se muestra mientras el ViewModel
 * está escribiendo en la base de datos (isLoading = true) e impide tocar otra cosa.
 */
@Composable
fun DialogoCargando(visible: Boolean, texto: String = "Guardando en la base de datos…") {
    if (!visible) return
    Dialog(onDismissRequest = {}) { // vacío: no se puede cerrar tocando fuera
        Surface(shape = MaterialTheme.shapes.large, tonalElevation = 6.dp) {
            Row(
                modifier = Modifier.padding(24.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator(Modifier.size(32.dp))
                Text(texto, style = MaterialTheme.typography.bodyLarge)
            }
        }
    }
}
