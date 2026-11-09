import java.io.File
import java.io.IOException
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

// Guarda el resumen de la Semana 3 en un archivo .txt (el mismo contenido que se ve en consola).

const val ARCHIVO_RESUMEN = "resumen_pizzeria.txt"

private val FORMATO_FECHA_HORA = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm")

// Arma el texto del resumen con las mismas secciones que muestra la consola.
fun textoResumen(secciones: List<SeccionResumen>, ahora: LocalDateTime = LocalDateTime.now()): String = buildString {
    val linea = "=".repeat(53)
    appendLine(linea)
    appendLine("   RESUMEN DE LA PIZZERÍA - SEMANA 3")
    appendLine("   Generado: ${ahora.format(FORMATO_FECHA_HORA)}")
    appendLine(linea)
    for (seccion in secciones) {
        appendLine()
        appendLine("[ ${seccion.titulo} ]")
        seccion.lineas.forEach { appendLine("  $it") }
    }
    appendLine(linea)
}

// Escribe el archivo (UTF-8) y devuelve su ubicación. Lanza IOException si no se puede escribir.
fun guardarResumen(secciones: List<SeccionResumen>, nombreArchivo: String = ARCHIVO_RESUMEN): File {
    val archivo = File(nombreArchivo)
    archivo.writeText(textoResumen(secciones), Charsets.UTF_8)
    return archivo.absoluteFile
}