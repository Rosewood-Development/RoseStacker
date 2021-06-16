package dev.rosewood.rosestacker.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.TreeSet;
import org.bukkit.Chunk;

public final class QueryUtils {

    public static String buildChunksWhere(Collection<Chunk> chunks) {
        Map<String, Map<Integer, TreeSet<Integer>>> queryValues = new HashMap<>(); // Map<WorldName, Map<ChunkX, Set<ChunkZ>>>
        for (Chunk chunk : chunks) {
            Map<Integer, TreeSet<Integer>> xMap = queryValues.get(chunk.getWorld().getName());
            if (xMap == null) {
                Map<Integer, TreeSet<Integer>> temp = new HashMap<>();
                queryValues.put(chunk.getWorld().getName(), temp);
                xMap = temp;
            }

            TreeSet<Integer> zSet = xMap.get(chunk.getX());
            if (zSet == null) {
                TreeSet<Integer> temp = new TreeSet<>();
                xMap.put(chunk.getX(), temp);
                zSet = temp;
            }

            zSet.add(chunk.getZ());
        }

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
                    queryWhere.append(String.format("chunk_x = %d AND chunk_z = %d", entry2.getKey(), values.first()));
                } else if (isRange && values.size() > 2) {
                    queryWhere.append(String.format("chunk_x = %d AND chunk_z BETWEEN %d AND %d", entry2.getKey(), values.first(), values.last()));
                } else {
                    queryWhere.append(String.format("chunk_x = %d AND chunk_z IN (", entry2.getKey()));
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
