// Punto de entrada (se crean los objetos iniciales, conecta las dependencias y arranca el controlador)

fun crearCatalogo(): List<Producto> = listOf(
    Producto("Pepperoni", 9500, mapOf("masa" to 1, "salsa de tomate" to 1, "queso" to 1, "pepperoni" to 1)),
    Producto("Margarita", 8500, mapOf("masa" to 1, "salsa de tomate" to 1, "queso" to 1, "albahaca" to 1)),
    Producto("Hawaiana", 9000, mapOf("masa" to 1, "salsa de tomate" to 1, "queso" to 1, "jamón" to 1, "piña" to 1))
)

fun crearRepartidores(): List<Repartidor> = listOf(
    Repartidor("Ambar", "+56911111111", 1.2),
    Repartidor("Bastian", "+56922222222", 2.5),
    Repartidor("Luis", "+56933333333", 0.8)
)

// Poblamiento de pedidos iniciales
fun crearPedidosIniciales(catalogo: List<Producto>, repartidores: List<Repartidor>): Array<Pedido> {
    val (pepperoni, margarita, hawaiana) = catalogo

    val enCamino = Pedido(
        1, Cliente("Camila Rojas", "Av. Providencia 1234", "+56987654321", "camila@correo.cl"),
        listOf(pepperoni, pepperoni, margarita), estadoInicial = EstadoPedido.EN_CAMINO
    )
    // Ejemplo de repartidor ocupado
    val luis = repartidores.first { it.nombre == "Luis" }
    luis.ocupar()
    enCamino.asignarRepartidor(luis)

    // Ejemplo de pedido listo
    val listo = Pedido(
        2, Cliente("Diego Soto", "Los Leones 456", "+56911112222", "diego@correo.cl"),
        List(4) { hawaiana }, notas = "Sin cebolla", estadoInicial = EstadoPedido.LISTO
    )

    // Ejemplo de pedido en preparacion
    val enPreparacion = Pedido(
        3, Cliente("Valentina Pérez", "Irarrázaval 789", TELEFONO_POR_DEFECTO, CORREO_POR_DEFECTO),
        listOf(margarita)
    )

    return arrayOf(enCamino, listo, enPreparacion)
}

fun main() {
    val catalogo = crearCatalogo()
    val repartidores = crearRepartidores()

    val operaciones: OperacionesPizzeria = PizzeriaBackend(
        catalogo = catalogo,
        inventario = crearInventarioInicial(),
        repartidores = repartidores,
        pedidosIniciales = crearPedidosIniciales(catalogo, repartidores)
    )

    PizzeriaControlador(operaciones, PizzeriaUI()).iniciar()
}
