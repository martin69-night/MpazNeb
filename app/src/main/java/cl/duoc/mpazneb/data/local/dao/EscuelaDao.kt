package cl.duoc.mpazneb.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import cl.duoc.mpazneb.data.local.entity.EscuelaEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO de la tabla "escuela" (una sola fila con la configuración del GPS).
 */
@Dao
interface EscuelaDao {

    @Query("SELECT * FROM escuela WHERE id = 1")
    fun observe(): Flow<EscuelaEntity?>

    @Query("SELECT * FROM escuela WHERE id = 1")
    suspend fun get(): EscuelaEntity?

    /** REPLACE: como siempre es la fila 1, inserta la primera vez y después la reemplaza. */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun save(escuela: EscuelaEntity)
}
