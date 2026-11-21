package cl.duoc.mpazneb.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cl.duoc.mpazneb.data.local.entity.ResultadoEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO de la tabla "resultados" (un registro por cada intento terminado).
 */
@Dao
interface ResultadoDao {

    /** Historial de un estudiante, del más nuevo al más antiguo. */
    @Query("SELECT * FROM resultados WHERE estudianteId = :estudianteId ORDER BY fecha DESC")
    fun observeByEstudiante(estudianteId: Long): Flow<List<ResultadoEntity>>

    /** Cuántos intentos lleva un estudiante en una actividad. */
    @Query("SELECT COUNT(*) FROM resultados WHERE estudianteId = :estudianteId AND actividadId = :actividadId")
    suspend fun contarIntentos(estudianteId: Long, actividadId: Long): Int

    /** Inserta y devuelve el id generado (se usa para guardar las respuestas del intento). */
    @Insert
    suspend fun insert(resultado: ResultadoEntity): Long
}
