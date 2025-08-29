package org.lucassousa.securedoc.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.lucassousa.securedoc.sevice.SignatureService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class SignatureController {

    @Autowired
    private SignatureService signatureService;

    @Operation(summary = "Check API status",
            description = "GET endpoint to check if the Signature API is running. Returns a simple status message.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "API is running successfully")
    })
    @GetMapping("/status")
    public ResponseEntity<Map<String, String>> status() {
        Map<String, String> response = new HashMap<>();
        response.put("status", "Signature API is running");
        response.put("description", "This API allows generating and verifying digital signatures using SHA-512 and RSA.");
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Generate a digital signature for a file",
            description = "POST endpoint to generate a digital signature using CMS attached standard. "
                    + "The file to be signed, PKCS12 (certificate + private key), and password must be provided. "
                    + "The returned signature is Base64 encoded.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Signature generated successfully"),
            @ApiResponse(responseCode = "500", description = "Error generating signature")
    })
    @PostMapping("/signature")
    public ResponseEntity<Map<String, String>> signFile(
            @RequestParam("file") MultipartFile file,
            @RequestParam("pkcs12") MultipartFile pkcs12,
            @RequestParam("password") String password
    ) {
        try {
            byte[] signature = signatureService.generateSignature(file.getBytes(), pkcs12.getBytes(), password);
            String signatureBase64 = Base64.getEncoder().encodeToString(signature);

            Map<String, String> response = new HashMap<>();
            response.put("signature", signatureBase64);
            response.put("algorithm", "SHA-512 with RSA (CMS attached)");

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(Collections.singletonMap("error", e.getMessage()));
        }
    }

    @Operation(summary = "Verify a digital signature attached to a file",
            description = "POST endpoint to verify a CMS attached digital signature. "
                    + "The signed file must be provided. "
                    + "Returns JSON containing status, signer info, signing time, document hash, and hash algorithm.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Signature verified successfully"),
            @ApiResponse(responseCode = "500", description = "Error verifying signature")
    })
    @PostMapping("/verify")
    public ResponseEntity<Map<String, Object>> verifyFile(@RequestParam("signedFile") MultipartFile signedFile) {
        try {
            Map<String, Object> result = signatureService.verifySignature(signedFile.getBytes());
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            Map<String, Object> errorResponse = new HashMap<>();
            errorResponse.put("status", "INVALIDO");
            errorResponse.put("error", e.getMessage());
            return ResponseEntity.status(500).body(errorResponse);
        }
    }
}
