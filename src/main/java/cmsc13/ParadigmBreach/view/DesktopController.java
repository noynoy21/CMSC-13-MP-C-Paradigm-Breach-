package cmsc13.ParadigmBreach.view;

import cmsc13.ParadigmBreach.audio.AudioManager;
import cmsc13.ParadigmBreach.audio.MusicTrack;
import cmsc13.ParadigmBreach.ui.SceneManager;
import cmsc13.ParadigmBreach.ui.Screen;
import javafx.fxml.FXML;
import javafx.scene.layout.FlowPane;

public class DesktopController {

    @FXML 
    private FlowPane desktopArea;

    // private GameStageManager gsManager;

    @FXML
    private void initialize() {
        desktopArea.getChildren().addAll(
            new DesktopIcon("Terminal", "/images/desktop_icons/command_line.png", this::openTerminal),
            new DesktopIcon("Files",    "/images/desktop_icons/files.png",    this::openFiles),
            new DesktopIcon("Files",    "/images/desktop_icons/files.png",    this::openFiles)
        );

        AudioManager.get().playMusic(MusicTrack.DESKTOP);

        // gsManager = new GameStageManager(null);
    }

    private void openTerminal() {
        SceneManager.get().openWindow(Screen.TERMINAL);
    }
    private void openFiles()    {
        SceneManager.get().openWindow(Screen.TUTORIALREADME);
    }
}
