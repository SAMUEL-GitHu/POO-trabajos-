package pe.edu.upeu.sysventas.repository.servise;

import java.util.List;

public interface ICrudGenericoService<T,ID>{
    T save(T t);
    T update(ID id,T t);
    List<T> findALL();
    T findById(ID id);
    void delete(ID id);
}
