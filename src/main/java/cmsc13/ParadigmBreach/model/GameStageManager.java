package cmsc13.ParadigmBreach.model;

import java.util.Iterator;
import java.util.List;

import cmsc13.ParadigmBreach.model.questionBank.Question;
import cmsc13.ParadigmBreach.model.questionBank.QuestionBank;
import cmsc13.ParadigmBreach.model.questionBank.Stage1Questions;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;

public class GameStageManager {

    private TextArea output;
    private TextField input;

    private QuestionBank currStageQuestions;

    private Question currQuestion;

    public GameStageManager(TextArea output, TextField input) {
        this.output = output;
        this.input = input;
        currStageQuestions = new Stage1Questions();
    }

    public String handleAsk() {
        List<Question> qList = currStageQuestions.getShuffledQuestions();
        Iterator<Question> qIterator = qList.iterator();
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
