package pe.edu.upeu.sysventas.service;

import pe.edu.upeu.sysventas.model.VentCarrito;

import java.util.List;

public interface IVentaCarritoService extends ICrudGenericoService<VentCarrito, Long>{
    List<VentCarrito> listaCarritoCliente(String dni);
    void deleteCarAll(String dniruc);

    /**
     * Agrega un producto al carrito. Si el cliente ya tiene ese mismo
     * producto con esa misma talla y color, NO crea otra linea: suma la
     * cantidad a la linea que ya existe (y recalcula su total).
     * El precio queda fijado al de la primera vez que se agrego.
     *
     * @return la linea del carrito (nueva o actualizada)
     * @throws IllegalStateException si la cantidad total supera el stock
     *         de la variante
     */
    VentCarrito agregarAlCarrito(VentCarrito item);
}
