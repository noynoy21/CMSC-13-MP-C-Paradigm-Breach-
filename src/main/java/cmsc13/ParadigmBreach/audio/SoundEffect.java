package cmsc13.ParadigmBreach.audio;

public enum SoundEffect implements AudioAsset {
    TERMINAL_OPEN(""),
    BUTTON_CLICK("");

    private final String resourcePath;

    SoundEffect(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    @Override
    public String resourcePath() {
        return resourcePath;
    }
}
