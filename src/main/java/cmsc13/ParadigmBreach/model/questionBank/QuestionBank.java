package cmsc13.ParadigmBreach.model.questionBank;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.yaml.snakeyaml.Yaml;

public class QuestionBank {
    protected List<Question> listOfQuestions; // set of initial questions
    protected List<Question> shuffledQuestions; // shuffled questions

    public QuestionBank(String filePath) {
        String[] parts = filePath.split("\\.");

        if (parts.length <= 1) {
            throw new IllegalArgumentException("filePath input is not a file: " + filePath);
        }

        String fileType = parts[parts.length-1];

        switch (fileType) {
            case "yml" -> {
                listOfQuestions = loadQuestionsFromYaml(filePath);
            }
            default -> {
                throw new IllegalArgumentException("Invalid file type: ." + fileType);
            }
        }
        shuffleQuestions();
    }

    private List<Question> loadQuestionsFromYaml(String resourcePath) {
        List<Question> questions = new ArrayList<>();
        Set<String> questionIds = new HashSet<>();
        Yaml yaml = new Yaml();

        InputStream inputStream = QuestionBank.class.getResourceAsStream("/" + resourcePath);
        if (inputStream == null) {
            inputStream = QuestionBank.class.getClassLoader().getResourceAsStream(resourcePath);
        }
        if (inputStream == null) {
            throw new IllegalArgumentException("YAML resource file not found: " + resourcePath);
        }

        try {
            List<Map<String, Object>> data = yaml.load(inputStream);
            
            int questionNumber = 0;
            for (Map<String, Object> map : data) {
                questionNumber++;
                String id = (String) map.get("id");
                if (id == null || !id.matches("q-\\d{5}")) {
                    throw new IllegalArgumentException(
                        "Question " + questionNumber + " must have an ID in the form q-#####"
                    );
                }
                if (!questionIds.add(id)) {
                    throw new IllegalArgumentException(
                        "Duplicate question ID " + id + " in " + resourcePath
                    );
                }

                String prompt = (String) map.get("prompt");
                @SuppressWarnings("unchecked")
                List<String> choices = (List<String>) map.get("choices");
                int correctIndex = (int) map.get("correctChoiceIndx");

                questions.add(new Question(id, prompt, choices, correctIndex));
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to parse YAML question bank: " + resourcePath, e);
        }

        return questions;
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
