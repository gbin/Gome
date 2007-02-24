/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome.io;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.rms.RecordStore;
import javax.microedition.rms.RecordStoreException;

import com.indigonauts.gome.sgf.SgfModel;

public class GamesIOManager extends IOManager {
    private static final String SGF = ".sgf"; //$NON-NLS-1$

    private static final String INDEX_NAME = "index.txt"; //$NON-NLS-1$

    public static Vector getAllLocallyAccessibleGamesList() throws IOException {
        Vector answer = getLocalGamesList();
        Vector others = getRootBundledGamesList();
        Enumeration all = others.elements();
        while (all.hasMoreElements()) {
            answer.addElement(all.nextElement());
        }
        return answer;
    }

    public static Vector getLocalGamesList() {
        Vector answer = new Vector();
        String[] listRecordStores = RecordStore.listRecordStores();
        if (listRecordStores == null) // no record store for the midlet
            return answer;
        for (int i = 0; i < listRecordStores.length; i++) {
            String filename = listRecordStores[i];
            if (filename.toLowerCase().endsWith(SGF))
                answer.addElement(new StoreFileEntry("store:" + filename, filename)); //$NON-NLS-1$
        }
        return answer;

    }

    public static Vector getRootBundledGamesList() throws IOException {
        return singleton.getFileList("jar:/" + INDEX_NAME, null); //$NON-NLS-1$
    }

    public static void saveLocalGame(String fileName, SgfModel gameToSave) throws RecordStoreException {
        if (!fileName.toLowerCase().endsWith(SGF))
            fileName += SGF;
        String gameStr = gameToSave.toString();
        byte[] game = gameStr.getBytes();
        singleton.saveLocalStore(fileName, game);
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
    public static SgfModel extractGameFromCollection(String url, int gameIndex, DownloadStatus status)
            throws IllegalArgumentException, IOException {
        SgfModel game = null;
        DataInputStream readFileAsStream = null;
        InputStreamReader inputStreamReader = null;
        try {
            readFileAsStream = singleton.readFileAsStream(url, status);
            inputStreamReader = new InputStreamReader(readFileAsStream);
            for (int i = 1; i < gameIndex; i++) {
                if (!skipAGame(inputStreamReader))
                    return null;

                status.setPercent((i * 100) / gameIndex);
            }
            game = SgfModel.parse(inputStreamReader);

        } finally {
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

    private static boolean skipAGame(InputStreamReader is) throws IOException {
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

    // private static StringBuffer postProcessAGame(InputStreamReader is,
    // DownloadStatus status) throws IOException {
    // long begin = System.currentTimeMillis();
    //        
    // System.out.println("PostProcess " + begin );
    // boolean ignoreinside = false;
    // // skip garbage before the games
    // char c;
    // do {
    // c = (char) is.read();
    // } while (c != '(' && c != -1);
    //
    // StringBuffer game = new StringBuffer("("); //$NON-NLS-1$
    // int level = 1;
    //
    // do {
    // c = (char) is.read();
    // if (c != '\n' && c != '\r')
    // game.append(c);
    // if (c == '[')
    // ignoreinside = true;
    // if (c == ']')
    // ignoreinside = false;
    //
    // while (c == '\\') {
    // c = (char) is.read(); // ignore the caracter after an escape
    // // in the
    // // comments for example
    // game.append(c);
    // c = (char) is.read();
    // game.append(c);
    // }
    // if (!ignoreinside) {
    // if (c == '(')
    // level++;
    // else if (c == ')')
    // level--;
    // }
    // } while (level != 0 && c != -1);
    // System.out.println("PostProcessTime " + (System.currentTimeMillis() -
    // begin ));
    // return game;
    // }

}
