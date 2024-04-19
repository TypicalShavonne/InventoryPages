package me.kevinnovak.inventorypages.storage;

import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.HashMap;

public class PlayerInventoryData {

    private HashMap<Integer, ArrayList<ItemStack>> pageItemHashMap = new HashMap<>();
    private long maxPage;
    private long page;

    public PlayerInventoryData(HashMap<Integer, ArrayList<ItemStack>> pageItemHashMap, long maxPage, long page) {
        this.pageItemHashMap = pageItemHashMap;
        this.maxPage = maxPage;
        this.page = page;
    }

    public HashMap<Integer, ArrayList<ItemStack>> getPageItemHashMap() {
        return pageItemHashMap;
    }

    public void setPageItemHashMap(int page, ArrayList<ItemStack> items) {
        getPageItemHashMap().put(page, items);
    }

    public long getMaxPage() {
        return maxPage;
    }

    public void setMaxPage(long number) {
        this.maxPage = number;
    }

    public void addMaxPage(long number) {
        this.maxPage = this.maxPage + number;
    }

    public long getPage() {
        return page;
    }

    public void setPage(long number) {
        this.page = number;
    }

    public void addPage(long number) {
        this.page = this.page + number;
    }

}
