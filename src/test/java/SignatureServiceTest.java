package org.lucassousa.securedoc.sevice;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class SignatureServiceTest {

    private static SignatureService signatureService;
    private static byte[] pkcs12Bytes;
    private static byte[] fileBytes;
    private static byte[] signedBytes;

    @BeforeAll
    public static void setup() throws Exception {
        signatureService = new SignatureService();

        File pkcs12File = new File("resources/pkcs12/certificado_teste_hub");
        pkcs12Bytes = new byte[(int) pkcs12File.length()];
        try (FileInputStream fis = new FileInputStream(pkcs12File)) {
            fis.read(pkcs12Bytes);
        }

        File file = new File("resources/arquivos/doc.txt");
        fileBytes = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(fileBytes);
        }

        signedBytes = signatureService.generateSignature(fileBytes, pkcs12Bytes, "bry123456");
    }

    @Test
    public void testSignatureGeneration() throws Exception {
        assertTrue(signedBytes.length > 0);
    }

    @Test
    public void testSignatureVerification() throws Exception {
        Map<String, Object> result = signatureService.verifySignature(signedBytes);

        assertEquals("VALIDO", result.get("status"));
        assertTrue(result.containsKey("signerCN"));
        System.out.println("Signer CN: " + result.get("signerCN"));
    }
}
