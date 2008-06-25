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

import com.indigonauts.gome.common.Util;
import com.indigonauts.gome.i18n.I18N;
import com.indigonauts.gome.io.IOManager;
import com.indigonauts.gome.ui.GameController;
import com.indigonauts.gome.ui.MenuEngine;
import com.indigonauts.gome.ui.Options;

//#if BT
import com.indigonauts.gome.multiplayer.bt.BluetoothServiceConnector;

//#endif

public class Gome extends MIDlet implements CommandListener {
  //#if DEBUG
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("Gome");
  //#endif

  //#expand public static final String VERSION = "%VERSION%";
  public static final String VERSION = "DEV";
  //#expand public static final String LOCALE = "%LOCALE%";
  public static final String LOCALE = "en_US";

  private static final String OPTIONS_FILE = VERSION + ".o"; //$NON-NLS-1$
  private static long HOUR = 60 * 60 * 1000L;
  private static long EXPIRATION_PERIOD = 48 * HOUR;

  public MenuEngine menuEngine;

  public GameController gameController;

  public MainCanvas mainCanvas;

  public Display display;

  public static Gome singleton;

  public GomeOptions options;

  private Options optionsForm;

  public Gome() {
    super();
    singleton = this;
    display = Display.getDisplay(this);
  }

  void loadOptions() throws IOException {
    //#if DEBUG
    log.info("Load options");
    //#endif
    DataInputStream input = null;
    try {
      input = IOManager.singleton.readFromLocalStore(OPTIONS_FILE);
      options = new GomeOptions(input);
      //#if DEBUG
      log.info("Load options OK");
      //#endif

    } finally {
      if (input != null)
        input.close();
    }
  }

  public void saveOptions() throws RecordStoreException, IOException {
    ByteArrayOutputStream outba = new ByteArrayOutputStream();
    DataOutputStream os = new DataOutputStream(outba);
    options.marshalOut(os);
    IOManager.singleton.saveLocalStore(OPTIONS_FILE, outba.toByteArray());
    os.close();
  }

  public void startApp() {

    //#if DEBUG
    log.info("Application (re)start");
    //#endif
    try {
      if (gameController == null) {
        try {
          loadOptions();
        } catch (Throwable t) {
          options = new GomeOptions();
        }
        if (!checkLicense())
          return;
        bootGome();
      } else {
        //#if DEBUG
        log.info("This is just a restart");
        //#endif
      }
    } catch (Throwable t) {
      //#if DEBUG
      log.error("Load error", t);
      //#endif
      Util.errorNotifier(t);
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
    //#if DEBUG
    log.debug("Check License");
    //#endif
    try {
      if (options.user.length() == 0 || !Util.keygen(options.user).equals(options.key)) {
        //#if DEBUG
        log.debug("Current expiration date = " + Gome.singleton.options.expiration);
        log.debug("Current time = " + System.currentTimeMillis());
        //#endif

        if (options.expiration != 0 && System.currentTimeMillis() > options.expiration) {
          //#if DEBUG
          log.debug("License EXPIRED");
          //#endif

          Alert al = new Alert(I18N.expired, I18N.expiredExplanation, null, AlertType.ERROR);
          al.setTimeout(Alert.FOREVER);
          optionsForm = new Options(I18N.options, this, true);
          display.setCurrent(al, optionsForm);
          return false;
        }

        if (Gome.singleton.options.expiration == 0) {
          options.expiration = System.currentTimeMillis() + EXPIRATION_PERIOD;
          saveOptions();
          //#if DEBUG
          log.debug("Initiated new license period");
          //#endif

        }
      }
      //#if DEBUG
      else {
        log.info("Software licensed to " + Gome.singleton.options.user);
      }
      //#endif
      return true;
    } catch (Throwable t) {
      Util.errorNotifier(t);
      return false;
    }

  }

  public void commandAction(Command command, Displayable displayable) {
    //#if DEBUG
    log.debug("commandAction for license");
    log.debug("optionsForm = " + optionsForm);
    log.debug("options = " + options);
    //#endif
    boolean save = optionsForm.save();
    if (options.user.length() == 0) {
      Util.messageBox(I18N.option.invalidKey, I18N.option.invalidKeyExplanation, AlertType.ERROR); //$NON-NLS-1$ //$NON-NLS-2$
      return;
    }
    if (save) {
      try {
        bootGome();

      } catch (Throwable t) {
        Util.errorNotifier(t);
      }

    }
  }

  //#if BT
  public BluetoothServiceConnector bluetoothServiceConnector;

  //#endif

  public void bootGome() {
    gameController = new GameController(display);
    mainCanvas = new MainCanvas(gameController);
    menuEngine = new MenuEngine(gameController);
    menuEngine.startNewGame();

    String message;
    if (options.user.length() == 0) {
      long msLeft = options.expiration - System.currentTimeMillis();
      message = Util.expandString(I18N.hoursLeft, new String[] { String.valueOf(msLeft / HOUR), String.valueOf((msLeft % HOUR) / (60 * 1000L)) });
      mainCanvas.setSplashInfo(message);
    }
    //#if BT
    if (options.bluetooth == 0) {
      //#if DEBUG
      log.debug("Bluetooth service is selected in options... start it");
      //#endif
      startBTService();
    }
    //#endif
  }

  //#if BT
  public void startBTService() {
    try {
      //#if DEBUG
      log.debug("Instantiate the bluetooth connector");
      //#endif
      bluetoothServiceConnector = new BluetoothServiceConnector(gameController);

      //#if DEBUG
      log.debug("Start BT Service");
      //#endif
      bluetoothServiceConnector.start();
    } catch (Throwable e) {
      Util.messageBox(I18N.bt.noBTAvailableTitle, I18N.bt.noBTAvailable + " (" + e.toString() + ")", AlertType.ERROR);
      bluetoothServiceConnector = null;

    }
  }
  //#endif
}
