package cmsc13.ParadigmBreach.model;

import java.util.ArrayList;
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
import cmsc13.ParadigmBreach.util.OutputKind;
import cmsc13.ParadigmBreach.view.TerminalOutput;

public class GameStageManager {
    private TerminalOutput terminalOut;

    private QuestionBank currStageQuestions;
    private Question currQuestion;
    private int answeredQuestions = 0;
    private List<Question> activeStageQuestions;
    private Iterator<Question> qIterator;

    private String currentStageKey = "0";
    private static final List<String> STAGE_SEQUENCE = List.of("0", "1", "2", "3", "4", "5", "6", "7");

    // =========================================================================
    // GAME LOOP CONFIGURATION: Question Limit per Stage
    // 
    // To play with the arbitrary limit constraint (e.g., 10 questions):
    //     Keep QUESTIONS_PER_STAGE = 10; and comment out the unlimited line.
    // 
    // To play with the zero-constraints version (all available questions):
    //     Uncomment the Integer.MAX_VALUE line below.
    // =========================================================================
    private static final int QUESTIONS_PER_STAGE = 2;
    // private static final int QUESTIONS_PER_STAGE = Integer.MAX_VALUE; // Zero-constraints version

    private static final List<Character> letterChoices = List.of('a', 'b', 'c', 'd');

    public GameStageManager(TerminalOutput terminalOut) {
        this.terminalOut = terminalOut;
        setStage("0");
    }

    public void handleQnaInput(String input) {
        String[] inArray = input.toLowerCase().split(" ");
        String command = inArray[0];

        switch (command) {
            case "ask" -> {
                if (inArray.length > 1) {
                    terminalOut.append("Too many arguments: " + input, OutputKind.ERROR);
                    terminalOut.append("command only needs 0 arguments: ask", OutputKind.RESPONSE);
                    break;
                }
                handleAsk();
            }
            case "ans" -> {
                if (canAnswer(input)) {
                    handleAns(inArray[1].charAt(0));
                }
            }
            case "stage" -> {
                if (inArray.length < 2) {
                    terminalOut.append("Too few arguments: " + input, OutputKind.ERROR);
                    terminalOut.append("Usage: stage < 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 >", OutputKind.RESPONSE);
                    break;
                }
                setStage(inArray[1]);
            }
        }
    }

    private void handleAsk() {
        if (qIterator == null) {
            qIterator = activeStageQuestions.iterator();
        }

        if (qIterator.hasNext()) {
            if (currQuestion == null || currQuestion.isAnswered()) {
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
            terminalOut.append("No questions left in current stage. Module complete!", OutputKind.CORRECT);
        }
    }

    public void setStage(String stageKey) {
        stageKey = stageKey.toLowerCase();
        // Handle stage_X format conversion
        if (stageKey.startsWith("stage_")) {
            stageKey = stageKey.replace("stage_", "");
        }

        switch (stageKey) {
            case "0" -> currStageQuestions = new Stage0Questions();
            case "1" -> currStageQuestions = new Stage1Questions();
            case "2" -> currStageQuestions = new Stage2Questions();
            case "3" -> currStageQuestions = new Stage3Questions();
            case "4" -> currStageQuestions = new Stage4Questions();
            case "5" -> currStageQuestions = new Stage5Questions();
            case "6" -> currStageQuestions = new Stage6Questions();
            case "7" -> currStageQuestions = new Stage7Questions();
            default -> {
                terminalOut.append("Unknown stage: " + stageKey + ". Defaulting to Stage 0.", OutputKind.ERROR);
                stageKey = "0";
                currStageQuestions = new Stage0Questions();
            }
        }

        this.currentStageKey = stageKey;

        // Apply question limit configuration
        List<Question> rawShuffled = currStageQuestions.getShuffledQuestions();
        int limit = Math.min(QUESTIONS_PER_STAGE, rawShuffled.size());
        activeStageQuestions = new ArrayList<>(rawShuffled.subList(0, limit));

        qIterator = null;
        currQuestion = null;
        answeredQuestions = 0;
        
        terminalOut.clear();
        terminalOut.append("Successfully switched to Stage " + stageKey.toUpperCase(), OutputKind.CORRECT);
        terminalOut.append("Required questions for this stage: " + activeStageQuestions.size(), OutputKind.RESPONSE);
        terminalOut.append("Type 'ask' to begin questions.", OutputKind.RESPONSE);
    }

    private void handleAns(char lettChoice) {
        if (lettChoice == letterChoices.get(currQuestion.getCorrectChoiceIndx())) {
            answeredQuestions++;
            terminalOut.appendSummary("\nQuestion no." + answeredQuestions + ": correct.", OutputKind.CORRECT);
            terminalOut.append("CORRECT! You chose the correct answer.", OutputKind.CORRECT);
        } else {
            answeredQuestions++;
            terminalOut.appendSummary("\nQuestion no." + answeredQuestions + ": wrong.", OutputKind.ERROR);
            terminalOut.append("Wrong! You chose the wrong answer :(", OutputKind.ERROR);
        }

        currQuestion.setAnswered(true);
        terminalOut.append("Question answered.", OutputKind.RESPONSE);

        // Check if stage requirement has been met
        if (answeredQuestions >= activeStageQuestions.size()) {
            checkStageCompletion();
        }
    }

    private void checkStageCompletion() {
        terminalOut.append("\n========================================", OutputKind.CORRECT);
        terminalOut.append("STAGE " + currentStageKey.toUpperCase() + " CLEARED!", OutputKind.CORRECT);
        terminalOut.append("========================================", OutputKind.CORRECT);

        int currentIndex = STAGE_SEQUENCE.indexOf(currentStageKey);
        
        // Check if this was the final stage (Win Condition)
        if (currentIndex == -1 || currentIndex >= STAGE_SEQUENCE.size() - 1) {
            triggerWinCondition();
        } else {
            // Automatically advance to the next stage
            String nextStageKey = STAGE_SEQUENCE.get(currentIndex + 1);
            terminalOut.append("Advancing to next security sector: Stage " + nextStageKey + "...\n", OutputKind.RESPONSE);
            setStage(nextStageKey);
        }
    }

    private void triggerWinCondition() {
        terminalOut.append("\n========================================", OutputKind.CORRECT);
        terminalOut.append("🔥 FIREWALL DEFEATED. SYSTEM BREACHED. 🔥", OutputKind.CORRECT);
        terminalOut.append("You have successfully cleared all stages and broken the paradigm, C1pher!", OutputKind.CORRECT);
        terminalOut.append("========================================", OutputKind.CORRECT);
    }

    private boolean canAnswer(String input) {
        String[] inArray = input.toLowerCase().split(" ");

        if (currQuestion == null) {
            terminalOut.append("No question has been asked.", OutputKind.ERROR);
            terminalOut.append("Try the command: ask", OutputKind.ERROR);
            return false;
        } 
        if (currQuestion.isAnswered()) {
            terminalOut.append("Question already answered.", OutputKind.ERROR);
            return false;
        }

        if (inArray.length < 2) {
            terminalOut.append("Too few arguments: " + input, OutputKind.ERROR);
            terminalOut.append("command only needs 1 argument: ans _", OutputKind.RESPONSE);
            return false;
        }
        if (inArray.length > 2) {
            terminalOut.append("Too many arguments: " + input, OutputKind.ERROR);
            terminalOut.append("command only needs 1 argument: ans _", OutputKind.RESPONSE);
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
            terminalOut.append("letter choice must be among: a | b | c | d.", OutputKind.RESPONSE);
            return false;
        }

        return true;
    }

    private String buildQuestion(Question q) {
        StringBuilder sb = new StringBuilder();
        String question = q.getPrompt();
        sb.append("Q: ").append(question);
        sb.append("\n");

        sb.append("Choices:\n");
        String letter;
        String choicePrompts;
        for (int i = 0; i < 4; i++) {
            letter = "[" + letterChoices.get(i) + "]: ";
            choicePrompts = q.getChoices().get(i);
            sb.append(letter).append(choicePrompts);
            sb.append("\n");
        }
        currQuestion.setHasBeenAsked(true);

        return sb.toString();
    }
}