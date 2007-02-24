/*
 * (c) 2006 Indigonauts
 */

package com.indigonauts.gome.common;

import java.io.InputStream;
import java.util.Hashtable;

public class ResourceBundle {
    private Hashtable map = new Hashtable();
    private static final String I18N_PACKAGE = "/com/indigonauts/gome/i18n/"; //$NON-NLS-1$
    //#ifdef DEBUG
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("ResourceBundle");
    //#endif

    public ResourceBundle(final String fileName, String locale) {
        InputStream in = null;
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

        byte c;
        StringBuffer line = new StringBuffer();

        try {
            while ((c = (byte) in.read()) != -1) {
                if ((c != '\n') && (c != '\r')) {
                    line.append(new String(new byte[] { c }));
                } else {
                    parseAndStoreLine(line);
                    line = new StringBuffer();
                }
            }
            // do eventually a remaining line at the end of the file
            parseAndStoreLine(line);
        } catch (Throwable t) {
          //#ifdef DEBUG  
          log.error(t);
          //#endif
        }

    }

    private void parseAndStoreLine(StringBuffer line) {
        String lineStr = line.toString();
        int equalIndex = lineStr.indexOf("="); //$NON-NLS-1$
        if (equalIndex == -1)
            return;

        String key = lineStr.substring(0, equalIndex).trim();
        String message = lineStr.substring(equalIndex + 1, lineStr.length());

        this.map.put(key, message);
    }
    
    public String getString(String key, String[] args){
        String originalMessage = getString(key);
        StringBuffer newMessage = new StringBuffer();        
        
        for (int i = 0; i < args.length;i++){
            String k = "%" + i;
            int index = originalMessage.indexOf(k);
            if (index == -1)
                break;
            
            String token = originalMessage.substring(0,index);
            originalMessage = originalMessage.substring(index+2,originalMessage.length());
            newMessage.append(token+ args[i]);
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
        return ((String)object).replace('|','\n').trim();
    }
}
