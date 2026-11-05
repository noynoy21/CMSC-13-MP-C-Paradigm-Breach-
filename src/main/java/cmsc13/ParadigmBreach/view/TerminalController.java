package cmsc13.ParadigmBreach.view;

import cmsc13.ParadigmBreach.model.GameStageManager;
import cmsc13.ParadigmBreach.util.OutputKind;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.text.TextFlow;

public class TerminalController {
    private final String helpCommandOutput = """
                                             Commands:
                                             HELP       Prints out available commands.
                                             EXIT       Closes the command-line terminal.
                                             ASK        Ask a test question.
                                             STAGE      Switch between game stages (e.g., stage 1).
                                             """;
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
    private TextField input;

    private GameStageManager gsManager;
    private TerminalOutput terminalOut; // centralize the control of terminal outputs

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

        gsManager = new GameStageManager(terminalOut);
    }

    @FXML
    private void onEnter() {
        String cmd = input.getText().trim();
        input.clear();
        if (cmd.isEmpty()) return;

        terminalOut.append("\n> " + cmd + "\n", OutputKind.COMMAND);

        switch (cmd.toLowerCase().split(" ")[0]) {
            case "ask", "ans", "stage" -> {
                gsManager.handleQnaInput(cmd);
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
}