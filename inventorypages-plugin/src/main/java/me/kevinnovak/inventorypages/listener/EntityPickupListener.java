package me.kevinnovak.inventorypages.listener;

import me.kevinnovak.inventorypages.InventoryPages;
import me.kevinnovak.inventorypages.inventory.PlayerPageInventory;
import me.kevinnovak.inventorypages.manager.DebugManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class EntityPickupListener implements Listener {
    public EntityPickupListener() {
        Bukkit.getPluginManager().registerEvents(this, InventoryPages.plugin);
        DebugManager.debug("LOADING EVENT", "Loaded EntityPickupItemEvent.");
    }

    @EventHandler
    public void onPickup(EntityPickupItemEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            ItemStack item = event.getItem().getItemStack();
            if (item.isSimilar(PlayerPageInventory.nextItem) || item.isSimilar(PlayerPageInventory.prevItem)) {
                event.setCancelled(true);
                event.getItem().remove();
            }
        }
    }
}
