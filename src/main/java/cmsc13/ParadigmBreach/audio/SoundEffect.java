package cmsc13.ParadigmBreach.audio;

public enum SoundEffect implements AudioAsset {
    BEEP_COUNTDOWN("/audio/sfx/[TEST]_countdown_beeping.mp3", 1); // credit to: https://www.youtube.com/watch?v=OFxvJac1lpQ

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
