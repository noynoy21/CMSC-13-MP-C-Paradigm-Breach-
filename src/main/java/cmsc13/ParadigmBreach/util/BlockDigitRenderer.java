package cmsc13.ParadigmBreach.util;

/** Renders non-negative numbers as five-row block digits. */
public final class BlockDigitRenderer {
    private static final String[][] DIGITS = {
        {"  ████  ", " ██  ██ ", " ██  ██ ", " ██  ██ ", "  ████  "},
        {"   ██   ", "  ███   ", "   ██   ", "   ██   ", "  ████  "},
        {" ██████ ", "      ██", "  █████ ", " ██     ", "████████"},
        {"██████  ", "      ██", "  █████ ", "      ██", "██████  "},
        {"██   ██ ", "██   ██ ", "████████", "     ██ ", "     ██ "},
        {"████████", "██      ", "███████ ", "      ██", "███████ "},
        {"  █████ ", "██      ", "███████ ", "██    ██", " ██████ "},
        {"████████", "     ██ ", "    ██  ", "   ██   ", "   ██   "},
        {" ██████ ", "██    ██", " ██████ ", "██    ██", " ██████ "},
        {" ██████ ", "██    ██", " ███████", "      ██", " ██████ "}
    };

    private BlockDigitRenderer() { }

    public static String render(int value) {
        if (value < 0) {
            throw new IllegalArgumentException("value must not be negative");
        }

        String digits = Integer.toString(value);
        StringBuilder art = new StringBuilder();

        for (int row = 0; row < 5; row++) {
            if (row > 0) {
                art.append('\n');
            }

            for (int index = 0; index < digits.length(); index++) {
                if (index > 0) {
                    art.append("  ");
                }
                art.append(DIGITS[digits.charAt(index) - '0'][row]);
            }
        }

        return art.toString();
    }
}
