package cl.duoc.mpazneb.data.repository

import cl.duoc.mpazneb.data.local.AppDatabase
import cl.duoc.mpazneb.data.local.entity.ActividadEntity
import cl.duoc.mpazneb.data.local.entity.PreguntaEntity
import cl.duoc.mpazneb.model.EstadoActividad
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio de actividades y preguntas. Aquí viven las reglas de negocio.
 */
class ActividadRepository(private val db: AppDatabase) {

    fun observarTodas(): Flow<List<ActividadEntity>> = db.actividadDao.getAll()
    fun observar(id: Long): Flow<ActividadEntity?> = db.actividadDao.observeById(id)
    suspend fun obtener(id: Long): ActividadEntity? = db.actividadDao.getById(id)

    fun observarPreguntas(actividadId: Long): Flow<List<PreguntaEntity>> = db.preguntaDao.observeByActividad(actividadId)
    fun observarTodasLasPreguntas(): Flow<List<PreguntaEntity>> = db.preguntaDao.getAll()
    suspend fun obtenerPreguntas(actividadId: Long): List<PreguntaEntity> = db.preguntaDao.getByActividad(actividadId)
    suspend fun obtenerPregunta(id: Long): PreguntaEntity? = db.preguntaDao.getById(id)

    /** Validación contra la base: ¿ya existe otra actividad con ese nombre? */
    suspend fun nombreRepetido(nombre: String, idActual: Long): Boolean =
        db.actividadDao.contarConNombre(nombre.trim(), idActual) > 0

    /** Si el id es 0 inserta (siempre como borrador); si no, actualiza. */
    suspend fun guardar(actividad: ActividadEntity) {
        if (actividad.id == 0L) {
            db.actividadDao.insert(actividad.copy(estado = EstadoActividad.BORRADOR))
        } else {
            db.actividadDao.update(actividad)
        }
    }

    /** Regla de negocio: sin preguntas no se puede dejar disponible. Devuelve el mensaje para la pantalla. */
    suspend fun cambiarEstado(actividad: ActividadEntity, nuevo: EstadoActividad): String {
        if (nuevo == EstadoActividad.DISPONIBLE && obtenerPreguntas(actividad.id).isEmpty()) {
            return "Agrega al menos una pregunta antes de dejarla disponible."
        }
        db.actividadDao.update(actividad.copy(estado = nuevo))
        return "La actividad quedó: ${nuevo.etiqueta.lowercase()}"
    }

    suspend fun guardarPregunta(pregunta: PreguntaEntity) {
        if (pregunta.id == 0L) db.preguntaDao.insert(pregunta) else db.preguntaDao.update(pregunta)
    }

    suspend fun eliminarPregunta(pregunta: PreguntaEntity) = db.preguntaDao.delete(pregunta)
}
