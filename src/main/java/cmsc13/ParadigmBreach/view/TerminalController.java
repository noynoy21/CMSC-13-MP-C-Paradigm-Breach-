package cmsc13.ParadigmBreach.view;

import cmsc13.ParadigmBreach.model.GameStageManager;
import cmsc13.ParadigmBreach.model.PenguAssistant;
import cmsc13.ParadigmBreach.audio.AudioManager;
import cmsc13.ParadigmBreach.audio.SoundEffect;
import cmsc13.ParadigmBreach.util.BlockDigitRenderer;
import cmsc13.ParadigmBreach.util.LifeTracker;
import cmsc13.ParadigmBreach.util.OutputKind;
import cmsc13.ParadigmBreach.util.QuestionTimer;
import javafx.application.Platform;
import javafx.animation.PauseTransition;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.text.TextFlow;
import javafx.stage.WindowEvent;
import javafx.util.Duration;

public class TerminalController {
    private static final int QUESTION_TIME_LIMIT_SECONDS = 30;

    private final String helpCommandOutput = """
                                             Commands:
                                             HELP       Prints out available commands.
                                             EXIT       Closes the command-line terminal.
                                             ASK        Ask a question; answer within 30 seconds.
                                             ANS        Answer a question; ans _.
                                             PNGU -wdyt Peek at Pengu's choice (costs one token).
                                             PNGU -ans  Let Pengu answer for free.
                                             """;
                                            //  STAGE      Switch between game stages (e.g., stage 1).
                                            //  CLEAR      Clears the current texts in the command-line.

 
    @FXML 
    private TextFlow outputFlow;
    @FXML
    private ScrollPane outputScroll;
    @FXML 
    private TextFlow summaryFlow;
    @FXML
    private ScrollPane summaryScroll;

    @FXML
    private Label timerLabel;

    @FXML
    private Label healthLabel;

    @FXML
    private Label penguTokensLabel;

    @FXML
    private ImageView penguImage;

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
    private PauseTransition penguReactionReset;

    @FXML
    private void initialize() {
        terminalOut = new TerminalOutput(outputFlow, outputScroll, summaryFlow, summaryScroll);

        terminalOut.append(
            "ParadigmBreach v1.0\nType 'help' for commands.",
            OutputKind.COMMAND
        );
        terminalOut.appendSummary("Stage: ", OutputKind.RESPONSE);
        terminalOut.appendSummary("Progress:", OutputKind.RESPONSE);
        Platform.runLater(input::requestFocus);   // start with the cursor in the input

        lifeTracker = new LifeTracker(this::updateHealthDisplay);
        penguAssistant = new PenguAssistant(this::updatePenguTokenDisplay);
        gsManager = new GameStageManager(terminalOut, lifeTracker, penguAssistant);
        questionTimer = new QuestionTimer(
            QUESTION_TIME_LIMIT_SECONDS,
            this::updateTimerDisplay,
            this::onQuestionTimeout
        );
        penguNormalImage = loadPenguImage("/images/pengu/tux_oEyes.png");
        penguHappyImage = loadPenguImage("/images/pengu/tux_happyEyes.png");
        penguFailImage = loadPenguImage("/images/pengu/tux_xEyes.png");
        penguImage.setImage(penguNormalImage);
        timerLabel.setText("TIME: --");
        installWindowCloseHandler();
    }

    @FXML
    private void onEnter() {
        String cmd = input.getText().trim();
        input.clear();
        if (cmd.isEmpty()) return;

        terminalOut.append("\n> " + cmd + "\n", OutputKind.COMMAND);
        expireQuestionTimerIfNeeded();

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

    private void startQuestionTimer() {
        questionTimer.start();
    }

    private void updateTimerDisplay(int remainingSeconds) {
        timerLabel.setText("TIME LEFT:\n" + BlockDigitRenderer.render(remainingSeconds));
    }

    private void onQuestionTimeout() {
        timerLabel.setText("TIME'S UP");
        gsManager.timeoutCurrentQuestion();
    }

    private void updateHealthDisplay(int livesRemaining) {
        String hearts = LifeTracker.renderHearts(livesRemaining);
        healthLabel.setText("LIFE REMAINING:\n" + hearts);
    }

    private void updatePenguTokenDisplay(int tokensRemaining) {
        penguTokensLabel.setText("PENGU TOKENS:\n" + tokensRemaining);
    }

    private Image loadPenguImage(String path) {
        var imageUrl = TerminalController.class.getResource(path);
        if (imageUrl == null) {
            throw new IllegalStateException("Missing Pengu image: " + path);
        }
        return new Image(imageUrl.toExternalForm());
    }

    private void showPenguReaction(Image reactionImage) {
        if (penguReactionReset != null) {
            penguReactionReset.stop();
        }

        penguImage.setImage(reactionImage);
        penguReactionReset = new PauseTransition(Duration.millis(900));
        penguReactionReset.setOnFinished(event -> penguImage.setImage(penguNormalImage));
        penguReactionReset.playFromStart();
    }

    private void expireQuestionTimerIfNeeded() {
        questionTimer.expireIfDue();
    }

    private void stopQuestionTimer(String labelText) {
        questionTimer.stop();
        timerLabel.setText(labelText);
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
                        event -> stopQuestionTimer("TIME: --")
                    );
                }
            });
        });
    }
}
