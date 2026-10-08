package mx.tec.charla.data.qr

import android.content.Context
import android.net.Uri
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import com.google.mlkit.vision.common.InputImage
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.tasks.await

/** Lee códigos QR con el escáner de Play Services o desde una imagen existente. */
class LectorQr @Inject constructor(@ApplicationContext private val context: Context) {

    private val soloQr = BarcodeScannerOptions.Builder()
        .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
        .build()

    /** null si el usuario cierra el escáner sin leer un código. */
    suspend fun escanear(): String? = suspendCancellableCoroutine { continuacion ->
        val opciones = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
        GmsBarcodeScanning.getClient(context, opciones).startScan()
            .addOnSuccessListener { continuacion.resume(it.rawValue) }
            .addOnCanceledListener { continuacion.resume(null) }
            .addOnFailureListener { continuacion.resumeWithException(it) }
    }

    /** Texto del primer QR de la imagen, o null si no contiene uno. */
    suspend fun leerDeImagen(imagen: Uri): String? {
        val entrada = InputImage.fromFilePath(context, imagen)
        val codigos = BarcodeScanning.getClient(soloQr).process(entrada).await()
        return codigos.firstNotNullOfOrNull { it.rawValue }
    }
}