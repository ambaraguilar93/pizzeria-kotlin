// Anotación que marca los productos (pizzas o combos) y califican para promociones especiales.
@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.RUNTIME)
@MustBeDocumented
annotation class Promocionable(
    val porcentaje: Int,
    val motivo: String
)
