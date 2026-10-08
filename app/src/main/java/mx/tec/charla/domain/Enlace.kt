package mx.tec.charla.domain

/** Extrae el origen http(s) de un QR, una captura o un texto compartido. */
object Enlace {
    private val DIRECCION = Regex("""https?://[A-Za-z0-9.-]+(:\d{1,5})?""")

    fun servidorDesde(texto: String): String? = DIRECCION.find(texto)?.value
}