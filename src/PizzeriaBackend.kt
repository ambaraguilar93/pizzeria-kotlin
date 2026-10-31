// Clase que hace el trabajo del negocio.
// Implementa la interfaz y escribe el codigo de cada funcion.
class PizzeriaBackend(
    private val catalogo: List<Producto>,
    private val inventario: Inventario,
    private val repartidores: List<Repartidor>,
    pedidosIniciales: Array<Pedido>
) : OperacionesPizzeria {

    // Arreglo con los pedidos
    private var pedidosDelDia: Array<Pedido> = pedidosIniciales

    override fun listarProductos(): List<Producto> = catalogo

    // Polimorfismo porque tanto clientes como repartidores se tratan como Usuario
    override fun listarUsuarios(): List<Usuario> {
        val clientes: List<Cliente> = pedidosDelDia.map { it.cliente }.distinctBy { it.nombre }
        return clientes + repartidores
    }

    override fun crearPedido(cliente: Cliente, productos: List<Producto>, notas: String?): Pedido {
        val nuevoId = (pedidosDelDia.maxOfOrNull { it.id } ?: 0) + 1
        return Pedido(nuevoId, cliente, productos, notas)
    }

    override fun verificarDisponibilidad(productos: List<Producto>): Disponibilidad {
        val necesarios = calcularIngredientesNecesarios(productos)
        val faltantes = calcularFaltantes(inventario, necesarios)

        return when {
            faltantes.isEmpty() -> Disponibilidad.Suficiente
            else -> Disponibilidad.Insuficiente(faltantes)
        }
    }

    // Aqui se descuentan los ingredientes, se asigna un repartidor y se registra el pedido.
    override fun confirmarPedido(pedido: Pedido): Disponibilidad {
        check(pedidosDelDia.none { it.id == pedido.id }) { "El pedido N° ${pedido.id} ya fue confirmado." }

        val disponibilidad = verificarDisponibilidad(pedido.productos)
        if (disponibilidad is Disponibilidad.Suficiente) {
            prepararLote(inventario, pedido.productos)
            pedido.asignarRepartidor(elegirRepartidor(repartidores)?.also { it.ocupar() })
            pedidosDelDia += pedido
        }
        return disponibilidad
    }

    override fun avanzarPedido(idPedido: Int): ResultadoAvance {
        val pedido = pedidosDelDia.find { it.id == idPedido }
            ?: throw IllegalArgumentException("No existe el pedido N° $idPedido.")

        val mensaje = when (pedido.estado) {
            EstadoPedido.EN_PREPARACION -> {
                pedido.cambiarEstado(EstadoPedido.LISTO)
                "El pedido N° $idPedido está ${pedido.estado.etiqueta.lowercase()}."
            }
            EstadoPedido.LISTO -> {
                val repartidor = pedido.repartidor
                    ?: throw IllegalStateException("El pedido N° $idPedido no tiene repartidor asignado.")
                repartidor.entregarPedido(pedido)
            }
            EstadoPedido.EN_CAMINO ->
                throw IllegalStateException("El pedido N° $idPedido ya está en camino.")
        }
        return ResultadoAvance(pedido, mensaje)
    }

    override fun obtenerReporteDelDia(): ReporteDia {
        val pendientes = pedidosDelDia.filter { it.estado == EstadoPedido.EN_PREPARACION }
        val completados = pedidosDelDia.filter { it.estado != EstadoPedido.EN_PREPARACION }
        return ReporteDia(pedidosDelDia.toList(), completados, pendientes, pedidosDelDia.sumOf { it.totales.total })
    }

    override fun obtenerEstadisticas(): Estadisticas {
        val vendidos = pedidosDelDia.flatMap { it.productos }
        val unidades = vendidos.groupingBy { it.nombre }.eachCount()
        val ventaBruta = vendidos.groupBy { it.nombre }.mapValues { (_, lista) -> lista.sumOf { it.precio } }

        return Estadisticas(
            unidadesPorProducto = catalogo.associate { it.nombre to (unidades[it.nombre] ?: 0) },
            ventaBrutaPorProducto = catalogo.associate { it.nombre to (ventaBruta[it.nombre] ?: 0) },
            unidadesTotales = vendidos.size,
            totalRecaudado = pedidosDelDia.sumOf { it.totales.total }
        )
    }

    override fun obtenerInventario(): Map<String, Int> = inventario.toMap()

    override fun obtenerIngredientesDelMenu(): Set<String> =
        catalogo.flatMap { it.ingredientes.keys }.toSet()

    override fun reabastecer(ingrediente: String, cantidad: Int) {
        reabastecer(inventario, ingrediente, cantidad)
    }
}
