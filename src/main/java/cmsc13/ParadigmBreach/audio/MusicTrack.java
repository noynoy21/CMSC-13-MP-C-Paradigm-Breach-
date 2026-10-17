package cmsc13.ParadigmBreach.audio;

public enum MusicTrack implements AudioAsset {
    SPLASH_SCREEN("/audio/music/[TEMP]_splash_music.mp3"); // credit to: https://www.youtube.com/watch?v=kbx2guA73N8&list=PLfP6i5T0-DkISmj1guO3s_nh2xrLMI1Nm by: PunchDeck

    private final String resourcePath;

    MusicTrack(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    @Override
    public String resourcePath() {
        return resourcePath;
    }
}
