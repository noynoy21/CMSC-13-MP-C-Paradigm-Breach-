package cmsc13.ParadigmBreach.model;

import java.util.Iterator;
import java.util.List;

import cmsc13.ParadigmBreach.audio.AudioManager;
import cmsc13.ParadigmBreach.audio.SoundEffect;
import cmsc13.ParadigmBreach.model.questionBank.Question;
import cmsc13.ParadigmBreach.model.questionBank.QuestionBank;
import cmsc13.ParadigmBreach.model.questionBank.Stage0Questions;
import cmsc13.ParadigmBreach.model.questionBank.Stage1Questions;
import cmsc13.ParadigmBreach.model.questionBank.Stage2Questions;
import cmsc13.ParadigmBreach.model.questionBank.Stage3Questions;
import cmsc13.ParadigmBreach.model.questionBank.Stage4Questions;
import cmsc13.ParadigmBreach.model.questionBank.Stage5Questions;
import cmsc13.ParadigmBreach.model.questionBank.Stage6Questions;
import cmsc13.ParadigmBreach.model.questionBank.Stage7Questions;
import cmsc13.ParadigmBreach.util.LifeTracker;
import cmsc13.ParadigmBreach.util.OutputKind;
import cmsc13.ParadigmBreach.view.TerminalOutput;

public class GameStageManager {
    public enum QnaOutcome {
        NONE,
        QUESTION_STARTED,
        ANSWER_ACCEPTED,
        STAGE_CHANGED
    }

    private TerminalOutput terminalOut;
    private final LifeTracker lifeTracker;

    private QuestionBank currStageQuestions;
    private Question currQuestion;
    private int answeredQuestions = 0;
    private Iterator<Question> qIterator;

    private static final List<Character> letterChoices = List.of('a', 'b', 'c', 'd');

    public GameStageManager(TerminalOutput terminalOut, LifeTracker lifeTracker) {
        this.terminalOut = terminalOut;
        this.lifeTracker = lifeTracker;
        setStage("0");
    }

    public QnaOutcome handleQnaInput(String input) {
        String[] inArray = input.toLowerCase().trim().split("\\s+");
        String command = inArray[0];

        switch (command) {
            case "ask" -> {
                if (inArray.length > 1) {
                    terminalOut.append("Too many arguments: " + input, OutputKind.ERROR);
                    terminalOut.append("command only needs 0 arguments: ask", OutputKind.RESPONSE);
                    return QnaOutcome.NONE;
                }
                return handleAsk() ? QnaOutcome.QUESTION_STARTED : QnaOutcome.NONE;
            }
            case "ans" -> {
                if (canAnswer(input)) {
                    handleAns(inArray[1].charAt(0));
                    return QnaOutcome.ANSWER_ACCEPTED;
                }
                return QnaOutcome.NONE;
            }
            case "stage" -> {
                if (inArray.length < 2) {
                    terminalOut.append("Too few arguments: " + input, OutputKind.ERROR);
                    terminalOut.append("Usage: stage < 0 | 1 | 2 | 3 >", OutputKind.RESPONSE);
                    return QnaOutcome.NONE;
                }
                return setStage(inArray[1]) ? QnaOutcome.STAGE_CHANGED : QnaOutcome.NONE;
            }
        }

        return QnaOutcome.NONE;
    }

    public int getHealth() {
        return lifeTracker.getLivesRemaining();
    }

    private boolean handleAsk() {
        if (lifeTracker.getLivesRemaining() == 0) {
            terminalOut.append("This stage is over. Switch stages to continue.", OutputKind.ERROR);
            return false;
        }

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
                return true;
            } else if (!currQuestion.isAnswered()) {
                terminalOut.append("Answer current question first", OutputKind.ERROR);
            } 
        } else {
            terminalOut.append("No question left in current stage.", OutputKind.ERROR);
        }

        return false;
    }

    public boolean setStage(String stageKey) {
        switch (stageKey) {
            case "0", "stage_0" -> currStageQuestions = new Stage0Questions();
            case "1", "stage_1" -> currStageQuestions = new Stage1Questions();
            case "2", "stage_2" -> currStageQuestions = new Stage2Questions();
            case "3", "stage_3" -> currStageQuestions = new Stage3Questions();
            case "4", "stage_4" -> currStageQuestions = new Stage4Questions();
            case "5", "stage_5" -> currStageQuestions = new Stage5Questions();
            case "6", "stage_6" -> currStageQuestions = new Stage6Questions();
            case "7", "stage_7" -> currStageQuestions = new Stage7Questions();
            // case "c", "stage_c" -> currStageQuestions = new StageCQuestions();
            // case "j", "stage_j" -> currStageQuestions = new StageJQuestions();
            // case "p", "stage_p" -> currStageQuestions = new StagePQuestions();
            // case "s", "stage_s" -> currStageQuestions = new StageSQuestions();
            default -> {
                terminalOut.append("Unknown stage: " + stageKey + ". Defaulting to OOPStage.", OutputKind.ERROR);
                stageKey = "0";
                currStageQuestions = new Stage0Questions();
            }
        }

        qIterator = null;
        currQuestion = null;
        answeredQuestions = 0;
        lifeTracker.reset();
        
        terminalOut.clear();
        terminalOut.append("Successfully switched to stage: " + stageKey.toUpperCase(), OutputKind.CORRECT);
        terminalOut.append("To switch stages, type \n'stage < 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 >'\n Type 'ask' to begin questions for this stage.", OutputKind.RESPONSE);
        return true;
    }

    private void handleAns(char lettChoice) {
        // input validation.
        if (lettChoice == letterChoices.get(currQuestion.getCorrectChoiceIndx())){ // correct answer
            answeredQuestions++;

            terminalOut.appendSummary("\nQuestion no." + answeredQuestions + ": correct.", OutputKind.CORRECT);

            terminalOut.append("CORRECT! You choose the correct answer.", OutputKind.CORRECT);
            lifeTracker.refresh();
        } else { // wrong answer
            answeredQuestions++;

            terminalOut.appendSummary("\nQuestion no." + answeredQuestions + ": wrong.", OutputKind.ERROR);

            terminalOut.append("Wrong! You choose the wrong answer:(", OutputKind.ERROR);
            loseHealth();
        }

        
        currQuestion.setAnswered(true);
        terminalOut.append("Question answered.", OutputKind.RESPONSE);
    }

    public void timeoutCurrentQuestion() {
        if (currQuestion == null || currQuestion.isAnswered() || !currQuestion.hasBeenAsked()) {
            return;
        }

        currQuestion.setAnswered(true);
        answeredQuestions++;
        terminalOut.append("Time is up. This question was marked as unanswered.", OutputKind.ERROR);
        terminalOut.appendSummary("\nQuestion no." + answeredQuestions + ": timed out.", OutputKind.ERROR);
        loseHealth();
    }

    private void loseHealth() {
        int health = lifeTracker.loseLife();
        if (health == 0) {
            terminalOut.append("Stage failed. Switch stages to start again.", OutputKind.ERROR);
        }
    }

    private boolean canAnswer(String input) {
        String[] inArray = input.toLowerCase().trim().split("\\s+");

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
