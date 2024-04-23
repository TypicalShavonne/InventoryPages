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
import me.kevinnovak.inventorypages.support.PAPISupport;
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
    private String version = Bukkit.getServer().getClass().getName().split("\\.")[3];
    private static boolean papiSupport = false;

    @Override
    public void onLoad() {
        plugin = this;
        nms = new CrossVersionSupport(plugin);
    }

    @Override
    public void onEnable() {
        initFiles();
        DebugManager.setDebug(getConfig().getBoolean("debug.enabled"));
        initDatabase();
        initInventories();
        initCommands();
        initListeners();
        initSupports();

        AutoSaveManager.startAutoSave(getConfig().getInt("auto-saving.interval"));

        MessageUtil.log("&f--------------------------------");
        MessageUtil.log("&2 _                                          ");
        MessageUtil.log("&2(_)_ ____   __  _ __   __ _  __ _  ___  ___ ");
        MessageUtil.log("&2| | '_ \\ \\ / / | '_ \\ / _` |/ _` |/ _ \\/ __|");
        MessageUtil.log("&2| | | | \\ V /  | |_) | (_| | (_| |  __/\\__ \\");
        MessageUtil.log("&2|_|_| |_|\\_(_) | .__/ \\__,_|\\__, |\\___||___/");
        MessageUtil.log("&2               |_|          |___/           ");
        MessageUtil.log("&2                        _          _        ");
        MessageUtil.log("&2 _ __ ___  ___ ___   __| | ___  __| |       ");
        MessageUtil.log("&2| '__/ _ \\/ __/ _ \\ / _` |/ _ \\/ _` |       ");
        MessageUtil.log("&2| | |  __/ (_| (_) | (_| |  __/ (_| |       ");
        MessageUtil.log("&2|_|  \\___|\\___\\___/ \\__,_|\\___|\\__,_|       ");
        MessageUtil.log("");
        MessageUtil.log("&fVersion: &b" + getDescription().getVersion());
        MessageUtil.log("&fAuthor: &bKevinNovak, Cortez_Romeo");
        MessageUtil.log("&eKhởi chạy plugin trên phiên bản: " + version);
        MessageUtil.log("");
        MessageUtil.log("&fSupport:");
        MessageUtil.log((papiSupport ? "&2[YES] &aPlaceholderAPI" : "&4[NO] &cPlaceholderAPI"));
        MessageUtil.log("");
        MessageUtil.log("&f--------------------------------");

        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            try {
                DatabaseManager.loadPlayerInventory(player);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (Metrics.isEnabled())
            new Metrics(this, 21649);
    }

    public void initDatabase() {
        try {
            databaseType = DatabaseType.valueOf(getConfig().getString("database.type").toUpperCase());
            PlayerInventoryDataStorage.init(databaseType);
        } catch (IllegalArgumentException exception) {
            MessageUtil.log("&c--------------------------------------");
            MessageUtil.log("&eDatabase type &c&l" + getConfig().getString("database.type") + "&e không hợp lệ!");
            MessageUtil.log("&eVui lòng kiểm tra lại type trong config.yml");
            MessageUtil.log("&eDatabase sẽ được load mặc định theo type &b&lYAML");
            MessageUtil.log("&c--------------------------------------");
            PlayerInventoryDataStorage.init(DatabaseType.YAML);
        }
    }

    public void initFiles() {
        File inventoryFolder = new File(getDataFolder() + "/inventories");
        if (!inventoryFolder.exists())
            inventoryFolder.mkdirs();

        File backupFolder = new File(getDataFolder() + "/backup");
        if (!backupFolder.exists())
            backupFolder.mkdirs();

        // config.yml
        saveDefaultConfig();
        File configFile = new File(getDataFolder(), "config.yml");
        try {
            ConfigUpdater.update(this, "config.yml", configFile, "bang-hoi-war.cong-diem.mobs");
        } catch (IOException e) {
            e.printStackTrace();
        }
        reloadConfig();
        DebugManager.debug("LOADING FILE", "Loaded config.yml.");

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
        DebugManager.debug("LOADING FILE", "Loaded message.yml.");

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
        DebugManager.debug("LOADING FILE", "Loaded playerinventory.yml.");
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
        new EntityPickupListener();
        new PlayerRespawnListener();
    }

    public void initSupports() {
        // papi
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new PAPISupport().register();
            papiSupport = true;
        }
    }

    public static boolean isPapiSupport() {
        return papiSupport;
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
            }
        }
        MessageUtil.log("&f--------------------------------");
        MessageUtil.log("&4 _                                          ");
        MessageUtil.log("&4(_)_ ____   __  _ __   __ _  __ _  ___  ___ ");
        MessageUtil.log("&4| | '_ \\ \\ / / | '_ \\ / _` |/ _` |/ _ \\/ __|");
        MessageUtil.log("&4| | | | \\ V /  | |_) | (_| | (_| |  __/\\__ \\");
        MessageUtil.log("&4|_|_| |_|\\_(_) | .__/ \\__,_|\\__, |\\___||___/");
        MessageUtil.log("&4               |_|          |___/           ");
        MessageUtil.log("&4                        _          _        ");
        MessageUtil.log("&4 _ __ ___  ___ ___   __| | ___  __| |       ");
        MessageUtil.log("&4| '__/ _ \\/ __/ _ \\ / _` |/ _ \\/ _` |       ");
        MessageUtil.log("&4| | |  __/ (_| (_) | (_| |  __/ (_| |       ");
        MessageUtil.log("&4|_|  \\___|\\___\\___/ \\__,_|\\___|\\__,_|       ");
        MessageUtil.log("");
        MessageUtil.log("&fVersion: &b" + getDescription().getVersion());
        MessageUtil.log("&fAuthor: &bKevinNovak, Cortez_Romeo");
        MessageUtil.log("");
        MessageUtil.log("&f--------------------------------");    }
}
