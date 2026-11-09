import java.time.DateTimeException
import java.time.LocalDate
import java.time.format.DateTimeFormatter

// Validación y formato de textos

// Valores de relleno para clientes sin datos de contacto.
const val TELEFONO_POR_DEFECTO = "+56 (000) 000-0000"
const val CORREO_POR_DEFECTO = "correo@invalido.com"

private val REGEX_CORREO = Regex("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")

// Teléfono en 4 grupos, país (2 dígitos), área (3), y dos bloques (3 y 4).
private val REGEX_TELEFONO = Regex("""^(\d{2})(\d{3})(\d{3})(\d{4})$""")

// Código de país que se agrega cuando el usuario escribe solo los 10 dígitos.
private const val CODIGO_PAIS_DEFECTO = "56"

private val REGEX_NO_DIGITO = Regex("""\D""")

// Fecha con formato (dd/mm/aaaa).
private val REGEX_FECHA = Regex("""^(\d{1,2})/(\d{1,2})/(\d{4})$""")

private val FORMATO_FECHA = DateTimeFormatter.ofPattern("dd/MM/yyyy")

// Rango permitido para la cantidad de un producto.
val RANGO_CANTIDAD = 1..100

// Formato uniforme del teléfono - +XX (XXX) XXX-XXXX.
fun formatearTelefono(texto: String): String? {
    val digitos = texto.replace(REGEX_NO_DIGITO, "")
    val completo = when (digitos.length) {
        10 -> CODIGO_PAIS_DEFECTO + digitos
        12 -> digitos
        else -> return null
    }
    val (pais, area, bloque1, bloque2) = REGEX_TELEFONO.matchEntire(completo)?.destructured ?: return null
    return "+$pais ($area) $bloque1-$bloque2"
}

// Regla del teléfono
fun esTelefonoValido(texto: String): Boolean = formatearTelefono(texto) != null

// Regla del correo
fun esCorreoValido(texto: String): Boolean = REGEX_CORREO.matches(texto)

// Mensaje de error claro según lo que le falte al correo
fun describirErrorCorreo(texto: String): String = when {
    texto.isBlank() -> "El correo no puede estar vacío."
    '@' !in texto -> "Correo no válido: falta la @ (ejemplo: nombre@dominio.com)."
    texto.count { it == '@' } > 1 -> "Correo no válido: solo puede tener una @."
    '.' !in texto.substringAfter('@') -> "Correo no válido: falta el dominio después de la @ (ejemplo: nombre@dominio.com)."
    else -> "Correo no válido (ejemplo: nombre@dominio.com)."
}

// Regla de la cantidad (rango).
fun esCantidadValida(cantidad: Int): Boolean = cantidad in RANGO_CANTIDAD

// Convierte "dd/mm/aaaa" en fecha.
fun parsearFecha(texto: String): LocalDate? {
    val (dia, mes, anio) = REGEX_FECHA.matchEntire(texto.trim())?.destructured ?: return null
    return try {
        LocalDate.of(anio.toInt(), mes.toInt(), dia.toInt())
    } catch (e: DateTimeException) {
        null
    }
}

fun formatearFecha(fecha: LocalDate): String = fecha.format(FORMATO_FECHA)

// Regla de nombre de cliente.
fun formatearNombre(texto: String): String =
    texto.trim()
        .split(" ")
        .filter { it.isNotEmpty() }
        .joinToString(" ") { palabra -> palabra.lowercase().replaceFirstChar { it.uppercase() } }

// Regla de precios.
fun formatearPrecio(valor: Int): String = "$" + "%,d".format(valor).replace(',', '.')

fun convertirEntero(texto: String): Int? =
    try {
        texto.trim().toInt()
    } catch (e: NumberFormatException) {
        null
    }
