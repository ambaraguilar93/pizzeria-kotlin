open class Usuario(
    val nombre: String,
    val contacto: String
) {
    open fun descripcion(): String = "$nombre ($contacto)"
}
