package dev.rosewood.rosestacker.nms.v1_19_R1.hologram;

import dev.rosewood.rosestacker.nms.hologram.Hologram;
import dev.rosewood.rosestacker.nms.hologram.HologramLine;
import dev.rosewood.rosestacker.nms.v1_19_R1.entity.SynchedEntityDataWrapper;
import java.util.ArrayList;
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
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_19_R1.util.CraftChatMessage;
import org.bukkit.entity.Player;

public class HologramImpl extends Hologram {
    
    private static final List<SynchedEntityData.DataItem<?>> DATA_ITEMS = List.of(
            new SynchedEntityData.DataItem<>(EntityDataSerializers.FLOAT.createAccessor(8), 0.5F),
            new SynchedEntityData.DataItem<>(EntityDataSerializers.BOOLEAN.createAccessor(10), true),
            new SynchedEntityData.DataItem<>(EntityDataSerializers.PARTICLE.createAccessor(11), new BlockParticleOption(ParticleTypes.BLOCK, Blocks.AIR.defaultBlockState()))
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

            List<SynchedEntityData.DataItem<?>> dataItems = new ArrayList<>(DATA_ITEMS);
            Optional<Component> chatMessage = Optional.of(CraftChatMessage.fromStringOrNull(line.getText()));
            dataItems.add(new SynchedEntityData.DataItem<>(EntityDataSerializers.OPTIONAL_COMPONENT.createAccessor(2), chatMessage));

            for (Player player : players) {
                Boolean visible = this.watchers.get(player);
                if (visible == null)
                    return;

                List<SynchedEntityData.DataItem<?>> allDataItems = new ArrayList<>(dataItems);
                allDataItems.add(new SynchedEntityData.DataItem<>(EntityDataSerializers.BOOLEAN.createAccessor(3), visible));

                ((CraftPlayer) player).getHandle().connection.send(new ClientboundSetEntityDataPacket(line.getEntityId(), new SynchedEntityDataWrapper(allDataItems), false));
            }
        }
    }

    @Override
    protected void delete(Player player) {
        ClientboundRemoveEntitiesPacket packet = new ClientboundRemoveEntitiesPacket(this.hologramLines.stream().mapToInt(HologramLine::getEntityId).toArray());

        ((CraftPlayer) player).getHandle().connection.send(packet);
    }

}
