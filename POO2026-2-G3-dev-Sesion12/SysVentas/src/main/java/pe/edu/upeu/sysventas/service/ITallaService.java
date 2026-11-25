package pe.edu.upeu.sysventas.service;

import pe.edu.upeu.sysventas.dto.ComboBoxOption;
import pe.edu.upeu.sysventas.model.Talla;

import java.util.List;

public interface ITallaService extends ICrudGenericoService<Talla, Long> {
    List<ComboBoxOption> listarCombobox();
}
