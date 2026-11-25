package pe.edu.upeu.sysventas.service.impl;

import pe.edu.upeu.sysventas.model.ProductoVariante;
import pe.edu.upeu.sysventas.repository.ICrudGenericoRepository;
import pe.edu.upeu.sysventas.repository.ProductoRepository;
import pe.edu.upeu.sysventas.repository.ProductoVarianteRepository;
import pe.edu.upeu.sysventas.service.IProductoVarianteService;

import java.util.List;

public class ProductoVarianteServiceImp extends CrudGenericoServiceImp<ProductoVariante, Long>
        implements IProductoVarianteService {

    private final ProductoVarianteRepository repo;
    private final ProductoRepository productoRepository;

    public ProductoVarianteServiceImp(ProductoVarianteRepository repo,
                                      ProductoRepository productoRepository) {
        this.repo = repo;
        this.productoRepository = productoRepository;
    }

    @Override
    protected ICrudGenericoRepository<ProductoVariante, Long> getRepo() {
        return repo;
    }

    @Override
    public List<ProductoVariante> listarPorProducto(Long idProducto) {
        return repo.listarPorProducto(idProducto);
    }

    @Override
    public ProductoVariante update(Long id, ProductoVariante variante) {
        ProductoVariante actualizado = super.update(id, variante);
        productoRepository.recalcularStockTotal(variante.getIdProducto().getIdProducto());
        return actualizado;
    }

    @Override
    public void delete(Long id) {
        ProductoVariante variante = repo.findById(id).orElse(null);
        super.delete(id);
        if (variante != null) {
            productoRepository.recalcularStockTotal(
                    variante.getIdProducto().getIdProducto());
        }
    }

    @Override
    public ProductoVariante registrarVariante(ProductoVariante variante) {
        if (variante.getStock() == null || variante.getStock() < 0) {
            throw new IllegalArgumentException("El stock de la variante no puede ser negativo");
        }
        Long idProducto = variante.getIdProducto().getIdProducto();
        ProductoVariante resultado = repo.buscarPorProductoTallaColor(
                        idProducto,
                        variante.getIdTalla().getIdTalla(),
                        variante.getIdColor().getIdColor())
                .map(existente -> {
                    // Ya existe esa talla+color: se suma el stock, no se duplica.
                    existente.setStock(existente.getStock() + variante.getStock());
                    return repo.update(existente);
                })
                .orElseGet(() -> repo.save(variante));
        productoRepository.recalcularStockTotal(idProducto);
        return resultado;
    }
}
