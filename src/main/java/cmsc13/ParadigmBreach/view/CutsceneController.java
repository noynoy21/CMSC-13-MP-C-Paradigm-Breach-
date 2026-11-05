package cmsc13.ParadigmBreach.view;

import javafx.animation.FadeTransition;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class CutsceneController {

    @FXML private StackPane root;
    @FXML private ImageView cutsceneImage;
    @FXML private Label cutsceneText;

    private Runnable onComplete;
    private boolean leaving = false;

    private final EventHandler<KeyEvent> keyHandler = e -> finishCutscene();
    private final EventHandler<MouseEvent> mouseHandler = e -> finishCutscene();

    public void initData(String imagePath, String text, Runnable onComplete) {
        this.onComplete = onComplete;

        if (imagePath != null && !imagePath.isEmpty()) {
            try {
                cutsceneImage.setImage(new Image(getClass().getResourceAsStream(imagePath)));
            } catch (Exception e) {
                System.err.println("Failed to load cutscene image: " + imagePath);
            }
        }
        cutsceneText.setText(text);

        root.sceneProperty().addListener((obs, oldScene, scene) -> {
            if (scene != null) {
                scene.addEventFilter(KeyEvent.KEY_PRESSED, keyHandler);
                scene.addEventFilter(MouseEvent.MOUSE_PRESSED, mouseHandler);
            }
        });
    }

    private void finishCutscene() {
        if (leaving) return;
        leaving = true;

        Scene scene = root.getScene();
        if (scene != null) {
            scene.removeEventFilter(KeyEvent.KEY_PRESSED, keyHandler);
            scene.removeEventFilter(MouseEvent.MOUSE_PRESSED, mouseHandler);
        }

        FadeTransition ft = new FadeTransition(Duration.seconds(0.5), root);
        ft.setToValue(0);
        ft.setOnFinished(e -> {
            root.getScene().getWindow().hide();
            if (onComplete != null) {
                onComplete.run();
            }
        });
        ft.play();
    }
}