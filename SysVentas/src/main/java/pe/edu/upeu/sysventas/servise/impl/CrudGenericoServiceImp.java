package pe.edu.upeu.sysventas.servise.impl;

import pe.edu.upeu.sysventas.exception.ModelNotFoundException;
import pe.edu.upeu.sysventas.repository.ICrudGenericoRepository;
import pe.edu.upeu.sysventas.servise.ICrudGenericoService;

import java.util.List;

public abstract class CrudGenericoServiceImp<T,ID> implements ICrudGenericoService<T,ID> {
    protected abstract ICrudGenericoRepository<T, ID> getRepo();

    @Override
    public T save(T t) {
        return getRepo().save(t);
    }

    @Override
    public T update(ID id, T t) {
        if (!getRepo().existById(id)) {
            throw new ModelNotFoundException("ID no existe " + id);
        }
        return getRepo().update(t);
    }

    @Override
    public List<T> findALL() {
        return getRepo().findALL();
    }

    @Override
    public T findById(ID id) {
        return getRepo().findById(id).orElseThrow(() -> new ModelNotFoundException("ID no existe " + id));
    }

    @Override
    public void delete(ID id) {
        if (!getRepo().existById(id)) {
            throw new ModelNotFoundException("ID no existe: " + id);
        }
        getRepo().deleteById(id);
    }
}
