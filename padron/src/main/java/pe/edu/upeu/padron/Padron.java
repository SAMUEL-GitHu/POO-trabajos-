package pe.edu.upeu.padron;

public class Padron {
    private int folio;
    private String nombreCompleto;
    private String rfc;
    private String seccionElectoral;
    private String distrito;

    public Padron(int folio, String nombreCompleto, String rfc, String seccionElectoral, String distrito) {
        this.folio = folio;
        this.nombreCompleto = nombreCompleto;
        this.rfc = rfc;
        this.seccionElectoral = seccionElectoral;
        this.distrito = distrito;
    }

    public int getFolio() { return folio; }
    public void setFolio(int folio) { this.folio = folio; }
    public String getNombreCompleto() { return nombreCompleto; }
    public void setNombreCompleto(String nombreCompleto) { this.nombreCompleto = nombreCompleto; }
    public String getRfc() { return rfc; }
    public void setRfc(String rfc) { this.rfc = rfc; }
    public String getSeccionElectoral() { return seccionElectoral; }
    public void setSeccionElectoral(String seccionElectoral) { this.seccionElectoral = seccionElectoral; }
    public String getDistrito() { return distrito; }
    public void setDistrito(String distrito) { this.distrito = distrito; }
}
