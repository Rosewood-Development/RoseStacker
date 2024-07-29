package dev.rosewood.rosestacker.listener.paper;

import com.destroystokyo.paper.event.entity.PreCreatureSpawnEvent;
import dev.rosewood.rosegarden.RosePlugin;
import dev.rosewood.rosestacker.config.SettingKey;
import dev.rosewood.rosestacker.manager.EntityCacheManager;
import dev.rosewood.rosestacker.manager.StackManager;
import dev.rosewood.rosestacker.manager.StackSettingManager;
import dev.rosewood.rosestacker.stack.StackedEntity;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.SpawnCategory;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent.SpawnReason;

public class PaperPreCreatureSpawnListener implements Listener {

    private static final Map<String, SpawnCategory> SPAWN_CATEGORY_LOOKUP = Arrays.stream(SpawnCategory.values()).collect(Collectors.toMap(SpawnCategory::name, Function.identity()));

    private final RosePlugin rosePlugin;

    public PaperPreCreatureSpawnListener(RosePlugin rosePlugin) {
        this.rosePlugin = rosePlugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPreCreatureSpawn(PreCreatureSpawnEvent event) {
        if (!SettingKey.ENTITY_OBEY_MOB_CAPS.get() || event.getReason() != SpawnReason.NATURAL)
            return;

        String category = this.rosePlugin.getManager(StackSettingManager.class).getEntityStackSettings(event.getType()).getEntityTypeData().spawnCategory();
        SpawnCategory spawnCategory = SPAWN_CATEGORY_LOOKUP.get(category);
        int limit = event.getSpawnLocation().getWorld().getSpawnLimit(spawnCategory);

        int total = 0;
        Collection<Entity> entities = this.rosePlugin.getManager(EntityCacheManager.class).getNearbyEntities(event.getSpawnLocation(), 16, x -> x.getSpawnCategory() == spawnCategory);
        for (Entity entity : entities) {
            LivingEntity livingEntity = (LivingEntity) entity;
            StackedEntity stackedEntity = this.rosePlugin.getManager(StackManager.class).getStackedEntity(livingEntity);
            if (stackedEntity == null) {
                total++;
            } else {
                total += stackedEntity.getStackSize();
            }

            if (total >= limit) {
                event.setCancelled(true);
                return;
            }
        }
    }

}
