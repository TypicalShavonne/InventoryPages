package me.kevinnovak.inventorypages.listener;

import me.kevinnovak.inventorypages.InventoryPages;
import me.kevinnovak.inventorypages.manager.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

import java.io.IOException;

public class PlayerJoinListener implements Listener {
    public PlayerJoinListener() {
        Bukkit.getPluginManager().registerEvents(this, InventoryPages.plugin);
    }

    @EventHandler
    public void playerJoin(PlayerJoinEvent event) throws InterruptedException, IOException {
        Player player = event.getPlayer();
        DatabaseManager.loadInvFromFileIntoHashMap(player);
    }
}
