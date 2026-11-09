// Categorías del menú.
enum class CategoriaProducto(val etiqueta: String) {
    PIZZA("pizza"),
    BEBIDA("bebida"),
    INGREDIENTE("ingrediente"),
    COMBO("combo")
}

// Clase abierta (open) para que cada producto del menú sea una subclase con su propia anotación.
open class Producto(
    val nombre: String,
    val precio: Int,
    val ingredientes: Map<String, Int> = emptyMap(),
    val categoria: CategoriaProducto = CategoriaProducto.PIZZA
) {
    // Validaciones de producto
    init {
        require(nombre.isNotBlank()) { "El producto debe tener un nombre." }
        require(precio > 0) { "El precio de '$nombre' debe ser mayor a 0 (recibido: $precio)." }
        require(categoria == CategoriaProducto.BEBIDA || ingredientes.isNotEmpty()) {
            "'$nombre' debe tener al menos un ingrediente."
        }
    }

    fun detallarProducto(): String {
        val base = "$nombre - ${formatearPrecio(precio)} (${categoria.etiqueta})"
        return if (ingredientes.isEmpty()) base else "$base | Ingredientes: ${ingredientes.keys.joinToString(", ")}"
    }

    // Igualdad, dos productos son el mismo si coinciden nombre y categoría.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Producto) return false
        return nombre.trim().equals(other.nombre.trim(), ignoreCase = true) && categoria == other.categoria
    }

    // Se calcula con los mismos atributos que equals.
    override fun hashCode(): Int = 31 * nombre.trim().lowercase().hashCode() + categoria.hashCode()

    override fun toString(): String = "Producto(nombre=$nombre, categoria=${categoria.etiqueta}, precio=$precio)"
}
