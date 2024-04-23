package me.kevinnovak.inventorypages.manager;

import me.kevinnovak.inventorypages.InventoryPages;
import me.kevinnovak.inventorypages.util.ZipUtil;

import java.io.File;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Date;

public class BackupManager {

    private int totalDatabase;

    public BackupManager() {}

    public int getTotalDatabase() {
        return totalDatabase;
    }

    public void backupAll() {
        DebugManager.debug("BACKUP", "Start creating backup files (for all database).");

        // Save trước khi backup dữ liệu
        DatabaseManager.updateAndSaveAllInventoriesToDatabase();

        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH-mm-ss MM-dd-yyyy");
        Date date = new Date();
        String OUTPUT_ZIP_FILE =  InventoryPages.plugin.getDataFolder() + "\\backup\\" + simpleDateFormat.format(date) + ".zip";
        String SOURCE_FOLDER = InventoryPages.plugin.getDataFolder() + "\\database";
        ZipUtil zipUtil = new ZipUtil();

        zipUtil.generateFileList(new File(SOURCE_FOLDER), SOURCE_FOLDER);
        zipUtil.zipIt(OUTPUT_ZIP_FILE, SOURCE_FOLDER);
        totalDatabase = zipUtil.getFileList().size();

        DebugManager.debug("BACKUP", "Created backup files (for all database).");
    }

}
