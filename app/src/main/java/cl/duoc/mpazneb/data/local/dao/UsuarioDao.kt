package cl.duoc.mpazneb.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import cl.duoc.mpazneb.data.local.entity.UsuarioEntity
import kotlinx.coroutines.flow.Flow

/**
 * DAO de la tabla "usuarios" (docente / asistente).
 */
@Dao
interface UsuarioDao {

    /** Busca un usuario por su alias. Devuelve null si no existe. */
    @Query("SELECT * FROM usuarios WHERE alias = :alias LIMIT 1")
    suspend fun buscarPorAlias(alias: String): UsuarioEntity?

    /** Cuenta los usuarios: si es 0, la base está vacía y se cargan los datos iniciales. */
    @Query("SELECT COUNT(*) FROM usuarios")
    suspend fun contar(): Int

    /** Observa un usuario por id (el panel lo usa para mostrar y actualizar la foto). */
    @Query("SELECT * FROM usuarios WHERE id = :id")
    fun observeById(id: Long): Flow<UsuarioEntity?>

    /**
     * Profesor/a del estudiante: el docente cuyo curso es el mismo del estudiante.
     * Usa una subconsulta para relacionar las tablas "usuarios" y "estudiantes".
     */
    @Query(
        """SELECT * FROM usuarios
           WHERE perfil = 'DOCENTE'
             AND curso = (SELECT curso FROM estudiantes WHERE id = :estudianteId)
           LIMIT 1"""
    )
    fun observeDocenteDe(estudianteId: Long): Flow<UsuarioEntity?>

    @Insert
    suspend fun insert(usuario: UsuarioEntity)

    @Update
    suspend fun update(usuario: UsuarioEntity)
}
