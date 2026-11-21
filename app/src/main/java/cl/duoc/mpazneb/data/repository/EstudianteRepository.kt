package cl.duoc.mpazneb.data.repository

import cl.duoc.mpazneb.data.local.AppDatabase
import cl.duoc.mpazneb.data.local.dao.EstudianteDao
import cl.duoc.mpazneb.data.local.dao.RespuestaDao
import cl.duoc.mpazneb.data.local.dao.ResultadoDao
import cl.duoc.mpazneb.data.local.entity.EstudianteEntity
import cl.duoc.mpazneb.data.local.entity.RespuestaEntity
import cl.duoc.mpazneb.data.local.entity.ResultadoEntity
import cl.duoc.mpazneb.data.remote.EstudiantesApiService
import cl.duoc.mpazneb.data.remote.ObservacionRequest
import cl.duoc.mpazneb.data.remote.RetrofitInstance
import cl.duoc.mpazneb.data.remote.exigirExito
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio de estudiantes y de su avance (resultados y respuestas).
 *
 * PERSISTENCIA REMOTA (microservicio estudiantes-service) + LOCAL (Room):
 * - Registrar, guardar la observación y eliminar van primero al microservicio; si responde bien, se guarda en Room.
 * - Cada intento terminado se guarda en Room y además se envía al microservicio. Si no hay red, el intento
 *   queda guardado en el teléfono igual: la práctica del estudiante nunca se interrumpe.
 * - Las lecturas salen de Room (Flow).
 *
 * Recibe los DAO y la API por el constructor para poder reemplazarlos por mocks (MockK) en las pruebas.
 */
class EstudianteRepository(
    private val estudianteDao: EstudianteDao,
    private val resultadoDao: ResultadoDao,
    private val respuestaDao: RespuestaDao,
    private val api: EstudiantesApiService
) {
    /** Constructor que usan los ViewModels: la base de datos real y el cliente Retrofit real. */
    constructor(db: AppDatabase) : this(db.estudianteDao, db.resultadoDao, db.respuestaDao, RetrofitInstance.estudiantesApi)

    // ---------------- Lecturas (Room) ----------------

    fun observarTodos(): Flow<List<EstudianteEntity>> = estudianteDao.getAll()
    fun observar(id: Long): Flow<EstudianteEntity?> = estudianteDao.observeById(id)
    suspend fun obtener(id: Long): EstudianteEntity? = estudianteDao.getById(id)

    /** Validación contra la base: ¿ya hay un estudiante con ese apodo en el curso? */
    suspend fun aliasRepetido(alias: String, curso: Int): Boolean =
        estudianteDao.contarConAlias(alias.trim(), curso) > 0

    fun observarResultados(estudianteId: Long): Flow<List<ResultadoEntity>> = resultadoDao.observeByEstudiante(estudianteId)
    fun observarRespuestas(estudianteId: Long): Flow<List<RespuestaEntity>> = respuestaDao.observeByEstudiante(estudianteId)
    suspend fun intentosUsados(estudianteId: Long, actividadId: Long): Int = resultadoDao.contarIntentos(estudianteId, actividadId)

    // ---------------- Microservicio estudiantes-service ----------------

    /** GET /api/estudiantes: trae los estudiantes del microservicio y los guarda en Room. */
    suspend fun sincronizar() {
        estudianteDao.upsertAll(api.listarEstudiantes())
    }

    /** POST /api/estudiantes. El PIN viaja y se guarda como hash, nunca tal cual. */
    suspend fun crear(alias: String, curso: Int, pin: String, fotoRuta: String?) {
        val nuevo = EstudianteEntity(alias = alias.trim(), curso = curso, pinHash = hashSha256(pin), fotoRuta = fotoRuta)
        estudianteDao.upsert(api.crearEstudiante(nuevo))
    }

    /** PATCH /api/estudiantes/{id}/observacion. */
    suspend fun guardarObservacion(estudianteId: Long, texto: String) {
        val actualizado = api.guardarObservacion(estudianteId, ObservacionRequest(texto.trim()))
        estudianteDao.upsert(actualizado)
    }

    /** DELETE /api/estudiantes/{id}: el microservicio borra al estudiante y su historial; luego se borra de Room. */
    suspend fun eliminar(estudianteId: Long) {
        api.eliminarEstudiante(estudianteId).exigirExito()
        respuestaDao.deleteByEstudiante(estudianteId)
        resultadoDao.deleteByEstudiante(estudianteId)
        estudianteDao.deleteById(estudianteId)
    }

    /**
     * Guarda un intento terminado: primero el resultado y cada respuesta en Room (con el id del resultado),
     * y después lo envía al microservicio (POST /api/estudiantes/{id}/resultados).
     */
    suspend fun registrarIntento(estudianteId: Long, actividadId: Long, respuestas: List<RespuestaEntity>): ResultadoEntity {
        val resultado = ResultadoEntity(
            estudianteId = estudianteId,
            actividadId = actividadId,
            aciertos = respuestas.count { it.esCorrecta },
            total = respuestas.size
        )
        val id = resultadoDao.insert(resultado)
        respuestaDao.insertAll(respuestas.map { it.copy(resultadoId = id) })
        enviarResultado(resultado)
        return resultado.copy(id = id)
    }

    /** Si no hay conexión, el intento ya quedó en Room: solo se registra el aviso en Logcat. */
    private suspend fun enviarResultado(resultado: ResultadoEntity) {
        try {
            api.registrarResultado(resultado.estudianteId, resultado)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            println("No se pudo enviar el resultado al microservicio: ${e.message}")
        }
    }
}
