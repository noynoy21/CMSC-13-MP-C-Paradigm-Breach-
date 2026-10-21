package cmsc13.ParadigmBreach.view;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;

public class TutorialReadmeController {
    @FXML 
    private StackPane stackPane;
    @FXML 
    private Button backButton;
    @FXML 
    private Button nextButton;
    @FXML 
    private Label pageLabel;
    private final List<String> pagePaths = List.of(
        "/fxml_files/tutorial_pages/welcome.fxml",
        "/fxml_files/tutorial_pages/desktop.fxml",
        "/fxml_files/tutorial_pages/terminal.fxml"
    );

    private final List<Parent> pages = new ArrayList<>();
    private int currPageIdx = 0;

    @FXML 
    private void initialize() {
        loadPages();
        
        showPage(0);
    }

    private void loadPages() {
        try {
            for (String path : pagePaths) {
                URL url = getClass().getResource(path);
                if (url == null) {
                    throw new IllegalArgumentException("Tutorial page not found: " + path);
                }

                pages.add(new FXMLLoader(url).load());
            }
        } catch (IOException e) {
            throw new UncheckedIOException("Could not load tutorial pages", e);
        }
    }

    private void showPage(int index) {
        currPageIdx = index;
        Parent page = pages.get(currPageIdx);
        stackPane.getChildren().setAll(page);
        StackPane.setAlignment(page, Pos.CENTER);

        pageLabel.setText(
            "Page " + (currPageIdx + 1) + " of " + pages.size()
        );
        backButton.setDisable(currPageIdx == 0);
        nextButton.setDisable(currPageIdx == pages.size() - 1);
    }

    @FXML 
    private void nextPage() {
        if (currPageIdx < pages.size()-1) {
            showPage(currPageIdx + 1);
        }
    }

    @FXML 
    private void prevPage() {
        if (currPageIdx > 0) {
            showPage(currPageIdx - 1);
        }
    }
}
