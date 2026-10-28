module ParadigmBreach {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.media;
    requires org.yaml.snakeyaml;

    opens cmsc13.ParadigmBreach to javafx.fxml;
    opens cmsc13.ParadigmBreach.view to javafx.fxml;
    opens cmsc13.ParadigmBreach.model.questionBank to org.yaml.snakeyaml;

    exports cmsc13.ParadigmBreach;
    exports cmsc13.ParadigmBreach.view;
}
