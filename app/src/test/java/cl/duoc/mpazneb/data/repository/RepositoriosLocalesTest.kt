package cl.duoc.mpazneb.data.repository

import cl.duoc.mpazneb.data.local.AppDatabase
import cl.duoc.mpazneb.data.local.dao.EscuelaDao
import cl.duoc.mpazneb.data.local.dao.EstudianteDao
import cl.duoc.mpazneb.data.local.dao.RespuestaDao
import cl.duoc.mpazneb.data.local.dao.ResultadoDao
import cl.duoc.mpazneb.data.local.dao.UsuarioDao
import cl.duoc.mpazneb.data.local.entity.EscuelaEntity
import cl.duoc.mpazneb.data.local.entity.EstudianteEntity
import cl.duoc.mpazneb.data.local.entity.UsuarioEntity
import cl.duoc.mpazneb.model.Perfil
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest

/**
 * Pruebas de los repositorios que trabajan solo con Room (usuarios y escuela).
 * Se mockea con MockK la base de datos completa (AppDatabase) y cada uno de sus DAO.
 */
class RepositoriosLocalesTest : StringSpec({

    val usuarioDao = mockk<UsuarioDao>(relaxed = true)
    val estudianteDao = mockk<EstudianteDao>(relaxed = true)
    val resultadoDao = mockk<ResultadoDao>(relaxed = true)
    val respuestaDao = mockk<RespuestaDao>(relaxed = true)
    val escuelaDao = mockk<EscuelaDao>(relaxed = true)
    val db = mockk<AppDatabase>()

    val docente = UsuarioEntity(id = 1, alias = "docente.demo", claveHash = hashSha256("Mpaz2026"), perfil = Perfil.DOCENTE, nombre = "Profesora Carmen", curso = 1)
    val luna = EstudianteEntity(id = 7, alias = "Luna", curso = 1, pinHash = hashSha256("1234"))

    beforeTest {
        clearAllMocks(answers = false)
        every { db.usuarioDao } returns usuarioDao
        every { db.estudianteDao } returns estudianteDao
        every { db.resultadoDao } returns resultadoDao
        every { db.respuestaDao } returns respuestaDao
        every { db.escuelaDao } returns escuelaDao
    }
    afterTest { Sesion.usuario = null }

    // ---------- UsuarioRepository ----------

    "hashSha256() genera el hash conocido de 1234" {
        hashSha256("1234") shouldBe "03ac674216f3e15c761ee1a5e255f067953623c8b388b4459e13f978d7c846f4"
    }

    "un login correcto abre la sesión" {
        coEvery { usuarioDao.buscarPorAlias("docente.demo") } returns docente
        runTest {
            UsuarioRepository(db).iniciarSesion("  Docente.Demo ", "Mpaz2026") shouldBe docente
            Sesion.usuario shouldBe docente
        }
    }

    "un login con clave incorrecta o usuario inexistente devuelve null" {
        coEvery { usuarioDao.buscarPorAlias("docente.demo") } returns docente
        coEvery { usuarioDao.buscarPorAlias("nadie") } returns null
        val repo = UsuarioRepository(db)
        runTest {
            repo.iniciarSesion("docente.demo", "otra") shouldBe null
            repo.iniciarSesion("nadie", "Mpaz2026") shouldBe null
            Sesion.usuario shouldBe null
        }
    }

    "el PIN del estudiante se compara con su hash" {
        coEvery { estudianteDao.getById(7) } returns luna
        val repo = UsuarioRepository(db)
        runTest {
            repo.pinCorrecto(7, "1234") shouldBe true
            repo.pinCorrecto(7, "0000") shouldBe false
        }
    }

    "cambiar la foto actualiza la sesión y cerrar sesión la borra" {
        Sesion.usuario = docente
        val repo = UsuarioRepository(db)
        runTest {
            repo.cambiarFoto(docente, "/fotos/carmen.jpg")
            coVerify { usuarioDao.update(docente.copy(fotoRuta = "/fotos/carmen.jpg")) }
            Sesion.usuario?.fotoRuta shouldBe "/fotos/carmen.jpg"
            repo.cerrarSesion()
            Sesion.usuario shouldBe null
        }
    }

    "se observa el usuario y el docente del estudiante" {
        every { usuarioDao.observeById(1) } returns flowOf(docente)
        every { usuarioDao.observeDocenteDe(7) } returns flowOf(docente)
        val repo = UsuarioRepository(db)
        runTest {
            repo.observar(1).first() shouldBe docente
            repo.observarDocenteDe(7).first() shouldBe docente
        }
    }

    // ---------- EscuelaRepository ----------

    "la escuela sin configurar usa los valores por defecto" {
        every { escuelaDao.observe() } returns flowOf(null)
        runTest { EscuelaRepository(db).observar().first() shouldBe EscuelaEntity() }
    }

    "se guarda la ubicación, el radio y el modo solo en la escuela" {
        coEvery { escuelaDao.get() } returns EscuelaEntity()
        val repo = EscuelaRepository(db)
        runTest {
            repo.guardarUbicacion(-33.59, -70.69)
            repo.cambiarRadio(500)
            repo.cambiarSoloEnEscuela(true)
            coVerify { escuelaDao.save(EscuelaEntity(latitud = -33.59, longitud = -70.69)) }
            coVerify { escuelaDao.save(EscuelaEntity(radioMetros = 500)) }
            coVerify { escuelaDao.save(EscuelaEntity(soloEnEscuela = true)) }
        }
    }

    "si no hay fila de escuela se crea con los valores por defecto" {
        coEvery { escuelaDao.get() } returns null
        runTest {
            EscuelaRepository(db).cambiarRadio(100)
            coVerify { escuelaDao.save(EscuelaEntity(radioMetros = 100)) }
        }
    }
})
