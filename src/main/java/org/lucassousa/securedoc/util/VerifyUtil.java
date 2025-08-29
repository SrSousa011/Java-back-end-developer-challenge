package org.lucassousa.securedoc.util;

import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSProcessableByteArray;

import java.io.File;
import java.io.FileInputStream;

public class VerifyUtil {
    public static void main(String[] args) throws Exception {
        File file = new File("doc.txt.p7s");
        byte[] signedBytes = new byte[(int) file.length()];
        FileInputStream fis = new FileInputStream(file);
        fis.read(signedBytes);
        fis.close();

        CMSSignedData cms = new CMSSignedData(signedBytes);
        boolean valid = cms.getSignerInfos().size() > 0;
        System.out.println("Signature valid: " + valid);
    }
}
