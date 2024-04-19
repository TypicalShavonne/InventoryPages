package me.kevinnovak.inventorypages.inventory;

import me.kevinnovak.inventorypages.file.inventory.PlayerInventoryFile;
import me.kevinnovak.inventorypages.util.ItemUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

public class PlayerPageInventory {
    public static ItemStack nextItem, prevItem, noPageItem;
    public static Integer prevPos, nextPos;


    public static void setupItems() {
        FileConfiguration playerInvCfg = PlayerInventoryFile.get();
        prevItem = ItemUtil.getItem(playerInvCfg.getString("items.prev.type"),
                playerInvCfg.getString("items.prev.value"),
                (short) playerInvCfg.getInt("items.prev.data"),
                playerInvCfg.getString("items.prev.name"),
                playerInvCfg.getStringList("items.prev.lore"));
        prevPos = playerInvCfg.getInt("items.prev.slot");

        nextItem = ItemUtil.getItem(playerInvCfg.getString("items.next.type"),
                playerInvCfg.getString("items.next.value"),
                (short) playerInvCfg.getInt("items.next.data"),
                playerInvCfg.getString("items.next.name"),
                playerInvCfg.getStringList("items.next.lore"));
        nextPos = playerInvCfg.getInt("items.next.slot");

        noPageItem = ItemUtil.getItem(playerInvCfg.getString("items.noPage.type"),
                playerInvCfg.getString("items.noPage.value"),
                (short) playerInvCfg.getInt("items.noPage.data"),
                playerInvCfg.getString("items.noPage.name"),
                playerInvCfg.getStringList("items.noPage.lore"));
    }

}
