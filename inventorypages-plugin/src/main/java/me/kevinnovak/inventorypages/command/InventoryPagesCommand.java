package me.kevinnovak.inventorypages.command;

import me.kevinnovak.inventorypages.InventoryPages;
import me.kevinnovak.inventorypages.file.inventory.MessageFile;
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
                MessageUtil.sendMessage(sender, MessageFile.get().getString("messages.no-permission"));
                return false;
            }
        }

        return false;
    }
}
