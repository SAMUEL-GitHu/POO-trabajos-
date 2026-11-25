# Cambios — Sesión 12 (Tienda de Ropa)

Comparado con la Sesión 11 (`POO2026-2-G3-dev-Sesion11`). Todo lo que ya
existía se conserva y se mejora; lo único nuevo de fondo es el cifrado de
claves (investigación adicional de la rúbrica 6) y las tablas de
talla/color/variante, que son el tema de la tienda.

## Modelo de datos (`.architect` + `schema_sv.sql`)

- **3 tablas nuevas**: `talla`, `color`, `producto_variante`
  (producto × talla × color con stock propio).
- **25 relaciones** (antes 14): ahora toda FK tiene su relación dibujada.
- Las 18 tablas van **antes** del bloque de relaciones en el archivo:
  Power Architect lee en orden y la primera versión ponía las tablas
  nuevas después, así que al abrirlo fallaba con
  "Null fkTable not allowed".
- `producto` perdió las columnas de texto `talla` y `color`.
- Claves primarias autoincrementales; dinero `DECIMAL(10,2)`; stock y
  cantidades `INTEGER` (antes `DOUBLE`).
- `UNIQUE` en: usuario, marca/categoría/talla/color/unidad,
  código de perfil, RUC del emisor, DNI/RUC del proveedor,
  `(serie, num_doc, tipo_doc)` de venta y compra, y
  `(producto, talla, color)` de la variante.
- `usuario.clave` `VARCHAR(60)` → `VARCHAR(255)` (para el hash BCrypt).
- `cliente.rep_legal` ahora permite `NULL`.
- Tamaños corregidos: `perfil.nombre` 60 / `perfil.codigo` 20 (estaban al
  revés), `marca.nombre` y `categoria.nombre` 20 → 60.
- Datos iniciales de **ropa** (antes: categoría "Televisor", marca "LG"):
  5 categorías, 4 marcas, 11 tallas, 7 colores, 6 productos con 17
  variantes cuyo stock suma el stock del producto.

## Base de datos al arrancar (error grave corregido)

- `application.properties` apuntaba a `scripts.sql`, que **solo insertaba
  usuarios y no creaba ninguna tabla**: en una base nueva la app fallaba
  al arrancar. Ahora apunta a `schema_sv.sql` (18 tablas + datos) y
  `scripts.sql` se eliminó.
- El zip ya **no incluye la carpeta `data/`** (la base H2 se regenera sola
  al primer arranque) y `.gitignore` la excluye para no subirla a GitHub.

## Seguridad — cifrado de claves (rúbrica 6)

- Nueva dependencia `org.mindrot:jbcrypt:0.4` y clase
  `utils/PasswordUtil` (BCrypt, costo 10).
- `UsuarioServiceImp.login()`: busca por usuario y verifica con
  `BCrypt.checkpw`. Ya no existe `findByUsuarioAndClave` (la clave ya no
  se compara en SQL).
- Las claves de los datos iniciales son hashes BCrypt (admin/admin123,
  davidmp/raul/pedrito con 123456).
- **Migración automática**: si un usuario viejo tiene la clave en texto
  plano y entra con la clave correcta, se le guarda el hash en ese
  momento. Nadie se queda fuera por el cambio.
- `module-info.java`: jBCrypt 0.4 no trae nombre de módulo, así que el
  `requires` es `jbcrypt` (nombre automático del archivo) y no
  `org.mindrot.jbcrypt`, que no compila.

## Base de datos: H2 local y Neon (PostgreSQL)

La app soporta dos motores, se elige en `application.properties`
(ver `docs/NEON.md`):

- **Neon (activa)**: PostgreSQL en la nube. Driver `org.postgresql`
  42.7.4, `DatabaseConfig` lo elige por el prefijo `jdbc:postgresql:`.
  Script `schema_neon.sql`: mismas 18 tablas y datos que H2, con
  `MERGE` convertido a `INSERT ... ON CONFLICT DO NOTHING` y `setval`
  para dejar las secuencias en el máximo id (en PostgreSQL insertar
  ids fijos no adelanta la secuencia). La base debe ser una **nueva**
  y vacía (`sysventas12`): la vieja `sysventas` tiene el modelo de la
  Sesión 11 y la app fallaría.
- **H2 local (respaldo)**: sin internet, archivo
  `SysVentas/data/sysventas.mv.db`, script `schema_sv.sql`. Para
  volver, se comentan las líneas de Neon y se descomentan las de H2.

## Ventas (el arreglo más importante)

- **La venta ahora descuenta stock.** Antes nunca lo hacía.
- **El carrito suma cantidades.** Si el cliente ya tiene ese producto
  con esa talla y color en el carrito, al agregarlo de nuevo **no se
  crea otra línea: se suma a la que ya existe** (1 + 2 = una línea con
  3). Lo hace `VentaCarritoServiceImp.agregarAlCarrito()` y la base lo
  respalda con el `UNIQUE(dniruc, id_producto, id_variante)` de
  `vent_carrito`. El botón "Add" ya no deja pedir más de lo que queda
  (stock de la variante menos lo que ya está en el carrito) y el mensaje
  avisa cuántos se pueden agregar. El precio queda fijado al de la
  primera vez que se agregó la línea.
- **El comprobante ya es de verdad.** Antes serie, número y dirección
  del cliente eran fijos de ejemplo ("B001 - 00000042", "Av. Sangalli
  1234"). Ahora la serie y el número son el correlativo real de la
  venta, la dirección es la del cliente, cada item lleva su talla y
  color ("Polo Clásico Algodón - L - Negro"), el P. UNIT es el precio
  real y el "SON:" va en letras ("SETENTA CON 00/100 SOLES"). Además se
  corrigió que el P. UNIT se armaba con el subtotal: con cantidad 2 el
  P. UNIT salía 70.00 y el SUBTOTAL 140.00. La cantidad ya no se muestra
  como "2.0" sino "2".
- La columna "Talla / Color" del carrito mostraba "(stock 0)" porque el
  repositorio no traía el stock de la variante; ahora trae el real.
- **Una sola transacción**: venta + detalles + descuento de stock +
  recálculo del stock total, en una conexión. Si falta stock, rollback:
  no queda ninguna venta a medias.
- **Número de documento correlativo** (`00000001`, `00000002`, …) por
  serie y tipo. Antes era fijo `"00"` en todas las ventas, y con el
  `UNIQUE(serie, num_doc, tipo_doc)` la segunda venta habría fallado.
- **Variante obligatoria**: al elegir un producto se cargan sus tallas y
  colores; el stock que se muestra y se valida es el de la variante, no
  el total del producto. Sin variantes, no se puede agregar al carrito.
- `VentaController`: cantidad entera (antes `double`), validación de
  cantidad ≤ stock, botón "Add" deshabilitado si no hay stock, `Toast`
  de confirmación al agregar y al vender, y si la venta falla el carrito
  **no se vacía**.
- `Venta.calcularTotal()` corregido: antes sumaba el IGV sobre un total
  que ya lo incluía.
- `guardarCliente()`: valida DNI 8 / RUC 11 dígitos, pone el tipo de
  documento según la longitud, y `repLegal` solo si es RUC (antes copiaba
  la razón social siempre y el tipo siempre era DNI).
- `VentCarritoRepository` reescrito: trae talla/color de la variante con
  JOINs, ya no pierde `tipo_producto` al leer, y su `update` actualiza
  todos los campos.
- `VentaDetalle` / `CompraDetalle` / `VentCarrito` / `CompCarrito`:
  cantidad `Integer` y campo `idVariante`.

## Productos

- **Sección de variantes** en el formulario: talla + color + stock, con
  tabla de variantes del producto. Si la combinación ya existe, suma el
  stock en vez de duplicarla.
- `producto.stock` es solo lectura: es la suma de sus variantes y se
  recalcula solo.
- Validación real: texto no numérico ya no se convierte en `0.0` en
  silencio (`parseDoubleSafe` devuelve `null` y avisa); el formulario
  muestra errores con `Toast` y marca los campos.
- `editForm`: comparación de `String` con `==` → `.equals(...)`.
- Borrar con clave foránea en uso ya no rompe la app: se atrapa la
  excepción y se avisa con `Toast`.
- Botón Cancelar conectado a `clearForm()` (antes no hacía nada).
- Se quitó el campo "Stock Old" del formulario (`stockold` sigue en la
  tabla por compatibilidad, pero ya no se edita a mano).
- `ClienteRepository.updateRow()` ahora sí actualiza la dirección.
- `Compra` reescrita con `@Data @SuperBuilder` (antes `@Builder` sin
  `@SuperBuilder` no heredaba los campos de `DocumentoComercial` y no
  tenía getters).

## Login y menú

- Login fallido **muestra un `Alert`** ("Usuario o clave incorrectos") y
  valida campos vacíos. Antes fallaba en silencio (solo un
  `System.out.println`).
- Menú: se quitó "Reg. Cliente" porque apuntaba a
  `main_cliente.fxml`, **una vista que no existe**: al hacer clic, la app
  se rompía. El cliente se registra dentro de la pantalla de Ventas.
  El perfil Reporte ahora ve Ventas en vez de esa entrada rota.

## Documentación

- `docs/CASO_TIENDA_ROPA.md` — caso y requerimientos (rúbrica 0).
- `docs/MODELO_BD.md` — modelo de 18 tablas y cómo generar las tablas.
- `docs/GUIA_EXPOSICION.md` — guion de la demo y preguntas probables.
- `docs/GITHUB.md` — cómo subir el código y agregar al docente.
- `README.md` reescrito.
- `SysVentas/docs/MANUAL_SYSVENTAS.md`: corregida la afirmación de que
  "no hay base de datos real" (la persistencia es H2 por JDBC desde
  antes) y el Anexo C (persistencia, IDs y `==` ya corregidos).

## Limitaciones conocidas (no se ocultan)

1. **Compras no tiene pantalla.** Las tablas existen, pero no hay
   controlador ni vista para registrar compras. Es trabajo futuro.
2. La venta guarda `serie "V"`, `tipoDoc "Factura"` y el correlativo lo
   genera el servicio. Los datos del emisor del comprobante (RUC,
   razón social, dirección, teléfono) siguen fijos en
   `generarComprobante`, pero coinciden con el emisor guardado en la
   base (`emisor`, RUC 10436319172).
3. El precio es del producto, no de la variante: todas las tallas y
   colores de una prenda cuestan lo mismo.
4. No hay paginación de tablas (se descartó a propósito: una sola
   investigación adicional, el cifrado, para no arriesgar la exposición).
