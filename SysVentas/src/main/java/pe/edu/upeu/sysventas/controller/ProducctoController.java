package pe.edu.upeu.sysventas.controller;

import lombok.RequiredArgsConstructor;
import pe.edu.upeu.sysventas.servise.ICategoriaService;
import pe.edu.upeu.sysventas.servise.IMarcaService;
import pe.edu.upeu.sysventas.servise.IProductoService;
import pe.edu.upeu.sysventas.servise.IUnidadMedidaService;

@RequiredArgsConstructor
public class ProducctoController {
    private final IMarcaService ms;
    private final ICategoriaService cs;
    private final IProductoService ps;
    private final IUnidadMedidaService us;

}
