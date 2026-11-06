package cmsc13.ParadigmBreach.persistence;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.util.HexFormat;
import java.util.Locale;
import java.util.Optional;

import org.yaml.snakeyaml.LoaderOptions;
import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.constructor.SafeConstructor;

/** Reads and atomically writes one save slot for a player profile. */
public final class GameSaveRepository implements GameSaveStore {
    private final Path savePath;

    public GameSaveRepository(Path savePath) {
        this.savePath = savePath.toAbsolutePath().normalize();
    }

    public static GameSaveRepository forPlayer(String playerName) {
        String profile = playerName == null || playerName.isBlank() ? "C1pher" : playerName.trim();
        return new GameSaveRepository(defaultDataDirectory()
            .resolve("profiles")
            .resolve(profileKey(profile))
            .resolve("save.yml"));
    }

    public Path getSavePath() {
        return savePath;
    }

    @Override
    public Optional<GameSaveData> load() throws IOException {
        if (!Files.exists(savePath)) {
            return Optional.empty();
        }

        LoaderOptions options = new LoaderOptions();
        options.setAllowDuplicateKeys(false);
        Yaml yaml = new Yaml(new SafeConstructor(options));
        try (Reader reader = Files.newBufferedReader(savePath, StandardCharsets.UTF_8)) {
            Object raw = yaml.load(reader);
            return Optional.of(GameSaveData.fromYamlObject(raw));
        } catch (IllegalArgumentException e) {
            throw e;
        } catch (RuntimeException e) {
            throw new IOException("Could not parse save file " + savePath, e);
        }
    }

    /** Copies an invalid save before the application starts a fresh profile. */
    @Override
    public String preserveExistingSave() throws IOException {
        if (!Files.exists(savePath)) {
            return null;
        }

        Path backupPath = savePath.resolveSibling(
            "save-invalid-" + Instant.now().toString().replace(':', '-') + ".yml"
        );
        return Files.copy(savePath, backupPath, StandardCopyOption.COPY_ATTRIBUTES).toString();
    }

    @Override
    public void save(GameSaveData data) throws IOException {
        Path parent = savePath.getParent();
        Files.createDirectories(parent);

        Path temporaryPath = Files.createTempFile(parent, "save-", ".tmp");
        try (Writer writer = Files.newBufferedWriter(temporaryPath, StandardCharsets.UTF_8)) {
            new Yaml().dump(data.toYamlMap(), writer);
        }

        try {
            Files.move(temporaryPath, savePath,
                StandardCopyOption.ATOMIC_MOVE,
                StandardCopyOption.REPLACE_EXISTING);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temporaryPath, savePath, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private static Path defaultDataDirectory() {
        String osName = System.getProperty("os.name", "").toLowerCase(Locale.ROOT);
        String home = System.getProperty("user.home", ".");

        if (osName.contains("win")) {
            String appData = System.getenv("APPDATA");
            return appData == null || appData.isBlank()
                ? Path.of(home, "AppData", "Roaming", "ParadigmBreach")
                : Path.of(appData, "ParadigmBreach");
        }
        if (osName.contains("mac")) {
            return Path.of(home, "Library", "Application Support", "ParadigmBreach");
        }

        String xdgDataHome = System.getenv("XDG_DATA_HOME");
        return xdgDataHome == null || xdgDataHome.isBlank()
            ? Path.of(home, ".local", "share", "ParadigmBreach")
            : Path.of(xdgDataHome, "ParadigmBreach");
    }

    private static String profileKey(String playerName) {
        String normalized = playerName.trim().toLowerCase(Locale.ROOT);
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(normalized.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 is unavailable", e);
        }
    }
}
