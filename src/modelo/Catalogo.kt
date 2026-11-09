// Menú de la pizzería.
// Los que llevan @Promocionable califican para promociones especiales.

class PizzaPepperoni : Producto(
    nombre = "Pepperoni",
    precio = 9500,
    ingredientes = mapOf("masa" to 1, "salsa de tomate" to 1, "queso" to 1, "pepperoni" to 1),
    categoria = CategoriaProducto.PIZZA
)

@Promocionable(porcentaje = 15, motivo = "Promo vegetariana")
class PizzaMargarita : Producto(
    nombre = "Margarita",
    precio = 8500,
    ingredientes = mapOf("masa" to 1, "salsa de tomate" to 1, "queso" to 1, "albahaca" to 1),
    categoria = CategoriaProducto.PIZZA
)

@Promocionable(porcentaje = 10, motivo = "Promo tropical")
class PizzaHawaiana : Producto(
    nombre = "Hawaiana",
    precio = 9000,
    ingredientes = mapOf("masa" to 1, "salsa de tomate" to 1, "queso" to 1, "jamón" to 1, "piña" to 1),
    categoria = CategoriaProducto.PIZZA
)

class BebidaFamiliar : Producto(
    nombre = "Bebida 1,5 L",
    precio = 2500,
    categoria = CategoriaProducto.BEBIDA
)

// Ingrediente extra se descuenta del inventario el ingrediente que representa.
class ExtraQueso : Producto(
    nombre = "Extra queso",
    precio = 1500,
    ingredientes = mapOf("queso" to 1),
    categoria = CategoriaProducto.INGREDIENTE
)

// Combo es una pizza pepperoni con su bebida. Consume los ingredientes de la pizza.
@Promocionable(porcentaje = 20, motivo = "Combo familiar")
class ComboFamiliar : Producto(
    nombre = "Combo Familiar",
    precio = 11000,
    ingredientes = mapOf("masa" to 1, "salsa de tomate" to 1, "queso" to 1, "pepperoni" to 1),
    categoria = CategoriaProducto.COMBO
)

fun crearCatalogo(): List<Producto> = listOf(
    PizzaPepperoni(), PizzaMargarita(), PizzaHawaiana(), BebidaFamiliar(), ExtraQueso(), ComboFamiliar()
)
