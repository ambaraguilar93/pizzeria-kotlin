// Capa - Interfaz con las funciones
interface OperacionesPizzeria {
    fun listarProductos(): List<Producto>
    fun listarUsuarios(): List<Usuario>

    fun crearPedido(cliente: Cliente, productos: List<Producto>, notas: String?): Pedido
    fun verificarDisponibilidad(productos: List<Producto>): Disponibilidad
    fun confirmarPedido(pedido: Pedido): Disponibilidad
    fun avanzarPedido(idPedido: Int): ResultadoAvance

    fun obtenerReporteDelDia(): ReporteDia
    fun obtenerEstadisticas(): Estadisticas

    fun obtenerInventario(): Map<String, Int>
    fun obtenerIngredientesDelMenu(): Set<String>
    fun reabastecer(ingrediente: String, cantidad: Int)
}
