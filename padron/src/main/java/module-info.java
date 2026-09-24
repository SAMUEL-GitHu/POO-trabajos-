module pe.edu.upeu.padron {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;

    exports pe.edu.upeu.padron;
    opens pe.edu.upeu.padron to javafx.fxml;
}
