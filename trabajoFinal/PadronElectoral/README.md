# Padron Electoral (Ejercicio 2 - JavaFX)

CRUD en JavaFX para gestionar un padron de votantes: **alta, baja,
consulta y modificacion**, con **busqueda por nombre o por numero de
folio**. Cada registro guarda nombre completo, RFC, seccion electoral
y distrito, tal como pide el enunciado.

Configuracion del proyecto:
- **Group ID:** `pe.edu.upeu`
- **Java 21**
- **JavaFX**

Sigue la misma arquitectura por capas y las mismas librerias que el
`SysVentas` de tu repositorio `POO-trabajos-`: repositorio generico en
memoria (con `ArrayList`, igual que `AbstractJpaRepository`) ->
servicio generico -> controlador FXML, todo conectado con un
`AppContext` (inyeccion manual de dependencias). Todo el codigo vive
directamente dentro del paquete `pe.edu.upeu` (sin subpaquete extra
como `.sysventas` o `.padronelectoral`).

## Requisitos
- JDK 21 (GraalVM 21 o cualquier JDK 21 funciona igual).
- IntelliJ IDEA.
- Conexion a internet la primera vez que abras el proyecto, para que
  Maven descargue JavaFX, Lombok y las dependencias de validacion
  (las mismas que usa SysVentas).
- **Habilitar "Annotation Processing"** en IntelliJ (por Lombok):
  `Settings > Build, Execution, Deployment > Compiler > Annotation Processors`
  y marca "Enable annotation processing". Sin esto, IntelliJ marcara
  en rojo los getters/setters de `Votante` (los genera Lombok en
  tiempo de compilacion).

## Como abrirlo en IntelliJ
1. Descomprime `PadronElectoral.zip`.
2. **File > Open...** y selecciona la carpeta `PadronElectoral`
   (la que contiene `pom.xml`).
3. Espera a que Maven descargue las dependencias e indexe el proyecto
   (barra inferior derecha).
4. Activa "Enable annotation processing" (paso anterior).

## Como ejecutarlo
**Opcion A - con el plugin de Maven (recomendada):**
Panel Maven (lateral derecho) > `PadronElectoral > Plugins > javafx` >
doble clic en `javafx:run`.

**Opcion B - ejecutando la clase directamente:**
Abre `src/main/java/pe/edu/upeu/App.java` > clic derecho > **Run
'App.main()'**.

## Estructura del proyecto
```
PadronElectoral/
├── pom.xml
└── src/main/
    ├── java/
    │   ├── module-info.java
    │   └── pe/edu/upeu/
    │       ├── App.java                    (launcher)
    │       ├── PadronElectoralApp.java      (Application, carga el FXML)
    │       ├── config/
    │       │   └── AppContext.java          (inyeccion de dependencias)
    │       ├── controller/
    │       │   └── VotanteController.java   (alta, baja, consulta, modificar, buscar)
    │       ├── exception/
    │       │   └── ModelNotFoundException.java
    │       ├── model/
    │       │   └── Votante.java             (folio, nombre, RFC, seccion, distrito)
    │       ├── repository/
    │       │   ├── ICrudGenericoRepository.java
    │       │   ├── AbstractJpaRepository.java   (ArrayList en memoria)
    │       │   └── VotanteRepository.java
    │       └── servise/
    │           ├── ICrudGenericoService.java
    │           ├── IVotanteService.java
    │           └── impl/
    │               ├── CrudGenericoServiceImp.java
    │               └── VotanteServiceImp.java
    └── resources/
        ├── css/style.css
        └── view/main_votante.fxml
```

## Funcionalidad
- **Alta**: llena nombre, RFC, seccion electoral y distrito, y
  presiona "Alta". El folio se asigna automaticamente.
- **Consulta**: selecciona un registro en la tabla y presiona
  "Consultar" para ver el detalle. Al seleccionar una fila, los datos
  tambien se cargan en el formulario.
- **Modificacion**: selecciona un registro, edita los campos y
  presiona "Modificar".
- **Baja**: selecciona un registro y presiona "Baja" (pide
  confirmacion).
- **Busqueda**: escribe un nombre (o parte de el) o un folio en la
  barra superior; la tabla se filtra en tiempo real.

Los datos viven en memoria (en un `ArrayList` dentro de
`VotanteRepository`, igual que `ProductoRepository` en SysVentas)
mientras la app esta abierta.
