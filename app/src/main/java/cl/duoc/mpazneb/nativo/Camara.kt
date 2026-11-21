package cl.duoc.mpazneb.nativo

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import java.io.File

/**
 * RECURSO NATIVO: CÁMARA.
 * La foto se toma con la cámara del sistema (contrato TakePicturePreview, que entrega un Bitmap).
 * Aquí solo se guarda como JPG en la carpeta privada de la app y se devuelve la ruta,
 * que después queda guardada en la base de datos.
 * Permiso: CAMERA, pedido en tiempo de ejecución (ver ui/components/BotonCamara).
 * La galería (selector de fotos del sistema) no necesita permiso.
 */
class Camara(private val context: Context) {

    fun guardarFoto(foto: Bitmap): String {
        val archivo = File(context.filesDir, "foto_${System.currentTimeMillis()}.jpg")
        archivo.outputStream().use { salida -> foto.compress(Bitmap.CompressFormat.JPEG, 90, salida) }
        return archivo.absolutePath
    }

    /** Copia una foto elegida en la galería a la carpeta de la app, achicada a máx. 800 px. */
    fun guardarDesdeGaleria(uri: Uri): String {
        val fuente = ImageDecoder.createSource(context.contentResolver, uri)
        val foto = ImageDecoder.decodeBitmap(fuente) { decoder, info, _ ->
            val escala = maxOf(1, maxOf(info.size.width, info.size.height) / 800)
            decoder.setTargetSize(info.size.width / escala, info.size.height / escala)
            decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE // para poder comprimirla a JPG
        }
        return guardarFoto(foto)
    }
}
