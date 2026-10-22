package cmsc13.ParadigmBreach.view;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntConsumer;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

public class TutorialReadmeController {
    private enum NavPage {
        PAGE_1(2),
        PAGE_2(3);

        private final int pageIdx;

        NavPage(int pageIdx) {
            this.pageIdx = pageIdx;
        }

        public int getPageIdx() {
            return pageIdx;
        }
    }

    // README.md host fxml injections
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
        "/fxml_files/tutorial_pages/homepage-tutorials.fxml",
        "/fxml_files/tutorial_pages/desktop.fxml",
        "/fxml_files/tutorial_pages/terminal.fxml"
    );

    private final List<Parent> pages = new ArrayList<>();
    private int currPageIdx = 0;

    // tutorial homepage injections
    @FXML 
    private VBox homepageBox;
    @FXML
    private Button hpBtn1; // homepage buttons
    @FXML
    private Button hpBtn2;

    private final Map<NavPage, Button> buttons = new EnumMap<>(NavPage.class);

    private IntConsumer pageNavigator = index -> {};

    @FXML 
    private void initialize() {
        if (stackPane != null){
            loadPages();
            showPage(0);
        } else if (homepageBox != null) {
            buttons.put(NavPage.PAGE_1, hpBtn1);
            buttons.put(NavPage.PAGE_2, hpBtn2);

            buttons.forEach((page, button) -> 
                button.setOnAction(
                    e -> pageNavigator.accept(page.getPageIdx())
                ));
        }
    }

    private void setPageNavigator(IntConsumer pageNavigator) {
        this.pageNavigator = pageNavigator;
    }

    private void loadPages() {
        try {
            for (String path : pagePaths) {
                URL url = getClass().getResource(path);
                if (url == null) {
                    throw new IllegalArgumentException("Tutorial page not found: " + path);
                }
                FXMLLoader loader = new FXMLLoader(url);
                Parent page = loader.load();

                if (path.equals(pagePaths.get(1))) {
                    TutorialReadmeController homepageController =
                        loader.getController();
                    homepageController.setPageNavigator(this::showPage);
                }

                pages.add(page);

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
            "Page " + (currPageIdx) + " of " + (pages.size()-1)
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
