package dev.esophose.rosestacker.utils;

import dev.esophose.rosestacker.stack.StackedEntity;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.entity.LivingEntity;

public final class EntitySerializer {

    /**
     * Serializes a stacked entity into a byte array
     *
     * @param stackedEntity to turn into a byte array.
     * @return byte array of the stacked entity dat
     */
    public static byte[] toBlob(StackedEntity stackedEntity) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
             ObjectOutputStream dataOutput = new ObjectOutputStream(outputStream)) {

            // Write the size of the items
            List<String> nbtStrings = stackedEntity.getStackedEntityNBTStrings();
            dataOutput.writeInt(nbtStrings.size());

            // Save every element in the list
            for (String nbtString : nbtStrings)
                dataOutput.writeUTF(nbtString);

            // Write the original mob name
            String originalCustomName = stackedEntity.getOriginalCustomName();
            dataOutput.writeUTF(originalCustomName == null ? "" : originalCustomName);

            // Serialize that array
            dataOutput.close();
            return outputStream.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Deserializes a stacked entity from a byte array
     *
     * @param id the id of the stacked entity
     * @param livingEntity the living entity to attach the stack to
     * @param data byte array to convert to a stacked entity
     * @return the stacked entity
     */
    public static StackedEntity fromBlob(int id, LivingEntity livingEntity, byte[] data) {
        try (ByteArrayInputStream inputStream = new ByteArrayInputStream(data);
             ObjectInputStream dataInput = new ObjectInputStream(inputStream)) {

            // Read list length
            int length = dataInput.readInt();
            List<String> stackNbtStrings = Collections.synchronizedList(new LinkedList<>());

            // Read the serialized itemstack list
            for (int i = 0; i < length; i++)
                stackNbtStrings.add(dataInput.readUTF());

            // Read original mob name, if any
            String originalCustomName = dataInput.readUTF();

            return new StackedEntity(id, livingEntity, stackNbtStrings, originalCustomName.isEmpty() ? null : originalCustomName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
