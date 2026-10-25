// Modelo que representa un pedido - cuántas pizzas, cuánto cuesta y su estado.
data class Pedido(
    val idPedido: Int,
    val cantidadPizzas: Int,
    var totalPedido: Int = 0,                  // se calcula después, con el descuento
    var estadoPedido: String = "Preparación",  // se cambia a "En Camino" y "Entregado"
    var repartidor: Repartidor? = null         // se asigna una vez confirmado (nullable).
)