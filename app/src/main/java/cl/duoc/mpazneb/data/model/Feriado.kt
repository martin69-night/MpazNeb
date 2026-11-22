package cl.duoc.mpazneb.data.model

/**
 * Esta clase representa un feriado obtenido desde la API externa Nager.Date.
 * Ejemplo de JSON: {"date":"2026-12-08","localName":"Inmaculada Concepción","global":true, ...}
 * Solo se declaran los campos que usa la app; Gson ignora el resto.
 */
data class Feriado(
    val date: String,        // Fecha "2026-12-08"
    val localName: String,   // Nombre en español
    val global: Boolean      // true = nacional; false = solo algunas regiones
)
