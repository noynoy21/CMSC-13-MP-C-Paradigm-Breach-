package cmsc13.ParadigmBreach.model.questionBank;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Question {
    private final String id;
    private String prompt;
    // Keep YAML choice order as the stable identity for validation and saved Pengu thoughts.
    private List<String> choices;
    private int correctChoiceIndx;
    // Maps each displayed position to its stable YAML choice index.
    private List<Integer> displayedChoiceOrder;
    private boolean isAnswered;
    private boolean hasBeenAsked;

    /** Creates a question loaded from a source that provides a stable ID. */
    public Question(String id, String prompt, List<String> choices, int correctChoiceIndx) {
        this.id = id;
        this.prompt = prompt;
        this.choices = new ArrayList<>(choices);
        this.correctChoiceIndx = correctChoiceIndx;
        resetDisplayedChoiceOrder();
        this.isAnswered = false;
        this.hasBeenAsked = false;
    }

    public String getId() { return id; }

    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }

    /** Returns choices in their stable YAML order. */
    public List<String> getChoices() { return choices; }
    public void setChoices(List<String> choices) {
        this.choices = new ArrayList<>(choices);
        resetDisplayedChoiceOrder();
    }

    /** The correct index is always relative to the stable YAML choice order. */
    public int getCorrectChoiceIndx() { return correctChoiceIndx; }
    public void setCorrectChoiceIndx(int correctChoiceIndx) { this.correctChoiceIndx = correctChoiceIndx; }

    /** Shuffles display positions while preserving each choice's stable YAML index. */
    public void shuffleDisplayedChoices() {
        List<Integer> originalOrder = new ArrayList<>(displayedChoiceOrder.size());
        for (int i = 0; i < displayedChoiceOrder.size(); i++) {
            originalOrder.add(i);
        }

        List<Integer> shuffledOrder = new ArrayList<>(originalOrder);
        do {
            Collections.shuffle(shuffledOrder);
        } while (shuffledOrder.size() > 1 && shuffledOrder.equals(originalOrder));
        displayedChoiceOrder = shuffledOrder;
    }

    public String getChoiceAtDisplayIndex(int displayIndex) {
        return choices.get(getOriginalChoiceIndexForDisplay(displayIndex));
    }

    public int getOriginalChoiceIndexForDisplay(int displayIndex) {
        return displayedChoiceOrder.get(displayIndex);
    }

    public int getDisplayIndexForOriginalChoice(int originalChoiceIndex) {
        int displayIndex = displayedChoiceOrder.indexOf(originalChoiceIndex);
        if (displayIndex < 0) {
            throw new IllegalArgumentException("Unknown original choice index: " + originalChoiceIndex);
        }
        return displayIndex;
    }

    private void resetDisplayedChoiceOrder() {
        displayedChoiceOrder = new ArrayList<>(choices.size());
        for (int i = 0; i < choices.size(); i++) {
            displayedChoiceOrder.add(i);
        }
    }

    public boolean isAnswered() { return isAnswered; }
    public void setAnswered(boolean answered) { isAnswered = answered; }

    public boolean hasBeenAsked() { return hasBeenAsked; }
    public void setHasBeenAsked(boolean hasBeenAsked) { this.hasBeenAsked = hasBeenAsked; }

}
