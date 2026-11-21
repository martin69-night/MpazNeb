package cl.duoc.mpazneb.data.remote

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Singleton que contiene la configuración de Retrofit del MICROSERVICIO PROPIO (igual que la Guía 14).
 *
 * URL: http://localhost:8080/ funciona en el celular (USB) y en el emulador porque
 * iniciar-microservicio.bat ejecuta "adb reverse tcp:8080 tcp:8080", que redirige el
 * puerto 8080 del teléfono al puerto 8080 del PC donde corre Spring Boot.
 * (Sin adb reverse, en el emulador se puede usar http://10.0.2.2:8080/)
 */
object RetrofitInstance {

    const val BASE_URL = "http://localhost:8080/"

    /** Muestra cada petición y respuesta en Logcat (filtrar por "okhttp"). */
    private val logging = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

    private val http = OkHttpClient.Builder()
        .addInterceptor(logging)
        .connectTimeout(5, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    // Se instancia el servicio de la API una sola vez
    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(http)
            .addConverterFactory(GsonConverterFactory.create()) // Conversor JSON
            .build()
            .create(ApiService::class.java) // Implementa la interfaz ApiService
    }
}
