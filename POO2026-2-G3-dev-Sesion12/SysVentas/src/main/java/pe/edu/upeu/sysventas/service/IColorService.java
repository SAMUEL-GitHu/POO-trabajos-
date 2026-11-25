package pe.edu.upeu.sysventas.service;

import pe.edu.upeu.sysventas.dto.ComboBoxOption;
import pe.edu.upeu.sysventas.model.Color;

import java.util.List;

public interface IColorService extends ICrudGenericoService<Color, Long> {
    List<ComboBoxOption> listarCombobox();
}
