package cz.inited.pdfsign1;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.apache.pdfbox.exceptions.SignatureException;
import org.apache.pdfbox.pdmodel.interactive.digitalsignature.SignatureInterface;

public class FileSigner implements SignatureInterface {
    
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
        
        //
        // tady se provede podepsani u klienta
        //
        
        // nacti novy podpis
        File file = new File("signature.bin");
        long length = file.length();
        
        // Create the byte array to hold the data
        byte[] bytes = new byte[(int) length];
        
        // Read in the bytes
        int offset = 0;
        int numRead = 0;
        
        InputStream is = new FileInputStream(file);
        try {
            while (offset < bytes.length
                    && (numRead = is.read(bytes, offset, bytes.length - offset)) >= 0) {
                offset += numRead;
            }
        } finally {
            is.close();
        }
        
        // Ensure all the bytes have been read in
        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }
        return bytes;
        
    }
    
}
