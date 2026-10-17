package cmsc13.ParadigmBreach.model.questionBank;

import java.util.List;

public class Question {
    private String prompt;
    private List<String> choices;
    private int correctChoiceIndx;
    private boolean isAnswered;
    private boolean hasBeenAsked;

    public Question(String prompt, List<String> choices, int correctChoiceIndx) {
        this.prompt = prompt;
        this.choices = choices;
        this.correctChoiceIndx = correctChoiceIndx;
        this.isAnswered = false;
        this.hasBeenAsked = false;
    }

    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }

    public List<String> getChoices() { return choices; }
    public void setChoices(List<String> choices) { this.choices = choices; }

    public int getCorrectChoiceIndx() { return correctChoiceIndx; }
    public void setCorrectChoiceIndx(int correctChoiceIndx) { this.correctChoiceIndx = correctChoiceIndx; }

    public boolean isAnswered() { return isAnswered; }
    public void setAnswered(boolean answered) { isAnswered = answered; }

    public boolean hasBeenAsked() { return hasBeenAsked; }
    public void setHasBeenAsked(boolean hasBeenAsked) { this.hasBeenAsked = hasBeenAsked; }

}
