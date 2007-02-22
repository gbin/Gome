/*
 * (c) 2006 Indigonauts
 */

package com.indigonauts.gome;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.lcdui.Display;
import javax.microedition.midlet.MIDlet;
import javax.microedition.rms.RecordStoreException;

import com.indigonauts.gome.common.ResourceBundle;
import com.indigonauts.gome.io.IOManager;
import com.indigonauts.gome.ui.GameController;
import com.indigonauts.gome.ui.MenuEngine;

public class Gome extends MIDlet {
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("Gome");

    public static final String VERSION = "1.1"; //$NON-NLS-1$

    private static final String OPTIONS_FILE = VERSION + ".opt"; //$NON-NLS-1$

    public ResourceBundle bundle;

    public MenuEngine menuEngine;

    public GameController gameController;

    public MainCanvas mainCanvas;

    public Display display;

    public static Gome singleton;

    public GomeOptions options;

    public Gome() {
        singleton = this;
    }

    void loadOptions() throws IOException {
        DataInputStream input = null;
        try {
            input = IOManager.getSingleton().readFromLocalStore(OPTIONS_FILE);
            options = new GomeOptions(input);
        } finally {
            if (input != null)
                input.close();
        }
    }

    public void saveOptions() throws RecordStoreException, IOException {
        ByteArrayOutputStream outba = new ByteArrayOutputStream();
        DataOutputStream os = new DataOutputStream(outba);
        options.marshalOut(os);
        IOManager.getSingleton().saveLocalStore(OPTIONS_FILE, outba.toByteArray());
        os.close();
    }

    public void startApp() {

        log.info("Application start");

        if (display == null) {
            display = Display.getDisplay(this);
            Loader splash = new Loader();
            display.setCurrent(splash);

            Thread t = new Thread(splash);
            t.start();
            splash.serviceRepaints();
        }
    }

    public void pauseApp() {
        if (mainCanvas != null)
            mainCanvas.stopScroller(); // don't use any CPU.

    }

    public void destroyApp(boolean unconditional) {
        // nothing
    }

    public void exit() {
        destroyApp(false);
        notifyDestroyed();
    }
}
