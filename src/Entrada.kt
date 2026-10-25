// Funciones que leen datos de la consola.
// Cada dato se pide hasta 3 veces, si sigue siendo inválido, se usa un valor por defecto.

const val MAX_INTENTOS = 3

// Lee un texto. Por defecto solo exige que no esté vacío.
fun leerTexto(
    mensaje: String,
    valorPorDefecto: String,
    mensajeError: String = "El dato no puede estar vacío.",
    esValido: (String) -> Boolean = { it.isNotBlank() }
): String {
    for (intento in 1..MAX_INTENTOS) {
        print(mensaje)
        val texto = readlnOrNull()?.trim() ?: ""

        if (esValido(texto)) {
            return texto
        }

        val restantes = MAX_INTENTOS - intento
        if (restantes > 0) {
            println("$mensajeError Intentos restantes: $restantes")
        }
    }

    println("Se usará el valor por defecto: $valorPorDefecto")
    return valorPorDefecto
}

// Lee un número entero.
fun leerEntero(mensaje: String, valorPorDefecto: Int): Int {
    for (intento in 1..MAX_INTENTOS) {
        print(mensaje)
        val numero = readlnOrNull()?.trim()?.toIntOrNull()

        if (numero != null) {
            return numero
        }

        val restantes = MAX_INTENTOS - intento
        if (restantes > 0) {
            println("Debes ingresar un número entero. Intentos restantes: $restantes")
        }
    }

    println("Se usará el valor por defecto: $valorPorDefecto")
    return valorPorDefecto
}

// Regla del teléfono: "+" opcional y entre 8 y 11 dígitos.
fun esTelefonoValido(texto: String): Boolean {
    val digitos = texto.removePrefix("+")
    return digitos.length in 8..11 && digitos.all { it.isDigit() }
}

// Lee un teléfono usando la regla anterior.
fun leerTelefono(): String {
    return leerTexto(
        mensaje = "Teléfono de contacto: ",
        valorPorDefecto = "Sin teléfono",
        mensajeError = "Teléfono no válido (8 a 11 dígitos).",
        esValido = ::esTelefonoValido
    )
}

// Pide los datos del cliente y devuelve un Cliente ya armado.
fun registrarCliente(): Cliente {
    println("\n--- REGISTRO DE CLIENTE ---")
    val nombre = leerTexto("Nombre del cliente: ", "Cliente sin nombre")
    val direccion = leerTexto("Dirección de entrega: ", "Sin dirección")
    val telefono = leerTelefono()

    return Cliente(nombre, direccion, telefono)
}