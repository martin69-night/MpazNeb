package cl.duoc.mpazneb.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import cl.duoc.mpazneb.data.local.entity.RespuestaEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO de la tabla "respuestas".
 */
@Dao
interface RespuestaDao {

    /** Todas las respuestas de un estudiante (subconsulta sobre sus resultados). */
    @Query("SELECT * FROM respuestas WHERE resultadoId IN (SELECT id FROM resultados WHERE estudianteId = :estudianteId)")
    fun observeByEstudiante(estudianteId: Long): Flow<List<RespuestaEntity>>

    @Insert
    suspend fun insertAll(respuestas: List<RespuestaEntity>)
}
