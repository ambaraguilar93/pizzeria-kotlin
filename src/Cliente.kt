// Modelo que representa a un cliente de la pizzería.
// Todos los atributos son val porque una vez registrado, los datos del cliente no cambian.
data class Cliente(
    val nombre: String,
    val direccion: String,
    val telefono: String   // String y no Int para usar formato +56
)