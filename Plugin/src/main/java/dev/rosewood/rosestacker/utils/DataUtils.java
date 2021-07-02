package dev.rosewood.rosestacker.utils;

import dev.rosewood.rosestacker.RoseStacker;
import dev.rosewood.rosestacker.stack.StackedBlock;
import dev.rosewood.rosestacker.stack.StackedSpawner;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class DataUtils {

    private static final NamespacedKey CHUNK_ENTITIES_KEY = new NamespacedKey(RoseStacker.getInstance(), "chunk_entities");
    private static final int ENTITY_DATA_VERSION = 1;

    private static final NamespacedKey CHUNK_ITEMS_KEY = new NamespacedKey(RoseStacker.getInstance(), "chunk_items");
    private static final int ITEM_DATA_VERSION = 1;

    private static final NamespacedKey CHUNK_SPAWNERS_KEY = new NamespacedKey(RoseStacker.getInstance(), "chunk_spawners");
    private static final int SPAWNER_DATA_VERSION = 1;

    private static final NamespacedKey CHUNK_BLOCKS_KEY = new NamespacedKey(RoseStacker.getInstance(), "chunk_blocks");
    private static final int BLOCK_DATA_VERSION = 1;

    public static List<StackedSpawner> readStackedSpawners(Chunk chunk) {
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();

        List<StackedSpawner> stackedSpawners = new ArrayList<>();
        byte[] data = pdc.get(CHUNK_SPAWNERS_KEY, PersistentDataType.BYTE_ARRAY);
        if (data == null)
            return stackedSpawners;

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
             ObjectInputStream dataInput = new ObjectInputStream(inputStream)) {

            int dataVersion = dataInput.readInt();
            if (dataVersion == 1) {
                int length = dataInput.readInt();
                for (int i = 0; i < length; i++) {
                    int stackSize = dataInput.readInt();
                    int x = dataInput.readInt();
                    int y = dataInput.readInt();
                    int z = dataInput.readInt();
                    boolean placedByPlayer = dataInput.readBoolean();
                    Block block = chunk.getBlock(x, y, z);
                    if (block.getType() == Material.SPAWNER)
                        stackedSpawners.add(new StackedSpawner(stackSize, (CreatureSpawner) block.getState(), placedByPlayer));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            pdc.remove(CHUNK_SPAWNERS_KEY);
        }

        return stackedSpawners;
    }

    public static void writeStackedSpawners(Collection<StackedSpawner> stackedSpawners, Chunk chunk) {
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();
        byte[] data = null;

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ObjectOutputStream dataOutput = new ObjectOutputStream(outputStream)) {

            dataOutput.writeInt(SPAWNER_DATA_VERSION);
            dataOutput.writeInt(stackedSpawners.size());

            for (StackedSpawner stackedSpawner : stackedSpawners) {
                dataOutput.writeInt(stackedSpawner.getStackSize());
                dataOutput.writeInt(stackedSpawner.getLocation().getBlockX() & 0xF);
                dataOutput.writeInt(stackedSpawner.getLocation().getBlockY());
                dataOutput.writeInt(stackedSpawner.getLocation().getBlockZ() & 0xF);
                dataOutput.writeBoolean(stackedSpawner.isPlacedByPlayer());
            }

            // Serialize that array
            dataOutput.close();
            data = outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (data != null)
            pdc.set(CHUNK_SPAWNERS_KEY, PersistentDataType.BYTE_ARRAY, data);
    }

    public static List<StackedBlock> readStackedBlocks(Chunk chunk) {
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();

        List<StackedBlock> stackedBlocks = new ArrayList<>();
        byte[] data = pdc.get(CHUNK_BLOCKS_KEY, PersistentDataType.BYTE_ARRAY);
        if (data == null)
            return stackedBlocks;

        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
             ObjectInputStream dataInput = new ObjectInputStream(inputStream)) {

            int dataVersion = dataInput.readInt();
            if (dataVersion == 1) {
                int length = dataInput.readInt();
                for (int i = 0; i < length; i++) {
                    int stackSize = dataInput.readInt();
                    int x = dataInput.readInt();
                    int y = dataInput.readInt();
                    int z = dataInput.readInt();

                    stackedBlocks.add(new StackedBlock(stackSize, chunk.getBlock(x, y, z)));
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            pdc.remove(CHUNK_BLOCKS_KEY);
        }

        return stackedBlocks;
    }

    public static void writeStackedBlocks(Collection<StackedBlock> stackedBlocks, Chunk chunk) {
        PersistentDataContainer pdc = chunk.getPersistentDataContainer();
        byte[] data = null;

        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ObjectOutputStream dataOutput = new ObjectOutputStream(outputStream)) {

            dataOutput.writeInt(BLOCK_DATA_VERSION);
            dataOutput.writeInt(stackedBlocks.size());

            for (StackedBlock stackedBlock : stackedBlocks) {
                dataOutput.writeInt(stackedBlock.getStackSize());
                dataOutput.writeInt(stackedBlock.getLocation().getBlockX() & 0xF);
                dataOutput.writeInt(stackedBlock.getLocation().getBlockY());
                dataOutput.writeInt(stackedBlock.getLocation().getBlockZ() & 0xF);
            }

            // Serialize that array
            dataOutput.close();
            data = outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (data != null)
            pdc.set(CHUNK_BLOCKS_KEY, PersistentDataType.BYTE_ARRAY, data);
    }

}
