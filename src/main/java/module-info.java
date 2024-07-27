module org.nexus.indexador {
    requires javafx.controls;
    requires javafx.fxml;

    opens org.nexus.indexador to javafx.fxml;
    exports org.nexus.indexador;
    exports org.nexus.indexador.controllers;
    opens org.nexus.indexador.controllers to javafx.fxml;
}