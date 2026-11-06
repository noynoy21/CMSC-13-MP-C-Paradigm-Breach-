package cmsc13.ParadigmBreach.persistence;

import java.io.IOException;
import java.util.Optional;

/** Storage boundary for save slots; implementations can use files, databases, or a service. */
public interface GameSaveStore {
    Optional<GameSaveData> load() throws IOException;

    void save(GameSaveData data) throws IOException;

    /** Preserves the current unreadable save before a fresh slot is written; null means no save existed. */
    String preserveExistingSave() throws IOException;
}
