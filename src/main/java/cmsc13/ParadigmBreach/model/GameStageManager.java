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
import cmsc13.ParadigmBreach.ui.SceneManager;
import cmsc13.ParadigmBreach.ui.Screen;
import cmsc13.ParadigmBreach.util.LifeTracker;
import cmsc13.ParadigmBreach.util.OutputKind;
import cmsc13.ParadigmBreach.view.TerminalOutput;

public class GameStageManager {
    public enum QnaOutcome {
        NONE,
        QUESTION_STARTED,
        ANSWER_ACCEPTED,
        PENGU_ASKED,
        PENGU_OUT_OF_TOKENS,
        PENGU_FAILED,
        PENGU_ANSWERED,
        STAGE_CHANGED
    }

    private TerminalOutput terminalOut;
    private final LifeTracker lifeTracker;
    private final PenguAssistant penguAssistant;

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
    private static final int QUESTIONS_PER_STAGE = 10;
    // private static final int QUESTIONS_PER_STAGE = Integer.MAX_VALUE; // Zero-constraints version

    private static final List<Character> letterChoices = List.of('a', 'b', 'c', 'd');

    public GameStageManager(TerminalOutput terminalOut, LifeTracker lifeTracker, PenguAssistant penguAssistant) {
        this.terminalOut = terminalOut;
        this.lifeTracker = lifeTracker;
        this.penguAssistant = penguAssistant;
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
                    terminalOut.append("Usage: stage < 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 >", OutputKind.RESPONSE);
                    break;
                } else if (inArray.length > 2) {
                    terminalOut.append("Too many arguments: " + input, OutputKind.ERROR);
                    terminalOut.append("Usage: stage < 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 >", OutputKind.RESPONSE);
                    break;
                }
                return setStage(inArray[1]) ? QnaOutcome.STAGE_CHANGED : QnaOutcome.NONE;
            }
            case "pngu" -> {
                if (inArray.length < 2) {
                    terminalOut.append("Too few arguments: " + input, OutputKind.ERROR);
                    appendPenguUsage();
                    return QnaOutcome.PENGU_FAILED;
                } else if (inArray.length > 2) {
                    terminalOut.append("Too many arguments: " + input, OutputKind.ERROR);
                    appendPenguUsage();
                    return QnaOutcome.PENGU_FAILED;
                }
                return handlePenguInput(inArray[1]);
            }
        }

        return QnaOutcome.NONE;
    }

    private QnaOutcome handlePenguInput(String argument) {
        switch (argument) {
            case "-peek" -> {
                if (!hasActiveQuestion()) {
                    terminalOut.append("Ask an unanswered question before asking Pengu for a peek.", OutputKind.ERROR);
                    return QnaOutcome.PENGU_FAILED;
                }
                if (!penguAssistant.spendPeekToken()) {
                    terminalOut.append("Pengu is out of peek tokens for this stage.", OutputKind.ERROR);
                    return QnaOutcome.PENGU_OUT_OF_TOKENS;
                }

                int choiceIndex = penguAssistant.getThoughtChoiceIndex(currQuestion);
                appendPenguChoice("Pengu thinks the answer is", choiceIndex);
                return QnaOutcome.PENGU_ASKED;
            }
            case "-copy" -> {
                if (!hasActiveQuestion()) {
                    terminalOut.append("Ask an unanswered question before letting Pengu answer.", OutputKind.ERROR);
                    return QnaOutcome.PENGU_FAILED;
                }

                int choiceIndex = penguAssistant.getCopyChoiceIndex(currQuestion);
                appendPenguChoice("Pengu answers", choiceIndex);
                handleAns(letterChoices.get(choiceIndex));
                return QnaOutcome.PENGU_ANSWERED;
            }
            default -> {
                terminalOut.append("Unknown Pengu command: " + argument, OutputKind.ERROR);
                appendPenguUsage();
                return QnaOutcome.PENGU_FAILED;
            }
        }
    }

    private boolean hasActiveQuestion() {
        return currQuestion != null && currQuestion.hasBeenAsked() && !currQuestion.isAnswered();
    }

    private void appendPenguChoice(String prefix, int choiceIndex) {
        char letter = letterChoices.get(choiceIndex);
        String answer = currQuestion.getChoices().get(choiceIndex);
        terminalOut.append(prefix + " [" + letter + "]: " + answer, OutputKind.RESPONSE);
    }

    private void appendPenguUsage() {
        terminalOut.append("Usage: pngu -peek | pngu -copy", OutputKind.RESPONSE);
    }

    public int getHealth() {
        return lifeTracker.getLivesRemaining();
    }

    public int getPenguTokensRemaining() {
        return penguAssistant.getTokensRemaining();
    }

    private boolean handleAsk() {
        if (lifeTracker.getLivesRemaining() == 0) {
            terminalOut.append("This stage is over. Switch stages to continue.", OutputKind.ERROR);
            return false;
        }

        // initialize the iterator
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
                return true;
            } else if (!currQuestion.isAnswered()) {
                terminalOut.append("Answer current question first", OutputKind.ERROR);
            } 
        } else {
            terminalOut.append("No questions left in current stage. Module complete!", OutputKind.CORRECT);
        }

        return false;
    }

    //  cheatcode
    public boolean setStage(String stageKey) {
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
        lifeTracker.reset();
        penguAssistant.resetForStage();
        
        terminalOut.clear();
        terminalOut.append("Successfully switched to stage: " + stageKey.toUpperCase(), OutputKind.CORRECT);
        // terminalOut.append("To switch stages, type \n'stage < 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 >'", OutputKind.RESPONSE);
        terminalOut.append("Type 'ask' to begin questions for this stage.", OutputKind.RESPONSE);
        return true;
    }

    private void handleAns(char lettChoice) {
        if (lettChoice == letterChoices.get(currQuestion.getCorrectChoiceIndx())) {
            answeredQuestions++;
            terminalOut.appendSummary("\nQuestion no." + answeredQuestions + ": correct.", OutputKind.CORRECT);

            terminalOut.append("CORRECT! You choose the correct answer.", OutputKind.CORRECT);
            AudioManager.get().playSFX(SoundEffect.CORRECT);
            lifeTracker.refresh();
        } else { // wrong answer
            answeredQuestions++;
            terminalOut.appendSummary("\nQuestion no." + answeredQuestions + ": wrong.", OutputKind.ERROR);

            terminalOut.append("Wrong! You choose the wrong answer:(", OutputKind.ERROR);
            AudioManager.get().playSFX(SoundEffect.WRONG);
            loseHealth();
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
            // Automatically advance to the next stage via transition cutscene
            String nextStageKey = STAGE_SEQUENCE.get(currentIndex + 1);
            terminalOut.append("Stage clear\n", OutputKind.RESPONSE);
            
            List<CutsceneSlide> transitionSlides = List.of(
                new CutsceneSlide("/images/tutorial_images/terminal_screenshot.png", "Stage " + currentStageKey.toUpperCase() + " cleared"),
                new CutsceneSlide("/images/tutorial_images/desktop.png", "Next stage: STAGE " + nextStageKey.toUpperCase())
            );

            SceneManager.get().openCutsceneWindow(
                Screen.CUTSCENE,
                transitionSlides,
                () -> {
                    setStage(nextStageKey);
                }
            );
        }
    }

    private void triggerWinCondition() {
        terminalOut.append("YOU WIN", OutputKind.CORRECT);

        List<CutsceneSlide> winSlides = List.of(
            new CutsceneSlide("/images/splash_arts/ascii-art-text.png", "CLEAR"),
            new CutsceneSlide("/images/tutorial_images/splash_screen.png", "Lorem ipsum"),
            new CutsceneSlide("/images/desktop_wallpaper_hex.png", "Lorem ipsum")
        );

        SceneManager.get().openCutsceneWindow(
            Screen.CUTSCENE,
            winSlides,
            () -> {
                terminalOut.append("Thanks for playing", OutputKind.CORRECT);
            }
        );
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
        sb.append("Q: \n").append(question);
        sb.append("\n");

        sb.append("Choices:\n");
        String letter;
        String choicePrompts;
        for (int i = 0; i < 4; i++) {
            letter = "[" + letterChoices.get(i) + "]: ";
            choicePrompts = q.getChoices().get(i);
            sb.append(letter).append("\n");
            sb.append(choicePrompts);
            sb.append("\n");
        }
        currQuestion.setHasBeenAsked(true);

        return sb.toString();
    }
}
