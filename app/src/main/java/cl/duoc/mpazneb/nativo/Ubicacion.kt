package cl.duoc.mpazneb.nativo

import android.annotation.SuppressLint
import android.content.Context
import android.location.Location
import android.location.LocationManager
import android.os.CancellationSignal
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull

/**
 * RECURSO NATIVO: GPS.
 * Obtiene la ubicación actual con el LocationManager de Android (sin librerías extra).
 * Se usa para fijar dónde está la escuela y para revisar que el estudiante practique dentro de ella.
 * Permisos: ACCESS_FINE_LOCATION / ACCESS_COARSE_LOCATION, pedidos en tiempo de ejecución.
 */
class Ubicacion(context: Context) {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager

    fun activada(): Boolean =
        locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
            locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

    /** Devuelve la ubicación actual, o null si no se obtuvo en 10 segundos. */
    @SuppressLint("MissingPermission") // la pantalla pide el permiso antes de llamar a esta función
    suspend fun obtener(): Location? {
        val proveedor = if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            LocationManager.GPS_PROVIDER
        } else {
            LocationManager.NETWORK_PROVIDER
        }
        val nueva = withTimeoutOrNull(10_000) {
            suspendCancellableCoroutine<Location?> { continuacion ->
                val senal = CancellationSignal()
                continuacion.invokeOnCancellation { senal.cancel() }
                locationManager.getCurrentLocation(proveedor, senal, Runnable::run) { lugar ->
                    continuacion.resume(lugar)
                }
            }
        }
        // Si no llegó una ubicación nueva, se usa la última conocida.
        return nueva ?: locationManager.getLastKnownLocation(proveedor)
    }

    companion object {
        /** Distancia en metros entre dos puntos. */
        fun distancia(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Int {
            val resultado = FloatArray(1)
            Location.distanceBetween(lat1, lon1, lat2, lon2, resultado)
            return resultado[0].toInt()
        }
    }
}
