class Cliente(
    nombre: String,
    val direccion: String,
    val telefono: String,
    val correo: String
) : Usuario(nombre, telefono) {

    override fun descripcion(): String = "Cliente: $nombre - $telefono - $correo"

    fun tieneCorreoRegistrado(): Boolean = correo != CORREO_POR_DEFECTO
}
