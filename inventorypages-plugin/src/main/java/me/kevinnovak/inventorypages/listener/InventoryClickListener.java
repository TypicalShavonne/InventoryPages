package me.kevinnovak.inventorypages.listener;

import me.kevinnovak.inventorypages.InventoryPages;
import me.kevinnovak.inventorypages.file.MessageFile;
import me.kevinnovak.inventorypages.inventory.PlayerPageInventory;
import me.kevinnovak.inventorypages.manager.DatabaseManager;
import me.kevinnovak.inventorypages.manager.DebugManager;
import me.kevinnovak.inventorypages.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.InventoryView;
import org.bukkit.inventory.ItemStack;

public class InventoryClickListener implements Listener {
    public InventoryClickListener() {
        Bukkit.getPluginManager().registerEvents(this, InventoryPages.plugin);
        DebugManager.debug("LOADING EVENT", "Loaded InventoryClickEvent.");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Inventory clickedInv = getClickedInventory(event.getView(), event.getRawSlot());
        if (clickedInv != null) {
            if (clickedInv.getType() == InventoryType.PLAYER) {
                InventoryHolder holder = clickedInv.getHolder();
                if (holder instanceof Player) {
                    Player player = (Player) holder;
                    if (hasSwitcherItems(player)) {
                        ItemStack item = event.getCurrentItem();
                        int customInvSLot = event.getSlot() - 9;
                        if (isSwitcherItem(item, PlayerPageInventory.prevItem) || customInvSLot == DatabaseManager.playerInvs.get(player.getUniqueId().toString()).getPrevItemPos()) {
                            event.setCancelled(true);
                            DatabaseManager.playerInvs.get(player.getUniqueId().toString()).prevPage();
                        } else if (isSwitcherItem(item, PlayerPageInventory.nextItem)  || customInvSLot == DatabaseManager.playerInvs.get(player.getUniqueId().toString()).getNextItemPos()) {
                            event.setCancelled(true);
                            DatabaseManager.playerInvs.get(player.getUniqueId().toString()).nextPage();
                        } else if (isSwitcherItem(item, PlayerPageInventory.noPageItem)) {
                            MessageUtil.sendMessage(player, MessageFile.get().getString("messages.no-page-message"));
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    public Inventory getClickedInventory(InventoryView view, int slot) {

        int topInvSize = view.getTopInventory().getSize();
        if (view.getTopInventory().getType() == InventoryType.PLAYER) {
            int topInvRemainder = topInvSize % 9;
            if (topInvRemainder != 0) {
                topInvSize = topInvSize - topInvRemainder;
            }
        }

        Inventory clickedInventory;
        if (slot < 0) {
            clickedInventory = null;
        } else if (view.getTopInventory() != null && slot < topInvSize) {
            clickedInventory = view.getTopInventory();
        } else {
            clickedInventory = view.getBottomInventory();
        }
        return clickedInventory;
    }

    public Boolean hasSwitcherItems(Player player) {
        String playerUUID = player.getUniqueId().toString();
        if (DatabaseManager.playerInvs.containsKey(playerUUID)) {
            if (player.getGameMode() != GameMode.CREATIVE) {
                return true;
            }
        }
        return false;
    }

    public Boolean isSwitcherItem(ItemStack item, ItemStack switcherItem) {
        if (item != null) {
            if (item.getType() != null) {
                if (item.getType().equals(switcherItem.getType())) {
                    if (item.getItemMeta() != null) {
                        if (item.getItemMeta().getDisplayName() != null) {
                            if (item.getItemMeta().getDisplayName().equals(switcherItem.getItemMeta().getDisplayName())) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

}
