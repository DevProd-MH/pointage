module com.pointage.pointage {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires jackcess;

    opens com.pointage.pointage to javafx.fxml;
    exports com.pointage.pointage;
}