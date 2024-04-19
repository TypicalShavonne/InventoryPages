package me.kevinnovak.inventorypages.storage;

import me.kevinnovak.inventorypages.InventoryPages;
import me.kevinnovak.inventorypages.enums.DatabaseType;
import org.bukkit.entity.Player;

import java.io.File;

public class PlayerInventoryDataStorage {

    private static PlayerInventoryStorage STORAGE;

    public static void init(DatabaseType databaseType) {
        File file = new File(InventoryPages.plugin.getDataFolder() + "/dulieu/");
        if (!file.exists()) {
            file.mkdirs();
        }

        if (databaseType == DatabaseType.YAML)
            PlayerInventoryDataStorage.STORAGE = new PlayerInventoryDataFileStorage();
    }

    public static PlayerInventoryData getPlayerInventoryData(Player player) {
        return PlayerInventoryDataStorage.STORAGE.getData(player);
    }

    public static void savePlayerInventoryData(PlayerInventoryData data) {
        PlayerInventoryDataStorage.STORAGE.saveData(data);
    }

}
