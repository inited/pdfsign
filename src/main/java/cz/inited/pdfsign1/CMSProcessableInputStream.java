package cz.inited.pdfsign1;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.bouncycastle.cms.CMSException;
import org.bouncycastle.cms.CMSProcessable;

public /**
 * Wrap a InputStream into a CMSProcessable object for bouncy castle. It's an
 * alternative to the CMSProcessableByteArray.
 * 
 * @author Thomas Chojecki
 * 
 */
class CMSProcessableInputStream implements CMSProcessable
{

  InputStream in;

  public CMSProcessableInputStream(InputStream is)
  {
    in = is;
  }

  public Object getContent()
  {
    return in;
  }

  public void write(OutputStream out) throws IOException, CMSException
  {
    // read the content only one time
    byte[] buffer = new byte[8 * 1024];
    int read;
    while ((read = in.read(buffer)) != -1)
    {
      out.write(buffer, 0, read);
    }
    in.close();
  }
}
