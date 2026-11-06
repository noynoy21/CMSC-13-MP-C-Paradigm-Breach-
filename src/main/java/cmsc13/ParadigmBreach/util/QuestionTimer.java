package cmsc13.ParadigmBreach.util;

import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.function.IntConsumer;

import javafx.animation.Animation;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.util.Duration;

/** A JavaFX countdown timer for a single question. Use its methods on the FX thread. */
public final class QuestionTimer {
    private final int durationSeconds;
    private final IntConsumer onTick;
    private final Runnable onTimeout;
    private final Timeline timeline;

    private long deadlineNanos;
    private long remainingNanos;
    private boolean running;

    public QuestionTimer(int durationSeconds, IntConsumer onTick, Runnable onTimeout) {
        if (durationSeconds <= 0) {
            throw new IllegalArgumentException("durationSeconds must be greater than zero");
        }

        this.durationSeconds = durationSeconds;
        this.onTick = Objects.requireNonNull(onTick);
        this.onTimeout = Objects.requireNonNull(onTimeout);
        this.timeline = new Timeline(
            new KeyFrame(Duration.seconds(1), event -> updateCountdown())
        );
        this.timeline.setCycleCount(Animation.INDEFINITE);
    }

    public void start() {
        stop();
        remainingNanos = TimeUnit.SECONDS.toNanos(durationSeconds);
        resume();
    }

    /** Pauses the countdown while its terminal window is hidden. */
    public void pause() {
        if (!running) {
            return;
        }

        remainingNanos = deadlineNanos - System.nanoTime();
        if (remainingNanos <= 0) {
            expire();
            return;
        }

        running = false;
        timeline.stop();
    }

    /** Resumes a paused countdown without resetting its remaining time. */
    public void resume() {
        if (running || remainingNanos <= 0) {
            return;
        }

        deadlineNanos = System.nanoTime() + remainingNanos;
        running = true;
        onTick.accept((int) ((remainingNanos + TimeUnit.SECONDS.toNanos(1) - 1)
            / TimeUnit.SECONDS.toNanos(1)));
        timeline.playFromStart();
    }

    public void stop() {
        running = false;
        remainingNanos = 0;
        timeline.stop();
    }

    public boolean isRunning() {
        return running;
    }

    /** Checks the deadline before input is handled, so a late answer cannot win the next UI pulse. */
    public void expireIfDue() {
        if (running && System.nanoTime() >= deadlineNanos) {
            expire();
        }
    }

    private void updateCountdown() {
        if (!running) {
            return;
        }

        long remainingNanos = deadlineNanos - System.nanoTime();
        if (remainingNanos <= 0) {
            expire();
            return;
        }

        long nanosPerSecond = TimeUnit.SECONDS.toNanos(1);
        int remainingSeconds = (int) ((remainingNanos + nanosPerSecond - 1) / nanosPerSecond);
        onTick.accept(remainingSeconds);
    }

    private void expire() {
        if (!running) {
            return;
        }

        stop();
        onTimeout.run();
    }
}
