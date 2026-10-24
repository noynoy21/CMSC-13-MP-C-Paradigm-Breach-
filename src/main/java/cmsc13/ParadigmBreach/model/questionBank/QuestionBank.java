package cmsc13.ParadigmBreach.model.questionBank;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class QuestionBank {
    protected List<Question> listOfQuestions; // set of initial questions
    protected List<Question> shuffledQuestions; // shuffled questions

    public QuestionBank(List<Question> list) {
        listOfQuestions = list;
        shuffleQuestions();
    }

    public QuestionBank(String csvResourcePath) {
        listOfQuestions = loadQuestionsFromCsv(csvResourcePath);
        shuffleQuestions();
    }

    private List<Question> loadQuestionsFromCsv(String resourcePath) {
        List<Question> questions = new ArrayList<>();

        var inputStream = QuestionBank.class.getResourceAsStream("/" + resourcePath);
        
        if (inputStream == null) {
            inputStream = QuestionBank.class.getClassLoader().getResourceAsStream(resourcePath);
        }

        if (inputStream == null) {
            throw new IllegalArgumentException("CSV resource file not found: " + resourcePath);
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }

                List<String> tokens = parseCsvLine(line);
                if (tokens.size() < 6) {
                    continue; 
                }

                String prompt = tokens.get(0).replace("\\n", "\n");
                List<String> choices = List.of(
                    tokens.get(1).replace("\\n", "\n"), 
                    tokens.get(2).replace("\\n", "\n"), 
                    tokens.get(3).replace("\\n", "\n"), 
                    tokens.get(4).replace("\\n", "\n"));
                int correctIndex = Integer.parseInt(tokens.get(5).trim());

                questions.add(new Question(prompt, choices, correctIndex));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse CSV question bank: " + resourcePath, e);
        }

        return questions;
    }

    private List<String> parseCsvLine(String line) {
        List<String> tokens = new ArrayList<>();
        boolean inQuotes = false;
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                tokens.add(sb.toString().trim().replaceAll("^\"|\"$", ""));
                sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        tokens.add(sb.toString().trim().replaceAll("^\"|\"$", ""));
        return tokens;
    }

    public void shuffleQuestions() {
        shuffledQuestions = new ArrayList<>(listOfQuestions);
        Collections.shuffle(shuffledQuestions);
    }

    public Question askQuestion() {
        if (shuffledQuestions.isEmpty()) throw new IllegalStateException("All questions were already asked!");
        return shuffledQuestions.remove(0);
    }

    public List<Question> getShuffledQuestions() { return this.shuffledQuestions; }
}
