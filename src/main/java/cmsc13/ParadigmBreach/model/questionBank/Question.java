package cmsc13.ParadigmBreach.model.questionBank;

import java.util.List;

public record Question(
    String prompt,
    List<String> choices,
    int correctChoiceIndx,
    boolean isAnswered
) {
    public Question(String prompt, List<String> choices, int correctChoiceIndx) {
        this(prompt, choices, correctChoiceIndx, false);
    }
}
