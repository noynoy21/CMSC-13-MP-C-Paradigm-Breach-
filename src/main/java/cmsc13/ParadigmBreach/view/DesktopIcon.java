package cmsc13.ParadigmBreach.view;

import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;

public class DesktopIcon extends VBox {
    public DesktopIcon(String appName, String imagePath, Runnable onOpen) {
        ImageView iconImage = new ImageView(new Image(DesktopIcon.class.getResource(imagePath).toExternalForm()));
        iconImage.setFitHeight(48);
        iconImage.setPreserveRatio(true);

        Label appLabel = new Label(appName);
        
        getChildren().addAll(iconImage, appLabel);
        getStyleClass().add("desktop-icon");
        setAlignment(Pos.CENTER);
        setSpacing(4);
        setPrefWidth(90);
        setFocusTraversable(true);

        setOnMouseClicked(e -> {
            requestFocus();                   // single click = select
            if (e.getClickCount() == 2) {     // double click = open
                onOpen.run();
            }
        });
    }
}
