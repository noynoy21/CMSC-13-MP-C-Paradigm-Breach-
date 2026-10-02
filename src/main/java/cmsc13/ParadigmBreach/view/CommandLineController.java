package cmsc13.ParadigmBreach.view;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class CommandLineController {
    private final String helpCommandOutput = """
                                             Commands:
                                             HELP       Prints out available commands.
                                             CLEAR      Clears the current texts in the command-line.
                                             EXIT       Closes the command-line terminal.
                                             """;
 
    @FXML 
    private TextArea output;
    @FXML 
    private TextField input;

    @FXML
    private void initialize() {
        output.appendText("ParadigmBreach v1.0\nType 'help' for commands.\n\n");
        Platform.runLater(input::requestFocus);   // start with the cursor in the input
    }

    @FXML
    private void onEnter() {
        String cmd = input.getText().trim();
        input.clear();
        if (cmd.isEmpty()) return;

        output.appendText("> " + cmd + "\n");
        output.appendText(handle(cmd) + "\n");
    }

    private String handle(String cmd) {
        switch (cmd.toLowerCase()) {
            case "help" ->  {return helpCommandOutput;}
            case "clear" -> {output.clear(); return "";}
            case "exit" -> {output.getScene().getWindow().hide(); return "";}
            default -> {return "Unknown command: " + cmd;}
        }
    }
}