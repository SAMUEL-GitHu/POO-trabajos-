# Subir el código a GitHub

## 1. Crear el repositorio

1. En GitHub → **New repository**.
2. Nombre sugerido: `POO2026-2-G3-dev-Sesion12` (o `SysVentas-Tienda-Ropa`).
3. Público o privado, como prefiera el equipo. Si es privado, el paso 3
   es obligatorio.
4. **No** marcar "Add a README" (el proyecto ya trae uno).

## 2. Subir el código (desde la carpeta del proyecto)

```bash
cd POO2026-2-G3-dev-Sesion12
git init
git add .
git commit -m "Sesión 12: tienda de ropa - variantes talla/color, BCrypt, venta transaccional"
git branch -M main
git remote add origin https://github.com/<usuario>/<repo>.git
git push -u origin main
```

## 3. Agregar al docente

1. En el repositorio → **Settings → Collaborators → Add people**.
2. Buscar **`dmamanipar`** y enviar la invitación.
3. Si el repositorio es **privado**, sin este paso el docente no puede ver
   el código y la rúbrica 0 no se cumple.

## 4. Qué se sube y qué no

- **Sí**: todo el código (`SysVentas/`), el modelo
  (`db/03_Modelo_Ventas_Ropa_Talla_Color.architect`), los `docs/` y el
  `README.md`.
- **No**: la carpeta `data/` (la base H2 local). Está en `.gitignore` en
  la raíz y en `SysVentas/`; se regenera sola al primer arranque con
  `schema_sv.sql`.
- **No**: `target/` (compilados) ni `.idea/` (configuración del IDE).
