package me.kevinnovak.inventorypages.manager;

import me.kevinnovak.inventorypages.InventoryPages;
import me.kevinnovak.inventorypages.storage.PlayerInventoryData;
import me.kevinnovak.inventorypages.storage.PlayerInventoryDataStorage;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

public class DatabaseManager {

    public static HashMap<String, PlayerInventoryData> playerInvs = new HashMap<>();
    public static File crashedFile = new File(InventoryPages.plugin.getDataFolder() + "/database/crashed.yml");
    public static FileConfiguration crashedData = YamlConfiguration.loadConfiguration(crashedFile);

    public static void loadPlayerInventory(Player player) {
        clearAndRemoveCrashedPlayer(player);

        String playerUUID = player.getUniqueId().toString();

        if (playerInvs.containsKey(playerUUID))
            return;

        playerInvs.put(playerUUID, PlayerInventoryDataStorage.getPlayerInventoryData(player));
        addCrashedPlayer(player);
        playerInvs.get(playerUUID).showPage(player.getGameMode());
        DebugManager.debug("LOADING DATABASE PLAYER (" + player.getName() + ")", "Completed with no issues.");
    }

    public static void updateAndSaveAllInventoriesToDatabase() {
        if (!Bukkit.getServer().getOnlinePlayers().isEmpty()) {
            for (Player player : Bukkit.getServer().getOnlinePlayers()) {
                String playerUUID = player.getUniqueId().toString();
                if (playerInvs.containsKey(playerUUID)) {
                    updateInvToHashMap(player);
                    savePlayerInventory(player);
                }
            }
            DebugManager.debug("UPDATING AND SAVING ALL INVENTORIES", "Completed with no issues.");
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
                DebugManager.debug("CLEARING CRASHED PLAYER (" + player.getName() + ")", "Completed with no issues.");
            }
        }
    }

    // ======================================
    // Save Inventory From HashMap To Database
    // ======================================
    public static void savePlayerInventory(Player player) {
        PlayerInventoryDataStorage.savePlayerInventoryData(playerInvs.get(player.getUniqueId().toString()));
        DebugManager.debug("SAVING DATABASE PLAYER (" + player.getName() + ")", "Completed with no issues.");
    }

    // ======================================
    // Update Inventory To HashMap
    // ======================================
    public static void updateInvToHashMap(Player player) {
        String playerUUID = player.getUniqueId().toString();
        if (DatabaseManager.playerInvs.containsKey(playerUUID)) {
            DatabaseManager.playerInvs.get(playerUUID).saveCurrentPage();
            DebugManager.debug("UPDATING INV. TO HASHMAP PLAYER (" + player.getName() + ")", "Completed with no issues.");
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
            DebugManager.debug("REMOVING INV. FROM HASHMAP PLAYER (" + player.getName() + ")", "Completed with no issues.");
        }
    }

    // ======================================
    // Save Crashed File
    // ======================================
    public static void saveCrashedFile() {
        try {
            crashedData.save(crashedFile);
            DebugManager.debug("SAVING CRASHED FILE", "Completed with no issues.");
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
            if (!crashedData.getConfigurationSection("crashed").getKeys(false).isEmpty()) {
                return true;
            }
        }
        return false;
    }

    // ======================================
    // Has Crashed
    // ======================================
    public static Boolean hasCrashed(Player player) {
        return crashedData.contains("crashed." + player.getUniqueId().toString());
    }

    // ======================================
    // Add Crashed Player
    // ======================================
    public static void addCrashedPlayer(Player player) {
        crashedData.set("crashed." + player.getUniqueId().toString(), true);
        saveCrashedFile();
        DebugManager.debug("ADDING CRASHED FILE PLAYER (" + player.getName() + ")", "Completed with no issues.");
    }

}
