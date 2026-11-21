package cl.duoc.mpazneb.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import cl.duoc.mpazneb.model.Perfil

/**
 * Entidad de Room que representa la tabla "usuarios": docente/UTP y asistente de aula.
 * Datos ficticios: un alias para ingresar y un nombre para mostrar (no son personas reales).
 */
@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,          // Identificador único autoincremental
    val alias: String,         // Usuario para ingresar, por ejemplo "docente.demo"
    val claveHash: String,     // Hash SHA-256 de la contraseña (nunca la contraseña tal cual)
    val perfil: Perfil,        // DOCENTE o ASISTENTE (Room guarda el enum como texto)
    val nombre: String,        // Cómo lo ven los estudiantes, por ejemplo "Profesora Carmen"
    val curso: Int? = null,    // Curso a cargo (1 a 4). La asistente no tiene curso: null
    val fotoRuta: String? = null // Ruta de la foto de perfil (cámara o galería), o null
)
