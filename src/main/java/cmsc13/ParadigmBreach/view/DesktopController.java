package cmsc13.ParadigmBreach.view;

import cmsc13.ParadigmBreach.ui.SceneManager;
import cmsc13.ParadigmBreach.ui.Screen;
import javafx.fxml.FXML;
import javafx.scene.layout.FlowPane;
import javafx.stage.Stage;

public class DesktopController {
    private Stage primaryStage;
    // private App mainApp;

    @FXML 
    private FlowPane desktopArea;

    @FXML
    private void initialize() {
        desktopArea.getChildren().addAll(
            new DesktopIcon("Terminal", "/images/desktop_icons/command_line.png", this::openTerminal),
            new DesktopIcon("Files",    "/images/desktop_icons/files.png",    this::openFiles),
            new DesktopIcon("Files",    "/images/desktop_icons/files.png",    this::openFiles)
        );
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    private void openTerminal() {
        SceneManager.get().openWindow(Screen.TERMINAL);
    }
    private void openFiles()    {
        SceneManager.get().openWindow(Screen.READMETUTORIAL);
    }
}
