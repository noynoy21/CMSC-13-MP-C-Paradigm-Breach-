package cmsc13.ParadigmBreach.view;

import java.util.List;
import java.util.function.IntConsumer;

import cmsc13.ParadigmBreach.persistence.GameSaveData;
import cmsc13.ParadigmBreach.persistence.GameSaveRepository;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.VBox;

public final class SaveFileSelectionController {
    @FXML private VBox slotList;
    @FXML private Button newSessionButton;

    private IntConsumer onLoadSlot;
    private Runnable onNewSession;

    public void initData(List<GameSaveRepository.SlotInfo> slots, IntConsumer onLoadSlot, Runnable onNewSession) {
        this.onLoadSlot = onLoadSlot;
        this.onNewSession = onNewSession;
        slotList.getChildren().clear();

        for (GameSaveRepository.SlotInfo slot : slots) {
            Button slotButton = new Button(slotLabel(slot));
            slotButton.setMaxWidth(Double.MAX_VALUE);
            slotButton.setStyle(buttonStyle());
            slotButton.setDisable(!slot.isLoadable());
            if (slot.isLoadable()) {
                slotButton.setOnAction(event -> this.onLoadSlot.accept(slot.slotNumber()));
            }
            slotList.getChildren().add(slotButton);
        }
    }

    @FXML
    private void onNewSession() {
        if (onNewSession != null) {
            onNewSession.run();
        }
    }

    private String slotLabel(GameSaveRepository.SlotInfo slot) {
        String prefix = "SLOT " + slot.slotNumber() + "  |  ";
        if (!slot.occupied()) {
            return prefix + "EMPTY";
        }
        if (slot.saveData().isEmpty()) {
            return prefix + "UNAVAILABLE - " + (slot.problem() == null ? "Unreadable save" : slot.problem());
        }

        GameSaveData data = slot.saveData().get();
        String progress = data.isGameCompleted() ? "Complete" : "Stage " + data.getStageKey();
        return prefix + data.getPlayerName() + "  |  " + progress;
    }

    private String buttonStyle() {
        return "-fx-background-color: rgba(51, 255, 51, 0.10);"
            + "-fx-text-fill: #33ff33; -fx-font-family: 'Cascadia Mono';"
            + "-fx-font-size: 14px; -fx-border-color: rgba(51, 255, 51, 0.55);"
            + "-fx-padding: 12 16; -fx-alignment: center-left;";
    }
}
