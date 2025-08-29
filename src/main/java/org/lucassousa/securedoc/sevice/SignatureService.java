package org.lucassousa.securedoc.sevice;

import org.bouncycastle.cert.X509CertificateHolder;
import org.bouncycastle.cert.jcajce.JcaCertStore;
import org.bouncycastle.cms.*;
import org.bouncycastle.operator.jcajce.JcaContentSignerBuilder;
import org.bouncycastle.cms.jcajce.JcaSignerInfoGeneratorBuilder;
import org.bouncycastle.operator.jcajce.JcaDigestCalculatorProviderBuilder;
import org.bouncycastle.util.Store;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@Service
public class SignatureService {

    public byte[] generateSignature(byte[] fileBytes, byte[] pkcs12Bytes, String password) throws Exception {

        KeyStore ks = KeyStore.getInstance("PKCS12");
        ks.load(new ByteArrayInputStream(pkcs12Bytes), password.toCharArray());

        String alias = ks.aliases().nextElement(); // assume first alias
        PrivateKey privateKey = (PrivateKey) ks.getKey(alias, password.toCharArray());
        X509Certificate cert = (X509Certificate) ks.getCertificate(alias);

        CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
        gen.addSignerInfoGenerator(
                new JcaSignerInfoGeneratorBuilder(
                        new JcaDigestCalculatorProviderBuilder().build()
                ).build(new JcaContentSignerBuilder("SHA512withRSA").build(privateKey), cert)
        );
        gen.addCertificates(new JcaCertStore(Collections.singletonList(cert)));

        CMSProcessableByteArray content = new CMSProcessableByteArray(fileBytes);
        CMSSignedData signedData = gen.generate(content, true);

        return signedData.getEncoded();
    }

    public Map<String, Object> verifySignature(byte[] signedBytes) throws Exception {
        CMSSignedData cms = new CMSSignedData(signedBytes);

        boolean valid = !cms.getSignerInfos().getSigners().isEmpty();

        Map<String, Object> result = new HashMap<>();
        result.put("status", valid ? "VALIDO" : "INVALIDO");

        if (valid) {
            Store<X509CertificateHolder> certStore = cms.getCertificates();
            for (SignerInformation signerInfo : cms.getSignerInfos().getSigners()) {
                Collection<X509CertificateHolder> certCollection = certStore.getMatches(signerInfo.getSID());
                if (!certCollection.isEmpty()) {
                    X509CertificateHolder certHolder = certCollection.iterator().next();
                    result.put("signerCN", certHolder.getSubject().toString());
                }
            }
        }

        return result;
    }

}
