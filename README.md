# Camino de Estrellas — MPAZ NEB

App Android en Kotlin + Jetpack Compose para el caso **MPAZ NEB** de la Escuela Escritora Marcela Paz (DSY1105, Duoc UC). Ayuda a practicar **Lectura y Matemática de 1.º a 4.º básico**: cada estudiante entra con su avatar y su PIN, ve **todas las actividades disponibles de su curso** como un camino que se desbloquea de lo más fácil a lo más difícil, gana estrellas de práctica e insignias, y su avance queda guardado para que la docente y la asistente orienten el refuerzo.

**Integrantes:** Nicolás Ortega · Martín Vásquez · Justin Galleguillos

## Qué hace cada perfil

| Perfil | Qué puede hacer |
|---|---|
| **Estudiante** | Entra eligiendo su curso, tocando su avatar y escribiendo su PIN de 4 números en un teclado grande (no necesita leer ni escribir). Llega a **Mis asignaturas**: arriba ve a su profesor/a con su foto y abajo Lectura y Matemática con su avance. Al tocar una asignatura ve **todas sus actividades disponibles en orden**; se abren una a una al terminar la anterior. Gana de 1 a 3 estrellas (de práctica, no son nota) e insignias. |
| **Docente / UTP** | Crea y edita actividades y preguntas, fotografía el material, deja una actividad disponible o la desactiva, registra estudiantes, ve su progreso y sube su foto de perfil (cámara o galería), que ven los estudiantes de su curso. |
| **Asistente de aula** | Registra estudiantes (con la foto del dibujo de su personaje), ve el progreso y las respuestas de cada uno, escribe observaciones y configura la escuela (GPS). No puede editar actividades ni respuestas correctas. |

El estudiante solo ve las actividades **de su curso** que la docente dejó **disponibles**, así que el nivel lo decide siempre un adulto.

## Recursos nativos

| Recurso | Para qué se usa | Permiso |
|---|---|---|
| **Cámara** | El estudiante dibuja su personaje en papel y se le saca una foto: ese dibujo es su avatar, así nunca se fotografía la cara de un niño. La docente puede fotografiar el material de una actividad y tomarse su foto de perfil (también puede elegirla de la galería). | `CAMERA` (en tiempo de ejecución); la galería no necesita permiso |
| **GPS** | Se guarda la ubicación de la escuela. Con la opción "Solo practicar dentro de la escuela", el camino del estudiante se abre únicamente si el GPS confirma que está dentro del radio (condición del caso: uso dentro del establecimiento). | `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` |
| **Notificaciones** | Avisan cuando un estudiante termina una actividad y cuántas estrellas ganó. | `POST_NOTIFICATIONS` (Android 13+) |

## Requisitos de la pauta

| Requisito | Dónde |
|---|---|
| Material 3 | Tema propio (`ui/theme`), `Scaffold`, `TopAppBar`, `NavigationBar`, `Card`, `FilterChip`, `Switch` |
| Formularios validados | Login, PIN, actividad, pregunta, estudiante y observación. Cada campo muestra borde rojo, ícono y mensaje (`ui/components/Componentes.kt`) |
| Validaciones locales | Funciones puras en `validacion/Validaciones.kt`, con pruebas JUnit |
| Validaciones contra la base de datos | Usuario y contraseña, PIN del estudiante, nombre de actividad repetido, apodo repetido en el curso, actividad sin preguntas |
| Relación entre tablas | El profesor/a del estudiante se obtiene con una subconsulta: el docente cuyo `curso` es el del estudiante (`UsuarioDao.observeDocenteDe`) |
| Navegación | Navigation Compose con rutas tipadas (`ui/navigation/Navegacion.kt`) |
| Gestión de estado | Cada pantalla tiene un `ViewModel` (`AndroidViewModel`) con un `UiState` en `StateFlow`; las pantallas lo leen con `collectAsState()` |
| Persistencia local | Room / SQLite con 7 tablas (`data/local/entity`, `data/local/dao`, `data/local/AppDatabase.kt`). Cada escritura pasa por `executeOperation` y muestra un diálogo "Guardando en la base de datos…" |
| Recursos nativos | Cámara, GPS y notificaciones (`nativo/`) |
| Animaciones | Transiciones entre pantallas, botón con rebote, pasos del ingreso que se deslizan, PIN que se sacude si es incorrecto, nodo del camino que "late", preguntas que entran deslizándose, retroalimentación con rebote, barra de avance, estrellas que aparecen una por una y listas animadas |

## Arquitectura (MVVM)

```
Screen (Compose)  →  ViewModel (StateFlow)  →  Repository  →  DAO  →  Room (SQLite)
```

```
cl.duoc.mpazneb/
├── MpazApp.kt                  Carga los datos de prueba la primera vez y crea el canal de notificaciones
├── MainActivity.kt
├── data/
│   ├── local/
│   │   ├── entity/             Una entidad por archivo (UsuarioEntity, EstudianteEntity, ActividadEntity, ...)
│   │   ├── dao/                Un DAO por tabla (consultas con Flow, escrituras con suspend)
│   │   ├── AppDatabase.kt      @Database + instancia única (singleton) "camino_db"
│   │   └── DatosIniciales.kt   Datos ficticios para probar
│   └── repository/             Reglas de negocio (Sesion, Usuario, Actividad, Estudiante, Escuela)
├── model/                      Enums y lógica pura: Camino.kt (desbloqueo) y Logros.kt (insignias)
├── validacion/                 Validaciones de formularios (funciones puras, con pruebas JUnit)
├── nativo/                     Camara, Ubicacion (GPS), Notificador
├── viewmodel/                  BaseViewModel (executeOperation + isLoading) y un ViewModel por pantalla
└── ui/
    ├── components/             Componentes reutilizables (CampoTexto, BotonPrincipal, DialogoCargando, ...)
    ├── navigation/             Rutas tipadas y NavHost
    ├── screen/                 Una pantalla por archivo
    └── theme/                  Color.kt, Theme.kt, Type.kt
```

**Cómo guarda un ViewModel (patrón del curso):**

```kotlin
abstract class BaseViewModel(application: Application) : AndroidViewModel(application) {
    protected val db = AppDatabase.obtener(application)
    private val _isLoading = MutableStateFlow(false)
    val isLoading = _isLoading.asStateFlow()

    protected fun executeOperation(operation: suspend () -> Unit) { /* isLoading = true → operación → isLoading = false */ }
}
```

Cada pantalla crea su ViewModel con `viewModel()`, lee `uiState` e `isLoading` con `collectAsState()` y muestra `DialogoCargando(isLoading)` mientras Room guarda.

Tablas: `usuarios` (con nombre, curso a cargo y foto), `estudiantes`, `actividades`, `preguntas`, `resultados` (cada intento), `respuestas` (cada respuesta de un intento) y `escuela` (ubicación y radio).

## Cómo ejecutar

1. Abrir la carpeta en **Android Studio** y esperar el *Gradle Sync*.
2. Usar un emulador con **Android 13 (API 33) o superior**, de preferencia una tableta (la app funciona desde Android 11, `minSdk 30`).
   Si tenían instalada una versión anterior de la app, desinstálenla primero.
3. Ejecutar `app`. También se puede compilar con `./gradlew assembleDebug` y probar con `./gradlew test`.

**Datos de prueba (ficticios):**
- Docentes: `docente.demo` (1.º, Profesora Carmen), `docente2.demo` (2.º), `docente3.demo` (3.º) y `docente4.demo` (4.º), clave `Mpaz2026`
- Asistente: `asistente.demo` / `Mpaz2026`
- Estudiantes: Luna y Benja (1.º), Cami y Tomi (2.º), Pía (3.º) y Nico (4.º), todos con PIN `1234`

**En el emulador:**
- **GPS:** en *Extended controls → Location* se elige un punto y se presiona *Set location*.
- **Cámara:** la cámara virtual muestra una escena de prueba.
- **Base de datos:** se puede ver en vivo con *App Inspection → Database Inspector*.

## Recorrido para probar

1. Como **docente** (`docente.demo`), ir a **Mi perfil** y tomarse una foto o elegirla de la galería.
2. Como **estudiante**, entrar como Luna (1.º, PIN `1234`): arriba aparece la Profesora Carmen con su foto. Tocar **Lectura**, hacer "El gato Tomás": se gana estrellas y se desbloquea la siguiente actividad.
3. Como **asistente**, en la pestaña Escuela, activar las notificaciones, usar la ubicación actual como la escuela y activar "Solo practicar dentro de la escuela".
4. Registrar un estudiante nuevo con la foto de su dibujo y verlo en el ingreso de estudiantes.
5. Revisar el **progreso** de Luna: intentos, respuestas y una observación guardada.
6. Como **docente**, crear una actividad con preguntas y dejarla disponible: aparece en el camino de ese curso.

> Todos los datos son ficticios: apodos, sin RUT, sin diagnósticos ni calificaciones. Los estudiantes nunca tienen foto de su cara: su avatar es la foto de un dibujo. Solo los adultos pueden subir una foto real de sí mismos.
