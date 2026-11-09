// Resultado de intentar registrar un cliente o un producto.
sealed interface ResultadoRegistro<out T> {
    data class Registrado<out T>(val elemento: T) : ResultadoRegistro<T>
    data class Repetido<out T>(val existente: T, val intento: T) : ResultadoRegistro<T>
}

// Registro de elementos sin duplicados.
class Registro<T : Any>(elementosIniciales: Collection<T> = emptyList()) {

    private val elementos = LinkedHashMap<T, T>()

    init {
        elementosIniciales.forEach { registrar(it) }
    }

    fun registrar(elemento: T): ResultadoRegistro<T> {
        val existente = elementos[elemento]
        if (existente != null) return ResultadoRegistro.Repetido(existente, elemento)

        elementos[elemento] = elemento
        return ResultadoRegistro.Registrado(elemento)
    }

    // Reemplaza el elemento ya registrado por una versión actualizada.
    fun actualizar(elemento: T): Boolean {
        if (elemento !in elementos) return false
        elementos[elemento] = elemento
        return true
    }

    fun buscar(elemento: T): T? = elementos[elemento]

    fun listar(): List<T> = elementos.values.toList()
}
