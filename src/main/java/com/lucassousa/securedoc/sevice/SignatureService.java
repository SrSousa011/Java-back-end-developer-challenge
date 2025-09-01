package com.lucassousa.securedoc.sevice;


import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.ContentSigner;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import com.lucassousa.securedoc.util.VerifyUtil;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;


@Service
public class SignatureService {


    public byte[] generateSignature(byte[] fileBytes, byte[] pkcs12Bytes, String password) throws Exception {

        if (fileBytes == null || fileBytes.length == 0) {
            throw new IllegalArgumentException("File content cannot be null or empty");
        }
        if (pkcs12Bytes == null || pkcs12Bytes.length == 0) {
            throw new IllegalArgumentException("PKCS#12 content cannot be null or empty");
        }
        if (password == null || password.trim().isEmpty()) {
            throw new IllegalArgumentException("Password cannot be null or empty");
        }

        try {
            KeyStore ks = KeyStore.getInstance("PKCS12");
            ks.load(new ByteArrayInputStream(pkcs12Bytes), password.toCharArray());

            String alias = findSigningAlias(ks, password);
            if (alias == null) {
                throw new RuntimeException("No suitable signing certificate found in PKCS#12 file");
            }

            PrivateKey privateKey = (PrivateKey) ks.getKey(alias, password.toCharArray());
            X509Certificate cert = (X509Certificate) ks.getCertificate(alias);

            if (privateKey == null) {
                throw new RuntimeException("Private key not found for alias: " + alias);
            }
            if (cert == null) {
                throw new RuntimeException("Certificate not found for alias: " + alias);
            }

            CMSSignedDataGenerator generator = new CMSSignedDataGenerator();

            ContentSigner signer = new JcaContentSignerBuilder("SHA512withRSA").build(privateKey);

            generator.addSignerInfoGenerator(
                    new JcaSignerInfoGeneratorBuilder(
                            new JcaDigestCalculatorProviderBuilder().build()
                    ).build(signer, cert)
            );

            generator.addCertificates(new JcaCertStore(Collections.singletonList(cert)));

            CMSProcessableByteArray content = new CMSProcessableByteArray(fileBytes);

            CMSSignedData signedData = generator.generate(content, true);

            return signedData.getEncoded();

        } catch (Exception e) {
            throw new RuntimeException("Failed to generate signature: " + e.getMessage(), e);
        }
    }


    public Map<String, Object> verifySignature(byte[] signedBytes) {

        if (signedBytes == null || signedBytes.length == 0) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("status", "INVALIDO");
            errorResult.put("error", "Signed content cannot be null or empty");
            return errorResult;
        }

        try {
            VerifyUtil.VerificationResult result = VerifyUtil.verifySignature(signedBytes);

            Map<String, Object> response = getStringObjectMap(result);

            return response;

        } catch (Exception e) {
            Map<String, Object> errorResult = new HashMap<>();
            errorResult.put("status", "INVALIDO");
            errorResult.put("error", "Signature verification failed: " + e.getMessage());
            errorResult.put("cryptographicValidation", false);
            errorResult.put("certificateChainValidation", false);
            return errorResult;
        }
    }

    private static Map<String, Object> getStringObjectMap(VerifyUtil.VerificationResult result) {
        Map<String, Object> response = new HashMap<>();
        response.put("status", result.getStatus());

        if (result.isValid() && result.getSignerInfo() != null) {
            Map<String, Object> infos = new HashMap<>();
            VerifyUtil.SignerInfo signerInfo = result.getSignerInfo();

            infos.put("signerCN", signerInfo.getCommonName());
            infos.put("signingTime", signerInfo.getSigningTime());
            infos.put("documentHash", signerInfo.getDocumentHash());
            infos.put("hashAlgorithm", signerInfo.getHashAlgorithm());
            infos.put("fullSubject", signerInfo.getFullSubject());
            infos.put("trustChainValid", result.isTrustChainValid());

            response.put("infos", infos);
        }

        if (result.getError() != null) {
            response.put("error", result.getError());
        }

        response.put("cryptographicValidation", result.isValid());
        response.put("certificateChainValidation", result.isTrustChainValid());
        return response;
    }


    public byte[] extractSignedContent(byte[] signedBytes) throws Exception {
        CMSSignedData cmsSignedData = new CMSSignedData(signedBytes);
        CMSProcessable signedContent = cmsSignedData.getSignedContent();

        if (signedContent == null) {
            throw new RuntimeException("No attached content found in signature");
        }

        return (byte[]) signedContent.getContent();
    }


    private String findSigningAlias(KeyStore keystore, String password) throws Exception {

        String challengeAlias = "e2618a8b-20de-4dd2-b209-70912e3177f4";
        if (keystore.containsAlias(challengeAlias)) {
            try {
                PrivateKey key = (PrivateKey) keystore.getKey(challengeAlias, password.toCharArray());
                if (key != null) {
                    return challengeAlias;
                }
            } catch (Exception e) {
            }
        }

        Enumeration<String> aliases = keystore.aliases();
        while (aliases.hasMoreElements()) {
            String alias = aliases.nextElement();
            if (keystore.isKeyEntry(alias)) {
                try {
                    PrivateKey key = (PrivateKey) keystore.getKey(alias, password.toCharArray());
                    if (key != null) {
                        return alias;
                    }
                } catch (Exception e) {
                }
            }
        }

        return null;
    }


     public Map<String, Object> getSignatureInfo() {
        Map<String, Object> info = new HashMap<>();
        info.put("hashAlgorithm", "SHA-512");
        info.put("encryptionAlgorithm", "RSA");
        info.put("signatureStandard", "CMS (Cryptographic Message Syntax)");
        info.put("signatureType", "Attached");
        info.put("javaCompatibility", "Java 8+");
        info.put("bouncyCastleVersion", "1.70");
        info.put("springBootVersion", "2.7.x");
        return info;
    }
}
