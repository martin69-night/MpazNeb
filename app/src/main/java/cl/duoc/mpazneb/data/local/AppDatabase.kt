package cl.duoc.mpazneb.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import cl.duoc.mpazneb.data.local.dao.ActividadDao
import cl.duoc.mpazneb.data.local.dao.EscuelaDao
import cl.duoc.mpazneb.data.local.dao.EstudianteDao
import cl.duoc.mpazneb.data.local.dao.PreguntaDao
import cl.duoc.mpazneb.data.local.dao.RespuestaDao
import cl.duoc.mpazneb.data.local.dao.ResultadoDao
import cl.duoc.mpazneb.data.local.dao.UsuarioDao
import cl.duoc.mpazneb.data.local.entity.ActividadEntity
import cl.duoc.mpazneb.data.local.entity.EscuelaEntity
import cl.duoc.mpazneb.data.local.entity.EstudianteEntity
import cl.duoc.mpazneb.data.local.entity.PreguntaEntity
import cl.duoc.mpazneb.data.local.entity.RespuestaEntity
import cl.duoc.mpazneb.data.local.entity.ResultadoEntity
import cl.duoc.mpazneb.data.local.entity.UsuarioEntity

/**
 * Clase abstracta que representa la base de datos Room de la aplicación (archivo "camino_db").
 * Configura las 7 entidades (tablas) y la versión del esquema.
 */
@Database(
    entities = [
        UsuarioEntity::class,
        EstudianteEntity::class,
        ActividadEntity::class,
        PreguntaEntity::class,
        ResultadoEntity::class,
        RespuestaEntity::class,
        EscuelaEntity::class
    ],
    version = 2, // 2: usuarios ahora tiene nombre, curso y foto
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // Proveedores de los DAO para interactuar con cada tabla
    abstract val usuarioDao: UsuarioDao
    abstract val estudianteDao: EstudianteDao
    abstract val actividadDao: ActividadDao
    abstract val preguntaDao: PreguntaDao
    abstract val resultadoDao: ResultadoDao
    abstract val respuestaDao: RespuestaDao
    abstract val escuelaDao: EscuelaDao

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        /**
         * Devuelve SIEMPRE la misma instancia de la base de datos (patrón Singleton).
         * Es importante: si cada ViewModel creara su propia base, los Flow de una pantalla
         * no se enterarían de los cambios hechos en otra.
         */
        fun obtener(context: Context): AppDatabase =
            instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context = context.applicationContext,
                    klass = AppDatabase::class.java,
                    name = "camino_db"
                )
                    // Si se cambia una tabla y se sube "version", la base se recrea con los datos iniciales.
                    .fallbackToDestructiveMigration(dropAllTables = true)
                    .build()
                    .also { instancia = it }
            }
    }
}
