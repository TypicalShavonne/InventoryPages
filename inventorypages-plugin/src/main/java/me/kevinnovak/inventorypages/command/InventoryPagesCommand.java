package me.kevinnovak.inventorypages.command;

import me.kevinnovak.inventorypages.InventoryPages;
import me.kevinnovak.inventorypages.file.MessageFile;
import me.kevinnovak.inventorypages.file.inventory.PlayerInventoryFile;
import me.kevinnovak.inventorypages.manager.AutoSaveManager;
import me.kevinnovak.inventorypages.manager.DebugManager;
import me.kevinnovak.inventorypages.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InventoryPagesCommand implements CommandExecutor {
    public InventoryPagesCommand() {
        InventoryPages.plugin.getCommand("clear").setExecutor(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {
            if (!sender.hasPermission("inventorypages.admin")) {
                MessageUtil.sendMessage(sender, MessageFile.get().getString("messages.no-permission"), true);
                return false;
            }
        }

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                InventoryPages.plugin.reloadConfig();
                MessageFile.reload();
                PlayerInventoryFile.reload();
                DebugManager.setDebug(InventoryPages.plugin.getConfig().getBoolean("debug.enabled"));
                if (AutoSaveManager.getAutoSaveStatus() && !InventoryPages.plugin.getConfig().getBoolean("auto-saving.enabled")) {
                    AutoSaveManager.stopAutoSave();
                } else {
                    AutoSaveManager.startAutoSave(InventoryPages.plugin.getConfig().getInt("auto-saving.interval"));
                }
                AutoSaveManager.reloadTimeAutoSave();
                DebugManager.debug("RELOADING PLUGIN", "Reloaded plugin");
                MessageUtil.sendMessage(sender, MessageFile.get().getString("messages.reload"), true);
            }
        }

        return false;
    }
}
