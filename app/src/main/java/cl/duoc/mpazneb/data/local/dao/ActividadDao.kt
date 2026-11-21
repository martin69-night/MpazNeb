package cl.duoc.mpazneb.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import androidx.room.Upsert
import cl.duoc.mpazneb.data.local.entity.ActividadEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO de la tabla "actividades".
 */
@Dao
interface ActividadDao {

    /** Todas las actividades. Flow = cuando la tabla cambia, Room emite la lista nueva. */
    @Query("SELECT * FROM actividades ORDER BY curso, modulo, nombre")
    fun getAll(): Flow<List<ActividadEntity>>

    @Query("SELECT * FROM actividades WHERE id = :id")
    fun observeById(id: Long): Flow<ActividadEntity?>

    @Query("SELECT * FROM actividades WHERE id = :id")
    suspend fun getById(id: Long): ActividadEntity?

    /** Validación contra la base: ¿existe OTRA actividad con ese nombre? */
    @Query("SELECT COUNT(*) FROM actividades WHERE LOWER(nombre) = LOWER(:nombre) AND id != :idExcluir")
    suspend fun contarConNombre(nombre: String, idExcluir: Long): Int

    @Insert
    suspend fun insert(actividad: ActividadEntity): Long

    @Update
    suspend fun update(actividad: ActividadEntity)

    // ---------- Sincronización con el microservicio ----------

    /** Upsert = si el id existe lo actualiza, si no lo inserta (se usa con lo que responde el microservicio). */
    @Upsert
    suspend fun upsert(actividad: ActividadEntity)

    @Upsert
    suspend fun upsertAll(actividades: List<ActividadEntity>)

    @Query("DELETE FROM actividades WHERE id = :id")
    suspend fun deleteById(id: Long)

    /** Borra las que ya no existen en el microservicio. */
    @Query("DELETE FROM actividades WHERE id NOT IN (:ids)")
    suspend fun deleteNotIn(ids: List<Long>)
}
