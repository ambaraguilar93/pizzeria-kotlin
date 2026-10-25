// Punto de entrada: junta todas las piezas y muestra el resumen del pedido.
fun main() {
    println("=====================================")
    println("==  BIENVENIDO A LA PIZZERÍA       ==")
    println("=====================================")

    // Datos iniciales del negocio
    val pizza: Pizza = Pizza("Pepperoni", 9500, 10)
    val repartidores: List<Repartidor> = listOf(
        Repartidor("Ambar"),
        Repartidor("Bastian"),
        Repartidor("Luis")
    )

    // Registrar al cliente y mostrar los datos registrados
    val cliente: Cliente = registrarCliente()

    println("\nDatos registrados:")
    println("  Nombre:    ${cliente.nombre}")
    println("  Dirección: ${cliente.direccion}")
    println("  Teléfono:  ${cliente.telefono}")

    // Datos del pedido
    println("\n--- NUEVO PEDIDO ---")
    println("Pizza: ${pizza.nombrePizza} - Precio unitario: $${pizza.precioUnitario}")
    println("Ingredientes disponibles: ${pizza.ingredientesDisponibles}")
    val cantidad: Int = leerEntero("Cantidad de pizzas: ", 0)

    if (cantidad <= 0) {
        println("Cantidad no válida. El pedido no puede ser procesado.")
        return
    }

    // Calcular total con descuento
    val pedido: Pedido = Pedido(idPedido = 1, cantidadPizzas = cantidad)
    val subtotal: Int = pizza.precioUnitario * cantidad
    val descuento: Int = calcularDescuento(subtotal, cantidad)
    pedido.totalPedido = calcularTotal(pizza, cantidad)

    // Verificar stock y reducir inventario
    println("\n--- VERIFICACIÓN DE STOCK ---")
    if (!verificarStock(pizza, cantidad)) {
        println("Pedido cancelado por falta de ingredientes.")
        return
    }

    // Asignar repartidor
    pedido.repartidor = asignarRepartidor(repartidores)

    // Resumen del pedido
    println("\n=====================================")
    println("        RESUMEN DEL PEDIDO")
    println("=====================================")
    println("CLIENTE")
    println("  Nombre:    ${cliente.nombre}")
    println("  Dirección: ${cliente.direccion}")
    println("  Teléfono:  ${cliente.telefono}")
    println("PIZZA")
    println("  Nombre:    ${pizza.nombrePizza}")
    println("  Precio:    $${pizza.precioUnitario}")
    println("PEDIDO N° ${pedido.idPedido}")
    println("  Cantidad:  ${pedido.cantidadPizzas}")
    println("  Subtotal:  $$subtotal")
    println("  Descuento: $$descuento")
    println("  Total:     $${pedido.totalPedido}")
    println("  Estado:    ${pedido.estadoPedido}")
    println("  Repartidor: ${pedido.repartidor?.nombre ?: "Sin asignar"}")
    println("=====================================")
}