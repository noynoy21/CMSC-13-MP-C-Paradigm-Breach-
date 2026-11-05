package cmsc13.ParadigmBreach.view;

import java.util.List;

import cmsc13.ParadigmBreach.model.CutsceneSlide;
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

    private List<CutsceneSlide> slides;
    private int currentIndex = 0;
    private Runnable onComplete;
    private boolean leaving = false;

    private final EventHandler<KeyEvent> keyHandler = e -> handleAdvance();
    private final EventHandler<MouseEvent> mouseHandler = e -> handleAdvance();

    public void initData(List<CutsceneSlide> slides, Runnable onComplete) {
        this.slides = slides;
        this.onComplete = onComplete;
        this.currentIndex = 0;

        if (slides != null && !slides.isEmpty()) {
            showSlide(currentIndex);
        }

        root.sceneProperty().addListener((obs, oldScene, scene) -> {
            if (scene != null) {
                scene.addEventFilter(KeyEvent.KEY_PRESSED, keyHandler);
                scene.addEventFilter(MouseEvent.MOUSE_PRESSED, mouseHandler);
            }
        });
    }

    // Backward compatibility overload for single slide
    public void initData(String imagePath, String text, Runnable onComplete) {
        initData(List.of(new CutsceneSlide(imagePath, text)), onComplete);
    }

    private void showSlide(int index) {
        CutsceneSlide slide = slides.get(index);
        if (slide.imagePath() != null && !slide.imagePath().isEmpty()) {
            try {
                var url = getClass().getResource(slide.imagePath());
                if (url != null) {
                    cutsceneImage.setImage(new Image(url.toExternalForm()));
                } else {
                    cutsceneImage.setImage(new Image(getClass().getResourceAsStream(slide.imagePath())));
                }
            } catch (Exception e) {
                System.err.println("Failed to load cutscene image: " + slide.imagePath());
                cutsceneImage.setImage(null);
            }
        } else {
            cutsceneImage.setImage(null);
        }
        cutsceneText.setText(slide.text());
    }

    private void handleAdvance() {
        if (leaving) return;

        if (currentIndex < slides.size() - 1) {
            currentIndex++;
            showSlide(currentIndex);
        } else {
            finishCutscene();
        }
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