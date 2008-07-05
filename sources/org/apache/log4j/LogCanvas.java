/*
 * Created on Jul 22, 2004
 * $Id: LogCanvas.java,v 1.1 2004/11/10 10:28:13 afrei Exp $
 */
package org.apache.log4j;

import java.util.Vector;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

/**
 * This is the canvas that displays the log messages and allows scrolling
 * 
 * @author Ren&eacute; M&uuml;ller
 */
public class LogCanvas extends Canvas {
  /** list of available log entries */
  private Vector logEntries = new Vector();

  /** current log index */
  private int logIndex = 0;

  /** width of graphic canvas */
  private int width;

  /** height of graphic canvas */
  private int height;

  /** Font to be used */
  private Font font;

  /** Height of font in pixels */
  private int fontHeight;

  /** top left corner of log canvas */
  private int x0;

  /** top left corner of log canvas */
  private int y0;

  private int currentIndex = 0;

  private static final int CAPACITY = 30;

  /**
   * Constructor creates LogCanvas which does not have any commands yet
   */
  public LogCanvas() {
    logEntries.setSize(CAPACITY);
  }

  /**
   * Add GUI commands and corresponding listener to this log canvas. (usually
   * the main application)
   * 
   * @param cmds
   *            Commands to be added to the Log Canvas
   * @param listener
   *            CommandListener associated with the Log Canvas
   */
  public void setCommandAndListener(Command[] cmds, CommandListener listener) {
    for (int i = 0; i < cmds.length; i++) {
      this.addCommand(cmds[i]);
    }
    this.setCommandListener(listener);
  }

  /**
   * Now paint the canvas
   * 
   * @param g
   *            graphics reference to the canvas
   */
  protected void paint(Graphics g) {
    if (font == null) {
      // Font is not yet set
      // cache the font and width, height value
      // when it is used the first time
      font = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_SMALL);
      width = this.getWidth();
      height = this.getHeight();
      fontHeight = font.getHeight();
    }

    int y = fontHeight; // 1st line y value

    // message will be rendered in black color, on top of white background
    g.setColor(255, 255, 255);
    g.fillRect(0, 0, width, height);
    g.setColor(0, 0, 0);
    g.setFont(font);

    g.translate(-x0, -y0);

    for (int i = logIndex; i < logEntries.size(); i++) {
      String s = (String) logEntries.elementAt(i);
      if (s != null) {
        g.drawString(s, 0, y, Graphics.BASELINE | Graphics.LEFT);
        y += fontHeight;
      }
    }
  }

  /**
   * Called when a key is pressed, this method handles the scrolling
   * 
   * @param key
   *            number of the pressed key
   */
  public void keyPressed(int key) {
    if (getGameAction(key) == Canvas.RIGHT) {
      x0 += 50;
      repaint();
    } else if (getGameAction(key) == Canvas.LEFT) {
      x0 -= 50;
      if (x0 < 0)
        x0 = 0;
      repaint();
    } else if (getGameAction(key) == Canvas.DOWN) {
      y0 += 50;
      repaint();
    } else if (getGameAction(key) == Canvas.UP) {
      y0 -= 50;
      if (y0 < 0)
        y0 = 0;
      repaint();
    }
  }

  /**
   * Add new log entry and redraw canvas
   * 
   * @param entry
   *            string to be added to the log
   */
  public void addEntry(String entry) {
    logEntries.setElementAt(entry, currentIndex++);
    if (currentIndex == CAPACITY) {
      currentIndex = 0;
    }

    repaint();
  }
}
