package dev.rosewood.rosestacker.nms.v1_16_R3.hologram;

import dev.rosewood.rosestacker.nms.hologram.Hologram;
import dev.rosewood.rosestacker.nms.v1_16_R3.entity.DataWatcherWrapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.server.v1_16_R3.Blocks;
import net.minecraft.server.v1_16_R3.DataWatcher;
import net.minecraft.server.v1_16_R3.DataWatcherRegistry;
import net.minecraft.server.v1_16_R3.EntityTypes;
import net.minecraft.server.v1_16_R3.IChatBaseComponent;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityDestroy;
import net.minecraft.server.v1_16_R3.PacketPlayOutEntityMetadata;
import net.minecraft.server.v1_16_R3.PacketPlayOutSpawnEntity;
import net.minecraft.server.v1_16_R3.ParticleParamBlock;
import net.minecraft.server.v1_16_R3.Particles;
import net.minecraft.server.v1_16_R3.Vec3D;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_16_R3.util.CraftChatMessage;
import org.bukkit.entity.Player;

public class HologramImpl extends Hologram {

    public HologramImpl(int entityId, Location location, String text) {
        super(entityId, location, text);
    }

    @Override
    protected void create(Player player) {
        PacketPlayOutSpawnEntity packet = new PacketPlayOutSpawnEntity(
                this.entityId,
                UUID.randomUUID(),
                this.location.getX(),
                this.location.getY(),
                this.location.getZ(),
                90,
                0,
                EntityTypes.AREA_EFFECT_CLOUD,
                1,
                Vec3D.ORIGIN
        );

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    @Override
    protected void update(Player player) {
        Boolean visible = this.watchers.get(player);
        if (visible == null)
            return;

        List<DataWatcher.Item<?>> dataItems = new ArrayList<>();
        Optional<IChatBaseComponent> nameComponent = Optional.of(CraftChatMessage.fromStringOrNull(this.text));
        dataItems.add(new DataWatcher.Item<>(DataWatcherRegistry.f.a(2), nameComponent));
        dataItems.add(new DataWatcher.Item<>(DataWatcherRegistry.i.a(3), visible));
        dataItems.add(new DataWatcher.Item<>(DataWatcherRegistry.c.a(7), 0.5F));
        dataItems.add(new DataWatcher.Item<>(DataWatcherRegistry.i.a(9), true));
        dataItems.add(new DataWatcher.Item<>(DataWatcherRegistry.j.a(10), new ParticleParamBlock(Particles.BLOCK, Blocks.AIR.getBlockData())));

        PacketPlayOutEntityMetadata packet = new PacketPlayOutEntityMetadata(this.entityId, new DataWatcherWrapper(dataItems), false);
        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

    @Override
    protected void delete(Player player) {
        PacketPlayOutEntityDestroy packet = new PacketPlayOutEntityDestroy(this.entityId);

        ((CraftPlayer) player).getHandle().playerConnection.sendPacket(packet);
    }

}
