package dev.rosewood.rosestacker.folia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

/**
 * Source-level guard rails for the Folia threading rules. These cannot prove runtime correctness on a real Folia
 * server, they stop the known unsafe patterns from creeping back in.
 */
class FoliaOwnershipAuditTest {

    private static final Path SOURCES = Path.of("src/main/java");
    private static final Path PLUGIN_YML = Path.of("src/main/resources/plugin.yml");

    @Test
    void pluginDeclaresFoliaSupport() throws IOException {
        String yml = Files.readString(PLUGIN_YML);
        assertTrue(Pattern.compile("^folia-supported:\\s*true\\s*$", Pattern.MULTILINE).matcher(yml).find(),
                "plugin.yml must declare folia-supported: true");
    }

    @Test
    void noBukkitSchedulerOrRawThreads() throws IOException {
        List<String> offenders = scan(Pattern.compile("Bukkit\\.getScheduler\\(\\)|BukkitRunnable|new Thread\\(|Executors\\.|getServer\\(\\)\\.getScheduler\\(\\)"));
        assertEquals(List.of(), offenders, "Use ThreadUtils/Folia (or RoseGarden's scheduler) instead of the Bukkit scheduler or raw threads");
    }

    @Test
    void worldWideEntityScansAreFoliaGuarded() throws IOException {
        // getEntities()/getLivingEntities() on a World are only legal on Folia from the owning region.
        // Remaining callers must sit in classic-only branches (checked by hand) listed here.
        List<String> offenders = scan(Pattern.compile("targetWorld\\.get(Living)?Entities\\(\\)"));
        for (String offender : offenders)
            assertTrue(offender.contains("StackingThread.java"), "unexpected world-wide scan: " + offender);

        String stackingThread = Files.readString(SOURCES.resolve("dev/rosewood/rosestacker/stack/StackingThread.java"));
        assertTrue(stackingThread.contains("cleanupOrphanedEntitiesFolia"), "Folia must scan per chunk owner");
        assertTrue(stackingThread.contains("loadLoadedChunksFolia"), "Folia must load chunks per chunk owner");
    }

    @Test
    void ownerlessRunSyncIsNotUsedOutsideThreadUtils() throws IOException {
        // runSync(Runnable) schedules on the global region on Folia; anything touching entities or blocks must pass its owner.
        Pattern ownerless = Pattern.compile("ThreadUtils\\.runSync(Delayed)?\\((\\(\\)|[a-zA-Z_.]+::|[a-zA-Z_]+\\))");
        List<String> offenders = new ArrayList<>();
        for (String hit : scan(ownerless)) {
            // StackedEntity keeps the classic-only (non-Folia) branches that call runSync from async threads
            if (!hit.contains("StackedEntity.java"))
                offenders.add(hit);
        }
        assertEquals(List.of(), offenders, "Pass the owning Entity/Location to ThreadUtils.runSync");
    }

    @Test
    void entityCacheDoesNotScanWorldsOnFolia() throws IOException {
        String cache = Files.readString(SOURCES.resolve("dev/rosewood/rosestacker/manager/EntityCacheManager.java"));
        int reload = cache.indexOf("public void reload()");
        int guard = cache.indexOf("if (Folia.ENABLED)", reload);
        int schedule = cache.indexOf("runTaskTimer(this::refresh", reload);
        assertTrue(reload >= 0 && guard > reload && guard < schedule, "the world-wide refresh task must not be scheduled on Folia");
    }

    private static List<String> scan(Pattern pattern) throws IOException {
        List<String> hits = new ArrayList<>();
        try (Stream<Path> files = Files.walk(SOURCES)) {
            for (Path file : (Iterable<Path>) files.filter(p -> p.toString().endsWith(".java"))::iterator) {
                if (file.endsWith("utils/ThreadUtils.java") || file.endsWith("utils/Folia.java"))
                    continue;

                List<String> lines = Files.readAllLines(file);
                for (int i = 0; i < lines.size(); i++)
                    if (pattern.matcher(lines.get(i)).find())
                        hits.add(SOURCES.relativize(file) + ":" + (i + 1) + ": " + lines.get(i).trim());
            }
        }
        return hits;
    }

}
