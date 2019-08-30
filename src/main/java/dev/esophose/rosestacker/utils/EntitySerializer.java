package dev.esophose.rosestacker.utils;

import dev.esophose.rosestacker.entitydata.EntityData;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

public class EntitySerializer {

    /**
     * A method to serialize an {@link EntityData} list to Base64 String.
     *
     * @param entityData to turn into a Base64 String.
     * @return Base64 string of the items.
     */
    public static String toBase64(List<EntityData> entityData) {
        try {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            BukkitObjectOutputStream dataOutput = new BukkitObjectOutputStream(outputStream);

            // Write the size of the data
            dataOutput.writeInt(entityData.size());

            // Save every element in the list
            for (EntityData data : entityData)
                dataOutput.writeObject(data);

            // Serialize that array
            dataOutput.close();
            return Base64Coder.encodeLines(outputStream.toByteArray());
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Gets a list of EntityDatas from Base64 string.
     *
     * @param data Base64 string to convert to EntityData list.
     * @return ItemStack array created from the Base64 string.
     */
    public static List<EntityData> fromBase64(String data) {
        try {
            ByteArrayInputStream inputStream = new ByteArrayInputStream(Base64Coder.decodeLines(data));
            BukkitObjectInputStream dataInput = new BukkitObjectInputStream(inputStream);
            int length = dataInput.readInt();
            List<EntityData> entityData = new ArrayList<>();

            // Read the serialized entitydata list
            for (int i = 0; i < length; i++)
                entityData.add((EntityData) dataInput.readObject());

            dataInput.close();
            return entityData;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

}
