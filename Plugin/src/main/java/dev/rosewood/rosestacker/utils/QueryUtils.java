package dev.rosewood.rosestacker.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import org.bukkit.Chunk;

public final class QueryUtils {

    public static String buildChunksWhere(Collection<Chunk> chunks) {
        // Map<WorldName, Map<ChunkX, Set<ChunkZ>>>
        Map<String, Map<Integer, TreeSet<Integer>>> queryValuesX = new HashMap<>();
        Map<String, Map<Integer, TreeSet<Integer>>> queryValuesZ = new HashMap<>();

        for (Chunk chunk : chunks) {
            Map<Integer, TreeSet<Integer>> mapX = queryValuesX.get(chunk.getWorld().getName());
            Map<Integer, TreeSet<Integer>> mapZ = queryValuesZ.get(chunk.getWorld().getName());

            if (mapX == null) {
                mapX = new HashMap<>();
                queryValuesX.put(chunk.getWorld().getName(), mapX);
            }

            if (mapZ == null) {
                mapZ = new HashMap<>();
                queryValuesZ.put(chunk.getWorld().getName(), mapZ);
            }

            TreeSet<Integer> zSet = mapX.get(chunk.getX());
            TreeSet<Integer> xSet = mapZ.get(chunk.getZ());

            if (zSet == null) {
                zSet = new TreeSet<>();
                mapX.put(chunk.getX(), zSet);
            }

            if (xSet == null) {
                xSet = new TreeSet<>();
                mapZ.put(chunk.getZ(), xSet);
            }

            zSet.add(chunk.getZ());
            xSet.add(chunk.getX());
        }

        String queryX = buildQuery(queryValuesX, true);
        String queryZ = buildQuery(queryValuesZ, false);

        // Pick whichever query is smaller, it's likely to have less logical branches
        return queryX.length() < queryZ.length() ? queryX : queryZ;
    }

    private static String buildQuery(Map<String, Map<Integer, TreeSet<Integer>>> queryValues, boolean isChunkX) {
        String chunkCoord1 = "chunk_" + (isChunkX ? "x" : "z");
        String chunkCoord2 = "chunk_" + (isChunkX ? "z" : "x");

        StringBuilder queryWhere = new StringBuilder();
        for (Iterator<Map.Entry<String, Map<Integer, TreeSet<Integer>>>> iterator1 = queryValues.entrySet().iterator(); iterator1.hasNext();) {
            Map.Entry<String, Map<Integer, TreeSet<Integer>>> entry1 = iterator1.next();
            if (queryValues.size() > 1)
                queryWhere.append('(');

            queryWhere.append(String.format("world = '%s' AND ", entry1.getKey()));
            if (entry1.getValue().size() > 1)
                queryWhere.append('(');

            for (Iterator<Map.Entry<Integer, TreeSet<Integer>>> iterator2 = entry1.getValue().entrySet().iterator(); iterator2.hasNext();) {
                Map.Entry<Integer, TreeSet<Integer>> entry2 = iterator2.next();
                TreeSet<Integer> values = entry2.getValue();

                if (entry1.getValue().size() > 1)
                    queryWhere.append('(');

                boolean isRange = values.last() - values.first() == values.size() - 1;
                if (values.size() == 1) {
                    queryWhere.append(String.format("%s = %d AND %s = %d", chunkCoord1, entry2.getKey(), chunkCoord2, values.first()));
                } else if (isRange && values.size() > 2) {
                    queryWhere.append(String.format("%s = %d AND %s BETWEEN %d AND %d", chunkCoord1, entry2.getKey(), chunkCoord2, values.first(), values.last()));
                } else {
                    queryWhere.append(String.format("%s = %d AND %s IN (", chunkCoord1, entry2.getKey(), chunkCoord2));
                    for (Iterator<Integer> iterator3 = entry2.getValue().iterator(); iterator3.hasNext();) {
                        Integer entry3 = iterator3.next();
                        queryWhere.append(entry3);
                        if (iterator3.hasNext())
                            queryWhere.append(", ");
                    }
                    queryWhere.append(")");
                }

                if (entry1.getValue().size() > 1)
                    queryWhere.append(')');

                if (iterator2.hasNext())
                    queryWhere.append(" OR ");
            }

            if (entry1.getValue().size() > 1)
                queryWhere.append(")");

            if (queryValues.size() > 1)
                queryWhere.append(')');

            if (iterator1.hasNext())
                queryWhere.append(" OR ");
        }

        return queryWhere.toString();
    }

}
