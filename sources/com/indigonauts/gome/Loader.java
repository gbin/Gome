/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome;

import java.io.IOException;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import com.indigonauts.gome.common.Util;

public class Loader extends Canvas {

  //#ifdef DEBUG
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("Loader");
  //#endif

  //#ifdef SPLASH
  private Image gomoku;

  //#endif

  public Loader() {
    super();
    //#ifdef SPLASH
    try {
      gomoku = Image.createImage("/gomoku.png");
    } catch (IOException e) {
      e.printStackTrace();
    }
    //#ifdef MIDP2
    setFullScreenMode(true);
    //#endif
    //#endif
  }

  protected void paint(Graphics g) {
    //#ifdef SPLASH
    g.setColor(0xccccff);
    g.fillRect(0, 0, getWidth(), getHeight());
    g.drawImage(gomoku, getWidth() / 2, getHeight() / 2, Graphics.HCENTER | Graphics.BOTTOM);
    g.setColor(Util.COLOR_BLACK);
    //#endif
    Font f = g.getFont();
    g.drawString("Gome v" + Gome.VERSION, getWidth() / 2, getHeight() / 2, Graphics.TOP | Graphics.HCENTER);
    g.drawString("(c) 2005-2007 Indigonauts", getWidth() / 2, getHeight() / 2 + f.getHeight(), Graphics.TOP | Graphics.HCENTER);
    //#ifdef DEBUG
    log.debug("Splash showed");
    //#endif

  }

}
