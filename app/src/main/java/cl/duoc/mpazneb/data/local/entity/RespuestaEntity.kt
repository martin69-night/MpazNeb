package cl.duoc.mpazneb.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room que representa la tabla "respuestas": lo que contestó el estudiante en cada pregunta.
 */
@Entity(tableName = "respuestas")
data class RespuestaEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val resultadoId: Long,     // A qué intento pertenece
    val preguntaId: Long,
    val elegida: Int,          // Alternativa elegida (0, 1 o 2)
    val esCorrecta: Boolean
)
