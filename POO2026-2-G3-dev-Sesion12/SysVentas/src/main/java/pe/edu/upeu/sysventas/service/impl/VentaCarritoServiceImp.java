package pe.edu.upeu.sysventas.service.impl;

import pe.edu.upeu.sysventas.exception.ModelNotFoundException;
import pe.edu.upeu.sysventas.model.ProductoVariante;
import pe.edu.upeu.sysventas.model.VentCarrito;
import pe.edu.upeu.sysventas.repository.ICrudGenericoRepository;
import pe.edu.upeu.sysventas.repository.ProductoVarianteRepository;
import pe.edu.upeu.sysventas.repository.VentCarritoRepository;
import pe.edu.upeu.sysventas.service.IVentaCarritoService;

import java.util.List;

public class VentaCarritoServiceImp extends CrudGenericoServiceImp<VentCarrito, Long>
        implements IVentaCarritoService {

    private final VentCarritoRepository ventCarritoRepository;
    private final ProductoVarianteRepository varianteRepository;

    public VentaCarritoServiceImp(VentCarritoRepository ventCarritoRepository,
                                  ProductoVarianteRepository varianteRepository) {
        this.ventCarritoRepository = ventCarritoRepository;
        this.varianteRepository = varianteRepository;
    }

    @Override
    protected ICrudGenericoRepository<VentCarrito, Long> getRepo() {
        return ventCarritoRepository;
    }

    @Override
    public List<VentCarrito> listaCarritoCliente(String dni) {
        return ventCarritoRepository.listaCarritoCliente(dni);
    }

    @Override
    public void deleteCarAll(String dniruc) {
        ventCarritoRepository.deleteByDniruc(dniruc);
    }

    /**
     * Un cliente, un producto, una variante = UNA sola linea en el carrito.
     *
     * <p>Antes cada "Add" insertaba una linea nueva, asi que agregar 1 polo
     * y luego 2 mas del mismo talle y color mostraba dos lineas (1 y 2) en
     * vez de una sola con 3. Ahora se busca la linea que ya existe y se le
     * suma la cantidad. La base de datos lo respalda con el UNIQUE
     * (dniruc, id_producto, id_variante) de la tabla vent_carrito.</p>
     */
    @Override
    public VentCarrito agregarAlCarrito(VentCarrito item) {
        if (item.getCantidad() == null || item.getCantidad() <= 0) {
            throw new IllegalArgumentException("La cantidad debe ser mayor que cero");
        }
        if (item.getIdVariante() == null || item.getIdVariante().getIdVariante() == null) {
            throw new IllegalArgumentException(
                    "Falta la variante (talla/color) del producto");
        }
        // El stock manda es el de la variante en la base de datos, no lo que
        // muestra la pantalla (que puede estar desactualizado).
        ProductoVariante variante = varianteRepository
                .findById(item.getIdVariante().getIdVariante())
                .orElseThrow(() -> new ModelNotFoundException("Variante no encontrada"));
        if (item.getCantidad() > variante.getStock()) {
            throw new IllegalStateException("Stock insuficiente: solo quedan "
                    + variante.getStock() + " de " + variante.getDescripcion() + ".");
        }
        for (VentCarrito linea : listaCarritoCliente(item.getDniruc())) {
            if (linea.getIdProducto().getIdProducto().equals(
                        item.getIdProducto().getIdProducto())
                    && linea.getIdVariante().getIdVariante().equals(
                        item.getIdVariante().getIdVariante())) {
                int nuevaCantidad = linea.getCantidad() + item.getCantidad();
                if (nuevaCantidad > variante.getStock()) {
                    throw new IllegalStateException("Ya tienes " + linea.getCantidad()
                            + " en el carrito y solo quedan " + variante.getStock()
                            + " de " + variante.getDescripcion()
                            + ". Puedes agregar "
                            + (variante.getStock() - linea.getCantidad()) + " mas.");
                }
                // El precio es el de la primera vez que se agrego la linea.
                linea.setCantidad(nuevaCantidad);
                linea.setPtotal(linea.getPunitario() * nuevaCantidad);
                return update(linea.getIdCarrito(), linea);
            }
        }
        return save(item);
    }
}
