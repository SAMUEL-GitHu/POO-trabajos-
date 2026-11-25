package pe.edu.upeu.sysventas.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CompraDetalle {
    private Long idCompraDetalle;
    private Double pu;
    /** Cantidad de prendas: entera, una prenda no se compra en fraccion. */
    private Integer cantidad;
    private Double subtotal;
    private Compra idCompra;
    private Producto idProducto;
    /** Variante exacta comprada (talla + color): es la que repone stock. */
    private ProductoVariante idVariante;
}