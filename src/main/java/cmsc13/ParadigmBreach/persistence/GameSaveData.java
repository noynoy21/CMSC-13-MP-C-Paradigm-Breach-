package cmsc13.ParadigmBreach.persistence;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;


/** Plain, versioned data required to rebuild gameplay without serializing UI objects. */
public final class GameSaveData {
    public static final int CURRENT_VERSION = 2;

    public enum QuestionStatus {
        NOT_ASKED,
        ACTIVE,
        CORRECT,
        INCORRECT,
        TIMED_OUT
    }

    public record QuestionProgress(String id, QuestionStatus status, Integer selectedChoiceIndex) {
        public QuestionProgress {
            if (id == null || !id.matches("q-\\d{5}")) {
                throw new IllegalArgumentException("Invalid question ID: " + id);
            }
            if (status == null) {
                throw new IllegalArgumentException("Question status is required for " + id);
            }
            if (selectedChoiceIndex != null && (selectedChoiceIndex < 0 || selectedChoiceIndex > 3)) {
                throw new IllegalArgumentException("Invalid selected choice for " + id);
            }
        }
    }

    /** Semantic summary data that can be rendered again using the live summary format. */
    public record StageSummary(String stageKey, boolean retry, List<QuestionStatus> outcomes) {
        public StageSummary {
            if (stageKey == null || !stageKey.matches("[0-7]")) {
                throw new IllegalArgumentException("Invalid summary stage: " + stageKey);
            }
            outcomes = List.copyOf(outcomes);
            if (outcomes.stream().anyMatch(status -> status == null
                || status == QuestionStatus.NOT_ASKED || status == QuestionStatus.ACTIVE)) {
                throw new IllegalArgumentException("Stage summary outcomes must be resolved questions");
            }
        }
    }

    private final String playerName;
    private final String stageKey;
    private final List<QuestionProgress> questions;
    private final String currentQuestionId;
    private final int livesRemaining;
    private final int penguTokensRemaining;
    private final long remainingQuestionMillis;
    private final String pendingNextStageKey;
    private final boolean gameCompleted;
    private final String penguThoughtQuestionId;
    private final int penguThoughtChoiceIndex;
    private final List<StageSummary> summaryHistory;
    private final String savedAt;

    public GameSaveData(
        String playerName,
        String stageKey,
        List<QuestionProgress> questions,
        String currentQuestionId,
        int livesRemaining,
        int penguTokensRemaining,
        long remainingQuestionMillis,
        String pendingNextStageKey,
        boolean gameCompleted,
        String penguThoughtQuestionId,
        int penguThoughtChoiceIndex,
        List<StageSummary> summaryHistory,
        String savedAt
    ) {
        this.playerName = requireText(playerName, "playerName");
        this.stageKey = requireText(stageKey, "stageKey");
        this.questions = List.copyOf(questions);
        this.currentQuestionId = currentQuestionId;
        this.livesRemaining = livesRemaining;
        this.penguTokensRemaining = penguTokensRemaining;
        this.remainingQuestionMillis = remainingQuestionMillis;
        this.pendingNextStageKey = pendingNextStageKey;
        this.gameCompleted = gameCompleted;
        this.penguThoughtQuestionId = penguThoughtQuestionId;
        this.penguThoughtChoiceIndex = penguThoughtChoiceIndex;
        this.summaryHistory = List.copyOf(summaryHistory);
        this.savedAt = savedAt == null ? Instant.now().toString() : savedAt;
    }

    public String getPlayerName() { return playerName; }
    public String getStageKey() { return stageKey; }
    public List<QuestionProgress> getQuestions() { return questions; }
    public String getCurrentQuestionId() { return currentQuestionId; }
    public int getLivesRemaining() { return livesRemaining; }
    public int getPenguTokensRemaining() { return penguTokensRemaining; }
    public long getRemainingQuestionMillis() { return remainingQuestionMillis; }
    public String getPendingNextStageKey() { return pendingNextStageKey; }
    public boolean isGameCompleted() { return gameCompleted; }
    public String getPenguThoughtQuestionId() { return penguThoughtQuestionId; }
    public int getPenguThoughtChoiceIndex() { return penguThoughtChoiceIndex; }
    public List<StageSummary> getSummaryHistory() { return summaryHistory; }
    public String getSavedAt() { return savedAt; }

    public Map<String, Object> toYamlMap() {
        Map<String, Object> root = new LinkedHashMap<>();
        root.put("version", CURRENT_VERSION);
        root.put("savedAt", savedAt);
        root.put("playerName", playerName);
        root.put("stageKey", stageKey);
        root.put("questions", questions.stream().map(GameSaveData::questionToMap).toList());
        root.put("currentQuestionId", currentQuestionId);
        root.put("livesRemaining", livesRemaining);
        root.put("penguTokensRemaining", penguTokensRemaining);
        root.put("remainingQuestionMillis", remainingQuestionMillis);
        root.put("pendingNextStageKey", pendingNextStageKey);
        root.put("gameCompleted", gameCompleted);
        root.put("penguThoughtQuestionId", penguThoughtQuestionId);
        root.put("penguThoughtChoiceIndex", penguThoughtChoiceIndex);
        root.put("summaryHistory", summaryHistory.stream().map(GameSaveData::stageSummaryToMap).toList());
        return root;
    }

    // Validates the YAML root and dispatches the current format or the supported v1 migration.
    public static GameSaveData fromYamlObject(Object raw) {
        Map<?, ?> map = requireMap(raw, "save root");
        int version = requireInt(map, "version");
        if (version == 1) {
            return fromVersionOne(map);
        }
        if (version != CURRENT_VERSION) {
            throw new UnsupportedSaveVersionException(version);
        }

        return new GameSaveData(
            requireString(map, "playerName"),
            requireString(map, "stageKey"),
            readQuestions(map),
            optionalString(map.get("currentQuestionId")),
            requireInt(map, "livesRemaining"),
            requireInt(map, "penguTokensRemaining"),
            requireLong(map, "remainingQuestionMillis"),
            optionalString(map.get("pendingNextStageKey")),
            requireBoolean(map, "gameCompleted"),
            optionalString(map.get("penguThoughtQuestionId")),
            requireInt(map, "penguThoughtChoiceIndex"),
            readSummaryHistory(map.get("summaryHistory")),
            optionalString(map.get("savedAt"))
        );
    }

    // Upgrades v1 by deriving its stage summary before constructing the current save model.
    private static GameSaveData fromVersionOne(Map<?, ?> map) {
        String stageKey = requireString(map, "stageKey");
        List<QuestionProgress> questions = readQuestions(map);
        List<QuestionStatus> currentOutcomes = questions.stream()
            .map(QuestionProgress::status)
            .filter(status -> status == QuestionStatus.CORRECT
                || status == QuestionStatus.INCORRECT
                || status == QuestionStatus.TIMED_OUT)
            .toList();

        return new GameSaveData(
            requireString(map, "playerName"),
            stageKey,
            questions,
            optionalString(map.get("currentQuestionId")),
            requireInt(map, "livesRemaining"),
            requireInt(map, "penguTokensRemaining"),
            requireLong(map, "remainingQuestionMillis"),
            optionalString(map.get("pendingNextStageKey")),
            requireBoolean(map, "gameCompleted"),
            optionalString(map.get("penguThoughtQuestionId")),
            requireInt(map, "penguThoughtChoiceIndex"),
            List.of(new StageSummary(stageKey, false, currentOutcomes)),
            optionalString(map.get("savedAt"))
        );
    }

    // Converts nested YAML question entries into typed progress records and rejects unknown statuses.
    private static List<QuestionProgress> readQuestions(Map<?, ?> map) {
        List<?> rawQuestions = requireList(map.get("questions"), "questions");
        List<QuestionProgress> questions = new ArrayList<>(rawQuestions.size());
        for (Object rawQuestion : rawQuestions) {
            Map<?, ?> questionMap = requireMap(rawQuestion, "question entry");
            String id = requireString(questionMap, "id");
            String statusText = requireString(questionMap, "status");
            QuestionStatus status;
            try {
                status = QuestionStatus.valueOf(statusText);
            } catch (IllegalArgumentException e) {
                throw new IllegalArgumentException("Unknown saved question status: " + statusText, e);
            }
            questions.add(new QuestionProgress(
                id,
                status,
                optionalInt(questionMap.get("selectedChoiceIndex"), "selectedChoiceIndex")
            ));
        }
        return questions;
    }

    // Parses each saved stage's retry flag and resolved outcomes into the typed history model.
    private static List<StageSummary> readSummaryHistory(Object raw) {
        List<?> entries = requireList(raw, "summaryHistory");
        List<StageSummary> history = new ArrayList<>(entries.size());
        for (Object entry : entries) {
            Map<?, ?> entryMap = requireMap(entry, "stage summary entry");
            String stageKey = requireString(entryMap, "stageKey");
            boolean retry = requireBoolean(entryMap, "retry");
            List<?> rawOutcomes = requireList(entryMap.get("outcomes"), "outcomes");
            List<QuestionStatus> outcomes = new ArrayList<>(rawOutcomes.size());
            for (Object rawOutcome : rawOutcomes) {
                if (!(rawOutcome instanceof String outcomeText)) {
                    throw new IllegalArgumentException("Summary outcome must be a string");
                }
                try {
                    outcomes.add(QuestionStatus.valueOf(outcomeText));
                } catch (IllegalArgumentException e) {
                    throw new IllegalArgumentException("Unknown summary outcome: " + outcomeText, e);
                }
            }
            history.add(new StageSummary(stageKey, retry, outcomes));
        }
        return history;
    }

    private static Map<String, Object> questionToMap(QuestionProgress question) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("id", question.id());
        map.put("status", question.status().name());
        map.put("selectedChoiceIndex", question.selectedChoiceIndex());
        return map;
    }

    private static Map<String, Object> stageSummaryToMap(StageSummary summary) {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("stageKey", summary.stageKey());
        map.put("retry", summary.retry());
        map.put("outcomes", summary.outcomes().stream().map(QuestionStatus::name).toList());
        return map;
    }

    private static String requireText(String value, String field) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(field + " is required");
        }
        return value;
    }

    private static Map<?, ?> requireMap(Object value, String field) {
        if (!(value instanceof Map<?, ?> map)) {
            throw new IllegalArgumentException(field + " must be a mapping");
        }
        return map;
    }

    private static List<?> requireList(Object value, String field) {
        if (!(value instanceof List<?> list)) {
            throw new IllegalArgumentException(field + " must be a list");
        }
        return list;
    }

    private static String requireString(Map<?, ?> map, String field) {
        Object value = map.get(field);
        if (!(value instanceof String string) || string.isBlank()) {
            throw new IllegalArgumentException(field + " must be a non-empty string");
        }
        return string;
    }

    private static String optionalString(Object value) {
        if (value == null) {
            return null;
        }
        if (!(value instanceof String string)) {
            throw new IllegalArgumentException("Optional save value must be a string");
        }
        return string;
    }

    private static int requireInt(Map<?, ?> map, String field) {
        long value = exactLong(map.get(field), field);
        if (value < Integer.MIN_VALUE || value > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(field + " is outside the integer range");
        }
        return (int) value;
    }

    private static long requireLong(Map<?, ?> map, String field) {
        return exactLong(map.get(field), field);
    }

    private static Integer optionalInt(Object value, String field) {
        return value == null ? null : exactInt(value, field);
    }

    private static int exactInt(Object value, String field) {
        long number = exactLong(value, field);
        if (number < Integer.MIN_VALUE || number > Integer.MAX_VALUE) {
            throw new IllegalArgumentException(field + " is outside the integer range");
        }
        return (int) number;
    }

    // Accepts YAML numbers only when they are exact whole values within the long range.
    private static long exactLong(Object value, String field) {
        if (!(value instanceof Number number)) {
            throw new IllegalArgumentException(field + " must be a number");
        }
        try {
            return new BigDecimal(number.toString()).longValueExact();
        } catch (ArithmeticException | NumberFormatException e) {
            throw new IllegalArgumentException(field + " must be a whole number in range", e);
        }
    }

    private static boolean requireBoolean(Map<?, ?> map, String field) {
        Object value = map.get(field);
        if (!(value instanceof Boolean bool)) {
            throw new IllegalArgumentException(field + " must be true or false");
        }
        return bool;
    }

    public static final class UnsupportedSaveVersionException extends IllegalArgumentException {
        private final int version;

        public UnsupportedSaveVersionException(int version) {
            super("Save version " + version + " is not supported (current version is " + CURRENT_VERSION + ")");
            this.version = version;
        }

        public int getVersion() { return version; }
    }
}
