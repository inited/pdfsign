package cz.inited.pdfsign1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Calendar;

import org.apache.pdfbox.io.RandomAccessFile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.PDSignature;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;

public class App {
    public static void main(String[] args) throws Exception {

        SignatureInterface mySigner = new BouncySigner();
        //SignatureInterface mySigner = new FileSigner();
        
        File document = new File("file.pdf");
        
        // creating output document and prepare the IO streams.
        String name = document.getName();
        String substring = name.substring(0, name.lastIndexOf("."));
        
        File outputDocument = new File(document.getParent(), substring + "_signed.pdf");
        FileInputStream fis = new FileInputStream(document);
        FileOutputStream fos = new FileOutputStream(outputDocument);
        
        byte[] buffer = new byte[8 * 1024];
        int c;
        while ((c = fis.read(buffer)) != -1) {
            fos.write(buffer, 0, c);
        }
        fis.close();
        fis = new FileInputStream(outputDocument);
        
        File scratchFile = File.createTempFile("pdfbox_scratch", ".bin");
        RandomAccessFile randomAccessFile = new RandomAccessFile(scratchFile, "rw");
        
        try {
            // load document
            PDDocument doc = PDDocument.loadNonSeq(document, randomAccessFile);
            
            // create signature dictionary
            PDSignature signature = new PDSignature();
            signature.setFilter(PDSignature.FILTER_ADOBE_PPKLITE);
            signature.setSubFilter(PDSignature.SUBFILTER_ADBE_PKCS7_DETACHED);
            signature.setName("signer name");
            signature.setLocation("signer location");
            signature.setReason("reason for signature");
            
            // datum podpisu se nesmi menit mezi jednotlivymi behy
            Calendar datumPodpisu = Calendar.getInstance();
            datumPodpisu.set(Calendar.MILLISECOND, 0);
            datumPodpisu.set(Calendar.SECOND, 0);
            datumPodpisu.set(Calendar.MINUTE, 0);
            datumPodpisu.set(Calendar.HOUR, 0);
            signature.setSignDate(datumPodpisu);
            
            // verze dokumentu se nesmi menit mezi jednotlivymi behy
            doc.setDocumentId(123L);
            
            doc.addSignature(signature, mySigner);
            doc.saveIncremental(fis, fos);
            
        } finally {
            if (scratchFile != null && scratchFile.exists() && !scratchFile.delete()) {
                scratchFile.deleteOnExit();
            }
        }
    }
}
