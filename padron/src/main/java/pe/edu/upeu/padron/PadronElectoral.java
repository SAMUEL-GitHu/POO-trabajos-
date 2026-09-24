package pe.edu.upeu.padron;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class PadronElectoral extends javafx.application.Application {
    private final ObservableList<Padron> registros = FXCollections.observableArrayList();
    private FilteredList<Padron> registrosFiltrados;

    private final TextField txtFolio = new TextField();
    private final TextField txtNombre = new TextField();
    private final TextField txtRfc = new TextField();
    private final TextField txtSeccion = new TextField();
    private final TextField txtDistrito = new TextField();
    private final TextField txtBuscar = new TextField();
    private final TableView<Padron> tabla = new TableView<>();

    @Override
    public void start(Stage stage) {
        cargarDatosEjemplo();
        configurarTabla();
        registrosFiltrados = new FilteredList<>(registros, p -> true);
        tabla.setItems(registrosFiltrados);

        Label titulo = new Label("Padrón Electoral");
        titulo.setStyle("-fx-font-size: 26px; -fx-font-weight: bold;");
        Label subtitulo = new Label("Gestión de registros de votantes");
        subtitulo.setStyle("-fx-font-size: 14px;");

        VBox encabezado = new VBox(4, titulo, subtitulo);
        encabezado.setPadding(new Insets(0, 0, 10, 0));

        GridPane formulario = crearFormulario();
        HBox botones = crearBotones();

        VBox izquierda = new VBox(12, encabezado, formulario, botones);
        izquierda.setPadding(new Insets(15));
        izquierda.setPrefWidth(390);
        izquierda.setStyle("-fx-background-color: #f4f4f4; -fx-border-color: #dddddd; -fx-border-radius: 6;");

        txtBuscar.setPromptText("Buscar por folio, nombre o RFC...");
        txtBuscar.textProperty().addListener((obs, anterior, nuevo) -> filtrar(nuevo));
        Button limpiarBusqueda = new Button("Limpiar");
        limpiarBusqueda.setOnAction(e -> txtBuscar.clear());
        HBox busqueda = new HBox(8, txtBuscar, limpiarBusqueda);
        HBox.setHgrow(txtBuscar, Priority.ALWAYS);

        VBox derecha = new VBox(10, new Label("Consulta de registros"), busqueda, tabla);
        derecha.setPadding(new Insets(15));
        VBox.setVgrow(tabla, Priority.ALWAYS);
        HBox.setHgrow(derecha, Priority.ALWAYS);

        HBox contenido = new HBox(12, izquierda, derecha);
        contenido.setPadding(new Insets(15));
        HBox.setHgrow(derecha, Priority.ALWAYS);
        VBox.setVgrow(contenido, Priority.ALWAYS);

        BorderPane root = new BorderPane();
        root.setCenter(contenido);
        root.setStyle("-fx-background-color: white;");

        Scene scene = new Scene(root, 1050, 650);
        stage.setTitle("Padrón Electoral - UPeU");
        stage.setScene(scene);
        stage.show();
    }

    private GridPane crearFormulario() {
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(10);

        txtFolio.setPromptText("Ej. 1001");
        txtNombre.setPromptText("Nombre completo");
        txtRfc.setPromptText("RFC");
        txtSeccion.setPromptText("Sección electoral");
        txtDistrito.setPromptText("Distrito");

        agregarCampo(grid, 0, "Folio:", txtFolio);
        agregarCampo(grid, 1, "Nombre completo:", txtNombre);
        agregarCampo(grid, 2, "RFC:", txtRfc);
        agregarCampo(grid, 3, "Sección electoral:", txtSeccion);
        agregarCampo(grid, 4, "Distrito:", txtDistrito);
        return grid;
    }

    private void agregarCampo(GridPane grid, int fila, String etiqueta, TextField campo) {
        Label label = new Label(etiqueta);
        grid.add(label, 0, fila);
        grid.add(campo, 1, fila);
        GridPane.setHgrow(campo, Priority.ALWAYS);
        campo.setMaxWidth(Double.MAX_VALUE);
    }

    private HBox crearBotones() {
        Button btnAlta = new Button("Dar de alta");
        Button btnModificar = new Button("Modificar");
        Button btnBaja = new Button("Dar de baja");
        Button btnLimpiar = new Button("Limpiar formulario");

        btnAlta.setOnAction(e -> alta());
        btnModificar.setOnAction(e -> modificar());
        btnBaja.setOnAction(e -> baja());
        btnLimpiar.setOnAction(e -> limpiarFormulario());

        HBox botones = new HBox(8, btnAlta, btnModificar, btnBaja, btnLimpiar);
        botones.setAlignment(Pos.CENTER_LEFT);
        return botones;
    }

    private void configurarTabla() {
        TableColumn<Padron, Integer> colFolio = new TableColumn<>("Folio");
        colFolio.setCellValueFactory(new PropertyValueFactory<>("folio"));

        TableColumn<Padron, String> colNombre = new TableColumn<>("Nombre completo");
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));

        TableColumn<Padron, String> colRfc = new TableColumn<>("RFC");
        colRfc.setCellValueFactory(new PropertyValueFactory<>("rfc"));

        TableColumn<Padron, String> colSeccion = new TableColumn<>("Sección");
        colSeccion.setCellValueFactory(new PropertyValueFactory<>("seccionElectoral"));

        TableColumn<Padron, String> colDistrito = new TableColumn<>("Distrito");
        colDistrito.setCellValueFactory(new PropertyValueFactory<>("distrito"));

        tabla.getColumns().addAll(colFolio, colNombre, colRfc, colSeccion, colDistrito);
        tabla.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        tabla.getSelectionModel().selectedItemProperty().addListener((obs, anterior, seleccionado) -> {
            if (seleccionado != null) cargarFormulario(seleccionado);
        });
    }

    private void alta() {
        try {
            int folio = Integer.parseInt(txtFolio.getText().trim());
            if (buscarPorFolio(folio) != null) {
                mostrarAlerta(Alert.AlertType.WARNING, "El folio ya existe.");
                return;
            }
            if (camposTextoVacios()) return;
            registros.add(new Padron(folio, txtNombre.getText().trim(), txtRfc.getText().trim(),
                    txtSeccion.getText().trim(), txtDistrito.getText().trim()));
            limpiarFormulario();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Registro agregado correctamente.");
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "El folio debe ser un número entero.");
        }
    }

    private void modificar() {
        Padron seleccionado = tabla.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Seleccione un registro para modificar.");
            return;
        }
        try {
            int folio = Integer.parseInt(txtFolio.getText().trim());
            if (camposTextoVacios()) return;
            Padron otro = buscarPorFolio(folio);
            if (otro != null && otro != seleccionado) {
                mostrarAlerta(Alert.AlertType.WARNING, "El nuevo folio ya pertenece a otro registro.");
                return;
            }
            seleccionado.setFolio(folio);
            seleccionado.setNombreCompleto(txtNombre.getText().trim());
            seleccionado.setRfc(txtRfc.getText().trim());
            seleccionado.setSeccionElectoral(txtSeccion.getText().trim());
            seleccionado.setDistrito(txtDistrito.getText().trim());
            tabla.refresh();
            limpiarFormulario();
            mostrarAlerta(Alert.AlertType.INFORMATION, "Registro modificado correctamente.");
        } catch (NumberFormatException e) {
            mostrarAlerta(Alert.AlertType.ERROR, "El folio debe ser un número entero.");
        }
    }

    private void baja() {
        Padron seleccionado = tabla.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Seleccione un registro para dar de baja.");
            return;
        }
        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Desea eliminar el registro de " + seleccionado.getNombreCompleto() + "?",
                ButtonType.YES, ButtonType.NO);
        confirmacion.setTitle("Confirmar baja");
        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.YES) {
                registros.remove(seleccionado);
                limpiarFormulario();
            }
        });
    }

    private boolean camposTextoVacios() {
        if (txtNombre.getText().trim().isEmpty() || txtRfc.getText().trim().isEmpty()
                || txtSeccion.getText().trim().isEmpty() || txtDistrito.getText().trim().isEmpty()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Complete todos los campos del registro.");
            return true;
        }
        return false;
    }

    private Padron buscarPorFolio(int folio) {
        for (Padron p : registros) if (p.getFolio() == folio) return p;
        return null;
    }

    private void filtrar(String texto) {
        String filtro = texto == null ? "" : texto.trim().toLowerCase();
        registrosFiltrados.setPredicate(p -> filtro.isEmpty()
                || String.valueOf(p.getFolio()).contains(filtro)
                || p.getNombreCompleto().toLowerCase().contains(filtro)
                || p.getRfc().toLowerCase().contains(filtro));
    }

    private void cargarFormulario(Padron p) {
        txtFolio.setText(String.valueOf(p.getFolio()));
        txtNombre.setText(p.getNombreCompleto());
        txtRfc.setText(p.getRfc());
        txtSeccion.setText(p.getSeccionElectoral());
        txtDistrito.setText(p.getDistrito());
    }

    private void limpiarFormulario() {
        txtFolio.clear();
        txtNombre.clear();
        txtRfc.clear();
        txtSeccion.clear();
        txtDistrito.clear();
        tabla.getSelectionModel().clearSelection();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje, ButtonType.OK);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }

    private void cargarDatosEjemplo() {
        registros.addAll(
                new Padron(1001, "Juan Pérez García", "PERJ010101ABC", "001", "01"),
                new Padron(1002, "María López Quispe", "LOQM020202DEF", "002", "01"),
                new Padron(1003, "Carlos Mamani Flores", "MAFC030303GHI", "003", "02")
        );
    }
}
