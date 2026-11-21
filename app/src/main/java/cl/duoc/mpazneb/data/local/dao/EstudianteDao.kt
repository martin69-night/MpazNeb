package cl.duoc.mpazneb.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cl.duoc.mpazneb.data.local.entity.EstudianteEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO de la tabla "estudiantes".
 */
@Dao
interface EstudianteDao {

    /** Todos los estudiantes ordenados por curso y apodo. Flow = se actualiza solo. */
    @Query("SELECT * FROM estudiantes ORDER BY curso, alias")
    fun getAll(): Flow<List<EstudianteEntity>>

    /** Un estudiante, observado en tiempo real. */
    @Query("SELECT * FROM estudiantes WHERE id = :id")
    fun observeById(id: Long): Flow<EstudianteEntity?>

    @Query("SELECT * FROM estudiantes WHERE id = :id")
    suspend fun getById(id: Long): EstudianteEntity?

    /** Validación contra la base: ¿ya existe ese apodo en el curso? */
    @Query("SELECT COUNT(*) FROM estudiantes WHERE LOWER(alias) = LOWER(:alias) AND curso = :curso")
    suspend fun contarConAlias(alias: String, curso: Int): Int

    @Insert
    suspend fun insert(estudiante: EstudianteEntity)

    @Update
    suspend fun update(estudiante: EstudianteEntity)
}
