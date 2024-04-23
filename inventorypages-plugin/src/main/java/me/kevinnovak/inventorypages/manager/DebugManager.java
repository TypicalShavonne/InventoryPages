package me.kevinnovak.inventorypages.manager;

import me.kevinnovak.inventorypages.InventoryPages;
import org.bukkit.Bukkit;

public class DebugManager {

    public static boolean debug;
    private static String debugPrefix;

    public static boolean getDebug() {
        return debug;
    }

    public static void setDebug(boolean b) {
        debug = b;
        debugPrefix = InventoryPages.plugin.getConfig().getString("debug.prefix");
    }

    public static void debug(String prefix, String message) {
        if (!debug)
            return;

        Bukkit.getConsoleSender().sendMessage(InventoryPages.nms.addColor(debugPrefix + prefix.toUpperCase() + " >>> " + message));
    }


}
