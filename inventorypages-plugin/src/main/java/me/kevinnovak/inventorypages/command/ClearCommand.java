package me.kevinnovak.inventorypages.command;

import me.kevinnovak.inventorypages.InventoryPages;
import me.kevinnovak.inventorypages.file.MessageFile;
import me.kevinnovak.inventorypages.manager.DatabaseManager;
import me.kevinnovak.inventorypages.manager.DebugManager;
import me.kevinnovak.inventorypages.util.MessageUtil;
import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ClearCommand implements CommandExecutor {
    public ClearCommand() {
        InventoryPages.plugin.getCommand("clear").setExecutor(this);
        DebugManager.debug("LOADING COMMAND", "Loaded ClearCommand");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (sender instanceof Player) {
            Player player = (Player) sender;

            if (!player.hasPermission("inventorypages.clear")) {
                MessageUtil.sendMessage(player, MessageFile.get().getString("messages.no-permission"), true);
                return false;
            }

            String playerUUID = player.getUniqueId().toString();
            GameMode gm = player.getGameMode();

            if (args.length == 1) {
                if (args[0].equalsIgnoreCase("all")) {
                    DatabaseManager.playerInvs.get(playerUUID).clearAllPages(gm);
                    MessageUtil.sendMessage(player, MessageFile.get().getString("messages.clear-all"));
                }
            } else {
                DatabaseManager.playerInvs.get(playerUUID).clearPage(gm);
                MessageUtil.sendMessage(player, MessageFile.get().getString("messages.clear"));
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
