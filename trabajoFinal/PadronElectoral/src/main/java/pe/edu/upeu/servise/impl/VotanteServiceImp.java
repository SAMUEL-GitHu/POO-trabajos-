package pe.edu.upeu.servise.impl;

import pe.edu.upeu.model.Votante;
import pe.edu.upeu.repository.ICrudGenericoRepository;
import pe.edu.upeu.repository.VotanteRepository;
import pe.edu.upeu.servise.IVotanteService;

public class VotanteServiceImp extends CrudGenericoServiceImp<Votante, Long> implements IVotanteService {

    private final VotanteRepository votanteRepository;

    public VotanteServiceImp(VotanteRepository votanteRepository) {
        this.votanteRepository = votanteRepository;
    }

    @Override
    protected ICrudGenericoRepository<Votante, Long> getRepo() {
        return votanteRepository;
    }
}
