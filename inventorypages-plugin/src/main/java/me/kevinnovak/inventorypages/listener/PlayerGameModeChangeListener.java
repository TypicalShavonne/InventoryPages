package me.kevinnovak.inventorypages.listener;

import me.kevinnovak.inventorypages.InventoryPages;
import me.kevinnovak.inventorypages.manager.DatabaseManager;
import me.kevinnovak.inventorypages.manager.DebugManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerGameModeChangeEvent;

public class PlayerGameModeChangeListener implements Listener {
    public PlayerGameModeChangeListener() {
        Bukkit.getPluginManager().registerEvents(this, InventoryPages.plugin);
        DebugManager.debug("LOADING EVENTS", "Loaded PlayerGameModeChangeEvent");
    }

    @EventHandler
    public void onPlayerGameModeChange(PlayerGameModeChangeEvent event) {
        Player player = event.getPlayer();
        String playerUUID = player.getUniqueId().toString();
        if (DatabaseManager.playerInvs.containsKey(playerUUID)) {
            DatabaseManager.playerInvs.get(playerUUID).getCustomInventory().saveCurrentPage();
            DatabaseManager.playerInvs.get(playerUUID).getCustomInventory().showPage(event.getNewGameMode());
        }
    }
}
