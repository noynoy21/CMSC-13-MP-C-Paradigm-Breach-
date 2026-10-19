package cmsc13.ParadigmBreach.view;

import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class TutorialReadmeController {
    @FXML 
    private StackPane stackPane;
    @FXML 
    private Button backButton;
    @FXML 
    private Button nextButton;
    @FXML 
    private Label pageLabel;

    private final List<Node> pages = new ArrayList<>();
    private int currPageIdx = 0;

    @FXML 
    private void initialize() {
        
        showPage(0);
    }

    private void showPage(int index) {
        currPageIdx = index;
        stackPane.getChildren().setAll(pages.get(currPageIdx));

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

    private Node createPage(String title, String body) {
        Label heading = new Label(title);

        TextArea text = new TextArea(body);
        text.setEditable(false);
        text.setWrapText(true);

        VBox page = new VBox(12, heading, text);
        VBox.setVgrow(text, Priority.ALWAYS);
        return page;
    }

}
