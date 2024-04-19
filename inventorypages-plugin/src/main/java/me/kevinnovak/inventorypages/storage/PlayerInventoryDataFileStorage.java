package me.kevinnovak.inventorypages.storage;

import me.kevinnovak.inventorypages.InventoryPages;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class PlayerInventoryDataFileStorage implements PlayerInventoryStorage {

    private File getFile(String playerUUID) {
        File file = new File(InventoryPages.plugin.getDataFolder() + "/database/" + playerUUID.substring(0, 1) + "/" + playerUUID + ".yml");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    public PlayerInventoryData fromFile(File file, String playerUUID) {
        YamlConfiguration storage = YamlConfiguration.loadConfiguration(file);

        HashMap<Integer, ArrayList<ItemStack>> pageItemHashMap = new HashMap<>();

        PlayerInventoryData data = new PlayerInventoryData(pageItemHashMap, 1, 0);

        if (storage.getLong("maxPage") == 0) {
            return data;
        }

        return data;
    }

    @Override
    public void saveData(PlayerInventoryData data) {

    }

    @Override
    public PlayerInventoryData getData(Player player) {
        return null;
    }
}
