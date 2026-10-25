// Reglas de negocio de la pizzería.

// Regla de stock: cada pizza consume 1 unidad de ingredientes.
const val INGREDIENTES_POR_PIZZA = 1

// Descuento del 10% si el pedido supera las 3 pizzas (4 o más). Si no, 0.
fun calcularDescuento(subtotal: Int, cantidad: Int): Int {
    return if (cantidad > 3) subtotal * 10 / 100 else 0
}

// Total del pedido = precio unitario x cantidad, menos el descuento.
fun calcularTotal(pizza: Pizza, cantidad: Int): Int {
    val subtotal = pizza.precioUnitario * cantidad
    val descuento = calcularDescuento(subtotal, cantidad)
    return subtotal - descuento
}

// Verifica si alcanzan los ingredientes. Si alcanzan, descuenta el inventario
// y devuelve true. Si no, informa y devuelve false.
fun verificarStock(pizza: Pizza, cantidad: Int): Boolean {
    val requeridos = cantidad * INGREDIENTES_POR_PIZZA

    return if (pizza.ingredientesDisponibles >= requeridos) {
        pizza.ingredientesDisponibles -= requeridos
        println("Stock confirmado. Ingredientes restantes: ${pizza.ingredientesDisponibles}")
        true
    } else {
        println("No hay ingredientes suficientes para preparar el pedido.")
        println("Requeridos: $requeridos | Disponibles: ${pizza.ingredientesDisponibles}")
        false
    }
}

// Busca el primer repartidor disponible y lo marca como ocupado.
// Devuelve null si todos están ocupados.
fun asignarRepartidor(repartidores: List<Repartidor>): Repartidor? {
    val repartidor = repartidores.find { it.disponible }

    if (repartidor != null) {
        repartidor.disponible = false
    } else {
        println("No hay repartidores disponibles por el momento.")
    }

    return repartidor
}