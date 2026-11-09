# Pizzería – Semana 3: Creando soluciones con Kotlin

App de consola en Kotlin para gestionar pedidos, clientes, inventario y repartidores. Esta semana suma Regex y Ranges, anotaciones y reflection, sobrecarga de operadores, desestructuración e igualdad de objetos (`equals` / `hashCode`).

## Cómo ejecutarla
- **IntelliJ IDEA:** abrir esta carpeta, abrir `src/app/Main.kt` y presionar el botón verde junto a `fun main()`. `kotlin-reflect` ya viene en `lib/`; si marca error en `kotlin.reflect.full`, agregarla en *File > Project Structure > Modules > Dependencies*.
- **Terminal:** `kotlinc $(find src -name '*.kt') -cp lib/kotlin-reflect-2.0.21.jar -include-runtime -d Main.jar && java -jar Main.jar`
- **Sin compilar:** `java -jar Main.jar`

Al iniciar se muestra la bienvenida y el menú. El resumen de la Semana 3 se ve con la **opción 9**, que además lo guarda en un archivo `.txt` (ver más abajo).

## Menú
1. Registrar nuevo pedido
2. Reporte del día
3. Avanzar estado de un pedido (En preparación → Listo → En camino → Entregado)
4. Ver inventario
5. Reabastecer ingrediente
6. Estadísticas de ventas
7. Ver clientes y repartidores
8. Pruebas de casos límite
9. Resumen completo de la Semana 3 (se muestra en consola y se guarda en `resumen_pizzeria.txt`)
0. Salir

## Reglas de negocio
- **Productos:** pizzas, bebida, extra de queso y combo familiar (categorías `PIZZA`, `BEBIDA`, `INGREDIENTE`, `COMBO`).
- **`@Promocionable`:** Margarita 15 %, Hawaiana 10 %, Combo Familiar 20 % (se lee con reflection).
- **Volumen:** 10 % si el pedido tiene más de 3 pizzas.
- **Periodo de promoción:** 5 % adicional si la fecha está entre 01/11/2026 y 30/11/2026 (ambos incluidos).
- **Orden de descuentos:** primero `@Promocionable`, sobre lo que queda se calculan volumen y periodo. Luego IVA 19 %.
- **Cantidad por producto:** entre 1 y 100.
- **Correo:** formato `nombre@dominio.com`, se explica el error y se vuelve a pedir.
- **Teléfono:** 10 dígitos (se agrega `56`) o 12, se guarda como `+XX (XXX) XXX-XXXX`.
- **Fecha:** `dd/mm/aaaa` o Enter para hoy.
- **Cliente repetido** (mismo nombre y correo): no se guarda de nuevo y el cliente decide:
  - *Teléfono:* se pregunta si se actualiza el registrado, con `s` queda para los próximos pedidos.
  - *Dirección:* elige a cuál va **solo este pedido** (nueva o anterior), el registro no cambia.

## Conceptos de la Semana 3

| Paso | Qué se hizo | Dónde |
|---|---|---|
| 1. Regex y Ranges | Correo, teléfono y fecha con Regex; rango de fechas de promoción y rango de cantidad `1..100` con `in`. | `Validaciones.kt`, `Operaciones.kt`, `PizzeriaUI.kt` |
| 2. Anotaciones y reflection | `@Promocionable(porcentaje, motivo)` descubierta con reflection al procesar el pedido. `describirClase` lista propiedades y métodos de Pedido, Producto y Cliente. | `Anotaciones.kt`, `Catalogo.kt`, `Reflexion.kt` |
| 3. Operadores | `Pedido + Pedido` (mismo cliente) combina productos y recalcula el total. `==` en `Producto` compara nombre + categoría. | `Pedido.kt`, `Producto.kt` |
| 4. Desestructuración | `val (nombre, correo, telefono) = cliente` y `val (cliente, productos, total) = pedido`. | `Cliente.kt`, `Pedido.kt` |
| 5. Igualdad | `equals`/`hashCode`: Cliente (nombre + correo) y Producto (nombre + categoría). `Registro` evita duplicados. | `Cliente.kt`, `Producto.kt`, `Registro.kt` |
| 6. Resumen | Pedidos combinados, cliente desestructurado, promocionables, igualdad, validaciones y análisis de clases (opción 9). | `Demostracion.kt` |

## Archivo de resumen (`resumen_pizzeria.txt`)
Al elegir la **opción 9**, el resumen se muestra en consola y también se guarda como texto en `resumen_pizzeria.txt`.
- **Dónde queda:** en la carpeta desde la que se ejecuta el programa (en IntelliJ, la carpeta del proyecto). El programa muestra la ruta completa: `Resumen guardado en: ...`.
- **Qué contiene:** las mismas secciones de la consola (pedidos combinados, cliente desestructurado, promocionables, igualdad, validaciones y análisis de clases), con fecha y hora de generación.
- **Cada vez que se elige la opción 9** el archivo se **sobrescribe** con la versión nueva. Se guarda en UTF-8.
- **Si no se puede escribir** (carpeta protegida, por ejemplo), se muestra un mensaje de error y el programa sigue funcionando.
- **Código:** `ResumenArchivo.kt` (`textoResumen` arma el texto y `guardarResumen` lo escribe con `File.writeText`), llamado desde `PizzeriaControlador`.

## Estructura
- `anotaciones/`: `Anotaciones.kt`
- `modelo/`: `Usuario`, `Cliente`, `Repartidor`, `Producto`, `Catalogo`, `Pedido`, `Resultados`
- `negocio/`: `OperacionesPizzeria` (interfaz), `PizzeriaBackend`, `Registro`, `Operaciones`, `Inventario`
- `util/`: `Validaciones.kt`, `Reflexion.kt`, `ResumenArchivo.kt`
- `app/`: `Main`, `PizzeriaControlador`, `PizzeriaUI`, `Demostracion`, `Pruebas`

## Errores
El modelo y el backend validan con `require` / `check`, el controlador los captura con `try-catch`. Los datos inválidos se piden de nuevo hasta que sean correctos. Los campos opcionales usan `?.`, `?:` y `let`.