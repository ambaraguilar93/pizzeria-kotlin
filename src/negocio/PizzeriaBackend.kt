import java.time.LocalDate

// Clase que hace el trabajo del negocio.
// Implementa la interfaz y escribe el codigo de cada funcion.
class PizzeriaBackend(
    catalogo: List<Producto>,
    private val inventario: Inventario,
    private val repartidores: List<Repartidor>,
    pedidosIniciales: Array<Pedido>
) : OperacionesPizzeria {

    // Arreglo con los pedidos
    private var pedidosDelDia: Array<Pedido> = pedidosIniciales

    // Registros sin duplicados (usan el equals/hashCode de Cliente y de Producto).
    private val registroClientes = Registro(pedidosIniciales.map { it.cliente })
    private val registroProductos = Registro(catalogo)

    override fun listarProductos(): List<Producto> = registroProductos.listar()

    override fun listarUsuarios(): List<Usuario> = registroClientes.listar() + repartidores

    override fun registrarCliente(cliente: Cliente): ResultadoRegistro<Cliente> = registroClientes.registrar(cliente)

    override fun actualizarTelefono(cliente: Cliente, nuevoTelefono: String): Cliente {
        val actualizado = cliente.conTelefono(nuevoTelefono)
        check(registroClientes.actualizar(actualizado)) { "El cliente ${cliente.nombre} no está registrado." }
        return actualizado
    }

    override fun registrarProducto(producto: Producto): ResultadoRegistro<Producto> = registroProductos.registrar(producto)

    override fun crearPedido(cliente: Cliente, productos: List<Producto>, notas: String?, fecha: LocalDate): Pedido {
        val nuevoId = (pedidosDelDia.maxOfOrNull { it.id } ?: 0) + 1
        return Pedido(nuevoId, cliente, productos, notas, fecha = fecha)
    }

    override fun verificarDisponibilidad(productos: List<Producto>): Disponibilidad {
        val necesarios = calcularIngredientesNecesarios(productos)
        val faltantes = calcularFaltantes(inventario, necesarios)

        return when {
            faltantes.isEmpty() -> Disponibilidad.Suficiente
            else -> Disponibilidad.Insuficiente(faltantes)
        }
    }

    // Aqui se descuentan los ingredientes y se registra el pedido (en preparación).
    override fun confirmarPedido(pedido: Pedido): Disponibilidad {
        check(pedidosDelDia.none { it.id == pedido.id }) { "El pedido N° ${pedido.id} ya fue confirmado." }

        val disponibilidad = verificarDisponibilidad(pedido.productos)
        if (disponibilidad is Disponibilidad.Suficiente) {
            prepararLote(inventario, pedido.productos)
            pedidosDelDia += pedido
        }
        return disponibilidad
    }

    // Elige el repartidor disponible más cercano, lo ocupa y se lo asigna al pedido (null si no hay ninguno).
    private fun asignarRepartidor(pedido: Pedido): Repartidor? =
        elegirRepartidor(repartidores)?.also {
            it.ocupar()
            pedido.asignarRepartidor(it)
        }

    // Cada llamada avanza un paso (En preparación → Listo → En camino → Entregado).
    override fun avanzarPedido(idPedido: Int): ResultadoAvance {
        val pedido = pedidosDelDia.find { it.id == idPedido }
            ?: throw IllegalArgumentException("No existe el pedido N° $idPedido.")

        val mensaje = when (pedido.estado) {
            EstadoPedido.EN_PREPARACION -> {
                pedido.cambiarEstado(EstadoPedido.LISTO)
                val asignacion = asignarRepartidor(pedido)
                    ?.let { "Repartidor asignado: ${it.nombre}." }
                    ?: "No hay repartidores disponibles por ahora; se asignará al despachar."
                "El pedido N° $idPedido está ${pedido.estado.etiqueta.lowercase()}. $asignacion"
            }
            EstadoPedido.LISTO -> {
                // Si no había repartidores, se intenta asignar uno ahora.
                val repartidor = pedido.repartidor
                    ?: asignarRepartidor(pedido)
                    ?: throw IllegalStateException("No hay repartidores disponibles para despachar el pedido N° $idPedido.")
                repartidor.despacharPedido(pedido)
            }
            EstadoPedido.EN_CAMINO -> {
                val repartidor = pedido.repartidor
                    ?: throw IllegalStateException("El pedido N° $idPedido no tiene repartidor asignado.")
                repartidor.finalizarEntrega(pedido)
            }
            EstadoPedido.ENTREGADO ->
                throw IllegalStateException("El pedido N° $idPedido ya fue entregado.")
        }
        return ResultadoAvance(pedido, mensaje)
    }

    override fun obtenerReporteDelDia(): ReporteDia {
        val pendientes = pedidosDelDia.filter { it.estado != EstadoPedido.ENTREGADO }
        val completados = pedidosDelDia.filter { it.estado == EstadoPedido.ENTREGADO }
        return ReporteDia(pedidosDelDia.toList(), completados, pendientes, pedidosDelDia.sumOf { it.totales.total })
    }

    override fun obtenerEstadisticas(): Estadisticas {
        val vendidos = pedidosDelDia.flatMap { it.productos }
        val unidades = vendidos.groupingBy { it.nombre }.eachCount()
        val ventaBruta = vendidos.groupBy { it.nombre }.mapValues { (_, lista) -> lista.sumOf { it.precio } }

        return Estadisticas(
            unidadesPorProducto = listarProductos().associate { it.nombre to (unidades[it.nombre] ?: 0) },
            ventaBrutaPorProducto = listarProductos().associate { it.nombre to (ventaBruta[it.nombre] ?: 0) },
            unidadesTotales = vendidos.size,
            totalRecaudado = pedidosDelDia.sumOf { it.totales.total }
        )
    }

    override fun obtenerInventario(): Map<String, Int> = inventario.toMap()

    override fun obtenerIngredientesDelMenu(): Set<String> =
        listarProductos().flatMap { it.ingredientes.keys }.toSet()

    override fun reabastecer(ingrediente: String, cantidad: Int) {
        reabastecer(inventario, ingrediente, cantidad)
    }
}
