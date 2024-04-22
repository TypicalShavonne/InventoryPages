package me.kevinnovak.inventorypages.storage;

import me.kevinnovak.inventorypages.file.MessageFile;
import me.kevinnovak.inventorypages.manager.DebugManager;
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
    private Integer page = 0, maxPage = 1, prevItemPos, nextItemPos;
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
        this.prevItemPos = prevPos;
        this.nextItem = nextItem;
        this.nextItemPos = nextPos;
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
        DebugManager.debug("(database) MAX PAGE", playerName + "'s max page now is " + number + ".");
    }

    public void addMaxPage(int number) {
        if (number < 0)
            number = 0;

        this.maxPage = this.maxPage + number;
        saveCurrentPage();
        showPage(player.getGameMode());
        DebugManager.debug("(database) MAX PAGE", playerName + " has been added " + number + " more pages.");
    }

    public void removeMaxPage(int number) {
        if (this.maxPage - number < 0) {
            number = this.maxPage;
        }

        this.maxPage = this.maxPage - number;
        saveCurrentPage();
        showPage(player.getGameMode());
        DebugManager.debug("(database) MAX PAGE", playerName + " has been removed " + number + " pages.");
    }

    public int getNextItemPos() {
        return this.nextItemPos;
    }

    public void setNextItemPos(int number) {
        if (number < 0)
            number = 0;

        if (number > 26)
            number = 26;

        if (number == prevItemPos) {
            if (number == 0)
                number = 1;
            else if (number == 26)
                number = 25;
        }

        this.nextItemPos = number;
    }

    public int getPrevItemPos() {
        return this.prevItemPos;
    }

    public void setPrevItemPos(int number) {
        if (number < 0)
            number = 0;

        if (number > 26)
            number = 26;

        if (number == nextItemPos) {
            if (number == 0)
                number = 1;
            else if (number == 26)
                number = 25;
        }

        this.prevItemPos = number;
    }

    // ======================================
    // Save Current Page
    // ======================================
    public void saveCurrentPage() {
        if (player.getGameMode() != GameMode.CREATIVE) {
            ArrayList<ItemStack> pageItems = new ArrayList<ItemStack>(25);
            for (int i = 0; i < 27; i++) {
                if (i != prevItemPos && i != nextItemPos) {
                    pageItems.add(this.player.getInventory().getItem(i + 9));
                }
            }
            this.items.put(this.page, pageItems);
            //DebugManager.debug("SAVING CURRENT PAGE", "Saved current page (survival items) of " + playerName);
        } else {
            for (int i = 0; i < 27; i++) {
                creativeItems.set(i, this.player.getInventory().getItem(i + 9));
                //DebugManager.debug("SAVING CURRENT PAGE", "Saved current page (creative items) of " + playerName);
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
                ItemStack item = this.getItems(page).get(i);
                if (item != null) {
                    this.player.getWorld().dropItemNaturally(this.player.getLocation(), item);
                    this.getItems(page).set(i, null);
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
            for (int slotNumber = 0; slotNumber < 27; slotNumber++) {
                int slotNumberClone = slotNumber;
                if (slotNumber == prevItemPos) {
                    if (this.page == 0) {
                        this.player.getInventory().setItem(slotNumber + 9, addPageNums(noPageItem));
                    } else {
                        this.player.getInventory().setItem(slotNumber + 9, addPageNums(prevItem));
                    }
                    foundPrev = true;
                } else if (slotNumber == nextItemPos) {
                    if (this.page == maxPage) {
                        this.player.getInventory().setItem(slotNumber + 9, addPageNums(noPageItem));
                    } else {
                        this.player.getInventory().setItem(slotNumber + 9, addPageNums(nextItem));
                    }
                    foundNext = true;
                } else {
                    if (foundPrev) {
                        slotNumberClone--;
                    }
                    if (foundNext) {
                        slotNumberClone--;
                    }
                    this.player.getInventory().setItem(slotNumber + 9, this.getItems(this.page).get(slotNumberClone));
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

    ArrayList<ItemStack> getItems(int page) {

        if (!pageExists(page)) {
            createPage(page);
        }

        return items.get(page);
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
    public Integer getPage() {
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
        for (Integer page = 0; page < maxPage + 1; page++) {
            for (Integer slotNumber = 0; slotNumber < 25; slotNumber++) {
                if (getItems(page).get(slotNumber) == null) {
                    SimpleEntry<Integer, Integer> pageAndPos = new AbstractMap.SimpleEntry<Integer, Integer>(page, slotNumber);
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
                this.getItems(nextFreeSpace.getKey()).set(nextFreeSpace.getValue(), item);
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
