package pe.edu.upeu.sysventas.service;

//import pe.edu.upeu.sysventas.dto.comprobante.Comprobante;
import pe.edu.upeu.sysventas.dto.comprobante.Comprobante;
import pe.edu.upeu.sysventas.model.Venta;

import java.io.File;

public interface IVentaService extends ICrudGenericoService<Venta, Long>{
    File getFile(String filex);
    /*JasperPrint runReport(Long idv) throws JRException, SQLException;
    JasperPrint runReportVentas(String fInicio, String ffinal) throws JRException, SQLException;*/
    Comprobante generarComprobante(Venta venta);

    /**
     * Registra la venta completa en una sola transaccion (cabecera +
     * detalles + descuento de stock de cada variante). Si algo falla,
     * no se guarda nada.
     */
    Venta registrarVentaConDetalles(Venta venta,
                                    java.util.List<pe.edu.upeu.sysventas.model.VentaDetalle> detalles);

}
