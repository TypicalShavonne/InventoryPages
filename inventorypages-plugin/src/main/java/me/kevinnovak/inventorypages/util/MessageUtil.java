package me.kevinnovak.inventorypages.util;

import me.kevinnovak.inventorypages.InventoryPages;
import me.kevinnovak.inventorypages.file.inventory.MessageFile;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageUtil {

    public static void throwErrorMessage(String message) {
        Bukkit.getLogger().severe(message);
        log("&4&l[INVENTORY PAGES ERROR] &c&lNếu lỗi này ảnh hưởng đến trải nghiệm của người chơi, hãy liên hệ mình qua discord: Cortez_Romeo#1290");
    }

    public static void sendBroadCast(String message) {

        if (message.equals(""))
            return;

        for (Player p : Bukkit.getOnlinePlayers()) {
            sendMessage(p, message);
        }
    }

    public static void log(String message) {
        Bukkit.getConsoleSender().sendMessage(InventoryPages.nms.addColor(message));
    }

    public static void sendMessage(CommandSender sender, String message) {
        sender.sendMessage(InventoryPages.nms.addColor(message));
    }

    public static void sendMessage(Player player, String message) {

        if (player == null | message.equals(""))
            return;

        message = MessageFile.get().getString("PREFIX") + message;

/*        if (!InventoryPages.PAPISupport())
            player.sendMessage(InventoryPages.nms.addColor(message));
        else
            player.sendMessage(InventoryPages.nms.addColor(PlaceholderAPI.setPlaceholders(player, message)));*/
        player.sendMessage(InventoryPages.nms.addColor(message));
    }

}
