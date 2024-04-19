package me.kevinnovak.inventorypages.manager;

import me.kevinnovak.inventorypages.CustomInventory;
import me.kevinnovak.inventorypages.InventoryPages;
import me.kevinnovak.inventorypages.InventoryStringDeSerializer;
import me.kevinnovak.inventorypages.inventory.PlayerPageInventory;
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

public class DatabaseManager {

    public static HashMap<String, CustomInventory> playerInvs = new HashMap<String, CustomInventory>();
    public static File crashedFile = new File(InventoryPages.plugin.getDataFolder() + "/backups/crashed.yml");
    public static FileConfiguration crashedData = YamlConfiguration.loadConfiguration(crashedFile);


    public static void loadInvFromFileIntoHashMap(Player player) throws IOException {
        clearAndRemoveCrashedPlayer(player);

        int maxPage = 1;
        Boolean foundPerm = false;
        for (int i = 2; i < 101; i++) {
            if (player.hasPermission("inventorypages.pages." + i)) {
                foundPerm = true;
                maxPage = i - 1;
            }
        }

        if (foundPerm) {
            String playerUUID = player.getUniqueId().toString();
            CustomInventory inventory = new CustomInventory(InventoryPages.plugin, player, maxPage, PlayerPageInventory.prevItem, PlayerPageInventory.prevPos, PlayerPageInventory.nextItem, PlayerPageInventory.nextPos, PlayerPageInventory.noPageItem);
            DatabaseManager.playerInvs.put(playerUUID, inventory);
            addCrashedPlayer(player);
            DatabaseManager.playerInvs.get(playerUUID).showPage(player.getGameMode());
        }
    }

    public static void updateAndSaveAllInventoriesToFiles() {
        if (!Bukkit.getServer().getOnlinePlayers().isEmpty()) {
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                String playerUUID = player.getUniqueId().toString();
                if (playerInvs.containsKey(playerUUID)) {
                    updateInvToHashMap(player);
                    saveInvFromHashMapToFile(player);
                }
            }
        }
    }

    public static void clearAndRemoveCrashedPlayer(Player player) {
        if (crashedPlayersExist()) {
            if (hasCrashed(player)) {
                for (int i = 0; i < 27; i++) {
                    player.getInventory().setItem(i + 9, null);
                }
                crashedData.set("crashed." + player.getUniqueId().toString(), null);
                saveCrashedFile();
            }
        }
    }

    // ======================================
    // Save Inventory From HashMap To File
    // ======================================
    public static void saveInvFromHashMapToFile(Player player) {
        String playerUUID = player.getUniqueId().toString();
        if (DatabaseManager.playerInvs.containsKey(playerUUID)) {
            File playerFile = new File(InventoryPages.plugin.getDataFolder() + "/database/" + playerUUID.substring(0, 1) + "/" + playerUUID + ".yml");
            FileConfiguration playerData = YamlConfiguration.loadConfiguration(playerFile);

            // save survival items
            for (Map.Entry<Integer, ArrayList<ItemStack>> pageItemEntry : DatabaseManager.playerInvs.get(playerUUID).getItems().entrySet()) {
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
            if (DatabaseManager.playerInvs.get(playerUUID).hasUsedCreative()) {
                for (int slotNumber = 0; slotNumber < DatabaseManager.playerInvs.get(playerUUID).getCreativeItems().size(); slotNumber++) {
                    if (DatabaseManager.playerInvs.get(playerUUID).getCreativeItems().get(slotNumber) != null) {
                        playerData.set("items.creative.0." + slotNumber, InventoryStringDeSerializer.toBase64(DatabaseManager.playerInvs.get(playerUUID).getCreativeItems().get(slotNumber)));
                    } else {
                        playerData.set("items.creative.0." + slotNumber, null);
                    }
                }
            }

            // save current page
            playerData.set("page", DatabaseManager.playerInvs.get(playerUUID).getPage());

            try {
                playerData.save(playerFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // ======================================
    // Update Inventory To HashMap
    // ======================================
    public static void updateInvToHashMap(Player player) {
        String playerUUID = player.getUniqueId().toString();
        if (DatabaseManager.playerInvs.containsKey(playerUUID)) {
            DatabaseManager.playerInvs.get(playerUUID).saveCurrentPage();
        }
    }

    // ======================================
    // Remove Inventory From HashMap
    // ======================================
    public static void removeInvFromHashMap(Player player) {
        String playerUUID = player.getUniqueId().toString();
        if (DatabaseManager.playerInvs.containsKey(playerUUID)) {
            DatabaseManager.playerInvs.remove(playerUUID);
            clearAndRemoveCrashedPlayer(player);
        }
    }

    // ======================================
    // Save Crashed File
    // ======================================
    public static void saveCrashedFile() {
        try {
            crashedData.save(crashedFile);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    // ======================================
    // Crashed Players Exist
    // ======================================
    public static Boolean crashedPlayersExist() {
        if (crashedData.contains("crashed")) {
            if (crashedData.getConfigurationSection("crashed").getKeys(false).size() > 0) {
                return true;
            }
        }
        return false;
    }

    // ======================================
    // Has Crashed
    // ======================================
    public static Boolean hasCrashed(Player player) {
        if (crashedData.contains("crashed." + player.getUniqueId().toString())) {
            return true;
        }
        return false;
    }

    // ======================================
    // Add Crashed Player
    // ======================================
    public static void addCrashedPlayer(Player player) {
        crashedData.set("crashed." + player.getUniqueId().toString(), true);
        saveCrashedFile();
    }

}
