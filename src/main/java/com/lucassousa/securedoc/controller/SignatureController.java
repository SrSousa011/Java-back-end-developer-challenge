package com.lucassousa.securedoc.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.bouncycastle.asn1.ASN1UTCTime;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.bouncycastle.util.Store;
import com.lucassousa.securedoc.sevice.SignatureService;
import com.lucassousa.securedoc.util.HashUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.*;


@RestController
@RequestMapping("/api")
public class SignatureController {

    @Autowired
    private SignatureService signatureService;

    @Operation(summary = "Check API status",
            description = "GET endpoint to check if the Signature API is running. Returns a simple status message.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Request processed successfully"),
            @ApiResponse(responseCode = "201", description = "Resource created successfully"),
            @ApiResponse(responseCode = "204", description = "No content to return"),
            @ApiResponse(responseCode = "400", description = "Bad request - invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - you don’t have permission"),
            @ApiResponse(responseCode = "404", description = "Resource not found"),
            @ApiResponse(responseCode = "409", description = "Conflict - resource already exists"),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity - validation error"),
            @ApiResponse(responseCode = "500", description = "Internal server error"),
            @ApiResponse(responseCode = "502", description = "Bad Gateway - invalid response from upstream"),
            @ApiResponse(responseCode = "503", description = "Service unavailable"),
            @ApiResponse(responseCode = "504", description = "Gateway timeout")
    })
    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> status() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "Signature API is running");
        response.put("description", "This API allows generating and verifying digital signatures using SHA-512 and RSA.");
        response.put("javaVersion", System.getProperty("java.version"));
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Generate a digital signature for a file",
            description = "POST endpoint to generate a digital signature using CMS attached standard. "
                    + "The file to be signed, PKCS12 (certificate + private key), and password must be provided. "
                    + "The returned signature is Base64 encoded.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Signature generated successfully"),
            @ApiResponse(responseCode = "201", description = "Signature created successfully"),
            @ApiResponse(responseCode = "204", description = "No content"),
            @ApiResponse(responseCode = "400", description = "Bad request - invalid input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - access denied"),
            @ApiResponse(responseCode = "404", description = "Signature not found"),
            @ApiResponse(responseCode = "409", description = "Conflict - signature already exists"),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity - validation error"),
            @ApiResponse(responseCode = "500", description = "Error generating signature"),
            @ApiResponse(responseCode = "502", description = "Bad Gateway - upstream service error"),
            @ApiResponse(responseCode = "503", description = "Service unavailable"),
            @ApiResponse(responseCode = "504", description = "Gateway timeout")
    })

    @PostMapping(value = "/signature", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> signFile(
            @RequestParam("doc") MultipartFile file,
            @RequestParam("pkcs12") MultipartFile pkcs12,
            @RequestParam("password") String password
    ) {
        try {
            byte[] signature = signatureService.generateSignature(
                    file.getBytes(),
                    pkcs12.getBytes(),
                    password
            );

            String signatureBase64 = Base64.getEncoder().encodeToString(signature);

            Path folder = Paths.get("src/main/resources/signatured");
            if (!Files.exists(folder)) {
                Files.createDirectories(folder);
            }

            String originalFilename = file.getOriginalFilename();
            String p7sFilename = originalFilename != null ? originalFilename + ".p7s" : "signed_document.p7s";
            Path p7sFilePath = folder.resolve(p7sFilename);
            Files.write(p7sFilePath, signature);
            System.out.println("Signature saved to disk: " + p7sFilePath.toAbsolutePath());

            Map<String, String> response = new HashMap<>();
            response.put("signature", signatureBase64);
            response.put("algorithm", "SHA-512 with RSA (CMS attached)");
            response.put("filename", file.getOriginalFilename());
            response.put("fileSize", String.valueOf(file.getSize()));
            response.put("p7sFile", p7sFilename);
            response.put("p7sSaved", "true");

            response.put("downloadP7s", p7sFilePath.toAbsolutePath().toString());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            errorResponse.put("status", "ERROR");
            return ResponseEntity.status(500).body(errorResponse);
        }
    }


    @Operation(summary = "Verify a digital signature attached to a file",
            description = "POST endpoint to verify a CMS attached digital signature. "
                    + "The signed file must be provided. "
                    + "Returns JSON containing status, signer info, signing time, document hash, and hash algorithm.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Signature verified successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - invalid signature format"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - access denied"),
            @ApiResponse(responseCode = "404", description = "Signature not found"),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity - signature could not be verified"),
            @ApiResponse(responseCode = "500", description = "Error verifying signature"),
            @ApiResponse(responseCode = "502", description = "Bad Gateway - upstream verification service error"),
            @ApiResponse(responseCode = "503", description = "Service unavailable"),
            @ApiResponse(responseCode = "504", description = "Gateway timeout")
    })

    @PostMapping(value="/verify", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> verifyFile(
            @RequestParam("signedFile") MultipartFile signedFile,
            @RequestParam("originalFile") MultipartFile originalFile) {

        Map<String, Object> response = new HashMap<>();
        response.put("filename", signedFile.getOriginalFilename());

        try {
            byte[] signedBytes = signedFile.getBytes();
            byte[] originalBytes = originalFile.getBytes();

            CMSSignedData signedData = new CMSSignedData(new CMSProcessableByteArray(originalBytes), signedBytes);
            SignerInformation signerInfo = signedData.getSignerInfos().getSigners().iterator().next();
            Store<X509CertificateHolder> certStore = signedData.getCertificates();

            boolean signatureValid;
            Date signingTime = null;
            List<Map<String, Object>> infos = new ArrayList<>();

            Attribute signingTimeAttr = signerInfo.getSignedAttributes().get(CMSAttributes.signingTime);
            if (signingTimeAttr != null) {
                signingTime = ((ASN1UTCTime) signingTimeAttr.getAttrValues().getObjectAt(0)).getAdjustedDate();
            }

            String topLevelSigningTime = signingTime != null ? new java.text.SimpleDateFormat("dd/MM/yyyy").format(signingTime) : null;
            response.put("signingTime", topLevelSigningTime);

            X509CertificateHolder signerCertHolder = (X509CertificateHolder) certStore.getMatches(signerInfo.getSID()).iterator().next();
            X509Certificate signerCert = new JcaX509CertificateConverter()
                    .setProvider(new BouncyCastleProvider())
                    .getCertificate(signerCertHolder);

            signatureValid = signerInfo.verify(
                    new JcaSimpleSignerInfoVerifierBuilder()
                            .setProvider(new BouncyCastleProvider())
                            .build(signerCert)
            );

            Path chainFolder = Paths.get("src/main/resources/cadeia");
            try (DirectoryStream<Path> stream = Files.newDirectoryStream(chainFolder, "*.cer")) {
                for (Path certPath : stream) {
                    try {
                        Map<String, Object> info = new HashMap<>();
                        info.put("fileName", certPath.getFileName().toString());

                        byte[] certBytes = Files.readAllBytes(certPath);
                        X509Certificate cert = (X509Certificate) CertificateFactory.getInstance("X.509")
                                .generateCertificate(new ByteArrayInputStream(certBytes));

                        MessageDigest sha256 = MessageDigest.getInstance("SHA-256");
                        byte[] certHashBytes = sha256.digest(cert.getEncoded());
                        info.put("documentHash", bytesToHex(certHashBytes).toUpperCase());

                        info.put("signerName", cert.getIssuerX500Principal().getName());

                        info.put("signingTime", signingTime != null ? signingTime.toInstant().toString() : null);

                        info.put("hashAlgorithm", getDigestAlgorithm(signerInfo));

                        infos.add(info);

                    } catch (Exception e) {
                        System.err.println("Error processing certificate file " + certPath.getFileName() + ": " + e.getMessage());
                    }
                }
            }

            response.put("status", signatureValid ? "VALIDO" : "INVALIDO");
            response.put("infos", infos);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            response.put("status", "INVALIDO");
            response.put("infos", new ArrayList<>());
            response.put("error", e.getMessage());
            return ResponseEntity.status(500).body(response);
        }
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    private static String getDigestAlgorithm(SignerInformation signerInfo) {
        String algOID = signerInfo.getDigestAlgOID();
        switch (algOID) {
            case "1.3.14.3.2.26": return "SHA-1";
            case "2.16.840.1.101.3.4.2.1": return "SHA-256";
            case "2.16.840.1.101.3.4.2.2": return "SHA-384";
            case "2.16.840.1.101.3.4.2.3": return "SHA-512";
            default: return "UNKNOWN";
        }
    }




    @PostMapping(value = "/hash", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
            summary = "Generate SHA-512 hash for a file",
            description = "Takes a file as input and returns the generated SHA-512 hash."
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "SHA-512 hash generated successfully"),
            @ApiResponse(responseCode = "400", description = "Bad request - invalid file input"),
            @ApiResponse(responseCode = "401", description = "Unauthorized - authentication required"),
            @ApiResponse(responseCode = "403", description = "Forbidden - access denied"),
            @ApiResponse(responseCode = "404", description = "File not found"),
            @ApiResponse(responseCode = "422", description = "Unprocessable Entity - unable to process file"),
            @ApiResponse(responseCode = "500", description = "Internal server error while generating hash"),
            @ApiResponse(responseCode = "502", description = "Bad Gateway - upstream service error"),
            @ApiResponse(responseCode = "503", description = "Service unavailable"),
            @ApiResponse(responseCode = "504", description = "Gateway timeout")
    })

    public ResponseEntity<Map<String, String>> generateHash(
            @RequestParam("doc") MultipartFile file) {
        try {
            String hash = HashUtil.generateSHA512Hash(file.getBytes());
            Map<String, String> response = new HashMap<>();
            response.put("hash", hash);
            response.put("algorithm", "SHA-512");
            response.put("filename", file.getOriginalFilename());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }

    @Operation(summary = "Get signature algorithm information",
            description = "GET endpoint to retrieve information about supported signature algorithms and formats.")
    @GetMapping("/info")
    public ResponseEntity<Map<String, Object>> getSignatureInfo() {
        Map<String, Object> info = signatureService.getSignatureInfo();

        Map<String, String> javaInfo = new HashMap<>();
        javaInfo.put("version", System.getProperty("java.version"));
        javaInfo.put("vendor", System.getProperty("java.vendor"));
        javaInfo.put("runtime", System.getProperty("java.runtime.version"));

        info.put("javaEnvironment", javaInfo);

        return ResponseEntity.ok(info);
    }
}