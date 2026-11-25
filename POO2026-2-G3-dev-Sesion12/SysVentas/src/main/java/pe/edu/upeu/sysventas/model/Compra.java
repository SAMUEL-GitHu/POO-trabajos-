package pe.edu.upeu.sysventas.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.util.List;

/**
 * Compra a un proveedor (hereda de DocumentoComercial, igual que Venta).
 *
 * <p>Correccion Sesion 12: antes usaba {@code @Builder} sin
 * {@code @SuperBuilder} y sin {@code @Data}, asi que el builder no
 * incluia los campos heredados (id, precioBase, igv, precioTotal, serie,
 * numDoc, tipoDoc) y la clase no tenia getters ni setters propios. Ahora
 * usa {@code @SuperBuilder} + {@code @Data}, igual que {@link Venta}.</p>
 */
@Data
@SuperBuilder
@NoArgsConstructor
@AllArgsConstructor
public class Compra extends DocumentoComercial {

    private Proveedor idProveedor;
    private Usuario idUsuario;
    private LocalDate fechaComp;
    private LocalDate fechaReg;
    private List<CompraDetalle> detalleCompra;

    @Override
    public double calcularTotal() {
        // precioTotal YA incluye el IGV (base + igv): no se vuelve a sumar.
        return getPrecioTotal();
    }
}
