data class Producto(
    val nombre: String,
    val precio: Int,
    val ingredientes: Map<String, Int>
) {
    // Validaciones de producto
    init {
        require(precio > 0) { "El precio de '$nombre' debe ser mayor a 0 (recibido: $precio)." }
        require(ingredientes.isNotEmpty()) { "'$nombre' debe tener al menos un ingrediente." }
    }

    fun detallarProducto(): String =
        "$nombre - ${formatearPrecio(precio)} | Ingredientes: ${ingredientes.keys.joinToString(", ")}"
}
