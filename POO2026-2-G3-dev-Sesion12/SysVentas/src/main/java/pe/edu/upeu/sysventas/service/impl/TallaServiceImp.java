package pe.edu.upeu.sysventas.service.impl;

import pe.edu.upeu.sysventas.dto.ComboBoxOption;
import pe.edu.upeu.sysventas.model.Talla;
import pe.edu.upeu.sysventas.repository.ICrudGenericoRepository;
import pe.edu.upeu.sysventas.repository.TallaRepository;
import pe.edu.upeu.sysventas.service.ITallaService;

import java.util.ArrayList;
import java.util.List;

public class TallaServiceImp extends CrudGenericoServiceImp<Talla, Long>
        implements ITallaService {

    private final TallaRepository repo;

    public TallaServiceImp(TallaRepository repo) {
        this.repo = repo;
    }

    @Override
    protected ICrudGenericoRepository<Talla, Long> getRepo() {
        return repo;
    }

    @Override
    public List<ComboBoxOption> listarCombobox() {
        List<ComboBoxOption> listar = new ArrayList<>();
        for (Talla t : repo.findAll()) {
            ComboBoxOption cb = new ComboBoxOption();
            cb.setKey(String.valueOf(t.getIdTalla()));
            cb.setValue(t.getNombre());
            listar.add(cb);
        }
        return listar;
    }
}
