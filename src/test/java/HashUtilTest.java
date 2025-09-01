import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.security.MessageDigest;

import static org.junit.jupiter.api.Assertions.*;


public class HashUtilTest {

    @TempDir
    File tempDir;

    private File testFile;
    private final String testContent = "This is a test document for hashing.";

    @BeforeEach
    void setUp() throws IOException {
        testFile = new File(tempDir, "test_doc.txt");
        try (FileWriter writer = new FileWriter(testFile)) {
            writer.write(testContent);
        }
    }

    @Test
    void testSHA512HashGeneration() throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-512");
        byte[] expectedHash = digest.digest(testContent.getBytes());

        StringBuilder expectedHex = new StringBuilder();
        for (byte b : expectedHash) {
            expectedHex.append(String.format("%02x", b));
        }

        String actualHash = com.lucassousa.securedoc.util.HashUtil.generateSHA512Hash(testFile.getAbsolutePath());

        assertNotNull(actualHash);
        assertEquals(expectedHex.toString().toLowerCase(), actualHash.toLowerCase());
        assertEquals(128, actualHash.length()); // SHA-512 produces 64 bytes = 128 hex characters
    }

    @Test
    void testHashConsistency() throws Exception {
        String hash1 = com.lucassousa.securedoc.util.HashUtil.generateSHA512Hash(testFile.getAbsolutePath());
        String hash2 = com.lucassousa.securedoc.util.HashUtil.generateSHA512Hash(testFile.getAbsolutePath());

        assertEquals(hash1, hash2);
    }

    @Test
    void testNonExistentFile() {
        assertThrows(Exception.class, () -> {
            com.lucassousa.securedoc.util.HashUtil.generateSHA512Hash("non_existent_file.txt");
        });
    }

    @Test
    void testEmptyFile() throws Exception {
        File emptyFile = new File(tempDir, "empty.txt");
        emptyFile.createNewFile();

        String hash = com.lucassousa.securedoc.util.HashUtil.generateSHA512Hash(emptyFile.getAbsolutePath());
        assertNotNull(hash);
        assertEquals(128, hash.length());

        String expectedEmptyHash = "cf83e1357eefb8bdf1542850d66d8007d620e4050b5715dc83f4a921d36ce9ce47d0d13c5d85f2b0ff8318d2877eec2f63b931bd47417a81a538327af927da3e";
        assertEquals(expectedEmptyHash, hash.toLowerCase());
    }
}
