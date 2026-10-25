// Modelo que representa una pizza del menú y su inventario de ingredientes.
data class Pizza(
    val nombrePizza: String,
    val precioUnitario: Int,
    var ingredientesDisponibles: Int   // var porque se descuenta cuando se confirma un pedido
)