// Aqui se agrupan varias clases que transportan datos del backend a la interfaz.

data class Faltante(val ingrediente: String, val requeridos: Int, val disponibles: Int) {
    val cantidadFaltante: Int get() = requeridos - disponibles
}

sealed interface Disponibilidad {
    object Suficiente : Disponibilidad
    data class Insuficiente(val faltantes: List<Faltante>) : Disponibilidad
}

data class ReporteDia(
    val pedidos: List<Pedido>,
    val completados: List<Pedido>,
    val pendientes: List<Pedido>,
    val totalVendido: Int
)

data class Estadisticas(
    val unidadesPorProducto: Map<String, Int>,
    val ventaBrutaPorProducto: Map<String, Int>,
    val unidadesTotales: Int,
    val totalRecaudado: Int
)

data class ResultadoAvance(val pedido: Pedido, val mensaje: String)
