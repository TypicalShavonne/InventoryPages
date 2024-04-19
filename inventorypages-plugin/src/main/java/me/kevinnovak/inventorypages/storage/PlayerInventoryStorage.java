package me.kevinnovak.inventorypages.storage;

import org.bukkit.entity.Player;

public interface PlayerInventoryStorage {
    void saveData(PlayerInventoryData data);
    PlayerInventoryData getData(Player player);
}
