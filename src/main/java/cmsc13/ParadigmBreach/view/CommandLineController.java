package cmsc13.ParadigmBreach.view;

import java.util.List;

import cmsc13.ParadigmBreach.model.questionBank.Question;
import cmsc13.ParadigmBreach.model.questionBank.Stage3Questions;
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
                                             ASK        Ask a test question.
                                             """;
 
    @FXML 
    private TextArea output;
    @FXML 
    private TextField input;

    private Stage3Questions stage3Questions;

    @FXML
    private void initialize() {
        output.appendText("ParadigmBreach v1.0\nType 'help' for commands.\n\n");
        Platform.runLater(input::requestFocus);   // start with the cursor in the input
        stage3Questions = new Stage3Questions();
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
            case "ask" -> {
                try {
                    Question question = stage3Questions.askQuestion();
                    return buildQuestion(question);
                } catch (IllegalStateException e) {
                    return "No more questions to ask!";
                }
            }
            default -> {return "Unknown command: " + cmd;}
        }
    }

    private String buildQuestion(Question q) {
        List<Character> letterChoices = List.of('a', 'b', 'c', 'd');
        StringBuilder sb = new StringBuilder();
        String question = q.prompt();
        sb.append("Q: " + question);
        sb.append("\n");

        sb.append("Choices:\n");
        for (int i = 0; i < 4; i++) {
            sb.append(letterChoices.get(i) + ": " + q.choices().get(i));
            sb.append("\n");
        }

        return sb.toString();
    }
}