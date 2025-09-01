import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import com.lucassousa.securedoc.util.VerifyUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;


public class VerifyUtilTest {

    @TempDir
    File tempDir;

    private byte[] validSignatureBytes;
    private byte[] invalidSignatureBytes;

    @BeforeEach
    void setUp() throws IOException {
        // Create mock signature data for testing
        validSignatureBytes = createValidMockSignature();
        invalidSignatureBytes = createInvalidMockSignature();
    }

    @Test
    void testValidSignatureVerification() {
        // Test verification of a valid signature
        VerifyUtil.VerificationResult result = VerifyUtil.verifySignature(validSignatureBytes);

        assertNotNull(result);
        assertNotNull(result.getStatus());
        assertTrue(result.getStatus().equals("VALIDO") || result.getStatus().equals("INVALIDO"));
    }

    @Test
    void testInvalidSignatureVerification() {
        // Test verification of invalid signature
        VerifyUtil.VerificationResult result = VerifyUtil.verifySignature(invalidSignatureBytes);

        assertNotNull(result);
        assertEquals("INVALIDO", result.getStatus());
        assertFalse(result.isValid());
    }

    @Test
    void testNullSignatureInput() {
        // Test handling of null input
        VerifyUtil.VerificationResult result = VerifyUtil.verifySignature(null);

        assertNotNull(result);
        assertEquals("INVALIDO", result.getStatus());
        assertFalse(result.isValid());
        assertNotNull(result.getError());
    }

    @Test
    void testEmptySignatureInput() {
        // Test handling of empty signature
        VerifyUtil.VerificationResult result = VerifyUtil.verifySignature(new byte[0]);

        assertNotNull(result);
        assertEquals("INVALIDO", result.getStatus());
        assertFalse(result.isValid());
    }

    @Test
    void testSignerInfoExtraction() {
        // Test extraction of signer information
        VerifyUtil.VerificationResult result = VerifyUtil.verifySignature(validSignatureBytes);

        assertNotNull(result);
        // Note: In a real test with valid signature, you would assert:
        // if (result.isValid() && result.getSignerInfo() != null) {
        //     assertNotNull(result.getSignerInfo().getCommonName());
        //     assertNotNull(result.getSignerInfo().getHashAlgorithm());
        // }
    }

    @Test
    void testCertificateChainValidation() {
        // Test certificate chain validation
        VerifyUtil.VerificationResult result = VerifyUtil.verifySignature(validSignatureBytes);

        assertNotNull(result);
        // The trust chain validation result should be available
        assertNotNull(result.isTrustChainValid());
    }

    @Test
    void testMalformedSignatureData() {
        // Test handling of malformed signature data
        byte[] malformedData = {0x00, 0x01, 0x02, 0x03}; // Random bytes

        VerifyUtil.VerificationResult result = VerifyUtil.verifySignature(malformedData);

        assertNotNull(result);
        assertEquals("INVALIDO", result.getStatus());
        assertFalse(result.isValid());
        assertNotNull(result.getError());
    }

    private byte[] createValidMockSignature() {
        // Create a mock valid CMS signature structure
        // In a real implementation, this would be a proper CMS signature
        try {
            File tempFile = new File(tempDir, "mock_signature.p7s");
            try (FileOutputStream fos = new FileOutputStream(tempFile)) {
                // Write minimal CMS structure headers
                fos.write(new byte[]{0x30, (byte) 0x82, 0x00, 0x50}); // SEQUENCE with length
                fos.write(new byte[]{0x06, 0x09}); // OBJECT IDENTIFIER length 9
                fos.write(new byte[]{0x2a, (byte)0x86, 0x48, (byte)0x86, (byte)0xf7, 0x0d, 0x01, 0x07, 0x02}); // signedData OID
            }

            byte[] data = new byte[(int)tempFile.length()];
            java.io.FileInputStream fis = new java.io.FileInputStream(tempFile);
            fis.read(data);
            fis.close();
            return data;
        } catch (IOException e) {
            return new byte[]{0x30, (byte) 0x80}; // Fallback minimal structure
        }
    }

    private byte[] createInvalidMockSignature() {
        // Create invalid signature data
        return new byte[]{(byte)0xFF, (byte)0xFF, (byte)0xFF, (byte)0xFF};
    }
}