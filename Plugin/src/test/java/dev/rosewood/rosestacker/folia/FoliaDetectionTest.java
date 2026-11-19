package dev.rosewood.rosestacker.folia;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import org.junit.jupiter.api.Test;

/**
 * Proves the Folia boundary class: detection flips with the presence of Folia's regionized server class, and the
 * outer class never links against Folia-only API (so it loads on legacy Spigot/Paper).
 */
class FoliaDetectionTest {

    private static final String FOLIA = "dev.rosewood.rosestacker.utils.Folia";
    private static final String REGIONIZED_SERVER = "io.papermc.paper.threadedregions.RegionizedServer";
    // Empty class named like Folia's RegionizedServer, compiled once so the tests need no Folia dependency
    private static final String REGIONIZED_SERVER_STUB = "yv66vgAAAD0ADQoAAgADBwAEDAAFAAYBABBqYXZhL2xhbmcvT2JqZWN0AQAGPGluaXQ+AQADKClWBwAIAQAxaW8vcGFwZXJtYy9wYXBlci90aHJlYWRlZHJlZ2lvbnMvUmVnaW9uaXplZFNlcnZlcgEABENvZGUBAA9MaW5lTnVtYmVyVGFibGUBAApTb3VyY2VGaWxlAQAVUmVnaW9uaXplZFNlcnZlci5qYXZhACEABwACAAAAAAABAAEABQAGAAEACQAAAB0AAQABAAAABSq3AAGxAAAAAQAKAAAABgABAAAAAQABAAsAAAACAAw=";

    @Test
    void classicServerIsNotDetectedAsFolia() throws Exception {
        assertFalse(loadFoliaFlag(false));
    }

    @Test
    void foliaServerIsDetectedWhenRegionizedServerExists() throws Exception {
        assertTrue(loadFoliaFlag(true));
    }

    @Test
    void outerClassDoesNotReferenceFoliaOnlyApi() throws IOException {
        String outer = new String(readClass(FOLIA), java.nio.charset.StandardCharsets.ISO_8859_1);
        for (String foliaOnly : new String[]{"isOwnedByCurrentRegion", "getRegionScheduler", "getScheduler", "ScheduledTask"})
            assertFalse(outer.contains(foliaOnly), "Folia outer class must not link " + foliaOnly + "; it belongs in Folia$Impl");

        String impl = new String(readClass(FOLIA + "$Impl"), java.nio.charset.StandardCharsets.ISO_8859_1);
        assertTrue(impl.contains("isOwnedByCurrentRegion"));
        assertTrue(impl.contains("getRegionScheduler"));
    }

    private static boolean loadFoliaFlag(boolean regionizedServerPresent) throws Exception {
        ClassLoader loader = new IsolatingLoader(FoliaDetectionTest.class.getClassLoader(), regionizedServerPresent);
        Class<?> folia = Class.forName(FOLIA, true, loader);
        assertEquals(loader, folia.getClassLoader(), "Folia must be defined by the isolating loader");
        return folia.getField("ENABLED").getBoolean(null);
    }

    private static byte[] readClass(String name) throws IOException {
        try (InputStream in = FoliaDetectionTest.class.getClassLoader().getResourceAsStream(name.replace('.', '/') + ".class")) {
            if (in == null)
                throw new IOException("missing class " + name);
            return in.readAllBytes();
        }
    }

    /**
     * Re-defines Folia from the same bytecode and fakes the presence (or absence) of Folia's regionized server class.
     */
    private static final class IsolatingLoader extends ClassLoader {

        private final boolean regionizedServerPresent;

        IsolatingLoader(ClassLoader parent, boolean regionizedServerPresent) {
            super(parent);
            this.regionizedServerPresent = regionizedServerPresent;
        }

        @Override
        protected Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
            if (name.equals(REGIONIZED_SERVER)) {
                if (!this.regionizedServerPresent)
                    throw new ClassNotFoundException(name);

                synchronized (this.getClassLoadingLock(name)) {
                    Class<?> loaded = this.findLoadedClass(name);
                    if (loaded != null)
                        return loaded;
                    byte[] bytes = Base64.getDecoder().decode(REGIONIZED_SERVER_STUB);
                    return this.defineClass(name, bytes, 0, bytes.length);
                }
            }

            if (name.equals(FOLIA) || name.startsWith(FOLIA + "$")) {
                synchronized (this.getClassLoadingLock(name)) {
                    Class<?> loaded = this.findLoadedClass(name);
                    if (loaded != null)
                        return loaded;
                    try {
                        byte[] bytes = readClass(name);
                        return this.defineClass(name, bytes, 0, bytes.length);
                    } catch (IOException e) {
                        throw new ClassNotFoundException(name, e);
                    }
                }
            }

            return super.loadClass(name, resolve);
        }

    }

}
