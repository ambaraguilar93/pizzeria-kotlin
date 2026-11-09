typealias Inventario = MutableMap<String, Int>

fun crearInventarioInicial(): Inventario = mutableMapOf(
    "masa" to 25,
    "salsa de tomate" to 25,
    "queso" to 25,
    "pepperoni" to 10,
    "albahaca" to 8,
    "jamón" to 12,
    "piña" to 6
)

// Calcula cuanto de cada ingrediente se necesita
fun calcularIngredientesNecesarios(productos: List<Producto>): Map<String, Int> =
    productos
        .flatMap { it.ingredientes.entries }
        .groupBy({ it.key }, { it.value })
        .mapValues { (_, cantidades) -> cantidades.sum() }

// Devuelve los ingredientes faltantes
fun calcularFaltantes(inventario: Map<String, Int>, necesarios: Map<String, Int>): List<Faltante> =
    necesarios
        .filter { (ingrediente, cantidad) -> (inventario[ingrediente] ?: 0) < cantidad }
        .map { (ingrediente, cantidad) -> Faltante(ingrediente, cantidad, inventario[ingrediente] ?: 0) }

// Descuenta del inventario lo consumido
fun descontarInventario(inventario: Inventario, necesarios: Map<String, Int>) {
    val nuevosValores = necesarios.mapValues { (ingrediente, cantidad) ->
        (inventario[ingrediente] ?: 0) - cantidad
    }
    require(nuevosValores.values.all { it >= 0 }) {
        "El inventario no puede quedar con cantidades negativas."
    }
    inventario.putAll(nuevosValores)
}

// Calcula los ingredientes necesarios y actualiza el inventario.
// Devuelve lo que se descontó. Si no alcanza, lanza error y no modifica nada.
fun prepararLote(inventario: Inventario, productos: List<Producto>): Map<String, Int> {
    val necesarios = calcularIngredientesNecesarios(productos)
    descontarInventario(inventario, necesarios)
    return necesarios
}

// Suma stock a un ingrediente que ya existe. La cantidad debe ser positiva.
fun reabastecer(inventario: Inventario, ingrediente: String, cantidad: Int) {
    require(ingrediente in inventario) { "El ingrediente '$ingrediente' no existe en el inventario." }
    require(cantidad > 0) { "La cantidad a reabastecer debe ser mayor a 0 (recibido: $cantidad)." }
    inventario[ingrediente] = (inventario[ingrediente] ?: 0) + cantidad
}
