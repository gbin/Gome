/*
 * (c) 2006 Indigonauts
 */

package com.indigonauts.gome;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.midlet.MIDlet;
import javax.microedition.rms.RecordStoreException;

import com.indigonauts.gome.common.ResourceBundle;
import com.indigonauts.gome.common.Util;
import com.indigonauts.gome.io.IOManager;
import com.indigonauts.gome.ui.GameController;
import com.indigonauts.gome.ui.MenuEngine;
import com.indigonauts.gome.ui.Options;

public class Gome extends MIDlet implements CommandListener, Runnable {
  //#ifdef DEBUG
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("Gome");
  //#endif

  public static final String VERSION = "1.2"; //$NON-NLS-1$

  private static final String OPTIONS_FILE = VERSION + ".o"; //$NON-NLS-1$
  private static long HOUR = 60 * 60 * 1000L;
  private static long EXPIRATION_PERIOD = 48 * HOUR;

  public ResourceBundle bundle;

  public MenuEngine menuEngine;

  public GameController gameController;

  public MainCanvas mainCanvas;

  public Display display;

  public static Gome singleton;

  public GomeOptions options;

  private Options optionsForm;

  public Gome() {
    singleton = this;
  }

  void loadOptions() throws IOException {
    //#ifdef DEBUG
    log.info("Load options");
    //#endif
    DataInputStream input = null;
    try {
      input = IOManager.singleton.readFromLocalStore(OPTIONS_FILE);
      options = new GomeOptions(input);
      //#ifdef DEBUG
      log.info("Load options OK");
      //#endif

    } finally {
      if (input != null)
        input.close();
    }
  }

  public void saveOptions() throws RecordStoreException, IOException {
    //#ifdef DEBUG
    //# log.info("Save options");
    //#endif
    ByteArrayOutputStream outba = new ByteArrayOutputStream();
    DataOutputStream os = new DataOutputStream(outba);
    options.marshalOut(os);
    IOManager.singleton.saveLocalStore(OPTIONS_FILE, outba.toByteArray());
    os.close();
    //#ifdef DEBUG
    //# log.info("Save options done OK");
    //#endif
  }

  public void startApp() {
    //#ifdef DEBUG
    log.info("Application start");
    //#endif

    if (display == null) {
      display = Display.getDisplay(this);

      Loader splash = new Loader();

      display.setCurrent(splash);

      Thread t = new Thread(this);
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

  public boolean checkLicense() {
    //#ifdef DEBUG
    log.debug("Check License");
    //#endif
    try {
      if (options.user.length() == 0 || !Util.keygen(options.user).equals(options.key)) {
        //#ifdef DEBUG
        log.debug("Current expiration date = " + Gome.singleton.options.expiration);
        log.debug("Current time = " + System.currentTimeMillis());
        //#endif

        if (options.expiration != 0 && System.currentTimeMillis() > options.expiration) {
          //#ifdef DEBUG
          log.debug("License EXPIRED");
          //#endif

          Alert al = new Alert(bundle.getString("ui.expired"), bundle.getString("ui.expiredExplanation"), null, AlertType.ERROR);
          al.setTimeout(Alert.FOREVER);
          optionsForm = new Options(Gome.singleton.bundle.getString("ui.options"), this, true);
          display.setCurrent(al, optionsForm);
          return false;
        }

        if (Gome.singleton.options.expiration == 0) {
          options.expiration = System.currentTimeMillis() + EXPIRATION_PERIOD;
          saveOptions();
          //#ifdef DEBUG
          log.debug("Initiated new license period");
          //#endif

        }
      }
      //#ifdef DEBUG
      else {
        log.info("Software licensed to " + Gome.singleton.options.user);
      }
      //#endif
      return true;
    } catch (Throwable t) {
      Util.messageBox(Gome.singleton.bundle.getString("ui.error"), t.getMessage() + ", " + t.toString(), AlertType.ERROR);
      return false;
    }

  }

  public void commandAction(Command command, Displayable displayable) {
    //#ifdef DEBUG
    log.debug("commandAction for license");
    log.debug("optionsForm = " + optionsForm);
    log.debug("options = " + options);
    //#endif
    boolean save = optionsForm.save();
    if (options.user.length() == 0) {
      Util.messageBox(bundle.getString("ui.option.invalidKey"), bundle.getString("ui.option.invalidKeyExplanation"), AlertType.ERROR); //$NON-NLS-1$ //$NON-NLS-2$
      return;
    }
    if (save) {
      try {
        bootGome();

      } catch (Throwable t) {
        Util.messageBox(bundle.getString("ui.error"), t.getMessage() + ", " + t.toString(), AlertType.ERROR); //$NON-NLS-1$ //$NON-NLS-2$
      }

    }
  }

  private void bootGome() {
    if (gameController == null)
      gameController = new GameController(display);
    if (mainCanvas == null)
      mainCanvas = new MainCanvas();
    if (menuEngine == null) {
      menuEngine = new MenuEngine();
    }
    menuEngine.startNewGame();
    String message;
    if (options.user.length() != 0) {
      message = bundle.getString("ui.registeredTo", new String[] { Gome.singleton.options.user });
    } else {
      long msLeft = options.expiration - System.currentTimeMillis();

      message = bundle.getString("ui.hoursLeft", new String[] { String.valueOf(msLeft / HOUR), String.valueOf((msLeft % HOUR) / (60 * 1000L)) });
    }

    mainCanvas.setSplashInfo(message);
  }

  public void run() {
    try {
      try {
        loadOptions();
      } catch (Throwable t) {
        options = new GomeOptions();
      }
      bundle = new ResourceBundle("main", Gome.singleton.options.locale); //$NON-NLS-1$
      if (!checkLicense())
        return;
      bootGome();
    } catch (Throwable t) {
      //#ifdef DEBUG
      log.error("Load error", t);
      t.printStackTrace();
      //#endif
      Util.messageBox(Gome.singleton.bundle.getString("ui.error"), t.getMessage() + ", " + t.toString(), AlertType.ERROR); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

}
