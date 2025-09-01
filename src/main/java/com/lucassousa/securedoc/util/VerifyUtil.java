package com.lucassousa.securedoc.util;

import org.bouncycastle.asn1.ASN1GeneralizedTime;
import org.bouncycastle.asn1.ASN1Set;
import org.bouncycastle.asn1.ASN1UTCTime;
import org.bouncycastle.asn1.cms.Attribute;
import org.bouncycastle.asn1.cms.AttributeTable;
import org.bouncycastle.asn1.cms.CMSAttributes;
import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaX509CertificateConverter;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;
import org.bouncycastle.cms.SignerInformationVerifier;
import org.bouncycastle.cms.jcajce.JcaSimpleSignerInfoVerifierBuilder;
import org.bouncycastle.util.Store;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.cert.*;
import java.text.SimpleDateFormat;
import java.util.*;


public class VerifyUtil {

    private static final String CHAIN_PATH = "src/main/resources/cadeia/";

    public static void main(String[] args) throws Exception {
        File signedFile = new File("doc.txt.p7s");
        if (!signedFile.exists()) {
            System.out.println("Signed file not found. Please run SignUtil first.");
            return;
        }

        byte[] signedBytes = readFileBytes(signedFile);
        VerificationResult result = verifySignature(signedBytes);

        System.out.println("=== SIGNATURE VERIFICATION RESULTS ===");
        System.out.println("Status: " + result.getStatus());
        System.out.println("Valid: " + result.isValid());
        System.out.println("Trust Chain Valid: " + result.isTrustChainValid());

        if (result.getSignerInfo() != null) {
            SignerInfo info = result.getSignerInfo();
            System.out.println("\n=== SIGNER INFORMATION ===");
            System.out.println("Common Name (CN): " + info.getCommonName());
            System.out.println("Signing Time: " + info.getSigningTime());
            System.out.println("Document Hash (HEX): " + info.getDocumentHash());
            System.out.println("Hash Algorithm: " + info.getHashAlgorithm());
            System.out.println("Full Subject: " + info.getFullSubject());
        }

        if (result.getError() != null) {
            System.out.println("\nError: " + result.getError());
        }
    }


    public static VerificationResult verifySignature(byte[] signedBytes) {
        try {
            CMSSignedData cmsSignedData = new CMSSignedData(signedBytes);

            SignerInformationStore signers = cmsSignedData.getSignerInfos();
            Collection<SignerInformation> signerCollection = signers.getSigners();

            if (signerCollection.isEmpty()) {
                return new VerificationResult(false, "INVALIDO", "No signers found", false);
            }

            Store<X509CertificateHolder> certStore = cmsSignedData.getCertificates();

            boolean allSignaturesValid = true;
            SignerInfo signerInfo = null;

            for (SignerInformation signerInformation : signerCollection) {
                try {
                    Collection<X509CertificateHolder> certCollection = certStore.getMatches(signerInformation.getSID());
                    if (certCollection.isEmpty()) {
                        allSignaturesValid = false;
                        continue;
                    }

                    X509CertificateHolder certHolder = certCollection.iterator().next();
                    X509Certificate cert = new JcaX509CertificateConverter().getCertificate(certHolder);

                    SignerInformationVerifier verifier = new JcaSimpleSignerInfoVerifierBuilder()
                            .build(certHolder);
                    boolean signatureValid = signerInformation.verify(verifier);

                    if (!signatureValid) {
                        allSignaturesValid = false;
                        continue;
                    }

                    signerInfo = extractSignerInfo(signerInformation, certHolder, cmsSignedData);

                } catch (Exception e) {
                    allSignaturesValid = false;
                    System.err.println("Error verifying signature: " + e.getMessage());
                }
            }

            boolean trustChainValid = verifyCertificateChain(cmsSignedData);

            String status = allSignaturesValid ? "VALIDO" : "INVALIDO";
            return new VerificationResult(allSignaturesValid, status, null, trustChainValid, signerInfo);

        } catch (Exception e) {
            return new VerificationResult(false, "INVALIDO", "Error processing signature: " + e.getMessage(), false);
        }
    }


    private static SignerInfo extractSignerInfo(SignerInformation signerInformation,
                                                X509CertificateHolder certHolder,
                                                CMSSignedData cmsSignedData) throws Exception {

        SignerInfo info = new SignerInfo();

        String subject = certHolder.getSubject().toString();
        info.setFullSubject(subject);
        info.setCommonName(extractCN(subject));

        AttributeTable signedAttributes = signerInformation.getSignedAttributes();
        if (signedAttributes != null) {
            Attribute timeAttr = signedAttributes.get(CMSAttributes.signingTime);
            if (timeAttr != null) {
                try {
                    ASN1Set values = timeAttr.getAttrValues();
                    if (values.size() > 0) {
                        ASN1UTCTime utcTime = ASN1UTCTime.getInstance(values.getObjectAt(0));
                        Date signingTime = utcTime.getDate();
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                        info.setSigningTime(sdf.format(signingTime));
                    }
                } catch (Exception e) {
                    try {
                        ASN1Set values = timeAttr.getAttrValues();
                        if (values.size() > 0) {
                            ASN1GeneralizedTime genTime = ASN1GeneralizedTime.getInstance(values.getObjectAt(0));
                            Date signingTime = genTime.getDate();
                            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                            info.setSigningTime(sdf.format(signingTime));
                        }
                    } catch (Exception ex) {
                        System.err.println("Could not extract signing time: " + ex.getMessage());
                        info.setSigningTime("Unknown");
                    }
                }
            }
        }

        byte[] documentHash = signerInformation.getContentDigest();
        if (documentHash != null) {
            info.setDocumentHash(bytesToHex(documentHash));
        }

        String hashAlgorithm = signerInformation.getDigestAlgOID();
        info.setHashAlgorithm(getHashAlgorithmName(hashAlgorithm));

        return info;
    }


    private static boolean verifyCertificateChain(CMSSignedData cmsSignedData) {
        try {
            Set<X509Certificate> trustedCerts = loadTrustedCertificates();

            if (trustedCerts.isEmpty()) {
                System.out.println("Warning: No trusted CA certificates found in " + CHAIN_PATH);
                return false;
            }

            Store<X509CertificateHolder> certStore = cmsSignedData.getCertificates();
            Collection<X509CertificateHolder> certs = certStore.getMatches(null);

            if (certs.isEmpty()) {
                return false;
            }

            JcaX509CertificateConverter converter = new JcaX509CertificateConverter();
            Set<X509Certificate> cmsCerts = new HashSet<>();
            for (X509CertificateHolder holder : certs) {
                cmsCerts.add(converter.getCertificate(holder));
            }

            CertPathValidator validator = CertPathValidator.getInstance("PKIX");
            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            Set<TrustAnchor> trustAnchors = new HashSet<>();
            for (X509Certificate caCert : trustedCerts) {
                trustAnchors.add(new TrustAnchor(caCert, null));
            }

            for (X509Certificate cert : cmsCerts) {
                try {
                    List<X509Certificate> certPath = Arrays.asList(cert);
                    CertPath path = cf.generateCertPath(certPath);

                    PKIXParameters params = new PKIXParameters(trustAnchors);
                    params.setRevocationEnabled(false);

                    validator.validate(path, params);
                    return true;

                } catch (Exception e) {
                    System.out.println("Certificate validation failed: " + e.getMessage());
                }
            }

            return false;

        } catch (Exception e) {
            System.err.println("Error verifying certificate chain: " + e.getMessage());
            return false;
        }
    }


    private static Set<X509Certificate> loadTrustedCertificates() {
        Set<X509Certificate> trustedCerts = new HashSet<>();

        try {
            File chainDir = new File(CHAIN_PATH);
            if (!chainDir.exists() || !chainDir.isDirectory()) {
                System.out.println("Chain directory not found: " + CHAIN_PATH);
                return trustedCerts;
            }

            CertificateFactory cf = CertificateFactory.getInstance("X.509");

            File[] certFiles = chainDir.listFiles((dir, name) ->
                    name.toLowerCase().endsWith(".crt") ||
                            name.toLowerCase().endsWith(".cer") ||
                            name.toLowerCase().endsWith(".pem") ||
                            !name.contains(".")
            );

            if (certFiles != null) {
                for (File certFile : certFiles) {
                    try (FileInputStream fis = new FileInputStream(certFile)) {
                        X509Certificate cert = (X509Certificate) cf.generateCertificate(fis);
                        trustedCerts.add(cert);
                        System.out.println("Loaded CA certificate: " + certFile.getName());
                    } catch (Exception e) {
                        System.err.println("Failed to load certificate " + certFile.getName() + ": " + e.getMessage());
                    }
                }
            }

        } catch (Exception e) {
            System.err.println("Error loading trusted certificates: " + e.getMessage());
        }

        return trustedCerts;
    }

    private static String extractCN(String subject) {
        String[] parts = subject.split(",");
        for (String part : parts) {
            part = part.trim();
            if (part.startsWith("CN=")) {
                return part.substring(3);
            }
        }
        return "Unknown";
    }

    private static String bytesToHex(byte[] bytes) {
        StringBuilder result = new StringBuilder();
        for (byte b : bytes) {
            result.append(String.format("%02x", b));
        }
        return result.toString().toUpperCase();
    }

    private static String getHashAlgorithmName(String oid) {
        switch (oid) {
            case "2.16.840.1.101.3.4.2.3": return "SHA-512";
            case "2.16.840.1.101.3.4.2.1": return "SHA-256";
            case "1.3.14.3.2.26": return "SHA-1";
            default: return "Unknown (" + oid + ")";
        }
    }

    private static byte[] readFileBytes(File file) throws IOException {
        byte[] bytes = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(bytes);
        }
        return bytes;
    }

    public static class VerificationResult {
        private final boolean valid;
        private final String status;
        private final String error;
        private final boolean trustChainValid;
        private final SignerInfo signerInfo;

        public VerificationResult(boolean valid, String status, String error, boolean trustChainValid) {
            this(valid, status, error, trustChainValid, null);
        }

        public VerificationResult(boolean valid, String status, String error, boolean trustChainValid, SignerInfo signerInfo) {
            this.valid = valid;
            this.status = status;
            this.error = error;
            this.trustChainValid = trustChainValid;
            this.signerInfo = signerInfo;
        }

        public boolean isValid() { return valid; }
        public String getStatus() { return status; }
        public String getError() { return error; }
        public boolean isTrustChainValid() { return trustChainValid; }
        public SignerInfo getSignerInfo() { return signerInfo; }
    }

    public static class SignerInfo {
        private String commonName;
        private String signingTime;
        private String documentHash;
        private String hashAlgorithm;
        private String fullSubject;

        public String getCommonName() { return commonName; }
        public void setCommonName(String commonName) { this.commonName = commonName; }

        public String getSigningTime() { return signingTime; }
        public void setSigningTime(String signingTime) { this.signingTime = signingTime; }

        public String getDocumentHash() { return documentHash; }
        public void setDocumentHash(String documentHash) { this.documentHash = documentHash; }

        public String getHashAlgorithm() { return hashAlgorithm; }
        public void setHashAlgorithm(String hashAlgorithm) { this.hashAlgorithm = hashAlgorithm; }

        public String getFullSubject() { return fullSubject; }
        public void setFullSubject(String fullSubject) { this.fullSubject = fullSubject; }
    }
}