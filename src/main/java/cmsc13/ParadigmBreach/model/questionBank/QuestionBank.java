package cmsc13.ParadigmBreach.model.questionBank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuestionBank {
    protected List<Question> listOfQuestions; // set of initial questions
    protected List<Question> shuffledQuestions;

    public QuestionBank(List<Question> list) {
        listOfQuestions = list;
        shuffleQuestions();
    }

    public void shuffleQuestions() {
        shuffledQuestions = new ArrayList<>(listOfQuestions);
        Collections.shuffle(shuffledQuestions);
    }

    public Question askQuestion() {
        if (shuffledQuestions.isEmpty()) throw new IllegalStateException("All questions were already asked!");
        return shuffledQuestions.remove(0);
    }
}
