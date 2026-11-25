package pe.edu.upeu.sysventas.service.impl;

import pe.edu.upeu.sysventas.dto.ComboBoxOption;
import pe.edu.upeu.sysventas.model.Color;
import pe.edu.upeu.sysventas.repository.ColorRepository;
import pe.edu.upeu.sysventas.repository.ICrudGenericoRepository;
import pe.edu.upeu.sysventas.service.IColorService;

import java.util.ArrayList;
import java.util.List;

public class ColorServiceImp extends CrudGenericoServiceImp<Color, Long>
        implements IColorService {

    private final ColorRepository repo;

    public ColorServiceImp(ColorRepository repo) {
        this.repo = repo;
    }

    @Override
    protected ICrudGenericoRepository<Color, Long> getRepo() {
        return repo;
    }

    @Override
    public List<ComboBoxOption> listarCombobox() {
        List<ComboBoxOption> listar = new ArrayList<>();
        for (Color c : repo.findAll()) {
            ComboBoxOption cb = new ComboBoxOption();
            cb.setKey(String.valueOf(c.getIdColor()));
            cb.setValue(c.getNombre());
            listar.add(cb);
        }
        return listar;
    }
}
