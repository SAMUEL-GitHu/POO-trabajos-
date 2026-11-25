# SysVentas — Tienda de Ropa (POO 2026-2, Grupo 3, Sesión 12)

Sistema de escritorio (JavaFX) para una tienda de ropa en Juliaca:
productos con **talla y color por variante**, clientes, ventas con
comprobante y descuento de stock en una sola transacción. Claves cifradas
con BCrypt. Base de datos en **Neon (PostgreSQL en la nube)** por
defecto, con **H2 local** como respaldo sin internet (ver `docs/NEON.md`).

## Estructura

```
POO2026-2-G3-dev-Sesion12/
├── db/
│   └── 03_Modelo_Ventas_Ropa_Talla_Color.architect   # Modelo Power Architect (18 tablas)
├── docs/
│   ├── CASO_TIENDA_ROPA.md      # Caso y requerimientos
│   ├── MODELO_BD.md             # Modelo de datos
│   ├── CAMBIOS_SESION12.md      # Qué cambió respecto a la Sesión 11
│   ├── GUIA_EXPOSICION.md       # Guion de la demo y preguntas probables
│   ├── NEON.md                  # Base en Neon y cómo volver a H2 local
│   └── GITHUB.md                # Cómo subir el código y agregar al docente
└── SysVentas/
    ├── pom.xml
    ├── src/main/java/pe/edu/upeu/sysventas/   # Repositorio → Servicio → Controlador
    ├── src/main/resources/
    │   ├── schema_neon.sql        # 18 tablas + datos (Neon, se ejecuta al arrancar)
    │   ├── schema_sv.sql          # Lo mismo para H2 local (respaldo)
    │   ├── application.properties # db.url Neon, db.ddl.script=schema_neon.sql
    │   └── view/                  # FXML (login, productos, ventas)
    └── docs/MANUAL_SYSVENTAS.md
```

## Requisitos

- JDK 21
- Maven 3.9+

## Cómo correrlo

```bash
cd SysVentas
mvn clean javafx:run
```

La primera vez se crean solas las 18 tablas + datos iniciales de ropa
en la base **Neon** (`sysventas12`, hay que crearla antes en Neon y
poner el usuario y la clave en `application.properties`, ver
`docs/NEON.md`). Sin internet, se cambia a H2 local con 3 líneas y la
base se crea sola en `./data/sysventas`. La carpeta `data/` no se sube
a GitHub.

## Usuarios de demo

| Usuario | Clave | Perfil |
|---|---|---|
| admin | admin123 | Root |
| davidmp | 123456 | Administrador |
| raul | 123456 | Administrador |
| pedrito | 123456 | Reporte |

## Qué se puede probar

1. **Productos**: crear un producto y agregarle variantes (talla + color +
   stock). El stock total es la suma de sus variantes.
2. **Ventas**: elegir cliente (DNI 12345678, "Cliente General"), elegir
   producto y su variante, cantidad, agregar al carrito y registrar la
   venta. El stock de la variante baja; si no hay stock, la venta no se
   guarda.
3. **Login fallido**: muestra el error en pantalla; las claves se guardan
   con BCrypt, nunca en texto plano.

Ver el detalle en `docs/CASO_TIENDA_ROPA.md` y la guía de la demo en
`docs/GUIA_EXPOSICION.md`.
