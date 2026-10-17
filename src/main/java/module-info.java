module ParadigmBreach {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.media;

    opens cmsc13.ParadigmBreach to javafx.fxml;
    opens cmsc13.ParadigmBreach.view to javafx.fxml;

    exports cmsc13.ParadigmBreach;
    exports cmsc13.ParadigmBreach.view;
}
