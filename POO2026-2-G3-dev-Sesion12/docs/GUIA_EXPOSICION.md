# Guía de exposición — Sesión 12 (Tienda de Ropa)

## Antes de exponer

1. Borrar la carpeta `data/` dentro de `SysVentas/` (si existe) para
   demostrar que la base se crea sola.
2. Abrir el proyecto en IntelliJ IDEA y compilar: `mvn clean compile`
   (o el botón de Maven).
3. Ejecutar: `mvn clean javafx:run` desde la carpeta `SysVentas/`.
   Requiere JDK 21.
4. Tener abierto Power Architect con
   `db/03_Modelo_Ventas_Ropa_Talla_Color.architect` para mostrar el modelo.

## Guion de la demo (5–7 minutos)

### 1. Login y seguridad (1 min)

- Entrar con **admin / admin123**.
- Mostrar que la clave **no está en texto plano**: abrir
  `schema_sv.sql` y señalar el hash `$2a$10$...` del usuario admin.
- Probar un login fallido (clave incorrecta) y mostrar el `Alert`
  "Usuario o clave incorrectos" (rúbrica 5: validar y avisar).
- Decir: *"La investigación adicional es el cifrado de claves con
  BCrypt, que es el ejemplo que pone la misma rúbrica. Las claves viejas
  en texto plano se migran solas al primer login."*

### 2. Modelo de datos (1 min)

- Mostrar el `.architect`: **18 tablas, 25 relaciones**.
- Señalar las 3 tablas nuevas: `talla`, `color`, `producto_variante`.
- Explicar por qué: *"Antes la talla y el color eran dos columnas de
  texto en producto. Un polo tiene varias tallas y colores a la vez, y
  el stock es por combinación. Por eso la variante es su propia tabla
  con su propio stock y UNIQUE(producto, talla, color)."*
- Mostrar que el script `schema_sv.sql` es el espejo del modelo y que
  la app lo ejecuta sola al arrancar (antes apuntaba a un script que no
  creaba tablas y la app fallaba en una base nueva).

### 3. Productos y variantes (1,5 min)

- Menú → Gestión Productos. Mostrar la tabla con los 6 productos de ropa.
- Abrir "Polo Clásico Algodón": el stock total (19) es solo lectura.
- En la sección Variantes, agregar una variante nueva (por ejemplo,
  talla S, color Rojo, stock 4): el stock total sube a 23.
- Volver a agregar la misma combinación (S-Rojo, stock 2): **no se
  duplica, suma** (queda 6) y el total sube a 25.
- Decir: *"Cada integrante tiene su módulo: productos, ventas y
  clientes."*

### 4. Venta con talla y color (2 min)

- Menú → Ventas. Cliente: escribir `12345678` y Buscar (o elegir
  "Cliente General" del autocompletado).
- Producto: escribir "Polo", elegir "Polo Clásico Algodón".
- **Elegir la variante** en el desplegable (por ejemplo, M - Negro,
  stock 8): el campo Stock muestra 8, no 19.
- Cantidad 3 → Add. La tabla del carrito muestra la columna
  "Talla / Color" con "M - Negro (stock 8)".
- **Probar que el carrito suma**: agregar 2 más del mismo producto y la
  misma variante. **No aparece una segunda línea**: la línea pasa a
  cantidad 5 con su total actualizado, y el mensaje dice "Cantidad
  actualizada: ahora son 5...".
- Probar cantidad 99: el botón Add se deshabilita (no deja vender más
  que el stock).
- Registrar Venta → se muestra el comprobante.
- Fíjate en el comprobante: la serie y el número son los de la venta
  (V - 00000001), la dirección es la del cliente, el detalle dice
  "Polo Clásico Algodón - M - Negro" con cantidad 3 y P. UNIT S/ 35.00,
  y el "SON:" va en letras.
- Volver a Productos y abrir el polo: la variante M-Negro ahora tiene
  stock 5 y el total bajó de 25 a 22. **La venta descontó stock.**
- Decir: *"Venta, detalles y descuento de stock van en una sola
  transacción: si algo falla, no queda nada guardado a medias."*

### 5. Cierre (30 s)

- Mostrar la estructura en capas: Repositorio → Servicio → Controlador,
  y el `docs/` con el caso, el modelo y esta guía.
- Cerrar con: *"El código está en GitHub y el modelo en Power Architect,
  como pide la evaluación."*

## Preguntas probables y respuestas

**¿Por qué BCrypt y no MD5 o SHA-256?**
BCrypt es lento a propósito (costo 10 = 2^10 rondas) y lleva la sal
incorporada en el propio hash, así que dos claves iguales no generan el
mismo hash y no sirve una tabla precalculada (rainbow table). MD5 y
SHA-256 son rápidos: eso es justo lo que no se quiere para claves.

**¿Por qué no pusieron la talla y el color como columnas de producto?**
Porque un producto tiene muchas combinaciones y cada una tiene su propio
stock. Con columnas de texto solo cabría una combinación por producto y
no se podría controlar el stock real de la tienda.

**¿Qué pasa si dos personas venden la última prenda a la vez?**
El descuento usa `UPDATE ... WHERE stock >= ?`: si el stock ya no
alcanza, la actualización afecta 0 filas y la venta se cancela con
rollback. Además la venta valida el stock antes de descontar.

**¿Por qué el stock total no se escribe a mano?**
Porque es un dato calculado (suma de variantes). Si se escribiera a mano,
se desincronizaría con las variantes. Por eso el campo es solo lectura
y se recalcula solo.

**¿Qué falta?**
El módulo de Compras no tiene pantalla (las tablas existen en el modelo
y en la base). Es el trabajo futuro inmediato; no se inventó una pantalla
a última hora para no arriesgar la exposición.

**¿Dónde está el código?**
En GitHub (ver `docs/GITHUB.md`); el docente `dmamanipar` es
colaborador del repositorio. El modelo está en
`db/03_Modelo_Ventas_Ropa_Talla_Color.architect`.
