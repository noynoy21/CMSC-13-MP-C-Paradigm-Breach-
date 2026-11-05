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

    private Iterator<Question> qIterator;

    private static final List<Character> letterChoices = List.of('a', 'b', 'c', 'd');

    // --- Game Loop Configuration ---
    private static final int QUESTIONS_PER_STAGE = 10;
    private final List<String> campaignStages = List.of("1", "2", "3", "4", "5", "6", "7");
    private int currentStageIndex = 0;
    private int stageQuestionCount = 0;
    private int totalStageQuestions = 0;

    public GameStageManager(TerminalOutput terminalOut) {
        this.terminalOut = terminalOut;
        // Start the game loop at Stage 1 (or use Stage 0 for trial/tutorial)
        setStage(campaignStages.get(currentStageIndex));
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
                    terminalOut.append("Usage: stage < 1 | 2 | 3 | 4 | 5 | 6 | 7 >", OutputKind.RESPONSE);
                    break;
                }
                setStage(inArray[1]);
            }
        }
    }

    private void handleAsk() {
        if (qIterator == null || !qIterator.hasNext()) {
            terminalOut.append("Module complete or no active questions. Use 'stage <num>' to switch or restart.", OutputKind.ERROR);
            return;
        }

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
    }

    public void setStage(String stageKey) {
        String normalizedKey = stageKey.toLowerCase().replace("stage_", "").replace("stage", "").trim();

        switch (normalizedKey) {
            case "0" -> currStageQuestions = new Stage0Questions();
            case "1" -> currStageQuestions = new Stage1Questions();
            case "2" -> currStageQuestions = new Stage2Questions();
            case "3" -> currStageQuestions = new Stage3Questions();
            case "4" -> currStageQuestions = new Stage4Questions();
            case "5" -> currStageQuestions = new Stage5Questions();
            case "6" -> currStageQuestions = new Stage6Questions();
            case "7" -> currStageQuestions = new Stage7Questions();
            default -> {
                terminalOut.append("Unknown stage: " + stageKey + ". Defaulting to Stage 1.", OutputKind.ERROR);
                currStageQuestions = new Stage1Questions();
                normalizedKey = "1";
            }
        }

        // Sync campaign tracker if a campaign stage is selected
        if (campaignStages.contains(normalizedKey)) {
            currentStageIndex = campaignStages.indexOf(normalizedKey);
        }

        // Randomly pick up to QUESTIONS_PER_STAGE from the shuffled questions pool
        List<Question> allShuffled = currStageQuestions.getShuffledQuestions();
        totalStageQuestions = Math.min(QUESTIONS_PER_STAGE, allShuffled.size());
        List<Question> stagePool = new ArrayList<>(allShuffled.subList(0, totalStageQuestions));
        
        qIterator = stagePool.iterator();
        currQuestion = null;
        answeredQuestions = 0;
        stageQuestionCount = 0;
        
        terminalOut.clear();
        terminalOut.append("Successfully loaded stage: " + normalizedKey.toUpperCase(), OutputKind.CORRECT);
        terminalOut.append("Goal: Answer " + totalStageQuestions + " questions to clear this module.\nType 'ask' to begin.", OutputKind.RESPONSE);
    }

    private void handleAns(char lettChoice) {
        if (lettChoice == letterChoices.get(currQuestion.getCorrectChoiceIndx())) { 
            answeredQuestions++;
            terminalOut.appendSummary("\nQuestion no." + (stageQuestionCount + 1) + ": correct.", OutputKind.CORRECT);
            terminalOut.append("CORRECT! You choose the correct answer.", OutputKind.CORRECT);
            AudioManager.get().playSFX(SoundEffect.CORRECT);
        } else { 
            answeredQuestions++;
            terminalOut.appendSummary("\nQuestion no." + (stageQuestionCount + 1) + ": wrong.", OutputKind.ERROR);
            terminalOut.append("Wrong! You choose the wrong answer:(", OutputKind.ERROR);
            AudioManager.get().playSFX(SoundEffect.WRONG);
        }

        currQuestion.setAnswered(true);
        stageQuestionCount++;
        terminalOut.append("Progress: " + stageQuestionCount + "/" + totalStageQuestions + " questions completed.", OutputKind.RESPONSE);

        // Check if the stage/module quota has been met
        if (stageQuestionCount >= totalStageQuestions) {
            handleStageComplete();
        }
    }

    private void handleStageComplete() {
        String clearedStage = campaignStages.get(currentStageIndex);
        terminalOut.append("\n========================================", OutputKind.CORRECT);
        terminalOut.append("      MODULE " + clearedStage.toUpperCase() + " CLEARED SUCCESSFULLY!      ", OutputKind.CORRECT);
        terminalOut.append("========================================", OutputKind.CORRECT);

        currentStageIndex++;
        if (currentStageIndex < campaignStages.size()) {
            // Automatically advance to the next campaign stage
            String nextStage = campaignStages.get(currentStageIndex);
            terminalOut.append("Initiating breach to Stage " + nextStage + "...\n", OutputKind.RESPONSE);
            setStage(nextStage);
        } else {
            // --- WIN CONDITION ---
            terminalOut.append("\n****************************************", OutputKind.CORRECT);
            terminalOut.append("*          VICTORY! YOU WIN!           *", OutputKind.CORRECT);
            terminalOut.append("*  All Paradigm modules have been broken!*", OutputKind.CORRECT);
            terminalOut.append("****************************************", OutputKind.CORRECT);
            currQuestion = null;
            qIterator = null;
        }
    }

    private boolean canAnswer(String input) {
        String[] inArray = input.toLowerCase().split(" ");

        if (currQuestion == null) {
            terminalOut.append("No question has been asked.", OutputKind.ERROR);
            terminalOut.append("Try the command: ask", OutputKind.ERROR);
            return false;
        } 
        if (currQuestion.isAnswered()) {
            terminalOut.append("Question already answered. Type 'ask' for the next question.", OutputKind.ERROR);
            return false;
        }

        if (inArray.length < 2 || inArray.length > 2) {
            terminalOut.append("Invalid arguments: " + input, OutputKind.ERROR);
            terminalOut.append("Usage: ans <a|b|c|d>", OutputKind.RESPONSE);
            return false;
        }
        
        String arg = inArray[1];
        if (arg.length() != 1 || !letterChoices.contains(arg.charAt(0))) {
            terminalOut.append("Invalid choice: " + input, OutputKind.ERROR);
            terminalOut.append("Letter choice must be among: a | b | c | d.", OutputKind.RESPONSE);
            return false;
        }

        return true;
    }

    private String buildQuestion(Question q) {
        StringBuilder sb = new StringBuilder();
        sb.append("Q: ").append(q.getPrompt()).append("\n");
        sb.append("Choices:\n");
        
        for (int i = 0; i < 4; i++) {
            String letter = "[" + letterChoices.get(i) + "]: ";
            String choicePrompts = q.getChoices().get(i);
            sb.append(letter).append(choicePrompts).append("\n");
        }
        currQuestion.setHasBeenAsked(true);

        return sb.toString();
    }
}