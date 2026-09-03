package pe.edu.upeu.sysventas.enums;

public enum TipoProducto {
    PRODUCTO("produccto"),
    PREPARADO("preparado"),
    SERVICIO("servicio");

    String descripcion;
    TipoProducto(String descripcion){
        this.descripcion=descripcion;
    }
}
