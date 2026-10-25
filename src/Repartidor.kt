// Modelo que representa a un repartidor y si está libre para tomar un pedido.
data class Repartidor(
    val nombre: String,
    var disponible: Boolean = true   // var porque pasa a false cuando se le asigna un pedido.
)