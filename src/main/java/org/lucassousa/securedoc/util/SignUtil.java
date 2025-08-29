package org.lucassousa.securedoc.util;

import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.cert.jcajce.JcaCertStore;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collections;

public class SignUtil {
    public static void main(String[] args) throws Exception {
        String pkcs12Path = "resources/pkcs12/certificado_teste_hub";
        String filePath = "resources/arquivos/doc.txt";

        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new FileInputStream(pkcs12Path), "bry123456".toCharArray());

        PrivateKey privateKey = (PrivateKey) ks.getKey("e2618a8b-20de-4dd2-b209-70912e3177f4", "bry123456".toCharArray());
        X509Certificate cert = (X509Certificate) ks.getCertificate("e2618a8b-20de-4dd2-b209-70912e3177f4");

        CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
        gen.addSignerInfoGenerator(
                new JcaSignerInfoGeneratorBuilder(
                        new org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder().build()
                ).build(new JcaContentSignerBuilder("SHA512withRSA").build(privateKey), cert)
        );
        gen.addCertificates(new JcaCertStore(Collections.singletonList(cert)));

        // Read file manually for Java 8
        File file = new File(filePath);
        byte[] data = new byte[(int) file.length()];
        FileInputStream fis = new FileInputStream(file);
        int readBytes = fis.read(data);
        fis.close();

        CMSSignedData signedData = gen.generate(new CMSProcessableByteArray(data), true);
        FileOutputStream fos = new FileOutputStream("doc.txt.p7s");
        fos.write(signedData.getEncoded());
        fos.close();

        System.out.println("Signature generated: doc.txt.p7s");
    }
}
