package cmsc13.ParadigmBreach.model.questionBank;

import java.util.List;

public record Question(
    String prompt,
    List<String> choices,
    int correctChoiceIndx
) {}
