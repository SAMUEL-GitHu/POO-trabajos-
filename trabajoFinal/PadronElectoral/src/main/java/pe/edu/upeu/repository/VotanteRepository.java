package pe.edu.upeu.repository;

import pe.edu.upeu.model.Votante;

public class VotanteRepository extends AbstractJpaRepository<Votante, Long> {

    private long sequence = 1;

    @Override
    protected Long getId(Votante entity) {
        return entity.getIdVotante();
    }

    @Override
    protected void setId(Votante entity, Long id) {
        entity.setIdVotante(id);
    }

    @Override
    protected Long generateId() {
        return sequence++;
    }
}
