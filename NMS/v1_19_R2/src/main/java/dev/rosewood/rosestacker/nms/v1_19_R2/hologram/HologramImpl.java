package dev.rosewood.rosestacker.nms.v1_19_R2.hologram;

import dev.rosewood.rosestacker.nms.hologram.Hologram;
import dev.rosewood.rosestacker.nms.hologram.HologramLine;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
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
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R2.util.CraftChatMessage;
import org.bukkit.entity.Player;

public class HologramImpl extends Hologram {
    
    private static final List<SynchedEntityData.DataValue<?>> DATA_VALUES = Arrays.asList(
            SynchedEntityData.DataValue.create(EntityDataSerializers.FLOAT.createAccessor(8), 0.5F),
            SynchedEntityData.DataValue.create(EntityDataSerializers.BOOLEAN.createAccessor(10), true),
            SynchedEntityData.DataValue.create(EntityDataSerializers.PARTICLE.createAccessor(11), new BlockParticleOption(ParticleTypes.BLOCK, Blocks.AIR.defaultBlockState()))
    );

    public HologramImpl(List<String> text, Location location, Supplier<Integer> entityIdSupplier) {
        super(text, location, entityIdSupplier);
    }

    @Override
    protected void create(Player player) {
        for (HologramLine line : this.hologramLines) {
            ClientboundAddEntityPacket packet = new ClientboundAddEntityPacket(
                    line.getEntityId(),
                    UUID.randomUUID(),
                    line.getLocation().getX(),
                    line.getLocation().getY(),
                    line.getLocation().getZ(),
                    90,
                    0,
                    EntityType.AREA_EFFECT_CLOUD,
                    1,
                    Vec3.ZERO,
                    0
            );

            ((CraftPlayer) player).getHandle().connection.send(packet);
        }
    }

    @Override
    protected void update(Collection<Player> players, boolean force) {
        for (HologramLine line : this.hologramLines) {
            if (!force && !line.checkDirty())
                continue;

            List<SynchedEntityData.DataValue<?>> dataValues = new ArrayList<>(DATA_VALUES);
            Optional<Component> chatMessage = Optional.of(CraftChatMessage.fromStringOrNull(line.getText()));
            dataValues.add(SynchedEntityData.DataValue.create(EntityDataSerializers.OPTIONAL_COMPONENT.createAccessor(2), chatMessage));

            for (Player player : players) {
                Boolean visible = this.watchers.get(player);
                if (visible == null)
                    return;

                List<SynchedEntityData.DataValue<?>> allDataValues = new ArrayList<>(dataValues);
                allDataValues.add(SynchedEntityData.DataValue.create(EntityDataSerializers.BOOLEAN.createAccessor(3), visible));

                ((CraftPlayer) player).getHandle().connection.send(new ClientboundSetEntityDataPacket(line.getEntityId(), allDataValues));
            }
        }
    }

    @Override
    protected void delete(Player player) {
        ClientboundRemoveEntitiesPacket packet = new ClientboundRemoveEntitiesPacket(this.hologramLines.stream().mapToInt(HologramLine::getEntityId).toArray());

        ((CraftPlayer) player).getHandle().connection.send(packet);
    }

}
