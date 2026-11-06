package cmsc13.ParadigmBreach.model;

import java.util.Objects;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.IntConsumer;

import cmsc13.ParadigmBreach.model.questionBank.Question;
import cmsc13.ParadigmBreach.util.PenguTokenTracker;

/** Chooses and remembers Pengu's answer for each question. */
public final class PenguAssistant {
    private final PenguTokenTracker tokenTracker;

    private Question thoughtQuestion;
    private int thoughtChoiceIndex = -1;

    public PenguAssistant(IntConsumer onTokensChanged) {
        tokenTracker = new PenguTokenTracker(onTokensChanged);
    }

    public int getTokensRemaining() {
        return tokenTracker.getTokensRemaining();
    }

    public void restoreTokens(int savedTokens) {
        tokenTracker.restore(savedTokens);
    }

    public boolean spendPeekToken() {
        return tokenTracker.spendToken();
    }

    public void resetForStage() {
        thoughtQuestion = null;
        thoughtChoiceIndex = -1;
        tokenTracker.resetForStage();
    }

    public String getThoughtQuestionId() {
        return thoughtQuestion == null ? null : thoughtQuestion.getId();
    }

    public int getSavedThoughtChoiceIndex() {
        return thoughtChoiceIndex;
    }

    public void restoreThought(Question question, int choiceIndex) {
        if (question == null) {
            if (choiceIndex != -1) {
                throw new IllegalArgumentException("A Pengu thought choice requires a question");
            }
            thoughtQuestion = null;
            thoughtChoiceIndex = -1;
            return;
        }
        if (choiceIndex < 0 || choiceIndex > 3) {
            throw new IllegalArgumentException("Pengu thought choice must be between 0 and 3");
        }
        thoughtQuestion = question;
        thoughtChoiceIndex = choiceIndex;
    }

    /** Returns the same thought on repeated peeks for a given question. */
    public int getThoughtChoiceIndex(Question question) {
        Objects.requireNonNull(question);
        if (thoughtQuestion != question) {
            thoughtQuestion = question;
            thoughtChoiceIndex = chooseChoiceIndex(question, 2);
        }
        return thoughtChoiceIndex;
    }

    /** Copies a separate guess with a one-in-four chance of being correct. */
    public int getCopyChoiceIndex(Question question) {
        Objects.requireNonNull(question);
        return chooseChoiceIndex(question, 4);
    }

    private int chooseChoiceIndex(Question question, int correctChanceDenominator) {
        int correctIndex = question.getCorrectChoiceIndx();
        if (ThreadLocalRandom.current().nextInt(correctChanceDenominator) == 0) {
            return correctIndex;
        }

        // Select uniformly from the other three choices.
        return (correctIndex + 1 + ThreadLocalRandom.current().nextInt(3)) % 4;
    }
}
