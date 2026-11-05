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

    public boolean spendPeekToken() {
        return tokenTracker.spendToken();
    }

    public void resetForStage() {
        thoughtQuestion = null;
        thoughtChoiceIndex = -1;
        tokenTracker.resetForStage();
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
