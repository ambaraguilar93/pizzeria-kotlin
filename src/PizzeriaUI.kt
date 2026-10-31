// Interfaz de consola, lee datos y muestra resultados.
class PizzeriaUI {

    companion object {
        const val MAX_INTENTOS = 3
    }

    // Se vuelve true si la entrada se termina para no quedar en un ciclo infinito
    private var entradaTerminada = false

    private fun leerLinea(): String? {
        val linea = readlnOrNull()
        if (linea == null) entradaTerminada = true
        return linea
    }

    // Cada dato se pide hasta 3 veces, si sigue inválido, se usa el valor por defecto
    fun leerTexto(
        mensaje: String,
        valorPorDefecto: String,
        mensajeError: String = "El dato no puede estar vacío.",
        esValido: (String) -> Boolean = { it.isNotBlank() }
    ): String {
        for (intento in 1..MAX_INTENTOS) {
            print(mensaje)
            val texto = leerLinea()?.trim() ?: ""

            if (esValido(texto)) {
                return texto
            }

            val restantes = MAX_INTENTOS - intento
            if (restantes > 0) {
                println("$mensajeError Intentos restantes: $restantes")
            }
        }

        println("Se usará el valor por defecto: $valorPorDefecto")
        return valorPorDefecto
    }

    fun leerEntero(mensaje: String, valorPorDefecto: Int): Int {
        for (intento in 1..MAX_INTENTOS) {
            print(mensaje)
            val numero = convertirEntero(leerLinea() ?: "")

            if (numero != null) {
                return numero
            }

            val restantes = MAX_INTENTOS - intento
            if (restantes > 0) {
                println("Debes ingresar un número entero. Intentos restantes: $restantes")
            }
        }

        println("Se usará el valor por defecto: $valorPorDefecto")
        return valorPorDefecto
    }

    fun leerOpcionMenu(): Int {
        mostrarMenu()
        val opcion = leerEntero("Elige una opción: ", -1)
        return if (entradaTerminada) 0 else opcion
    }

    fun confirmar(mensaje: String): Boolean =
        leerTexto(mensaje, "n", "Responde s o n.") { it.lowercase() in setOf("s", "n") }.lowercase() == "s"

    fun leerNotas(): String? {
        print("Notas del pedido (Enter para omitir): ")
        return leerLinea()?.trim()?.takeIf { it.isNotEmpty() }
    }

    fun leerCliente(): Cliente {
        println("\n--- REGISTRO DE CLIENTE ---")
        val nombre = formatearNombre(leerTexto("Nombre del cliente: ", "Cliente sin nombre"))
        val direccion = leerTexto("Dirección de entrega: ", "Sin dirección")
        val telefono = leerTexto(
            mensaje = "Teléfono de contacto: ",
            valorPorDefecto = TELEFONO_POR_DEFECTO,
            mensajeError = "Teléfono no válido (8 a 11 dígitos).",
            esValido = ::esTelefonoValido
        )
        val correo = leerTexto(
            mensaje = "Correo electrónico: ",
            valorPorDefecto = CORREO_POR_DEFECTO,
            mensajeError = "Correo no válido (ejemplo: nombre@correo.cl).",
            esValido = ::esCorreoValido
        )

        return Cliente(nombre, direccion, telefono, correo)
    }

    // Menú de pizzas, deja seleccionar una o mas
    fun leerProductos(catalogo: List<Producto>): List<Producto> {
        val seleccion = mutableListOf<Producto>()

        do {
            println("\n--- MENÚ DE PIZZAS ---")
            catalogo.forEachIndexed { indice, producto -> println("  ${indice + 1}. ${producto.detallarProducto()}") }

            val opcion = leerEntero("Elige una pizza (1-${catalogo.size}): ", 0)
            val producto = catalogo.getOrNull(opcion - 1)

            if (producto == null) {
                println("Opción no válida.")
            } else {
                val cantidad = leerEntero("Cantidad de ${producto.nombre}: ", 0)
                if (cantidad > 0) {
                    seleccion.addAll(List(cantidad) { producto })
                } else {
                    println("Cantidad no válida: debe ser mayor a 0.")
                }
            }
        } while (confirmar("¿Agregar otra pizza? (s/n): "))

        return seleccion
    }

    fun mostrarBienvenida() {
        println("=====================================")
        println("==     BIENVENIDO A LA PIZZERÍA    ==")
        println("=====================================")
    }

    fun mostrarMenu() {
        println("\n=============== MENÚ ===============")
        println("1. Registrar nuevo pedido")
        println("2. Ver reporte del día")
        println("3. Avanzar estado de un pedido")
        println("4. Ver inventario")
        println("5. Reabastecer ingrediente")
        println("6. Ver estadísticas de ventas")
        println("7. Ver clientes y repartidores")
        println("8. Ejecutar pruebas de casos límite")
        println("0. Salir")
        println("====================================")
    }

    fun mostrarMensaje(mensaje: String) = println(mensaje)

    fun mostrarError(mensaje: String?) =
        println("Error: ${mensaje ?: "ocurrió un problema inesperado."} Corrige los datos e intenta nuevamente.")

    fun mostrarFaltantes(disponibilidad: Disponibilidad.Insuficiente) {
        println("No hay ingredientes suficientes para aceptar el pedido:")
        for (faltante in disponibilidad.faltantes) {
            println("  - ${faltante.ingrediente}: requeridos ${faltante.requeridos}, " +
                    "disponibles ${faltante.disponibles} (faltan ${faltante.cantidadFaltante})")
        }
        println("Pasos para reabastecer:")
        println("  1. Contactar al proveedor y pedir: " +
                disponibilidad.faltantes.joinToString(", ") { "${it.ingrediente} (${it.cantidadFaltante} uds.)" } + ".")
        println("  2. Registrar el ingreso con la opción 'Reabastecer ingrediente' del menú.")
        println("  3. Volver a tomar el pedido.")
    }

    fun mostrarResumenPedido(pedido: Pedido, confirmado: Boolean) {
        val cliente = pedido.cliente
        val totales = pedido.totales   // se calculó una sola vez al crear el pedido
        val titulo = if (confirmado) "PEDIDO CONFIRMADO N° ${pedido.id}" else "RESUMEN DEL PEDIDO N° ${pedido.id}"

        println("\n=====================================")
        println("  $titulo")
        println("=====================================")
        println("CLIENTE")
        println("  Nombre:    ${cliente.nombre}")
        println("  Teléfono:  ${cliente.telefono}")
        println("  Correo:    ${cliente.correo}")
        println("  Dirección: ${cliente.direccion}")
        println("PRODUCTOS")
        for ((producto, cantidad) in pedido.productos.groupingBy { it }.eachCount()) {
            println("  ${cantidad}x ${producto.nombre} - ${formatearPrecio(producto.precio)} c/u = ${formatearPrecio(producto.precio * cantidad)}")
        }
        println("TOTALES")
        println("  Subtotal:  ${formatearPrecio(totales.subtotal)}")
        println("  Descuento: -${formatearPrecio(totales.descuento)}")
        println("  IVA ($TASA_IVA%): ${formatearPrecio(totales.impuestos)}")
        println("  TOTAL:     ${formatearPrecio(totales.total)}")
        println("ESTADO")
        println("  Estado:     ${pedido.estado.etiqueta}")
        println("  Repartidor: ${pedido.repartidor?.nombre ?: if (confirmado) "Sin repartidores disponibles" else "Se asigna al confirmar"}")
        println("  Notas:      ${pedido.notas ?: "Sin notas"}")
        println("=====================================")
    }

    fun mostrarNotificacion(pedido: Pedido) {
        pedido.cliente.correo
            .takeIf { pedido.cliente.tieneCorreoRegistrado() }
            ?.let { correo -> println("Notificación enviada a $correo: tu pedido N° ${pedido.id} está ${pedido.estado.etiqueta.lowercase()}.") }
            ?: println("El cliente no tiene correo registrado: no se envió notificación.")
    }

    fun mostrarReporteDelDia(reporte: ReporteDia) {
        println("\n=====================================")
        println("      REPORTE DE PEDIDOS DEL DÍA")
        println("=====================================")

        for (pedido in reporte.pedidos) {
            println("Pedido N° ${pedido.id} | Cliente: ${pedido.cliente.nombre}")
            println("   Pizzas: ${pedido.resumenProductos()}")
            println("   Total: ${formatearPrecio(pedido.totales.total)} | Estado: ${pedido.estado.etiqueta}")
        }

        println("-------------------------------------")
        println("Completados (${reporte.completados.size}): ${reporte.completados.joinToString { "N° ${it.id}" }.ifEmpty { "ninguno" }}")
        println("Pendientes  (${reporte.pendientes.size}): ${reporte.pendientes.joinToString { "N° ${it.id}" }.ifEmpty { "ninguno" }}")
        println("Total vendido: ${formatearPrecio(reporte.totalVendido)}")
        println("=====================================")
    }

    fun mostrarEstadisticas(estadisticas: Estadisticas) {
        println("\n--- ESTADÍSTICAS DE VENTAS ---")
        for ((producto, unidades) in estadisticas.unidadesPorProducto) {
            val bruta = estadisticas.ventaBrutaPorProducto[producto] ?: 0
            println("  $producto: $unidades unidades | venta bruta ${formatearPrecio(bruta)}")
        }
        println("Total de unidades vendidas: ${estadisticas.unidadesTotales}")
        println("Total recaudado (con descuentos e IVA): ${formatearPrecio(estadisticas.totalRecaudado)}")
    }

    fun mostrarInventario(inventario: Map<String, Int>, ingredientesDelMenu: Set<String>) {
        println("\n--- INVENTARIO ---")
        for ((ingrediente, cantidad) in inventario) {
            println("  $ingrediente: $cantidad")
        }
        println("Ingredientes distintos usados en el menú: ${ingredientesDelMenu.size}")
    }

    fun mostrarUsuarios(usuarios: List<Usuario>) {
        println("\n--- CLIENTES Y REPARTIDORES ---")
        for (usuario in usuarios) {
            println("  ${usuario.descripcion()}")
        }
    }
}
