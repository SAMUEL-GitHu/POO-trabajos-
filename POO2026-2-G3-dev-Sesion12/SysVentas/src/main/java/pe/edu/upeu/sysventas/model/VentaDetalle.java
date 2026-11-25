package pe.edu.upeu.sysventas.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VentaDetalle {
    private Long idVentaDetalle;
    private Double pu;
    /** Cantidad de prendas: entera, una prenda no se vende en fraccion. */
    private Integer cantidad;
    private Double descuento;
    private Double subtotal;
    private Venta idVenta;
    private Producto idProducto;
    /** Variante exacta vendida (talla + color). Sin ella no se sabria de que talla/color se vendio. */
    private ProductoVariante idVariante;
}