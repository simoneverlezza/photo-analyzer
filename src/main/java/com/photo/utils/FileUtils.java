package com.photo.utils;

import java.io.File;
import java.io.FileInputStream;
import java.security.MessageDigest;

public class FileUtils {

    public static String bytesToMB(long bytes) {
        double mb = (double) bytes / (1024 * 1024);
        return String.format("%.2f", mb);
    }

    public static String calculateChecksum(File file) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");

        try (FileInputStream fis = new FileInputStream(file)) {
            byte[] byteArray = new byte[1024];
            int bytesCount;
            while ((bytesCount = fis.read(byteArray)) != -1) {
                digest.update(byteArray, 0, bytesCount);
            }
        }

        byte[] bytes = digest.digest();
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }
}
