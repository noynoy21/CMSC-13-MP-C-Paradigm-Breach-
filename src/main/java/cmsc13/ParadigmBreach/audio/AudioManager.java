package cmsc13.ParadigmBreach.audio;

import java.util.EnumMap;
import java.util.Map;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.scene.media.AudioClip;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

public final class AudioManager {
    private static AudioManager instance;

    private MediaPlayer musicPlayer;

    // cached audioclips for each sound effect audio files
    private final Map<SoundEffect, AudioClip> soundClips =
        new EnumMap<>(SoundEffect.class);

    private AudioManager() {}

    public static void init() {
        if (instance == null) {
            instance = new AudioManager();
        }
    }

    public static AudioManager get() {
        if (instance == null) {
            throw new IllegalStateException("AudioManager.init() was not called");
        }
        return instance;
    }

    public void playSFX(SoundEffect sfx) {
        AudioClip clip = soundClips.get(sfx);

        if (clip == null) {
            var url = AudioManager.class.getResource(sfx.resourcePath());
            if (url == null) {
                throw new IllegalArgumentException(
                    "Audio resource not found: " + sfx.resourcePath()
                );
            }

            clip = new AudioClip(url.toExternalForm());
            soundClips.put(sfx, clip);
        }

        double volume = Math.max(0.0, Math.min(1.0, sfx.volume()));
        clip.play(volume);
    }

    public void playMusic(MusicTrack music) {
        String resourcePath = music.resourcePath();

        stopMusic();

        var url = AudioManager.class.getResource(resourcePath);
        if (url == null) {
            throw new IllegalArgumentException("Audio resource not found: " + resourcePath);
        }

        musicPlayer = new MediaPlayer(new Media(url.toExternalForm()));
        musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        musicPlayer.play();

        setMusicVolume(music.volume());
    }

    public void stopMusic() {
        if (musicPlayer != null) {
            musicPlayer.stop();
            musicPlayer.dispose();
            musicPlayer = null;
        }
    }

    public void fadeOutMusic(double seconds) {
        if (musicPlayer == null) {
            return;
        }

        if (seconds <= 0) {
            stopMusic();
            return;
        }

        MediaPlayer player = musicPlayer;
        double startingVolume = player.getVolume();

        Timeline fade = new Timeline(
            new KeyFrame(Duration.ZERO,
                new javafx.animation.KeyValue(player.volumeProperty(), startingVolume)),
            new KeyFrame(Duration.seconds(seconds),
                new javafx.animation.KeyValue(player.volumeProperty(), 0.0))
        );

        fade.setOnFinished(event -> {
            player.stop();
            player.dispose();

            if (musicPlayer == player) {
                musicPlayer = null;
            }
        });

        fade.play();
    }

    public void setMusicVolume(double volume) {
        if (musicPlayer != null) {
            musicPlayer.setVolume(Math.max(0.0, Math.min(1.0, volume)));
        }
    }
}