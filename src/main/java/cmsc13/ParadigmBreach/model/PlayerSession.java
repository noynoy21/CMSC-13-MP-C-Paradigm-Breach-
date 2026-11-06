package cmsc13.ParadigmBreach.model;

public class PlayerSession {
    private static String playerName = "C1pher";
    private static Integer selectedSaveSlot;

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

    public static Integer getSelectedSaveSlot() {
        return selectedSaveSlot;
    }

    public static void setSelectedSaveSlot(Integer slotNumber) {
        if (slotNumber != null && (slotNumber < 1 || slotNumber > 3)) {
            throw new IllegalArgumentException("Save slot must be between 1 and 3");
        }
        selectedSaveSlot = slotNumber;
    }
}
