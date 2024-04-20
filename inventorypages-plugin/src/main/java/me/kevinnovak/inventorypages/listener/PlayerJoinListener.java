package me.kevinnovak.inventorypages.listener;

import me.kevinnovak.inventorypages.InventoryPages;
import me.kevinnovak.inventorypages.manager.DatabaseManager;
import me.kevinnovak.inventorypages.manager.DebugManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.IOException;

public class PlayerJoinListener implements Listener {
    public PlayerJoinListener() {
        Bukkit.getPluginManager().registerEvents(this, InventoryPages.plugin);
        DebugManager.debug("LOADING EVENTS", "Loaded PlayerJoinEvent");
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) throws InterruptedException, IOException {
        Player player = event.getPlayer();
        DatabaseManager.loadPlayerInventory(player);
    }
}
