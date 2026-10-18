package cmsc13.ParadigmBreach.model;

import java.util.Iterator;
import java.util.List;

import cmsc13.ParadigmBreach.audio.AudioManager;
import cmsc13.ParadigmBreach.audio.SoundEffect;
import cmsc13.ParadigmBreach.model.questionBank.OOPStage;
import cmsc13.ParadigmBreach.model.questionBank.Question;
import cmsc13.ParadigmBreach.model.questionBank.QuestionBank;
import cmsc13.ParadigmBreach.util.OutputKind;
import cmsc13.ParadigmBreach.view.TerminalOutput;

public class GameStageManager {
    private TerminalOutput terminalOut;

    private QuestionBank currStageQuestions;
    private Question currQuestion;
    private int answeredQuestions = 0;

    private Iterator<Question> qIterator;

    private static final List<Character> letterChoices = List.of('a', 'b', 'c', 'd');

    public GameStageManager(TerminalOutput terminalOut) {
        this.terminalOut = terminalOut;
        currStageQuestions = new OOPStage();
    }

    public String handleQnaInput(String input) {
        switch (input.toLowerCase().split(" ")[0]) {
            case "ask" -> {
                return handleAsk();
            }
            case "ans" -> {
                return handleAns(input);
            }
            default -> {return "Unknown command: " + input;}
        }
    }

    private String handleAsk() {
        String out;
        // initialize the iterator
        if (qIterator == null) {
            qIterator = currStageQuestions.getShuffledQuestions().iterator();
        }

        if (qIterator.hasNext()) {
            if (currQuestion == null || currQuestion.isAnswered()) { // start questions or move to next question
                currQuestion = qIterator.next();
            }

            if (!currQuestion.hasBeenAsked()) {
                terminalOut.clear();
                out = buildQuestion(currQuestion);
                AudioManager.get().playSFX(SoundEffect.BEEP_COUNTDOWN);
            } else if (!currQuestion.isAnswered()) {
                out = "Answer current question first";
            } else {
                out = "";
            }
        } else {
            out = "No question left in current stage.";
        }

        return out;
    }

    private String handleAns(String input) {
        if (currQuestion.isAnswered()) {
            return "Question already answered.";
        } else {
            currQuestion.setAnswered(true);
            answeredQuestions++;

            terminalOut.appendSummary("\nQuestion no." + answeredQuestions + ": answered.", OutputKind.RESPONSE);

            return "Question answered.";
        }
    }

    private String buildQuestion(Question q) {
        StringBuilder sb = new StringBuilder();
        String question = q.getPrompt();
        sb.append("Q: ").append(question);
        sb.append("\n");

        sb.append("Choices:\n");
        String letter;
        String choicePromts;
        for (int i = 0; i < 4; i++) {
            letter = "[" + letterChoices.get(i) + "]: ";
            choicePromts = q.getChoices().get(i);
            sb.append(letter).append(choicePromts);
            sb.append("\n");
        }
        currQuestion.setHasBeenAsked(true);

        return sb.toString();
    }
}
