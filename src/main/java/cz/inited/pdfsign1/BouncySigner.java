package cz.inited.pdfsign1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.CertStore;
import java.security.cert.Certificate;
import java.security.cert.CollectionCertStoreParameters;
import java.security.cert.X509Certificate;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import org.apache.pdfbox.exceptions.SignatureException;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.CMSSignedDataGenerator;
import org.bouncycastle.cms.CMSSignedGenerator;
import org.bouncycastle.jce.provider.BouncyCastleProvider;

public class BouncySigner implements SignatureInterface {
    
    private static BouncyCastleProvider provider = new BouncyCastleProvider();
    
    private PrivateKey privKey;
    
    private Certificate[] cert;
    
    public BouncySigner() throws Exception {
        String pass = "Heslo123";
        
        FileInputStream is = new FileInputStream("cert.p12");
        
        KeyStore keystore = KeyStore.getInstance("PKCS12");
        keystore.load(is, pass.toCharArray());
        
        Enumeration<String> aliases = keystore.aliases();
        String alias = aliases.nextElement();
        
        privKey = (PrivateKey) keystore.getKey(alias, pass.toCharArray());
        cert = keystore.getCertificateChain(alias);
    }
    
    public byte[] sign(InputStream content) throws SignatureException, IOException {
        
        // uloz data k podpisu
        File outputDocument = new File("data_to_sign.bin");
        FileOutputStream fos = new FileOutputStream(outputDocument);
        byte[] buffer = new byte[8 * 1024];
        int c;
        while ((c = content.read(buffer)) != -1) {
            fos.write(buffer, 0, c);
        }
        content.close();
        fos.close();
        content = new FileInputStream(outputDocument);
        
        
        CMSProcessableInputStream input = new CMSProcessableInputStream(content);
        CMSSignedDataGenerator gen = new CMSSignedDataGenerator();
        // CertificateChain
        List<Certificate> certList = Arrays.asList(cert);
        
        CertStore certStore = null;
        try {
            certStore = CertStore.getInstance("Collection", new CollectionCertStoreParameters(certList), provider);
            gen.addSigner(privKey, (X509Certificate) certList.get(0), CMSSignedGenerator.DIGEST_SHA256);
            gen.addCertificatesAndCRLs(certStore);
            CMSSignedData signedData = gen.generate(input, false, provider);
            
            byte[] signature = signedData.getEncoded();
            outputDocument = new File("signature.bin");
            fos = new FileOutputStream(outputDocument);
            fos.write(signature, 0, signature.length);
            fos.close();
            return signature;
        } catch (Exception e) {
            // should be handled
            System.err.println("Error while creating pkcs7 signature.");
            e.printStackTrace();
        }
        throw new RuntimeException("Problem while preparing signature");
    }
}
