module pe.edu.upeu.sysventas {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires static lombok;
    requires jakarta.validation;


    requires java.sql;
    requires java.naming;
    requires org.slf4j;
    requires com.zaxxer.hikari;
    // Driver PostgreSQL (Neon). Su Automatic-Module-Name es
    // "org.postgresql.jdbc" (verificado en el MANIFEST del jar).
    requires org.postgresql.jdbc;
    requires java.prefs;
    requires org.jsoup;
    requires java.desktop;
    requires escpos.coffee;
    requires com.github.librepdf.openpdf;
    // jBCrypt 0.4 no trae module-info ni Automatic-Module-Name: su nombre
    // de modulo automatico es el del archivo (jbcrypt-0.4.jar -> "jbcrypt").
    requires jbcrypt;


    opens pe.edu.upeu.sysventas to javafx.fxml;
    opens pe.edu.upeu.sysventas.controller to javafx.fxml;
    exports pe.edu.upeu.sysventas;
    opens pe.edu.upeu.sysventas.model;
}