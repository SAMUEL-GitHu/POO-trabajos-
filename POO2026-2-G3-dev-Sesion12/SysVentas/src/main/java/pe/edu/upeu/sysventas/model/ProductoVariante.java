package pe.edu.upeu.sysventas.model;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Una variante de un producto (tabla {@code producto_variante}):
 * un mismo producto en una talla y un color concretos, con su propio stock.
 *
 * <p>Ejemplo: el producto "Polo Pique Clasico" tiene la variante
 * talla M / color Negro con stock 8, y la variante talla L / color
 * Negro con stock 5. La combinacion (producto, talla, color) no se
 * repite: lo garantiza el UNIQUE de la base de datos.</p>
 *
 * <p>El stock del producto base ({@link Producto#getStock()}) es el
 * total: la suma del stock de todas sus variantes.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductoVariante {
    private Long idVariante;

    @NotNull(message = "El producto de la variante es obligatorio")
    private Producto idProducto;

    @NotNull(message = "La talla de la variante es obligatoria")
    private Talla idTalla;

    @NotNull(message = "El color de la variante es obligatorio")
    private Color idColor;

    @NotNull(message = "El stock de la variante es obligatorio")
    @PositiveOrZero(message = "El stock de la variante no puede ser negativo")
    private Integer stock;

    /** Texto para mostrar en los ComboBox: "M - Negro (stock 8)". */
    public String getDescripcion() {
        String talla = idTalla != null ? idTalla.getNombre() : "?";
        String color = idColor != null ? idColor.getNombre() : "?";
        return talla + " - " + color + " (stock " + (stock != null ? stock : 0) + ")";
    }

    /** Talla y color sin el stock: "M - Negro". Para el comprobante y el ticket. */
    public String getDescripcionCorta() {
        String talla = idTalla != null ? idTalla.getNombre() : "?";
        String color = idColor != null ? idColor.getNombre() : "?";
        return talla + " - " + color;
    }
}
