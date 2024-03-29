module AIR {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires ejml.simple;
    requires ejml.ddense;
    requires ejml.core;
    opens GANAS to ejml.simple, ejml.ddense, ejml.core;
    opens RL.Controllers to javafx.fxml, javafx.graphics, javafx.controls, java.sql;
    exports RL.Controllers;
    exports GANAS;
}