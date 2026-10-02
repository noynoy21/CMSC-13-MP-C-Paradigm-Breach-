package cmsc13.ParadigmBreach;

import cmsc13.ParadigmBreach.ui.SceneManager;
import cmsc13.ParadigmBreach.view.DesktopController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;


/**
 * JavaFX App
 */
public class App extends Application {
    private Stage primaryStage;
    private BorderPane rootLayout;
    private BorderPane commandLineLayout;

    @Override
    public void start(Stage primaryStage) {
        this.primaryStage = primaryStage;
        this.primaryStage.setTitle("ParadigmBreach");

        SceneManager.init(primaryStage);
        SceneManager.get().showSplashArt();
    }

    public static void main(String[] args) {
        launch();
    }
}