import java.time.LocalDate
import kotlin.math.roundToInt

// Reglas de negocio
const val TASA_IVA = 19
const val PIZZAS_PARA_DESCUENTO = 3
const val PORCENTAJE_DESCUENTO = 10

// Periodo de promoción con un rango de fechas.
val INICIO_PROMOCION: LocalDate = LocalDate.of(2026, 11, 1)
val FIN_PROMOCION: LocalDate = LocalDate.of(2026, 11, 30)
val PERIODO_PROMOCION = INICIO_PROMOCION..FIN_PROMOCION
const val PORCENTAJE_PROMOCION = 5

// Resultado del cálculo de un pedido
data class Totales(
    val subtotal: Int,
    val descuento: Int,
    val impuestos: Int,
    val total: Int,
    val descuentoPromocion: Int = 0,
    val descuentoAnotaciones: Int = 0
)

fun contarPizzas(productos: List<Producto>): Int =
    productos.count { it.categoria == CategoriaProducto.PIZZA }

fun calcularDescuento(subtotal: Int, cantidadPizzas: Int): Int =
    if (cantidadPizzas > PIZZAS_PARA_DESCUENTO) subtotal * PORCENTAJE_DESCUENTO / 100 else 0

fun estaEnPromocion(fecha: LocalDate): Boolean = fecha in PERIODO_PROMOCION

// Descuento adicional del 5% si el pedido se hizo dentro del periodo de promoción.
fun calcularDescuentoPromocion(subtotal: Int, fecha: LocalDate?): Int =
    if (fecha != null && estaEnPromocion(fecha)) subtotal * PORCENTAJE_PROMOCION / 100 else 0

// Descuento de los productos @Promocionable.
fun calcularDescuentoAnotaciones(productos: List<Producto>): Int =
    productos.sumOf { it.precio * porcentajePromocion(it) / 100 }

// Total (subtotal, descuentos e IVA).
fun calcularTotales(productos: List<Producto>, fecha: LocalDate? = null): Totales {
    val subtotal = productos.sumOf { it.precio }
    val descuentoAnotaciones = calcularDescuentoAnotaciones(productos)
    val subtotalConPromos = subtotal - descuentoAnotaciones
    val descuentoPromocion = calcularDescuentoPromocion(subtotalConPromos, fecha)
    val descuento = descuentoAnotaciones + calcularDescuento(subtotalConPromos, contarPizzas(productos)) + descuentoPromocion
    val base = subtotal - descuento
    val impuestos = (base * TASA_IVA / 100.0).roundToInt()
    return Totales(subtotal, descuento, impuestos, base + impuestos, descuentoPromocion, descuentoAnotaciones)
}

// Se asigna repartidor al pedido
fun elegirRepartidor(repartidores: List<Repartidor>): Repartidor? =
    repartidores.sortedBy { it.distanciaKm }.find { it.disponible }
