module com.mycompany.minesweeper {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.base;

    opens com.mycompany.minesweeper to javafx.fxml;
    exports com.mycompany.minesweeper;
    requires javafx.media;
}
