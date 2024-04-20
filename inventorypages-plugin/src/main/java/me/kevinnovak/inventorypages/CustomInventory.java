package me.kevinnovak.inventorypages;

import me.kevinnovak.inventorypages.file.MessageFile;
import me.kevinnovak.inventorypages.util.MessageUtil;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CustomInventory {
    public InventoryPages plugin;
    private Player player;
    private ItemStack prevItem, nextItem, noPageItem;
    private int page = 0, maxPage = InventoryPages.plugin.getConfig().getInt("inventory-settings.max-page-default"), prevPos, nextPos;
    private Boolean hasUsedCreative = false;
    private HashMap<Integer, ArrayList<ItemStack>> items = new HashMap<>();
    ;
    private ArrayList<ItemStack> creativeItems = new ArrayList<>(27);

    // ======================================
    // Constructor
    // ======================================
    public CustomInventory(InventoryPages plugin, Player player, HashMap<Integer, ArrayList<ItemStack>> pageItemHashMap, ArrayList<ItemStack> creativeItems, int maxPage, ItemStack prevItem, Integer prevPos, ItemStack nextItem, Integer nextPos, ItemStack noPageItem) {
        if (maxPage < 0)
            maxPage = 0;

        this.plugin = plugin;
        this.player = player;
        this.maxPage = maxPage;
        this.prevItem = prevItem;
        this.prevPos = prevPos;
        this.nextItem = nextItem;
        this.nextPos = nextPos;
        this.noPageItem = noPageItem;

        // initialize creative inventory if it's empty
        if (creativeItems.isEmpty())
            for (int i = 0; i < 27; i++) {
                creativeItems.add(null);
            }

        this.setItems(pageItemHashMap);
        this.setCreativeItems(creativeItems);

        // create pages
        for (int i = 0; i < maxPage + 1; i++) {
            if (!pageExists(i)) {
                createPage(i);
            }
        }

        GameMode gm = player.getGameMode();
        
        boolean droppedItem = false;
        for (int i = 0; i < 27; i++) {
            ItemStack item = InventoryPages.nms.getItemStack(player.getInventory().getItem(i + 9));
            if (item != null) {
                if (this.storeOrDropItem(item, gm)) {
                    droppedItem = true;
                }
            }
        }
        if (droppedItem)
            MessageUtil.sendMessage(player, MessageFile.get().getString("messages.items-dropped"));

        //player.sendMessage("Your max pages are: " + (maxPage + 1));
    }

    public int getMaxPage() {
        return this.maxPage;
    }

    public void setMaxPage(int number) {
        if (number < 0) {
            this.maxPage = 0;
            return;
        }

        this.maxPage = number;
        showPage(player.getGameMode());
    }

    public void addMaxPage(int number) {
        if (number < 0)
            return;
        this.maxPage = this.maxPage + number;
        showPage(player.getGameMode());
    }

    public void removeMaxPage(int number) {
        if (this.maxPage - number < 0) {
            this.maxPage = 0;
            return;
        }
        this.maxPage = this.maxPage - number;
        showPage(player.getGameMode());
    }

    public void saveCurrentPage() {
        if (player.getGameMode() != GameMode.CREATIVE) {
            ArrayList<ItemStack> pageItems = new ArrayList<ItemStack>(25);
            for (int i = 0; i < 27; i++) {
                if (i != prevPos && i != nextPos) {
                    pageItems.add(InventoryPages.nms.getItemStack(this.player.getInventory().getItem(i + 9)));
                }
            }
            this.items.put(this.page, pageItems);
        } else {
            for (int i = 0; i < 27; i++) {
                creativeItems.set(i, InventoryPages.nms.getItemStack(this.player.getInventory().getItem(i + 9)));
            }
        }
    }

    public void clearPage(GameMode gm) {
        clearPage(this.page, gm);
    }

    void clearPage(int page, GameMode gm) {
        if (gm != GameMode.CREATIVE) {
            ArrayList<ItemStack> pageItems = new ArrayList<>(25);
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

    public void clearAllPages(GameMode gm) {
        if (gm != GameMode.CREATIVE) {
            for (int i = 0; i < this.maxPage + 1; i++) {
                clearPage(i, gm);
            }
        } else {
            clearPage(gm);
        }
    }

    public void dropPage(GameMode gm) {
        dropPage(this.page, gm);
    }

    public void dropPage(int page, GameMode gm) {
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
    public void showPage() {
        this.showPage(this.page);
    }

    public void showPage(Integer page) {
        showPage(page, GameMode.SURVIVAL);
    }

    public void showPage(GameMode gm) {
        showPage(this.page, gm);
    }

    void showPage(Integer page, GameMode gameMode) {
        if (page > maxPage) {
            this.page = maxPage;
        } else {
            this.page = page;
        }
        //player.sendMessage("GameMode: " + gm);
        if (gameMode != GameMode.CREATIVE) {
            Boolean foundPrev = false;
            Boolean foundNext = false;
            for (int slotNumber = 0; slotNumber < 27; slotNumber++) {
                int slotNumberClone = slotNumber;
                // slot number là vị trí của nút "trở về", tiến hành set nút "trang trước"
                if (slotNumber == prevPos) {
                    if (this.page == 0) {
                        this.player.getInventory().setItem(slotNumber + 9, addPageNumber(noPageItem));
                    } else {
                        this.player.getInventory().setItem(slotNumber + 9, addPageNumber(prevItem));
                    }
                    foundPrev = true;

                } else if (slotNumber == nextPos) { // lần này slot number là vị trí của nút "trang sau"
                    if (this.page == maxPage) {
                        this.player.getInventory().setItem(slotNumber + 9, addPageNumber(noPageItem));
                    } else {
                        this.player.getInventory().setItem(slotNumber + 9, addPageNumber(nextItem));
                    }
                    foundNext = true;
                } else {
                    if (foundPrev) {
                        slotNumberClone--;
                    }
                    if (foundNext) {
                        slotNumberClone--;
                    }
                    // check một lần nữa cho make sure
                    if (!pageExists(this.page))
                        createPage(this.page);
                    this.player.getInventory().setItem(slotNumber + 9, InventoryPages.nms.getItemStack(this.items.get(this.page).get(slotNumberClone)));
                }
            }
            //player.sendMessage("Showing Page: " + this.page);
        } else { // đối với chế độ sáng tạo
            this.hasUsedCreative = true;
            for (int i = 0; i < 27; i++) {
                this.player.getInventory().setItem(i + 9, InventoryPages.nms.getItemStack(this.creativeItems.get(i)));
            }
        }
    }

    // ======================================
    // Add Page Numbers
    // ======================================
    ItemStack addPageNumber(ItemStack item) {
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
        if (items.containsKey(page)) {
            return true;
        }
        return false;
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
    public HashMap<Integer, ArrayList<ItemStack>> getItems() {
        return this.items;
    }

    void setItems(HashMap<Integer, ArrayList<ItemStack>> items) {
        for (Integer page : items.keySet()) {
            if (items.get(page) == null || items.get(page).isEmpty()) {
                for (int i = 0; i <= 25; i++)
                    items.put(0, null);
            }
        }
        this.items = items;
    }

    // ======================================
    // Get/Set Creative Items
    // ======================================
    public ArrayList<ItemStack> getCreativeItems() {
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
    public Boolean hasUsedCreative() {
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
                    SimpleEntry<Integer, Integer> pageAndPos = new SimpleEntry<Integer, Integer>(i, j);
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