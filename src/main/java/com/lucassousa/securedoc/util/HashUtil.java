package com.lucassousa.securedoc.util;

import java.io.InputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class HashUtil {

    public static void main(String[] args) throws Exception {
        String resourcePath = "/arquivos/doc.txt";
        String hash = generateSHA512HashFromResource(resourcePath);
        System.out.println("SHA-512 Hash: " + hash);
    }


    public static String generateSHA512HashFromResource(String resourcePath) throws Exception {
        if (resourcePath == null || resourcePath.trim().isEmpty()) {
            throw new IllegalArgumentException("Resource path cannot be null or empty");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");

            InputStream inputStream = HashUtil.class.getResourceAsStream(resourcePath);
            if (inputStream == null) {
                throw new IOException("Resource not found: " + resourcePath);
            }

            try {
                byte[] buffer = new byte[8192];
                int bytesRead;

                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            } finally {
                inputStream.close();
            }

            byte[] hashBytes = digest.digest();
            return bytesToHex(hashBytes);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-512 algorithm not available", e);
        } catch (IOException e) {
            throw new RuntimeException("Error reading resource: " + resourcePath, e);
        }
    }


    public static String generateSHA512Hash(String filePath) throws Exception {
        if (filePath == null || filePath.trim().isEmpty()) {
            throw new IllegalArgumentException("File path cannot be null or empty");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");

            try (java.io.FileInputStream fis = new java.io.FileInputStream(filePath)) {
                byte[] buffer = new byte[8192];
                int bytesRead;

                while ((bytesRead = fis.read(buffer)) != -1) {
                    digest.update(buffer, 0, bytesRead);
                }
            }

            byte[] hashBytes = digest.digest();
            return bytesToHex(hashBytes);

        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-512 algorithm not available", e);
        } catch (IOException e) {
            throw new RuntimeException("Error reading file: " + filePath, e);
        }
    }


    private static String bytesToHex(byte[] bytes) {
        StringBuilder hexString = new StringBuilder();
        for (byte b : bytes) {
            hexString.append(String.format("%02x", b));
        }
        return hexString.toString();
    }


    public static String generateSHA512Hash(byte[] content) throws Exception {
        if (content == null) {
            throw new IllegalArgumentException("Content cannot be null");
        }

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hashBytes = digest.digest(content);
            return bytesToHex(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-512 algorithm not available", e);
        }
    }
}