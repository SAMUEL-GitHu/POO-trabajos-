package pe.edu.upeu.sysventas.repository;

import pe.edu.upeu.sysventas.model.Color;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ColorRepository extends AbstractJpaRepository<Color, Long> {

    @Override
    protected String getTableName() {
        return "color";
    }

    @Override
    protected String getPkColumn() {
        return "id_color";
    }

    @Override
    protected Color mapRow(ResultSet rs) throws SQLException {
        return Color.builder()
                .idColor(rs.getLong("id_color"))
                .nombre(rs.getString("nombre"))
                .codigoHex(rs.getString("codigo_hex"))
                .build();
    }

    @Override
    protected Color insert(Connection conn, Color e) throws SQLException {
        long id = executeInsertGetKey(conn,
                "INSERT INTO color(nombre, codigo_hex) VALUES(?, ?)",
                e.getNombre(), e.getCodigoHex());
        e.setIdColor(id);
        return e;
    }

    @Override
    protected Color updateRow(Connection conn, Color e) throws SQLException {
        executeUpdate(conn,
                "UPDATE color SET nombre=?, codigo_hex=? WHERE id_color=?",
                e.getNombre(), e.getCodigoHex(), e.getIdColor());
        return e;
    }
}
