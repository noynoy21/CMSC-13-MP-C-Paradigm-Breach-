package cmsc13.ParadigmBreach.ui;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.EnumMap;
import java.util.Map;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class SceneManager {

    private static SceneManager instance;

    private final Stage primaryStage;
    private final Map<Screen, Stage> openSingles = new EnumMap<>(Screen.class);
    private final Map<Screen, Object> singleControllers = new EnumMap<>(Screen.class);

    private SceneManager(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public static void init(Stage primaryStage) { instance = new SceneManager(primaryStage); }

    public static SceneManager get() {
        if (instance == null) throw new IllegalStateException("SceneManager.init(stage) was not called");
        return instance;
    }

    private record Zip(Parent root, Object controller) {}

    private Zip load(Screen screen) {
        URL url = SceneManager.class.getResource(screen.getFxmlPath());
        if (url == null) throw new IllegalArgumentException("FXML not found: " + screen.getFxmlPath());
        try {
            FXMLLoader loader = new FXMLLoader(url);
            Parent root = loader.load();
            return new Zip(root, loader.getController());
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to load " + screen, e);
        }
    }

    public void showSplashArt() {
        Zip zippedSplashArt = load(Screen.SPLASHART);
        primaryStage.setTitle(Screen.DESKTOP.getTitle());
        primaryStage.setScene(new Scene(zippedSplashArt.root, Screen.SPLASHART.getWidth(), Screen.SPLASHART.getHeight()));
        primaryStage.show();
    }

    /** Puts the desktop in the main window. Call once from App.start(). */
    public void showDesktop() {
        Zip zippedDesktop = load(Screen.DESKTOP);
        primaryStage.setTitle(Screen.DESKTOP.getTitle());
        primaryStage.setScene(new Scene(zippedDesktop.root, Screen.DESKTOP.getWidth(), Screen.DESKTOP.getHeight()));
        primaryStage.setMaximized(true);
        primaryStage.show();
        openWindow(Screen.TUTORIALREADME);
    }

    /** Opens a screen in its own window, owned by the desktop. Returns its controller. */
    @SuppressWarnings("unchecked")
    public <T> T openWindow(Screen screen) {
        if (screen.isSingleInstance()) {
            Stage existing = openSingles.get(screen);
            if (existing != null) {                    // already open: just focus it
                existing.toFront();
                existing.requestFocus();
                return (T) singleControllers.get(screen);
            }
        }

        Zip zippedScreen = load(screen);
        Stage window = new Stage();
        window.initOwner(primaryStage);
        window.initModality(Modality.NONE);            // desktop stays clickable
        window.setTitle(screen.getTitle());
        window.setScene(new Scene(zippedScreen.root, screen.getWidth(), screen.getHeight()));

        if (screen.isSingleInstance()) {
            openSingles.put(screen, window);
            singleControllers.put(screen, zippedScreen.controller);
            window.setOnHidden(e -> {                  // fires on any close
                openSingles.remove(screen);
                singleControllers.remove(screen);
            });
        }

        window.show();
        return (T) zippedScreen.controller;
    }

    public Stage getPrimaryStage() { return primaryStage; }
}