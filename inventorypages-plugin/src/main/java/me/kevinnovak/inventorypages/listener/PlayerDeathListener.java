package me.kevinnovak.inventorypages.listener;

import me.kevinnovak.inventorypages.InventoryPages;
import me.kevinnovak.inventorypages.manager.DatabaseManager;
import me.kevinnovak.inventorypages.manager.DebugManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class PlayerDeathListener implements Listener {
    public PlayerDeathListener() {
        Bukkit.getPluginManager().registerEvents(this, InventoryPages.plugin);
        DebugManager.debug("LOADING EVENT", "Loaded PlayerDeathEvent.");
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        String playerUUID = player.getUniqueId().toString();
        if (DatabaseManager.playerInvs.containsKey(playerUUID)) {
            //save items before death
            DatabaseManager.updateInvToHashMap(player);

            event.setKeepInventory(true);

            GameMode gm = player.getGameMode();

            // Default drop all
            int dropOption = 2;

            // If you have keep unopened, drop only the current page
            if (player.hasPermission("inventorypages.keep.unopened")) {
                dropOption = 1;
            }

            // If you have keep all, don't drop anything
            if (player.hasPermission("inventorypages.keep.all")) {
                dropOption = 0;
            }

            if (dropOption == 1) {
                DatabaseManager.playerInvs.get(playerUUID).dropPage(gm);
            } else if (dropOption == 2) {
                DatabaseManager.playerInvs.get(playerUUID).dropAllPages(gm);
            }

            if (!player.hasPermission("inventorypages.keep.hotbar")) {
                dropHotbar(player);
            }
        }
    }

    private void dropHotbar(Player player) {
        PlayerInventory playerInv = player.getInventory();
        for (int i = 0; i <= 8; i++) {
            ItemStack item = InventoryPages.nms.getItemStack(playerInv.getItem(i));
            if (item != null) {
                player.getWorld().dropItemNaturally(player.getLocation(), item);
                player.getInventory().remove(item);
            }
        }
    }
}
