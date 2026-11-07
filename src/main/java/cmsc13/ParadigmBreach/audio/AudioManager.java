package cmsc13.ParadigmBreach.audio;

import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.EnumMap;
import java.util.HashMap;
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
    private final Map<AudioAsset, String> playableUrls = new HashMap<>();

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
            clip = new AudioClip(resolvePlayableUrl(sfx));
            soundClips.put(sfx, clip);
        }

        double volume = Math.max(0.0, Math.min(1.0, sfx.volume()));
        clip.play(volume);
    }

    public void playMusic(MusicTrack music) {
        stopMusic();

        musicPlayer = new MediaPlayer(new Media(resolvePlayableUrl(music)));
        musicPlayer.setCycleCount(MediaPlayer.INDEFINITE);
        musicPlayer.play();

        setMusicVolume(music.volume());
    }

    private String resolvePlayableUrl(AudioAsset asset) {
        String cachedUrl = playableUrls.get(asset);
        if (cachedUrl != null) {
            return cachedUrl;
        }

        String resourcePath = asset.resourcePath();
        try (InputStream audio = AudioManager.class.getResourceAsStream(resourcePath)) {
            if (audio == null) {
                throw new IllegalArgumentException("Audio resource not found: " + resourcePath);
            }

            String resourceName = resourcePath.substring(resourcePath.lastIndexOf('/') + 1);
            int extensionIndex = resourceName.lastIndexOf('.');
            String suffix = extensionIndex >= 0 ? resourceName.substring(extensionIndex) : ".audio";
            Path tempFile = Files.createTempFile("paradigm-breach-", suffix);
            tempFile.toFile().deleteOnExit();
            Files.copy(audio, tempFile, StandardCopyOption.REPLACE_EXISTING);

            String playableUrl = tempFile.toUri().toString();
            playableUrls.put(asset, playableUrl);
            return playableUrl;
        } catch (IOException e) {
            throw new UncheckedIOException("Failed to prepare audio resource: " + resourcePath, e);
        }
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
