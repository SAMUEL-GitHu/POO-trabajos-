package pe.edu.upeu.sysventas.repository.servise.impl;

import pe.edu.upeu.sysventas.model.Categoria;
import pe.edu.upeu.sysventas.repository.CategoriaRepository;
import pe.edu.upeu.sysventas.repository.ICrudGenericoRepository;
import pe.edu.upeu.sysventas.repository.servise.ICategoriaService;
import pe.edu.upeu.sysventas.repository.servise.ICrudGenericoService;

public class CategoriaServiceImp extends CrudGenericoServiceImp<Categoria, Long> implements ICategoriaService {


    private final CategoriaRepository categoriaRepository;

    public CategoriaServiceImp(CategoriaRepository categoriaRepository) {
        this.categoriaRepository = categoriaRepository;
    }

    @Override
    protected ICrudGenericoRepository<Categoria, Long> getRepo() {
        return categoriaRepository;
    }
}
