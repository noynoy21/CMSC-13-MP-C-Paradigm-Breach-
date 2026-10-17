package cmsc13.ParadigmBreach.view;

import cmsc13.ParadigmBreach.model.GameStageManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class CommandLineController {
    private final String helpCommandOutput = """
                                             Commands:
                                             HELP       Prints out available commands.
                                             EXIT       Closes the command-line terminal.
                                             ASK        Ask a test question.
                                             """;
                                            //  CLEAR      Clears the current texts in the command-line.

 
    @FXML 
    private TextArea quesArea;
    @FXML 
    private TextArea summaryArea;
    @FXML 
    private TextField input;

    private GameStageManager gsManager;

    @FXML
    private void initialize() {
        quesArea.appendText("ParadigmBreach v1.0\nType 'help' for commands.\n\n");
        summaryArea.appendText("Stage: ");
        summaryArea.appendText("\nProgress:");
        Platform.runLater(input::requestFocus);   // start with the cursor in the input
        gsManager = new GameStageManager(quesArea, summaryArea, input);
        
    }

    @FXML
    private void onEnter() {
        String cmd = input.getText().trim();
        input.clear();
        if (cmd.isEmpty()) return;

        quesArea.appendText("> " + cmd + "\n");
        quesArea.appendText(handle(cmd) + "\n");
    }

    private String handle(String cmd) {
        switch (cmd.toLowerCase().split(" ")[0]) {
            case "help" ->  {return helpCommandOutput;}
            // case "clear" -> {quesArea.clear(); return "";}
            case "exit" -> {quesArea.getScene().getWindow().hide(); return "";}
            case "ask", "ans" -> {
                return gsManager.handleQnaInput(cmd);
            }
            default -> {return "Unknown command: " + cmd;}
        }
    }
}