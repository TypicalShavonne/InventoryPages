package me.kevinnovak.inventorypages.storage;

import me.kevinnovak.inventorypages.file.MessageFile;
import me.kevinnovak.inventorypages.util.MessageUtil;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.AbstractMap;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PlayerInventoryData {
    private Player player;
    private String playerName;
    private String playerUUID;
    private ItemStack prevItem, nextItem, noPageItem;
    private Integer page = 0, maxPage = 1, prevPos, nextPos;
    private Boolean hasUsedCreative = false;
    private HashMap<Integer, ArrayList<ItemStack>> items = new HashMap<>();
    private ArrayList<ItemStack> creativeItems = new ArrayList<>(27);

    // ======================================
    // Constructor
    // ======================================
    PlayerInventoryData(Player player, String playerName, String playerUUID, int maxPage, HashMap<Integer, ArrayList<ItemStack>> items, ArrayList<ItemStack> creativeItems, ItemStack prevItem, Integer prevPos, ItemStack nextItem, Integer nextPos, ItemStack noPageItem) {
        this.player = player;
        this.playerName = playerName;
        this.playerUUID = playerUUID;
        this.maxPage = maxPage;
        this.prevItem = prevItem;
        this.prevPos = prevPos;
        this.nextItem = nextItem;
        this.nextPos = nextPos;
        this.noPageItem = noPageItem;

        // create pages
        for (int i = 0; i < maxPage + 1; i++) {
            if (!pageExists(i)) {
                createPage(i);
            }
        }

        // initialize creative inventory
        for (int i = 0; i < 27; i++) {
            this.creativeItems.add(null);
        }

        if (items != null)
            this.setItems(items);

        if (creativeItems != null)
            this.setCreativeItems(creativeItems);

        GameMode gm = player.getGameMode();

        boolean droppedItem = false;
        for (int i = 0; i < 27; i++) {
            ItemStack item = player.getInventory().getItem(i + 9);
            if (item != null) {
                if (this.storeOrDropItem(item, gm)) {
                    droppedItem = true;
                }
            }
        }
        if (droppedItem)
            MessageUtil.sendMessage(player, MessageFile.get().getString("messages.items-dropped"));
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

    public int getMaxPage() {
        return this.maxPage;
    }

    public void setMaxPage(int number) {
        if (number < 0)
            number = 0;

        this.maxPage = number;
        saveCurrentPage();
        showPage(player.getGameMode());
    }

    public void addMaxPage(int number) {
        if (number < 0)
            number = 0;

        this.maxPage = this.maxPage + number;
        saveCurrentPage();
        showPage(player.getGameMode());
    }

    public void removeMaxPage(int number) {
        if (this.maxPage - number < 0) {
            number = this.maxPage;
        }

        this.maxPage = this.maxPage - number;
        saveCurrentPage();
        showPage(player.getGameMode());
    }

    // ======================================
    // Save Current Page
    // ======================================
    public void saveCurrentPage() {
        if (player.getGameMode() != GameMode.CREATIVE) {
            ArrayList<ItemStack> pageItems = new ArrayList<ItemStack>(25);
            for (int i = 0; i < 27; i++) {
                if (i != prevPos && i != nextPos) {
                    pageItems.add(this.player.getInventory().getItem(i + 9));
                }
            }
            this.items.put(this.page, pageItems);
        } else {
            for (int i = 0; i < 27; i++) {
                creativeItems.set(i, this.player.getInventory().getItem(i + 9));
            }
        }
    }

    // ======================================
    // Clear Page
    // ======================================
    public void clearPage(GameMode gm) {
        clearPage(this.page, gm);
    }

    void clearPage(int page, GameMode gm) {
        if (gm != GameMode.CREATIVE) {
            ArrayList<ItemStack> pageItems = new ArrayList<ItemStack>(25);
            for (int i = 0; i < 25; i++) {
                pageItems.add(null);
            }
            this.items.put(page, pageItems);
        } else {
            for (int i = 0; i < 27; i++) {
                creativeItems.set(i, null);
            }
        }
    }

    // ======================================
    // Clear All Pages
    // ======================================
    public void clearAllPages(GameMode gm) {
        if (gm != GameMode.CREATIVE) {
            for (int i = 0; i < this.maxPage + 1; i++) {
                clearPage(i, gm);
            }
        } else {
            clearPage(gm);
        }
    }

    // ======================================
    // Drop Page
    // ======================================
    public void dropPage(GameMode gm) {
        dropPage(this.page, gm);
    }

    void dropPage(int page, GameMode gm) {
        if (gm != GameMode.CREATIVE) {
            for (int i = 0; i < 25; i++) {
                ItemStack item = this.items.get(page).get(i);
                if (item != null) {
                    this.player.getWorld().dropItemNaturally(this.player.getLocation(), item);
                    this.items.get(page).set(i, null);
                }
            }
        } else {
            for (int i = 0; i < 27; i++) {
                ItemStack item = this.creativeItems.get(i);
                if (item != null) {
                    this.player.getWorld().dropItemNaturally(this.player.getLocation(), item);
                    this.creativeItems.set(i, null);
                }
            }
        }
    }

    // ======================================
    // Drop All Pages
    // ======================================
    public void dropAllPages(GameMode gm) {
        if (gm != GameMode.CREATIVE) {
            for (int i = 0; i < this.maxPage + 1; i++) {
                dropPage(i, gm);
            }
        } else {
            dropPage(gm);
        }
    }

    // ======================================
    // Show Page
    // ======================================
    void showPage() {
        this.showPage(this.page);
    }

    void showPage(Integer page) {
        showPage(page, GameMode.SURVIVAL);
    }

    public void showPage(GameMode gm) {
        showPage(this.page, gm);
    }

    void showPage(Integer page, GameMode gm) {

        if (!pageExists(page))
            createPage(page);

        if (page > maxPage) {
            this.page = maxPage;
        } else {
            this.page = page;
        }
        //player.sendMessage("GameMode: " + gm);
        if (gm != GameMode.CREATIVE) {
            boolean foundPrev = false;
            boolean foundNext = false;
            for (int i = 0; i < 27; i++) {
                int j = i;
                if (i == prevPos) {
                    if (this.page == 0) {
                        this.player.getInventory().setItem(i + 9, addPageNums(noPageItem));
                    } else {
                        this.player.getInventory().setItem(i + 9, addPageNums(prevItem));
                    }
                    foundPrev = true;
                } else if (i == nextPos) {
                    if (this.page == maxPage) {
                        this.player.getInventory().setItem(i + 9, addPageNums(noPageItem));
                    } else {
                        this.player.getInventory().setItem(i + 9, addPageNums(nextItem));
                    }
                    foundNext = true;
                } else {
                    if (foundPrev) {
                        j--;
                    }
                    if (foundNext) {
                        j--;
                    }
                    this.player.getInventory().setItem(i + 9, this.items.get(this.page).get(j));
                }
            }
            //player.sendMessage("Showing Page: " + this.page);
        } else {
            this.hasUsedCreative = true;
            for (int i = 0; i < 27; i++) {
                this.player.getInventory().setItem(i + 9, this.creativeItems.get(i));
            }
        }
    }

    // ======================================
    // Add Page Numbers
    // ======================================
    ItemStack addPageNums(ItemStack item) {
        ItemStack modItem = new ItemStack(item);
        ItemMeta itemMeta = modItem.getItemMeta();
        List<String> itemLore = itemMeta.getLore();
        for (int j = 0; j < itemLore.size(); j++) {
            Integer currentPageUser = page + 1;
            Integer maxPageUser = maxPage + 1;
            itemLore.set(j, itemLore.get(j).replace("{CURRENT}", currentPageUser.toString()).replace("{MAX}", maxPageUser.toString()));
        }
        itemMeta.setLore(itemLore);
        modItem.setItemMeta(itemMeta);
        return modItem;
    }

    // ======================================
    // Previous Page
    // ======================================
    public void prevPage() {
        if (this.page > 0) {
            this.saveCurrentPage();
            this.page = this.page - 1;
            this.showPage();
            this.saveCurrentPage();
        }
    }

    // ======================================
    // Next Page
    // ======================================
    public void nextPage() {
        if (this.page < maxPage) {
            this.saveCurrentPage();
            this.page = this.page + 1;
            this.showPage();
            this.saveCurrentPage();
        }
    }

    // ======================================
    // Page Exists
    // ======================================
    Boolean pageExists(Integer page) {
        return items.containsKey(page);
    }

    // ======================================
    // Create Page
    // ======================================
    void createPage(Integer page) {
        ArrayList<ItemStack> pageItems = new ArrayList<ItemStack>(25);
        for (int i = 0; i < 25; i++) {
            pageItems.add(null);
        }
        this.items.put(page, pageItems);
    }

    // ======================================
    // Get/Set Items
    // ======================================
    HashMap<Integer, ArrayList<ItemStack>> getItems() {
        return this.items;
    }

    void setItems(HashMap<Integer, ArrayList<ItemStack>> items) {
        this.items = items;
    }

    // ======================================
    // Get/Set Creative Items
    // ======================================
    ArrayList<ItemStack> getCreativeItems() {
        return this.creativeItems;
    }

    void setCreativeItems(ArrayList<ItemStack> creativeItems) {
        this.creativeItems = creativeItems;
    }

    // ======================================
    // Get/Set Current Page
    // ======================================
    Integer getPage() {
        return this.page;
    }

    void setPage(Integer page) {
        this.page = page;
    }

    // ======================================
    // Get/Set Has Used Creative Boolean
    // ======================================
    Boolean hasUsedCreative() {
        return this.hasUsedCreative;
    }

    void setUsedCreative(Boolean hasUsedCreative) {
        this.hasUsedCreative = hasUsedCreative;
    }

    // ======================================
    // Next Free Space
    // ======================================
    SimpleEntry<Integer, Integer> nextFreeSpace() {
        for (Integer i = 0; i < maxPage + 1; i++) {
            for (Integer j = 0; j < 25; j++) {
                if (items.get(i).get(j) == null) {
                    SimpleEntry<Integer, Integer> pageAndPos = new AbstractMap.SimpleEntry<Integer, Integer>(i, j);
                    return pageAndPos;
                }
            }
        }
        return null;
    }

    // ======================================
    // Next Creative Free Space
    // ======================================
    int nextCreativeFreeSpace() {
        for (Integer i = 0; i < 27; i++) {
            if (creativeItems.get(i) == null) {
                return i;
            }
        }
        return -1;
    }

    // ======================================
    // Store/Drop Item
    // ======================================
    // returns true if dropped
    Boolean storeOrDropItem(ItemStack item, GameMode gm) {
        if (gm != GameMode.CREATIVE) {
            SimpleEntry<Integer, Integer> nextFreeSpace = nextFreeSpace();
            if (nextFreeSpace != null) {
                this.items.get(nextFreeSpace.getKey()).set(nextFreeSpace.getValue(), item);
                return false;
            } else {
                this.player.getWorld().dropItem(player.getLocation(), item);
                return true;
            }
        } else {
            int nextFreeSpace = nextCreativeFreeSpace();
            if (nextFreeSpace != -1) {
                this.creativeItems.set(nextFreeSpace, item);
                return false;
            } else {
                this.player.getWorld().dropItem(player.getLocation(), item);
                return true;
            }
        }

    }
}
