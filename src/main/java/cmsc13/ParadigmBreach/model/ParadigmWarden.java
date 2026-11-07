package cmsc13.ParadigmBreach.model;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ThreadLocalRandom;

/** Stage-specific portrait and dialogue for a paradigm warden. */
public final class ParadigmWarden {
    private static final Map<String, ParadigmWarden> WARDENS_BY_STAGE = Map.of(
        "0", new ParadigmWarden(
            "/images/cutscene_images/placeholder.jpeg",
            List.of("That's okay. Take a breath and give it another try!"),
            List.of("Excellent work! You got it right!")
        ),
        "1", new ParadigmWarden(
            "/images/cutscene_images/Missingnoascii.png",
            List.of(
                "Your answers are as broken as your defenses!",
                "Still guessing? I can keep this up all day."
            ),
            List.of(
                "Wait... you know what you're doing?",
                "No... you found a way through my defenses!"
            )
        ),
        "2", new ParadigmWarden(
            "/images/cutscene_images/machopascii_edited.png",
            List.of(
                "Your logic is weaker than my punch!",
                "I'll knock down every wrong answer!"
            ),
            List.of(
                "That was a solid move!",
                "You found a way past my guard!"
            )
        ),
        "3", new ParadigmWarden(
            "/images/cutscene_images/alakazamascii_edited.png",
            List.of(
                "Your reasoning is predictable!",
                "My calculations say you will fail."
            ),
            List.of(
                "Impossible... you outsmarted my prediction!",
                "How did you see through that?"
            )
        ),
        "4", new ParadigmWarden(
            "/images/cutscene_images/dittoascii_edited.png",
            List.of(
                "You're still not inheriting success!",
                "This class is closed to you!"
            ),
            List.of(
                "You're breaking my encapsulation!",
                "No... you found a way to override me!"
            )
        ),
        "5", new ParadigmWarden(
            "/images/cutscene_images/jirachiascii_edited.png",
            List.of(
                "Your commands aren't convincing me!",
                "Try again before your luck runs out."
            ),
            List.of(
                "Your answer... it came true!",
                "I didn't expect you to see the bigger picture."
            )
        ),
        "6", new ParadigmWarden(
            "/images/cutscene_images/nidoranascii_edited.png",
            List.of(
                "That answer triggered nothing!",
                "You can't handle my events!"
            ),
            List.of(
                "That answer triggered... fear?",
                "You're reacting faster than I can!"
            )
        ),
        "7", new ParadigmWarden(
            "/images/cutscene_images/Missingnoascii.png",
            List.of(
                "These paradigms don't map the way you think they do!",
                "Keep guessing; the patterns will stay hidden!"
            ),
            List.of(
                "You mapped the components...?",
                "No... the patterns are coming apart!"
            )
        )
    );

    private final String imagePath;
    private final List<String> taunts;
    private final List<String> afraidLines;

    private ParadigmWarden(String imagePath, List<String> taunts, List<String> afraidLines) {
        this.imagePath = imagePath;
        this.taunts = List.copyOf(taunts);
        this.afraidLines = List.copyOf(afraidLines);
    }

    public static Optional<ParadigmWarden> forStage(String stageKey) {
        return Optional.ofNullable(WARDENS_BY_STAGE.get(stageKey));
    }

    public String getImagePath() {
        return imagePath;
    }

    public String getDialogue(boolean answerWasCorrect) {
        List<String> lines = answerWasCorrect ? afraidLines : taunts;
        return lines.get(ThreadLocalRandom.current().nextInt(lines.size()));
    }
}
