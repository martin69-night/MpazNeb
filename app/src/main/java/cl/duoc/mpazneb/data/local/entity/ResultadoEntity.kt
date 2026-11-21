package cl.duoc.mpazneb.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room que representa la tabla "resultados": cada intento terminado de un estudiante.
 */
@Entity(tableName = "resultados")
data class ResultadoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val estudianteId: Long,
    val actividadId: Long,
    val aciertos: Int,
    val total: Int,
    val fecha: Long = System.currentTimeMillis() // Timestamp para ordenar el historial
) {
    /** Estrellas de práctica (no es una nota): 3 si todo bien, 2 desde 60 %, y 1 por participar. */
    fun estrellas(): Int = when {
        total == 0 -> 0
        aciertos == total -> 3
        aciertos * 100 / total >= 60 -> 2
        else -> 1
    }
}
