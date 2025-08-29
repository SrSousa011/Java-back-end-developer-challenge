package org.lucassousa.securedoc.util;

import java.io.FileInputStream;
import java.security.MessageDigest;

public class HashUtil {
    public static void main(String[] args) throws Exception {
        String filePath = "resources/arquivos/doc.txt";
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        FileInputStream fis = new FileInputStream(filePath);
        byte[] buffer = new byte[8192];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
            digest.update(buffer, 0, bytesRead);
        }
        fis.close();
        byte[] hashBytes = digest.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));
        }
        System.out.println("SHA-512 Hash: " + hexString.toString());
    }
}
