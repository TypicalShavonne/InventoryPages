package me.kevinnovak.inventorypages.storage;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import me.kevinnovak.inventorypages.InventoryPages;
import me.kevinnovak.inventorypages.inventory.PlayerPageInventory;
import me.kevinnovak.inventorypages.manager.DebugManager;
import me.kevinnovak.inventorypages.util.StringUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;

public class PlayerInventoryDataMySQLStorage implements PlayerInventoryStorage {
    private static Connection connection;
    private static String table;

    public PlayerInventoryDataMySQLStorage(String host, String port, String name, String user, String password) throws SQLException, ClassNotFoundException {
        table = InventoryPages.plugin.getConfig().getString("database.mysql.database.table");
        Class.forName("com.mysql.jdbc.Driver");
        final String url = "jdbc:mysql://" + host + ":" + port + "/" + name + "?autoReconnect=true";
        connection = DriverManager.getConnection(url, user, password);
        createTable();
    }

    private static void createTable() {

        if (ifTableExist(table)) {
            DebugManager.debug("LOADING DATABASE", "Connected to table " + table + ".");
        } else {
            try {
                Statement stmt = connection.createStatement();
                String sql = "CREATE TABLE " + table + " " +
                        "(UUID varchar(50) not NULL, " +
                        " PLAYERNAME VARCHAR(50), " +
                        " ITEMS TEXT, " +
                        " CREATIVEITEMS TEXT, " +
                        " MAXPAGE VARCHAR(50), " +
                        " PAGE VARCHAR(50), " +
                        " PREVITEMPOS VARCHAR(50), " +
                        " NEXTITEMPOS VARCHAR(50), " +
                        " PRIMARY KEY (UUID))";

                stmt.executeUpdate(sql);
                DebugManager.debug("LOADING DATABASE", "Created and connected to table " + table + ".");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static boolean ifTableExist(String name) {
        try {
            DatabaseMetaData dbm = connection.getMetaData();
            ResultSet tables = dbm.getTables(null, null, name, null);
            if (tables.next()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public PlayerInventoryData fromMySQL(String playerName, String playerUUID) {

        HashMap<Integer, ArrayList<ItemStack>> pageItemHashMap = new HashMap<>();
        int maxPageDefault = InventoryPages.plugin.getConfig().getInt("inventory-settings.max-page-default");
        if (maxPageDefault < 0)
            maxPageDefault = 0;
        PlayerInventoryData data = new PlayerInventoryData(Bukkit.getPlayer(playerName), playerName, playerUUID, maxPageDefault,null, null, PlayerPageInventory.prevItem, PlayerPageInventory.prevPos, PlayerPageInventory.nextItem, PlayerPageInventory.nextPos, PlayerPageInventory.noPageItem);

        if (!hasData(playerUUID))
            return data;

        String query = "select * from " + table + " where UUID=?";
        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, playerUUID);
            ResultSet resultSet = ps.executeQuery();

            while (resultSet.next()) {
                int maxPage = resultSet.getInt("MAXPAGE");
                int page = resultSet.getInt("PAGE");
                String creativeItemsString = resultSet.getString("CREATIVEITEMS");

                data.setMaxPage(maxPage);
                if (InventoryPages.plugin.getConfig().getBoolean("inventory-settings.use-saved-current-page"))
                    data.setPage(page);

                // load survival items
                Gson gson = new Gson();
                HashMap<Integer, ArrayList<String>> pageItemsBase64 = gson.fromJson(resultSet.getString("ITEMS"), new TypeToken<HashMap<Integer, ArrayList<String>>>(){}.getType());
                for (int pages = 0; pages < maxPage + 1; pages++) {
                    ArrayList<ItemStack> pageItems = new ArrayList<>(25);
                    for (int slotNumber = 0; slotNumber < 25; slotNumber++) {
                        pageItems.add(StringUtil.stacksFromBase64(pageItemsBase64.get(pages).get(slotNumber))[0]);
                    }
                    pageItemHashMap.put(pages, pageItems);
                }
                data.setItems(pageItemHashMap);

                // load creative items
                if (creativeItemsString != null) {
                    ArrayList<String> creativeItemsBase64 = gson.fromJson(creativeItemsString, new TypeToken<ArrayList<String>>(){}.getType());
                    ArrayList<ItemStack> creativeItemsItemStack = new ArrayList<>();
                    for (String base64Item : creativeItemsBase64)
                        creativeItemsItemStack.add(StringUtil.stacksFromBase64(base64Item)[0]);
                    data.setCreativeItems(creativeItemsItemStack);
                }

                if (!InventoryPages.plugin.getConfig().getBoolean("inventory-settings.focus-using-default-item-position")) {
                    data.setPrevItemPos(resultSet.getInt("PREVITEMPOS"));
                    data.setNextItemPos(resultSet.getInt("NEXTITEMPOS"));
                }
            }
            data.setPlayerName(playerName);
            data.setPlayerUUID(playerUUID);

        }
        catch (Exception exception) {
            exception.printStackTrace();
        }
        return data;
    }

    private static boolean hasData(String playerUUID) {
        String query = "select * from " + table + " where UUID=?";
        try (PreparedStatement ps = connection.prepareStatement(query)){
            ps.setString(1, playerUUID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return true;
            }
            rs.close();
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return false;
    }

    private static void initData(String playerUUID) {
        ArrayList<String> queries = new ArrayList<>();
        queries.add("INSERT INTO " + table + " (UUID, PLAYERNAME, ITEMS, CREATIVEITEMS, MAXPAGE, PAGE, PREVITEMPOS, NEXTITEMPOS) values('" + playerUUID + "', '', '', '', '', '', '', '')");
        queries.forEach(cmd -> {
            try (PreparedStatement ps = connection.prepareStatement(cmd)) {
                ps.execute();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    @Override
    public PlayerInventoryData getData(Player player) {
        return fromMySQL(player.getName(), player.getUniqueId().toString());
    }

    @Override
    public void saveData(PlayerInventoryData playerInventoryData) {
        if (!hasData(playerInventoryData.getPlayerUUID()))
            initData(playerInventoryData.getPlayerUUID());

        String query = "UPDATE " + table + " "
                + "SET PLAYERNAME = ?,"
                + " ITEMS = ?,"
                + " CREATIVEITEMS = ?,"
                + " MAXPAGE = ?,"
                + " PAGE = ?,"
                + " PREVITEMPOS = ?,"
                + " NEXTITEMPOS = ?"
                + " WHERE UUID = ?";

        try (PreparedStatement ps = connection.prepareStatement(query)) {
            ps.setString(1, playerInventoryData.getPlayerName());

            HashMap<Integer, ArrayList<String>> pageItemsBase64 = new HashMap<>();
            for (int page : playerInventoryData.getItems().keySet()) {
                ArrayList<String> itemsBase64 = new ArrayList<>();
                for (ItemStack itemStack : playerInventoryData.getItems().get(page)) {
                    itemsBase64.add(StringUtil.toBase64(itemStack));
                }
                pageItemsBase64.put(page, itemsBase64);
            }

            // Sử dụng thư viện của google gson để chuyển chuỗi HashMap sang json
            Gson gson = new Gson();
            ps.setString(2, gson.toJson(pageItemsBase64));

            if (playerInventoryData.hasUsedCreative()) {
                ArrayList<String> creativeItemsBase64 = new ArrayList<>();
                for (ItemStack itemStack : playerInventoryData.getCreativeItems())
                    creativeItemsBase64.add(StringUtil.toBase64(itemStack));
                ps.setString(3, gson.toJson(creativeItemsBase64));
            }
            else
                ps.setString(3, null);

            ps.setInt(4, playerInventoryData.getMaxPage());
            ps.setInt(5, playerInventoryData.getPage());
            ps.setInt(6, playerInventoryData.getPrevItemPos());
            ps.setInt(7, playerInventoryData.getNextItemPos());
            ps.setString(8, playerInventoryData.getPlayerUUID());


            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
