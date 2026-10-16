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
    private TextField input;

    private QuestionBank currStageQuestions;

    private Question currQuestion;

    private List<Question> qList;
    private Iterator<Question> qIterator;

    public GameStageManager(TextArea output, TextField input) {
        this.output = output;
        this.input = input;
        currStageQuestions = new OOPStage();
    }

    public String handleAsk() {
        if (qList == null) {
            qList = currStageQuestions.getShuffledQuestions();
            qIterator = qList.iterator();
        }

        if (!qIterator.hasNext()) {
            return "No question left in current stage.";
        }

        while (qIterator.hasNext()) {
            if (currQuestion == null || !currQuestion.isAnswered()) {
                currQuestion = qIterator.next();
                return buildQuestion(currQuestion);
            } else {
                return "Question already asked.";
            }
        }

        return "No more questions";
    }

    private String buildQuestion(Question q) {
        List<String> letterChoices = List.of("[a]", "[b]", "[c]", "[c]");
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
