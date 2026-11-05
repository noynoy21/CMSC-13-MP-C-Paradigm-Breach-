package cmsc13.ParadigmBreach.view;

import cmsc13.ParadigmBreach.model.PlayerSession;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

public class NameInputController {

    @FXML private StackPane root;
    @FXML private TextField nameField;
    @FXML private Button confirmButton;

    private Runnable onComplete;

    public void initData(Runnable onComplete) {
        this.onComplete = onComplete;
        nameField.setText("C1pher");
        nameField.selectAll();
    }

    @FXML
    private void initialize() {
        nameField.setOnAction(e -> onConfirm());
    }

    @FXML
    private void onConfirm() {
        String inputName = nameField.getText();
        PlayerSession.setPlayerName(inputName);

        FadeTransition ft = new FadeTransition(Duration.seconds(0.4), root);
        ft.setToValue(0);
        ft.setOnFinished(e -> {
            root.getScene().getWindow().hide();
            if (onComplete != null) {
                onComplete.run();
            }
        });
        ft.play();
    }
}