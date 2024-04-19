package me.kevinnovak.inventorypages.command;

import me.kevinnovak.inventorypages.InventoryPages;
import me.kevinnovak.inventorypages.manager.DatabaseManager;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClearCommand implements CommandExecutor {
    public ClearCommand() {
        InventoryPages.plugin.getCommand("clear").setExecutor(this);
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (player.hasPermission("inventorypages.clear")) {
                player.sendMessage("no permission");
                return false;
            }

            String playerUUID = player.getUniqueId().toString();
            GameMode gm = player.getGameMode();

            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("all")) {
                    DatabaseManager.playerInvs.get(playerUUID).clearAllPages(gm);
                    player.sendMessage("clear all");
                }
            } else {
                DatabaseManager.playerInvs.get(playerUUID).clearPage(gm);
                player.sendMessage("clear");
            }
            clearHotbar(player);
            DatabaseManager.playerInvs.get(playerUUID).showPage(gm);
        }

        return false;
    }

    public void clearHotbar(Player player) {
        for (int i = 0; i < 9; i++) {
            player.getInventory().setItem(i, null);
        }
    }
}