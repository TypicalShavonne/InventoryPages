package me.kevinnovak.inventorypages.storage;

import me.kevinnovak.inventorypages.CustomInventory;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;

public class PlayerInventoryData {

    private String playerName;
    private String playerUUID;
    private HashMap<Integer, ArrayList<ItemStack>> pageItemHashMap = new HashMap<>();
    private ArrayList<ItemStack> creativeItems = new ArrayList<>(27);
    private CustomInventory customInventory;
    private int maxPage;
    private int page;

    public PlayerInventoryData(String playerName, String playerUUID, HashMap<Integer, ArrayList<ItemStack>> pageItemHashMap, ArrayList<ItemStack> creativeItems, CustomInventory customInventory, int maxPage, int page) {
        this.playerName = playerName;
        this.playerUUID = playerUUID;
        this.pageItemHashMap = pageItemHashMap;
        this.creativeItems = creativeItems;
        this.customInventory = customInventory;
        this.maxPage = maxPage;
        this.page = page;
    }

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getPlayerUUID() {
        return playerUUID;
    }

    public void setPlayerUUID(String playerUUID) {
        this.playerUUID = playerUUID;
    }

    public HashMap<Integer, ArrayList<ItemStack>> getPageItemHashMap() {
        return pageItemHashMap;
    }

    public void setPageItemHashMap(int page, ArrayList<ItemStack> items) {
        getPageItemHashMap().put(page, items);
    }

    public void setPageItemHashMap(HashMap<Integer, ArrayList<ItemStack>> pageItemHashMap) {
        this.pageItemHashMap = pageItemHashMap;
    }

    public ArrayList<ItemStack> getCreativeItems() {
        return creativeItems;
    }

    public void setCreativeItems(ArrayList<ItemStack> items) {
        this.creativeItems = items;
    }

    public CustomInventory getCustomInventory() {
        return customInventory;
    }

    public void setCustomInventory(CustomInventory customInventory) {
        this.customInventory = customInventory;
    }

    public int getMaxPage() {
        return maxPage;
    }

    public void setMaxPage(int number) {
        if (customInventory != null)
            customInventory.setMaxPage(number);
        this.maxPage = number;
    }

    public void addMaxPage(int number) {
        if (number < 0)
            return;

        if (customInventory != null)
            customInventory.addMaxPage(number);
        this.maxPage = this.maxPage + number;
    }

    public void removeMaxPage(int number) {
        if (this.maxPage - number < 0)
            this.maxPage = 0;

        if (customInventory != null)
            customInventory.removeMaxPage(number);
        this.maxPage = this.maxPage + number;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int number) {
        this.page = number;
    }

}
