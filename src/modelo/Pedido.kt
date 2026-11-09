import java.time.LocalDate

enum class EstadoPedido(val etiqueta: String) {
    EN_PREPARACION("En preparación"),
    LISTO("Listo"),
    EN_CAMINO("En camino"),
    ENTREGADO("Entregado");

    val siguiente: EstadoPedido? get() = values().getOrNull(ordinal + 1)
}

class Pedido(
    val id: Int,
    val cliente: Cliente,
    val productos: List<Producto>,
    val notas: String? = null,
    estadoInicial: EstadoPedido = EstadoPedido.EN_PREPARACION,
    val fecha: LocalDate = LocalDate.now()
) {
    companion object {
        const val ID_COMBINADO = 0
    }

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

    // El total se calcula con la fecha del pedido, dentro del periodo de promoción lleva el descuento adicional.
    val totales: Totales = calcularTotales(productos, fecha)

    val cantidadPizzas: Int get() = contarPizzas(productos)

    fun calcularTotal(): Int = totales.total

    // Productos del pedido que llevan @Promocionable.
    // Se descubren con reflection al procesar el pedido para no repetir una pizza pedida varias veces.
    fun productosPromocionables(): List<ProductoPromocionable> = descubrirPromocionables(productos.distinct())

    // El pedido avanza paso a paso de preparación → listo → en camino → entregado.
    fun cambiarEstado(nuevoEstado: EstadoPedido) {
        check(nuevoEstado == estado.siguiente) {
            val permitido = estado.siguiente?.let { "El único cambio permitido es a '${it.etiqueta}'." }
                ?: "El pedido ya está en su estado final."
            "No se puede pasar de '${estado.etiqueta}' a '${nuevoEstado.etiqueta}'. $permitido"
        }
        estado = nuevoEstado
    }

    // Sobrecarga del operador.
    // Solo se pueden combinar pedidos del mismo cliente.
    operator fun plus(otro: Pedido): Pedido {
        require(cliente == otro.cliente) {
            "Solo se pueden combinar pedidos del mismo cliente (${cliente.nombre} y ${otro.cliente.nombre})."
        }
        val notasUnidas = listOfNotNull(notas, otro.notas).distinct().joinToString(" | ").ifEmpty { null }
        val fechaCombinada = if (fecha.isAfter(otro.fecha)) fecha else otro.fecha
        return Pedido(ID_COMBINADO, cliente, productos + otro.productos, notasUnidas, fecha = fechaCombinada)
    }

    // Desestructuración.
    // El total se calcula aparte, por eso las funciones component se declaran a mano.
    operator fun component1(): Cliente = cliente
    operator fun component2(): List<Producto> = productos
    operator fun component3(): Int = totales.total

    // Pizzas repetidas
    fun resumenProductos(): String =
        productos.groupingBy { it }.eachCount().entries
            .joinToString(", ") { (producto, cantidad) -> "${cantidad}x ${producto.nombre}" }
}
