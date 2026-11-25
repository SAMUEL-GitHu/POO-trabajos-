package pe.edu.upeu.sysventas.service;

import pe.edu.upeu.sysventas.model.ProductoVariante;

import java.util.List;

public interface IProductoVarianteService extends ICrudGenericoService<ProductoVariante, Long> {

    /** Variantes de un producto (talla + color + stock de cada una). */
    List<ProductoVariante> listarPorProducto(Long idProducto);

    /**
     * Registra una variante. Si el producto ya tiene esa misma talla y color,
     * no crea una fila repetida: suma el stock a la variante que ya existe.
     * En ambos casos recalcula el stock total del producto.
     */
    ProductoVariante registrarVariante(ProductoVariante variante);
}
