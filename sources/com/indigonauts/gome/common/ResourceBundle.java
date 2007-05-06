/*
 * (c) 2006 Indigonauts
 */

package com.indigonauts.gome.common;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.util.Hashtable;

public class ResourceBundle {
  private Hashtable map = new Hashtable();
  private static final String I18N_PACKAGE = "/com/indigonauts/gome/i18n/"; //$NON-NLS-1$
  //#ifdef DEBUG
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("ResourceBundle");

  //#endif

  public ResourceBundle(final String fileName, String locale) throws UnsupportedEncodingException {
    InputStream in = null;
    Reader reader;
    StringBuffer completeFileName = new StringBuffer();
    completeFileName.append(I18N_PACKAGE);
    completeFileName.append(fileName);
    completeFileName.append("_"); //$NON-NLS-1$
    completeFileName.append(locale);
    completeFileName.append(".properties"); //$NON-NLS-1$

    String path = completeFileName.toString();
    in = this.getClass().getResourceAsStream(path);

    if (in == null) {

      String x = I18N_PACKAGE + fileName + "_en_US.properties"; //$NON-NLS-1$
      in = this.getClass().getResourceAsStream(x);
    }

    if (in == null) {
      return;
    }
    try {
      reader = new InputStreamReader(in, "UTF-8");
    } catch (Throwable t) {
      // utf8 not supported, it will be weird for some chrs but at least it will work in english
      reader = new InputStreamReader(in);
    }

    char c;
    int ct;
    StringBuffer line = new StringBuffer();

    try {

      while ((ct = reader.read()) != -1) {
        c = (char) ct;
        if ((c != '\n')) {
          line.append(c);
        } else {
          parseAndStoreLine(line);
          line = new StringBuffer();
        }
      }

      // do eventually a remaining line at the end of the file
      parseAndStoreLine(line);
      reader.close();
    } catch (Throwable t) {
      //#ifdef DEBUG  
      log.error(t);
      //#endif
    }

  }

  private void parseAndStoreLine(StringBuffer line) {
    String lineStr = line.toString();
    //#ifdef DEBUG
    //# log.debug("Read bundle line " + line);
    //#endif
    int equalIndex = lineStr.indexOf("="); //$NON-NLS-1$
    if (equalIndex == -1)
      return;

    String key = lineStr.substring(0, equalIndex).trim();
    String message = lineStr.substring(equalIndex + 1, lineStr.length());

    this.map.put(key, message);
  }

  public String getString(String key, String[] args) {
    String originalMessage = getString(key);
    StringBuffer newMessage = new StringBuffer();

    for (int i = 0; i < args.length; i++) {
      String k = "%" + i;
      int index = originalMessage.indexOf(k);
      if (index == -1)
        break;

      String token = originalMessage.substring(0, index);
      originalMessage = originalMessage.substring(index + 2, originalMessage.length());
      newMessage.append(token + args[i]);
    }
    newMessage.append(originalMessage);
    return newMessage.toString();
  }

  public String getString(String key) {
    Object object = null;
    try {
      object = map.get(key);
    } catch (Throwable t) {
      return key;
    }
    if (object == null)
      return key;
    return ((String) object).replace('|', '\n').trim();
  }
}
