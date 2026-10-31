enum class EstadoPedido(val etiqueta: String) {
    EN_PREPARACION("En preparación"),
    LISTO("Listo"),
    EN_CAMINO("En camino")
}

class Pedido(
    val id: Int,
    val cliente: Cliente,
    val productos: List<Producto>,
    val notas: String? = null,
    estadoInicial: EstadoPedido = EstadoPedido.EN_PREPARACION
) {
    init {
        require(productos.isNotEmpty()) { "El pedido N° $id no tiene productos." }
    }

    var estado: EstadoPedido = estadoInicial
        private set

    var repartidor: Repartidor? = null
        private set

    fun asignarRepartidor(nuevoRepartidor: Repartidor?) {
        repartidor = nuevoRepartidor
    }

    val totales: Totales = calcularTotales(productos)

    val cantidadPizzas: Int get() = productos.size

    fun calcularTotal(): Int = totales.total

    // El pedido puede pasar de preparación a listo a en camino.
    fun cambiarEstado(nuevoEstado: EstadoPedido) {
        check(nuevoEstado.ordinal > estado.ordinal) {
            "No se puede pasar de '${estado.etiqueta}' a '${nuevoEstado.etiqueta}'."
        }
        estado = nuevoEstado
    }

    // Pizzas repetidas
    fun resumenProductos(): String =
        productos.groupingBy { it }.eachCount().entries
            .joinToString(", ") { (producto, cantidad) -> "${cantidad}x ${producto.nombre}" }
}
