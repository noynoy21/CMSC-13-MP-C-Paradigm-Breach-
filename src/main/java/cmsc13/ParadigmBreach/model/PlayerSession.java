package cmsc13.ParadigmBreach.model;

public class PlayerSession {
    private static String playerName = "C1pher";

    public static String getPlayerName() {
        return playerName;
    }

    public static void setPlayerName(String name) {
        if (name == null || name.trim().isEmpty()) {
            playerName = "C1pher";
        } else {
            playerName = name.trim();
        }
    }
}