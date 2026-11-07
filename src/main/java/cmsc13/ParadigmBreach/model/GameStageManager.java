package cmsc13.ParadigmBreach.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import cmsc13.ParadigmBreach.persistence.GameSaveData;
import cmsc13.ParadigmBreach.persistence.GameSaveData.QuestionProgress;
import cmsc13.ParadigmBreach.persistence.GameSaveData.QuestionStatus;
import cmsc13.ParadigmBreach.persistence.GameSaveData.StageSummary;
import cmsc13.ParadigmBreach.persistence.GameSaveStore;
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
    private final GameSaveStore saveRepository;

    private QuestionBank currStageQuestions;
    private Question currQuestion;
    private int answeredQuestions = 0;
    private List<Question> activeStageQuestions;
    private Iterator<Question> qIterator;
    private final Map<String, QuestionProgress> questionProgressById = new LinkedHashMap<>();
    private final List<StageSummary> summaryHistory = new ArrayList<>();
    private long remainingQuestionMillis;
    private String pendingNextStageKey;
    private boolean gameCompleted;
    private boolean restoredFromSave;
    public static final int QUESTION_TIME_LIMIT_SECONDS = 30;
    private static final long MAX_QUESTION_TIME_MILLIS = QUESTION_TIME_LIMIT_SECONDS * 1_000L;

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

    public GameStageManager(
        TerminalOutput terminalOut,
        LifeTracker lifeTracker,
        PenguAssistant penguAssistant,
        GameSaveStore saveRepository
    ) {
        this.terminalOut = terminalOut;
        this.lifeTracker = lifeTracker;
        this.penguAssistant = penguAssistant;
        this.saveRepository = saveRepository;
        initializeFromSave();
    }

    private void initializeFromSave() {
        if (saveRepository == null) {
            setStage("0", false);
            return;
        }

        Optional<GameSaveData> savedData;
        String startupWarning = null;
        try {
            savedData = saveRepository.load();
        } catch (GameSaveData.UnsupportedSaveVersionException e) {
            savedData = Optional.empty();
            startupWarning = "This save uses an unsupported version and was left untouched. "
                + "A fresh session is running.";
        } catch (Exception e) {
            startupWarning = preserveInvalidSave("The saved game could not be read.");
            savedData = Optional.empty();
        }

        if (savedData.isPresent()) {
            try {
                GameSaveData data = savedData.get();
                restoreFromSave(data);
                restoredFromSave = true;
                if (pendingNextStageKey != null) {
                    String nextStage = pendingNextStageKey;
                    pendingNextStageKey = null;
                    terminalOut.append("Recovered the saved stage transition.", OutputKind.RESPONSE);
                    setStage(nextStage, false);
                } else if (gameCompleted) {
                    terminalOut.append("This profile has already completed the game.", OutputKind.CORRECT);
                    terminalOut.append("Type 'stage <number>' to replay a stage.", OutputKind.RESPONSE);
                } else {
                    // terminalOut.append("Saved progress restored for stage " + currentStageKey + ".", OutputKind.CORRECT);
                    
                    terminalOut.append("\n========================================", OutputKind.QUESTION);
                    terminalOut.append("LOADED STAGE " + currentStageKey.toUpperCase() + " FROM SAVE", OutputKind.QUESTION);
                    terminalOut.append("========================================", OutputKind.QUESTION);

                    if (currQuestion != null && !currQuestion.isAnswered()) {
                        terminalOut.append(buildQuestion(currQuestion), OutputKind.QUESTION);
                    } else if (lifeTracker.getLivesRemaining() > 0) {
                        terminalOut.append("Type 'ask' to continue.", OutputKind.RESPONSE);
                    }
                }
            } catch (RuntimeException e) {
                startupWarning = preserveInvalidSave("The saved progress no longer matches the question bank.");
                terminalOut.clear();
                terminalOut.clearSummary();
                summaryHistory.clear();
                setStage("0", false);
            }
        } else {
            setStage("0", false);
        }

        if (startupWarning != null) {
            terminalOut.append(startupWarning, OutputKind.ERROR);
        }
    }

    private String preserveInvalidSave(String reason) {
        try {
            String backupLocation = saveRepository.preserveExistingSave();
            if (backupLocation != null) {
                return reason + " A copy was kept at " + backupLocation + ". Starting a fresh game.";
            }
            return reason + " The original save could not be backed up; starting a fresh game.";
        } catch (Exception backupError) {
            return reason + " The original save could not be backed up; starting a fresh game.";
        }
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
                int displayIndex = currQuestion.getDisplayIndexForOriginalChoice(choiceIndex);
                handleAns(letterChoices.get(displayIndex));
                return QnaOutcome.PENGU_ANSWERED;
            }
            default -> {
                terminalOut.append("Unknown Pengu command: " + argument, OutputKind.ERROR);
                appendPenguUsage();
                return QnaOutcome.PENGU_FAILED;
            }
        }
    }

    private void appendPenguChoice(String prefix, int choiceIndex) {
        int displayIndex = currQuestion.getDisplayIndexForOriginalChoice(choiceIndex);
        char letter = letterChoices.get(displayIndex);
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

    public void restartCurrentStage() {
        setStage(currentStageKey, true);
    }

    public boolean wasRestoredFromSave() {
        return restoredFromSave;
    }

    public boolean hasActiveQuestion() {
        return currQuestion != null
            && currQuestion.hasBeenAsked()
            && !currQuestion.isAnswered()
            && lifeTracker.getLivesRemaining() > 0;
    }

    public long getRemainingQuestionMillis() {
        return remainingQuestionMillis;
    }

    public boolean isGameCompleted() {
        return gameCompleted;
    }

    /** Writes a snapshot only when the player explicitly chooses a destination slot. */
    public boolean saveProgress(GameSaveStore destination, long timerRemainingMillis) {
        if (destination == null) {
            terminalOut.append("Choose a save slot before saving.", OutputKind.ERROR);
            return false;
        }

        remainingQuestionMillis = Math.max(0, Math.min(timerRemainingMillis, MAX_QUESTION_TIME_MILLIS));
        List<QuestionProgress> savedQuestions = new ArrayList<>(activeStageQuestions.size());
        for (Question question : activeStageQuestions) {
            QuestionProgress progress = questionProgressById.get(question.getId());
            if (progress == null) {
                terminalOut.append("Progress could not be saved because the current stage state is incomplete.", OutputKind.ERROR);
                return false;
            }
            savedQuestions.add(progress);
        }

        GameSaveData data = new GameSaveData(
            PlayerSession.getPlayerName(),
            currentStageKey,
            savedQuestions,
            currQuestion == null ? null : currQuestion.getId(),
            lifeTracker.getLivesRemaining(),
            penguAssistant.getTokensRemaining(),
            remainingQuestionMillis,
            pendingNextStageKey,
            gameCompleted,
            penguAssistant.getThoughtQuestionId(),
            penguAssistant.getSavedThoughtChoiceIndex(),
            summaryHistory,
            null
        );

        try {
            destination.save(data);
            return true;
        } catch (Exception e) {
            terminalOut.append("Progress could not be saved: " + e.getMessage(), OutputKind.ERROR);
            return false;
        }
    }

    private void restoreFromSave(GameSaveData data) {
        if (!PlayerSession.getPlayerName().equalsIgnoreCase(data.getPlayerName())) {
            throw new IllegalArgumentException("The save belongs to another player profile");
        }

        currentStageKey = data.getStageKey();
        currStageQuestions = createQuestionBank(data.getStageKey());
        Map<String, Question> questionsById = new HashMap<>();
        for (Question question : currStageQuestions.getShuffledQuestions()) {
            if (questionsById.put(question.getId(), question) != null) {
                throw new IllegalArgumentException("Duplicate question ID in stage " + data.getStageKey());
            }
        }

        if (data.getQuestions().isEmpty()) {
            throw new IllegalArgumentException("A saved stage must contain at least one selected question");
        }

        activeStageQuestions = new ArrayList<>(data.getQuestions().size());
        questionProgressById.clear();
        boolean encounteredUnasked = false;
        boolean encounteredActive = false;
        String expectedCurrentQuestionId = null;
        int startedQuestionCount = 0;
        answeredQuestions = 0;

        for (QuestionProgress progress : data.getQuestions()) {
            Question question = questionsById.get(progress.id());
            if (question == null) {
                throw new IllegalArgumentException("Question ID no longer exists: " + progress.id());
            }
            if (questionProgressById.put(progress.id(), progress) != null) {
                throw new IllegalArgumentException("Duplicate saved question ID: " + progress.id());
            }

            QuestionStatus status = progress.status();
            if (status == QuestionStatus.NOT_ASKED) {
                encounteredUnasked = true;
                if (progress.selectedChoiceIndex() != null) {
                    throw new IllegalArgumentException("Unasked question has a selected answer: " + progress.id());
                }
            } else {
                if (encounteredUnasked) {
                    throw new IllegalArgumentException("Saved question order skips an unasked question");
                }
                if (encounteredActive) {
                    throw new IllegalArgumentException("A saved question follows the active question");
                }
                startedQuestionCount++;
                expectedCurrentQuestionId = progress.id();
                question.setHasBeenAsked(true);
                if (status == QuestionStatus.ACTIVE) {
                    if (encounteredActive || progress.selectedChoiceIndex() != null) {
                        throw new IllegalArgumentException("Invalid active question state: " + progress.id());
                    }
                    encounteredActive = true;
                } else {
                    question.setAnswered(true);
                    answeredQuestions++;
                    boolean hasSelection = progress.selectedChoiceIndex() != null;
                    if ((status == QuestionStatus.CORRECT || status == QuestionStatus.INCORRECT) != hasSelection) {
                        throw new IllegalArgumentException("Saved answer does not match outcome for " + progress.id());
                    }
                }
            }
            activeStageQuestions.add(question);
        }

        if (!java.util.Objects.equals(expectedCurrentQuestionId, data.getCurrentQuestionId())) {
            throw new IllegalArgumentException("Saved current question does not match question progress");
        }
        if (data.getRemainingQuestionMillis() < 0
            || data.getRemainingQuestionMillis() > MAX_QUESTION_TIME_MILLIS
            || (!encounteredActive && data.getRemainingQuestionMillis() != 0)) {
            throw new IllegalArgumentException("Invalid saved timer state");
        }
        if (encounteredActive && data.getLivesRemaining() == 0) {
            throw new IllegalArgumentException("A game-over save cannot have an active question");
        }

        qIterator = activeStageQuestions.iterator();
        for (int i = 0; i < startedQuestionCount; i++) {
            qIterator.next();
        }
        currQuestion = data.getCurrentQuestionId() == null
            ? null
            : questionsById.get(data.getCurrentQuestionId());
        remainingQuestionMillis = data.getRemainingQuestionMillis();
        pendingNextStageKey = data.getPendingNextStageKey();
        gameCompleted = data.isGameCompleted();
        if (pendingNextStageKey != null && !isNextStage(data.getStageKey(), pendingNextStageKey)) {
            throw new IllegalArgumentException("Invalid pending stage transition");
        }
        if (gameCompleted && (!"7".equals(data.getStageKey()) || pendingNextStageKey != null)) {
            throw new IllegalArgumentException("Invalid completed-game state");
        }
        boolean allQuestionsResolved = answeredQuestions == activeStageQuestions.size();
        if (pendingNextStageKey != null && (!allQuestionsResolved || data.getLivesRemaining() == 0 || encounteredActive)) {
            throw new IllegalArgumentException("A stage transition requires a successfully cleared stage");
        }
        if (gameCompleted && (!allQuestionsResolved || data.getLivesRemaining() == 0 || encounteredActive)) {
            throw new IllegalArgumentException("A completed game must have cleared the final stage");
        }
        if (allQuestionsResolved && data.getLivesRemaining() > 0
            && pendingNextStageKey == null && !gameCompleted) {
            throw new IllegalArgumentException("A cleared stage is missing its transition state");
        }

        summaryHistory.clear();
        summaryHistory.addAll(data.getSummaryHistory());
        validateSummaryHistory(data);

        lifeTracker.restore(data.getLivesRemaining());
        penguAssistant.restoreTokens(data.getPenguTokensRemaining());

        String thoughtQuestionId = data.getPenguThoughtQuestionId();
        if (thoughtQuestionId == null) {
            penguAssistant.restoreThought(null, data.getPenguThoughtChoiceIndex());
        } else {
            Question thoughtQuestion = questionsById.get(thoughtQuestionId);
            QuestionProgress thoughtProgress = questionProgressById.get(thoughtQuestionId);
            if (thoughtQuestion == null || thoughtProgress == null
                || thoughtProgress.status() == QuestionStatus.NOT_ASKED) {
                throw new IllegalArgumentException("Saved Pengu thought refers to an unknown or unasked question");
            }
            penguAssistant.restoreThought(thoughtQuestion, data.getPenguThoughtChoiceIndex());
        }

        rebuildSummaryFromHistory();
    }

    private void validateSummaryHistory(GameSaveData data) {
        if (summaryHistory.isEmpty()) {
            throw new IllegalArgumentException("Saved summary history is empty");
        }
        for (StageSummary stageSummary : summaryHistory) {
            if (!STAGE_SEQUENCE.contains(stageSummary.stageKey())) {
                throw new IllegalArgumentException("Saved summary refers to an unknown stage");
            }
        }

        StageSummary currentSummary = summaryHistory.get(summaryHistory.size() - 1);
        if (!currentSummary.stageKey().equals(data.getStageKey())) {
            throw new IllegalArgumentException("Saved summary does not end at the current stage");
        }

        List<QuestionStatus> currentOutcomes = data.getQuestions().stream()
            .map(QuestionProgress::status)
            .filter(status -> status == QuestionStatus.CORRECT
                || status == QuestionStatus.INCORRECT
                || status == QuestionStatus.TIMED_OUT)
            .toList();
        if (!currentSummary.outcomes().equals(currentOutcomes)) {
            throw new IllegalArgumentException("Saved summary outcomes do not match current stage progress");
        }
    }

    private void rebuildSummaryFromHistory() {
        terminalOut.clearSummary();
        for (StageSummary stageSummary : summaryHistory) {
            appendStageSummaryHeader(stageSummary.stageKey(), stageSummary.retry());
            for (int i = 0; i < stageSummary.outcomes().size(); i++) {
                appendQuestionSummary(i + 1, stageSummary.outcomes().get(i));
            }
        }
    }

    private void appendStageSummaryHeader(String stageKey, boolean retry) {
        String header = retry
            ? "\n--- Retry: STAGE " + stageKey.toUpperCase() + " ---"
            : "\n---- STAGE " + stageKey.toUpperCase() + " ----";
        terminalOut.appendSummary(header, OutputKind.RESPONSE);
    }

    private void appendQuestionSummary(int questionNumber, QuestionStatus status) {
        String result;
        OutputKind outputKind;
        switch (status) {
            case CORRECT -> {
                result = "correct";
                outputKind = OutputKind.CORRECT;
            }
            case INCORRECT -> {
                result = "wrong";
                outputKind = OutputKind.ERROR;
            }
            case TIMED_OUT -> {
                result = "timed out";
                outputKind = OutputKind.ERROR;
            }
            default -> throw new IllegalArgumentException("Not a resolved question outcome: " + status);
        }
        terminalOut.appendSummary("\nQuestion no." + questionNumber + ": " + result + ".", outputKind);
    }

    private void beginStageSummary(String stageKey, boolean retry) {
        summaryHistory.add(new StageSummary(stageKey, retry, List.of()));
        appendStageSummaryHeader(stageKey, retry);
    }

    private void recordQuestionSummary(QuestionStatus status) {
        int lastIndex = summaryHistory.size() - 1;
        StageSummary currentSummary = summaryHistory.get(lastIndex);
        if (!currentSummary.stageKey().equals(currentStageKey)) {
            throw new IllegalStateException("Current summary stage does not match gameplay stage");
        }
        List<QuestionStatus> outcomes = new ArrayList<>(currentSummary.outcomes());
        outcomes.add(status);
        summaryHistory.set(lastIndex, new StageSummary(currentStageKey, currentSummary.retry(), outcomes));
        appendQuestionSummary(outcomes.size(), status);
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
                questionProgressById.put(
                    currQuestion.getId(),
                    new QuestionProgress(currQuestion.getId(), QuestionStatus.ACTIVE, null)
                );
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
        return setStage(stageKey, false);
    }

    private boolean setStage(String stageKey, boolean restarting) {
        if (!STAGE_SEQUENCE.contains(stageKey)) {
            terminalOut.append("Unknown stage: " + stageKey + ". Defaulting to Stage 0.", OutputKind.ERROR);
            stageKey = "0";
        }

        currStageQuestions = createQuestionBank(stageKey);

        this.currentStageKey = stageKey;

        // Apply question limit configuration
        List<Question> rawShuffled = currStageQuestions.getShuffledQuestions();
        int limit = Math.min(QUESTIONS_PER_STAGE, rawShuffled.size());
        activeStageQuestions = new ArrayList<>(rawShuffled.subList(0, limit));

        qIterator = null;
        currQuestion = null;
        answeredQuestions = 0;
        questionProgressById.clear();
        for (Question question : activeStageQuestions) {
            questionProgressById.put(
                question.getId(),
                new QuestionProgress(question.getId(), QuestionStatus.NOT_ASKED, null)
            );
        }
        remainingQuestionMillis = 0;
        pendingNextStageKey = null;
        gameCompleted = false;
        lifeTracker.reset();
        penguAssistant.resetForStage();

        terminalOut.clear();

        // String status = restarting ? "Successfully restarted stage: " : "Successfully switched to stage: ";
        // terminalOut.append(status + stageKey.toUpperCase(), OutputKind.CORRECT);
        terminalOut.append("\n========================================", OutputKind.QUESTION);
        terminalOut.append("STAGE " + currentStageKey.toUpperCase() + " LOADED!", OutputKind.QUESTION);
        terminalOut.append("========================================", OutputKind.QUESTION);
        // terminalOut.append("To switch stages, type \n'stage < 0 | 1 | 2 | 3 | 4 | 5 | 6 | 7 >'", OutputKind.RESPONSE);
        terminalOut.append("Type 'ask' to begin questions for this stage.", OutputKind.RESPONSE);
        beginStageSummary(stageKey, restarting);
        if (!restarting) {
            List<CutsceneSlide> slides = getStageStartSlides(stageKey);
            if (!slides.isEmpty()) {
                SceneManager.get().openCutsceneWindow(Screen.CUTSCENE, slides, () -> { });
            }
        }   
        return true;
    }

    private QuestionBank createQuestionBank(String stageKey) {
        return switch (stageKey) {
            case "0" -> new Stage0Questions();
            case "1" -> new Stage1Questions();
            case "2" -> new Stage2Questions();
            case "3" -> new Stage3Questions();
            case "4" -> new Stage4Questions();
            case "5" -> new Stage5Questions();
            case "6" -> new Stage6Questions();
            case "7" -> new Stage7Questions();
            default -> throw new IllegalArgumentException("Unknown stage key: " + stageKey);
        };
    }

    private boolean isNextStage(String currentKey, String nextKey) {
        int currentIndex = STAGE_SEQUENCE.indexOf(currentKey);
        return currentIndex >= 0
            && currentIndex < STAGE_SEQUENCE.size() - 1
            && STAGE_SEQUENCE.get(currentIndex + 1).equals(nextKey);
    }

    private void handleAns(char lettChoice) {
        int selectedDisplayIndex = letterChoices.indexOf(lettChoice);
        int selectedChoiceIndex = currQuestion.getOriginalChoiceIndexForDisplay(selectedDisplayIndex);
        QuestionStatus result;
        remainingQuestionMillis = 0;
        if (selectedChoiceIndex == currQuestion.getCorrectChoiceIndx()) {
            result = QuestionStatus.CORRECT;
            answeredQuestions++;

            terminalOut.append("CORRECT! You choose the correct answer.", OutputKind.CORRECT);
            AudioManager.get().playSFX(SoundEffect.CORRECT);
            lifeTracker.refresh();
        } else { // wrong answer
            result = QuestionStatus.INCORRECT;
            answeredQuestions++;

            terminalOut.append("Wrong! You choose the wrong answer:(", OutputKind.ERROR);
            AudioManager.get().playSFX(SoundEffect.WRONG);
            loseHealth();
        }

        currQuestion.setAnswered(true);
        questionProgressById.put(
            currQuestion.getId(),
            new QuestionProgress(currQuestion.getId(), result, selectedChoiceIndex)
        );
        recordQuestionSummary(result);
        terminalOut.append("Question answered.", OutputKind.RESPONSE);

        if (lifeTracker.getLivesRemaining() == 0) {
            return;
        }

        // Check if stage requirement has been met
        if (answeredQuestions >= activeStageQuestions.size()) {
            checkStageCompletion();
        }
    }

    private void checkStageCompletion() {
        terminalOut.append("\n========================================", OutputKind.QUESTION);
        terminalOut.append("STAGE " + currentStageKey.toUpperCase() + " CLEARED!", OutputKind.QUESTION);
        terminalOut.append("========================================", OutputKind.QUESTION);

        int currentIndex = STAGE_SEQUENCE.indexOf(currentStageKey);
        
        // Check if this was the final stage (Win Condition)
        if (currentIndex == -1 || currentIndex >= STAGE_SEQUENCE.size() - 1) {
            gameCompleted = true;
            triggerWinCondition();
        } else {
            String nextStageKey = STAGE_SEQUENCE.get(currentIndex + 1);
            pendingNextStageKey = nextStageKey;
            remainingQuestionMillis = 0;
            terminalOut.append("Stage clear\n", OutputKind.RESPONSE);

            SceneManager.get().openCutsceneWindow(
                Screen.CUTSCENE,
                getStageClearSlides(currentStageKey, nextStageKey),
                () -> {
                    pendingNextStageKey = null;
                    setStage(nextStageKey);
                }
            );
        }
    }

    private void triggerWinCondition() {
        terminalOut.append("YOU WIN", OutputKind.CORRECT);

        List<CutsceneSlide> winSlides = List.of(
            new CutsceneSlide("/images/cutscene_images/placeholder.jpeg", String.format("\"Congratulations, %s! You have successfully breached Fox Silic! The future sure is looking bright!\"", PlayerSession.getPlayerName()))
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
        remainingQuestionMillis = 0;
        questionProgressById.put(
            currQuestion.getId(),
            new QuestionProgress(currQuestion.getId(), QuestionStatus.TIMED_OUT, null)
        );
        answeredQuestions++;
        terminalOut.append("Time is up. This question was marked as unanswered.", OutputKind.ERROR);
        recordQuestionSummary(QuestionStatus.TIMED_OUT);
        loseHealth();

        if (lifeTracker.getLivesRemaining() > 0 && answeredQuestions >= activeStageQuestions.size()) {
            checkStageCompletion();
        }
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

    // private String buildQuestion(Question q) {
    //     q.shuffleDisplayedChoices();
    //     StringBuilder sb = new StringBuilder();
    //     String question = q.getPrompt();
    //     sb.append("Q: \n").append(question);
    //     sb.append("\n");

    //     sb.append("Choices:\n");
    //     String letter;
    //     String choicePrompts;
    //     for (int i = 0; i < 4; i++) {
    //         letter = "[" + letterChoices.get(i) + "]: ";
    //         choicePrompts = q.getChoiceAtDisplayIndex(i);
    //         sb.append(letter).append("\n");
    //         sb.append(choicePrompts);
    //         sb.append("\n");
    //     }
    //     q.setHasBeenAsked(true);

    //     return sb.toString();
    // }

    private String buildQuestion(Question q) {
        StringBuilder sb = new StringBuilder();

        String bar = "=".repeat(20);
        sb.append(bar).append("\n");
        sb.append("Question ").append(q.getId()).append("\n");
        sb.append(bar).append("\n\n");

        sb.append(formatPrompt(q.getPrompt()));
        sb.append("\n----------------------------------------\n");

        List<String> choices = q.getChoices();
        for (int i = 0; i < choices.size(); i++) {
            String label = "  [" + letterChoices.get(i) + "] ";
            sb.append(indentHanging(choices.get(i).stripTrailing(), label));
            sb.append("\n");
            sb.append("\n----------------------------------------\n");
        }

        q.setHasBeenAsked(true);
        return sb.toString();
    }

    private String indentHanging(String text, String label) {
        String pad = " ".repeat(label.length());
        String[] lines = text.split("\n", -1);

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < lines.length; i++) {
            sb.append(i == 0 ? label : pad).append(lines[i]);
            if (i < lines.length - 1) {
                sb.append("\n");
            }
        }
        return sb.toString();
    }

    private String formatPrompt(String prompt) {
        StringBuilder sb = new StringBuilder();
        boolean inCode = false;

        for (String line : prompt.stripTrailing().split("\n", -1)) {
            String t = line.trim();

            if (t.startsWith("```")) {
                inCode = !inCode;
                continue; // drop the fence line itself
            }

            if (inCode && !line.isEmpty()) {
                sb.append("    ").append(line);
            } else {
                sb.append(line);
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private List<CutsceneSlide> getStageStartSlides(String stageKey) {
        return switch (stageKey) {
            case "0" -> List.of(
                new CutsceneSlide("/images/cutscene_images/placeholder.jpeg", "\"Let's do a sanity check!\""),
                new CutsceneSlide("/images/cutscene_images/placeholder.jpeg", "GET READY: Stage 0 (Initialization Trial)")
            );
            case "1" -> List.of(
                new CutsceneSlide("/images/cutscene_images/Missingnoascii.png", "Answer these security questions to gain surface-level access."),
                new CutsceneSlide("/images/cutscene_images/placeholder.jpeg", "GET READY: Stage 1 (Introduction to Programming Paradigms)")
            );
            case "2" -> List.of(
                new CutsceneSlide("/images/cutscene_images/machopascii_edited.png", "\"Halt!\""),
                new CutsceneSlide("/images/cutscene_images/placeholder.jpeg", "GET READY: Stage 2 (Procedural Programming)")
            );
            case "3" -> List.of(
                new CutsceneSlide("/images/cutscene_images/alakazamascii_edited.png", "\"Stop right there!\""),
                new CutsceneSlide("/images/cutscene_images/placeholder.jpeg", "GET READY: Stage 3 (Functional Programming)")
            );
            case "4" -> List.of(
                new CutsceneSlide("/images/cutscene_images/dittoascii_edited.png", "\"You shall not pass!\""),
                new CutsceneSlide("/images/cutscene_images/placeholder.jpeg", "GET READY: Stage 4 (Object-oriented Programming)")
            );
            case "5" -> List.of(
                new CutsceneSlide("/images/cutscene_images/jirachiascii_edited.png", "\"You're going down!"),
                new CutsceneSlide("/images/cutscene_images/placeholder.jpeg", "GET READY: Stage 5 (Imperative vs Declarative Programming)")
            );
            case "6" -> List.of(
                new CutsceneSlide("/images/cutscene_images/nidoranascii_edited.png", "\"You're not getting through me!\""),
                new CutsceneSlide("/images/cutscene_images/placeholder.jpeg", "GET READY: Stage 6 (Event-driven Programming)")
            );
            case "7" -> List.of(
                new CutsceneSlide("/images/cutscene_images/Missingnoascii.png", "?????????"),
                new CutsceneSlide("/images/cutscene_images/placeholder.jpeg", "GET READY: Stage 7 (Component Mappings between Programming Paradigms)")
            );
            default -> List.of(
                // new CutsceneSlide("/images/cutscene_images/placeholder.jpeg", "Entering Stage " + stageKey.toUpperCase())
            );
        };
    }

    private List<CutsceneSlide> getStageClearSlides(String currentKey, String nextKey) {
        // return List.of(
        //     new CutsceneSlide("/images/tutorial_images/terminal_screenshot.png", "STAGE " + currentKey.toUpperCase() + " BREACHED!\nFirewall integrity compromised."),
        //     new CutsceneSlide("/images/tutorial_images/desktop.png", "Preparing upload payload for Stage " + nextKey.toUpperCase() + "...")
        // );
        return switch (currentKey) {
            case "0" -> List.of(
                new CutsceneSlide("/images/cutscene_images/placeholder.jpeg", "\"You're all set! Good luck for real!\""),
                new CutsceneSlide("/images/cutscene_images/placeholder.jpeg", "STAGE 0 (Initialization Trial) cleared!")
            );
            case "1" -> List.of(
                new CutsceneSlide("/images/cutscene_images/Missingnoascii_edited.png", "Surface-level security access granted"),
                new CutsceneSlide("/images/cutscene_images/placeholder.jpeg", "Stage 1 (Introduction to Programming Paradigms) cleared!")
            );
            case "2" -> List.of(
                new CutsceneSlide("/images/cutscene_images/machopascii_edited.png", "\"This cannot be!\""),
                new CutsceneSlide("/images/cutscene_images/placeholder.jpeg", "Stage 2 (Procedural Programming) cleared!")
            );
            case "3" -> List.of(
                new CutsceneSlide("/images/cutscene_images/alakazamascii_edited.png", "\"This isn't supposed to happen!\""),
                new CutsceneSlide("/images/cutscene_images/placeholder.jpeg", "Stage 3 (Functional Programming) cleared!")
            );
            case "4" -> List.of(
                new CutsceneSlide("/images/cutscene_images/dittoascii_edited.png", "\"I have failed...\""),
                new CutsceneSlide("/images/cutscene_images/placeholder.jpeg", "Stage 4 (Object-oriented Programming) cleared!")
            );
            case "5" -> List.of(
                new CutsceneSlide("/images/cutscene_images/jirachiascii_edited.png", "\"Whatever.\""),
                new CutsceneSlide("/images/cutscene_images/placeholder.jpeg", "Stage 5 (Imperative vs Declarative Programming) cleared!")
            );
            case "6" -> List.of(
                new CutsceneSlide("/images/cutscene_images/nidoranascii_edited.png", "\"Noooo! You will pay for this!\""),
                new CutsceneSlide("/images/cutscene_images/placeholder.jpeg", "Stage 6 (Event-driven Programming) cleared!")
            );
            case "7" -> List.of(
                new CutsceneSlide("/images/cutscene_images/Missingnoascii.png", "\"YOU WIN!!!!\""),
                new CutsceneSlide("/images/cutscene_images/placeholder.jpeg", "Stage 7 (Component Mappings between Programming Paradigms) cleared!")
            );
            default -> List.of(
                // new CutsceneSlide("/images/cutscene_images/placeholder.jpeg", "Entering Stage " + stageKey.toUpperCase())
            );
        };
    }
}
