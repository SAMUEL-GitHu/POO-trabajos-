module pe.edu.upeu {
    requires javafx.controls;
    requires javafx.fxml;

    requires static lombok;
    requires jakarta.validation;

    opens pe.edu.upeu to javafx.fxml;
    opens pe.edu.upeu.controller to javafx.fxml;
    opens pe.edu.upeu.model to javafx.base;

    exports pe.edu.upeu;
}
