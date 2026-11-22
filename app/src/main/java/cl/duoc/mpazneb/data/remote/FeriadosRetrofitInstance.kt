package cl.duoc.mpazneb.data.remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// Singleton que contiene la configuración de Retrofit de la API externa (otra URL base, HTTPS)
object FeriadosRetrofitInstance {

    // Se instancia el servicio de la API una sola vez
    val api: FeriadosApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://date.nager.at/") // URL base de la API externa
            .addConverterFactory(GsonConverterFactory.create()) // Conversor JSON
            .build()
            .create(FeriadosApiService::class.java) // Implementa la interfaz FeriadosApiService
    }
}
