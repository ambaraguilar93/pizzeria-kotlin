// Validación y formato de textos

const val TELEFONO_POR_DEFECTO = "(000) 000-0000"
const val CORREO_POR_DEFECTO = "correo@invalido.com"

private val REGEX_CORREO = Regex("^[\\w.+-]+@[\\w-]+(\\.[\\w-]+)+$")

// Regla del teléfono
fun esTelefonoValido(texto: String): Boolean {
    val digitos = texto.removePrefix("+")
    return digitos.length in 8..11 && digitos.all { it.isDigit() }
}

// Regla del correo
fun esCorreoValido(texto: String): Boolean = REGEX_CORREO.matches(texto)

// Regla de nombre de cliente
fun formatearNombre(texto: String): String =
    texto.trim()
        .split(" ")
        .filter { it.isNotEmpty() }
        .joinToString(" ") { palabra -> palabra.lowercase().replaceFirstChar { it.uppercase() } }

// Regla de precios
fun formatearPrecio(valor: Int): String = "$" + "%,d".format(valor).replace(',', '.')

fun convertirEntero(texto: String): Int? =
    try {
        texto.trim().toInt()
    } catch (e: NumberFormatException) {
        null
    }
