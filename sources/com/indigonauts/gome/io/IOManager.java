/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome.io;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
import javax.microedition.lcdui.AlertType;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotFoundException;

import com.indigonauts.gome.Gome;
import com.indigonauts.gome.common.StringVector;
import com.indigonauts.gome.common.Util;

public class IOManager {
  //#ifdef DEBUG
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("IOManager");
  //#endif

  protected static IOManager singleton = new IOManager();

  private static final String EMAIL_SEND_BASE = "http://www.indigonauts.com/gome/uploadGame.php?";
  private static final int BUFF_SIZE = 1024;

  private HttpConnection currentHttpConnection;
  private static char[] map1 = new char[64];
  static {
    int i = 0;
    for (char c = 'A'; c <= 'Z'; c++) {
      map1[i++] = c;
    }
    for (char c = 'a'; c <= 'z'; c++) {
      map1[i++] = c;
    }
    for (char c = '0'; c <= '9'; c++) {
      map1[i++] = c;
    }
    map1[i++] = '+';
    map1[i++] = '/';
  }

  public static String base64Encode(byte[] in) {
    int iLen = in.length;
    int oDataLen = (iLen * 4 + 2) / 3;// output length without padding
    int oLen = ((iLen + 2) / 3) * 4;// output length including padding
    char[] out = new char[oLen];
    int ip = 0;
    int op = 0;
    int i0;
    int i1;
    int i2;
    int o0;
    int o1;
    int o2;
    int o3;
    while (ip < iLen) {
      i0 = in[ip++] & 0xff;
      i1 = ip < iLen ? in[ip++] & 0xff : 0;
      i2 = ip < iLen ? in[ip++] & 0xff : 0;
      o0 = i0 >>> 2;
      o1 = ((i0 & 3) << 4) | (i1 >>> 4);
      o2 = ((i1 & 0xf) << 2) | (i2 >>> 6);
      o3 = i2 & 0x3F;
      out[op++] = map1[o0];
      out[op++] = map1[o1];
      out[op] = op < oDataLen ? map1[o2] : '=';
      op++;
      out[op] = op < oDataLen ? map1[o3] : '=';
      op++;
    }
    return new String(out);
  }

  public static IOManager getSingleton() {
    return singleton;
  }

  private DataInputStream readBundledFile(String filename) throws IOException {
    InputStream resourceAsStream = getClass().getResourceAsStream(filename);
    if (resourceAsStream == null) {
      throw new IOException(Gome.singleton.bundle.getString("ui.noBundle"));
    }
    return new DataInputStream(resourceAsStream);
  }

  public DataInputStream readFileFromHttp(String url, DownloadStatus status) throws IOException {
    if (currentHttpConnection != null) {
      currentHttpConnection.close();
    }
    DataInputStream is = null;
    String ident = null;
    String pwdFile = null;

    while (true) {
      currentHttpConnection = (HttpConnection) Connector.open(url);
      pwdFile = currentHttpConnection.getHost() + ".pwd";
      try {
        ident = new String(loadLocalStore(pwdFile, null));
      } catch (RecordStoreException e1) {
        //#ifdef DEBUG
        log.debug("No ident is stored for " + pwdFile);
        //#endif
      }
      currentHttpConnection.setRequestProperty("Cache-Control", "no-store"); //$NON-NLS-1$ //$NON-NLS-2$
      currentHttpConnection.setRequestProperty("Pragma", "no-cache"); //$NON-NLS-1$ //$NON-NLS-2$
      if (ident != null) {
        currentHttpConnection.setRequestProperty("Authorization", "Basic " + ident);
      }

      is = currentHttpConnection.openDataInputStream();
      if (currentHttpConnection.getResponseCode() == 401 && currentHttpConnection.getHeaderField("WWW-Authenticate").startsWith("Basic")) {
        status.requestLoginPassword();
        synchronized (status) {
          try {
            status.wait();
            ident = base64Encode((status.getLogin() + ":" + status.getPassword()).getBytes());
            currentHttpConnection.close();
          } catch (InterruptedException e) {
            break;
          }
        }
      } else
        break;
    }

    if (ident != null) {
      try {
        saveLocalStore(pwdFile, ident.getBytes());
      } catch (RecordStoreException e) {
        // ignores it, too bad the paswrod will not be stored
      }

    }

    return is;
  }

  public static void sendFileByMail(FileEntry selectedFile, String email) {
    String url = selectedFile.getPath();
    String filename = url.substring(url.indexOf(':') + 1);
    postStoreFileToHttp(filename, EMAIL_SEND_BASE + "email=" + URLEncode(email) + "&game=" + filename);

  }

  public static void postStoreFileToHttp(final String filename, final String urlWithParams) {
    Runnable send = new Runnable() {
      public void run() {
        DataOutputStream os = null;
        try {
          HttpConnection httpConnection = (HttpConnection) Connector.open(urlWithParams, Connector.READ_WRITE);
          httpConnection.setRequestMethod(HttpConnection.POST);
          httpConnection.setRequestProperty("Cache-Control", "no-store"); //$NON-NLS-1$ //$NON-NLS-2$
          httpConnection.setRequestProperty("Pragma", "no-cache"); //$NON-NLS-1$ //$NON-NLS-2$
          httpConnection.setRequestProperty("Content-Type", "application/x-go-sgf");
          os = httpConnection.openDataOutputStream();
          byte[] game = loadLocalStore(filename, null);
          int l = game.length;
          int i = 0;
          for (; i < l - BUFF_SIZE; i += BUFF_SIZE) {
            os.write(game, i, BUFF_SIZE);
          }
          os.write(game, i, l - i);

        } catch (RecordStoreException e) {

          Util.messageBox(Gome.singleton.bundle.getString("ui.error"), Gome.singleton.bundle.getString("ui.error.recordStored"), AlertType.ERROR); //$NON-NLS-1$ //$NON-NLS-2$
          return;

        } catch (IOException e) {
          Util.messageBox(Gome.singleton.bundle.getString("ui.error"), Gome.singleton.bundle.getString("ui.error.posting"), AlertType.ERROR); //$NON-NLS-1$ //$NON-NLS-2$
          return;

        } finally {
          if (os != null)
            try {
              os.close();
            } catch (IOException e) {
              e.printStackTrace();
            }
        }
        Util.messageBox(Gome.singleton.bundle.getString("ui.success"), Gome.singleton.bundle.getString("ui.email.success"), AlertType.INFO); //$NON-NLS-1$ //$NON-NLS-2$

      }
    };
    Thread t = new Thread(send);
    t.start();
  }

  public DataInputStream readFromLocalStore(String fileName) throws IOException {
    try {
      ByteArrayInputStream di = new ByteArrayInputStream(loadLocalStore(fileName, null));

      return new DataInputStream(di);
    } catch (Exception e) {
      throw new IOException(e.getMessage());
    }
  }

  public static byte[] loadLocalStore(String fileName, DownloadStatus status) throws RecordStoreException {
    RecordStore rs = RecordStore.openRecordStore(fileName, false);
    if (status != null)
      status.setPercent(50);
    byte[] result = rs.getRecord(1);
    if (status != null)
      status.setPercent(90);
    rs.closeRecordStore();
    if (status != null)
      status.setPercent(100);
    return result;
  }

  public static String URLEncode(String s) {
    StringBuffer tmp = new StringBuffer();
    int length = s.length();
    for (int i = 0; i < length; i++) {
      char b = s.charAt(i);
      if ((b >= (char) 0x30 && b <= (char) 0x39) || (b >= (char) 0x41 && b <= (char) 0x5A) || (b >= (char) 0x61 && b <= (char) 0x7A)) {
        tmp.append(b);
      } else {
        tmp.append('%');
        if (b <= (char) 0xf)
          tmp.append('0');
        tmp.append(Integer.toHexString(b));
      }
    }
    return tmp.toString();
  }

  public void saveLocalStore(String fileName, byte[] content) throws RecordStoreException {
    try {
      deleteLocalStore(fileName);
    } catch (RecordStoreNotFoundException rnfe) {
      // ignore
    }

    RecordStore rs = RecordStore.openRecordStore(fileName, true);
    rs.addRecord(content, 0, content.length);
    rs.closeRecordStore();
  }

  public void deleteLocalStore(String url) throws RecordStoreException {
    RecordStore.deleteRecordStore(url.substring(url.indexOf(':') + 1));
  }

  public Vector getFileEntriesFromIndex(String indexFile) {
    //#ifdef DEBUG
    log.debug("getFileEntriesFromIndex :" + indexFile);
    //#endif
    Vector answer = new Vector();
    StringVector list = new StringVector(indexFile, '\n');
    Enumeration elements = list.elements();
    String rep = ((String) elements.nextElement()).trim();
    while (elements.hasMoreElements()) {
      String triplet = (String) elements.nextElement();
      if (triplet.trim().length() == 0)
        continue;
      StringVector fileEntry = new StringVector(triplet, ',');

      String name = rep + fileEntry.stringElementAt(0).trim();
      char mode = fileEntry.stringElementAt(1).trim().charAt(0);
      String description;
      if (fileEntry.size() > 2)
        description = fileEntry.stringElementAt(2);
      else
        description = fileEntry.stringElementAt(0); // if there is no
      FileEntry entry;

      if (mode == 'I') { //$NON-NLS-1$
        if (fileEntry.size() > 3) {
          //#ifdef DEBUG
          log.debug("retreive the illustrative area");
          //#endif
          String boardArea = fileEntry.stringElementAt(3).trim();
          //#ifdef DEBUG
          log.debug("boardArea =" + boardArea);
          //#endif
          String black = fileEntry.stringElementAt(4).trim();
          //#ifdef DEBUG
          log.debug("black =" + black);
          //#endif
          String white = fileEntry.stringElementAt(5).trim();
          //#ifdef DEBUG
          log.debug("white =" + white);
          //#endif
          entry = new IndexEntry(name, description, boardArea, black, white);
        } else {
          entry = new IndexEntry(name, description);
        }

      } else {
        int collectionSize = 1; // default, it is not a collection

        if (fileEntry.size() > 3) {
          String collSize = fileEntry.stringElementAt(3).trim();
          if (collSize.length() != 0)
            collectionSize = Integer.parseInt(collSize);
        }
        if (name.startsWith("http://")) { //$NON-NLS-1$
          entry = new URLFileEntry(name, mode, description, collectionSize);
        } else {
          if (fileEntry.size() > 4) {
            String boardArea = fileEntry.stringElementAt(4).trim();
            String black = fileEntry.stringElementAt(5).trim();
            String white = fileEntry.stringElementAt(6).trim();
            entry = new BundledFileEntry(name, mode, description, collectionSize, boardArea, black, white);
          } else {
            entry = new BundledFileEntry(name, mode, description, collectionSize);
          }
        }
      }
      answer.addElement(entry);
    }
    //#ifdef DEBUG
    log.debug("returned " + answer + " elements");
    //#endif
    return answer;
  }

  public Vector getFileList(String url, DownloadStatus status) throws IOException {
    byte[] data = loadFile(url, status);
    return getFileEntriesFromIndex(new String(data));
  }

  public byte[] loadFile(String url, DownloadStatus status) throws IOException {
    if (url.startsWith("store:")) { //$NON-NLS-1$
      url = url.substring(url.indexOf(":") + 1); //$NON-NLS-1$
      try {
        return loadLocalStore(url, status);
      } catch (RecordStoreException e) {
        throw new IOException("ui.error.recordStore"); //$NON-NLS-1$
      }
    }
    int bidon = 0;

    DataInputStream dis = null;
    ByteArrayOutputStream baos = new ByteArrayOutputStream(200);

    try {
      InputStream is = readFileAsStream(url, status);
      dis = new DataInputStream(is);
      byte[] buffer = new byte[512];
      int nbread = 0;
      nbread = dis.read(buffer);

      while (nbread != -1) {
        if (status != null) {
          bidon = (bidon + 10) % 100;
          status.setPercent(bidon);
        }
        baos.write(buffer, 0, nbread);
        nbread = dis.read(buffer);
      }

      baos.close();
    } finally {
      try {
        if (dis != null) {
          dis.close();
        }
      } catch (IOException e) {
        e.printStackTrace();

      }
      try {
        if (currentHttpConnection != null) {
          currentHttpConnection.close();
          currentHttpConnection = null;

        }

      } catch (IOException e) {
        e.printStackTrace();

      }
    }

    return baos.toByteArray();
  }

  public DataInputStream readFileAsStream(String url, DownloadStatus status) throws IOException {
    if (url.startsWith("http://")) { //$NON-NLS-1$
      return readFileFromHttp(url, status);
    } else if (url.startsWith("store:")) { //$NON-NLS-1$
      url = url.substring(url.indexOf(':') + 1);
      return readFromLocalStore(url);

    }
    url = url.substring(url.indexOf(':') + 1);
    return readBundledFile(url);
  }
}
