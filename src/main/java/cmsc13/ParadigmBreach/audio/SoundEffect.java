package cmsc13.ParadigmBreach.audio;

public enum SoundEffect implements AudioAsset {
    BEEP_COUNTDOWN("/audio/sfx/[TEST]_countdown_beeping.mp3", 1.0), // credit to: https://www.youtube.com/watch?v=OFxvJac1lpQ
    CORRECT("/audio/sfx/correct.mp3", 1.0),
    WRONG("/audio/sfx/wrong.mp3", 1.0),
    TOKEN_SUCCESS("/audio/sfx/pengu_token_success.mp3", 1.0),
    TOKEN_FAIL("/audio/sfx/pengu_token_fail.mp3", 1.0);

    private final String resourcePath;
    private final double volume;

    SoundEffect(String resourcePath, double volume) {
        this.resourcePath = resourcePath;
        this.volume = volume;
    }

    @Override
    public String resourcePath() {
        return resourcePath;
    }

    @Override
    public double volume() {
        return volume;
    }
}
