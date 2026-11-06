package cmsc13.ParadigmBreach.util;

import java.util.Objects;
import java.util.function.IntConsumer;

/** Tracks the two peek tokens available during each stage. */
public final class PenguTokenTracker {
    public static final int TOKENS_PER_STAGE = 2;

    private final IntConsumer onTokensChanged;
    private int tokensRemaining;

    public PenguTokenTracker(IntConsumer onTokensChanged) {
        this.onTokensChanged = Objects.requireNonNull(onTokensChanged);
        resetForStage();
    }

    public int getTokensRemaining() {
        return tokensRemaining;
    }

    public void resetForStage() {
        tokensRemaining = TOKENS_PER_STAGE;
        onTokensChanged.accept(tokensRemaining);
    }

    /** Restores a saved token count and republishes it to the UI. */
    public void restore(int savedTokens) {
        if (savedTokens < 0 || savedTokens > TOKENS_PER_STAGE) {
            throw new IllegalArgumentException("Saved tokens must be between 0 and " + TOKENS_PER_STAGE);
        }
        tokensRemaining = savedTokens;
        onTokensChanged.accept(tokensRemaining);
    }

    public boolean spendToken() {
        if (tokensRemaining == 0) {
            return false;
        }

        tokensRemaining--;
        onTokensChanged.accept(tokensRemaining);
        return true;
    }
}
