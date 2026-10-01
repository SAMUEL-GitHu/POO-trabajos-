package pe.edu.upeu.controller;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import lombok.RequiredArgsConstructor;
import pe.edu.upeu.model.Votante;
import pe.edu.upeu.servise.IVotanteService;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Controlador del Ejercicio 2: Padron electoral.
 * Permite dar de alta, baja, consultar y modificar registros de
 * votantes, y buscarlos por nombre completo o por folio.
 */
@RequiredArgsConstructor
public class VotanteController implements Initializable {

    private final IVotanteService votanteService;

    @FXML private TextField txtBusqueda;

    @FXML private TableView<Votante> tablaVotantes;
    @FXML private TableColumn<Votante, Long> colFolio;
    @FXML private TableColumn<Votante, String> colNombre;
    @FXML private TableColumn<Votante, String> colRfc;
    @FXML private TableColumn<Votante, String> colSeccion;
    @FXML private TableColumn<Votante, String> colDistrito;

    @FXML private TextField txtFolio;
    @FXML private TextField txtNombre;
    @FXML private TextField txtRfc;
    @FXML private TextField txtSeccion;
    @FXML private TextField txtDistrito;

    private final ObservableList<Votante> datos = FXCollections.observableArrayList();
    private FilteredList<Votante> datosFiltrados;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        colFolio.setCellValueFactory(new PropertyValueFactory<>("idVotante"));
        colNombre.setCellValueFactory(new PropertyValueFactory<>("nombreCompleto"));
        colRfc.setCellValueFactory(new PropertyValueFactory<>("rfc"));
        colSeccion.setCellValueFactory(new PropertyValueFactory<>("seccionElectoral"));
        colDistrito.setCellValueFactory(new PropertyValueFactory<>("distrito"));

        cargarDatosDemo();

        datosFiltrados = new FilteredList<>(datos, v -> true);
        tablaVotantes.setItems(datosFiltrados);

        tablaVotantes.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                mostrarEnFormulario(newSel);
            }
        });

        txtBusqueda.textProperty().addListener((obs, oldV, newV) -> filtrar(newV));
    }

    // ---------------------------------------------------------------
    // Operaciones CRUD (delegadas al service, como en el resto del proyecto)
    // ---------------------------------------------------------------

    @FXML
    private void onAlta() {
        if (!datosCompletos()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Faltan datos",
                    "Completa nombre, RFC, seccion electoral y distrito antes de dar de alta.");
            return;
        }

        Votante nuevo = Votante.builder()
                .nombreCompleto(txtNombre.getText().trim())
                .rfc(txtRfc.getText().trim())
                .seccionElectoral(txtSeccion.getText().trim())
                .distrito(txtDistrito.getText().trim())
                .build();

        votanteService.save(nuevo);
        datos.setAll(votanteService.findALL());
        limpiarFormulario();
        mostrarAlerta(Alert.AlertType.INFORMATION, "Alta registrada",
                "Se agrego el registro con folio " + nuevo.getIdVotante() + ".");
    }

    @FXML
    private void onModificar() {
        Votante seleccionado = tablaVotantes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin seleccion",
                    "Selecciona en la tabla el registro que deseas modificar.");
            return;
        }
        if (!datosCompletos()) {
            mostrarAlerta(Alert.AlertType.WARNING, "Faltan datos",
                    "Completa todos los campos antes de modificar.");
            return;
        }

        Votante actualizado = Votante.builder()
                .idVotante(seleccionado.getIdVotante())
                .nombreCompleto(txtNombre.getText().trim())
                .rfc(txtRfc.getText().trim())
                .seccionElectoral(txtSeccion.getText().trim())
                .distrito(txtDistrito.getText().trim())
                .build();

        votanteService.update(seleccionado.getIdVotante(), actualizado);
        datos.setAll(votanteService.findALL());
        mostrarAlerta(Alert.AlertType.INFORMATION, "Registro modificado",
                "Se actualizo el registro con folio " + actualizado.getIdVotante() + ".");
    }

    @FXML
    private void onBaja() {
        Votante seleccionado = tablaVotantes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin seleccion",
                    "Selecciona en la tabla el registro que deseas dar de baja.");
            return;
        }

        Alert confirmacion = new Alert(Alert.AlertType.CONFIRMATION,
                "¿Deseas eliminar el registro de \"" + seleccionado.getNombreCompleto() + "\" (folio "
                        + seleccionado.getIdVotante() + ")?", ButtonType.YES, ButtonType.NO);
        confirmacion.setTitle("Confirmar baja");
        confirmacion.setHeaderText(null);
        confirmacion.showAndWait().ifPresent(respuesta -> {
            if (respuesta == ButtonType.YES) {
                votanteService.delete(seleccionado.getIdVotante());
                datos.setAll(votanteService.findALL());
                limpiarFormulario();
            }
        });
    }

    @FXML
    private void onConsultar() {
        Votante seleccionado = tablaVotantes.getSelectionModel().getSelectedItem();
        if (seleccionado == null) {
            mostrarAlerta(Alert.AlertType.WARNING, "Sin seleccion",
                    "Selecciona en la tabla el registro que deseas consultar.");
            return;
        }
        String detalle = "Folio: " + seleccionado.getIdVotante()
                + "\nNombre completo: " + seleccionado.getNombreCompleto()
                + "\nRFC: " + seleccionado.getRfc()
                + "\nSeccion electoral: " + seleccionado.getSeccionElectoral()
                + "\nDistrito: " + seleccionado.getDistrito();
        mostrarAlerta(Alert.AlertType.INFORMATION, "Consulta de registro", detalle);
    }

    @FXML
    private void onLimpiar() {
        limpiarFormulario();
    }

    // ---------------------------------------------------------------
    // Busqueda por nombre o folio
    // ---------------------------------------------------------------

    private void filtrar(String texto) {
        String filtro = texto == null ? "" : texto.trim().toLowerCase();
        datosFiltrados.setPredicate(votante -> {
            if (filtro.isEmpty()) {
                return true;
            }
            boolean coincideNombre = votante.getNombreCompleto().toLowerCase().contains(filtro);
            boolean coincideFolio = String.valueOf(votante.getIdVotante()).equals(filtro);
            return coincideNombre || coincideFolio;
        });
    }

    // ---------------------------------------------------------------
    // Utilidades
    // ---------------------------------------------------------------

    private boolean datosCompletos() {
        return !txtNombre.getText().trim().isEmpty()
                && !txtRfc.getText().trim().isEmpty()
                && !txtSeccion.getText().trim().isEmpty()
                && !txtDistrito.getText().trim().isEmpty();
    }

    private void mostrarEnFormulario(Votante v) {
        txtFolio.setText(String.valueOf(v.getIdVotante()));
        txtNombre.setText(v.getNombreCompleto());
        txtRfc.setText(v.getRfc());
        txtSeccion.setText(v.getSeccionElectoral());
        txtDistrito.setText(v.getDistrito());
    }

    private void limpiarFormulario() {
        txtFolio.clear();
        txtNombre.clear();
        txtRfc.clear();
        txtSeccion.clear();
        txtDistrito.clear();
        tablaVotantes.getSelectionModel().clearSelection();
    }

    private void mostrarAlerta(Alert.AlertType tipo, String titulo, String mensaje) {
        Alert alerta = new Alert(tipo, mensaje, ButtonType.OK);
        alerta.setTitle(titulo);
        alerta.setHeaderText(null);
        alerta.showAndWait();
    }

    private void cargarDatosDemo() {
        votanteService.save(Votante.builder()
                .nombreCompleto("Ana Maria Lopez Quispe")
                .rfc("LOQA850312M12")
                .seccionElectoral("0452")
                .distrito("12")
                .build());
        votanteService.save(Votante.builder()
                .nombreCompleto("Carlos Fernando Mamani Ticona")
                .rfc("MATC900105H45")
                .seccionElectoral("0128")
                .distrito("05")
                .build());
        votanteService.save(Votante.builder()
                .nombreCompleto("Rosa Elena Huanca Condori")
                .rfc("HUCR781120M09")
                .seccionElectoral("0335")
                .distrito("08")
                .build());
        datos.setAll(votanteService.findALL());
    }
}
