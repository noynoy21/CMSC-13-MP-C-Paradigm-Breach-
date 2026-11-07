package cmsc13.ParadigmBreach.view;

import java.util.Objects;

import cmsc13.ParadigmBreach.model.PlayerSession;
import cmsc13.ParadigmBreach.util.OutputKind;
import javafx.application.Platform;
import javafx.scene.control.ScrollPane;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

public final class TerminalOutput {

    private final TextFlow flow;
    private final ScrollPane mainScroll;
    private final TextFlow commandSummaryFlow;
    private final ScrollPane commandSummaryScroll;
    private final TextFlow summaryFlow;
    private final ScrollPane summaryScroll;
    private boolean handlingCommand;
    private String currentCommand;

    public TerminalOutput(
        TextFlow flow,
        ScrollPane mainScroll,
        TextFlow commandSummaryFlow,
        ScrollPane commandSummaryScroll,
        TextFlow summaryFlow,
        ScrollPane summaryScroll
    ) {
        this.flow = Objects.requireNonNull(flow);
        this.mainScroll = Objects.requireNonNull(mainScroll);
        this.commandSummaryFlow = Objects.requireNonNull(commandSummaryFlow);
        this.commandSummaryScroll = Objects.requireNonNull(commandSummaryScroll);
        this.summaryFlow = Objects.requireNonNull(summaryFlow);
        this.summaryScroll = Objects.requireNonNull(summaryScroll);
        clearCommandSummary();
    }

    public void beginCommand(String command) {
        currentCommand = Objects.requireNonNull(command);
        handlingCommand = true;
        appendToCommandSummary("\n> " + command, OutputKind.COMMAND);
    }

    public void endCommand() {
        handlingCommand = false;
        currentCommand = null;
    }

    public void append(String message, OutputKind kind) {
        Objects.requireNonNull(message);
        Objects.requireNonNull(kind);

        if (handlingCommand) {
            appendToCommandSummary(message, kind);
            // Keep the active question visible above the interaction log as well.
            if (kind == OutputKind.QUESTION) {
                appendToOutput(message, kind);
            }
        } else {
            appendToOutput(message, kind);
        }
    }

    public void appendToOutputFlow(String message, OutputKind kind) {
        Objects.requireNonNull(message);
        Objects.requireNonNull(kind);
        appendToOutput(message, kind);
    }

    public void clear() {
        flow.getChildren().clear();
        clearCommandSummary();
    }

    public void appendSummary(String message, OutputKind kind) {
        Objects.requireNonNull(message);
        Objects.requireNonNull(kind);

        Text text = createText(message, kind);
        summaryFlow.getChildren().add(text);

        scrollToBottom(summaryScroll, summaryFlow);
    }

    private Text createText(String message, OutputKind kind) {
        Text text = new Text(message);
        if (!message.endsWith("\n")) {
            text.setText(message + "\n");
        }
        text.getStyleClass().add(kind.cssClass());
        return text;
    }

    public void clearSummary() {
        summaryFlow.getChildren().clear();
    }

    public void clearCommandSummary() {
        commandSummaryFlow.getChildren().clear();
        appendToCommandSummary(
            PlayerSession.getPlayerName() + "@Celeryon PDBR64 ~",
            OutputKind.QUESTION
        );
        if (handlingCommand && currentCommand != null) {
            appendToCommandSummary("\n> " + currentCommand, OutputKind.COMMAND);
        }
    }

    private void appendToOutput(String message, OutputKind kind) {
        flow.getChildren().add(createText(message, kind));
        scrollToBottom(mainScroll, flow);
    }

    private void appendToCommandSummary(String message, OutputKind kind) {
        commandSummaryFlow.getChildren().add(createText(message, kind));
        scrollToBottom(commandSummaryScroll, commandSummaryFlow);
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
