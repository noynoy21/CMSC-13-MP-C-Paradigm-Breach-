package cmsc13.ParadigmBreach.model.questionBank;

import java.util.List;

/*
    Functional Programming Questions
*/
public class Stage3Questions {
    private static Stage3Questions instance;
    private static final List<Question> listOfQuestions = List.of(
                                        new Question(
                                            "Which concept describes functions that can be passed as values?",
                                            List.of("Inheritance", "Higher-order functions", "Encapsulation", "Compilation"),
                                            1
                                        ),
                                        new Question(
                                            "What should a pure function do with its input?",
                                            List.of("Modify it directly", "Print it", "Return a result without side effects", "Store it globally"),
                                            2
                                        )
                                    );

    private Stage3Questions() {}

    public void init() { this.instance = new Stage3Questions(); }

    public Stage3Questions get() {
        if (instance == null) throw new IllegalStateException("Stage3Questions.init() was not called");
        return instance;
    }

    public List<Question> getListOfQuestions() { return this.listOfQuestions; }
}
