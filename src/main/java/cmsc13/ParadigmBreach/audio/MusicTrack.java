package cmsc13.ParadigmBreach.audio;

public enum MusicTrack implements AudioAsset {
    SPLASH_SCREEN("/audio/music/[TEMP]_splash_music.mp3", 0.45), // credit to: https://www.youtube.com/watch?v=kbx2guA73N8&list=PLfP6i5T0-DkISmj1guO3s_nh2xrLMI1Nm by: PunchDeck
    DESKTOP("/audio/music/[TEMP]_desktop_music.mp3", 0.5); // credit to: https://www.youtube.com/watch?v=QbqkR5VNaU8&list=PLfP6i5T0-DkISmj1guO3s_nh2xrLMI1Nm&index=3 by: Miguel Johnson
    
    private final String resourcePath;
    private final double volume;

    MusicTrack(String resourcePath, double volume) {
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
