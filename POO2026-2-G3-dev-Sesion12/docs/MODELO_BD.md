# Modelo de base de datos — Tienda de Ropa (Sesión 12)

Archivo del modelo: `db/03_Modelo_Ventas_Ropa_Talla_Color.architect`
(Power Architect). Script espejo para H2:
`SysVentas/src/main/resources/schema_sv.sql` (se ejecuta solo al arrancar).

## Resumen

- **18 tablas, 25 relaciones.** Toda columna `id_*` que es clave foránea
  tiene su relación dibujada en el modelo (en la Sesión 11 había FK sin
  relación: `comp_carrito` → proveedor/producto y `vent_carrito` →
  producto/cliente).
- Todas las claves primarias son **autoincrementales** (`IDENTITY` en H2).
- Dinero: `DECIMAL(10,2)`. Stock y cantidades: `INTEGER` (antes `DOUBLE`:
  no se puede vender 1.5 polos ni cobrar S/ 35.004).
- Claves únicas (`UNIQUE`) donde corresponde: usuario, nombres de
  marca/categoría/talla/color/unidad, código de perfil, RUC del emisor,
  DNI/RUC del proveedor, `(serie, num_doc, tipo_doc)` de venta y compra,
  `(producto, talla, color)` de la variante y
  `(dniruc, id_producto, id_variante)` del carrito de ventas: un cliente
  no puede tener dos líneas del mismo producto con la misma talla y
  color (al agregar de nuevo, se suma la cantidad a la línea que ya
  existe).

## Tablas nuevas (Sesión 12)

| Tabla | Para qué |
|---|---|
| `talla` | Catálogo de tallas: XS, S, M, L, XL, XXL, 28, 30, 32, 34, 36 |
| `color` | Catálogo de colores con su código hexadecimal (`#000000`, …) |
| `producto_variante` | **La tabla clave**: un producto × una talla × un color, con su propio `stock`. UNIQUE `(id_producto, id_talla, id_color)`: no se puede repetir la misma combinación. |

## Por qué variante y no "talla y color en producto"

En la Sesión 11, `producto` tenía dos columnas de texto libre `talla` y
`color`. Eso no funciona para una tienda real:

- Un mismo polo tiene **varias** tallas y colores a la vez (una sola fila
  no alcanza).
- El texto libre permite escribir "M", "m", "Mediana" para lo mismo, y no
  hay forma de saber el stock de cada combinación.
- No se podía validar nada: cualquier texto entraba.

Con `producto_variante`, cada combinación es una fila con su stock, y la
venta descuenta exactamente esa fila.

## Tablas (18)

| Tabla | Clave primaria | Relaciones principales |
|---|---|---|
| `emisor` | `id_emisor` | — (datos de la tienda para el comprobante) |
| `perfil` | `id_perfil` | → `usuario` |
| `proveedor` | `id_proveedor` | → `compra`, `comp_carrito` |
| `unid_medida` | `id_unidad` | → `producto` |
| `usuario` | `id_usuario` | → `perfil`; ← `venta`, `compra`, `vent_carrito`, `comp_carrito` |
| `talla` | `id_talla` | → `producto_variante` |
| `color` | `id_color` | → `producto_variante` |
| `marca` | `id_marca` | → `producto` |
| `categoria` | `id_categoria` | → `producto` |
| `cliente` | `dniruc` | ← `venta` |
| `producto` | `id_producto` | → `categoria`, `marca`, `unid_medida`; ← `producto_variante`, `venta_detalle`, `compra_detalle`, `vent_carrito`, `comp_carrito` |
| `producto_variante` | `id_variante` | → `producto`, `talla`, `color`; ← `venta_detalle`, `compra_detalle`, `vent_carrito`, `comp_carrito` |
| `venta` | `id_venta` | → `cliente`, `usuario`; ← `venta_detalle` |
| `venta_detalle` | `id_venta_detalle` | → `venta`, `producto`, `producto_variante` |
| `vent_carrito` | `id_carrito` | → `producto`, `producto_variante`, `usuario` |
| `compra` | `id_compra` | → `proveedor`, `usuario`; ← `compra_detalle` |
| `compra_detalle` | `id_compra_detalle` | → `compra`, `producto`, `producto_variante` |
| `comp_carrito` | `id_compcarrito` | → `proveedor`, `producto`, `producto_variante`, `usuario` |

## Correcciones aplicadas al modelo original

1. `usuario.clave` pasó de `VARCHAR(60)` a `VARCHAR(255)`: un hash BCrypt
   mide 60 caracteres, pero 255 deja margen y es lo correcto para hashes.
2. `perfil.nombre` (60) y `perfil.codigo` (20) estaban con los tamaños
   al revés; `marca.nombre` y `categoria.nombre` pasaron de 20 a 60.
3. `cliente.rep_legal` ahora permite `NULL`: una persona natural (DNI) no
   tiene representante legal.
4. `producto` perdió las columnas `talla` y `color` (ahora viven en
   `producto_variante`).
5. `emisor` ya no está huérfana: es la tabla que alimenta los datos del
   comprobante (RUC 10436319172, SysCenterLife S.A.C., Juliaca).

## Generar las tablas desde Power Architect

1. Abrir `db/03_Modelo_Ventas_Ropa_Talla_Color.architect` en Power Architect.
2. *DDL → Forward Engineer…* → elegir el motor (el modelo trae el
   generador de PostgreSQL; para H2 el script ya está en `schema_sv.sql`).
3. El script `schema_sv.sql` es el espejo exacto del modelo para H2 y es
   el que ejecuta la app: si se cambia el modelo, hay que cambiar el
   script (o al revés) para que no se desincronicen.

## Datos iniciales

`schema_sv.sql` trae datos de una tienda de ropa (ya no de electrónica):

- Categorías: Polos, Pantalones, Casacas, Camisas, Vestidos.
- Marcas: Andina, Urbano, Killa, Polo Perú.
- Tallas XS–XXL y 28–36; colores con su hexadecimal.
- 6 productos con 17 variantes; el `stock` de cada producto es la suma
  del stock de sus variantes (por ejemplo, Polo Clásico Algodón: 8 + 5 + 6
  = 19).
- Emisor: SysCenterLife S.A.C., RUC 10436319172, Av. La Torre – Juliaca.
- Cliente de mostrador: DNI 12345678, "Cliente General".
- Usuarios con clave cifrada (BCrypt): admin, davidmp, raul, pedrito.
