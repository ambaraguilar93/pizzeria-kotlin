// Pruebas de casos límite

private var pruebasOk = 0
private var pruebasFallidas = 0

private fun verificar(descripcion: String, esperado: Any?, obtenido: Any?) {
    if (esperado == obtenido) {
        pruebasOk++
        println("  [PRUEBA OK]    $descripcion")
    } else {
        pruebasFallidas++
        println("  [PRUEBA FALLÓ] $descripcion -> esperado: $esperado, obtenido: $obtenido")
    }
}

private val pizzaDePrueba = Producto(
    "Pepperoni", 9500,
    mapOf("masa" to 1, "salsa de tomate" to 1, "queso" to 1, "pepperoni" to 1)
)

private val clienteDePrueba = Cliente("Test", "Calle 1", TELEFONO_POR_DEFECTO, CORREO_POR_DEFECTO)

// Inventario con 10 pepperoni exactos, para probar el límite de 10 y 11 pizzas.
private fun inventarioDePrueba(): Inventario =
    mutableMapOf("masa" to 20, "salsa de tomate" to 20, "queso" to 20, "pepperoni" to 10)

private fun backendDePrueba(repartidores: List<Repartidor> = listOf(Repartidor("Test", "000", 1.0))) =
    PizzeriaBackend(listOf(pizzaDePrueba), inventarioDePrueba(), repartidores, emptyArray())

fun ejecutarPruebas() {
    pruebasOk = 0
    pruebasFallidas = 0

    println("\n=== DESCUENTO, IVA Y TOTAL ===")
    val esperados = mapOf(
        3 to Totales(28500, 0, 5415, 33915),
        4 to Totales(38000, 3800, 6498, 40698),
        10 to Totales(95000, 9500, 16245, 101745),
        11 to Totales(104500, 10450, 17870, 111920)
    )
    for ((cantidad, totalesEsperados) in esperados) {
        verificar("$cantidad pizzas", totalesEsperados, calcularTotales(List(cantidad) { pizzaDePrueba }))
    }

    println("\n=== INGREDIENTES ===")
    val backend = backendDePrueba()
    verificar("10 pizzas alcanzan", Disponibilidad.Suficiente, backend.verificarDisponibilidad(List(10) { pizzaDePrueba }))
    verificar(
        "11 pizzas: falta 1 pepperoni",
        Disponibilidad.Insuficiente(listOf(Faltante("pepperoni", 11, 10))),
        backend.verificarDisponibilidad(List(11) { pizzaDePrueba })
    )

    println("\n=== CONFIRMAR / CANCELAR ===")
    val backend2 = backendDePrueba()
    val pedido10 = backend2.crearPedido(clienteDePrueba, List(10) { pizzaDePrueba }, null)
    verificar("crear un pedido no toca el inventario", 10, backend2.obtenerInventario()["pepperoni"])
    verificar("confirmar 10 pizzas", Disponibilidad.Suficiente, backend2.confirmarPedido(pedido10))
    verificar("quedan 0 pepperoni", 0, backend2.obtenerInventario()["pepperoni"])
    val pedidoExtra = backend2.crearPedido(clienteDePrueba, listOf(pizzaDePrueba), null)
    verificar(
        "una pizza más ya no alcanza",
        Disponibilidad.Insuficiente(listOf(Faltante("pepperoni", 1, 0))),
        backend2.confirmarPedido(pedidoExtra)
    )
    verificar("el rechazo no registra el pedido", 1, backend2.obtenerReporteDelDia().pedidos.size)
    verificar("reabastecer deja el stock en 5", 5, run { backend2.reabastecer("pepperoni", 5); backend2.obtenerInventario()["pepperoni"] })

    println("\n=== PREPARAR LOTE ===")
    val inventarioLote = inventarioDePrueba()
    val descontado = prepararLote(inventarioLote, List(3) { pizzaDePrueba })
    verificar("lote de 3: ingredientes descontados", mapOf("masa" to 3, "salsa de tomate" to 3, "queso" to 3, "pepperoni" to 3), descontado)
    verificar("lote de 3: pepperoni queda en 7", 7, inventarioLote["pepperoni"])
    verificar("lote de 8 más: no alcanza y no modifica", true, lanzaError { prepararLote(inventarioLote, List(8) { pizzaDePrueba }) })
    verificar("tras el error, pepperoni sigue en 7", 7, inventarioLote["pepperoni"])

    println("\n=== REPARTIDOR MÁS CERCANO ===")
    val lejos = Repartidor("Lejos", "1", 3.0)
    val medio = Repartidor("Medio", "2", 2.0)
    val cerca = Repartidor("Cerca", "3", 1.0)
    val equipo = listOf(lejos, medio, cerca)
    verificar("el más cercano es Cerca", "Cerca", elegirRepartidor(equipo)?.nombre)
    cerca.ocupar()
    verificar("ocupado Cerca, sigue Medio", "Medio", elegirRepartidor(equipo)?.nombre)
    medio.ocupar()
    lejos.ocupar()
    verificar("todos ocupados -> null", null, elegirRepartidor(equipo))

    println("\n=== TELÉFONOS ===")
    verificar("+56912345678 es válido", true, esTelefonoValido("+56912345678"))
    verificar("12345678 es válido", true, esTelefonoValido("12345678"))
    verificar("'abc' es inválido", false, esTelefonoValido("abc"))
    verificar("'1234' (muy corto) es inválido", false, esTelefonoValido("1234"))
    verificar("'12345678901234' (muy largo) es inválido", false, esTelefonoValido("12345678901234"))
    verificar("'+569 1234 5678' (con espacios) es inválido", false, esTelefonoValido("+569 1234 5678"))

    println("\n=== CORREOS ===")
    verificar("camila@correo.cl es válido", true, esCorreoValido("camila@correo.cl"))
    verificar("'sin-arroba' es inválido", false, esCorreoValido("sin-arroba"))
    verificar("'a@b' (sin dominio) es inválido", false, esCorreoValido("a@b"))

    println("\n=== CANTIDADES NO NUMÉRICAS ===")
    verificar("'5' -> 5", 5, convertirEntero("5"))
    verificar("'diez' -> null", null, convertirEntero("diez"))
    verificar("'' -> null", null, convertirEntero(""))
    verificar("'3.5' -> null", null, convertirEntero("3.5"))

    println("\n=== ERRORES CAPTURADOS CON TRY-CATCH ===")
    verificar("precio negativo en producto", true, lanzaError { Producto("Mala", -100, mapOf("masa" to 1)) })
    verificar("reabastecer cantidad negativa", true, lanzaError { reabastecer(mutableMapOf("masa" to 5), "masa", -5) })
    verificar("reabastecer ingrediente inexistente", true, lanzaError { reabastecer(mutableMapOf("masa" to 5), "oro", 5) })
    verificar("pedido sin productos", true, lanzaError { Pedido(99, clienteDePrueba, emptyList()) })
    verificar("descontar más de lo disponible", true, lanzaError { descontarInventario(mutableMapOf("masa" to 1), mapOf("masa" to 2)) })
    verificar("retroceder el estado de un pedido", true, lanzaError { Pedido(98, clienteDePrueba, listOf(pizzaDePrueba), estadoInicial = EstadoPedido.EN_CAMINO).cambiarEstado(EstadoPedido.LISTO) })
    verificar("entregar un pedido que no está listo", true, lanzaError { Repartidor("Test", "1", 1.0).entregarPedido(Pedido(97, clienteDePrueba, listOf(pizzaDePrueba))) })

    println("\nResultado: $pruebasOk correctas, $pruebasFallidas fallidas.")
}

private fun lanzaError(bloque: () -> Unit): Boolean =
    try {
        bloque()
        false
    } catch (e: IllegalArgumentException) {
        println("          mensaje: ${e.message}")
        true
    } catch (e: IllegalStateException) {
        println("          mensaje: ${e.message}")
        true
    }
