import java.time.LocalDate

// Capa - Interfaz con las funciones
interface OperacionesPizzeria {
    fun listarProductos(): List<Producto>
    fun listarUsuarios(): List<Usuario>

    // Registro sin duplicados: un cliente (nombre + correo) o un producto (nombre + categoría) repetido no se guarda de nuevo.
    fun registrarCliente(cliente: Cliente): ResultadoRegistro<Cliente>
    // El cliente registrado cambia su teléfono (sigue siendo el mismo cliente). Devuelve el cliente actualizado.
    fun actualizarTelefono(cliente: Cliente, nuevoTelefono: String): Cliente
    fun registrarProducto(producto: Producto): ResultadoRegistro<Producto>

    fun crearPedido(cliente: Cliente, productos: List<Producto>, notas: String?, fecha: LocalDate = LocalDate.now()): Pedido
    fun verificarDisponibilidad(productos: List<Producto>): Disponibilidad
    fun confirmarPedido(pedido: Pedido): Disponibilidad
    fun avanzarPedido(idPedido: Int): ResultadoAvance

    fun obtenerReporteDelDia(): ReporteDia
    fun obtenerEstadisticas(): Estadisticas

    fun obtenerInventario(): Map<String, Int>
    fun obtenerIngredientesDelMenu(): Set<String>
    fun reabastecer(ingrediente: String, cantidad: Int)
}
