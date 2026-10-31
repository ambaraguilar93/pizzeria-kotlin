// Flujo coordinado.
// Pide datos a la interfaz, llama al backend y le pide a la interfaz mostrar el resultado.
class PizzeriaControlador(
    private val operaciones: OperacionesPizzeria,
    private val ui: PizzeriaUI
) {
    fun iniciar() {
        ui.mostrarBienvenida()

        var opcion: Int
        do {
            opcion = ui.leerOpcionMenu()

            when (opcion) {
                1 -> registrarPedido()
                2 -> ui.mostrarReporteDelDia(operaciones.obtenerReporteDelDia())
                3 -> avanzarPedido()
                4 -> ui.mostrarInventario(operaciones.obtenerInventario(), operaciones.obtenerIngredientesDelMenu())
                5 -> reabastecer()
                6 -> ui.mostrarEstadisticas(operaciones.obtenerEstadisticas())
                7 -> ui.mostrarUsuarios(operaciones.listarUsuarios())
                8 -> ejecutarPruebas()
                0 -> ui.mostrarMensaje("¡Hasta pronto!")
                else -> ui.mostrarMensaje("Opción no válida.")
            }
        } while (opcion != 0)
    }

    private fun registrarPedido() {
        val cliente = ui.leerCliente()
        val productos = ui.leerProductos(operaciones.listarProductos())

        if (productos.isEmpty()) {
            ui.mostrarMensaje("El pedido no tiene pizzas. Pedido cancelado.")
            return
        }

        val notas = ui.leerNotas()

        try {
            val pedido = operaciones.crearPedido(cliente, productos, notas)

            val disponibilidad = operaciones.verificarDisponibilidad(productos)
            if (disponibilidad is Disponibilidad.Insuficiente) {
                ui.mostrarFaltantes(disponibilidad)
                ui.mostrarMensaje("Pedido cancelado por falta de ingredientes.")
                return
            }

            ui.mostrarResumenPedido(pedido, confirmado = false)
            if (!ui.confirmar("¿Confirmar pedido? (s/n): ")) {
                ui.mostrarMensaje("Pedido cancelado.")
                return
            }

            when (val resultado = operaciones.confirmarPedido(pedido)) {
                is Disponibilidad.Insuficiente -> ui.mostrarFaltantes(resultado)
                Disponibilidad.Suficiente -> {
                    ui.mostrarResumenPedido(pedido, confirmado = true)
                    ui.mostrarNotificacion(pedido)
                }
            }
        } catch (e: IllegalArgumentException) {
            ui.mostrarError(e.message)
        } catch (e: IllegalStateException) {
            ui.mostrarError(e.message)
        } finally {
            ui.mostrarMensaje("(Fin del proceso del pedido)")
        }
    }

    private fun avanzarPedido() {
        ui.mostrarReporteDelDia(operaciones.obtenerReporteDelDia())
        val id = ui.leerEntero("N° de pedido a avanzar: ", 0)

        try {
            val resultado = operaciones.avanzarPedido(id)
            ui.mostrarMensaje(resultado.mensaje)
            ui.mostrarNotificacion(resultado.pedido)
        } catch (e: IllegalArgumentException) {
            ui.mostrarError(e.message)
        } catch (e: IllegalStateException) {
            ui.mostrarError(e.message)
        }
    }

    private fun reabastecer() {
        ui.mostrarMensaje("Ingredientes disponibles para reabastecer: ${operaciones.obtenerInventario().keys.joinToString(", ")}")
        val ingrediente = ui.leerTexto("Ingrediente a reabastecer: ", "").lowercase()
        if (ingrediente.isEmpty()) return

        val cantidad = ui.leerEntero("Cantidad a agregar: ", 0)

        try {
            operaciones.reabastecer(ingrediente, cantidad)
            ui.mostrarMensaje("Listo: $ingrediente ahora tiene ${operaciones.obtenerInventario()[ingrediente]} unidades.")
        } catch (e: IllegalArgumentException) {
            ui.mostrarError(e.message)
        }
    }
}
