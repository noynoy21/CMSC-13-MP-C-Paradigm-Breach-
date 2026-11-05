package cmsc13.ParadigmBreach.view;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import cmsc13.ParadigmBreach.audio.AudioManager;
import cmsc13.ParadigmBreach.audio.MusicTrack;
import cmsc13.ParadigmBreach.ui.SceneManager;
import cmsc13.ParadigmBreach.ui.Screen;
import javafx.animation.Animation;
import javafx.animation.FadeTransition;
import javafx.animation.Interpolator;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class SplashArtController {

    @FXML private StackPane root;
    @FXML private ImageView ParadigmBreachTitle;
    // @FXML private Label prompt;

    private final List<Animation> animations = new ArrayList<>();
    private PauseTransition blinkWait;
    private boolean leaving = false;
    private boolean stopped = false;
    private final EventHandler<KeyEvent> keyHandler = e -> leave();
    private final EventHandler<MouseEvent> mouseHandler = e -> leave();

    @FXML
    private void initialize() {
        floatUpDown(ParadigmBreachTitle, 24, 2);
        blinkHard(ParadigmBreachTitle, 1);

        root.setFocusTraversable(true);

        // Case 1: the scene already exists. Case 2: it gets attached later.
        if (root.getScene() != null) {
            attach(root.getScene());
        }
        root.sceneProperty().addListener((obs, oldScene, scene) -> {
            if (scene != null) attach(scene);
        });

        
        AudioManager.get().playMusic(MusicTrack.SPLASH_SCREEN);
    }

    private void attach(Scene scene) {
        // Filters run before any node can consume the event
        scene.addEventFilter(KeyEvent.KEY_PRESSED, keyHandler);
        scene.addEventFilter(MouseEvent.MOUSE_PRESSED, mouseHandler);
        Platform.runLater(root::requestFocus);
    }

    private void leave() {
        double fadeoutDur = 0.6;

        if (leaving) return;
        leaving = true;

        Scene scene = root.getScene();
        scene.removeEventFilter(KeyEvent.KEY_PRESSED, keyHandler);
        scene.removeEventFilter(MouseEvent.MOUSE_PRESSED, mouseHandler);

        FadeTransition out = new FadeTransition(Duration.seconds(fadeoutDur), root);
        out.setToValue(0);
        out.setOnFinished(e -> {
            stopAll();
            SceneManager.get().openNameInputWindow(
                Screen.NAME_INPUT,
                () -> SceneManager.get().openCutsceneWindow(
                    Screen.CUTSCENE,
                    List.of(new cmsc13.ParadigmBreach.model.CutsceneSlide(
                        "/images/splash_arts/ascii-art-text.png",
                        "Good evening, " + cmsc13.ParadigmBreach.model.PlayerSession.getPlayerName() + "!"
                    )),
                    () -> SceneManager.get().showDesktop()
                )
            );
        });
        out.play();

        AudioManager.get().fadeOutMusic(fadeoutDur);
    }

    private void stopAll() {
        stopped = true;
        animations.forEach(Animation::stop);
        if (blinkWait != null) blinkWait.stop();
    }

    // ---------- animations ----------

    private void floatUpDown(Node n, double distance, double seconds) {
        TranslateTransition t = new TranslateTransition(Duration.seconds(seconds), n);
        t.setByY(-distance);
        t.setInterpolator(Interpolator.EASE_BOTH);
        t.setAutoReverse(true);
        t.setCycleCount(Animation.INDEFINITE);
        t.play();
        animations.add(t);
    }

    private void blinkHard(Node n, double seconds) {
        Timeline tl = new Timeline(new KeyFrame(Duration.seconds(seconds),
                e -> n.setVisible(!n.isVisible())));
        tl.setCycleCount(Animation.INDEFINITE);
        tl.play();
        animations.add(tl);
    }

    private Image load(String path) {
        URL url = SplashArtController.class.getResource(path);
        if (url == null) {
            System.err.println("Missing splash layer: " + path);
            return null;                           // ImageView just shows nothing
        }
        return new Image(url.toExternalForm());
    }
}