module AIR {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    opens Environment.Controllers to javafx.fxml, javafx.graphics, javafx.controls, java.sql;
    exports Environment.Controllers;
}