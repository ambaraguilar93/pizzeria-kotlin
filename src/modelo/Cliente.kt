class Cliente(
    nombre: String,
    val direccion: String,
    val telefono: String,
    val correo: String
) : Usuario(nombre, telefono) {

    override fun descripcion(): String = "Cliente: $nombre - $telefono - $correo"

    fun tieneCorreoRegistrado(): Boolean = correo != CORREO_POR_DEFECTO

    // Sirve cuando un cliente ya registrado pide que su pedido se envíe a una dirección distinta.
    fun conDireccion(nuevaDireccion: String): Cliente =
        if (nuevaDireccion == direccion) this else Cliente(nombre, nuevaDireccion, telefono, correo)

    // Sirve cuando un cliente ya registrado ingresa un telefono diferente al registrado.
    fun conTelefono(nuevoTelefono: String): Cliente =
        if (nuevoTelefono == telefono) this else Cliente(nombre, direccion, nuevoTelefono, correo)

    // Desestructuración.
    // Cliente no es data class, por eso las funciones component se declaran a mano.
    operator fun component1(): String = nombre
    operator fun component2(): String = correo
    operator fun component3(): String = telefono

    // Igualdad.
    // Dos clientes son el mismo si coinciden nombre y correo.
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Cliente) return false
        return nombre.trim().equals(other.nombre.trim(), ignoreCase = true) &&
                correo.trim().equals(other.correo.trim(), ignoreCase = true)
    }

    // Mismos atributos que equals.
    override fun hashCode(): Int = 31 * nombre.trim().lowercase().hashCode() + correo.trim().lowercase().hashCode()

    override fun toString(): String = "Cliente(nombre=$nombre, correo=$correo)"
}
