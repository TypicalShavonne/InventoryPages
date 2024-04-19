package me.kevinnovak.inventorypages.listener;

import me.kevinnovak.inventorypages.InventoryPages;
import me.kevinnovak.inventorypages.manager.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitListener implements Listener {
    public PlayerQuitListener() {
        Bukkit.getPluginManager().registerEvents(this, InventoryPages.plugin);
    }

    @EventHandler
    public void playerQuit(PlayerQuitEvent event) throws InterruptedException {
        Player player = event.getPlayer();
        String playerUUID = player.getUniqueId().toString();
        if (DatabaseManager.playerInvs.containsKey(playerUUID)) {
            DatabaseManager.updateInvToHashMap(player);
            DatabaseManager.saveInvFromHashMapToFile(player);
            DatabaseManager.removeInvFromHashMap(player);
        }
    }
}
