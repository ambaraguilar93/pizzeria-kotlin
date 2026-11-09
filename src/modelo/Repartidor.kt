class Repartidor(
    nombre: String,
    contacto: String,
    val distanciaKm: Double,
    disponibleInicial: Boolean = true
) : Usuario(nombre, contacto) {

    var disponible: Boolean = disponibleInicial
        private set

    fun ocupar() {
        check(disponible) { "$nombre ya está ocupado." }
        disponible = false
    }

    fun liberar() {
        disponible = true
    }

    // Despacho, el pedido sale del local (estado cambia de Listo → En camino).
    fun despacharPedido(pedido: Pedido): String {
        check(pedido.estado == EstadoPedido.LISTO) {
            "El pedido N° ${pedido.id} no está listo (estado actual: ${pedido.estado.etiqueta})."
        }
        pedido.cambiarEstado(EstadoPedido.EN_CAMINO)
        return "$nombre sale con el pedido N° ${pedido.id} hacia ${pedido.cliente.direccion}."
    }

    // Fin del despacho, el pedido llega al cliente (estado cambia de En camino → Entregado) y el repartidor queda libre.
    fun finalizarEntrega(pedido: Pedido): String {
        check(pedido.estado == EstadoPedido.EN_CAMINO) {
            "El pedido N° ${pedido.id} no está en camino (estado actual: ${pedido.estado.etiqueta})."
        }
        pedido.cambiarEstado(EstadoPedido.ENTREGADO)
        liberar()
        return "$nombre entregó el pedido N° ${pedido.id} y queda disponible nuevamente."
    }

    override fun descripcion(): String =
        "Repartidor: $nombre a $distanciaKm km (${if (disponible) "disponible" else "ocupado"})"
}
