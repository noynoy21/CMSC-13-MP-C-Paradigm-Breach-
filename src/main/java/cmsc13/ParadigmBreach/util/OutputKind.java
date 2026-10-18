package cmsc13.ParadigmBreach.util;

/* 
    enum class for terminal outputs on QnA.
*/

public enum OutputKind {
    COMMAND("command"),
    QUESTION("question"),
    RESPONSE("response"),
    ERROR("error");
    // SYSTEM("system");

    private final String cssClass;

    OutputKind(String cssClass) {
        this.cssClass = cssClass;
    }

    public String cssClass() {
        return cssClass;
    }
}
