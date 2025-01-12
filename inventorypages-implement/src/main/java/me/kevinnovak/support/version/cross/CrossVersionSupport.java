package me.kevinnovak.support.version.cross;

import com.cryptomorin.xseries.SkullUtils;
import com.cryptomorin.xseries.XMaterial;
import com.cryptomorin.xseries.XSound;
import io.github.bananapuncher714.nbteditor.NBTEditor;
import me.kevinnovak.inventorypages.server.VersionSupport;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.bukkit.ChatColor.COLOR_CHAR;

public class CrossVersionSupport extends VersionSupport {
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)&[0-9A-FK-OR]");
    private static final String NBT_KEY = "InventoryPages";

    public CrossVersionSupport(Plugin plugin) {
        super(plugin);
    }

    @Override
    public ItemStack getItemStack(ItemStack itemStack) {
        if (itemStack == null)
            return null;

        ItemStack xItemStack = XMaterial.matchXMaterial(itemStack).parseItem();
        xItemStack.setAmount(itemStack.getAmount());
        xItemStack.setItemMeta(itemStack.getItemMeta());
        return xItemStack;
    }

    @Override
    public ItemStack createItemStack(String material, int amount, short data) {
        return XMaterial.matchXMaterial(material + ":" + data)
                .map(XMaterial::parseItem)
                .map(item -> {
                    item.setAmount(amount);
                    return item;
                })
                .orElseGet(() -> {
                    getPlugin().getLogger().severe("----------------------------------------------------");
                    getPlugin().getLogger().severe("MATERIAL " + material + " KHÔNG HỢP LỆ!");
                    getPlugin().getLogger().severe("Có thể do bạn nhập sai hoặc Material đó không tồn tại ở phiên bản này");
                    getPlugin().getLogger().severe(">> Link Materials <<");
                    getPlugin().getLogger().severe("https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Material.html");
                    getPlugin().getLogger().severe("----------------------------------------------------");
                    return new ItemStack(Material.BEDROCK);
                });
    }

    @Override
    public Sound createSound(String soundName) {
        return XSound.matchXSound(soundName).map(XSound::parseSound).orElseGet(() -> {
            getPlugin().getLogger().severe("----------------------------------------------------");
            getPlugin().getLogger().severe("SOUND NAME " + soundName + " KHÔNG HỢP LỆ!");
            getPlugin().getLogger().severe("Có thể do bạn nhập sai hoặc Sound đó không tồn tại ở phiên bản này");
            getPlugin().getLogger().severe(">> Link Sounds <<");
            getPlugin().getLogger().severe("https://hub.spigotmc.org/javadocs/spigot/org/bukkit/Sound.html");
            getPlugin().getLogger().severe("----------------------------------------------------");
            return XSound.BLOCK_AMETHYST_CLUSTER_BREAK.parseSound();
        });
    }

    @Override
    public ItemStack getHeadItem(String headValue) {
        ItemStack item = XMaterial.PLAYER_HEAD.parseItem();
        assert item != null;
        item.setItemMeta(SkullUtils.applySkin(item.getItemMeta(), headValue));
        return item;
    }

    @Override
    public ItemStack addCustomData(ItemStack i, String data) {
        return NBTEditor.set(i, data, NBT_KEY);
    }

    @Override
    public String getCustomData(ItemStack i) {
        if (NBTEditor.contains(i, NBT_KEY)) {
            return NBTEditor.getString(i, NBT_KEY);
        }
        return "";
    }

    @Override
    public String addColor(String textToTranslate) {
        if (textToTranslate == null)
            return "NULL";

        Matcher matcher = HEX_PATTERN.matcher(textToTranslate);
        StringBuilder buffer = new StringBuilder(textToTranslate.length() + 4 * 8);
        while (matcher.find()) {
            String group = matcher.group(1);
            matcher.appendReplacement(buffer, COLOR_CHAR + "x"
                    + COLOR_CHAR + group.charAt(0) + COLOR_CHAR + group.charAt(1)
                    + COLOR_CHAR + group.charAt(2) + COLOR_CHAR + group.charAt(3)
                    + COLOR_CHAR + group.charAt(4) + COLOR_CHAR + group.charAt(5)
            );
        }
        String hexTranslated = matcher.appendTail(buffer).toString();

        return ChatColor.translateAlternateColorCodes('&', hexTranslated);
    }

    @Override
    public String stripColor(String textToStrip) {
        return textToStrip == null ? null : STRIP_COLOR_PATTERN.matcher(textToStrip).replaceAll("");
    }

}
