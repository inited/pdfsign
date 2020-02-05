package cz.inited.pdfsign1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.cert.CertStore;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.bouncycastle.cms.CMSProcessable;
import org.bouncycastle.cms.CMSProcessableByteArray;
import org.bouncycastle.cms.CMSSignedData;
import org.bouncycastle.cms.SignerInformation;
import org.bouncycastle.cms.SignerInformationStore;

public class PDFBoxValidateSignature {
    public static void main(String[] args) throws Exception {
        File signedFile = new File("file_signed.pdf");
        // We load the signed document.
        PDDocument document = PDDocument.load(signedFile);
        List<PDSignature> signatureDictionaries = document.getSignatureDictionaries();
        // Then we validate signatures one at the time.
        for (PDSignature signatureDictionary : signatureDictionaries) {
            // NOTE that this code currently supports only "adbe.pkcs7.detached", the most
            // common signature /SubFilter anyway.
            byte[] signatureContent = signatureDictionary.getContents(new FileInputStream(signedFile));
            byte[] signedContent = signatureDictionary.getSignedContent(new FileInputStream(signedFile));
            saveBytesToFile(signatureContent, "signatureContent");
            saveBytesToFile(signedContent, "signedContent");
            // Now we construct a PKCS #7 or CMS.
            CMSProcessable cmsProcessableInputStream = new CMSProcessableByteArray(signedContent);
            CMSSignedData cmsSignedData = new CMSSignedData(cmsProcessableInputStream, signatureContent);
            SignerInformationStore signerInformationStore = cmsSignedData.getSignerInfos();
            Collection signers = signerInformationStore.getSigners();
            CertStore certs = cmsSignedData.getCertificatesAndCRLs("Collection", (String) null);
            Iterator signersIterator = signers.iterator();
            while (signersIterator.hasNext()) {
                SignerInformation signerInformation = (SignerInformation) signersIterator.next();
                Collection certificates = certs.getCertificates(signerInformation.getSID());
                Iterator certIt = certificates.iterator();
                X509Certificate signerCertificate = (X509Certificate) certIt.next();
                // And here we validate the document signature.
                if (signerInformation.verify(signerCertificate.getPublicKey(), (String) null)) {
                    System.out.println("PDF signature verification is correct.");
                    // IMPORTANT: Note that you should usually validate the signing certificate in
                    // this phase, e.g. trust, validity, revocation, etc. See
                    // http://www.nakov.com/blog/2009/12/01/x509-certificate-validation-in-java-build-and-verify-chain-and-verify-clr-with-bouncy-castle/.
                } else {
                    System.out.println("PDF signature verification failed.");
                }
            }
        }
    }
    
    private static void saveBytesToFile(byte[] content, String fname) throws IOException {
        
        FileOutputStream fos = new FileOutputStream(fname);
        fos.write(content);
        fos.close();
    }
}
