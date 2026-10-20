package cmsc13.ParadigmBreach.model.questionBank;

import java.util.List;

public class OOPStage extends QuestionBank {
    public OOPStage() {
        super(List.of(
            new Question(
                "In Java, a child class is unable to inherit a parent class's variables. What access modifier was used for these variables?", 
                List.of("default", "public", "privtate", "protected"), 
            2
            ),
            new Question(
                "Which pillar of OOP necessitates the use of accessor (getter) and mutator (setter) methods?", 
                List.of("Encapsulation", "Inheretance", "Abstraction", "Polymorphism"), 
                0),
            new Question(
                "This refers to the type conversion of a child object to a parent object. ",
                List.of("Upcasting", "Downcasting", "Leftcasting", "Rightcasting"),
                0
            ),
            new Question(
                "Which keyword in Java is essential for accessing a child class's parent variables?",
                List.of("super", "static", "this", "var"),
                0
            ),
            new Question(
                "A flashlight can be both solar powered and battery-backed. Which keyword in Java allows an object to inherit “properties” from multiple “blueprints”? ",
                List.of("extends", "implements",  "throws", "requires"),
                1
            ),
            new Question(
                "Which pillar of OOP necessitates the use of method overloading and method overriding?",
                List.of("Encapsulation", "Inheritance", "Abstraction", "Polymorphism"),
                0 // TODO: determine the correct answer for this question
            ),
            new Question(
                "The following statements are true about the pillar of OOP 'Abstraction' except one.",
                List.of(
                    "An abstract class can be used for instantiation.", 
                    "Abstract methods cannot be made in non-abstract classes.",
                    "When inheriting from an abstract class, it is required to override its methods, if any.\r\n",
                    "" // TODO: add another choice
                ),
                0 // TODO: determine the correct answer for this question
            ),
            new Question(
                "The following statements are true about 'Java constructors' except one.",
                List.of(
                    "Constructor overloading is the", // TODO: finish sentence
                    "A class can have multiple constructors, but must have different return types and/or parameters.",
                    "All classes must have defined constructors.",
                    "" // TODO: add another choice
                ),
                
                0 // TODO: determine the correct answer for this question
            )
            // TODO: add two more questions (for the minimum of 10)
        ));
    }
}
