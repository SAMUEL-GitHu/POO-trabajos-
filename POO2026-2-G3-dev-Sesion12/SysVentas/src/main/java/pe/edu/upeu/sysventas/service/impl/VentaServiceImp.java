package pe.edu.upeu.sysventas.service.impl;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import pe.edu.upeu.sysventas.dto.comprobante.Comprobante;
//import pe.edu.upeu.sysventas.dto.comprobante.ItemComprobante;
import pe.edu.upeu.sysventas.dto.comprobante.Comprobante;
import pe.edu.upeu.sysventas.dto.comprobante.ItemComprobante;
import pe.edu.upeu.sysventas.model.Venta;
import pe.edu.upeu.sysventas.model.VentaDetalle;
import pe.edu.upeu.sysventas.repository.ICrudGenericoRepository;
import pe.edu.upeu.sysventas.repository.ProductoRepository;
import pe.edu.upeu.sysventas.repository.ProductoVarianteRepository;
import pe.edu.upeu.sysventas.repository.VentaDetalleRepository;
import pe.edu.upeu.sysventas.repository.VentaRepository;
import pe.edu.upeu.sysventas.service.IVentaService;
import pe.edu.upeu.sysventas.utils.NumeroLetras;

import javax.sql.DataSource;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VentaServiceImp extends CrudGenericoServiceImp<Venta, Long>
        implements IVentaService {

    private static final Logger logger = LoggerFactory.getLogger(VentaServiceImp.class);

    private final VentaRepository ventaRepository;
    private final VentaDetalleRepository ventaDetalleRepository;
    private final ProductoVarianteRepository varianteRepository;
    private final ProductoRepository productoRepository;
    private final DataSource dataSource;

    public VentaServiceImp(VentaRepository ventaRepository,
                           VentaDetalleRepository ventaDetalleRepository,
                           ProductoVarianteRepository varianteRepository,
                           ProductoRepository productoRepository,
                           DataSource dataSource) {
        this.ventaRepository = ventaRepository;
        this.ventaDetalleRepository = ventaDetalleRepository;
        this.varianteRepository = varianteRepository;
        this.productoRepository = productoRepository;
        this.dataSource = dataSource;
    }

    @Override
    protected ICrudGenericoRepository<Venta, Long> getRepo() {
        return ventaRepository;
    }

    /**
     * Registra la venta completa en UNA transaccion: cabecera, detalles y
     * descuento de stock de cada variante (talla + color).
     *
     * <p>Antes la venta se guardaba en una conexion, cada detalle en otra y
     * el stock nunca se descontaba: si un detalle fallaba, la venta quedaba
     * guardada a medias y se podia vender mas de lo que habia. Ahora, si
     * algo falla (por ejemplo, stock insuficiente), se hace rollback de
     * todo y no se guarda nada.</p>
     *
     * @throws IllegalStateException si alguna variante no tiene stock suficiente
     */
    @Override
    public Venta registrarVentaConDetalles(Venta venta, List<VentaDetalle> detalles) {
        if (detalles == null || detalles.isEmpty()) {
            throw new IllegalArgumentException("La venta no tiene productos");
        }
        try (Connection conn = dataSource.getConnection()) {
            conn.setAutoCommit(false);
            try {
                // Numero correlativo: el controlador mandaba "00" fijo en todas
                // las ventas, y la tabla tiene UNIQUE(serie, num_doc, tipo_doc),
                // asi que la segunda venta fallaba. Ahora el numero se genera
                // aqui, dentro de la misma transaccion.
                venta.setNumDoc(String.format("%08d", siguienteNumeroDoc(conn, venta)));
                Venta guardada = ventaRepository.insertarEnTransaccion(conn, venta);
                for (VentaDetalle vd : detalles) {
                    if (vd.getIdVariante() == null || vd.getIdVariante().getIdVariante() == null) {
                        throw new IllegalArgumentException(
                                "Falta la variante (talla/color) del producto "
                                + vd.getIdProducto().getNombre());
                    }
                    if (vd.getCantidad() == null || vd.getCantidad() <= 0) {
                        throw new IllegalArgumentException(
                                "La cantidad debe ser mayor que cero");
                    }
                    Long idVariante = vd.getIdVariante().getIdVariante();
                    int stock = varianteRepository.stockActual(conn, idVariante);
                    if (stock < vd.getCantidad()) {
                        throw new IllegalStateException(
                                "Stock insuficiente: " + vd.getIdProducto().getNombre()
                                + " (" + vd.getIdVariante().getDescripcion() + ") tiene "
                                + stock + " y se quieren vender " + vd.getCantidad());
                    }
                    vd.setIdVenta(guardada);
                    ventaDetalleRepository.insertarEnTransaccion(conn, vd);
                    varianteRepository.descontarStock(conn, idVariante, vd.getCantidad());
                    productoRepository.recalcularStockTotal(
                            conn, vd.getIdProducto().getIdProducto());
                }
                conn.commit();
                guardada.setDetalleVenta(detalles);
                return guardada;
            } catch (Exception e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error al registrar la venta: " + e.getMessage(), e);
        }
    }

    /** Siguiente numero de documento para la serie/tipo de la venta (8 digitos). */
    private int siguienteNumeroDoc(Connection conn, Venta venta) throws SQLException {
        String sql = "SELECT COALESCE(MAX(CAST(num_doc AS INTEGER)), 0) FROM venta "
                + "WHERE serie = ? AND tipo_doc = ?";
        try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, venta.getSerie());
            ps.setString(2, venta.getTipoDoc());
            try (java.sql.ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt(1) + 1 : 1;
            }
        }
    }

    @Override
    public File getFile(String filex) {
        File newFolder = new File("jasper");
        Path camino = Paths.get(newFolder.getAbsolutePath() + "/" + filex);
        logger.debug("Ruta Jasper: {}", camino.toAbsolutePath());
        return camino.toFile();
    }

    /**
     * Arma el comprobante con los datos REALES de la venta.
     *
     * <p>Antes todo era de ejemplo y fijo: serie "B001", numero "00000042"
     * y la direccion del cliente "Av. Sangalli 1234" para todos. Ademas el
     * precio unitario se armaba con el subtotal, asi que con cantidad 2 el
     * P. UNIT salia 70.00 y el SUBTOTAL 140.00. Ahora la serie y el numero
     * son el correlativo de la venta, la direccion es la del cliente, cada
     * item lleva su talla y color, el P. UNIT es el precio real y el "SON:"
     * va en letras.</p>
     */
    @Override
    public Comprobante generarComprobante(Venta venta){
        Comprobante b = new Comprobante();
        b.setRucEmisor("10436319172");
        b.setRazonSocial("SysCenterLife S.A.C.");
        b.setDireccion("Av. La Torre  - Juliaca");
        b.setUbigeo("Puno, Juliaca, Cercado");
        b.setTelefono("951782520");

        // La venta se guarda como Factura, serie "V", con numero correlativo.
        b.setTipoDocumento("Factura".equalsIgnoreCase(venta.getTipoDoc())
                ? "FACTURA" : "BOLETA DE VENTA ELECTRÓNICA");
        b.setSerie(venta.getSerie());
        b.setNumero(venta.getNumDoc());
        b.setFechaEmision(venta.getFechaGener().toString());
        b.setMoneda("PEN");

        b.setTipoDocCliente(venta.getDniruc().getTipoDocumento().name());
        b.setNroDocCliente(venta.getDniruc().getDniruc());
        b.setNombreCliente(venta.getDniruc().getNombres());
        b.setDireccionCliente(venta.getDniruc().getDireccion());

        List<ItemComprobante> items = new ArrayList<>();
        BigDecimal descuentoTotal = BigDecimal.ZERO;

        for (VentaDetalle vd : venta.getDetalleVenta()) {
            String descripcion = vd.getIdProducto().getNombre();
            if (vd.getIdVariante() != null) {
                descripcion += " - " + vd.getIdVariante().getDescripcionCorta();
            }
            items.add(new ItemComprobante(descripcion,
                    vd.getCantidad().doubleValue(), new BigDecimal(vd.getPu())));
            if (vd.getDescuento() != null) {
                descuentoTotal = descuentoTotal.add(BigDecimal.valueOf(vd.getDescuento()));
            }
        }
        b.setItems(items);
        // Los totales se toman de la venta guardada (ya llevan el descuento
        // aplicado), asi la boleta nunca difiere de lo registrado.
        BigDecimal total = BigDecimal.valueOf(venta.getPrecioTotal())
                .setScale(2, RoundingMode.HALF_UP);
        b.setDescuento(descuentoTotal.setScale(2, RoundingMode.HALF_UP));
        b.setOpGravadas(BigDecimal.valueOf(venta.getPrecioBase())
                .setScale(2, RoundingMode.HALF_UP));
        b.setIgv(BigDecimal.valueOf(venta.getIgv()).setScale(2, RoundingMode.HALF_UP));
        b.setTotal(total);
        b.setTotalLetras(NumeroLetras.convertir(b.getTotal()));
        return b;
    }


}
