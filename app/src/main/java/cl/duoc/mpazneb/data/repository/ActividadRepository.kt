package cl.duoc.mpazneb.data.repository

import cl.duoc.mpazneb.data.local.AppDatabase
import cl.duoc.mpazneb.data.local.dao.ActividadDao
import cl.duoc.mpazneb.data.local.dao.PreguntaDao
import cl.duoc.mpazneb.data.local.entity.ActividadEntity
import cl.duoc.mpazneb.data.local.entity.PreguntaEntity
import cl.duoc.mpazneb.data.remote.ApiService
import cl.duoc.mpazneb.data.remote.EstadoRequest
import cl.duoc.mpazneb.data.remote.RetrofitInstance
import cl.duoc.mpazneb.data.remote.exigirExito
import cl.duoc.mpazneb.model.EstadoActividad
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio de actividades y preguntas. Aquí viven las reglas de negocio.
 *
 * PERSISTENCIA REMOTA + LOCAL:
 * - Las ESCRITURAS (crear, editar, cambiar estado, eliminar) van primero al MICROSERVICIO con Retrofit.
 *   Si el microservicio responde bien, la respuesta se guarda en Room (upsert) y la pantalla se actualiza sola.
 *   Si no hay conexión, Retrofit lanza una excepción y no se guarda nada (BaseViewModel muestra el aviso).
 * - Las LECTURAS salen de Room (Flow), así el camino del estudiante funciona aunque no haya red.
 * - sincronizar() trae todo desde el microservicio y actualiza Room.
 *
 * Recibe los DAO y la API por el constructor para poder reemplazarlos por mocks (MockK) en las pruebas.
 */
class ActividadRepository(
    private val actividadDao: ActividadDao,
    private val preguntaDao: PreguntaDao,
    private val api: ApiService
) {
    /** Constructor que usan los ViewModels: la base de datos real y el cliente Retrofit real. */
    constructor(db: AppDatabase) : this(db.actividadDao, db.preguntaDao, RetrofitInstance.api)

    // ---------------- Lecturas (Room) ----------------

    fun observarTodas(): Flow<List<ActividadEntity>> = actividadDao.getAll()
    fun observar(id: Long): Flow<ActividadEntity?> = actividadDao.observeById(id)
    suspend fun obtener(id: Long): ActividadEntity? = actividadDao.getById(id)

    fun observarPreguntas(actividadId: Long): Flow<List<PreguntaEntity>> = preguntaDao.observeByActividad(actividadId)
    fun observarTodasLasPreguntas(): Flow<List<PreguntaEntity>> = preguntaDao.getAll()
    suspend fun obtenerPreguntas(actividadId: Long): List<PreguntaEntity> = preguntaDao.getByActividad(actividadId)
    suspend fun obtenerPregunta(id: Long): PreguntaEntity? = preguntaDao.getById(id)

    /** Validación contra la base: ¿ya existe otra actividad con ese nombre? */
    suspend fun nombreRepetido(nombre: String, idActual: Long): Boolean =
        actividadDao.contarConNombre(nombre.trim(), idActual) > 0

    // ---------------- Sincronización (microservicio → Room) ----------------

    /** GET /api/actividades y GET /api/preguntas: deja Room igual que el microservicio. */
    suspend fun sincronizar() {
        val actividades = api.listarActividades()
        val preguntas = api.listarPreguntas()
        actividadDao.upsertAll(actividades)
        actividadDao.deleteNotIn(actividades.map { it.id })
        preguntaDao.upsertAll(preguntas)
        preguntaDao.deleteNotIn(preguntas.map { it.id })
    }

    // ---------------- Escrituras (microservicio → Room) ----------------

    /** Si el id es 0 la crea con POST (siempre como borrador); si no, la actualiza con PUT. */
    suspend fun guardar(actividad: ActividadEntity): ActividadEntity {
        val guardada = if (actividad.id == 0L) {
            api.crearActividad(actividad.copy(estado = EstadoActividad.BORRADOR))
        } else {
            api.actualizarActividad(actividad.id, actividad)
        }
        actividadDao.upsert(guardada)
        return guardada
    }

    /** Regla de negocio: sin preguntas no se puede dejar disponible. Devuelve el mensaje para la pantalla. */
    suspend fun cambiarEstado(actividad: ActividadEntity, nuevo: EstadoActividad): String {
        if (nuevo == EstadoActividad.DISPONIBLE && obtenerPreguntas(actividad.id).isEmpty()) {
            return "Agrega al menos una pregunta antes de dejarla disponible."
        }
        val actualizada = api.cambiarEstado(actividad.id, EstadoRequest(nuevo))
        actividadDao.upsert(actualizada)
        return "La actividad quedó: ${nuevo.etiqueta.lowercase()}"
    }

    /**
     * Regla de negocio: solo se eliminan actividades en BORRADOR (las demás pueden tener intentos de estudiantes).
     * Devuelve null si se eliminó, o el motivo si no se pudo.
     */
    suspend fun eliminar(actividad: ActividadEntity): String? {
        if (actividad.estado != EstadoActividad.BORRADOR) {
            return "Solo se pueden eliminar actividades en borrador."
        }
        api.eliminarActividad(actividad.id).exigirExito()
        preguntaDao.deleteByActividad(actividad.id)
        actividadDao.deleteById(actividad.id)
        return null
    }

    /** Pregunta nueva (id 0) con POST; existente con PUT. */
    suspend fun guardarPregunta(pregunta: PreguntaEntity) {
        val guardada = if (pregunta.id == 0L) {
            api.crearPregunta(pregunta.actividadId, pregunta)
        } else {
            api.actualizarPregunta(pregunta.id, pregunta)
        }
        preguntaDao.upsert(guardada)
    }

    suspend fun eliminarPregunta(pregunta: PreguntaEntity) {
        api.eliminarPregunta(pregunta.id).exigirExito()
        preguntaDao.delete(pregunta)
    }
}
