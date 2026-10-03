package cmsc13.ParadigmBreach.model.questionBank;

import java.util.List;

/*
    Functional Programming Questions
*/
public class Stage3Questions extends QuestionBank {
    // private static Stage3Questions instance;

    public Stage3Questions() {
        super(List.of(
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
        ));
    }
}
