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

    public void handleQnaInput(String input) {
        String[] inArray = input.toLowerCase().split(" ");
        String command = inArray[0];

        switch (command) {
            case "ask" -> {
                // ask doesn't need any argument
                if (inArray.length > 1) {
                    terminalOut.append("Too many arguments: " + input, OutputKind.ERROR);
                    terminalOut.append("command only needs 0 argmuents: ask", OutputKind.RESPONSE);
                    break;
                }

                handleAsk();
            }
            case "ans" -> {
                // ask needs exactly one argmunet
                if (canAnswer(input)) {
                    handleAns(inArray[1].charAt(0));
                }
            }
            // default -> { terminalOut.append("Unknown command: " + input, OutputKind.ERROR); }
        }
    }

    private void handleAsk() {
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
                String questionStr = buildQuestion(currQuestion);
                terminalOut.append(questionStr, OutputKind.QUESTION);
                AudioManager.get().playSFX(SoundEffect.BEEP_COUNTDOWN);
            } else if (!currQuestion.isAnswered()) {
                terminalOut.append("Answer current question first", OutputKind.ERROR);
            } 
        } else {
            terminalOut.append("No question left in current stage.", OutputKind.ERROR);
        }
    }

    private void handleAns(char lettChoice) {
        // input validation.
        if (lettChoice == letterChoices.get(currQuestion.getCorrectChoiceIndx())){ // correct answer
            answeredQuestions++;

            terminalOut.appendSummary("\nQuestion no." + answeredQuestions + ": correct.", OutputKind.CORRECT);

            terminalOut.append("CORRECT! You choose the correct answer.", OutputKind.CORRECT);
        } else { // wrong answer
            answeredQuestions++;

            terminalOut.appendSummary("\nQuestion no." + answeredQuestions + ": wrong.", OutputKind.ERROR);

            terminalOut.append("Wrong! You choose the wrong answer:(", OutputKind.ERROR);
        }

        
        currQuestion.setAnswered(true);
        terminalOut.append("Question answered.", OutputKind.RESPONSE);
    }

    private boolean canAnswer(String input) {
        String[] inArray = input.toLowerCase().split(" ");

        // if currentQuestion state is valid.
        if (currQuestion == null) {
            terminalOut.append("No question has been asked.", OutputKind.ERROR);
            terminalOut.append("Try the command: ask", OutputKind.ERROR);
            return false;
        } 
        if (currQuestion.isAnswered()) {
            terminalOut.append("Question already answered.", OutputKind.ERROR);
            return false;
        }

        // iif user input is valid.
        if (inArray.length < 2) {
            terminalOut.append("Too few arguments: " + input, OutputKind.ERROR);
            terminalOut.append("command only needs 1 argmuent: ans _", OutputKind.RESPONSE);
            return false;
        }
        if (inArray.length > 2) {
            terminalOut.append("Too many arguments: " + input, OutputKind.ERROR);
            terminalOut.append("command only needs 1 argmuent: ans _", OutputKind.RESPONSE);
            return false;
        }
        String arg = inArray[1];
        if (arg.length() != 1) {
            terminalOut.append("Invalid argument: " + input, OutputKind.ERROR);
            terminalOut.append("command argument must only be 1 letter choice.", OutputKind.RESPONSE);
            return false;
        }
        if (!letterChoices.contains(arg.charAt(0))) {
            terminalOut.append("Invalid argument: " + input, OutputKind.ERROR);
            terminalOut.append("letter choice must be among of: a | b | c | d.", OutputKind.RESPONSE);
            return false;
        }

        // if input passes all tests:
        return true;
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
