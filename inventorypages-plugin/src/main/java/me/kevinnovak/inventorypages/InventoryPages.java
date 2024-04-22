  package me.kevinnovak.inventorypages;

import com.tchristofferson.configupdater.ConfigUpdater;
import me.kevinnovak.inventorypages.command.ClearCommand;
import me.kevinnovak.inventorypages.command.InventoryPagesCommand;
import me.kevinnovak.inventorypages.enums.DatabaseType;
import me.kevinnovak.inventorypages.file.MessageFile;
import me.kevinnovak.inventorypages.file.inventory.PlayerInventoryFile;
import me.kevinnovak.inventorypages.inventory.PlayerPageInventory;
import me.kevinnovak.inventorypages.listener.*;
import me.kevinnovak.inventorypages.manager.AutoSaveManager;
import me.kevinnovak.inventorypages.manager.DatabaseManager;
import me.kevinnovak.inventorypages.manager.DebugManager;
import me.kevinnovak.inventorypages.server.VersionSupport;
import me.kevinnovak.inventorypages.storage.PlayerInventoryDataStorage;
import me.kevinnovak.inventorypages.util.MessageUtil;
import me.kevinnovak.support.version.cross.CrossVersionSupport;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public final class InventoryPages extends JavaPlugin {
    public static InventoryPages plugin;
    public static VersionSupport nms;
    public static DatabaseType databaseType;

    @Override
    public void onLoad() {
        plugin = this;
        nms = new CrossVersionSupport(plugin);
    }

    @Override
    public void onEnable() {

        //
        initFiles();
        DebugManager.setDebug(getConfig().getBoolean("debug.enabled"));
        initDatabase();
        initInventories();
        initCommands();
        initListeners();

        AutoSaveManager.startAutoSave(getConfig().getInt("auto-saving.interval"));
        //

        // load all online players into hashmap
        Bukkit.getServer().getLogger().info("[InventoryPages] Setting up inventories.");
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            try {
                DatabaseManager.loadPlayerInventory(player);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public void initDatabase() {
        databaseType = DatabaseType.valueOf(getConfig().getString("database.type").toUpperCase());
        try {
            PlayerInventoryDataStorage.init(databaseType);
        } catch (Exception exception) {
            PlayerInventoryDataStorage.init(DatabaseType.YAML);
            MessageUtil.log("&c--------------------------------------");
            MessageUtil.log("&eDatabase type &c&l" + getConfig().getString("database.type") + "&e không hợp lệ!");
            MessageUtil.log("&eVui lòng kiểm tra lại type trong config.yml");
            MessageUtil.log("&eDatabase sẽ được load mặc định theo type &b&lYAML");
            MessageUtil.log("&c--------------------------------------");
        }
    }

    public void initFiles() {
        File inventoryFolder = new File(getDataFolder() + "/inventories");
        if (!inventoryFolder.exists())
            inventoryFolder.mkdirs();

        // config.yml
        saveDefaultConfig();
        File configFile = new File(getDataFolder(), "config.yml");
        try {
            ConfigUpdater.update(this, "config.yml", configFile, "bang-hoi-war.cong-diem.mobs");
        } catch (IOException e) {
            e.printStackTrace();
        }
        reloadConfig();
        DebugManager.debug("LOADING FILE", "Loaded config.yml");

        // message.yml
        String messageFileName = "message.yml";
        MessageFile.setup();
        MessageFile.saveDefault();
        File messageFile = new File(getDataFolder(), "message.yml");
        try {
            ConfigUpdater.update(this, messageFileName, messageFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        MessageFile.reload();
        DebugManager.debug("LOADING FILE", "Loaded message.yml");

        // inventories/playerinventory.yml
        String inventoryFileName = "playerinventory.yml";
        PlayerInventoryFile.setup();
        PlayerInventoryFile.saveDefault();
        File inventoryFile = new File(InventoryPages.plugin.getDataFolder() + "/inventories/playerinventory.yml");
        try {
            ConfigUpdater.update(this, inventoryFileName, inventoryFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        PlayerInventoryFile.reload();
        DebugManager.debug("LOADING FILE", "Loaded playerinventory.yml");
    }

    public void initInventories() {
        PlayerPageInventory.setupItems();
    }

    public void initCommands() {
        new InventoryPagesCommand();
        new ClearCommand();
    }

    public void initListeners() {
        new InventoryClickListener();
        new PlayerDeathListener();
        new PlayerGameModeChangeListener();
        new PlayerJoinListener();
        new PlayerQuitListener();
        new PlayerRespawnListener();
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            String playerUUID = player.getUniqueId().toString();
            if (DatabaseManager.playerInvs.containsKey(playerUUID)) {
                // update inventories to hashmap and save to file
                DatabaseManager.updateInvToHashMap(player);
                DatabaseManager.savePlayerInventory(player);
                DatabaseManager.clearAndRemoveCrashedPlayer(player);

                for (int i = 9; i < 36; i++)
                    player.getInventory().setItem(i, null);
            }
        }
        Bukkit.getServer().getLogger().info("[InventoryPages] Plugin disabled.");
    }

}
