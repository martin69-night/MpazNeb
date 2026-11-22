package cl.duoc.mpazneb.ui.screen

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.lifecycle.viewmodel.compose.viewModel
import cl.duoc.mpazneb.data.local.entity.ActividadEntity
import cl.duoc.mpazneb.data.local.entity.EstudianteEntity
import cl.duoc.mpazneb.model.CURSOS
import cl.duoc.mpazneb.model.EstadoActividad
import cl.duoc.mpazneb.model.Modulo
import cl.duoc.mpazneb.model.Perfil
import cl.duoc.mpazneb.model.etiquetaCurso
import cl.duoc.mpazneb.ui.components.Avatar
import cl.duoc.mpazneb.ui.components.BotonCamara
import cl.duoc.mpazneb.ui.components.BotonGaleria
import cl.duoc.mpazneb.ui.components.ChipEstado
import cl.duoc.mpazneb.ui.components.DialogoCargando
import cl.duoc.mpazneb.ui.components.SelectorChips
import cl.duoc.mpazneb.ui.components.TarjetaFeriados
import cl.duoc.mpazneb.ui.components.TextoVacio
import cl.duoc.mpazneb.ui.components.rememberPedirUbicacion
import cl.duoc.mpazneb.ui.theme.Verde
import cl.duoc.mpazneb.viewmodel.ActividadesViewModel
import cl.duoc.mpazneb.viewmodel.Conexion
import cl.duoc.mpazneb.viewmodel.EscuelaViewModel
import cl.duoc.mpazneb.viewmodel.EstudiantesViewModel
import cl.duoc.mpazneb.viewmodel.PanelViewModel
import cl.duoc.mpazneb.viewmodel.PerfilViewModel

/** Las 4 pestañas del panel de adultos (barra de navegación inferior de Material 3). */
enum class Pestana(val titulo: String, val icono: ImageVector) {
    ACTIVIDADES("Actividades", Icons.AutoMirrored.Filled.List),
    ESTUDIANTES("Estudiantes", Icons.Filled.Face),
    ESCUELA("Escuela", Icons.Filled.LocationOn),
    PERFIL("Mi perfil", Icons.Filled.Person)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PanelScreen(
    onVerActividad: (Long) -> Unit,
    onNuevaActividad: () -> Unit,
    onVerEstudiante: (Long) -> Unit,
    onNuevoEstudiante: () -> Unit,
    onSalir: () -> Unit,
    viewModel: PanelViewModel = viewModel()
) {
    // rememberSaveable: la pestaña elegida se mantiene al girar la pantalla.
    var pestana by rememberSaveable { mutableStateOf(Pestana.ACTIVIDADES) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(pestana.titulo) },
                actions = {
                    Text(viewModel.alias, style = MaterialTheme.typography.labelLarge)
                    IconButton(onClick = { viewModel.cerrarSesion(); onSalir() }) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                Pestana.entries.forEach { p ->
                    NavigationBarItem(
                        selected = pestana == p,
                        onClick = { pestana = p },
                        icon = { Icon(p.icono, contentDescription = null) },
                        label = { Text(p.titulo) }
                    )
                }
            }
        },
        floatingActionButton = {
            // Solo la docente / UTP crea actividades. Ambos perfiles pueden registrar estudiantes.
            if (pestana == Pestana.ACTIVIDADES && viewModel.esDocente) {
                ExtendedFloatingActionButton(onClick = onNuevaActividad, icon = { Icon(Icons.Filled.Add, null) }, text = { Text("Nueva actividad") })
            }
            if (pestana == Pestana.ESTUDIANTES) {
                ExtendedFloatingActionButton(onClick = onNuevoEstudiante, icon = { Icon(Icons.Filled.Add, null) }, text = { Text("Nuevo estudiante") })
            }
        }
    ) { padding ->
        // ANIMACIÓN: fundido al cambiar de pestaña.
        AnimatedContent(
            targetState = pestana,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "pestana",
            modifier = Modifier.padding(padding).fillMaxSize()
        ) { actual ->
            when (actual) {
                Pestana.ACTIVIDADES -> ActividadesTab(onVerActividad)
                Pestana.ESTUDIANTES -> EstudiantesTab(onVerEstudiante)
                Pestana.ESCUELA -> EscuelaTab()
                Pestana.PERFIL -> PerfilTab()
            }
        }
    }
}

// ======================= Mi perfil =======================

/** Foto y datos del adulto. Los estudiantes de su curso ven esta foto en su menú. */
@Composable
private fun PerfilTab(viewModel: PerfilViewModel = viewModel()) {
    val usuario by viewModel.usuario.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    DialogoCargando(isLoading, "Guardando tu foto…")
    val actual = usuario ?: return

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp)
    ) {
        Avatar(actual.nombre.substringAfterLast(' '), actual.fotoRuta, tamano = 140.dp, descripcion = "Foto de ${actual.nombre}")
        Text(actual.nombre, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        Text(
            if (actual.perfil == Perfil.DOCENTE) "Docente · ${actual.curso?.let { etiquetaCurso(it) } ?: "sin curso"}" else "Asistente de aula",
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (actual.perfil == Perfil.DOCENTE) {
            Text(
                "Tus estudiantes verán esta foto en su menú, junto a tu nombre.",
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center
            )
        }
        Spacer(Modifier.height(8.dp))
        BotonCamara("Tomar foto", onFoto = viewModel::fotoTomada)
        BotonGaleria("Elegir de la galería", onFoto = viewModel::fotoElegida)
    }
}

// ======================= Actividades =======================

@Composable
private fun ActividadesTab(onVer: (Long) -> Unit, viewModel: ActividadesViewModel = viewModel()) {
    val ui by viewModel.uiState.collectAsState()
    LazyColumn(contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item { AvisoConexion(ui.conexion, onReintentar = viewModel::sincronizar) }
        item { TarjetaFeriados() } // API externa (feriados), separada del microservicio
        item {
            SelectorChips("Módulo", listOf(null) + Modulo.entries, ui.modulo, { it?.etiqueta ?: "Todos" }, viewModel::filtrarModulo)
        }
        item {
            SelectorChips("Curso", listOf(null) + CURSOS, ui.curso, { if (it == null) "Todos" else etiquetaCurso(it) }, viewModel::filtrarCurso)
        }
        if (ui.actividades.isEmpty()) item { TextoVacio("No hay actividades con estos filtros.") }
        // ANIMACIÓN: animateItem() reacomoda las tarjetas al filtrar.
        items(ui.actividades, key = { it.id }) { actividad ->
            TarjetaActividad(actividad, onClick = { onVer(actividad.id) }, modifier = Modifier.animateItem())
        }
    }
}

/** Muestra si las actividades vienen del microservicio o de lo guardado en el teléfono. */
@Composable
private fun AvisoConexion(conexion: Conexion, onReintentar: () -> Unit) {
    Card(Modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)) {
            Text(
                when (conexion) {
                    Conexion.SINCRONIZANDO -> "🔄 Sincronizando con el microservicio…"
                    Conexion.CONECTADO -> "🟢 Conectado al microservicio"
                    Conexion.SIN_CONEXION -> "🔴 Sin conexión: se muestran los datos guardados en el teléfono"
                },
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.weight(1f)
            )
            if (conexion != Conexion.SINCRONIZANDO) {
                TextButton(onClick = onReintentar) { Text(if (conexion == Conexion.CONECTADO) "Actualizar" else "Reintentar") }
            }
        }
    }
}

@Composable
private fun TarjetaActividad(actividad: ActividadEntity, onClick: () -> Unit, modifier: Modifier) {
    ElevatedCard(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(16.dp)) {
            Text(actividad.modulo.emoji, style = MaterialTheme.typography.headlineMedium)
            Spacer(Modifier.width(12.dp))
            Column(Modifier.weight(1f)) {
                Text(actividad.nombre, style = MaterialTheme.typography.titleMedium)
                Text(
                    "${etiquetaCurso(actividad.curso)} · ${actividad.dificultad.etiqueta}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            ChipEstado(actividad.estado.etiqueta, colorEstado(actividad.estado))
        }
    }
}

@Composable
fun colorEstado(estado: EstadoActividad) = when (estado) {
    EstadoActividad.BORRADOR -> MaterialTheme.colorScheme.outline
    EstadoActividad.DISPONIBLE -> Verde
    EstadoActividad.DESACTIVADA -> MaterialTheme.colorScheme.error
}

// ======================= Estudiantes =======================

@Composable
private fun EstudiantesTab(onVer: (Long) -> Unit, viewModel: EstudiantesViewModel = viewModel()) {
    val ui by viewModel.uiState.collectAsState()
    LazyColumn(contentPadding = PaddingValues(16.dp, 16.dp, 16.dp, 96.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        item {
            SelectorChips("Curso", listOf(null) + CURSOS, ui.curso, { if (it == null) "Todos" else etiquetaCurso(it) }, viewModel::filtrarCurso)
        }
        if (ui.estudiantes.isEmpty()) item { TextoVacio("No hay estudiantes en este curso.") }
        items(ui.estudiantes, key = { it.id }) { estudiante ->
            TarjetaEstudiante(estudiante, onClick = { onVer(estudiante.id) }, modifier = Modifier.animateItem())
        }
    }
}

@Composable
private fun TarjetaEstudiante(estudiante: EstudianteEntity, onClick: () -> Unit, modifier: Modifier) {
    ElevatedCard(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.padding(12.dp)) {
            Avatar(estudiante.alias, estudiante.fotoRuta, tamano = 52.dp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(estudiante.alias, style = MaterialTheme.typography.titleMedium)
                Text(etiquetaCurso(estudiante.curso), style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

// ======================= Escuela (GPS + notificaciones) =======================

@Composable
private fun EscuelaTab(viewModel: EscuelaViewModel = viewModel()) {
    val ui by viewModel.uiState.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    DialogoCargando(isLoading)
    val pedirUbicacion = rememberPedirUbicacion { concedido ->
        if (concedido) viewModel.usarUbicacionActual() else viewModel.permisoRechazado()
    }
    val escuela = ui.escuela

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier.verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Card(Modifier.fillMaxWidth()) {
            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text("📍 Ubicación de la escuela", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
                Text(
                    if (escuela.latitud != null && escuela.longitud != null) "Lat %.5f · Lon %.5f".format(escuela.latitud, escuela.longitud)
                    else "Todavía no se ha fijado."
                )
                Text(
                    "Párate dentro del colegio y toca el botón: se guardará ese punto como la escuela.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                OutlinedButton(onClick = pedirUbicacion, enabled = !ui.buscando, modifier = Modifier.fillMaxWidth()) {
                    Text(if (ui.buscando) "Buscando con el GPS…" else "Usar mi ubicación actual (GPS)")
                }
                ui.mensaje?.let { Text(it, style = MaterialTheme.typography.bodySmall) }

                SelectorChips("Radio permitido", listOf(100, 200, 500), escuela.radioMetros, { "$it m" }, viewModel::cambiarRadio)

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Column(Modifier.weight(1f)) {
                        Text("Solo practicar dentro de la escuela", style = MaterialTheme.typography.titleSmall)
                        Text("El caso indica que la app se usa dentro del establecimiento.", style = MaterialTheme.typography.bodySmall)
                    }
                    Switch(
                        checked = escuela.soloEnEscuela,
                        onCheckedChange = viewModel::cambiarSoloEnEscuela,
                        enabled = escuela.latitud != null
                    )
                }
            }
        }
        PermisoNotificaciones()
    }
}

/**
 * RECURSO NATIVO: NOTIFICACIONES. Desde Android 13 el permiso se pide en tiempo de ejecución.
 * Primero se explica para qué sirve y luego se pide.
 */
@Composable
private fun PermisoNotificaciones() {
    val context = LocalContext.current
    var concedido by remember {
        mutableStateOf(
            Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
                ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        )
    }
    val pedirPermiso = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { concedido = it }

    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Text("🔔 Notificaciones", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text("Avisan cuando un estudiante termina una actividad y cuántas estrellas ganó.")
            if (concedido) {
                Text("Activadas ✓", color = Verde)
            } else {
                TextButton(onClick = { pedirPermiso.launch(Manifest.permission.POST_NOTIFICATIONS) }) { Text("Activar notificaciones") }
            }
        }
    }
}
