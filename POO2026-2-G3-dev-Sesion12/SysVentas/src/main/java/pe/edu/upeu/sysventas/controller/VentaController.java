package pe.edu.upeu.sysventas.controller;


import com.github.anastaciocintra.escpos.EscPos;
import com.github.anastaciocintra.escpos.EscPosConst;
import com.github.anastaciocintra.escpos.Style;
import com.github.anastaciocintra.escpos.barcode.QRCode;
import com.github.anastaciocintra.output.PrinterOutputStream;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import pe.edu.upeu.sysventas.components.*;
import pe.edu.upeu.sysventas.dto.ComboBoxOption;
import pe.edu.upeu.sysventas.dto.ModeloDataAutocomplet;
import pe.edu.upeu.sysventas.dto.PersonaDto;
import pe.edu.upeu.sysventas.dto.SessionManager;
import pe.edu.upeu.sysventas.dto.comprobante.Comprobante;
import pe.edu.upeu.sysventas.enums.TipoDocumento;
import pe.edu.upeu.sysventas.enums.TipoProducto;
import pe.edu.upeu.sysventas.exception.ModelNotFoundException;
import pe.edu.upeu.sysventas.model.Cliente;
import pe.edu.upeu.sysventas.model.ProductoVariante;
import pe.edu.upeu.sysventas.model.VentCarrito;
import pe.edu.upeu.sysventas.model.Venta;
import pe.edu.upeu.sysventas.model.VentaDetalle;
import pe.edu.upeu.sysventas.service.*;
import pe.edu.upeu.sysventas.utils.ConsultaDNI;
import pe.edu.upeu.sysventas.utils.PrinterManager;

import javax.print.PrintService;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.function.Consumer;


public class VentaController {
    @FXML
    TextField autocompProducto;
    @FXML
    TextField nombreProducto, codigoPro, stockPro, cantidadPro, punitPro, preTPro, txtBaseImp, txtIgv, txtDescuento, txtImporteT;
    @FXML
    Button btnRegVenta, btnRegCarrito, btnFormCliente, btnRegCliente;
    @FXML
    TextField autocompCliente, txtDireccion;
    @FXML
    TextField razonSocial;
    @FXML
    TextField dniRuc;
    @FXML
    ComboBox<ComboBoxOption> cbxVariante;

    @FXML
    TableView<VentCarrito> tableView;
    AutoCompleteTextField actf;
    ModeloDataAutocomplet lastProducto;

    AutoCompleteTextField actfC;
    ModeloDataAutocomplet lastCliente;

    IProductoService ps;
    IClienteService cs;
    IVentaCarritoService daoC;
    IUsuarioService daoU;
    IVentaService daoV;
    IVentaDetalleService daoVD;
    IProductoVarianteService daoPV;
    Stage stage;
    @FXML
    private AnchorPane miContenedor;
    private final SortedSet<ModeloDataAutocomplet> entries = new TreeSet<>((ModeloDataAutocomplet o1, ModeloDataAutocomplet o2) -> o1.toString().compareTo(o2.toString()));
    private final SortedSet<ModeloDataAutocomplet> entriesC = new TreeSet<>((ModeloDataAutocomplet o1, ModeloDataAutocomplet o2) -> o1.toString().compareTo(o2.toString()));

    ConsultaDNI cDni;
    TipoProducto tipoProducto;
    /** Variante (talla + color) elegida para el producto que se esta vendiendo. */
    ProductoVariante varianteSeleccionada;
    /** Cantidad de la variante elegida que este cliente ya tiene en el carrito. */
    int cantidadEnCarrito = 0;


    public VentaController(IProductoService ps, IClienteService cs, IVentaCarritoService daoC, IUsuarioService daoU, IVentaService daoV, IVentaDetalleService daoVD, IProductoVarianteService daoPV, ConsultaDNI cDni) {
        this.ps = ps;
        this.cs = cs;
        this.daoC = daoC;
        this.daoU = daoU;
        this.daoV = daoV;
        this.daoVD = daoVD;
        this.daoPV = daoPV;
        this.cDni = cDni;
    }

    @FXML
    public void initialize(){

        Platform.runLater(() -> {
            stage = (Stage) miContenedor.getScene().getWindow();
            System.out.println("El título del stage es: " + stage.getTitle());
        });

        listarCliente();
        autoCompletarCliente();

        listarProducto();
        actf=new AutoCompleteTextField<>(entries, autocompProducto);
        autocompProducto.setOnKeyReleased(e->{
            lastProducto=(ModeloDataAutocomplet) actf.getLastSelectedObject();
            if(lastProducto!=null){
                System.out.println(lastProducto.getNameDysplay());
                nombreProducto.setText(lastProducto.getNameDysplay());
                codigoPro.setText(lastProducto.getIdx());
                String[] dato=lastProducto.getOtherData().split(":");
                punitPro.setText(dato[0]);
                stockPro.setText(dato[1]);
                tipoProducto=TipoProducto.valueOf(dato[2]);
                cargarVariantesProducto(Long.parseLong(lastProducto.getIdx()));
            }
        });
        cbxVariante.setOnAction(e -> {
            ComboBoxOption sel = cbxVariante.getSelectionModel().getSelectedItem();
            if (sel != null) {
                varianteSeleccionada = daoPV.findById(Long.parseLong(sel.getKey()));
                // El stock que manda es el de la variante, no el total del producto
                stockPro.setText(String.valueOf(varianteSeleccionada.getStock()));
                actualizarCantidadEnCarrito();
                calcularPT();
            }
        });


        // Al escribir el descuento en % se recalculan los totales al instante.
        txtDescuento.textProperty().addListener((obs, oldV, newV) -> listar());

        personalizarTabla();
        btnRegCarrito.setDisable(true);
        btnRegCliente.setDisable(true);
    }

    public void listarProducto(){
        entries.addAll(ps.listAutoCompletProducto());
    }

    /** Carga en el ComboBox las variantes (talla + color) del producto elegido. */
    public void cargarVariantesProducto(Long idProducto) {
        cbxVariante.getItems().clear();
        varianteSeleccionada = null;
        List<ProductoVariante> variantes = daoPV.listarPorProducto(idProducto);
        for (ProductoVariante v : variantes) {
            ComboBoxOption opt = new ComboBoxOption();
            opt.setKey(String.valueOf(v.getIdVariante()));
            opt.setValue(v.getDescripcion());
            cbxVariante.getItems().add(opt);
        }
        if (variantes.isEmpty()) {
            Stage st = StageManager.getPrimaryStage();
            double with = st.getMaxWidth() / 2;
            Toast.showToast(st, "Este producto aun no tiene tallas ni colores. Agregalos en Gestion de Productos.", 3000, with, 50);
            btnRegCarrito.setDisable(true);
            return;
        }
        // Preselecciona la primera variante con stock
        for (int i = 0; i < variantes.size(); i++) {
            if (variantes.get(i).getStock() > 0) {
                cbxVariante.getSelectionModel().select(i);
                break;
            }
        }
        if (cbxVariante.getSelectionModel().getSelectedItem() == null) {
            cbxVariante.getSelectionModel().select(0);
        }
    }

    public void listarCliente(){
        entriesC.clear();
        entriesC.addAll(cs.listAutoCompletCliente());
    }

    public void autoCompletarCliente(){
        actfC=new AutoCompleteTextField<>(entriesC, autocompCliente);
        autocompCliente.setOnKeyReleased(e->{
            lastCliente=(ModeloDataAutocomplet) actfC.getLastSelectedObject();
            if(lastCliente!=null){
                System.out.println(lastCliente.getNameDysplay());
                razonSocial.setText(lastCliente.getNameDysplay());
                dniRuc.setText(lastCliente.getIdx());
                listar();
            }else {
                btnRegCliente.setDisable(true);
                limpiarFormCliente();
            }
        });
    }




    @FXML
    public void buscarClienteCdni(){
        limpiarFormCliente();
        Stage stage = StageManager.getPrimaryStage();
        double with=stage.getMaxWidth()/2;
        if(autocompCliente.getText().length()==8 || autocompCliente.getText().length()==11){
            try {
                if(cs.findById(autocompCliente.getText())!=null){
                    btnRegCliente.setDisable(true);
                    Toast.showToast(stage, "El cliente si existe", 2000, with, 50);
                    return;}
                consultarDNIReniec(with);
            } catch (ModelNotFoundException e) {
                btnRegCliente.setDisable(true);
                Toast.showToast(stage, "El cliente no existe", 2000, with, 50);
                consultarDNIReniec(with);
            }
        }else{
            btnRegCliente.setDisable(true);
            Toast.showToast(stage, "El valor buscado debe tener 8 o 11 digitos", 2000, with, 50);
        }
    }



    public void consultarDNIReniec(double with){
        PersonaDto p=cDni.consultarDNI(autocompCliente.getText());
        if(p!=null){
            razonSocial.setText(p.getNombre()+" "+p.getApellidoPaterno()+" "+p.getApellidoMaterno());
            dniRuc.setText(p.getDni());
            btnRegCliente.setDisable(false);
            Toast.showToast(stage, "El cliente se encontró en RENIEC para registrar debe hacer clik en Add", 2000, with, 50);
        }else{
            btnRegCliente.setDisable(true);
            Toast.showToast(stage, "El cliente no se encuentra en RENIEC y debe registrar a través del formulario de cliente", 2000, with, 50);
        }
    }

    public void limpiarFormCliente(){
        razonSocial.clear();
        dniRuc.clear();
        txtDireccion.clear();
    }

    // Limpia el formulario de la venta para la siguiente venta.
    private void limpiarFormVenta() {
        autocompProducto.clear();
        nombreProducto.clear();
        codigoPro.clear();
        stockPro.clear();
        cantidadPro.clear();
        punitPro.clear();
        preTPro.clear();
        txtDescuento.clear();
        cbxVariante.getItems().clear();
        cbxVariante.getSelectionModel().clearSelection();
        autocompCliente.clear();
        limpiarFormCliente();
        cantidadEnCarrito = 0;
    }


    @FXML
    public void guardarCliente(){
        Stage stage = StageManager.getPrimaryStage();
        double with=stage.getMaxWidth()/2;
        String dniruc = dniRuc.getText() == null ? "" : dniRuc.getText().trim();
        String nombres = razonSocial.getText() == null ? "" : razonSocial.getText().trim();
        String direccion = txtDireccion.getText() == null ? "" : txtDireccion.getText().trim();
        // DNI = 8 digitos, RUC = 11 digitos. Antes se guardaba cualquier texto
        // y el tipo de documento siempre era DNI, aunque fuera un RUC.
        if (dniruc.length() != 8 && dniruc.length() != 11) {
            Toast.showToast(stage, "El DNI debe tener 8 digitos y el RUC 11.", 2500, with, 50);
            return;
        }
        if (nombres.isEmpty() || direccion.isEmpty()) {
            Toast.showToast(stage, "Completa el nombre y la direccion del cliente.", 2500, with, 50);
            return;
        }
        try {
            boolean esRuc = dniruc.length() == 11;
            Cliente c= Cliente.builder().dniruc(dniruc)
                    .nombres(nombres)
                    // Solo las empresas (RUC) tienen representante legal
                    .repLegal(esRuc ? nombres : null)
                    .tipoDocumento(esRuc ? TipoDocumento.RUC : TipoDocumento.DNI)
                    .direccion(direccion)
                    .build();
            cs.save(c);
            btnRegCliente.setDisable(true);
            Toast.showToast(stage, "El cliente se guardo satisfactoriamente!", 2000, with, 50);
            listarCliente();
            listar();
        }catch (Exception e){
            Toast.showToast(stage, "Error al guardar cliente!", 2000, with, 50);
        }
    }

    public void personalizarTabla(){
        TableViewHelper<VentCarrito> tableViewHelper = new TableViewHelper<>();
        LinkedHashMap<String, ColumnInfo> columns = new LinkedHashMap<>();
        columns.put("ID Prod", new ColumnInfo("idProducto.idProducto", 100.0)); // Columna visible "Columna 1" mapea al campo "campo1"
        columns.put("Nombre Producto", new ColumnInfo("nombreProducto", 300.0)); // Columna visible "Columna 1" mapea al campo "campo1"
        columns.put("Talla / Color", new ColumnInfo("idVariante.descripcion", 200.0));
        columns.put("Cantidad", new ColumnInfo("cantidad", 60.0)); // Columna visible "Columna 2" mapea al campo "campo2"
        columns.put("P.Unitario", new ColumnInfo("punitario", 100.0)); // Columna visible "Columna 2" mapea al campo "campo2"
        columns.put("P.Total", new ColumnInfo("ptotal", 100.0)); // Columna visible "Columna 2" mapea al campo "campo2"

        Consumer<VentCarrito> updateAction = (VentCarrito ventCarrito) -> { System.out.println("Actualizar: " + ventCarrito); };
        Consumer<VentCarrito> deleteAction = (VentCarrito ventCarrito) -> {deleteReg(ventCarrito); };

        tableViewHelper.addColumnsInOrderWithSize(tableView, columns,updateAction, deleteAction );

        tableView.setTableMenuButtonVisible(true);
    }

    /**
     * Cuanto de la variante elegida ya tiene este cliente en el carrito.
     * Sirve para que el boton "Add" no deje pedir mas de lo que queda:
     * si hay 5 en stock y ya hay 3 en el carrito, solo se pueden agregar 2.
     */
    private void actualizarCantidadEnCarrito() {
        cantidadEnCarrito = 0;
        if (varianteSeleccionada == null) return;
        String dniruc = dniRuc.getText() == null ? "" : dniRuc.getText().trim();
        if (dniruc.isEmpty()) return;
        actualizarCantidadEnCarrito(daoC.listaCarritoCliente(dniruc));
    }

    private void actualizarCantidadEnCarrito(List<VentCarrito> lineas) {
        cantidadEnCarrito = 0;
        if (varianteSeleccionada == null) return;
        for (VentCarrito linea : lineas) {
            if (linea.getIdVariante() != null
                    && linea.getIdVariante().getIdVariante() != null
                    && linea.getIdVariante().getIdVariante()
                            .equals(varianteSeleccionada.getIdVariante())) {
                cantidadEnCarrito += linea.getCantidad();
            }
        }
    }

    /** Stock de la variante que todavia se puede agregar (stock - lo del carrito). */
    private int stockDisponible() {
        if (varianteSeleccionada == null) return 0;
        return Math.max(0, varianteSeleccionada.getStock() - cantidadEnCarrito);
    }

    public void listar(){
        tableView.getItems().clear();
        List<VentCarrito> lista=daoC.listaCarritoCliente(dniRuc.getText());
        double impoTotal = 0, igv = 0;
        for (VentCarrito dato: lista){
            impoTotal += Double.parseDouble(String.valueOf(dato.getPtotal()));
        }
        // Descuento en porcentaje: se resta del total antes de sacar base e IGV.
        double pct = porcentajeDescuento();
        double impoConDescuento = Math.round(impoTotal * (1 - pct / 100.0) * 100.0) / 100.0;
        txtImporteT.setText(String.valueOf(impoConDescuento));
        double pv = impoConDescuento / 1.18;
        txtBaseImp.setText(String.valueOf(Math.round(pv * 100.0) / 100.0));
        txtIgv.setText(String.valueOf(Math.round((pv * 0.18) * 100.0) / 100.0));
        tableView.getItems().addAll(lista);
        actualizarCantidadEnCarrito(lista);
        calcularPT();
    }

    // Lee el descuento en porcentaje (0 a 100). Si esta vacio o no es numero, es 0.
    private double porcentajeDescuento() {
        Double pct = parseDoubleSafe(txtDescuento.getText());
        if (pct == null || pct < 0) return 0;
        if (pct > 100) return 100;
        return pct;
    }

    public void editVenCarrito(VentCarrito obj) {
        System.out.println(obj.getDniruc());
    }

    public void deleteReg(VentCarrito obj) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Confirmación");
        alert.setHeaderText("Confirmar acción");
        alert.setContentText("¿Estás seguro de que deseas eliminar este elemento?");
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            daoC.delete(obj.getIdCarrito());
            Stage stage = StageManager.getPrimaryStage();
            double with=stage.getMaxWidth()/2;
            Toast.showToast(stage, "¡Acción completada!", 2000, with, 50);
            listar();
        } else {
            System.out.println("Acción cancelada");
        }
    }

    @FXML
    private void calcularPT(){
        // La cantidad es entera y no puede pasar lo que queda por agregar
        // de la variante (su stock menos lo que ya esta en el carrito).
        // Antes un texto no numerico rompia la pantalla (NumberFormatException)
        // y se podia pedir mas cantidad que el stock sin ningun aviso.
        Integer cantidad = parseIntSafe(cantidadPro.getText());
        Double precio = parseDoubleSafe(punitPro.getText());
        if (cantidad == null || precio == null) {
            preTPro.clear();
            btnRegCarrito.setDisable(true);
            return;
        }
        preTPro.setText(String.valueOf(Math.round(precio * cantidad * 100.0) / 100.0));
        btnRegCarrito.setDisable(cantidad <= 0 || cantidad > stockDisponible());
    }

    private Integer parseIntSafe(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try { return Integer.parseInt(value.trim()); }
        catch (NumberFormatException e) { return null; }
    }

    private Double parseDoubleSafe(String value) {
        if (value == null || value.trim().isEmpty()) return null;
        try { return Double.parseDouble(value.trim()); }
        catch (NumberFormatException e) { return null; }
    }


    @FXML
    private void registarPCarrito(){
        Stage st = StageManager.getPrimaryStage();
        double with = st.getMaxWidth() / 2;
        String dniruc = dniRuc.getText() == null ? "" : dniRuc.getText().trim();
        if (dniruc.isEmpty()) {
            Toast.showToast(st, "Primero elige el cliente.", 2500, with, 50);
            return;
        }
        if (lastProducto == null || varianteSeleccionada == null) {
            Toast.showToast(st, "Elige el producto y su talla/color.", 2500, with, 50);
            return;
        }
        Integer cantidad = parseIntSafe(cantidadPro.getText());
        Double precio = parseDoubleSafe(punitPro.getText());
        Double total = parseDoubleSafe(preTPro.getText());
        if (cantidad == null || cantidad <= 0 || precio == null || total == null) {
            Toast.showToast(st, "Revisa la cantidad y el precio.", 2500, with, 50);
            return;
        }
        try {
            VentCarrito ss= VentCarrito.builder()
                    .dniruc(dniruc)
                    .idProducto(ps.findById(Long.parseLong(codigoPro.getText())))
                    .idVariante(varianteSeleccionada)
                    .nombreProducto(nombreProducto.getText())
                    .cantidad(cantidad)
                    .punitario(precio)
                    .ptotal(total)
                    .estado(1)
                    .idUsuario(daoU.findById(SessionManager.getInstance().getUserId()))
                    .tipoProducto(tipoProducto)
                    .build();
            // Si esa variante ya esta en el carrito, se suma a la misma
            // linea en vez de crear otra (ver agregarAlCarrito).
            VentCarrito linea = daoC.agregarAlCarrito(ss);
            if (linea.getCantidad() > cantidad) {
                Toast.showToast(st, "Cantidad actualizada: ahora son "
                        + linea.getCantidad() + " de "
                        + varianteSeleccionada.getDescripcion() + ".", 2500, with, 50);
            } else {
                Toast.showToast(st, "Agregado al carrito.", 2000, with, 50);
            }
            listar();
        } catch (IllegalStateException | IllegalArgumentException e) {
            Toast.showToast(st, e.getMessage(), 3500, with, 50);
        } catch (Exception e){
            Toast.showToast(st, "No se pudo agregar al carrito.", 2500, with, 50);
        }
    }

    @FXML
    public void registrarVenta(){
        Stage stage = StageManager.getPrimaryStage();
        double with=stage.getMaxWidth()/2;
        Locale locale = new Locale("es", "es-PE");
        LocalDateTime localDate = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy", locale);
        String fechaFormateada = localDate.format(formatter);
        List<VentCarrito> dd = daoC.listaCarritoCliente(dniRuc.getText());
        if(dd.isEmpty()){
            Toast.showToast(stage, "No hay productos en el carrito", 2000, with, 50);
            return;
        }
        try {
            Venta to=Venta.builder()
                    .dniruc(cs.findById(dniRuc.getText()))
                    .precioBase(Double.parseDouble(txtBaseImp.getText()))
                    .igv(Double.parseDouble(txtIgv.getText()))
                    .precioTotal(Double.parseDouble(txtImporteT.getText()))
                    .idUsuario(daoU.findById(SessionManager.getInstance().getUserId()))
                    .serie("V")
                    .tipoDoc("Factura")
                    .fechaGener(LocalDate.parse(fechaFormateada, formatter))
                    .numDoc("00" )
                    .build();
            List<VentaDetalle> vdList = new ArrayList<>();
            // El descuento en % se reparte en cada linea (se guarda en la base de datos).
            double pctDesc = porcentajeDescuento();
            for (VentCarrito car : dd) {
                double descuentoLinea = Math.round(car.getPtotal() * pctDesc / 100.0 * 100.0) / 100.0;
                vdList.add(VentaDetalle.builder()
                        .idProducto(ps.findById(car.getIdProducto().getIdProducto()))
                        .idVariante(car.getIdVariante())
                        .cantidad(car.getCantidad())
                        .descuento(descuentoLinea)
                        .pu(car.getPunitario())
                        .subtotal(car.getPtotal())
                        .build());
            }
            // Una sola transaccion: venta + detalles + descuento de stock.
            // Si falta stock, no se guarda nada y el carrito queda intacto.
            Venta idX = daoV.registrarVentaConDetalles(to, vdList);
            daoC.deleteCarAll(dniRuc.getText());
            listar();
            limpiarFormVenta();
            idX.setDetalleVenta(vdList);

            Comprobante comprobante = daoV.generarComprobante(idX);
            if(comprobante!=null && !dd.isEmpty()) {
                Toast.showToast(stage, "Venta registrada correctamente.", 2000, with, 50);
                mostrarVisorComprobante(comprobante);
                try {
                    print(Long.parseLong(String.valueOf(idX.getId())));
                } catch (Exception e) {
                    // La venta ya quedo registrada: si no hay impresora, solo se avisa.
                    Toast.showToast(stage, "Venta registrada. No se pudo imprimir (sin impresora).", 3000, with, 50);
                }
            }else{
                Toast.showToast(stage, "Error al generar comprobante", 2000, with, 50);
            }
        } catch (IllegalStateException | IllegalArgumentException e) {
            Toast.showToast(stage, e.getMessage(), 3500, with, 50);
        } catch (Exception e){
            Toast.showToast(stage, "No se pudo registrar la venta.", 2500, with, 50);
        }
    }


    public void mostrarVisorComprobante(Comprobante boleta) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/view/main_comprobante.fxml"));
            Parent visorRoot = loader.load();
            MainComprobanteController controller = loader.getController();
            controller.pasarDatos(boleta);
            Alert alert = new Alert(Alert.AlertType.NONE);
            alert.setTitle("Visualizar Comprobante");
            alert.setHeaderText(null);
            if (visorRoot instanceof javafx.scene.layout.Region) {
                ((javafx.scene.layout.Region) visorRoot).setPrefSize(800, 600);
            }
            StackPane stackPane = new StackPane(visorRoot);
            alert.getDialogPane().setContent(stackPane);
            ButtonType closeButton = new ButtonType("Cerrar");
            alert.getButtonTypes().add(closeButton);
            alert.setOnCloseRequest(event -> alert.close());
            alert.showAndWait().ifPresent(response -> {
                if (response == closeButton) {
                    alert.close();
                }
            });
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    public void print(Long idv){
        Venta vt=daoV.findById(idv);
        try {
            PrinterManager printerManager = PrinterManager.getInstance();
            PrintService printService = printerManager.getPrintService();
            PrinterOutputStream printerOutputStream = new PrinterOutputStream(printService);
            EscPos escpos = new EscPos(printerOutputStream);
            Style titleStyle = new Style()
                    .setJustification(EscPosConst.Justification.Center)
                    .setBold(true)
                    .setFontSize(Style.FontSize._2, Style.FontSize._2);
            Style normal = new Style()
                    .setJustification(EscPosConst.Justification.Left_Default);
            Style center = new Style()
                    .setJustification(EscPosConst.Justification.Center);
            escpos.writeLF(titleStyle, "BOLETA DE VENTA");
            escpos.writeLF(center, "Tienda Demo S.A.C.");
            escpos.writeLF(center, "RUC: 12345678901");
            escpos.writeLF(center, "Av. Principal 123 - Lima");
            escpos.writeLF(center, "--------------------------------");
//Datos del cliente
            escpos.writeLF(normal, "Cliente: "+vt.getDniruc().getNombres());
            escpos.writeLF(normal, "DNI: "+vt.getDniruc().getDniruc());
            escpos.writeLF(normal, "Fecha: "+vt.getFechaGener()+"");
            escpos.writeLF(normal, "--------------------------------");
            //Detalle
            escpos.writeLF(normal, "Cant Descripción Importe");
            int x=1;
            for(VentaDetalle vd:vt.getDetalleVenta()) {
                String desc = vd.getIdProducto().getNombre();
                if (vd.getIdVariante() != null) {
                    desc += " (" + vd.getIdVariante().getDescripcionCorta() + ")";
                }
                String punit=x+" "+vd.getCantidad()+" "+desc+" S/ "+vd.getSubtotal()+"";
                escpos.writeLF(normal, punit);
            }
            escpos.writeLF(normal, "--------------------------------");
            escpos.writeLF(normal, "TOTAL: S/"+vt.getPrecioTotal()+"");
            escpos.writeLF(normal, "--------------------------------");
            //Agregar QR con los datos principales
            String qrData = "Boleta N°001-000123 | Total: S/ "+vt.getPrecioTotal()+" | Fecha: "+vt.getFechaGener();
            QRCode qrCode = new QRCode()
                    .setJustification(EscPosConst.Justification.Center)
                    .setErrorCorrectionLevel(QRCode.QRErrorCorrectionLevel.QR_ECLEVEL_M_Default)
                    .setModel(QRCode.QRModel._1_Default);
            escpos.write(qrCode, qrData);
            escpos.feed(2);
            escpos.writeLF(center, "Gracias por su compra!");
            escpos.feed(6);
            //Corte total
            escpos.cut(EscPos.CutMode.FULL);
            escpos.close();
            System.out.println("Boleta impresa correctamente.");
            System.out.println("Impresión completada correctamente.");
        } catch (IOException e) {
            System.err.println("Error al inicializar la impresora: " + e.getMessage());
        }
    }


}