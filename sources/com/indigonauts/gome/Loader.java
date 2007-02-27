/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome;

import java.io.IOException;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import com.indigonauts.gome.common.ResourceBundle;
import com.indigonauts.gome.common.Util;
import com.indigonauts.gome.ui.GameController;
import com.indigonauts.gome.ui.MenuEngine;
import com.indigonauts.gome.ui.Options;

public class Loader extends Canvas implements Runnable, CommandListener {

  //#ifdef DEBUG
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("Loader");
  //#endif

  //#ifdef SPLASH
  private Image gomoku;
  //#endif
  private Options optionsForm;

  public Loader() {
    super();
    //#ifdef SPLASH
    try {
      gomoku = Image.createImage("/gomoku.png");
    } catch (IOException e) {
      e.printStackTrace();
    }
    //#endif
  }

  protected void paint(Graphics g) {
    //#ifdef SPLASH
    g.setColor(0xccccff);
    g.fillRect(0, 0, getWidth(), getHeight());
    g.drawImage(gomoku, getWidth() / 2, getHeight() / 2 - gomoku.getHeight(), Graphics.HCENTER | Graphics.VCENTER);
    g.setColor(Util.COLOR_BLACK);
    //#endif
    Font f = g.getFont();
    g.drawString("Gome v" + Gome.VERSION, getWidth() / 2, getHeight() / 2, Graphics.BASELINE | Graphics.HCENTER);
    g.drawString("(c) 2005-2007 Indigonauts", getWidth() / 2, getHeight() / 2 + f.getHeight(), Graphics.BASELINE | Graphics.HCENTER);

  }

  public void run() {
    try {
      try {
        Gome.singleton.loadOptions();
      } catch (Throwable t) {
        Gome.singleton.options = new GomeOptions();
      }
      Gome.singleton.bundle = new ResourceBundle("main", Gome.singleton.options.locale); //$NON-NLS-1$

      if (Gome.singleton.options.user.length() == 0 || !Util.keygen(Gome.singleton.options.user).equals(Gome.singleton.options.key)) {
        //#ifdef DEBUG
        log.debug("Current expiration date = " + Gome.singleton.options.expiration);
        log.debug("Current time = " + System.currentTimeMillis());
        //#endif

        if (Gome.singleton.options.expiration != 0 && System.currentTimeMillis() > Gome.singleton.options.expiration) {
          Alert al = new Alert(Gome.singleton.bundle.getString("ui.expired"), Gome.singleton.bundle.getString("ui.expiredExplanation"), null, AlertType.ERROR);
          al.setTimeout(Alert.FOREVER);
          optionsForm = new Options(Gome.singleton.bundle.getString("ui.options"), this, true);
          Gome.singleton.display.setCurrent(al, optionsForm);
          return;
        }
        if (Gome.singleton.options.expiration == 0) {
          Gome.singleton.options.expiration = System.currentTimeMillis() + 24 * 60 * 60 * 1000L;
          Gome.singleton.saveOptions();
        }
      }
      //#ifdef DEBUG
      else {
        log.info("Software licensed to " + Gome.singleton.options.user);
      }
      //#endif
      bootGome();

    } catch (Throwable t) {
      //#ifdef DEBUG
      log.error("Load error", t);
      t.printStackTrace();
      //#endif
      Util.messageBox(Gome.singleton.bundle.getString("ui.error"), t.getMessage() + ", " + t.toString(), AlertType.ERROR); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  private void bootGome() {
    if (Gome.singleton.gameController == null)
      Gome.singleton.gameController = new GameController(Gome.singleton.display);
    if (Gome.singleton.mainCanvas == null)
      Gome.singleton.mainCanvas = new MainCanvas();
    if (Gome.singleton.menuEngine == null) {
      Gome.singleton.menuEngine = new MenuEngine();
      Gome.singleton.menuEngine.startNewGame();
    }
    String message;
    if (Gome.singleton.options.user.length() != 0) {
      message = Gome.singleton.bundle.getString("ui.registeredTo", new String[] { Gome.singleton.options.user });
    } else {
      long msLeft = Gome.singleton.options.expiration - System.currentTimeMillis();
      long HOUR = 60 * 60 * 1000L;
      message = Gome.singleton.bundle.getString("ui.hoursLeft", new String[] { String.valueOf(msLeft / HOUR),String.valueOf((msLeft % HOUR)/(60*1000L))  });
    }

    Gome.singleton.mainCanvas.setSplashInfo(message);
  }

  public void commandAction(Command command, Displayable displayable) {
    boolean save = optionsForm.save();
    if (Gome.singleton.options.user.length() == 0) {
      Util.messageBox(Gome.singleton.bundle.getString("ui.option.invalidKey"), Gome.singleton.bundle.getString("ui.option.invalidKeyExplanation"), AlertType.ERROR); //$NON-NLS-1$ //$NON-NLS-2$
      return;
    }
    if (save) {
      try {
        bootGome();
      } catch (Throwable t) {
        Util.messageBox(Gome.singleton.bundle.getString("ui.error"), t.getMessage() + ", " + t.toString(), AlertType.ERROR); //$NON-NLS-1$ //$NON-NLS-2$
      }

    }
  }

}
