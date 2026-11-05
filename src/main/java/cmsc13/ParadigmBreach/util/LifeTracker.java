package cmsc13.ParadigmBreach.util;

import java.util.Objects;
import java.util.function.IntConsumer;

/** Tracks the player's lives and notifies the UI whenever they change. */
public final class LifeTracker {
    public static final int STARTING_LIVES = 4;

    private final IntConsumer onLivesChanged;
    private int livesRemaining;

    public LifeTracker(IntConsumer onLivesChanged) {
        this.onLivesChanged = Objects.requireNonNull(onLivesChanged);
        reset();
    }

    public int getLivesRemaining() {
        return livesRemaining;
    }

    /** Renders each remaining life as the sidebar's <3 icon. */
    public static String renderHearts(int livesRemaining) {
        return "<3 ".repeat(Math.max(0, livesRemaining)).stripTrailing();
    }

    public void reset() {
        livesRemaining = STARTING_LIVES;
        refresh();
    }

    /** Republishes the current life count so the UI can redraw its hearts. */
    public void refresh() {
        onLivesChanged.accept(livesRemaining);
    }

    /** Removes one life, stopping at zero, then notifies the UI. */
    public int loseLife() {
        if (livesRemaining > 0) {
            livesRemaining--;
            refresh();
        }
        return livesRemaining;
    }
}
