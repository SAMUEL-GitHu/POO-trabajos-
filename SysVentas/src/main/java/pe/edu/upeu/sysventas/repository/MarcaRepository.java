package pe.edu.upeu.sysventas.repository;

import pe.edu.upeu.sysventas.model.Marca;


public class MarcaRepository extends AbstractJpaRepository<Marca, Long>{

    private long sequense=1;

    @Override
    protected Long getId(Marca entity) {
        return entity.getIdMarca();
    }

    @Override
    protected void setId(Marca entity, Long id) {
    entity.setIdMarca(id);
    }

    @Override
    protected Long generateId() {
        return sequense++;
    }
}
