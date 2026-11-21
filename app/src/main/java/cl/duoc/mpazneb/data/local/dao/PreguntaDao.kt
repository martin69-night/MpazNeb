package cl.duoc.mpazneb.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import cl.duoc.mpazneb.data.local.entity.PreguntaEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO de la tabla "preguntas".
 */
@Dao
interface PreguntaDao {

    @Query("SELECT * FROM preguntas ORDER BY id")
    fun getAll(): Flow<List<PreguntaEntity>>

    /** Preguntas de una actividad, observadas en tiempo real. */
    @Query("SELECT * FROM preguntas WHERE actividadId = :actividadId ORDER BY id")
    fun observeByActividad(actividadId: Long): Flow<List<PreguntaEntity>>

    @Query("SELECT * FROM preguntas WHERE actividadId = :actividadId ORDER BY id")
    suspend fun getByActividad(actividadId: Long): List<PreguntaEntity>

    @Query("SELECT * FROM preguntas WHERE id = :id")
    suspend fun getById(id: Long): PreguntaEntity?

    @Insert
    suspend fun insert(pregunta: PreguntaEntity)

    @Update
    suspend fun update(pregunta: PreguntaEntity)

    @Delete
    suspend fun delete(pregunta: PreguntaEntity)

    // ---------- Sincronización con el microservicio ----------

    @Upsert
    suspend fun upsert(pregunta: PreguntaEntity)

    @Upsert
    suspend fun upsertAll(preguntas: List<PreguntaEntity>)

    @Query("DELETE FROM preguntas WHERE actividadId = :actividadId")
    suspend fun deleteByActividad(actividadId: Long)

    /** Borra las que ya no existen en el microservicio. */
    @Query("DELETE FROM preguntas WHERE id NOT IN (:ids)")
    suspend fun deleteNotIn(ids: List<Long>)
}
