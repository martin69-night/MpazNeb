package cl.duoc.mpazneb.nativo

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import cl.duoc.mpazneb.R

/**
 * RECURSO NATIVO: NOTIFICACIONES.
 * Avisa cuando un estudiante termina una actividad y cuántas estrellas ganó.
 * Permiso: POST_NOTIFICATIONS (Android 13+), pedido en la pestaña Escuela del panel.
 */
class Notificador(private val context: Context) {

    /** Android 8+ exige un canal antes de mostrar notificaciones. Se crea al abrir la app. */
    fun crearCanal() {
        val canal = NotificationChannel(CANAL, "Avance de estudiantes", NotificationManager.IMPORTANCE_DEFAULT)
        context.getSystemService(NotificationManager::class.java)?.createNotificationChannel(canal)
    }

    fun tienePermiso(): Boolean =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    @SuppressLint("MissingPermission") // se revisa con tienePermiso() justo antes
    fun actividadTerminada(alias: String, actividad: String, estrellas: Int) {
        if (!tienePermiso()) return
        val notificacion = NotificationCompat.Builder(context, CANAL)
            .setSmallIcon(R.drawable.ic_notificacion)
            .setContentTitle("¡$alias terminó «$actividad»!")
            .setContentText("Ganó " + "⭐".repeat(estrellas) + " de práctica.")
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(context).notify(System.currentTimeMillis().toInt(), notificacion)
    }

    private companion object {
        const val CANAL = "avance"
    }
}
