package me.kevinnovak.inventorypages.util;

import me.kevinnovak.inventorypages.InventoryPages;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class ItemUtil {

    public static ItemStack getItem(String type, String value, short itemData, String name, List<String> lore) {
        AtomicReference<ItemStack> material = new AtomicReference<>(new ItemStack(Material.BEDROCK));

        if (type.equalsIgnoreCase("customhead") || type.equalsIgnoreCase("playerhead"))
            material.set(InventoryPages.nms.getHeadItem(value));

        if (type.equalsIgnoreCase("material"))
            material.set(InventoryPages.nms.createItemStack(value, 1, itemData));

        ItemMeta materialMeta = material.get().getItemMeta();

        materialMeta.setDisplayName(InventoryPages.nms.addColor(name));

        List<String> newList = new ArrayList<>();

        for (String string : lore)
            newList.add(InventoryPages.nms.addColor(string));
        materialMeta.setLore(newList);

        material.get().setItemMeta(materialMeta);
        return material.get();
    }

}
