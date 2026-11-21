package cl.duoc.mpazneb.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Entidad de Room que representa la tabla "estudiantes".
 * Datos ficticios: un apodo, sin RUT ni fotos de rostros.
 */
@Entity(tableName = "estudiantes")
data class EstudianteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,                  // Identificador único autoincremental
    val alias: String,                 // Apodo ficticio, por ejemplo "Luna"
    val curso: Int,                    // 1 a 4 (básico)
    val pinHash: String,               // Hash SHA-256 del PIN de 4 números
    val fotoRuta: String? = null,      // Ruta de la foto (cámara) del DIBUJO del personaje
    val observacion: String? = null    // Observación pedagógica general de la asistente
)
