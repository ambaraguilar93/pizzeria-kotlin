import kotlin.reflect.KClass
import kotlin.reflect.KType
import kotlin.reflect.full.findAnnotation
import kotlin.reflect.full.memberFunctions
import kotlin.reflect.full.memberProperties

// Un producto que lleva @Promocionable, junto con el porcentaje y el motivo de su promoción.
data class ProductoPromocionable(val producto: Producto, val porcentaje: Int, val motivo: String)

// Lee la anotación de la clase. Devuelve null si no la lleva.
fun promocionDe(producto: Producto): Promocionable? =
    producto::class.findAnnotation<Promocionable>()

fun esPromocionable(producto: Producto): Boolean = promocionDe(producto) != null

fun motivoPromocion(producto: Producto): String? = promocionDe(producto)?.motivo

fun porcentajePromocion(producto: Producto): Int = promocionDe(producto)?.porcentaje ?: 0

// Descubre cuáles productos son promocionables.
fun descubrirPromocionables(productos: List<Producto>): List<ProductoPromocionable> =
    productos.mapNotNull { producto ->
        promocionDe(producto)?.let { promo -> ProductoPromocionable(producto, promo.porcentaje, promo.motivo) }
    }

// Propiedades y métodos de una clase, obtenidos con reflection.
data class DescripcionClase(
    val nombre: String,
    val propiedades: List<String>,
    val metodos: List<String>
)

// Lista las propiedades y los métodos de cualquier clase.
fun describirClase(clase: KClass<*>): DescripcionClase =
    DescripcionClase(
        nombre = clase.simpleName ?: "(clase anónima)",
        propiedades = clase.memberProperties
            .sortedBy { it.name }
            .map { "${it.name}: ${nombreDeTipo(it.returnType)}" },
        metodos = clase.memberFunctions
            .map { it.name }
            .distinct()
            .sorted()
    )

private fun nombreDeTipo(tipo: KType): String {
    val nombre = (tipo.classifier as? KClass<*>)?.simpleName ?: tipo.toString()
    return if (tipo.isMarkedNullable) "$nombre?" else nombre
}
