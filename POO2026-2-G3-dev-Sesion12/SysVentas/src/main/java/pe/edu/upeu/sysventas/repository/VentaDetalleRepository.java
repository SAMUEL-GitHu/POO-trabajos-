package pe.edu.upeu.sysventas.repository;

import pe.edu.upeu.sysventas.model.Producto;
import pe.edu.upeu.sysventas.model.ProductoVariante;
import pe.edu.upeu.sysventas.model.Venta;
import pe.edu.upeu.sysventas.model.VentaDetalle;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
public class VentaDetalleRepository extends AbstractJpaRepository<VentaDetalle, Long> {
    @Override protected String getTableName() { return "venta_detalle"; }
    @Override protected String getPkColumn()  { return "id_venta_detalle"; }
    @Override
    protected VentaDetalle mapRow(ResultSet rs) throws SQLException {
        Venta v = new Venta(); v.setId(rs.getLong("id_venta"));
        Producto p = new Producto(); p.setIdProducto(rs.getLong("id_producto"));
        ProductoVariante pv = null;
        long idVariante = rs.getLong("id_variante");
        if (!rs.wasNull()) {
            pv = new ProductoVariante();
            pv.setIdVariante(idVariante);
        }
        return VentaDetalle.builder().idVentaDetalle(rs.getLong("id_venta_detalle"))
                .pu(rs.getDouble("pu")).cantidad(rs.getInt("cantidad"))
                .descuento(rs.getDouble("descuento")).subtotal(rs.getDouble("subtotal"))
                .idVenta(v).idProducto(p).idVariante(pv).build();
    }
    @Override
    protected VentaDetalle insert(Connection conn, VentaDetalle e) throws SQLException {
        long id = executeInsertGetKey(conn,
                "INSERT INTO venta_detalle(pu,cantidad,descuento,subtotal,id_venta,id_producto,id_variante) VALUES(?,?,?,?,?,?,?)",
                e.getPu(),e.getCantidad(),e.getDescuento(),e.getSubtotal(),
                e.getIdVenta().getId(),e.getIdProducto().getIdProducto(),
                e.getIdVariante() != null ? e.getIdVariante().getIdVariante() : null);
        e.setIdVentaDetalle(id); return e;
    }

    /**
     * Inserta el detalle usando una conexion con transaccion ya abierta
     * (la maneja VentaServiceImp.registrarVentaConDetalles).
     */
    public VentaDetalle insertarEnTransaccion(Connection conn, VentaDetalle e)
            throws SQLException {
        return insert(conn, e);
    }
    @Override
    protected VentaDetalle updateRow(Connection conn, VentaDetalle e) throws SQLException {
        executeUpdate(conn,"UPDATE venta_detalle SET pu=?,cantidad=?,descuento=?,subtotal=?,id_venta=?,id_producto=?,id_variante=? WHERE id_venta_detalle=?",
                e.getPu(),e.getCantidad(),e.getDescuento(),e.getSubtotal(),
                e.getIdVenta().getId(),e.getIdProducto().getIdProducto(),
                e.getIdVariante() != null ? e.getIdVariante().getIdVariante() : null,
                e.getIdVentaDetalle()); return e;
    }
}
