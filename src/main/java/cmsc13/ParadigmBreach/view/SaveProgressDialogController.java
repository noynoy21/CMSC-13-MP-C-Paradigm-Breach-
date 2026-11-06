package cmsc13.ParadigmBreach.view;

import java.util.List;
import java.util.Optional;

import cmsc13.ParadigmBreach.persistence.GameSaveData;
import cmsc13.ParadigmBreach.persistence.GameSaveRepository;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/** Controller for the FXML-based save and exit dialog. */
public class SaveProgressDialogController {
    public enum CloseChoice {
        SAVE,
        EXIT_WITHOUT_SAVING,
        CANCEL
    }

    @FXML private HBox header;
    @FXML private Label dialogTitle;
    @FXML private Label heading;
    @FXML private Label message;
    @FXML private VBox slotList;
    @FXML private Button primaryButton;
    @FXML private Button exitButton;
    @FXML private Button cancelButton;

    private Stage stage;
    private CloseChoice closeChoice = CloseChoice.CANCEL;
    private Integer selectedSlot;
    private boolean primarySaves;
    private double dragOffsetX;
    private double dragOffsetY;

    @FXML
    private void initialize() {
        slotList.setVisible(false);
        slotList.setManaged(false);
        primaryButton.setVisible(false);
        primaryButton.setManaged(false);
        exitButton.setVisible(false);
        exitButton.setManaged(false);
        cancelButton.setVisible(false);
        cancelButton.setManaged(false);
    }

    public void configureClosePrompt() {
        dialogTitle.setText("PARADIGMBREACH  //  SESSION CONTROL");
        heading.setText("SAVE BEFORE EXIT?");
        message.setText("Your current progress is only written when you choose a save slot.");
        primaryButton.setText("SAVE PROGRESS");
        primaryButton.getStyleClass().setAll("dialog-button", "dialog-primary");
        primaryButton.setVisible(true);
        primaryButton.setManaged(true);
        exitButton.setVisible(true);
        exitButton.setManaged(true);
        cancelButton.setVisible(true);
        cancelButton.setManaged(true);
        primarySaves = true;
    }

    public void configureSlotPicker(List<GameSaveRepository.SlotInfo> slots) {
        dialogTitle.setText("PARADIGMBREACH  //  SAVE DESTINATION");
        heading.setText("SELECT A SLOT");
        message.setText("Choose an empty slot or overwrite one of the existing saves.");
        slotList.getChildren().clear();
        for (GameSaveRepository.SlotInfo slot : slots) {
            Button slotButton = new Button(slotLabel(slot));
            slotButton.getStyleClass().addAll("dialog-button", "dialog-slot");
            slotButton.setMaxWidth(Double.MAX_VALUE);
            slotButton.setAlignment(Pos.CENTER_LEFT);
            slotButton.setOnAction(event -> {
                selectedSlot = slot.slotNumber();
                closeDialog();
            });
            VBox.setVgrow(slotButton, Priority.NEVER);
            slotList.getChildren().add(slotButton);
        }
        slotList.setVisible(true);
        slotList.setManaged(true);
        cancelButton.setVisible(true);
        cancelButton.setManaged(true);
    }

    public void configureSaveFailure() {
        dialogTitle.setText("PARADIGMBREACH  //  SAVE ERROR");
        heading.setText("SAVE FAILED");
        message.setText("The selected slot could not be written. Your progress is still in this session.");
        primaryButton.setText("RETURN TO GAME");
        primaryButton.getStyleClass().setAll("dialog-button", "dialog-primary");
        primaryButton.setVisible(true);
        primaryButton.setManaged(true);
    }

    public CloseChoice getCloseChoice() {
        return closeChoice;
    }

    public Optional<Integer> getSelectedSlot() {
        return Optional.ofNullable(selectedSlot);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
        header.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
            dragOffsetX = event.getSceneX();
            dragOffsetY = event.getSceneY();
        });
        header.addEventHandler(MouseEvent.MOUSE_DRAGGED, event -> {
            stage.setX(event.getScreenX() - dragOffsetX);
            stage.setY(event.getScreenY() - dragOffsetY);
        });
    }

    @FXML
    private void onPrimary() {
        if (primarySaves) {
            closeChoice = CloseChoice.SAVE;
        }
        closeDialog();
    }

    @FXML
    private void onExitWithoutSaving() {
        closeChoice = CloseChoice.EXIT_WITHOUT_SAVING;
        closeDialog();
    }

    @FXML
    private void onCancel() {
        closeDialog();
    }

    private String slotLabel(GameSaveRepository.SlotInfo slot) {
        String prefix = "SLOT " + slot.slotNumber() + "   //   ";
        if (!slot.occupied()) {
            return prefix + "EMPTY";
        }
        if (slot.saveData().isEmpty()) {
            return prefix + "OVERWRITE UNREADABLE SAVE";
        }

        GameSaveData data = slot.saveData().get();
        String progress = data.isGameCompleted() ? "COMPLETE" : "STAGE " + data.getStageKey();
        return prefix + "OVERWRITE " + data.getPlayerName() + "   [" + progress + "]";
    }

    private void closeDialog() {
        if (stage != null) {
            stage.close();
        }
    }
}
