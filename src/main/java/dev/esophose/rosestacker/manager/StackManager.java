package dev.esophose.rosestacker.manager;

import dev.esophose.rosestacker.RoseStacker;
import dev.esophose.rosestacker.manager.ConfigurationManager.Setting;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class StackManager extends Manager implements Runnable {

    private BukkitTask task;

    public StackManager(RoseStacker roseStacker) {
        super(roseStacker);
    }

    @Override
    public void reload() {
        if (this.task != null)
            this.task.cancel();

        this.task = Bukkit.getScheduler().runTaskTimer(this.roseStacker, this, 0, 5);
    }

    @Override
    public void disable() {
        this.task.cancel();
    }

    @Override
    public void run() {

    }

}
