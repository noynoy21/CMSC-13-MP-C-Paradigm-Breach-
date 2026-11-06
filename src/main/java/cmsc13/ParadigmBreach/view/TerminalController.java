package cmsc13.ParadigmBreach.view;

import java.io.IOException;
import java.util.Locale;

import cmsc13.ParadigmBreach.App;
import cmsc13.ParadigmBreach.audio.AudioManager;
import cmsc13.ParadigmBreach.audio.SoundEffect;
import cmsc13.ParadigmBreach.model.GameStageManager;
import cmsc13.ParadigmBreach.model.PenguAssistant;
import cmsc13.ParadigmBreach.model.PlayerSession;
import cmsc13.ParadigmBreach.persistence.GameSaveRepository;
import cmsc13.ParadigmBreach.util.BlockDigitRenderer;
import cmsc13.ParadigmBreach.util.LifeTracker;
import cmsc13.ParadigmBreach.util.OutputKind;
import cmsc13.ParadigmBreach.util.QuestionTimer;
import cmsc13.ParadigmBreach.ui.SceneManager;
import cmsc13.ParadigmBreach.ui.Screen;
import cmsc13.ParadigmBreach.util.StringsConfig;
import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class TerminalController {
    private enum SaveDialogMode {
        CLOSE_PROMPT,
        SLOT_PICKER,
        SAVE_FAILURE
    }

    private final String helpCommandOutput = StringsConfig.get().getHelpCommandOutput();
    private final String gameOverError = StringsConfig.get().getGameOverError();
 
    @FXML 
    private TextFlow outputFlow;
    @FXML
    private ImageView gameOverImageView;
    @FXML
    private BorderPane gameOverBorderPane;
    @FXML
    private Label redoStagePromptLabel;
    @FXML
    private ScrollPane outputScroll;
    @FXML 
    private TextFlow summaryFlow;
    @FXML
    private ScrollPane summaryScroll;
    @FXML
    private ScrollPane gameOverErrorScroll;
    @FXML
    private TextFlow gameOverErrorFlow;

    @FXML
    private Label timerLabel;

    @FXML
    private Label healthLabel;

    @FXML
    private Label penguTokensLabel;

    @FXML
    private ImageView penguImage;

    @FXML
    private ImageView foxSilicSprite;

    @FXML 
    private TextField input;

    private GameStageManager gsManager;
    private TerminalOutput terminalOut; // centralize the control of terminal outputs
    private QuestionTimer questionTimer;
    private LifeTracker lifeTracker;
    private PenguAssistant penguAssistant;
    private Image penguNormalImage;
    private Image penguHappyImage;
    private Image penguFailImage;
    private Image penguGameOverImage;
    private Image gameOverImage;
    // private BorderPane gameOverBorder;
    private PauseTransition penguReactionReset;
    private Timeline gameOverBlink;
    private boolean awaitingRedoChoice;
    private boolean exitPending;

    @FXML
    private void initialize() {
        terminalOut = new TerminalOutput(outputFlow, outputScroll, summaryFlow, summaryScroll);

        terminalOut.append(
            "ParadigmBreach v1.0\nType 'help' for commands.",
            OutputKind.COMMAND
        );

        foxSilicSprite.setImage(loadImage("/images/gameover_screens/fox.png"));
        foxSilicSprite.setVisible(false);
        // Region foxContainer = (Region) foxSilicSprite.getParent();
        // foxSilicSprite.fitWidthProperty().bind(foxContainer.widthProperty());
        // foxSilicSprite.setFitHeight(0);

        penguNormalImage = loadImage("/images/pengu/tux_oEyes.png");
        penguHappyImage = loadImage("/images/pengu/tux_happyEyes.png");
        penguFailImage = loadImage("/images/pengu/tux_xEyes.png");

        penguGameOverImage = loadImage("/images/gameover_screens/red_tux_xEyes.png");
        gameOverImage = loadImage("/images/gameover_screens/IP_LEAKED.png");
        Text errorText = new Text(gameOverError);
        errorText.getStyleClass().add("error");
        gameOverErrorFlow.getChildren().setAll(errorText);

        penguImage.setImage(penguNormalImage);
        gameOverImageView.setImage(gameOverImage);
        timerLabel.setText("TIME: --");

        questionTimer = new QuestionTimer(
            GameStageManager.QUESTION_TIME_LIMIT_SECONDS,
            this::updateTimerDisplay,
            this::onQuestionTimeout
        );
        lifeTracker = new LifeTracker(this::updateHealthDisplay);
        penguAssistant = new PenguAssistant(this::updatePenguTokenDisplay);
        Integer selectedSlot = PlayerSession.getSelectedSaveSlot();
        gsManager = new GameStageManager(
            terminalOut,
            lifeTracker,
            penguAssistant,
            selectedSlot == null ? null : GameSaveRepository.forSlot(selectedSlot)
        );

        if (gsManager.isGameCompleted()) {
            timerLabel.setText("GAME COMPLETE");
        } else if (lifeTracker.getLivesRemaining() == 0) {
            timerLabel.setText("GAME OVER");
        } else if (gsManager.hasActiveQuestion()) {
            long remainingMillis = gsManager.getRemainingQuestionMillis();
            if (remainingMillis > 0) {
                questionTimer.startWithRemainingMillis(remainingMillis);
            } else {
                timerLabel.setText("TIME'S UP");
                gsManager.timeoutCurrentQuestion();
            }
        }

        Platform.runLater(input::requestFocus);   // start with the cursor in the input
        installWindowCloseHandler();
    }

    @FXML
    private void onEnter() {
        String cmd = input.getText().trim();
        input.clear();
        if (cmd.isEmpty()) return;

        terminalOut.append("\n> " + cmd + "\n", OutputKind.COMMAND);
        boolean redoChoiceWasPending = awaitingRedoChoice;
        expireQuestionTimerIfNeeded();

        if (cmd.toLowerCase(Locale.ROOT).split("\\s+")[0].equals("save")) {
            handleSaveCommand(cmd);
            return;
        }

        if (redoChoiceWasPending) {
            handleRedoChoice(cmd);
            return;
        }
        if (awaitingRedoChoice) {
            return;
        }

        switch (cmd.toLowerCase().split(" ")[0]) {
            case "ask", "ans", "stage", "pngu" -> {
                handleQuestionCommand(cmd);
            }
            case "help" ->  {
                terminalOut.append(helpCommandOutput, OutputKind.RESPONSE);
            }
            case "exit" -> {
                outputScroll.getScene().getWindow().hide();
            }
            default -> {
                terminalOut.append("Unknown command: " + cmd, OutputKind.ERROR);
            }
        }
    }

    private void handleQuestionCommand(String command) {
        GameStageManager.QnaOutcome outcome = gsManager.handleQnaInput(command);
        switch (outcome) {
            case QUESTION_STARTED -> startQuestionTimer();
            case ANSWER_ACCEPTED -> stopQuestionTimer("ANSWERED");
            case PENGU_ANSWERED -> {
                stopQuestionTimer("ANSWERED");
                showPenguReaction(penguHappyImage);
            }
            case PENGU_ASKED -> {
                AudioManager.get().playSFX(SoundEffect.TOKEN_SUCCESS);
                showPenguReaction(penguHappyImage);
            }
            case PENGU_OUT_OF_TOKENS, PENGU_FAILED -> {
                AudioManager.get().playSFX(SoundEffect.TOKEN_FAIL);
                showPenguReaction(penguFailImage);
            }
            case STAGE_CHANGED -> stopQuestionTimer("TIME: --");
            case NONE -> { }
        }
    }

    private void handleSaveCommand(String command) {
        String[] args = command.trim().split("\\s+");
        if (args.length != 3 || !"-s".equalsIgnoreCase(args[1])) {
            terminalOut.append("Usage: save -s <1|2|3>", OutputKind.RESPONSE);
            return;
        }

        int slotNumber;
        try {
            slotNumber = Integer.parseInt(args[2]);
        } catch (NumberFormatException e) {
            terminalOut.append("Invalid save slot. Choose 1, 2, or 3.", OutputKind.ERROR);
            return;
        }
        if (slotNumber < 1 || slotNumber > GameSaveRepository.SLOT_COUNT) {
            terminalOut.append("Invalid save slot. Choose 1, 2, or 3.", OutputKind.ERROR);
            return;
        }

        if (gsManager.saveProgress(GameSaveRepository.forSlot(slotNumber), questionTimer.getRemainingMillis())) {
            PlayerSession.setSelectedSaveSlot(slotNumber);
            terminalOut.append("Progress saved to slot " + slotNumber + ".", OutputKind.CORRECT);
        }
    }

    private void startQuestionTimer() {
        questionTimer.start();
    }

    private void updateTimerDisplay(int remainingSeconds) {
        timerLabel.setText("TIME LEFT:\n" + BlockDigitRenderer.render(remainingSeconds));
    }

    private void onQuestionTimeout() {
        timerLabel.setText("TIME'S UP");
        gsManager.timeoutCurrentQuestion();
        AudioManager.get().playSFX(SoundEffect.WRONG);
    }

    private void updateHealthDisplay(int livesRemaining) {
        String hearts = LifeTracker.renderHearts(livesRemaining);
        healthLabel.setText("LIFE REMAINING:\n" + hearts);

        if (livesRemaining == 0) {
            awaitingRedoChoice = true;
            gameOverErrorScroll.setVisible(true);
            gameOverErrorScroll.setManaged(true);

            summaryScroll.setVisible(false);
            summaryScroll.setManaged(false);
            startGameOverBlink();
        } else {
            awaitingRedoChoice = false;
            gameOverErrorScroll.setVisible(false);
            gameOverErrorScroll.setManaged(false);

            summaryScroll.setVisible(true);
            summaryScroll.setManaged(true);
            stopGameOverBlink();
        }
    }

    private void handleRedoChoice(String choice) {
        switch (choice.toLowerCase(Locale.ROOT)) {
            case "y", "yes" -> {
                awaitingRedoChoice = false;
                questionTimer.stop();
                stopPenguReaction();
                gsManager.restartCurrentStage();
                timerLabel.setText("TIME: --");
            }
            case "n", "no", "exit" -> {
                awaitingRedoChoice = false;
                questionTimer.stop();
                stopPenguReaction();
                if (gameOverBlink != null) {
                    gameOverBlink.stop();
                    gameOverBlink = null;
                }
                outputScroll.getScene().getWindow().hide();
            }
            default -> redoStagePromptLabel.setText("Please enter y or n: redo stage?");
        }
    }

    private void updatePenguTokenDisplay(int tokensRemaining) {
        penguTokensLabel.setText("PENGU TOKENS:\n" + tokensRemaining);
    }

    private Image loadImage(String path) {
        var imageUrl = TerminalController.class.getResource(path);
        if (imageUrl == null) {
            throw new IllegalStateException("Missing image: " + path);
        }
        return new Image(imageUrl.toExternalForm());
    }

    private void showPenguReaction(Image reactionImage) {
        if (gameOverBlink != null && gameOverBlink.getStatus() == Animation.Status.RUNNING) {
            return;
        }

        if (penguReactionReset != null) {
            penguReactionReset.stop();
        }

        penguImage.setImage(reactionImage);
        penguReactionReset = new PauseTransition(Duration.millis(900));
        penguReactionReset.setOnFinished(event -> penguImage.setImage(penguNormalImage));
        penguReactionReset.playFromStart();
    }

    private void stopPenguReaction() {
        if (penguReactionReset != null) {
            penguReactionReset.stop();
            penguReactionReset = null;
        }
        penguImage.setImage(penguNormalImage);
    }

    private void startGameOverBlink() {
        if (gameOverBlink != null && gameOverBlink.getStatus() == Animation.Status.RUNNING) {
            return;
        }

        if (penguReactionReset != null) {
            penguReactionReset.stop();
        }
        if (questionTimer != null) {
            questionTimer.stop();
        }

        timerLabel.setText("GAME OVER");
        redoStagePromptLabel.setText("Redo stage? (y/n)");
        penguImage.setImage(penguGameOverImage);
        penguImage.setVisible(true);

        outputFlow.setVisible(false);
        outputFlow.setManaged(false);

        gameOverImageView.setVisible(true);
        gameOverImageView.setManaged(true);

        gameOverBorderPane.setVisible(true);
        gameOverBorderPane.setManaged(true);

        gameOverBlink = new Timeline(
            new KeyFrame(Duration.ZERO, event -> setGameOverVisible(true)),
            new KeyFrame(Duration.millis(450), event -> setGameOverVisible(false)),
            new KeyFrame(Duration.millis(900), event -> setGameOverVisible(true))
        );
        gameOverBlink.setCycleCount(Animation.INDEFINITE);
        gameOverBlink.playFromStart();
    }

    private void setGameOverVisible(boolean visible) {
        gameOverImageView.setVisible(visible);
        penguImage.setVisible(visible);
        foxSilicSprite.setVisible(visible);
    }

    private void stopGameOverBlink() {
        if (gameOverBlink != null) {
            gameOverBlink.stop();
            gameOverBlink = null;
        }

        // gameOverBlink.stop();
        // gameOverBlink = null;

        gameOverImageView.setVisible(false);
        gameOverImageView.setManaged(false);
        gameOverBorderPane.setVisible(false);
        gameOverBorderPane.setManaged(false);
        foxSilicSprite.setVisible(false);

        outputFlow.setVisible(true);
        outputFlow.setManaged(true);

        penguImage.setVisible(true);
        penguImage.setImage(penguNormalImage);
    }

    private void expireQuestionTimerIfNeeded() {
        questionTimer.expireIfDue();
    }

    private void stopQuestionTimer(String labelText) {
        questionTimer.stop();
        timerLabel.setText(gsManager.getHealth() == 0 ? "GAME OVER" : labelText);
    }

    private void installWindowCloseHandler() {
        outputFlow.sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene == null) {
                return;
            }

            newScene.windowProperty().addListener((windowObservable, oldWindow, newWindow) -> {
                if (newWindow != null) {
                    newWindow.addEventHandler(
                        WindowEvent.WINDOW_HIDDEN,
                        event -> {
                            questionTimer.pause();
                        }
                    );
                    newWindow.addEventHandler(
                        WindowEvent.WINDOW_SHOWN,
                        event -> questionTimer.resume()
                    );
                }
            });
        });
    }

    public void handleDesktopCloseRequest(WindowEvent event) {
        if (exitPending) {
            event.consume();
            return;
        }
        event.consume();
        if (gsManager == null) {
            exitApplicationNow();
            return;
        }
        questionTimer.pause();

        SaveProgressDialogController closePrompt;
        try {
            closePrompt = showSaveProgressDialog(SaveDialogMode.CLOSE_PROMPT);
        } catch (IOException e) {
            terminalOut.append("Could not open the save dialog.", OutputKind.ERROR);
            questionTimer.resume();
            return;
        }
        if (closePrompt.getCloseChoice() == SaveProgressDialogController.CloseChoice.CANCEL) {
            questionTimer.resume();
            return;
        }
        if (closePrompt.getCloseChoice() == SaveProgressDialogController.CloseChoice.EXIT_WITHOUT_SAVING) {
            exitApplicationNow();
            return;
        }

        SaveProgressDialogController slotPicker;
        try {
            slotPicker = showSaveProgressDialog(SaveDialogMode.SLOT_PICKER);
        } catch (IOException e) {
            terminalOut.append("Could not open the save slot picker.", OutputKind.ERROR);
            questionTimer.resume();
            return;
        }
        var selectedSlot = slotPicker.getSelectedSlot();
        if (selectedSlot.isEmpty()) {
            questionTimer.resume();
            return;
        }

        int slotNumber = selectedSlot.get();
        boolean saved = gsManager.saveProgress(
            GameSaveRepository.forSlot(slotNumber),
            questionTimer.getRemainingMillis()
        );
        if (saved) {
            PlayerSession.setSelectedSaveSlot(slotNumber);
            exitApplicationNow();
        } else {
            try {
                showSaveProgressDialog(SaveDialogMode.SAVE_FAILURE);
            } catch (IOException e) {
                terminalOut.append("The save failed, and its error dialog could not be opened.", OutputKind.ERROR);
            }
            questionTimer.resume();
        }
    }

    private void exitApplicationNow() {
        if (exitPending) {
            return;
        }

        exitPending = true;
        if (questionTimer != null) questionTimer.stop();
        // Bypass JavaFX 27's Windows teardown path that raises UI Automation focus
        // events for an already-closed Glass view when Platform.exit() is called.
        App.exitImmediately();
    }

    private SaveProgressDialogController showSaveProgressDialog(SaveDialogMode mode) throws IOException {
        var fxml = TerminalController.class.getResource("/fxml_files/save-progress-dialog.fxml");
        if (fxml == null) {
            throw new IOException("save-progress-dialog.fxml is missing from the application resources.");
        }

        FXMLLoader loader = new FXMLLoader(fxml);
        Parent root = loader.load();
        SaveProgressDialogController controller = loader.getController();
        switch (mode) {
            case CLOSE_PROMPT -> controller.configureClosePrompt();
            case SLOT_PICKER -> controller.configureSlotPicker(GameSaveRepository.inspectSlots());
            case SAVE_FAILURE -> controller.configureSaveFailure();
        }

        Window owner = SceneManager.get().getPrimaryStage();
        Stage dialog = new Stage(StageStyle.UNDECORATED);
        dialog.initOwner(owner);
        dialog.initModality(Modality.WINDOW_MODAL);
        dialog.setResizable(false);
        dialog.setTitle("PARADIGMBREACH // SAVE");

        Scene scene = new Scene(root);
        scene.setOnKeyPressed(keyEvent -> {
            if (keyEvent.getCode() == KeyCode.ESCAPE) {
                dialog.close();
            }
        });
        dialog.setScene(scene);
        controller.setStage(dialog);
        dialog.setOnShown(shownEvent -> {
            dialog.setX(owner.getX() + (owner.getWidth() - dialog.getWidth()) / 2);
            dialog.setY(owner.getY() + (owner.getHeight() - dialog.getHeight()) / 2);
        });
        dialog.showAndWait();
        return controller;
    }
}
