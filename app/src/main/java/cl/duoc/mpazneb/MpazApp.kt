package cl.duoc.mpazneb

import android.app.Application
import cl.duoc.mpazneb.data.local.AppDatabase
import cl.duoc.mpazneb.data.local.cargarDatosIniciales
import cl.duoc.mpazneb.nativo.Notificador
import kotlinx.coroutines.runBlocking

/**
 * Clase Application: se ejecuta una vez al abrir la app, antes que cualquier pantalla.
 * - Carga los datos de prueba la primera vez (después solo hace un COUNT).
 * - Crea el canal de notificaciones (obligatorio desde Android 8).
 */
class MpazApp : Application() {
    override fun onCreate() {
        super.onCreate()
        runBlocking { cargarDatosIniciales(AppDatabase.obtener(this@MpazApp)) }
        Notificador(this).crearCanal()
    }
}
