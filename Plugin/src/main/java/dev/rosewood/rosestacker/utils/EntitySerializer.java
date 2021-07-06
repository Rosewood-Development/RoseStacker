package dev.rosewood.rosestacker.utils;

import dev.rosewood.rosestacker.nms.NMSAdapter;
import dev.rosewood.rosestacker.nms.NMSHandler;
import dev.rosewood.rosestacker.nms.object.CompactNBT;
import dev.rosewood.rosestacker.stack.StackedEntity;
import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.entity.LivingEntity;

public final class EntitySerializer {

    private EntitySerializer() {

    }

    /**
     * Deserializes a stacked entity from a byte array
     *
     * @param livingEntity the living entity to attach the stack to
     * @param data byte array to convert to a stacked entity
     * @return the stacked entity
     */
    public static StackedEntity fromBlob(LivingEntity livingEntity, byte[] data) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
             ObjectInputStream dataInput = new ObjectInputStream(inputStream)) {

            // Read list length
            int length = dataInput.readInt();
            List<byte[]> stackNbtData = new LinkedList<>();

            // Read the serialized nbt list
            for (int i = 0; i < length; i++) {
                byte[] nbtData = new byte[dataInput.readInt()];
                for (int n = 0; n < nbtData.length; n++)
                    nbtData[n] = dataInput.readByte();
                stackNbtData.add(nbtData);
            }

            NMSHandler nmsHandler = NMSAdapter.getHandler();
            CompactNBT compactNBT = nmsHandler.createCompactNBT(livingEntity);
            for (byte[] entry : stackNbtData)
                compactNBT.addFirst(nmsHandler.createEntityFromNBT(entry, livingEntity.getLocation(), livingEntity.getType()));

            return new StackedEntity(livingEntity, compactNBT);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
