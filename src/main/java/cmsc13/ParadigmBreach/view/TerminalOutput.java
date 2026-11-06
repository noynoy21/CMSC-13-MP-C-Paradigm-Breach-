package cmsc13.ParadigmBreach.view;

import java.util.Objects;

import cmsc13.ParadigmBreach.util.OutputKind;
import javafx.application.Platform;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public final class TerminalOutput {

    private final TextFlow flow;
    private final ScrollPane mainScroll;
    private final TextFlow summaryFlow;
    private final ScrollPane summaryScroll;

    public TerminalOutput(TextFlow flow, ScrollPane mainScroll, TextFlow summaryFlow, ScrollPane summaryScroll) {
        this.flow = Objects.requireNonNull(flow);
        this.mainScroll = Objects.requireNonNull(mainScroll);
        this.summaryFlow = Objects.requireNonNull(summaryFlow);
        this.summaryScroll = Objects.requireNonNull(summaryScroll);
    }

    public void append(String message, OutputKind kind) {
        Objects.requireNonNull(message);
        Objects.requireNonNull(kind);

        Text text = new Text(message);
        if (!message.endsWith("\n")) {
            text.setText(message + "\n");
        }
        text.getStyleClass().add(kind.cssClass());
        flow.getChildren().add(text);

        scrollToBottom(mainScroll, flow);
    }

    public void clear() {
        flow.getChildren().clear();
    }

    public void appendSummary(String message, OutputKind kind) {
        Objects.requireNonNull(message);
        Objects.requireNonNull(kind);

        Text text = new Text(message);
        if (!message.endsWith("\n")) {
            text.setText(message + "\n");
        }
        text.getStyleClass().add(kind.cssClass());
        summaryFlow.getChildren().add(text);

        scrollToBottom(summaryScroll, summaryFlow);
    }

    public void clearSummary() {
        summaryFlow.getChildren().clear();
    }

    private void scrollToBottom(ScrollPane scroll, TextFlow content) {
        Platform.runLater(() -> {
            scroll.applyCss();
            scroll.layout();
            content.layout();
            scroll.layout();
            scroll.setVvalue(scroll.getVmax());
        });
    }
}