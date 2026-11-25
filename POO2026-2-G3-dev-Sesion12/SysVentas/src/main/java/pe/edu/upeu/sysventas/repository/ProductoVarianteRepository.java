package pe.edu.upeu.sysventas.repository;

import pe.edu.upeu.sysventas.model.Color;
import pe.edu.upeu.sysventas.model.Producto;
import pe.edu.upeu.sysventas.model.ProductoVariante;
import pe.edu.upeu.sysventas.model.Talla;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio de {@code producto_variante}: un producto en una talla y
 * un color, con su propio stock.
 */
public class ProductoVarianteRepository extends AbstractJpaRepository<ProductoVariante, Long> {

    private static final String SELECT_JOIN =
            "SELECT pv.*, t.nombre AS talla_nombre, c.nombre AS color_nombre, " +
            "c.codigo_hex AS color_hex, p.nombre AS prod_nombre " +
            "FROM producto_variante pv " +
            "JOIN talla t ON pv.id_talla = t.id_talla " +
            "JOIN color c ON pv.id_color = c.id_color " +
            "JOIN producto p ON pv.id_producto = p.id_producto ";

    @Override
    protected String getTableName() {
        return "producto_variante";
    }

    @Override
    protected String getPkColumn() {
        return "id_variante";
    }

    @Override
    public List<ProductoVariante> findAll() {
        return executeQuery(SELECT_JOIN + "ORDER BY p.nombre, t.nombre, c.nombre");
    }

    @Override
    public Optional<ProductoVariante> findById(Long id) {
        return executeQueryOne(SELECT_JOIN + "WHERE pv.id_variante = ?", id);
    }

    /** Variantes de un producto, ordenadas por talla y color. */
    public List<ProductoVariante> listarPorProducto(Long idProducto) {
        return executeQuery(SELECT_JOIN + "WHERE pv.id_producto = ? " +
                "ORDER BY t.nombre, c.nombre", idProducto);
    }

    /** Busca la variante de un producto en una talla y un color dados. */
    public Optional<ProductoVariante> buscarPorProductoTallaColor(Long idProducto,
                                                                    Long idTalla,
                                                                    Long idColor) {
        return executeQueryOne(SELECT_JOIN +
                "WHERE pv.id_producto = ? AND pv.id_talla = ? AND pv.id_color = ?",
                idProducto, idTalla, idColor);
    }

    @Override
    protected ProductoVariante mapRow(ResultSet rs) throws SQLException {
        return ProductoVariante.builder()
                .idVariante(rs.getLong("id_variante"))
                .idProducto(Producto.builder()
                        .idProducto(rs.getLong("id_producto"))
                        .nombre(rs.getString("prod_nombre")).build())
                .idTalla(Talla.builder()
                        .idTalla(rs.getLong("id_talla"))
                        .nombre(rs.getString("talla_nombre")).build())
                .idColor(Color.builder()
                        .idColor(rs.getLong("id_color"))
                        .nombre(rs.getString("color_nombre"))
                        .codigoHex(rs.getString("color_hex")).build())
                .stock(rs.getInt("stock"))
                .build();
    }

    @Override
    protected ProductoVariante insert(Connection conn, ProductoVariante e) throws SQLException {
        long id = executeInsertGetKey(conn,
                "INSERT INTO producto_variante(id_producto, id_talla, id_color, stock) " +
                "VALUES(?, ?, ?, ?)",
                e.getIdProducto().getIdProducto(),
                e.getIdTalla().getIdTalla(),
                e.getIdColor().getIdColor(),
                e.getStock());
        e.setIdVariante(id);
        return e;
    }

    @Override
    protected ProductoVariante updateRow(Connection conn, ProductoVariante e) throws SQLException {
        executeUpdate(conn,
                "UPDATE producto_variante SET id_producto=?, id_talla=?, id_color=?, " +
                "stock=? WHERE id_variante=?",
                e.getIdProducto().getIdProducto(),
                e.getIdTalla().getIdTalla(),
                e.getIdColor().getIdColor(),
                e.getStock(), e.getIdVariante());
        return e;
    }

    /**
     * Descuenta stock de una variante dentro de una transaccion ya abierta.
     * La condicion {@code stock >= ?} evita vender mas de lo que hay aunque
     * dos ventas lleguen a la vez: si no queda stock, no se actualiza
     * ninguna fila y se lanza la excepcion.
     */
    public void descontarStock(Connection conn, Long idVariante, int cantidad)
            throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE producto_variante SET stock = stock - ? " +
                "WHERE id_variante = ? AND stock >= ?")) {
            ps.setInt(1, cantidad);
            ps.setLong(2, idVariante);
            ps.setInt(3, cantidad);
            if (ps.executeUpdate() == 0) {
                throw new SQLException(
                        "Stock insuficiente en la variante " + idVariante);
            }
        }
    }

    /** Stock actual de una variante (lectura dentro de una transaccion). */
    public int stockActual(Connection conn, Long idVariante) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT stock FROM producto_variante WHERE id_variante = ?")) {
            ps.setLong(1, idVariante);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("stock");
                }
                throw new SQLException("No existe la variante " + idVariante);
            }
        }
    }
}
