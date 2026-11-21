# Plan de trabajo del equipo — Camino de Estrellas (MPAZ NEB)

La pauta (20 %) revisa que GitHub y Trello muestren trabajo real y repartido. Conviene que cada integrante suba su parte en varios commits.

## Reparto sugerido

| Integrante | Responsable de | Archivos |
|---|---|---|
| Nicolás Ortega (Scrum Master) | Proyecto, tema, navegación, componentes, BaseViewModel, ingreso de adultos y README | `build.gradle.kts`, `ui/theme/`, `ui/navigation/`, `ui/components/`, `viewmodel/BaseViewModel.kt`, `LoginScreen` + `LoginViewModel`, `README.md` |
| Martín Vásquez | Base de datos, repositorios, validaciones y panel de la docente (actividades, preguntas y cámara del material) | `data/local/` (entity, dao, AppDatabase, DatosIniciales), `data/repository/`, `validacion/`, `PanelScreen` (incluida la pestaña Mi perfil con la foto del docente), `DetalleActividad`, `FormActividad` y `FormPregunta` (Screen + ViewModel), `nativo/Camara.kt` |
| Justin Galleguillos | Experiencia del estudiante, estudiantes y progreso, GPS y notificaciones | `model/` (Camino, Logros), `LoginEstudiante`, `Asignaturas`, `Mapa`, `Sesion`, `FormEstudiante` y `Progreso` (Screen + ViewModel), `nativo/Ubicacion.kt`, `nativo/Notificador.kt` |

## Commits sugeridos (en este orden)

1. `chore: configura proyecto Compose con Room y Navigation` (Nicolás)
2. `feat(theme): agrega tema Material 3 "Camino de Estrellas"` (Nicolás)
3. `feat(data): agrega entidades Room (una por archivo) y enums del modelo` (Martín)
4. `feat(data): agrega DAOs, AppDatabase singleton y datos iniciales ficticios` (Martín)
5. `feat(data): agrega repositorios con reglas de negocio` (Martín)
6. `feat(validacion): agrega validaciones de formularios con pruebas JUnit` (Martín)
7. `feat(viewmodel): agrega BaseViewModel con executeOperation, isLoading y componentes reutilizables` (Nicolás)
8. `feat(navigation): agrega rutas tipadas con transiciones animadas` (Nicolás)
9. `feat(login): agrega ingreso de docente y asistente` (Nicolás)
10. `feat(estudiante): agrega ingreso por curso, avatar y PIN` (Justin)
11. `feat(model): agrega lógica del camino, estrellas e insignias con pruebas` (Justin)
12. `feat(estudiante): agrega menú de asignaturas con profesor/a y camino por asignatura` (Justin)
13. `feat(estudiante): agrega actividad con retroalimentación y estrellas` (Justin)
14. `feat(panel): agrega panel con pestañas y catálogo de actividades` (Martín)
15. `feat(panel): agrega formularios de actividad y pregunta` (Martín)
16. `feat(nativo): agrega cámara y galería para el material, el avatar y la foto del docente` (Martín)
17. `feat(panel): agrega registro de estudiantes y progreso con observación` (Justin)
18. `feat(nativo): agrega GPS para practicar solo en la escuela` (Justin)
19. `feat(nativo): agrega notificación de actividad terminada` (Justin)
20. `docs: agrega README con funcionalidades y pasos de ejecución` (Nicolás)

**Ojo:** después de la fecha de entrega no se puede hacer ningún commit. Si se hace, la pauta pone un 1.0 automático.

## Tablero Trello

Columnas: *Backlog · Por hacer · En progreso · Hecho*. Hagan una tarjeta por cada commit con su responsable y fecha.
