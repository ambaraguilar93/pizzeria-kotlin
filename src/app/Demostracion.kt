import java.time.LocalDate

// Se arma un resumen que integra todas las funcionalidades avanzadas.

// Datos de ejemplo que usan tanto el resumen completo como el corto.
private class DatosDemostracion {
    val menu = crearCatalogo()
    val margarita = PizzaMargarita()
    val pepperoni = PizzaPepperoni()
    val bebida = BebidaFamiliar()
    val combo = ComboFamiliar()

    // Dos registros de ejemplo del mismo cliente y otro cliente
    val ana = Cliente("Ana Rojas", "Av. Apoquindo 100", "+56 (222) 555-0147", "ana@dominio.com")
    val anaRepetida = Cliente("ana rojas", "Otra dirección 55", "+56 (222) 555-9999", "ANA@dominio.com")
    val luis = Cliente("Luis Soto", "Los Leones 20", "+56 (311) 555-0198", "luis@dominio.com")

    val pedidoA = Pedido(1, ana, listOf(margarita, margarita, bebida), fecha = LocalDate.of(2026, 11, 10))
    val pedidoB = Pedido(2, anaRepetida, listOf(pepperoni, pepperoni, combo), notas = "Sin aceitunas", fecha = LocalDate.of(2026, 11, 12))
}

// Resumen completo de pedidos, más validaciones y análisis de clases.
fun armarResumenSemana3(): List<SeccionResumen> {
    val d = DatosDemostracion()

    return listOf(
        seccionPedidosCombinados(d.pedidoA, d.pedidoB, d.luis, d.pepperoni),
        seccionClienteDesestructurado(d.ana, d.pedidoA + d.pedidoB),
        seccionPromocionables(d.menu, d.pedidoA + d.pedidoB),
        seccionIgualdad(d.menu, d.margarita, d.ana, d.anaRepetida),
        seccionValidaciones(),
        seccionAnalisisDeClases()
    )
}

// Resumen corto.
fun armarResumenCorto(): List<SeccionResumen> {
    val d = DatosDemostracion()
    val combinado = d.pedidoA + d.pedidoB
    val (nombre, correo, telefono) = d.ana
    val (clienteCombinado, productosCombinados, totalCombinado) = combinado
    val promocionables = descubrirPromocionables(d.menu)
    val registroClientes = Registro(listOf(d.ana)).also { it.registrar(d.anaRepetida) }
    val registroProductos = Registro(d.menu).also { it.registrar(Producto("Margarita", 9990, mapOf("masa" to 1), CategoriaProducto.PIZZA)) }

    return listOf(
        SeccionResumen("PEDIDOS COMBINADOS (operador +)", listOf(
            "Pedido A ${formatearPrecio(d.pedidoA.calcularTotal())} + Pedido B ${formatearPrecio(d.pedidoB.calcularTotal())} " +
                    "= 1 pedido de ${clienteCombinado.nombre} con ${productosCombinados.size} productos",
            "Total recalculado: ${formatearPrecio(totalCombinado)} (sumados serían ${formatearPrecio(d.pedidoA.calcularTotal() + d.pedidoB.calcularTotal())})"
        )),
        SeccionResumen("CLIENTE DESESTRUCTURADO", listOf(
            "val (nombre, correo, telefono) = cliente",
            "$nombre | $correo | $telefono"
        )),
        SeccionResumen("PRODUCTOS PROMOCIONABLES (@Promocionable)", listOf(
            promocionables.joinToString(" | ") { "${it.producto.nombre} -${it.porcentaje}%" },
            "Detectados con reflection, sin lista escrita a mano"
        )),
        SeccionResumen("IGUALDAD DE OBJETOS (equals + hashCode)", listOf(
            "Cliente: ${d.ana == d.anaRepetida} (mismo nombre y correo) | clientes registrados: ${registroClientes.listar().size}",
            "Producto: ${d.margarita == Producto("Margarita", 9990, mapOf("masa" to 1), CategoriaProducto.PIZZA)} (mismo nombre y categoría) | menú: ${registroProductos.listar().size} productos"
        ))
    )
}

// Operador + el total se recalcula, no se suma.
private fun seccionPedidosCombinados(pedidoA: Pedido, pedidoB: Pedido, otroCliente: Cliente, producto: Producto): SeccionResumen {
    val combinado = pedidoA + pedidoB
    val totales = combinado.totales
    val sumaSeparada = pedidoA.calcularTotal() + pedidoB.calcularTotal()
    val errorOtroCliente = runCatching { pedidoA + Pedido(3, otroCliente, listOf(producto)) }
        .exceptionOrNull()?.message ?: "(no hubo error)"

    return SeccionResumen(
        "PEDIDOS COMBINADOS (operador +)",
        listOf(
            "Pedido A (${formatearFecha(pedidoA.fecha)}): ${pedidoA.resumenProductos()} -> total ${formatearPrecio(pedidoA.calcularTotal())}",
            "Pedido B (${formatearFecha(pedidoB.fecha)}): ${pedidoB.resumenProductos()} -> total ${formatearPrecio(pedidoB.calcularTotal())}",
            "A + B = un solo pedido de ${combinado.cliente.nombre} con ${combinado.productos.size} productos (${combinado.resumenProductos()})",
            "  Subtotal ${formatearPrecio(totales.subtotal)} | descuento -${formatearPrecio(totales.descuento)} " +
                    "(@Promocionable -${formatearPrecio(totales.descuentoAnotaciones)}, periodo -${formatearPrecio(totales.descuentoPromocion)}) | IVA ${formatearPrecio(totales.impuestos)}",
            "  TOTAL COMBINADO (recalculado): ${formatearPrecio(combinado.calcularTotal())}",
            "  Los totales por separado sumaban ${formatearPrecio(sumaSeparada)}: el total combinado no se suma, se recalcula.",
            "A + pedido de otro cliente: rechazado -> $errorOtroCliente"
        )
    )
}

// Desestructuración de Cliente y de Pedido
private fun seccionClienteDesestructurado(cliente: Cliente, pedido: Pedido): SeccionResumen {
    val (nombre, correo, telefono) = cliente
    val (clienteDelPedido, productos, total) = pedido

    return SeccionResumen(
        "CLIENTE DESESTRUCTURADO",
        listOf(
            "val (nombre, correo, telefono) = cliente",
            "  Nombre:   $nombre",
            "  Correo:   $correo",
            "  Teléfono: $telefono",
            "val (cliente, productos, total) = pedidoCombinado",
            "  Cliente: ${clienteDelPedido.nombre} | Productos: ${productos.size} | Total: ${formatearPrecio(total)}"
        )
    )
}

// Anotación @Promocionable leída con reflection
private fun seccionPromocionables(menu: List<Producto>, pedido: Pedido): SeccionResumen {
    val promocionables = descubrirPromocionables(menu)
    val noPromocionables = menu.filterNot { esPromocionable(it) }.map { it.nombre }
    val delPedido = pedido.productosPromocionables().map { it.producto.nombre }

    return SeccionResumen(
        "PRODUCTOS PROMOCIONABLES (@Promocionable + reflection)",
        buildList {
            add("Descubiertos al leer la anotación de cada clase (no hay una lista escrita a mano):")
            promocionables.forEach { add("  - ${it.producto.nombre} (${it.producto::class.simpleName}) -> ${it.porcentaje}% · ${it.motivo}") }
            add("No promocionables: ${noPromocionables.joinToString(", ")}")
            add("En el pedido combinado: ${delPedido.joinToString(", ").ifEmpty { "ninguno" }}")
        }
    )
}

// equals() y hashCode() para evitar duplicados
private fun seccionIgualdad(menu: List<Producto>, margarita: Producto, ana: Cliente, anaRepetida: Cliente): SeccionResumen {
    val anaOtroCorreo = Cliente(ana.nombre, ana.direccion, ana.telefono, "otro@dominio.com")
    val margaritaOtroPrecio = Producto("Margarita", 9990, mapOf("masa" to 1), CategoriaProducto.PIZZA)
    val margaritaBebida = Producto("Margarita", 1990, categoria = CategoriaProducto.BEBIDA)

    val registroClientes = Registro(listOf(ana))
    val resultadoCliente = registroClientes.registrar(anaRepetida)
    val registroProductos = Registro(menu)
    val resultadoProducto = registroProductos.registrar(margaritaOtroPrecio)

    return SeccionResumen(
        "IGUALDAD DE OBJETOS (equals + hashCode)",
        listOf(
            "Cliente repetido: ${ana == anaRepetida}   (mismo nombre y correo; cambian teléfono y dirección)",
            "  hashCode igual: ${ana.hashCode() == anaRepetida.hashCode()} | con otro correo: ${ana == anaOtroCorreo}",
            "  Registro de clientes: ${textoRegistro(resultadoCliente)} -> hay ${registroClientes.listar().size} cliente registrado",
            "Producto repetido: ${margarita == margaritaOtroPrecio}   (mismo nombre y categoría; cambia el precio)",
            "  hashCode igual: ${margarita.hashCode() == margaritaOtroPrecio.hashCode()} | mismo nombre en otra categoría: ${margarita == margaritaBebida}",
            "  Registro de productos: ${textoRegistro(resultadoProducto)} -> el menú sigue con ${registroProductos.listar().size} productos"
        )
    )
}

private fun <T> textoRegistro(resultado: ResultadoRegistro<T>): String = when (resultado) {
    is ResultadoRegistro.Registrado -> "registrado"
    is ResultadoRegistro.Repetido -> "repetido, no se guardó de nuevo"
}

// Validaciones con Regex y Ranges
private fun seccionValidaciones(): SeccionResumen {
    fun telefono(texto: String) = "  '$texto' -> ${formatearTelefono(texto) ?: "rechazado (se vuelve a pedir)"}"
    fun correo(texto: String) = "  '$texto' -> ${if (esCorreoValido(texto)) "válido" else describirErrorCorreo(texto)}"
    fun cantidad(numero: Int) = "$numero ${if (esCantidadValida(numero)) "sí" else "no"}"
    fun promocion(fecha: LocalDate) = "${formatearFecha(fecha)} ${if (estaEnPromocion(fecha)) "sí" else "no"}"

    return SeccionResumen(
        "VALIDACIONES (Regex y Ranges)",
        buildList {
            add("Teléfono (formato uniforme +XX (XXX) XXX-XXXX):")
            add(telefono("2225550147"))
            add(telefono("+56 222 555 0147"))
            add(telefono("(311) 555-0198"))
            add(telefono("12345"))
            add("Correo:")
            add(correo("ana@dominio.com"))
            add(correo("ana@dominio"))
            add(correo("ana.dominio.com"))
            add("Cantidad en el rango ${RANGO_CANTIDAD.first}..${RANGO_CANTIDAD.last}: " +
                    listOf(0, 1, 100, 101).joinToString(" | ") { cantidad(it) })
            add("Periodo de promoción ${formatearFecha(INICIO_PROMOCION)} al ${formatearFecha(FIN_PROMOCION)} " +
                    "($PORCENTAJE_PROMOCION% adicional): " +
                    listOf(LocalDate.of(2026, 11, 10), LocalDate.of(2026, 12, 3)).joinToString(" | ") { promocion(it) })
        }
    )
}

// Reflection para listar propiedades y métodos de las entidades
private fun seccionAnalisisDeClases(): SeccionResumen =
    SeccionResumen(
        "ANÁLISIS DE CLASES (reflection)",
        listOf(Pedido::class, Producto::class, Cliente::class).flatMap { clase ->
            val descripcion = describirClase(clase)
            listOf(
                descripcion.nombre,
                "  Propiedades: ${descripcion.propiedades.joinToString(", ")}",
                "  Métodos: ${descripcion.metodos.joinToString(", ")}"
            )
        }
    )
