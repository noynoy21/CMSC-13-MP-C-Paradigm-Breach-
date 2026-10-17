package cmsc13.ParadigmBreach;

import java.io.IOException;
import java.io.UncheckedIOException;

import cmsc13.ParadigmBreach.audio.AudioManager;
import cmsc13.ParadigmBreach.ui.SceneManager;
import javafx.application.Application;
import javafx.scene.text.Font;
import javafx.stage.Stage;


/**
 * JavaFX App
 */
public class App extends Application {
    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("ParadigmBreach");

        SceneManager.init(primaryStage);
        AudioManager.init();
        SceneManager.get().showSplashArt();

        try (var fontStream = App.class.getResourceAsStream("/fonts/CascadiaMono.ttf")) {
            if (fontStream == null) {
                throw new IllegalStateException("Cascadia Mono font resource not found");
            }

            Font font = Font.loadFont(fontStream, 13.5);
            if (font == null) {
                throw new IllegalStateException("Cascadia Mono font could not be loaded");
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load Cascadia Mono", e);
        }
    }

    public static void main(String[] args) {
        launch();
    }
}