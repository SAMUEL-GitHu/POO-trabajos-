package pe.edu.upeu.sysventas.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import pe.edu.upeu.sysventas.enums.TipoProducto;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VentCarrito {

    private Long idCarrito;
    private String dniruc;
    private Producto idProducto;
    /** Variante elegida (talla + color): es la que descuenta stock al vender. */
    private ProductoVariante idVariante;
    private String nombreProducto;
    /** Cantidad de prendas: entera, una prenda no se vende en fraccion. */
    private Integer cantidad;
    private Double punitario;
    private Double ptotal;
    private int estado;
    private Usuario idUsuario;
    private TipoProducto tipoProducto;
}