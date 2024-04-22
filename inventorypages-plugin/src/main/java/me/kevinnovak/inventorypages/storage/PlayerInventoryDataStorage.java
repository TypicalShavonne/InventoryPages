package me.kevinnovak.inventorypages.storage;

import me.kevinnovak.inventorypages.InventoryPages;
import me.kevinnovak.inventorypages.enums.DatabaseType;
import me.kevinnovak.inventorypages.manager.DebugManager;
import me.kevinnovak.inventorypages.util.MessageUtil;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import java.io.File;

public class PlayerInventoryDataStorage {

    private static PlayerInventoryStorage STORAGE;

    public static void init(DatabaseType databaseType) {
        File file = new File(InventoryPages.plugin.getDataFolder() + "/database/");
        if (!file.exists()) {
            file.mkdirs();
        }

        if (databaseType == DatabaseType.YAML) {
            STORAGE = new PlayerInventoryDataYAMLStorage();
        } else if (databaseType == DatabaseType.MYSQL) {
            FileConfiguration config = InventoryPages.plugin.getConfig();
            String host = config.getString("database.mysql.database.host");
            String port = config.getString("database.mysql.database.port");
            String name = config.getString("database.mysql.database.name");
            String user = config.getString("database.mysql.database.user");
            String password = config.getString("database.mysql.database.password");
            try {
                STORAGE = new PlayerInventoryDataMySQLStorage(host, port, name, user, password);
            } catch (Exception exception) {
                exception.printStackTrace();
                STORAGE = new PlayerInventoryDataYAMLStorage();
                InventoryPages.databaseType = DatabaseType.YAML;
                MessageUtil.log("&c--------------------------------------");
                MessageUtil.log("&eKhông thể kết nối tới dữ liệu MySQL");
                MessageUtil.log("&eVui lòng đọc dòng lỗi trên để xem chi tiết");
                MessageUtil.log("&eDatabase sẽ được load mặc định theo type &b&lYAML");
                MessageUtil.log("&c--------------------------------------");
            }
        }
        DebugManager.debug("LOADING DATABASE", "Loaded " + databaseType.toString() + " database.");
    }

    public static PlayerInventoryData getPlayerInventoryData(Player player) {
        return PlayerInventoryDataStorage.STORAGE.getData(player);
    }

    public static void savePlayerInventoryData(PlayerInventoryData data) {
        PlayerInventoryDataStorage.STORAGE.saveData(data);
    }

}
