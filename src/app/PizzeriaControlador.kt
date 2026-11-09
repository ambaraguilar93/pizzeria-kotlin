// Flujo coordinado.
// Pide datos a la interfaz, llama al backend y le pide a la interfaz mostrar el resultado.
class PizzeriaControlador(
    private val operaciones: OperacionesPizzeria,
    private val ui: PizzeriaUI,
    private val alIniciar: () -> Unit = {}
) {
    fun iniciar() {
        ui.mostrarBienvenida()
        alIniciar()

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
                9 -> mostrarYGuardarResumen()
                0 -> ui.mostrarMensaje("¡Hasta pronto!")
                else -> ui.mostrarMensaje("Opción no válida.")
            }
        } while (opcion != 0)
    }

    // Registra al cliente evitando duplicados.
    // Si el cliente ya existe y ingresó otro teléfono u otra dirección, él decide cuál se usa.
    private fun identificarCliente(ingresado: Cliente): Cliente =
        when (val resultado = operaciones.registrarCliente(ingresado)) {
            is ResultadoRegistro.Registrado -> {
                ui.mostrarMensaje("Cliente nuevo registrado: ${ingresado.nombre}.")
                resultado.elemento
            }
            is ResultadoRegistro.Repetido -> {
                var cliente = resultado.existente
                ui.mostrarClienteRepetido(cliente)

                // Teléfono: si acepta, se actualiza en el registro (queda para los próximos pedidos).
                if (ingresado.telefono != cliente.telefono && ui.preguntarActualizarTelefono(cliente, ingresado)) {
                    cliente = operaciones.actualizarTelefono(cliente, ingresado.telefono)
                    ui.mostrarMensaje("Teléfono actualizado a ${cliente.telefono}.")
                }
                // Dirección: solo vale para este pedido, el registro no cambia.
                if (ingresado.direccion != cliente.direccion && ui.preguntarDireccionDelPedido(cliente, ingresado)) {
                    cliente = cliente.conDireccion(ingresado.direccion)
                }
                cliente
            }
        }

    private fun registrarPedido() {
        val cliente = identificarCliente(ui.leerCliente())
        val productos = ui.leerProductos(operaciones.listarProductos())

        if (productos.isEmpty()) {
            ui.mostrarMensaje("El pedido no tiene productos. Pedido cancelado.")
            return
        }

        val fecha = ui.leerFechaPedido()
        val notas = ui.leerNotas()

        try {
            val pedido = operaciones.crearPedido(cliente, productos, notas, fecha)

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

    private fun mostrarYGuardarResumen() {
        val resumen = armarResumenSemana3()
        ui.mostrarResumenSemana3(resumen)
        try {
            ui.mostrarMensaje("Resumen guardado en: ${guardarResumen(resumen)}")
        } catch (e: java.io.IOException) {
            ui.mostrarError("No se pudo guardar el archivo (${e.message}).")
        }
    }
}
