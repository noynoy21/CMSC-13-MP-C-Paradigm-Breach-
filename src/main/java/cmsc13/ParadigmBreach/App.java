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

        // // initCommandLineLayout();
        // initDesktop();

        SceneManager.init(primaryStage);
        // SceneManager.get().showDesktop();
        SceneManager.get().showSplashArt();
    }

    // public void initCommandLineLayout() {
    //     try {    
    //         FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml_files/command-line.fxml"));
    //         BorderPane commandLine = (BorderPane) loader.load();

    //         Scene scene = new Scene(commandLine, 800, 500);

    //         primaryStage.setScene(scene);
    //         primaryStage.show();
    //     } catch(Exception e) {
    //         e.printStackTrace();
    //     }
    // }

    public void initDesktop() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml_files/desktop.fxml"));
            StackPane desktop = (StackPane) loader.load();

            DesktopController desktopController = loader.getController();
            desktopController.setPrimaryStage(this.primaryStage);

            Scene desktopScene = new Scene(desktop);

            primaryStage.setScene(desktopScene);
            primaryStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch();
    }

}