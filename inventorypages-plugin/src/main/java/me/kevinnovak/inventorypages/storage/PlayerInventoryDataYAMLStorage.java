package me.kevinnovak.inventorypages.storage;

import me.kevinnovak.inventorypages.InventoryPages;
import me.kevinnovak.inventorypages.inventory.PlayerPageInventory;
import me.kevinnovak.inventorypages.manager.DatabaseManager;
import me.kevinnovak.inventorypages.manager.DebugManager;
import me.kevinnovak.inventorypages.util.StringUtil;
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

public class PlayerInventoryDataYAMLStorage implements PlayerInventoryStorage {

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
        int maxPageDefault = InventoryPages.plugin.getConfig().getInt("inventory-settings.max-page-default");
        if (maxPageDefault < 0)
            maxPageDefault = 0;

        PlayerInventoryData data = new PlayerInventoryData(Bukkit.getPlayer(playerName), playerName, playerUUID, maxPageDefault,null, null, PlayerPageInventory.prevItem, PlayerPageInventory.prevPos, PlayerPageInventory.nextItem, PlayerPageInventory.nextPos, PlayerPageInventory.noPageItem);

        if (storage.getString("name") == null) {
            return data;
        } else {
            int maxPage = storage.getInt("maxPage");
            data.setMaxPage(maxPage);
            data.setPlayerName(playerName);
            data.setPlayerUUID(playerUUID);
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
                                item = StringUtil.stacksFromBase64(storage.getString("items.main." + page + "." + slotNumber))[0];
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
            data.setItems(pageItemHashMap);

            // load creative items
            if (storage.contains("items.creative.0")) {
                ArrayList<ItemStack> creativeItems = new ArrayList<>();
                for (int i = 0; i < 27; i++) {
                    ItemStack item = null;
                    if (storage.contains("items.creative.0." + i)) {
                        try {
                            item = StringUtil.stacksFromBase64(storage.getString("items.creative.0." + i))[0];
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                    creativeItems.add(item);
                }
                data.setCreativeItems(creativeItems);
            }

            if (InventoryPages.plugin.getConfig().getBoolean("inventory-settings.use-saved-current-page"))
                data.setPage(storage.getInt("currentPage"));

            if (!InventoryPages.plugin.getConfig().getBoolean("inventory-settings.focus-using-default-item-position")) {
                data.setPrevItemPos(storage.getInt("prevItemPos"));
                data.setNextItemPos(storage.getInt("nextItemPos"));
            }
        }

        return data;
    }

    @Override
    public PlayerInventoryData getData(Player player) {
        File file = getFile(player.getUniqueId().toString());
        return fromFile(file, player.getName(), player.getUniqueId().toString());
    }

    @Override
    public void saveData(PlayerInventoryData data) {
        File playerFile = getFile(data.getPlayerUUID());
        FileConfiguration playerDataCfg = YamlConfiguration.loadConfiguration(playerFile);
        PlayerInventoryData playerInventoryData = DatabaseManager.playerInvs.get(data.getPlayerUUID());

        playerDataCfg.set("name", playerInventoryData.getPlayerName());
        playerDataCfg.set("uuid", playerInventoryData.getPlayerUUID());
        playerDataCfg.set("maxPage", playerInventoryData.getMaxPage());
        playerDataCfg.set("currentPage", playerInventoryData.getPage());
        playerDataCfg.set("prevItemPos", playerInventoryData.getPrevItemPos());
        playerDataCfg.set("nextItemPos", playerInventoryData.getNextItemPos());

        // save survival items
        for (Map.Entry<Integer, ArrayList<ItemStack>> pageItemEntry : playerInventoryData.getItems().entrySet()) {
            for (int slotNumber = 0; slotNumber < pageItemEntry.getValue().size(); slotNumber++) {
                int pageNumber = pageItemEntry.getKey();
                if (pageItemEntry.getValue().get(slotNumber) != null) {
                    playerDataCfg.set("items.main." + pageNumber + "." + slotNumber, StringUtil.toBase64(InventoryPages.nms.getItemStack(pageItemEntry.getValue().get(slotNumber))));
                } else {
                    playerDataCfg.set("items.main." + pageNumber + "." + slotNumber, null);
                }
            }
        }

        // save creative items
        if (DatabaseManager.playerInvs.get(data.getPlayerUUID()).hasUsedCreative()) {
            for (int slotNumber = 0; slotNumber < 27; slotNumber++) {
                if (DatabaseManager.playerInvs.get(data.getPlayerUUID()).getCreativeItems().get(slotNumber) != null) {
                    playerDataCfg.set("items.creative.0." + slotNumber, StringUtil.toBase64(playerInventoryData.getCreativeItems().get(slotNumber)));
                } else {
                    playerDataCfg.set("items.creative.0." + slotNumber, null);
                }
            }
        }

        try {
            playerDataCfg.save(playerFile);
            DebugManager.debug("SAVING INV. FROM HASHMAP TO FILE PLAYER (" + data.getPlayerName() + ")", "Completed with no issues.");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
