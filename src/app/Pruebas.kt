// Pruebas de casos límite
import java.time.LocalDate

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

    println("\n=== TELÉFONOS: FORMATO +XX (XXX) XXX-XXXX ===")
    val formatoEsperado = "+56 (222) 555-0147"
    verificar("10 dígitos: se agrega el 56", formatoEsperado, formatearTelefono("2225550147"))
    verificar("con espacios", formatoEsperado, formatearTelefono("222 555 0147"))
    verificar("con guiones", formatoEsperado, formatearTelefono("222-555-0147"))
    verificar("con +56 y espacios", formatoEsperado, formatearTelefono("+56 222 555 0147"))
    verificar("12 dígitos con 56 pegado", formatoEsperado, formatearTelefono("562225550147"))
    verificar("con paréntesis", formatoEsperado, formatearTelefono("(222) 555-0147"))
    verificar("ya formateado queda igual", formatoEsperado, formatearTelefono(formatoEsperado))
    verificar("la salida es idéntica sin importar cómo se escriba", true,
        listOf("2225550147", "222 555 0147", "+56 222 555-0147", "56 222 555 0147").map { formatearTelefono(it) }.distinct().size == 1)
    verificar("otro ejemplo: (311) 555-0198", "+56 (311) 555-0198", formatearTelefono("(311) 555-0198"))
    verificar("9 dígitos (faltan) -> null", null, formatearTelefono("912345678"))
    verificar("11 dígitos -> null", null, formatearTelefono("56912345678"))
    verificar("13 dígitos (sobran) -> null", null, formatearTelefono("5622255501470"))
    verificar("con letras -> null", null, formatearTelefono("abc"))
    verificar("vacío -> null", null, formatearTelefono(""))
    verificar("12345 -> null", null, formatearTelefono("12345"))
    verificar("esTelefonoValido acepta 10 dígitos", true, esTelefonoValido("222 555 0147"))
    verificar("esTelefonoValido rechaza 8 dígitos", false, esTelefonoValido("12345678"))

    println("\n=== CORREOS ===")
    verificar("camila@correo.cl es válido", true, esCorreoValido("camila@correo.cl"))
    verificar("ana@dominio.com es válido", true, esCorreoValido("ana@dominio.com"))
    verificar("'sin-arroba' es inválido", false, esCorreoValido("sin-arroba"))
    verificar("'a@b' (sin dominio) es inválido", false, esCorreoValido("a@b"))
    verificar("'ana@dominio' (sin .com) es inválido", false, esCorreoValido("ana@dominio"))
    verificar("'ana.dominio.com' (sin @) es inválido", false, esCorreoValido("ana.dominio.com"))
    verificar("el mensaje para 'ana.dominio.com' habla de la @", true, describirErrorCorreo("ana.dominio.com").contains("@"))
    verificar("el mensaje para 'ana@dominio' habla del dominio", true, describirErrorCorreo("ana@dominio").contains("dominio"))
    verificar("el mensaje para vacío dice que no puede estar vacío", true, describirErrorCorreo("").contains("vacío"))

    println("\n=== RANGO DE CANTIDAD (1..100) ===")
    verificar("0 no sirve", false, esCantidadValida(0))
    verificar("1 es el mínimo", true, esCantidadValida(1))
    verificar("100 es el máximo", true, esCantidadValida(100))
    verificar("101 no sirve", false, esCantidadValida(101))
    verificar("negativo no sirve", false, esCantidadValida(-5))

    println("\n=== FECHAS: PARSEO Y PERIODO DE PROMOCIÓN ===")
    verificar("'10/11/2026' -> 10 de noviembre", LocalDate.of(2026, 11, 10), parsearFecha("10/11/2026"))
    verificar("'1/2/2026' (sin ceros) -> 1 de febrero", LocalDate.of(2026, 2, 1), parsearFecha("1/2/2026"))
    verificar("'31/02/2026' (no existe) -> null", null, parsearFecha("31/02/2026"))
    verificar("'2026-11-10' (otro formato) -> null", null, parsearFecha("2026-11-10"))
    verificar("'abc' -> null", null, parsearFecha("abc"))
    verificar("formatear fecha", "05/11/2026", formatearFecha(LocalDate.of(2026, 11, 5)))
    verificar("1 nov (inicio) está en promoción", true, estaEnPromocion(LocalDate.of(2026, 11, 1)))
    verificar("10 nov está en promoción", true, estaEnPromocion(LocalDate.of(2026, 11, 10)))
    verificar("30 nov (fin) está en promoción", true, estaEnPromocion(LocalDate.of(2026, 11, 30)))
    verificar("31 oct está fuera", false, estaEnPromocion(LocalDate.of(2026, 10, 31)))
    verificar("1 dic está fuera", false, estaEnPromocion(LocalDate.of(2026, 12, 1)))
    val tresPizzas = List(3) { pizzaDePrueba }
    verificar(
        "3 pizzas dentro del periodo: 5% de descuento",
        Totales(28500, 1425, 5144, 32219, descuentoPromocion = 1425),
        calcularTotales(tresPizzas, LocalDate.of(2026, 11, 10))
    )
    verificar(
        "3 pizzas fuera del periodo: el total queda igual",
        Totales(28500, 0, 5415, 33915),
        calcularTotales(tresPizzas, LocalDate.of(2026, 12, 3))
    )
    verificar(
        "4 pizzas dentro del periodo: se acumulan volumen y promoción",
        Totales(38000, 5700, 6137, 38437, descuentoPromocion = 1900),
        calcularTotales(List(4) { pizzaDePrueba }, LocalDate.of(2026, 11, 10))
    )
    verificar("sin fecha no hay promoción", Totales(28500, 0, 5415, 33915), calcularTotales(tresPizzas))
    verificar(
        "un Pedido usa su propia fecha para el descuento",
        Totales(28500, 1425, 5144, 32219, descuentoPromocion = 1425),
        Pedido(70, clienteDePrueba, tresPizzas, fecha = LocalDate.of(2026, 11, 1)).totales
    )
    verificar(
        "un Pedido fuera del periodo no tiene descuento de promoción",
        0,
        Pedido(71, clienteDePrueba, tresPizzas, fecha = LocalDate.of(2026, 12, 1)).totales.descuentoPromocion
    )

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
    verificar("despachar un pedido que no está listo", true, lanzaError { Repartidor("Test", "1", 1.0).despacharPedido(Pedido(97, clienteDePrueba, listOf(pizzaDePrueba))) })
    verificar("saltar de 'En preparación' a 'En camino'", true, lanzaError { Pedido(96, clienteDePrueba, listOf(pizzaDePrueba)).cambiarEstado(EstadoPedido.EN_CAMINO) })
    verificar("saltar de 'Listo' a 'Entregado'", true, lanzaError { Pedido(95, clienteDePrueba, listOf(pizzaDePrueba), estadoInicial = EstadoPedido.LISTO).cambiarEstado(EstadoPedido.ENTREGADO) })
    verificar("cambiar el estado de un pedido entregado", true, lanzaError { Pedido(94, clienteDePrueba, listOf(pizzaDePrueba), estadoInicial = EstadoPedido.ENTREGADO).cambiarEstado(EstadoPedido.ENTREGADO) })
    verificar("avanzar paso a paso llega a 'Entregado'", EstadoPedido.ENTREGADO, run {
        val p = Pedido(93, clienteDePrueba, listOf(pizzaDePrueba))
        p.cambiarEstado(EstadoPedido.LISTO)
        p.cambiarEstado(EstadoPedido.EN_CAMINO)
        p.cambiarEstado(EstadoPedido.ENTREGADO)
        p.estado
    })

    println("\n=== SEGUIMIENTO DE ENTREGAS ===")
    val repartidorFlujo = Repartidor("Flujo", "1", 1.0)
    val backend3 = backendDePrueba(listOf(repartidorFlujo))
    val pedidoFlujo = backend3.crearPedido(clienteDePrueba, listOf(pizzaDePrueba), null)
    backend3.confirmarPedido(pedidoFlujo)
    verificar("al confirmar no se asigna repartidor", null, pedidoFlujo.repartidor)
    verificar("al confirmar el repartidor sigue disponible", true, repartidorFlujo.disponible)
    backend3.avanzarPedido(pedidoFlujo.id)
    verificar("al quedar listo se asigna el repartidor", "Flujo", pedidoFlujo.repartidor?.nombre)
    verificar("al quedar listo el repartidor está ocupado", false, repartidorFlujo.disponible)
    backend3.avanzarPedido(pedidoFlujo.id)
    verificar("al despachar queda 'En camino'", EstadoPedido.EN_CAMINO, pedidoFlujo.estado)
    verificar("en camino aún no es completado", 0, backend3.obtenerReporteDelDia().completados.size)
    backend3.avanzarPedido(pedidoFlujo.id)
    verificar("al entregar queda 'Entregado'", EstadoPedido.ENTREGADO, pedidoFlujo.estado)
    verificar("al entregar se libera al repartidor", true, repartidorFlujo.disponible)
    verificar("entregado cuenta como completado", 1, backend3.obtenerReporteDelDia().completados.size)
    verificar("no quedan pendientes", 0, backend3.obtenerReporteDelDia().pendientes.size)
    verificar("un pedido entregado no avanza más", true, lanzaError { backend3.avanzarPedido(pedidoFlujo.id) })

    val repartidorOcupado = Repartidor("Ocupado", "2", 1.0)
    repartidorOcupado.ocupar()
    val backend4 = backendDePrueba(listOf(repartidorOcupado))
    val pedidoEspera = backend4.crearPedido(clienteDePrueba, listOf(pizzaDePrueba), null)
    backend4.confirmarPedido(pedidoEspera)
    backend4.avanzarPedido(pedidoEspera.id)
    verificar("sin repartidores: queda 'Listo' sin asignar", null, pedidoEspera.repartidor)
    verificar("sin repartidores no se puede despachar", true, lanzaError { backend4.avanzarPedido(pedidoEspera.id) })
    verificar("tras el error sigue en 'Listo'", EstadoPedido.LISTO, pedidoEspera.estado)
    repartidorOcupado.liberar()
    backend4.avanzarPedido(pedidoEspera.id)
    verificar("al liberarse un repartidor se despacha", "Ocupado", pedidoEspera.repartidor?.nombre)
    verificar("y el pedido queda 'En camino'", EstadoPedido.EN_CAMINO, pedidoEspera.estado)

    println("\n=== PRODUCTOS: CATEGORÍA E IGUALDAD ===")
    val margarita = PizzaMargarita()
    val margaritaOtroPrecio = Producto("Margarita", 9000, mapOf("masa" to 1), CategoriaProducto.PIZZA)
    verificar("misma pizza con otro precio es el mismo producto", true, margarita == margaritaOtroPrecio)
    verificar("hashCode coincide cuando son iguales", margarita.hashCode(), margaritaOtroPrecio.hashCode())
    verificar("mismo nombre pero otra categoría: distinto", false, margarita == Producto("Margarita", 1000, categoria = CategoriaProducto.BEBIDA))
    verificar("otro nombre: distinto", false, margarita == PizzaHawaiana())
    verificar("el nombre no distingue mayúsculas", true, margarita == Producto("MARGARITA", 8500, mapOf("masa" to 1)))
    verificar("un HashSet no guarda productos repetidos", 1, setOf(margarita, margaritaOtroPrecio).size)
    verificar("distinct() une los productos repetidos", 1, listOf(margarita, margaritaOtroPrecio).distinct().size)
    verificar("un producto no es igual a un String", false, margarita.equals("Margarita"))
    verificar("pizza sin ingredientes no es válida", true, lanzaError { Producto("Vacía", 1000) })
    verificar("nombre vacío no es válido", true, lanzaError { Producto("  ", 1000, mapOf("masa" to 1)) })
    verificar("una bebida sin ingredientes sí es válida", false, lanzaError { Producto("Agua", 1000, categoria = CategoriaProducto.BEBIDA) })

    println("\n=== PRODUCTOS: ANOTACIÓN @Promocionable ===")
    verificar("Margarita lleva @Promocionable", true, margarita.javaClass.isAnnotationPresent(Promocionable::class.java))
    verificar("Hawaiana lleva @Promocionable", true, PizzaHawaiana().javaClass.isAnnotationPresent(Promocionable::class.java))
    verificar("Pepperoni no la lleva", false, PizzaPepperoni().javaClass.isAnnotationPresent(Promocionable::class.java))
    verificar("el combo lleva @Promocionable", true, ComboFamiliar().javaClass.isAnnotationPresent(Promocionable::class.java))
    verificar("el motivo se conserva en ejecución", "Promo vegetariana", margarita.javaClass.getAnnotation(Promocionable::class.java)?.motivo)
    verificar("el porcentaje se conserva en ejecución", 15, margarita.javaClass.getAnnotation(Promocionable::class.java)?.porcentaje)

    println("\n=== PIZZAS VS OTROS PRODUCTOS EN EL DESCUENTO ===")
    val bebidaDePrueba = Producto("Bebida de prueba", 2000, categoria = CategoriaProducto.BEBIDA)
    verificar(
        "3 pizzas + 2 bebidas: sin descuento (las bebidas no son pizzas)",
        Totales(32500, 0, 6175, 38675),
        calcularTotales(List(3) { pizzaDePrueba } + List(2) { bebidaDePrueba })
    )
    verificar(
        "4 pizzas + 1 bebida: con descuento",
        Totales(40000, 4000, 6840, 42840),
        calcularTotales(List(4) { pizzaDePrueba } + bebidaDePrueba)
    )
    verificar(
        "cantidadPizzas ignora las bebidas",
        2,
        Pedido(92, clienteDePrueba, listOf(pizzaDePrueba, pizzaDePrueba, bebidaDePrueba)).cantidadPizzas
    )
    val inventarioExtra = inventarioDePrueba()
    prepararLote(inventarioExtra, listOf(bebidaDePrueba, ExtraQueso()))
    verificar("la bebida no gasta inventario y el extra queso gasta 1 queso", 19, inventarioExtra["queso"])

    println("\n=== CLIENTES: IGUALDAD Y DESESTRUCTURACIÓN ===")
    val ana = Cliente("Ana Rojas", "Calle 1", "912345678", "ana@correo.cl")
    val anaOtroTelefono = Cliente("ana rojas ", "Otra calle 5", "987654321", "ANA@correo.cl")
    verificar("mismo nombre y correo (otro teléfono y dirección): mismo cliente", true, ana == anaOtroTelefono)
    val anaOtraDireccion = ana.conDireccion("Nueva dirección 99")
    verificar("conDireccion: el pedido lleva la dirección nueva", "Nueva dirección 99", anaOtraDireccion.direccion)
    verificar("conDireccion: sigue siendo el mismo cliente (no se duplica)", true, ana == anaOtraDireccion)
    verificar("conDireccion: conserva nombre, correo y teléfono", true,
        anaOtraDireccion.nombre == ana.nombre && anaOtraDireccion.correo == ana.correo && anaOtraDireccion.telefono == ana.telefono)
    verificar("conDireccion: el cliente registrado no cambia", "Calle 1", ana.direccion)
    verificar("conDireccion con la misma dirección devuelve el mismo objeto", true, ana.conDireccion("Calle 1") === ana)
    val anaOtroFono = ana.conTelefono("999888777")
    verificar("conTelefono: cambia el teléfono y conserva dirección y correo", true,
        anaOtroFono.telefono == "999888777" && anaOtroFono.direccion == ana.direccion && anaOtroFono.correo == ana.correo)
    verificar("conTelefono: sigue siendo el mismo cliente", true, ana == anaOtroFono)
    verificar("conTelefono con el mismo teléfono devuelve el mismo objeto", true, ana.conTelefono(ana.telefono) === ana)
    verificar("un registro no se duplica al usar la dirección nueva", 1, setOf(ana, anaOtraDireccion).size)
    verificar("hashCode coincide cuando son iguales", ana.hashCode(), anaOtroTelefono.hashCode())
    verificar("mismo nombre, otro correo: distinto", false, ana == Cliente("Ana Rojas", "Calle 1", "912345678", "otra@correo.cl"))
    verificar("otro nombre, mismo correo: distinto", false, ana == Cliente("Ana Pérez", "Calle 1", "912345678", "ana@correo.cl"))
    verificar("un HashSet no guarda clientes repetidos", 1, setOf(ana, anaOtroTelefono).size)
    verificar("distinct() une los clientes repetidos", 1, listOf(ana, anaOtroTelefono).distinct().size)
    val (nombreAna, correoAna, telefonoAna) = ana
    verificar("desestructurar cliente: nombre", "Ana Rojas", nombreAna)
    verificar("desestructurar cliente: correo", "ana@correo.cl", correoAna)
    verificar("desestructurar cliente: teléfono", "912345678", telefonoAna)

    println("\n=== PEDIDOS: DESESTRUCTURACIÓN Y OPERADOR + ===")
    // Fechas fuera del periodo de promoción, para probar el + sin mezclar el descuento de la promoción
    val pedidoA = Pedido(81, ana, List(3) { pizzaDePrueba }, notas = "Sin cebolla", fecha = LocalDate.of(2026, 10, 20))
    val pedidoB = Pedido(82, anaOtroTelefono, listOf(pizzaDePrueba), notas = "Tocar el timbre", fecha = LocalDate.of(2026, 10, 25))
    val (clientePedido, productosPedido, totalPedido) = pedidoA
    verificar("desestructurar pedido: cliente", ana, clientePedido)
    verificar("desestructurar pedido: productos", 3, productosPedido.size)
    verificar("desestructurar pedido: total", pedidoA.calcularTotal(), totalPedido)

    val combinado = pedidoA + pedidoB
    verificar("A + B junta los productos (3 + 1)", 4, combinado.productos.size)
    verificar("el total se recalcula con el descuento por 4 pizzas", Totales(38000, 3800, 6498, 40698), combinado.totales)
    verificar("el total NO es la suma de los totales", false, combinado.calcularTotal() == pedidoA.calcularTotal() + pedidoB.calcularTotal())
    verificar("los pedidos originales no se modifican", 3, pedidoA.productos.size)
    verificar("las notas se unen", "Sin cebolla | Tocar el timbre", combinado.notas)
    verificar("la fecha es la más reciente", LocalDate.of(2026, 10, 25), combinado.fecha)
    val enPromocionA = Pedido(85, ana, List(3) { pizzaDePrueba }, fecha = LocalDate.of(2026, 11, 1))
    val enPromocionB = Pedido(86, ana, listOf(pizzaDePrueba), fecha = LocalDate.of(2026, 11, 5))
    verificar(
        "el pedido combinado dentro del periodo recalcula volumen + promoción",
        Totales(38000, 5700, 6137, 38437, descuentoPromocion = 1900),
        (enPromocionA + enPromocionB).totales
    )
    verificar("el pedido combinado no tiene número real", Pedido.ID_COMBINADO, combinado.id)
    verificar("el pedido combinado queda en preparación", EstadoPedido.EN_PREPARACION, combinado.estado)
    val enCamino = Pedido(83, ana, listOf(pizzaDePrueba), estadoInicial = EstadoPedido.EN_CAMINO)
    verificar("combinar con un pedido en camino no copia el estado", EstadoPedido.EN_PREPARACION, (pedidoA + enCamino).estado)
    verificar("combinar sin notas deja las notas en null", null, (enCamino + enCamino).notas)
    verificar("no se combinan pedidos de clientes distintos", true, lanzaError { pedidoA + Pedido(84, clienteDePrueba, listOf(pizzaDePrueba)) })
    verificar("se puede encadenar: A + B + A tiene 7 productos", 7, (pedidoA + pedidoB + pedidoA).productos.size)

    println("\n=== REFLECTION: PRODUCTOS PROMOCIONABLES ===")
    val promocionables = descubrirPromocionables(crearCatalogo())
    verificar("del menú se descubren Margarita, Hawaiana y el combo", listOf("Margarita", "Hawaiana", "Combo Familiar"), promocionables.map { it.producto.nombre })
    verificar("los motivos se leen de la anotación", listOf("Promo vegetariana", "Promo tropical", "Combo familiar"), promocionables.map { it.motivo })
    verificar("los porcentajes se leen de la anotación", listOf(15, 10, 20), promocionables.map { it.porcentaje })
    verificar("Pepperoni no es promocionable", false, esPromocionable(PizzaPepperoni()))
    verificar("una bebida no es promocionable", false, esPromocionable(BebidaFamiliar()))
    verificar("un Producto genérico (sin subclase) no es promocionable", false, esPromocionable(margaritaOtroPrecio))
    verificar("motivoPromocion de Margarita", "Promo vegetariana", motivoPromocion(margarita))
    verificar("motivoPromocion de Pepperoni es null", null, motivoPromocion(PizzaPepperoni()))
    verificar("porcentajePromocion de Margarita", 15, porcentajePromocion(margarita))
    verificar("porcentajePromocion de Pepperoni es 0", 0, porcentajePromocion(PizzaPepperoni()))
    val pedidoPromo = Pedido(60, clienteDePrueba, listOf(margarita, margarita, PizzaPepperoni(), PizzaHawaiana()))
    verificar(
        "un pedido descubre sus promocionables sin repetirlos",
        listOf("Margarita", "Hawaiana"),
        pedidoPromo.productosPromocionables().map { it.producto.nombre }
    )
    verificar("un pedido sin promocionables devuelve lista vacía", 0, Pedido(61, clienteDePrueba, listOf(PizzaPepperoni())).productosPromocionables().size)

    println("\n=== DESCUENTO POR @Promocionable Y COMBO ===")
    verificar("1 Margarita: -15% por la anotación", Totales(8500, 1275, 1373, 8598, 0, 1275), calcularTotales(listOf(margarita)))
    verificar("1 Combo Familiar: -20% por la anotación", Totales(11000, 2200, 1672, 10472, 0, 2200), calcularTotales(listOf(ComboFamiliar())))
    verificar("Pepperoni sin anotación: sin descuento por anotación", 0, calcularTotales(listOf(PizzaPepperoni())).descuentoAnotaciones)
    verificar(
        "4 Margarita: anotación + volumen (el volumen se calcula sobre lo que queda)",
        Totales(34000, 7990, 4942, 30952, 0, 5100),
        calcularTotales(List(4) { margarita })
    )
    verificar(
        "4 Margarita dentro del periodo: anotación + volumen + promoción",
        Totales(34000, 9435, 4667, 29232, 1445, 5100),
        calcularTotales(List(4) { margarita }, LocalDate.of(2026, 11, 10))
    )
    verificar(
        "2 Margarita + 1 Hawaiana dentro del periodo",
        Totales(26000, 4577, 4070, 25493, 1127, 3450),
        calcularTotales(listOf(margarita, margarita, PizzaHawaiana()), LocalDate.of(2026, 11, 10))
    )
    verificar(
        "2 Pepperoni + Combo: el combo no cuenta como pizza para el descuento por volumen",
        Totales(30000, 2200, 5282, 33082, 0, 2200),
        calcularTotales(listOf(PizzaPepperoni(), PizzaPepperoni(), ComboFamiliar()))
    )
    verificar("el combo es de categoría combo", CategoriaProducto.COMBO, ComboFamiliar().categoria)
    val inventarioCombo = inventarioDePrueba()
    prepararLote(inventarioCombo, listOf(ComboFamiliar()))
    verificar("el combo descuenta los ingredientes de su pizza", 9, inventarioCombo["pepperoni"])
    verificar("el menú incluye el combo", true, crearCatalogo().any { it is ComboFamiliar })

    println("\n=== REFLECTION: PROPIEDADES Y MÉTODOS DE UNA CLASE ===")
    val descPedido = describirClase(Pedido::class)
    val descProducto = describirClase(Producto::class)
    val descCliente = describirClase(Cliente::class)
    verificar("nombre de la clase", "Pedido", descPedido.nombre)
    verificar(
        "Pedido tiene cliente, fecha, productos y totales",
        true,
        listOf("cliente", "fecha", "productos", "totales").all { p -> descPedido.propiedades.any { it.startsWith("$p:") } }
    )
    verificar("Pedido tiene los métodos plus, cambiarEstado y component1", true, listOf("plus", "cambiarEstado", "component1").all { it in descPedido.metodos })
    verificar(
        "Producto tiene nombre, precio, categoria e ingredientes",
        true,
        listOf("nombre", "precio", "categoria", "ingredientes").all { p -> descProducto.propiedades.any { it.startsWith("$p:") } }
    )
    verificar("Producto tiene equals y hashCode", true, listOf("equals", "hashCode").all { it in descProducto.metodos })
    verificar(
        "Cliente tiene nombre, correo, telefono y direccion",
        true,
        listOf("nombre", "correo", "telefono", "direccion").all { p -> descCliente.propiedades.any { it.startsWith("$p:") } }
    )
    verificar("se muestran los tipos: precio es Int", true, "precio: Int" in descProducto.propiedades)
    verificar("se muestran los tipos nulos: notas es String?", true, "notas: String?" in descPedido.propiedades)

    println("\n=== REGISTRO SIN DUPLICADOS ===")
    val registroClientes = Registro<Cliente>()
    verificar("un cliente nuevo queda registrado", true, registroClientes.registrar(ana) is ResultadoRegistro.Registrado)
    val repetido = registroClientes.registrar(anaOtroTelefono)
    verificar("el mismo cliente (otro teléfono) se detecta como repetido", true, repetido is ResultadoRegistro.Repetido)
    verificar("el repetido devuelve el cliente que ya estaba registrado", true, (repetido as? ResultadoRegistro.Repetido)?.existente === ana)
    verificar("no se guardó de nuevo", 1, registroClientes.listar().size)
    verificar("un cliente distinto sí se registra", true, registroClientes.registrar(Cliente("Luis Soto", "Calle 3", "955555555", "luis@correo.cl")) is ResultadoRegistro.Registrado)
    verificar("ahora hay 2 clientes", 2, registroClientes.listar().size)

    val registroProductos = Registro(crearCatalogo())
    verificar("el menú parte con 6 productos", 6, registroProductos.listar().size)
    verificar("una Margarita repetida (otro precio) se rechaza", true, registroProductos.registrar(margaritaOtroPrecio) is ResultadoRegistro.Repetido)
    verificar("sigue habiendo 6 productos", 6, registroProductos.listar().size)
    val cuatroQuesos = Producto("Cuatro quesos", 9800, mapOf("queso" to 4))
    verificar("una pizza nueva se registra", true, registroProductos.registrar(cuatroQuesos) is ResultadoRegistro.Registrado)
    verificar("ahora hay 7 productos", 7, registroProductos.listar().size)
    verificar("mismo nombre pero otra categoría sí se registra", true, registroProductos.registrar(Producto("Margarita", 2000, categoria = CategoriaProducto.BEBIDA)) is ResultadoRegistro.Registrado)

    println("\n=== REGISTRO EN EL BACKEND ===")
    val backendRegistro = PizzeriaBackend(
        catalogo = crearCatalogo() + margaritaOtroPrecio,   // trae una Margarita repetida a propósito
        inventario = inventarioDePrueba(),
        repartidores = emptyList(),
        pedidosIniciales = arrayOf(Pedido(62, ana, listOf(pizzaDePrueba)))
    )
    verificar("el menú del backend no repite productos", 6, backendRegistro.listarProductos().size)
    verificar("los clientes de los pedidos iniciales ya están registrados", true, backendRegistro.registrarCliente(anaOtroTelefono) is ResultadoRegistro.Repetido)
    verificar("listarUsuarios no repite clientes", 1, backendRegistro.listarUsuarios().size)
    val anaActualizada = backendRegistro.actualizarTelefono(ana, "999888777")
    verificar("actualizarTelefono devuelve el cliente con el teléfono nuevo", "999888777", anaActualizada.telefono)
    verificar("el registro guarda el teléfono nuevo y no duplica", true,
        backendRegistro.listarUsuarios().filterIsInstance<Cliente>().let { it.size == 1 && it[0].telefono == "999888777" })
    verificar("un producto nuevo se registra y aparece en el menú", 7, run { backendRegistro.registrarProducto(cuatroQuesos); backendRegistro.listarProductos().size })
    verificar("registrar el mismo producto otra vez no lo duplica", 7, run { backendRegistro.registrarProducto(cuatroQuesos); backendRegistro.listarProductos().size })
    verificar("un cliente nuevo aparece en los usuarios", 2, run { backendRegistro.registrarCliente(Cliente("Luis Soto", "Calle 3", "955555555", "luis@correo.cl")); backendRegistro.listarUsuarios().size })

    println("\n=== RESUMEN DE LA SEMANA 3 ===")
    val resumen = armarResumenSemana3()
    val todasLasLineas = resumen.flatMap { it.lineas }
    verificar(
        "las 4 secciones pedidas van primero y en orden",
        listOf("PEDIDOS COMBINADOS", "CLIENTE DESESTRUCTURADO", "PRODUCTOS PROMOCIONABLES", "IGUALDAD DE OBJETOS"),
        resumen.take(4).map { it.titulo.substringBefore(" (") }
    )
    verificar("también incluye validaciones y análisis de clases", true,
        resumen.any { it.titulo.startsWith("VALIDACIONES") } && resumen.any { it.titulo.startsWith("ANÁLISIS DE CLASES") })
    verificar("el resumen es siempre el mismo (no depende de la fecha de hoy)", resumen, armarResumenSemana3())
    verificar("muestra el cliente repetido", true, "Cliente repetido: true" in todasLasLineas.map { it.substringBefore("   (") })
    verificar("muestra el producto repetido", true, "Producto repetido: true" in todasLasLineas.map { it.substringBefore("   (") })
    verificar("el cliente desestructurado muestra nombre, correo y teléfono", true,
        listOf("Nombre:   Ana Rojas", "Correo:   ana@dominio.com", "Teléfono: +56 (222) 555-0147").all { dato -> todasLasLineas.any { it.trim() == dato } })
    verificar("los promocionables salen de la anotación", true,
        todasLasLineas.any { it.contains("Margarita (PizzaMargarita) -> 15% · Promo vegetariana") } &&
            todasLasLineas.any { it.contains("Hawaiana (PizzaHawaiana) -> 10% · Promo tropical") } &&
            todasLasLineas.any { it.contains("Combo Familiar (ComboFamiliar) -> 20% · Combo familiar") })
    verificar("Pepperoni figura como no promocionable", true, todasLasLineas.any { it.startsWith("No promocionables:") && it.contains("Pepperoni") })
    verificar("el pedido de otro cliente se rechaza en el resumen", true, todasLasLineas.any { it.contains("rechazado -> Solo se pueden combinar pedidos del mismo cliente") })
    verificar("el total combinado se recalcula (45.265 con anotaciones + volumen + promoción)", true, todasLasLineas.any { it.contains("TOTAL COMBINADO (recalculado): ${formatearPrecio(45265)}") })

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
