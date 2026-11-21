package cl.duoc.mpazneb.data.repository

import cl.duoc.mpazneb.data.local.AppDatabase
import cl.duoc.mpazneb.data.local.entity.EstudianteEntity
import cl.duoc.mpazneb.data.local.entity.RespuestaEntity
import cl.duoc.mpazneb.data.local.entity.ResultadoEntity
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio de estudiantes y de su avance (resultados y respuestas).
 */
class EstudianteRepository(private val db: AppDatabase) {

    fun observarTodos(): Flow<List<EstudianteEntity>> = db.estudianteDao.getAll()
    fun observar(id: Long): Flow<EstudianteEntity?> = db.estudianteDao.observeById(id)
    suspend fun obtener(id: Long): EstudianteEntity? = db.estudianteDao.getById(id)

    /** Validación contra la base: ¿ya hay un estudiante con ese apodo en el curso? */
    suspend fun aliasRepetido(alias: String, curso: Int): Boolean =
        db.estudianteDao.contarConAlias(alias.trim(), curso) > 0

    /** Guarda un estudiante nuevo. El PIN se guarda como hash. */
    suspend fun crear(alias: String, curso: Int, pin: String, fotoRuta: String?) {
        db.estudianteDao.insert(EstudianteEntity(alias = alias.trim(), curso = curso, pinHash = hashSha256(pin), fotoRuta = fotoRuta))
    }

    suspend fun guardarObservacion(estudianteId: Long, texto: String) {
        val actual = db.estudianteDao.getById(estudianteId) ?: return
        db.estudianteDao.update(actual.copy(observacion = texto.trim()))
    }

    fun observarResultados(estudianteId: Long): Flow<List<ResultadoEntity>> = db.resultadoDao.observeByEstudiante(estudianteId)
    fun observarRespuestas(estudianteId: Long): Flow<List<RespuestaEntity>> = db.respuestaDao.observeByEstudiante(estudianteId)
    suspend fun intentosUsados(estudianteId: Long, actividadId: Long): Int = db.resultadoDao.contarIntentos(estudianteId, actividadId)

    /** Guarda un intento terminado: primero el resultado y luego cada respuesta con el id del resultado. */
    suspend fun registrarIntento(estudianteId: Long, actividadId: Long, respuestas: List<RespuestaEntity>): ResultadoEntity {
        val resultado = ResultadoEntity(
            estudianteId = estudianteId,
            actividadId = actividadId,
            aciertos = respuestas.count { it.esCorrecta },
            total = respuestas.size
        )
        val id = db.resultadoDao.insert(resultado)
        db.respuestaDao.insertAll(respuestas.map { it.copy(resultadoId = id) })
        return resultado.copy(id = id)
    }
}
