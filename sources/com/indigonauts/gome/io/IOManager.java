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
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;
//#ifdef JSR75
import javax.microedition.io.file.FileConnection;
import javax.microedition.io.file.FileSystemRegistry;
//#endif
import javax.microedition.lcdui.AlertType;
import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;
import javax.microedition.rms.RecordStoreNotFoundException;

import com.indigonauts.gome.Gome;
import com.indigonauts.gome.common.StringVector;
import com.indigonauts.gome.common.Util;
import com.indigonauts.gome.sgf.SgfModel;

public class IOManager {
  //#ifdef DEBUG
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("IOManager");
  //#endif
  private static final String SGF = ".sgf"; //$NON-NLS-1$

  private static final String INDEX_NAME = "index.txt"; //$NON-NLS-1$

  public static final String LOCAL_NAME = "file:///"; //$NON-NLS-1$

  public static IOManager singleton = new IOManager();

  private static final String EMAIL_SEND_BASE = "http://www.indigonauts.com/gome/uploadGame.php?";
  private static final int BUFF_SIZE = 1024;

  private static char[] map1;

  private static void initMap() {
    map1 = new char[64];
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
    if (map1 == null)
      initMap();
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

  private DataInputStream readBundledFile(String filename) throws IOException {
    InputStream resourceAsStream = getClass().getResourceAsStream(filename);
    if (resourceAsStream == null) {
      throw new IOException(Gome.singleton.bundle.getString("ui.noBundle"));
    }
    return new DataInputStream(resourceAsStream);
  }

  /*
   * if this returns null, we should ignore it, it is supposed to be recalled (for example with a login/passwd correctly set
   */
  public DataInputStream readFileFromHttp(String url, DownloadStatus status) throws IOException {
    //#ifdef DEBUG
    log.debug("readFileFromHttp " + url);
    //#endif

    DataInputStream is = null;
    String ident = null;
    String pwdFile = null;

    HttpConnection currentHttpConnection = (HttpConnection) Connector.open(url);
    //#ifdef DEBUG
    log.debug("opened");
    //#endif

    pwdFile = currentHttpConnection.getHost() + ".pwd";

    try {
      if (ident == null)
        ident = new String(loadLocalStore(pwdFile, null));
    } catch (RecordStoreException e1) {
      //#ifdef DEBUG
      log.debug("No ident is stored for " + pwdFile);
      //#endif
    }
    currentHttpConnection.setRequestProperty("Cache-Control", "no-store"); //$NON-NLS-1$ //$NON-NLS-2$
    currentHttpConnection.setRequestProperty("Pragma", "no-cache"); //$NON-NLS-1$ //$NON-NLS-2$
    currentHttpConnection.setRequestProperty("Content-Length", "0");
    currentHttpConnection.setRequestProperty("Connection", "close");
    currentHttpConnection.setRequestProperty("Gome-Version", Gome.VERSION); //$NON-NLS-1$ //$NON-NLS-2$
    if (ident != null) {
      //#ifdef DEBUG
      log.debug("Put ident header" + ident);
      //#endif
      currentHttpConnection.setRequestProperty("Authorization", "Basic " + ident);
    }

    is = currentHttpConnection.openDataInputStream();

    if (currentHttpConnection.getResponseCode() == 401 && currentHttpConnection.getHeaderField("WWW-Authenticate").startsWith("Basic")) {

      //#ifdef DEBUG
      log.debug("401 response, try to get the login/password");
      //#endif
      status.requestLoginPassword(pwdFile);
      return null;

    } else {
      //#ifdef DEBUG
      log.debug("no ident required");
      //#endif
    }

    return is;
  }

  public void storeLoginPassword(String pwdFile, String login, String passwd) throws RecordStoreException {
    String ident = base64Encode((login + ":" + passwd).getBytes());
    saveLocalStore(pwdFile, ident.getBytes());
  }

  public void sendFileByMail(FileEntry selectedFile, String email) {
    String url = selectedFile.getUrl();
    //#ifdef DEBUG
    log.debug("Send by email url = " + url);
    //#endif
    postFileToHttp(url, EMAIL_SEND_BASE + "email=" + URLEncode(email) + "&game=" + selectedFile.getName());

  }

  public void postFileToHttp(final String url, final String urlWithParams) {
    Runnable send = new Runnable() {
      public void run() {
        DataOutputStream os = null;
        try {
          HttpConnection httpConnection = (HttpConnection) Connector.open(urlWithParams, Connector.READ_WRITE);
          httpConnection.setRequestMethod(HttpConnection.POST);
          httpConnection.setRequestProperty("Cache-Control", "no-store"); //$NON-NLS-1$ //$NON-NLS-2$
          httpConnection.setRequestProperty("Pragma", "no-cache"); //$NON-NLS-1$ //$NON-NLS-2$
          httpConnection.setRequestProperty("Content-Type", "application/x-go-sgf");
          httpConnection.setRequestProperty("Gome-Version", Gome.VERSION); //$NON-NLS-1$ //$NON-NLS-2$
          os = httpConnection.openDataOutputStream();
          byte[] game = singleton.loadFile(url, null);
          int l = game.length;
          int i = 0;
          for (; i < l - BUFF_SIZE; i += BUFF_SIZE) {
            os.write(game, i, BUFF_SIZE);
          }
          os.write(game, i, l - i);

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
    //#ifdef DEBUG
    log.debug("fileName" + fileName);
    //#endif
    try {
      deleteLocalStore(fileName);
    } catch (RecordStoreNotFoundException rnfe) {
      // ignore
    }

    RecordStore rs = RecordStore.openRecordStore(fileName, true);
    rs.addRecord(content, 0, content.length);
    rs.closeRecordStore();
  }

  public void deleteLocalStore(String name) throws RecordStoreException {
    RecordStore.deleteRecordStore(name);
  }

  public Vector getFileEntriesFromIndex(String indexFile) {
    //#ifdef DEBUG
    log.debug("getFileEntriesFromIndex :" + indexFile);
    //#endif
    Vector answer = new Vector();
    StringVector list = new StringVector(indexFile, '\n');
    Enumeration elements = list.elements();
    String path = ((String) elements.nextElement()).trim();
    while (elements.hasMoreElements()) {
      String triplet = (String) elements.nextElement();
      if (triplet.trim().length() == 0)
        continue;
      StringVector fileEntry = new StringVector(triplet, ',');

      String name = fileEntry.stringElementAt(0).trim();
      char mode = fileEntry.stringElementAt(1).trim().charAt(0);
      String description;
      if (fileEntry.size() > 2)
        description = fileEntry.stringElementAt(2).trim();
      else
        description = fileEntry.stringElementAt(0).trim(); // if there is no
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
          entry = new IndexEntry(path, name, description, boardArea, black, white);
        } else {
          entry = new IndexEntry(path, name, description);
        }

      } else {
        int collectionSize = 1; // default, it is not a collection

        if (fileEntry.size() > 3) {
          String collSize = fileEntry.stringElementAt(3).trim();
          if (collSize.length() != 0)
            collectionSize = Integer.parseInt(collSize);
        }
        if (path.startsWith("http://")) { //$NON-NLS-1$
          entry = new URLFileEntry(path, name, mode, description, collectionSize);
        } else {
          if (fileEntry.size() > 4) {
            String boardArea = fileEntry.stringElementAt(4).trim();
            String black = fileEntry.stringElementAt(5).trim();
            String white = fileEntry.stringElementAt(6).trim();
            entry = new BundledFileEntry(path, name, mode, description, collectionSize, boardArea, black, white);
          } else {
            entry = new BundledFileEntry(path, name, mode, description, collectionSize);
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
    if (data == null)
      return new Vector();
    return getFileEntriesFromIndex(new String(data));
  }

  public byte[] loadFile(String url, DownloadStatus status) throws IOException {
    int percent = 0;

    DataInputStream dis = null;
    ByteArrayOutputStream baos = new ByteArrayOutputStream(200);

    try {
      InputStream is = readFileAsStream(url, status);
      if (is == null)
        return null;
      dis = new DataInputStream(is);
      byte[] buffer = new byte[512];
      int nbread = 0;
      nbread = dis.read(buffer);

      while (nbread != -1) {
        if (status != null) {
          percent = (percent + 10) % 100;
          status.setPercent(percent);
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

    }

    return baos.toByteArray();
  }

  public DataInputStream readFileAsStream(String url, DownloadStatus status) throws IOException {
    if (url.startsWith("http://")) { //$NON-NLS-1$
      return readFileFromHttp(url, status);
    }
    //#ifdef JSR75
    else if (url.startsWith(LOCAL_NAME)) {
      return loadJSR75(url, status);
    }
    //#else
    //# else if (url.startsWith(LOCAL_NAME)) { //$NON-NLS-1$
    //#  url = url.substring(url.indexOf(':') + 4);
    //#  return readFromLocalStore(url);
    //# }
    //#endif
    url = url.substring(url.indexOf(':') + 1);
    return readBundledFile(url);
  }

  public Vector getLocalGamesList() {
    Vector answer = new Vector();
    String[] listRecordStores = RecordStore.listRecordStores();
    if (listRecordStores == null) // no record store for the midlet
      return answer;
    for (int i = 0; i < listRecordStores.length; i++) {
      String filename = listRecordStores[i];
      if (filename.toLowerCase().endsWith(SGF))
        answer.addElement(new LocalFileEntry(LOCAL_NAME + filename, null, filename)); //$NON-NLS-1$
    }
    return answer;

  }

  public Vector getRootBundledGamesList() throws IOException {
    return singleton.getFileList("jar:/" + INDEX_NAME, null); //$NON-NLS-1$
  }

  public void saveLocalGame(String fileName, SgfModel gameToSave) throws RecordStoreException {
    if (!fileName.toLowerCase().endsWith(SGF))
      fileName += SGF;
    String gameStr = gameToSave.toString();
    byte[] game = gameStr.getBytes();
    saveLocalStore(fileName, game);
  }

  /**
   * @param sfgFilename
   *            It's a composite of the packed file name and index i.e.
   *            pack\001
   * @param gameIndex
   *            0 based
   * @return null if failed.
   * @throws SgfParsingException
   * @throws IOException
   */
  public SgfModel extractGameFromCollection(String url, int gameIndex, DownloadStatus status) throws IllegalArgumentException, IOException {
    //#ifdef DEBUG
    log.debug("Load " + url);
    //#endif
    SgfModel game = null;
    DataInputStream readFileAsStream = null;
    InputStreamReader inputStreamReader = null;
    try {
      readFileAsStream = singleton.readFileAsStream(url, status);
      if (readFileAsStream == null) // it is supposed to be recalled
        return null;
      inputStreamReader = new InputStreamReader(readFileAsStream);
      for (int i = 1; i < gameIndex; i++) {
        if (!skipAGame(inputStreamReader))
          return null;

        status.setPercent((i * 100) / gameIndex);
      }
      game = SgfModel.parse(inputStreamReader);

    }
    //#ifdef DEBUG
    catch (IllegalArgumentException e) {
      int index = SgfModel.index;
      inputStreamReader = new InputStreamReader(singleton.readFileAsStream(url, status));
      inputStreamReader.skip(index - 100);
      char[] buffer = new char[100];
      char[] buffer2 = new char[100];
      log.error("SGF ERROR");
      inputStreamReader.read(buffer);
      inputStreamReader.read(buffer2);
      log.error(new String(buffer) + "|->|" + new String(buffer2));
    }
    //#endif

    finally {
      try {
        if (inputStreamReader != null) {
          inputStreamReader.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return game;
  }

  private boolean skipAGame(InputStreamReader is) throws IOException {
    int level = 1;
    // skip garbage before the games
    char c;
    do {
      c = (char) is.read();
    } while (c != '(' && c != -1);
    boolean ignoreinside = false;
    do {
      c = (char) is.read();
      if (c == '[')
        ignoreinside = true;
      if (c == ']')
        ignoreinside = false;

      while (c == '\\') {
        is.read(); // ignore the caracter after an escape in the
        // comments for example
        c = (char) is.read();
      }
      if (!ignoreinside) {
        if (c == '(')
          level++;
        else if (c == ')')
          level--;
      }
    } while (level != 0 && c != -1);
    return c != -1;
  }

  //#ifdef JSR75
  public Vector getJSR75Roots() {
    Vector roots = new Vector();
    Enumeration listRoots = FileSystemRegistry.listRoots();
    while (listRoots.hasMoreElements()) {
      String url = (String) listRoots.nextElement();
      roots.addElement(new IndexEntry(LOCAL_NAME + url, null, url));
    }
    return roots;
  }

  public Vector loadJSR75Index(String baseRep, String subRep) throws IOException {
    //String fcRep = baseRep.substring(LOCAL_NAME.length() - 1); // -1 because it needs a / at the beginning ...
    //#ifdef DEBUG
    log.debug("Base rep = " + baseRep);
    log.debug("Sub rep = " + subRep);
    //#endif
    Vector list = new Vector();
    String fullPath = baseRep + ((subRep != null) ? subRep : "");
    FileConnection fc = (FileConnection) Connector.open(fullPath);
    Enumeration content = fc.list();
    String current;
    while (content.hasMoreElements()) {
      current = (String) content.nextElement();
      if (current.endsWith("/")) {
        list.addElement(new IndexEntry(fullPath, current, current));
      } else
        list.addElement(new LocalFileEntry(fullPath, current, current));
    }
    return list;
  }

  public DataInputStream loadJSR75(String fullPath, DownloadStatus status) throws IOException {
    FileConnection fc = (FileConnection) Connector.open(fullPath);
    return fc.openDataInputStream();
  }

  byte[] game;
  String fn;
  String cd;

  public void saveJSR75(String currentDirectory, String fileName, SgfModel gameToSave) throws IOException {
    fn = fileName;
    cd = currentDirectory;
    if (!fn.toLowerCase().endsWith(SGF))
      fn += SGF;
    //#ifdef DEBUG
    log.debug("Tried to save in " + currentDirectory + fileName);
    //#endif
    String gameStr = gameToSave.toString();
    game = gameStr.getBytes();

    Thread t = new Thread() {
      public void run() {
        OutputStream outputStream = null;
        FileConnection fc = null;
        try {
          fc = (FileConnection) Connector.open(cd + fn);
          if (!fc.exists()) {
            fc.create();
          }
          outputStream = fc.openOutputStream();
          outputStream.write(game);

        } catch (IOException e) {
          //#ifdef DEBUG
          log.error(e);
          //#endif
        } finally {
          try {
            if (outputStream != null)
              outputStream.close();
            if (fc != null)
              fc.close();
            fc = null;
          } catch (IOException e) {
          }
        }

        game = null;
        fn = null;
        //#ifdef DEBUG
        log.debug("Save done");
        //#endif
      }
    };
    t.start();
  }

  public void deleteJSR75(String url) {
    fn = url;
    Thread t = new Thread() {
      public void run() {
        FileConnection fc = null;
        try {
          fc = (FileConnection) Connector.open(fn);
          if (fc.exists()) {
            fc.delete();
          }

        } catch (IOException e) {
          //#ifdef DEBUG
          log.error(e);
          //#endif
        } finally {
          try {
            if (fc != null) {
              fc.close();
              fc = null;
            }
          } catch (IOException e) {
          }
        }
      }
    };
    t.start();
  }
  //#endif
}