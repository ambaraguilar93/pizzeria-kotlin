import java.time.LocalDate

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

    // Pide el dato una y otra vez hasta que sea válido.
    fun <T> leerHastaValido(
        mensaje: String,
        mensajeError: (String) -> String,
        convertir: (String) -> T?
    ): T? {
        while (true) {
            print(mensaje)
            val texto = leerLinea()?.trim() ?: ""

            val valor = convertir(texto)
            if (valor != null) return valor

            if (entradaTerminada) return null
            println(mensajeError(texto))
        }
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
        val telefono = leerHastaValido(
            mensaje = "Teléfono (10 dígitos, o 12 con código de país, ej: 222 555 0147): ",
            mensajeError = { "Teléfono no válido: debe tener 10 dígitos (sin código de país) o 12 (con código de país)." },
            convertir = ::formatearTelefono
        ) ?: TELEFONO_POR_DEFECTO
        val correo = leerHastaValido(
            mensaje = "Correo electrónico: ",
            mensajeError = ::describirErrorCorreo,
            convertir = { texto -> texto.takeIf { esCorreoValido(it) } }
        ) ?: CORREO_POR_DEFECTO

        return Cliente(nombre, direccion, telefono, correo)
    }

    // Aviso cuando el cliente ya estaba registrado y no se guarda de nuevo.
    fun mostrarClienteRepetido(existente: Cliente) {
        println("El cliente ya está registrado: ${existente.nombre} (${existente.correo}). No se guarda de nuevo.")
    }

    // El cliente decide si cambia el teléfono que tenemos registrado.
    fun preguntarActualizarTelefono(existente: Cliente, ingresado: Cliente): Boolean =
        confirmar("Ya tenemos este teléfono: ${existente.telefono}. ¿Actualizarlo a ${ingresado.telefono}? (s/n): ")

    // El cliente decide a qué dirección va este pedido (el registro no cambia).
    fun preguntarDireccionDelPedido(existente: Cliente, ingresado: Cliente): Boolean {
        println("Dirección registrada anteriormente: ${existente.direccion}")
        println("Nueva dirección ingresada:  ${ingresado.direccion}")
        return confirmar("¿Enviar este pedido a la dirección nueva? (s = nueva, n = anterior): ")
    }

    fun leerFechaPedido(): LocalDate =
        leerHastaValido(
            mensaje = "Fecha del pedido (dd/mm/aaaa, Enter para hoy): ",
            mensajeError = { "Fecha no válida: usa el formato dd/mm/aaaa y una fecha que exista (ej: 10/11/2026)." },
            convertir = { texto -> if (texto.isEmpty()) LocalDate.now() else parsearFecha(texto) }
        ) ?: LocalDate.now()

    private fun mensajeErrorCantidad(texto: String): String {
        val rango = "entre ${RANGO_CANTIDAD.first} y ${RANGO_CANTIDAD.last}"
        val numero = convertirEntero(texto) ?: return "Debes ingresar un número entero $rango."
        return "La cantidad debe estar $rango (ingresaste $numero)."
    }

    // Menú de productos, deja seleccionar uno o mas
    fun leerProductos(catalogo: List<Producto>): List<Producto> {
        val seleccion = mutableListOf<Producto>()

        do {
            println("\n--- MENÚ ---")
            catalogo.forEachIndexed { indice, producto -> println("  ${indice + 1}. ${producto.detallarProducto()}") }

            val opcion = leerEntero("Elige un producto (1-${catalogo.size}): ", 0)
            val producto = catalogo.getOrNull(opcion - 1)

            if (producto == null) {
                println("Opción no válida.")
            } else {
                // La cantidad debe estar en el rango 1..100: si no, se muestra el error y se vuelve a pedir.
                val cantidad = leerHastaValido(
                    mensaje = "Cantidad de ${producto.nombre} (${RANGO_CANTIDAD.first}-${RANGO_CANTIDAD.last}): ",
                    mensajeError = ::mensajeErrorCantidad,
                    convertir = { texto -> convertirEntero(texto)?.takeIf { esCantidadValida(it) } }
                )
                if (cantidad != null) {
                    seleccion.addAll(List(cantidad) { producto })
                }
            }
        } while (confirmar("¿Agregar otro producto? (s/n): "))

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
        println("9. Ver resumen completo de la Semana 3")
        println("0. Salir")
        println("====================================")
    }

    fun mostrarMensaje(mensaje: String) = println(mensaje)

    // Resumen ordenado que sirve de confirmación para el cliente y para la pizzería.
    fun mostrarResumenSemana3(secciones: List<SeccionResumen>, nota: String? = null) {
        println("\n=====================================================")
        println("   RESUMEN DE LA PIZZERÍA - SEMANA 3")
        println("=====================================================")
        for (seccion in secciones) {
            println("\n[ ${seccion.titulo} ]")
            seccion.lineas.forEach { println("  $it") }
        }
        nota?.let { println("\n$it") }
        println("=====================================================")
    }

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
        // Los productos con @Promocionable se descubren con reflection
        val promocionables = pedido.productosPromocionables()
        if (promocionables.isNotEmpty()) {
            println("PROMOCIONABLES")
            for (promocionable in promocionables) {
                println("  ${promocionable.producto.nombre} (${promocionable.motivo}: -${promocionable.porcentaje}%)")
            }
        }
        println("TOTALES")
        println("  Subtotal:  ${formatearPrecio(totales.subtotal)}")
        println("  Descuento: -${formatearPrecio(totales.descuento)}")
        if (totales.descuentoAnotaciones > 0) {
            println("    (incluye productos @Promocionable: -${formatearPrecio(totales.descuentoAnotaciones)})")
        }
        if (totales.descuentoPromocion > 0) {
            println("    (incluye promoción del periodo: -${formatearPrecio(totales.descuentoPromocion)})")
        }
        println("  IVA ($TASA_IVA%): ${formatearPrecio(totales.impuestos)}")
        println("  TOTAL:     ${formatearPrecio(totales.total)}")
        println("ESTADO")
        println("  Fecha:      ${formatearFecha(pedido.fecha)}")
        println("  Estado:     ${pedido.estado.etiqueta}")
        println("  Repartidor: ${pedido.repartidor?.nombre ?: "Se asigna cuando el pedido esté listo"}")
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
            println("   Productos: ${pedido.resumenProductos()}")
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
