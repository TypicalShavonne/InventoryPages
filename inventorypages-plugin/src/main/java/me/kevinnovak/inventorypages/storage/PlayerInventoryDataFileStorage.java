package me.kevinnovak.inventorypages.storage;

import me.kevinnovak.inventorypages.CustomInventory;
import me.kevinnovak.inventorypages.InventoryPages;
import me.kevinnovak.inventorypages.InventoryStringDeSerializer;
import me.kevinnovak.inventorypages.inventory.PlayerPageInventory;
import me.kevinnovak.inventorypages.manager.DatabaseManager;
import me.kevinnovak.inventorypages.manager.DebugManager;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class PlayerInventoryDataFileStorage implements PlayerInventoryStorage {

    private File getFile(String playerUUID) {

        String path = InventoryPages.plugin.getDataFolder() + "/database/" + playerUUID.substring(0, 1) + "/";
        if (!new File(path).exists())
            new File(path).mkdir();

        File file = new File(path + playerUUID + ".yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public PlayerInventoryData fromFile(File file, String playerName, String playerUUID) {
        YamlConfiguration storage = YamlConfiguration.loadConfiguration(file);

        HashMap<Integer, ArrayList<ItemStack>> pageItemHashMap = new HashMap<>();
        ArrayList<ItemStack> creativeItems = new ArrayList<>(27);
        int maxPageDefault = InventoryPages.plugin.getConfig().getInt("inventory-settings.max-page-default");
        if (maxPageDefault < 0)
            maxPageDefault = 0;

        CustomInventory customInventory = new CustomInventory(InventoryPages.plugin, Bukkit.getPlayer(playerName), pageItemHashMap, creativeItems, maxPageDefault, PlayerPageInventory.prevItem, PlayerPageInventory.prevPos, PlayerPageInventory.nextItem, PlayerPageInventory.nextPos, PlayerPageInventory.noPageItem);
        PlayerInventoryData data = new PlayerInventoryData(playerName, playerUUID, pageItemHashMap, creativeItems, customInventory, maxPageDefault, 0);

        if (!storage.contains("items.main")) {
            return data;
        } else {
            int maxPage = storage.getInt("maxPage");
            data.setMaxPage(maxPage);
            // load survival items
            for (int page = 0; page < maxPage + 1; page++) {
                //Bukkit.getLogger().info("Loading " + playerUUID + "'s Page: " + i);
                // số item sẽ có trong trang %page%
                ArrayList<ItemStack> pageItems = new ArrayList<>(25);
                for (int slotNumber = 0; slotNumber < 25; slotNumber++) {
                    ItemStack item = null;
                    if (storage.contains("items.main." + page + "." + slotNumber)) {
                        if (storage.getString("items.main." + page + "." + slotNumber) != null) {
                            try {
                                item = InventoryStringDeSerializer.stacksFromBase64(storage.getString("items.main." + page + "." + slotNumber))[0];
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                    pageItems.add(item);
                }
                // add item vào page %page%
                pageItemHashMap.put(page, pageItems);
            }
            data.setPageItemHashMap(pageItemHashMap);

            // load creative items
            if (storage.contains("items.creative.0")) {
                for (int i = 0; i < 27; i++) {
                    ItemStack item = null;
                    if (storage.contains("items.creative.0." + i)) {
                        try {
                            item = InventoryStringDeSerializer.stacksFromBase64(storage.getString("items.creative.0." + i))[0];
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    creativeItems.add(item);
                }
                data.setCreativeItems(creativeItems);
            }
        }

        return data;
    }

    @Override
    public void saveData(PlayerInventoryData data) {
        File playerFile = getFile(data.getPlayerUUID());
        FileConfiguration playerData = YamlConfiguration.loadConfiguration(playerFile);

        // save survival items
        for (Map.Entry<Integer, ArrayList<ItemStack>> pageItemEntry : DatabaseManager.playerInvs.get(data.getPlayerUUID()).getCustomInventory().getItems().entrySet()) {
            for (int slotNumber = 0; slotNumber < pageItemEntry.getValue().size(); slotNumber++) {
                int pageNumber = pageItemEntry.getKey();
                if (pageItemEntry.getValue().get(slotNumber) != null) {
                    playerData.set("items.main." + pageNumber + "." + slotNumber, InventoryStringDeSerializer.toBase64(InventoryPages.nms.getItemStack(pageItemEntry.getValue().get(slotNumber))));
                } else {
                    playerData.set("items.main." + pageNumber + "." + slotNumber, null);
                }
            }
        }

        // save creative items
        if (DatabaseManager.playerInvs.get(data.getPlayerUUID()).getCustomInventory().hasUsedCreative()) {
            for (int slotNumber = 0; slotNumber < 27; slotNumber++) {
                if (DatabaseManager.playerInvs.get(data.getPlayerUUID()).getCreativeItems().get(slotNumber) != null) {
                    playerData.set("items.creative.0." + slotNumber, InventoryStringDeSerializer.toBase64(DatabaseManager.playerInvs.get(data.getPlayerUUID()).getCreativeItems().get(slotNumber)));
                } else {
                    playerData.set("items.creative.0." + slotNumber, null);
                }
            }
        }

        // save current page
        playerData.set("maxPage", DatabaseManager.playerInvs.get(data.getPlayerUUID()).getMaxPage());
        playerData.set("page", DatabaseManager.playerInvs.get(data.getPlayerUUID()).getPage());

        try {
            playerData.save(playerFile);
            DebugManager.debug("SAVING INV. FROM HASHMAP TO FILE PLAYER (" + data.getPlayerName() + ")", "Completed with no issues.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public PlayerInventoryData getData(Player player) {
        File file = getFile(player.getUniqueId().toString());
        return fromFile(file, player.getName(), player.getUniqueId().toString());
    }
}
