# Caso — Tienda de Ropa "SysVentas" (Sesión 12, Evaluación Unidad 2)

## Contexto

Una tienda de ropa en Juliaca vende prendas al público (polos, pantalones,
casacas, camisas, vestidos). Cada prenda se vende en **talla y color**
concretos (por ejemplo: Polo Clásico, talla M, color Negro), y el stock se
controla **por variante**: no basta saber que quedan 19 polos, hay que saber
cuántos quedan en M-Negro, cuántos en L-Negro y cuántos en M-Blanco.

El sistema registra productos, clientes y ventas con comprobante, y descuenta
el stock al vender. Lo usa el personal de la tienda desde una aplicación de
escritorio (JavaFX) con base de datos local H2.

## Actores y perfiles

| Perfil | Qué puede hacer |
|---|---|
| **Root** | Todo: productos, ventas, clientes. |
| **Administrador** | Productos y ventas. |
| **Reporte** | Ventas (donde también se registra al cliente). |

Usuarios de demo (claves en `schema_sv.sql`, cifradas con BCrypt):

| Usuario | Clave | Perfil |
|---|---|---|
| admin | admin123 | Root |
| davidmp | 123456 | Administrador |
| raul | 123456 | Administrador |
| pedrito | 123456 | Reporte |

## Requerimientos funcionales

| ID | Requerimiento | Dónde está |
|---|---|---|
| RF01 | CRUD de productos (nombre, precio, utilidad, marca, categoría, unidad de medida, tipo) | `ProductoController` + `main_producto.fxml` |
| RF02 | Registrar tallas y colores de un producto, cada variante con su propio stock; el stock total del producto es la suma de sus variantes | `ProductoController` (sección Variantes) + `producto_variante` |
| RF03 | Buscar productos por nombre con autocompletado y elegir su talla/color al vender | `VentaController` + `main_venta.fxml` |
| RF04 | Registrar clientes por DNI (8 dígitos) o RUC (11 dígitos); el representante legal solo aplica a empresas (RUC) | `VentaController.guardarCliente()` |
| RF05 | Carrito de venta: agregar productos con cantidad, precio y total | `VentaController` + `vent_carrito` |
| RF06 | Registrar la venta con base imponible, IGV (18 %) e importe total, y generar su comprobante | `VentaController.registrarVenta()` |
| RF07 | La venta descuenta el stock de la variante vendida y actualiza el stock total; si no hay stock, **no se guarda nada** | `VentaServiceImp.registrarVentaConDetalles()` (una transacción) |
| RF08 | No permitir vender más cantidad que el stock de la variante elegida | `VentaController.calcularPT()` + validación en `registarPCarrito()` |
| RF09 | Login con usuario y clave; si la clave es incorrecta, mostrar el error en pantalla | `LoginController` + `UsuarioServiceImp` |
| RF10 | Cada integrante del equipo tiene su CRUD / funcionalidad mayor (productos, ventas, clientes) | Ver `docs/CAMBIOS_SESION12.md` |
| RF11 | Código fuente en GitHub (repo público o privado con el docente `dmamanipar` como colaborador) | Ver `docs/GITHUB.md` |
| RF12 | Modelo de base de datos en Power Architect y tablas generadas a partir del modelo | `db/03_Modelo_Ventas_Ropa_Talla_Color.architect` + `schema_sv.sql` |

## Requerimientos no funcionales

| ID | Requerimiento | Cómo se cumple |
|---|---|---|
| RNF01 | **Seguridad (investigación adicional, rúbrica 6):** las claves no se guardan en texto plano | BCrypt, costo 10 (`utils/PasswordUtil`). Las claves viejas en claro se migran solas al primer login correcto. |
| RNF02 | **Validación (rúbrica 5):** todo campo inválido avisa con un mensaje en pantalla, nunca rompe la app | Validaciones en `ProductoController` y `VentaController` (números, DNI/RUC, stock, campos vacíos) + `Toast`/`Alert` |
| RNF03 | **Integridad:** una venta a medias no debe quedar guardada | Una sola transacción JDBC para venta + detalles + descuento de stock, con rollback |
| RNF04 | **Dinero exacto:** los montos no usan `double` en base de datos | `DECIMAL(10,2)` en BD; `INTEGER` para stock y cantidades |
| RNF05 | **Arquitectura en capas** (rúbrica 1) | Repositorio → Servicio → Controlador, con interfaces; `AppContext` arma el grafo por constructor |
| RNF06 | **Datos que sobreviven al cierre** | Base H2 en archivo (`./data/sysventas`), ignorada en Git; se crea sola al primer arranque |

## Reglas de negocio

1. Un producto **no se vende sin variante**: si no tiene tallas/colores
   registrados, la venta avisa y no deja agregarlo al carrito.
2. El stock de una variante nunca baja de cero: el descuento usa
   `UPDATE ... SET stock = stock - ? WHERE id_variante = ? AND stock >= ?`.
3. El número de documento de la venta es correlativo por serie y tipo
   (`00000001`, `00000002`, …); antes era fijo `"00"` en todas las ventas.
4. El precio es del **producto**, no de la variante: todas las tallas y
   colores de una prenda cuestan lo mismo.
5. `producto.stock` es solo lectura en el formulario: se recalcula como la
   suma de sus variantes cada vez que una cambia.

## Fuera de alcance (limitaciones conocidas)

- **Compras no tiene pantalla**: las tablas `compra`, `compra_detalle` y
  `comp_carrito` existen en el modelo y en la BD (se conservan del proyecto
  original), pero no hay controlador ni vista para registrar compras. Se
  documenta como trabajo futuro, no se inventó una pantalla a última hora.
- No hay paginación de tablas (se evaluó y se descartó: solo se hizo **una**
  investigación adicional, el cifrado de claves, para no arriesgar la
  exposición).
- No hay control de acceso por botón dentro de una misma pantalla: el
  permiso es por menú según el perfil.
