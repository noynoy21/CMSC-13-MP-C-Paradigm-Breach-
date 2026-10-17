package cmsc13.ParadigmBreach.model;

import java.util.Iterator;
import java.util.List;

import cmsc13.ParadigmBreach.model.questionBank.OOPStage;
import cmsc13.ParadigmBreach.model.questionBank.Question;
import cmsc13.ParadigmBreach.model.questionBank.QuestionBank;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class GameStageManager {

    private TextArea output;
    private TextArea summaryArea;
    private TextField input;

    private QuestionBank currStageQuestions;
    private Question currQuestion;
    private int answeredQuestions = 0;

    private Iterator<Question> qIterator;

    private static final List<Character> letterChoices = List.of('a', 'b', 'c', 'd');

    public GameStageManager(TextArea output, TextArea summaryArea, TextField input) {
        this.output = output;
        this.summaryArea = summaryArea;
        this.input = input;
        currStageQuestions = new OOPStage();
    }

    public String handleQnaInput(String input) {
        switch (input.toLowerCase().split(" ")[0]) {
            case "ask" -> {
                return handleAsk();
            }
            case "ans" -> {
                return handleAns(input);
            }
            default -> {return "Unknown command: " + input;}
        }
    }

    private String handleAsk() {
        String out;
        // initialize the iterator
        if (qIterator == null) {
            qIterator = currStageQuestions.getShuffledQuestions().iterator();
        }

        if (qIterator.hasNext()) {
            if (currQuestion == null || currQuestion.isAnswered()) { // start questions or move to next question
                currQuestion = qIterator.next();
            }

            if (!currQuestion.hasBeenAsked()) {
                output.clear();
                out = buildQuestion(currQuestion);
            } else if (!currQuestion.isAnswered()) {
                out = "Answer current question first";
            } else {
                out = "";
            }
        } else {
            out = "No question left in current stage.";
        }

        return out;
    }

    private String handleAns(String input) {
        if (currQuestion.isAnswered()) {
            return "Question already answered.";
        } else {
            currQuestion.setAnswered(true);
            answeredQuestions++;

            summaryArea.appendText("\nQuestion no." + answeredQuestions + ": answered.");

            return "Question answered.";
        }
    }

    private String buildQuestion(Question q) {
        StringBuilder sb = new StringBuilder();
        String question = q.getPrompt();
        sb.append("Q: ").append(question);
        sb.append("\n");

        sb.append("Choices:\n");
        String letter;
        String choicePromts;
        for (int i = 0; i < 4; i++) {
            letter = "[" + letterChoices.get(i) + "]: ";
            choicePromts = q.getChoices().get(i);
            sb.append(letter).append(choicePromts);
            sb.append("\n");
        }
        currQuestion.setHasBeenAsked(true);

        return sb.toString();
    }
}
