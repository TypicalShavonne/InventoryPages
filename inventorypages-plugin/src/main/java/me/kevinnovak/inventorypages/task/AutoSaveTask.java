package me.kevinnovak.inventorypages.task;

import me.kevinnovak.inventorypages.InventoryPages;
import me.kevinnovak.inventorypages.manager.DatabaseManager;
import me.kevinnovak.inventorypages.manager.DebugManager;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

public class AutoSaveTask implements Runnable {

    private BukkitTask task;

    public AutoSaveTask(int time) {
        this.task = Bukkit.getScheduler().runTaskTimerAsynchronously(InventoryPages.plugin, this, 20L * time, 20L * time);
    }

    public BukkitTask getBukkitTask() {
        return task;
    }

    public int getTaskID() {
        return task.getTaskId();
    }

    @Override
    public void run() {
        if (Bukkit.getOnlinePlayers().isEmpty()) {
            DebugManager.debug("AUTO SAVING", "Không lưu vì không có người chơi.");
            return;
        }

        DatabaseManager.updateAndSaveAllInventoriesToDatabase();
        DebugManager.debug("AUTO SAVING", "Đã tự động lưu thành công " + Bukkit.getOnlinePlayers().size() + " dữ liệu.");
    }

    public void cancel() {
        task.cancel();
    }

}
