package dev.rosewood.rosestacker.nms.v1_18_R1.hologram;

import dev.rosewood.rosestacker.nms.hologram.Hologram;
import dev.rosewood.rosestacker.nms.v1_18_R1.entity.SynchedEntityDataWrapper;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket;
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_18_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_18_R1.util.CraftChatMessage;
import org.bukkit.entity.Player;

public class HologramImpl extends Hologram {

    public HologramImpl(int entityId, Location location, String text) {
        super(entityId, location, text);
    }

    @Override
    protected void create(Player player) {
        ClientboundAddEntityPacket packet = new ClientboundAddEntityPacket(
                this.entityId,
                UUID.randomUUID(),
                this.location.getX(),
                this.location.getY(),
                this.location.getZ(),
                90,
                0,
                EntityType.AREA_EFFECT_CLOUD,
                1,
                Vec3.ZERO
        );

        ((CraftPlayer) player).getHandle().connection.send(packet);
    }

    @Override
    protected void update(Player player) {
        Boolean visible = this.watchers.get(player);
        if (visible == null)
            return;

        List<SynchedEntityData.DataItem<?>> dataItems = new ArrayList<>();
        Optional<Component> nameComponent = Optional.of(CraftChatMessage.fromStringOrNull(this.text));
        dataItems.add(new SynchedEntityData.DataItem<>(EntityDataSerializers.OPTIONAL_COMPONENT.createAccessor(2), nameComponent));
        dataItems.add(new SynchedEntityData.DataItem<>(EntityDataSerializers.BOOLEAN.createAccessor(3), visible));
        dataItems.add(new SynchedEntityData.DataItem<>(EntityDataSerializers.FLOAT.createAccessor(8), 0.5F));
        dataItems.add(new SynchedEntityData.DataItem<>(EntityDataSerializers.BOOLEAN.createAccessor(10), true));
        dataItems.add(new SynchedEntityData.DataItem<>(EntityDataSerializers.PARTICLE.createAccessor(11), new BlockParticleOption(ParticleTypes.BLOCK, Blocks.AIR.defaultBlockState())));

        ClientboundSetEntityDataPacket packet = new ClientboundSetEntityDataPacket(this.entityId, new SynchedEntityDataWrapper(dataItems), false);
        ((CraftPlayer) player).getHandle().connection.send(packet);
    }

    @Override
    protected void delete(Player player) {
        ClientboundRemoveEntitiesPacket packet = new ClientboundRemoveEntitiesPacket(this.entityId);

        ((CraftPlayer) player).getHandle().connection.send(packet);
    }

}
