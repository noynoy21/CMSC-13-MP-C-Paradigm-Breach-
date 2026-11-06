package cmsc13.ParadigmBreach;

import java.io.IOException;
import java.io.UncheckedIOException;

import cmsc13.ParadigmBreach.audio.AudioManager;
import cmsc13.ParadigmBreach.ui.SceneManager;
import cmsc13.ParadigmBreach.ui.Screen;
import cmsc13.ParadigmBreach.view.TerminalController;
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
        primaryStage.setOnCloseRequest(event -> {
            TerminalController terminalController = SceneManager.get().getSingleController(Screen.TERMINAL);
            if (terminalController != null) {
                terminalController.handleDesktopCloseRequest(event);
            } else {
                event.consume();
                exitImmediately();
            }
        });
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

    /** Ends the process without entering the affected JavaFX window teardown path. */
    public static void exitImmediately() {
        AudioManager.get().stopMusic();
        System.exit(0);
    }
}
