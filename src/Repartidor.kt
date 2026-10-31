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

    fun entregarPedido(pedido: Pedido): String {
        check(pedido.estado == EstadoPedido.LISTO) {
            "El pedido N° ${pedido.id} no está listo (estado actual: ${pedido.estado.etiqueta})."
        }
        pedido.cambiarEstado(EstadoPedido.EN_CAMINO)
        return "$nombre sale con el pedido N° ${pedido.id} hacia ${pedido.cliente.direccion}."
    }

    override fun descripcion(): String =
        "Repartidor: $nombre a $distanciaKm km (${if (disponible) "disponible" else "ocupado"})"
}
