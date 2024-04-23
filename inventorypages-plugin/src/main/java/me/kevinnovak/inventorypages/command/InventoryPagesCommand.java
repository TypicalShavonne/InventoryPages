package me.kevinnovak.inventorypages.command;

import me.kevinnovak.inventorypages.InventoryPages;
import me.kevinnovak.inventorypages.file.MessageFile;
import me.kevinnovak.inventorypages.file.inventory.PlayerInventoryFile;
import me.kevinnovak.inventorypages.manager.AutoSaveManager;
import me.kevinnovak.inventorypages.manager.BackupManager;
import me.kevinnovak.inventorypages.manager.DatabaseManager;
import me.kevinnovak.inventorypages.manager.DebugManager;
import me.kevinnovak.inventorypages.util.MessageUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class InventoryPagesCommand implements CommandExecutor, TabExecutor {
    public InventoryPagesCommand() {
        InventoryPages.plugin.getCommand("inventorypagesrecoded").setExecutor(this);
        DebugManager.debug("LOADING COMMAND", "Loaded InventoryPageRecoded.");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player) {
            if (!sender.hasPermission("inventorypagesrecoded.admin")) {
                MessageUtil.sendMessage(sender, MessageFile.get().getString("messages.no-permission"));
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
                MessageUtil.sendMessage(sender, MessageFile.get().getString("messages.commands.inventorypagesrecoded.reload"));
                return false;
            }
            if (args[0].equalsIgnoreCase("backup")) {
                MessageUtil.sendMessage(sender, MessageFile.get().getString("messages.commands.inventorypagesrecoded.backup.start"));
                Bukkit.getScheduler().runTaskAsynchronously(InventoryPages.plugin, () -> {
                    BackupManager backupManager = new BackupManager();
                    backupManager.backupAll();
                    MessageUtil.sendMessage(sender, MessageFile.get().getString("messages.commands.inventorypagesrecoded.backup.success").replace("%number%", String.valueOf(backupManager.getTotalDatabase())));
                });
                return false;
            }
        }

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("setmaxpage") || args[0].equalsIgnoreCase("addmaxpage") || args[0].equalsIgnoreCase("removemaxpage")) {
                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    MessageUtil.sendMessage(sender, MessageFile.get().getString("messages.invalid-name").replace("%player%", args[1]));
                    return false;
                }

                int maxPage;
                try {
                    maxPage = Integer.parseInt(args[2]);
                } catch (Exception exception) {
                    MessageUtil.sendMessage(sender, MessageFile.get().getString("messages.invalid-number"));
                    return false;
                }

                if (args[0].equalsIgnoreCase("setmaxpage")) {
                    DatabaseManager.playerInvs.get(target.getUniqueId().toString()).setMaxPage(maxPage);
                    MessageUtil.sendMessage(sender, MessageFile.get().getString("messages.commands.inventorypagesrecoded.set-max-page")
                            .replace("%player%", args[1])
                            .replace("%number%", args[2]));
                    return false;
                }

                if (args[0].equalsIgnoreCase("addmaxpage")) {
                    DatabaseManager.playerInvs.get(target.getUniqueId().toString()).addMaxPage(maxPage);
                    MessageUtil.sendMessage(sender, MessageFile.get().getString("messages.commands.inventorypagesrecoded.add-max-page")
                            .replace("%player%", args[1])
                            .replace("%number%", args[2]));
                    return false;
                }

                if (args[0].equalsIgnoreCase("removemaxpage")) {
                    DatabaseManager.playerInvs.get(target.getUniqueId().toString()).removeMaxPage(maxPage);
                    MessageUtil.sendMessage(sender, MessageFile.get().getString("messages.commands.inventorypagesrecoded.remove-max-page")
                            .replace("%player%", args[1])
                            .replace("%number%", args[2]));
                    return false;
                }
            }
        }

        for (String message : MessageFile.get().getStringList("messages.commands.inventorypagesrecoded.messages")) {
            message = message.replace("%version%", InventoryPages.plugin.getDescription().getVersion());
            MessageUtil.sendMessage(sender, message);
        }

        return false;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        List<String> completions = new ArrayList<>();
        List<String> commands = new ArrayList<>();

        if (sender.hasPermission("inventorypagesrecoded.admin")) {
            if (args.length == 1) {
                commands.add(0, "reload");
                commands.add(1, "setmaxpage");
                commands.add(2, "addmaxpage");
                commands.add(3, "removemaxpage");
                commands.add(4, "backup");
                StringUtil.copyPartialMatches(args[0], commands, completions);
            } else if (args.length == 2) {
                if (args[0].equalsIgnoreCase("setmaxpage") || args[0].equalsIgnoreCase("addmaxpage") || args[0].equalsIgnoreCase("removemaxpage")) {
                    if (!Bukkit.getOnlinePlayers().isEmpty())
                        for (Player player : Bukkit.getOnlinePlayers()) {
                            commands.add(player.getName());
                        }
                }
                StringUtil.copyPartialMatches(args[1], commands, completions);
            }
        }
        Collections.sort(completions);
        return completions;
    }
}
