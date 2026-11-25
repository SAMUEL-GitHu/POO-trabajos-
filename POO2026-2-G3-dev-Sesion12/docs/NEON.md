# Base de datos en Neon (PostgreSQL en la nube)

La app puede usar dos motores (se elige en
`SysVentas/src/main/resources/application.properties`):

| Motor | URL | Cuándo usarla |
|---|---|---|
| **Neon** (activa) | `jdbc:postgresql://...` | La base está en la nube, se ve desde cualquier PC y en DBeaver. Necesita internet. |
| **H2 local** (respaldo) | `jdbc:h2:file:./data/sysventas` | Sin internet. La base es el archivo `SysVentas/data/sysventas.mv.db`. |

El código es el mismo para las dos: el driver se elige solo por el
prefijo de la URL (`DatabaseConfig`) y el SQL de la app es JDBC
estándar. Lo único distinto es el script inicial:

- H2: `schema_sv.sql` (usa `MERGE ... KEY`, propio de H2).
- Neon: `schema_neon.sql` (mismas 18 tablas y mismos datos iniciales,
  con `INSERT ... ON CONFLICT DO NOTHING` y un ajuste de secuencias
  con `setval`, porque en PostgreSQL insertar ids fijos no adelanta
  la secuencia y el primer registro creado por la app chocaría con
  el id 1).

## 1. Crear la base de datos en Neon (una sola vez)

**Si ya tienes el proyecto** (el de la Sesión 11, endpoint
`ep-restless-hall...`):

1. Entra a [Neon](https://neon.tech) y abre tu proyecto.
2. Crea una base de datos **nueva y vacía** llamada **`sysventas12`**.
   No uses la vieja `sysventas`: tiene las tablas de la Sesión 11
   (sin variantes de talla/color) y la app fallaría porque le
   faltarían columnas.
3. En el dashboard, en **Connection Details**, copia el **usuario**
   (role, ej. `neondb_owner`) y la **contraseña**. La contraseña no
   está en el proyecto: solo existe en Neon.

**Si no tienes el proyecto, crea uno nuevo:**

1. En [Neon](https://neon.tech), ve a **Projects → New Project**.
2. Nombre: `tienda-ropa` (o el que quieras). Región: **US East
   (N. Virginia u Ohio)**, la más cercana. Deja lo demás por defecto.
3. Ahí mismo pon el nombre de la base de datos: **`sysventas12`**, y
   **copia la contraseña** que te muestra (solo se ve una vez).
4. Al terminar, en el dashboard dale a **Connect** y copia el **host**,
   el **usuario** y la **contraseña**. Ojo: el host será **nuevo**,
   distinto del viejo `ep-restless-hall...`, así que también hay que
   cambiar la `db.url` en `application.properties` por la nueva (ver
   el paso 2). Si Neon no te deja crear otro proyecto, usa uno de los
   que ya tienes (`prueba_G3` o `crimson-darkness-68724061`) y solo
   crea la base `sysventas12` dentro de ese proyecto.

## 2. Poner las credenciales en la app

En `SysVentas/src/main/resources/application.properties` reemplaza:

```properties
db.url=jdbc:postgresql://<host-nuevo>:5432/sysventas12?sslmode=require
db.username=USUARIO_NEON
db.password=CLAVE_NEON
```

por tu host, tu usuario y tu clave de Neon. El host sale del botón
**Connect** del dashboard (si creaste un proyecto nuevo, el host es
nuevo: no uses el viejo `ep-restless-hall...`). No subas este archivo
con la clave a un repo público; el repo del curso es privado, pero
igual no compartas la clave con nadie.

## 3. Correr la app

```bash
cd SysVentas
mvn clean javafx:run
```

En el primer arranque el script `schema_neon.sql` crea las 18 tablas
y los datos iniciales (6 productos, 17 variantes, 4 usuarios). Los
usuarios de prueba son los mismos: `admin/admin123`.

## 4. Ver la base en DBeaver

1. Cierra la app (no hace falta para Neon, pero así no hay dudas).
2. **Database → New Database Connection → PostgreSQL.**
3. Host: `ep-restless-hall-aehwldwn-pooler.c-2.us-east-2.aws.neon.tech`,
   puerto `5432`, base de datos `sysventas12`, SSL en **require**.
4. Usuario y contraseña: los mismos de Neon.
5. Expande la conexión → **Schemas → public → Tables**: las 18 tablas.

## Volver a H2 local

En `application.properties` comenta las 3 líneas de Neon y
descomenta las 3 de H2 (están al final del archivo, marcadas). El
script vuelve a ser `schema_sv.sql` y la base se crea sola en
`SysVentas/data/` al arrancar, como antes.

## Ojo para la exposición

Con Neon la app **necesita internet** para abrir. Si en el aula no hay
internet seguro, usa H2 local ese día: cambia las 3 líneas y corre
la app, no hay que recompilar nada.
