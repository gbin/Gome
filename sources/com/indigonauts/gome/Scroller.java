/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome;

import java.util.Vector;

import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import com.indigonauts.gome.common.Point;
import com.indigonauts.gome.common.Util;

class Scroller extends Thread {
  //#ifdef DEBUG
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("Scroller");
  //#endif
  public static final Font SMALL_FONT_BOLD = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_BOLD, Font.SIZE_SMALL);

  private Canvas target;

  private int x;
  private int y;
  private int width;
  private int height;

  private int offset = 0;
  private int speed;
  private int bigStep;
  private boolean reachTheEnd = false;
  boolean waitingRepaint = true;

  boolean visible = false;
  boolean paused = true;
  private boolean running = true;

  public Scroller(Canvas target) {
    this.target = target;
    start();
  }

  public void run() {

    try {
      synchronized (this) {
        while (running) {
          if (paused) {
            //#ifdef DEBUG
            log.debug("scroller paused");
            //#endif
            this.wait();
            //#ifdef DEBUG
            log.debug("scroller resumed");
            //#endif
          } else {
            this.wait(speed);
          }
          //#ifdef DEBUG
          log.debug("scroller pulse");
          //#endif

          stepUp();
        }
      }
    } catch (InterruptedException e) {
      // interrupted, do nothing
    }
    //#ifdef DEBUG
    catch (Throwable t) {
      log.error("scroller loop error", t);
      Util.messageBox("Uncaught exception cf logs", "Uncaught exception " + t.getMessage(), AlertType.ERROR);
    }
    //#endif
  }

  private static final int boxHeight = SMALL_FONT_BOLD.getHeight() + 1;

  private Image tempBuff;
  private int[] afterRaster;

  private static final String SEP = "-";

  /*
   * Specify top right corner
   */
  private void drawOverlayBox(Graphics g, int x, int y, String[] content, int[] color, int bg) {
    int boxWidth = 0;
    int sep = SMALL_FONT_BOLD.stringWidth(SEP);

    for (int i = 0; i < content.length; i++)
      boxWidth += SMALL_FONT_BOLD.stringWidth(content[i]);

    boxWidth += content.length * sep;
    
    Graphics gt = tempBuff.getGraphics();
    gt.setColor(bg);
    gt.fillRect(0, 0, boxWidth, boxHeight);

    gt.setColor(Util.COLOR_WHITE);
    
    gt.fillRoundRect(0, 0, boxWidth, boxHeight, boxHeight, boxHeight);
    gt.setFont(SMALL_FONT_BOLD);
    
    int xx = 0;
    for (int i = 0; i < content.length - 1; i++) {
      gt.setColor(color[i]);
      gt.drawString(content[i], xx, 1, Graphics.TOP | Graphics.LEFT);
      xx += SMALL_FONT_BOLD.stringWidth(content[i]);
      gt.drawString(SEP, xx, 1, Graphics.TOP | Graphics.LEFT);
      xx += sep;
    }
    
    gt.setColor(color[content.length - 1]);
    gt.drawString(content[content.length - 1], xx, 1, Graphics.TOP | Graphics.LEFT);

    int len = boxWidth * boxHeight;
    if (afterRaster == null || afterRaster.length < len)
      afterRaster = new int[len];
    
    tempBuff.getRGB(afterRaster, 0, boxWidth, 0, 0, boxWidth, boxHeight);
    
    for (int i = 0; i < len; i++) {
      afterRaster[i] = 0xA0000000 + (afterRaster[i] & 0x00FFFFFF); // get the color of the pixel.
    }
    g.drawRGB(afterRaster, 0, boxWidth, x -boxWidth, y, boxWidth, boxHeight, true);

  }

  public void drawMe(Graphics g) // should be callbacked from the paint
  {
    g.setClip(0, y, width, height);
    waitingRepaint = false;
    g.setColor(Util.COLOR_LIGHT_BACKGROUND);
    g.fillRect(0, y, width, height);

    if (lines != null) {
      Util.drawText(g, 0, y - offset, lines, font, Util.COLOR_BLACK);
    }
    
    String[] strings;
    int[] colors;
    if (coordinates != null) {
      strings = new String[2];
      colors = new int[2];
      strings[1] = coordinates;
      colors[1] = Util.COLOR_RED;
    }
    else
    {
      strings = new String[1];
      colors = new int[1];
    }
    strings[0] = " #" + moveNb;

    drawOverlayBox(g, width-2, y+1, strings, colors, Util.COLOR_LIGHT_BACKGROUND);
    

  }

  public void bigStepUp() {
    offset += bigStep;
    stepUp();
  }

  public void bigStepDown() {
    offset -= bigStep;
    stepDown();
  }

  public void stepUp() {
    if (lines != null) {
      if (waitingRepaint)
        return;
      offset++;
      if (offset >= textHeight - height) {
        offset = 0;
        reachTheEnd = true;
      }
      waitingRepaint = true;
      repaint();
    }
  }

  public void repaint() {
    target.repaint(x, y, width, height);
  }

  public void stepDown() {
    if (waitingRepaint)
      return;
    if (lines != null) {
      offset--;
      if (offset <= 0)
        offset = textHeight - height;
      waitingRepaint = true;
      target.repaint(x, y, width, height);
    }
  }

  public void stop() {
    running = false;
    paused = false;
    synchronized (this) {
      notifyAll();
    }
  }

  public int getHeight() {
    return height;
  }

  public int getWidth() {
    return width;
  }

  public int getX() {
    return x;
  }

  public int getY() {
    return y;
  }

  public void setPosition(int x, int y, int width, int height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
    //#ifdef MIDP2
    tempBuff = Image.createImage(width / 2, boxHeight);
    //#endif
  }

  private int moveNb;
  private String coordinates;
  private Vector lines;
  private Font font;
  private int textHeight;

  public void setMoveNb(int nb) {
    moveNb = nb;
  }

  private static final char[] COORDS = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T' };

  public void setCoordinates(Point p) {
    coordinates = "" + COORDS[p.x] + (p.y + 1) + ((p.y + 1 < 10) ? " " : "");
    if (visible)
      repaint();
  }

  public void setComment(String comments) {
    reachTheEnd = false;
    // offset = -SMALL_FONT_BOLD.getHeight() - 2;
    offset = 0;
    if (comments == null) {
      lines = null;
      pause();
    } else {
      lines = Util.lineSplitter(comments, width, Gome.singleton.options.getScrollerFont());

      font = Gome.singleton.options.getScrollerFont();
      textHeight = lines.size() * font.getHeight();
      int extraLines = height / font.getHeight() + 1;
      if (lines.size() >= extraLines) {
        for (int i = 0; i < extraLines; i++) {
          lines.addElement(lines.elementAt(i)); // readd the beginning at the end to loop nicely
        }
        // put the new textHeight
        textHeight += height;
        resume(); // start the scroller itself
      } else {
        pause();
      }
    }
  }

  public synchronized void start() {

    reachTheEnd = false;
    offset = 0;
    running = true;
    setPriority(Thread.MIN_PRIORITY);
    super.start();
  }

  public void setBigStep(int bigStep) {
    this.bigStep = bigStep - 1;
  }

  public void setSpeed(int speed) {
    this.speed = speed;
    if (speed == -1)// manual
      pause();

  }

  private void pause() {
    //log.debug("pause scroller");
    this.paused = true;
  }

  private void resume() {
    //log.debug("resume scroller");
    this.paused = false;
    synchronized (this) {
      notifyAll();
    }
  }

  public boolean endOfScroll() {
    return reachTheEnd;
  }

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean visible) {
    //log.debug("Set scroller visible to "+ visible);
    this.visible = visible;
    if (!visible)
      pause();
  }

}