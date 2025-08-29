import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.security.MessageDigest;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class HashUtilTest {

    @Test
    public void testHashDocTxt() throws Exception {
        String filePath = "resources/arquivos/doc.txt";

        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        try (FileInputStream fis = new FileInputStream(filePath)) {
            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                digest.update(buffer, 0, bytesRead);
            }
        }

        byte[] hashBytes = digest.digest();
        StringBuilder hexString = new StringBuilder();
        for (byte b : hashBytes) {
            hexString.append(String.format("%02x", b));
        }

        String hashHex = hexString.toString();

        System.out.println("SHA-512 Hash: " + hashHex);

        assertEquals(128, hashHex.length());
    }
}
