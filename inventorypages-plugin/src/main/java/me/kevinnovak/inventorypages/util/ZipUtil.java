package me.kevinnovak.inventorypages.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ZipUtil {

    private final List<String> fileList;

    public ZipUtil() {
        fileList = new ArrayList<>();
    }

    public void zipIt(String zipFile, String sourceFolder) {
        byte[] buffer = new byte[1024];
        String source = new File(sourceFolder).getName();
        FileOutputStream fos = null;
        ZipOutputStream zos = null;
        try {
            fos = new FileOutputStream(zipFile);
            zos = new ZipOutputStream(fos);
            FileInputStream in = null;
            for (String file: this.fileList) {
                ZipEntry ze = new ZipEntry(source + File.separator + file);
                zos.putNextEntry(ze);
                try {
                    in = new FileInputStream(sourceFolder + File.separator + file);
                    int len;
                    while ((len = in.read(buffer)) > 0) {
                        zos.write(buffer, 0, len);
                    }
                } finally {
                    in.close();
                }
            }
            zos.closeEntry();
        } catch (IOException ex) {
            ex.printStackTrace();
        } finally {
            try {
                zos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public List<String> getFileList() {
        return fileList;
    }

    public void generateFileList(File node, String sourceFolder) {
        if (node.isFile()) {
            fileList.add(generateZipEntry(node.toString(), sourceFolder));
        }

        if (node.isDirectory()) {
            String[] subNote = node.list();
            for (String filename: subNote) {
                generateFileList(new File(node, filename), sourceFolder);
            }
        }
    }

    private String generateZipEntry(String file, String sourceFolder) {
        return file.substring(sourceFolder.length() + 1, file.length());
    }
}
