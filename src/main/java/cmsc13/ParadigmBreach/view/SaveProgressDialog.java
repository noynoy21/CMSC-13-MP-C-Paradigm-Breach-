package cmsc13.ParadigmBreach.view;

import java.net.URL;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

import cmsc13.ParadigmBreach.persistence.GameSaveData;
import cmsc13.ParadigmBreach.persistence.GameSaveRepository;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

/** Frameless save prompts styled to match the in-game terminal. */
public final class SaveProgressDialog {
    public enum CloseChoice {
        SAVE,
        EXIT_WITHOUT_SAVING,
        CANCEL
    }

    private SaveProgressDialog() {}

    public static CloseChoice showClosePrompt(Window owner) {
        AtomicReference<CloseChoice> result = new AtomicReference<>(CloseChoice.CANCEL);
        DialogShell shell = createShell(owner, "SESSION CONTROL", 540);

        VBox body = body(
            "SAVE BEFORE EXIT?",
            "Your current progress is only written when you choose a save slot."
        );
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.getChildren().addAll(
            button("SAVE PROGRESS", "save-dialog-primary", () -> {
                result.set(CloseChoice.SAVE);
                shell.stage().close();
            }),
            button("EXIT WITHOUT SAVING", "save-dialog-danger", () -> {
                result.set(CloseChoice.EXIT_WITHOUT_SAVING);
                shell.stage().close();
            }),
            button("CANCEL", "save-dialog-secondary", shell.stage()::close)
        );
        shell.content().getChildren().addAll(body, actions);

        show(shell);
        return result.get();
    }

    public static Optional<Integer> showSlotPicker(Window owner) {
        AtomicReference<Integer> selectedSlot = new AtomicReference<>();
        DialogShell shell = createShell(owner, "SAVE DESTINATION", 530);

        VBox body = body(
            "SELECT A SLOT",
            "Choose an empty slot or overwrite one of the existing saves."
        );
        VBox slots = new VBox(9);
        slots.setFillWidth(true);
        List<GameSaveRepository.SlotInfo> slotInfo = GameSaveRepository.inspectSlots();
        for (GameSaveRepository.SlotInfo slot : slotInfo) {
            Button slotButton = button(slotLabel(slot), "save-dialog-slot", () -> {
                selectedSlot.set(slot.slotNumber());
                shell.stage().close();
            });
            slotButton.setMaxWidth(Double.MAX_VALUE);
            slots.getChildren().add(slotButton);
        }
        VBox.setVgrow(slots, Priority.NEVER);

        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.getChildren().add(button("CANCEL", "save-dialog-secondary", shell.stage()::close));
        shell.content().getChildren().addAll(body, slots, actions);

        show(shell);
        return Optional.ofNullable(selectedSlot.get());
    }

    public static void showSaveFailure(Window owner) {
        DialogShell shell = createShell(owner, "SAVE ERROR", 480);
        VBox body = body(
            "SAVE FAILED",
            "The selected slot could not be written. Your progress is still in this session."
        );
        HBox actions = new HBox(10);
        actions.setAlignment(Pos.CENTER_RIGHT);
        actions.getChildren().add(button("RETURN TO GAME", "save-dialog-primary", shell.stage()::close));
        shell.content().getChildren().addAll(body, actions);
        show(shell);
    }

    private static String slotLabel(GameSaveRepository.SlotInfo slot) {
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

    private static DialogShell createShell(Window owner, String systemTitle, double width) {
        Stage stage = new Stage(StageStyle.UNDECORATED);
        stage.initOwner(owner);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.setResizable(false);

        BorderPane root = new BorderPane();
        root.getStyleClass().add("save-dialog-root");

        HBox header = new HBox(12);
        header.getStyleClass().add("save-dialog-header");
        header.setAlignment(Pos.CENTER_LEFT);
        Label title = new Label("PARADIGMBREACH  //  " + systemTitle);
        title.getStyleClass().add("save-dialog-header-title");
        HBox.setHgrow(title, Priority.ALWAYS);
        Button close = button("X", "save-dialog-close", stage::close);
        header.getChildren().addAll(title, close);
        root.setTop(header);

        VBox content = new VBox(22);
        content.setPadding(new Insets(26, 30, 24, 30));
        root.setCenter(content);

        URL stylesheet = SaveProgressDialog.class.getResource("/css_files/terminalLook.css");
        if (stylesheet != null) {
            root.getStyleClass().add("terminal-root");
        }
        Scene scene = new Scene(root, width, 100);
        if (stylesheet != null) {
            scene.getStylesheets().add(stylesheet.toExternalForm());
        }
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ESCAPE) {
                stage.close();
            }
        });

        double[] dragOffset = new double[2];
        header.setOnMousePressed(event -> {
            dragOffset[0] = event.getSceneX();
            dragOffset[1] = event.getSceneY();
        });
        header.setOnMouseDragged(event -> {
            stage.setX(event.getScreenX() - dragOffset[0]);
            stage.setY(event.getScreenY() - dragOffset[1]);
        });

        stage.setOnShown(event -> {
            if (owner != null) {
                stage.setX(owner.getX() + (owner.getWidth() - stage.getWidth()) / 2);
                stage.setY(owner.getY() + (owner.getHeight() - stage.getHeight()) / 2);
            }
        });
        stage.setScene(scene);
        return new DialogShell(stage, content);
    }

    private static VBox body(String headingText, String messageText) {
        VBox body = new VBox(9);
        Label heading = new Label(headingText);
        heading.getStyleClass().add("save-dialog-heading");
        Label message = new Label(messageText);
        message.getStyleClass().add("save-dialog-message");
        message.setWrapText(true);
        body.getChildren().addAll(heading, message);
        return body;
    }

    private static Button button(String text, String styleClass, Runnable action) {
        Button button = new Button(text);
        button.getStyleClass().add(styleClass);
        button.setOnAction(event -> action.run());
        return button;
    }

    private static void show(DialogShell shell) {
        shell.stage().sizeToScene();
        shell.stage().showAndWait();
    }

    private record DialogShell(Stage stage, VBox content) {}
}
