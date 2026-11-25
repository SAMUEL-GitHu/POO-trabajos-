package pe.edu.upeu.sysventas.repository;

import pe.edu.upeu.sysventas.model.Talla;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TallaRepository extends AbstractJpaRepository<Talla, Long> {

    @Override
    protected String getTableName() {
        return "talla";
    }

    @Override
    protected String getPkColumn() {
        return "id_talla";
    }

    @Override
    protected Talla mapRow(ResultSet rs) throws SQLException {
        return Talla.builder()
                .idTalla(rs.getLong("id_talla"))
                .nombre(rs.getString("nombre"))
                .build();
    }

    @Override
    protected Talla insert(Connection conn, Talla e) throws SQLException {
        long id = executeInsertGetKey(conn,
                "INSERT INTO talla(nombre) VALUES(?)",
                e.getNombre());
        e.setIdTalla(id);
        return e;
    }

    @Override
    protected Talla updateRow(Connection conn, Talla e) throws SQLException {
        executeUpdate(conn,
                "UPDATE talla SET nombre=? WHERE id_talla=?",
                e.getNombre(), e.getIdTalla());
        return e;
    }
}
