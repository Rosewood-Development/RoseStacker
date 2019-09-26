package dev.esophose.rosestacker.manager;

import dev.esophose.rosestacker.RoseStacker;
import net.minecraft.server.v1_14_R1.MobSpawnerAbstract;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.scheduler.BukkitTask;

import java.util.Random;

public class SpawnerSpawnManager extends Manager implements Runnable {

    /**
     * At what point should we override the normal spawner spawning?
     */
    private static final int DELAY_THRESHOLD = 3;

    private Random random;
    private BukkitTask task;

    public SpawnerSpawnManager(RoseStacker roseStacker) {
        super(roseStacker);

        this.random = new Random();
    }

    @Override
    public void reload() {
        if (this.task != null)
            this.task.cancel();

        this.task = Bukkit.getScheduler().runTaskTimer(this.roseStacker, this, 0, 1);
    }

    @Override
    public void disable() {
        if (this.task != null)
            this.task.cancel();
    }

    @Override
    public void run() {
        // TODO: Custom spawner algorithm so all the mobs actually get spawned when massive spawners attempt to spawn
//        StackManager stackManager = this.roseStacker.getStackManager();
//
//        for (Block block : stackManager.getStackedSpawners().keySet()) {
//            if (block.getType() != Material.SPAWNER)
//                continue;
//
//            CreatureSpawner spawner = (CreatureSpawner) block.getState();
//            if (spawner.getDelay() >= DELAY_THRESHOLD)
//                continue;
//
//            // Reset the spawn delay
//            int newDelay = this.random.nextInt(spawner.getMaxSpawnDelay() - spawner.getMinSpawnDelay() + 1) + spawner.getMinSpawnDelay();
//            spawner.setDelay(newDelay);
//            spawner.update();
//
//            // Spawn particles indicating the spawn occurred
//            block.getWorld().spawnParticle(Particle.FLAME, block.getLocation().clone().add(0.5, 0.5, 0.5), 30, 0.5, 0.5, 0.5, 0);
//        }
    }

}
