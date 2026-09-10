package pe.edu.upeu.sysventas.repository;

import java.util.List;
import java.util.Optional;

public interface ICrudGenericoRepository<T,ID> {
    T save(T entity);
    T update(T entity);
    Optional<T> findById(ID id);
    List<T> findALL();
    void deleteById(ID id);
    boolean existById(ID id);
}
