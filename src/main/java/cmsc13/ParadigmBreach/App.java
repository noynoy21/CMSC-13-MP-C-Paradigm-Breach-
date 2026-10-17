package cmsc13.ParadigmBreach;

import cmsc13.ParadigmBreach.audio.AudioManager;
import cmsc13.ParadigmBreach.ui.SceneManager;
import javafx.application.Application;
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
    }

    public static void main(String[] args) {
        launch();
    }
}