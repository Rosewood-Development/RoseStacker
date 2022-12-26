package dev.rosewood.rosestacker.nms.util;

import java.util.HashMap;
import java.util.Map;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Experimental
public final class ExtraUtils {

    private static final Map<NamespacedKey, EntityType> ENTITYTYPE_BY_KEY;
    static {
        ENTITYTYPE_BY_KEY = new HashMap<>();
        for (EntityType entityType : EntityType.values())
            if (entityType != EntityType.UNKNOWN)
                ENTITYTYPE_BY_KEY.put(entityType.getKey(), entityType);
    }

    public static EntityType getEntityTypeFromKey(NamespacedKey key) {
        return ENTITYTYPE_BY_KEY.get(key);
    }

}
