package pe.edu.upeu.sysventas.controller;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import lombok.RequiredArgsConstructor;
import pe.edu.upeu.sysventas.components.ColumnInfo;
import pe.edu.upeu.sysventas.components.TableViewHelper;
import pe.edu.upeu.sysventas.components.Toast;
import pe.edu.upeu.sysventas.components.ToltipCustom;
import pe.edu.upeu.sysventas.dto.ComboBoxOption;
import pe.edu.upeu.sysventas.enums.TipoProducto;
import pe.edu.upeu.sysventas.model.Color;
import pe.edu.upeu.sysventas.model.Producto;
import pe.edu.upeu.sysventas.model.ProductoVariante;
import pe.edu.upeu.sysventas.model.Talla;
import pe.edu.upeu.sysventas.service.ICategoriaService;
import pe.edu.upeu.sysventas.service.IColorService;
import pe.edu.upeu.sysventas.service.IMarcaService;
import pe.edu.upeu.sysventas.service.IProductoService;
import pe.edu.upeu.sysventas.service.IProductoVarianteService;
import pe.edu.upeu.sysventas.service.ITallaService;
import pe.edu.upeu.sysventas.service.IUnidadMedidaService;

import java.util.*;
import java.util.function.Consumer;

@RequiredArgsConstructor
public class ProductoController {

    private final IMarcaService ms;
    private final ICategoriaService cs;
    private final IProductoService ps;
    private final IUnidadMedidaService us;
    private final ITallaService ts;
    private final IColorService cos;
    private final IProductoVarianteService vs;

    @FXML
    ComboBox<ComboBoxOption> cbxTipoProducto, cbxMarca, cbxCategoria, cbxUnidMedida,
            cbxTalla, cbxColor;
    @FXML
    TextField txtNombreProducto, txtPUnit,
            txtPUnitOld, txtUtilidad, txtStock, txtStockVariante, txtFiltroDato;
    @FXML
    Button btnAgregarVariante;

    @FXML private TableView<Producto> tableView;
    @FXML private TableView<ProductoVariante> tableVariantes;
    ObservableList<Producto> listarProducto;

    @FXML
    Label lbnMsg;
    @FXML private AnchorPane miContenedor;
    Stage stage;
    Producto formulario;

    Long idProductoCE = 0L;
    private Validator validator;
    private final ToltipCustom ttc=new ToltipCustom();


    @FXML
    public  void initialize(){

        cbxTipoProducto.getItems().addAll(ps.listarTipoProducto());
        cbxMarca.getItems().addAll(ms.listarCombobox());
        cbxCategoria.getItems().addAll(cs.listarCombobox());
        cbxUnidMedida.getItems().addAll(us.listarCombobox());
        cbxTalla.getItems().addAll(ts.listarCombobox());
        cbxColor.getItems().addAll(cos.listarCombobox());

        // El stock total no se escribe a mano: es la suma del stock de las
        // variantes (talla + color) de abajo. Solo se muestra.
        txtStock.setEditable(false);

        TableViewHelper<Producto> tableViewHelper = new TableViewHelper<>();
        LinkedHashMap<String, ColumnInfo> columns = new LinkedHashMap<>();
        columns.put("ID Pro.", new ColumnInfo("idProducto", 60.0));
        columns.put("Tipo Producto", new ColumnInfo("tipoProducto", 150.0));
        columns.put("Nombre Producto", new ColumnInfo("nombre", 200.0));
        columns.put("P. Unitario", new ColumnInfo("pu", 150.0));
        columns.put("Utilidad", new ColumnInfo("utilidad", 100.0));
        columns.put("Marca", new ColumnInfo("idMarca.nombre", 200.0));
        columns.put("Categoria", new ColumnInfo("idCategoria.nombre", 200.0));
        columns.put("Stock total", new ColumnInfo("stock", 90.0));

        Consumer<Producto> updateAction = producto ->{  editForm(producto);
        idProductoCE=producto.getIdProducto();
        };
        Consumer<Producto> deleteAction = producto -> {
            stage = (Stage) miContenedor.getScene().getWindow();
            double w = stage.getWidth() / 1.5, h = stage.getHeight() / 2;
            try {
                ps.delete(producto.getIdProducto());
                Toast.showToast(stage, "Se eliminó correctamente!!", 2000, w, h);
            } catch (Exception e) {
                // La base de datos no deja borrar un producto que ya tiene
                // variantes o ventas: se avisa en vez de romper la pantalla.
                Toast.showToast(stage, "No se puede eliminar: el producto tiene variantes o ventas.", 3000, w, h);
            }
            listar();
        };

        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();

        tableViewHelper.addColumnsInOrderWithSize(tableView, columns, updateAction, deleteAction);
        tableView.setTableMenuButtonVisible(true);

        TableViewHelper<ProductoVariante> helperVariantes = new TableViewHelper<>();
        LinkedHashMap<String, ColumnInfo> colsVariante = new LinkedHashMap<>();
        colsVariante.put("Talla", new ColumnInfo("idTalla.nombre", 100.0));
        colsVariante.put("Color", new ColumnInfo("idColor.nombre", 150.0));
        colsVariante.put("Stock", new ColumnInfo("stock", 100.0));
        helperVariantes.addColumnsInOrderWithSize(tableVariantes, colsVariante,
                variante -> { /* las variantes no se editan: se vuelve a agregar y se suma el stock */ },
                variante -> {
                    stage = (Stage) miContenedor.getScene().getWindow();
                    double w = stage.getWidth() / 1.5, h = stage.getHeight() / 2;
                    try {
                        vs.delete(variante.getIdVariante());
                        Toast.showToast(stage, "Variante eliminada.", 2000, w, h);
                    } catch (Exception e) {
                        Toast.showToast(stage, "No se puede eliminar la variante: ya tiene ventas.", 3000, w, h);
                    }
                    cargarVariantes(idProductoCE);
                    listar();
                });
        btnAgregarVariante.setDisable(true);
        listar();
    }

    public void listar() {
        try {
            tableView.getItems().clear();
            listarProducto = FXCollections.observableArrayList(ps.findAll());
            tableView.getItems().addAll(listarProducto);
            //xtFiltroDato.textProperty().addListener((obs, o, n) -> filtrarProductos(n));
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    /** Variantes (talla + color + stock) del producto que se esta editando. */
    public void cargarVariantes(Long idProducto) {
        tableVariantes.getItems().clear();
        if (idProducto == null || idProducto <= 0L) {
            return;
        }
        tableVariantes.getItems().addAll(vs.listarPorProducto(idProducto));
    }

    @FXML
    public void agregarVariante() {
        stage = (Stage) miContenedor.getScene().getWindow();
        double w = stage.getWidth() / 1.5, h = stage.getHeight() / 2;
        if (idProductoCE <= 0L) {
            Toast.showToast(stage, "Primero guarda el producto.", 2500, w, h);
            return;
        }
        ComboBoxOption tallaSel = cbxTalla.getSelectionModel().getSelectedItem();
        ComboBoxOption colorSel = cbxColor.getSelectionModel().getSelectedItem();
        Integer stockVar = parseIntSafe(txtStockVariante.getText());
        if (tallaSel == null || colorSel == null || stockVar == null || stockVar < 0) {
            lbnMsg.setText("Elige la talla, el color y un stock mayor o igual a 0.");
            lbnMsg.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
            return;
        }
        try {
            ProductoVariante variante = ProductoVariante.builder()
                    .idProducto(Producto.builder().idProducto(idProductoCE).build())
                    .idTalla(Talla.builder()
                            .idTalla(Long.parseLong(tallaSel.getKey())).build())
                    .idColor(Color.builder()
                            .idColor(Long.parseLong(colorSel.getKey())).build())
                    .stock(stockVar)
                    .build();
            vs.registrarVariante(variante);
            Toast.showToast(stage, "Variante guardada.", 2000, w, h);
            cbxTalla.getSelectionModel().clearSelection();
            cbxColor.getSelectionModel().clearSelection();
            txtStockVariante.clear();
            cargarVariantes(idProductoCE);
            // Refresca el stock total que se muestra en el formulario y la tabla
            txtStock.setText(String.valueOf(ps.findById(idProductoCE).getStock()));
            listar();
        } catch (Exception e) {
            Toast.showToast(stage, "No se pudo guardar la variante.", 2500, w, h);
        }
    }

    @FXML
    public void validarFormulario() {
        formulario = new Producto();
        formulario.setNombre(txtNombreProducto.getText());
        formulario.setPu(parseDoubleSafe(txtPUnit.getText()));
        formulario.setPuold(parseDoubleSafe(txtPUnitOld.getText()));
        formulario.setUtilidad(parseDoubleSafe(txtUtilidad.getText()));
        // El stock NO se lee del formulario: es la suma de las variantes.
        // Al crear es 0; al editar se conserva el total que ya tiene.
        if (idProductoCE > 0L) {
            Producto actual = ps.findById(idProductoCE);
            formulario.setStock(actual.getStock());
            formulario.setStockold(actual.getStockold());
        } else {
            formulario.setStock(0);
            formulario.setStockold(0);
        }

        String idxTP = cbxTipoProducto.getSelectionModel().getSelectedItem() == null ? ""
                : cbxTipoProducto.getSelectionModel().getSelectedItem().getKey();
        formulario.setTipoProducto(idxTP.equals("") ? null : TipoProducto.valueOf(idxTP));

        String idxM = cbxMarca.getSelectionModel().getSelectedItem() == null ? "0"
                : cbxMarca.getSelectionModel().getSelectedItem().getKey();
        formulario.setIdMarca(idxM.equals("0") ? null : ms.findById(Long.parseLong(idxM)));

        String idxC = cbxCategoria.getSelectionModel().getSelectedItem() == null ? "0"
                : cbxCategoria.getSelectionModel().getSelectedItem().getKey();
        formulario.setIdCategoria(idxC.equals("0") ? null : cs.findById(Long.parseLong(idxC)));

        String idxUM = cbxUnidMedida.getSelectionModel().getSelectedItem() == null ? "0"
                : cbxUnidMedida.getSelectionModel().getSelectedItem().getKey();
        formulario.setIdUnidad(idxUM.equals("0") ? null : us.findById(Long.parseLong(idxUM)));

        Set<ConstraintViolation<Producto>> violaciones = validator.validate(formulario);
        List<ConstraintViolation<Producto>> violacionesOrdenadas = violaciones.stream()
                .sorted(Comparator.comparing(v -> v.getPropertyPath().toString())).toList();

        if (violacionesOrdenadas.isEmpty()) {
            procesarFormulario();
            listar();

        } else {
            mostrarErroresValidacion(violacionesOrdenadas);
        }
    }

    /**
     * Devuelve null si el texto esta vacio o no es un numero, para que la
     * validacion lo marque como error. Antes devolvia 0.0 y un "abc" en el
     * precio pasaba como 0 sin avisar.
     */
    private Double parseDoubleSafe(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try { return Double.parseDouble(value.trim()); }
        catch (NumberFormatException e) { return null; }
    }

    private Integer parseIntSafe(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try { return Integer.parseInt(value.trim()); }
        catch (NumberFormatException e) { return null; }
    }

    @FXML
    public void clearForm() {
        txtNombreProducto.clear();
        cbxTipoProducto.getSelectionModel().clearSelection();
        txtPUnit.clear(); txtPUnitOld.clear();
        txtUtilidad.clear(); txtStock.clear();
        cbxMarca.getSelectionModel().clearSelection();
        cbxCategoria.getSelectionModel().clearSelection();
        cbxUnidMedida.getSelectionModel().clearSelection();
        cbxTalla.getSelectionModel().clearSelection();
        cbxColor.getSelectionModel().clearSelection();
        txtStockVariante.clear();
        tableVariantes.getItems().clear();
        btnAgregarVariante.setDisable(true);
        idProductoCE = 0L;
        limpiarError();
    }

    public void editForm(Producto producto) {
        txtNombreProducto.setText(producto.getNombre());

        txtPUnit.setText(producto.getPu().toString());
        txtPUnitOld.setText(producto.getPuold().toString());
        txtUtilidad.setText(producto.getUtilidad().toString());
        txtStock.setText(producto.getStock() != null ? producto.getStock().toString() : "0");

        cbxTipoProducto.getSelectionModel().select(
                cbxTipoProducto.getItems().stream()
                        .filter(m -> m.getKey().equals(producto.getTipoProducto().name()))
                        .findFirst().orElse(null));

        cbxMarca.getSelectionModel().select(
                cbxMarca.getItems().stream()
                        .filter(m -> Long.parseLong(m.getKey()) == producto.getIdMarca().getIdMarca())
                        .findFirst().orElse(null));
        cbxCategoria.getSelectionModel().select(
                cbxCategoria.getItems().stream()
                        .filter(c -> Long.parseLong(c.getKey()) == producto.getIdCategoria().getIdCategoria())
                        .findFirst().orElse(null));
        cbxUnidMedida.getSelectionModel().select(
                cbxUnidMedida.getItems().stream()
                        .filter(u -> Long.parseLong(u.getKey()) == producto.getIdUnidad().getIdUnidad())
                        .findFirst().orElse(null));

        idProductoCE = producto.getIdProducto();
        btnAgregarVariante.setDisable(false);
        cargarVariantes(idProductoCE);
        limpiarError();
    }
    private void procesarFormulario() {
        stage = (Stage) miContenedor.getScene().getWindow();
        lbnMsg.setText("Formulario válido");
        lbnMsg.setStyle("-fx-text-fill: green; -fx-font-size: 16px;");
        limpiarError();
        double w = stage.getWidth() / 1.5, h = stage.getHeight() / 2;
        if (idProductoCE > 0L) {
            formulario.setIdProducto(idProductoCE);
            ps.update(idProductoCE, formulario);
            Toast.showToast(stage, "Se actualizó correctamente!!", 2000, w, h);
        } else {
            Producto guardado = ps.save(formulario);
            idProductoCE = guardado.getIdProducto();
            btnAgregarVariante.setDisable(false);
            cargarVariantes(idProductoCE);
            Toast.showToast(stage, "Se guardó correctamente!! Ahora agrega sus tallas y colores.", 3000, w, h);
        }
        listar();
    }
    public void limpiarError() {
        List.of(txtNombreProducto,
                        cbxTipoProducto,
                        txtPUnit, txtPUnitOld, txtUtilidad,
                        txtStock, cbxMarca, cbxCategoria, cbxUnidMedida)
                .forEach(c -> {c.getStyleClass().remove("text-field-error");
                    ttc.limpiarCampo(c);
                });
    }


    private void mostrarErroresValidacion(List<ConstraintViolation<Producto>> violaciones) {
        limpiarError();
        Map<String, Control> campos = new LinkedHashMap<>();
        campos.put("nombre", txtNombreProducto);
        campos.put("tipoProducto", cbxTipoProducto);
        campos.put("pu", txtPUnit);
        campos.put("puold", txtPUnitOld);
        campos.put("utilidad", txtUtilidad);
        campos.put("idMarca", cbxMarca);
        campos.put("idCategoria", cbxCategoria);
        campos.put("idUnidad", cbxUnidMedida);

        LinkedHashMap<String, String> erroresOrdenados = new LinkedHashMap<>();
        final Control[] primerCtrl = {null};
        for (String campo : campos.keySet()) {
            violaciones.stream()
                    .filter(v -> v.getPropertyPath().toString().equals(campo))
                    .findFirst().ifPresent(v -> {

                        erroresOrdenados.put(campo, v.getMessage());

                        Control c = campos.get(campo);
                        if (c != null && !c.getStyleClass().contains("text-field-error")){
                            //c.getStyleClass().add("text-field-error");
                            if (c != null) ttc.marcarError(c, v.getMessage().trim());
                        }
                        if (primerCtrl[0] == null) primerCtrl[0] = c;
                    });
        }
        if (!erroresOrdenados.isEmpty()) {
            lbnMsg.setText(erroresOrdenados.entrySet().iterator().next().getValue());
            lbnMsg.setStyle("-fx-text-fill: red; -fx-font-size: 16px;");
            if (primerCtrl[0] != null) Platform.runLater(primerCtrl[0]::requestFocus);
        }
    }
}
