  package me.kevinnovak.inventorypages;

import com.tchristofferson.configupdater.ConfigUpdater;
import me.kevinnovak.inventorypages.command.ClearCommand;
import me.kevinnovak.inventorypages.file.inventory.MessageFile;
import me.kevinnovak.inventorypages.file.inventory.PlayerInventoryFile;
import me.kevinnovak.inventorypages.inventory.PlayerPageInventory;
import me.kevinnovak.inventorypages.listener.*;
import me.kevinnovak.inventorypages.manager.DatabaseManager;
import me.kevinnovak.inventorypages.server.VersionSupport;
import me.kevinnovak.support.version.cross.CrossVersionSupport;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;

import java.io.File;
import java.io.IOException;

public final class InventoryPages extends JavaPlugin {
public static InventoryPages plugin;
    public static VersionSupport nms;

    @Override
    public void onLoad() {
        plugin = this;
        nms = new CrossVersionSupport(plugin);
    }

    @Override
    public void onEnable() {

        //
        initFiles();
        initInventories();
        initCommands();
        initListeners();
        //

        // load all online players into hashmap
        Bukkit.getServer().getLogger().info("[InventoryPages] Setting up inventories.");
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            try {
                DatabaseManager.loadInvFromFileIntoHashMap(player);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (getConfig().getBoolean("saving.enabled")) {
            Bukkit.getServer().getLogger().info("[InventoryPages] Setting up inventory saving.");
            startSaving();
        }

        Bukkit.getServer().getLogger().info("[InventoryPages] Plugin enabled!");
    }

    @Override
    public void onDisable() {
        for (Player player : Bukkit.getServer().getOnlinePlayers()) {
            String playerUUID = player.getUniqueId().toString();
            if (DatabaseManager.playerInvs.containsKey(playerUUID)) {
                // update inventories to hashmap and save to file
                DatabaseManager.updateInvToHashMap(player);
                DatabaseManager.saveInvFromHashMapToFile(player);
                DatabaseManager.clearAndRemoveCrashedPlayer(player);
            }
        }
        Bukkit.getServer().getLogger().info("[InventoryPages] Plugin disabled.");
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
    }

    public void initInventories() {
        PlayerPageInventory.setupItems();
    }

    public void initCommands() {
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

    public void startSaving() {
        BukkitScheduler scheduler = getServer().getScheduler();
        scheduler.scheduleSyncRepeatingTask(this, new Runnable() {
            @Override
            public void run() {
                DatabaseManager.updateAndSaveAllInventoriesToFiles();
            }
        }, 0L, 20L * getConfig().getInt("saving.interval"));
    }

}
