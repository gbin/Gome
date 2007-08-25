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

public class Scroller extends Thread {
  //#ifdef DEBUG
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("Scroller");
  //#endif
  public static final Font SMALL_FONT_BOLD = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_BOLD, Font.SIZE_SMALL);

  private static final char[] UPC = { 8593 };
  private static final char[] DOWNC = { 8595 };
  private static final char[] UP_DOWNC = { 8597 };
  private static final String UP = new String(UPC);
  private static final String DOWN = new String(DOWNC);
  private static final String UP_DOWN = new String(UP_DOWNC);
  
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

  private static final int boxHeight = SMALL_FONT_BOLD.getHeight();

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

  private Image tempBuff;
  private int[] afterRaster;

  private static final String SEP = "-";

  /*
   * Specify top right corner
   */
  private void drawOverlayBox(Graphics g, int xBox, int yBox, String[] content, int[] color, int bg, int alpha) {
    int boxWidth = 0;
    int sep = SMALL_FONT_BOLD.stringWidth(SEP);
    int space = SMALL_FONT_BOLD.charWidth(' ');

    for (int i = 0; i < content.length; i++)
      boxWidth += SMALL_FONT_BOLD.stringWidth(content[i]);

    boxWidth += content.length * sep;
    boxWidth += space;

    Graphics gt = tempBuff.getGraphics();
    gt.setColor(bg);
    gt.fillRect(0, 0, boxWidth, boxHeight);

    gt.setColor(Util.COLOR_WHITE);

    gt.fillRoundRect(0, 0, boxWidth, boxHeight, boxHeight, boxHeight);
    gt.setFont(SMALL_FONT_BOLD);

    int xx = space;
    for (int i = 0; i < content.length - 1; i++) {
      gt.setColor(color[i]);
      gt.drawString(content[i], xx, boxHeight, Graphics.BOTTOM | Graphics.LEFT);
      xx += SMALL_FONT_BOLD.stringWidth(content[i]);
      gt.setColor(Util.COLOR_DARKGREY);
      gt.drawString(SEP, xx, boxHeight, Graphics.BOTTOM | Graphics.LEFT);
      xx += sep;
    }

    gt.setColor(color[content.length - 1]);
    gt.drawString(content[content.length - 1], xx, boxHeight, Graphics.BOTTOM | Graphics.LEFT);

    int len = boxWidth * boxHeight;
    if (afterRaster == null || afterRaster.length < len)
      afterRaster = new int[len];

    tempBuff.getRGB(afterRaster, 0, boxWidth, 0, 0, boxWidth, boxHeight);

    for (int i = 0; i < len; i++) {
      afterRaster[i] = ((alpha<<24)&0xFF000000) + (afterRaster[i] & 0x00FFFFFF); // get the color of the pixel.
    }
    g.drawRGB(afterRaster, 0, boxWidth, xBox - boxWidth, yBox, boxWidth, boxHeight, true);

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

    int nb = 1;
    if (coordinates != null)
      nb++;
    if (fileIndex != null)
      nb++;
    if (older || younger)
      nb++;

    String[] strings = new String[nb];
    int[] colors = new int[nb];

    if (older && younger) {
      strings[--nb] = UP_DOWN;
      colors[nb] = Util.COLOR_RED;
    } else if (older) {
      strings[--nb] = UP;
      colors[nb] = Util.COLOR_RED;
    } else if (younger) {
      strings[--nb] = DOWN;
      colors[nb] = Util.COLOR_RED;
    }

    if (coordinates != null) {
      strings[--nb] = coordinates;
      colors[nb] = Util.COLOR_RED;
    }

    strings[--nb] = "#" + moveNb;
    colors[nb] = Util.COLOR_BLUE;

    if (fileIndex != null) {
      strings[--nb] = fileIndex;
      colors[nb] = Util.COLOR_GREEN;
    }
    
    int transparency = 0x00;
    if(offset/2<boxHeight)
    {
      transparency = (0xA0 * (boxHeight - (offset/2)))/boxHeight;
    }
    else if (offset > textHeight - height - boxHeight)
    {
      transparency = (0xA0 * (offset + boxHeight - textHeight + height))/boxHeight;
    }

    drawOverlayBox(g, width - 2, y + 1, strings, colors, Util.COLOR_LIGHT_BACKGROUND, transparency);
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
    tempBuff = Image.createImage(width / 2, boxHeight);
  }

  private int moveNb;
  private String coordinates;
  private Vector lines;
  private Font font;
  private int textHeight;
  private String fileIndex;
  private boolean younger;
  private boolean older;

  public void setBrothers(boolean older, boolean younger) {
    this.older = older;
    this.younger = younger;
  }

  public void setMoveNb(int nb) {
    moveNb = nb;
  }

  private int initTextWidth() {
    int l = 5; // #1 + 2 margin 
    if (moveNb > 10)
      l++;
    if (moveNb > 100)
      l++;

    l += 3; // - coordinates

    if (older || younger)
      l += 2; // - arrow
    return width - SMALL_FONT_BOLD.charWidth('-') * l;
  }

  private static final char[] COORDS = new char[] { 'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T' };

  public void setCoordinates(Point p) {
    coordinates = "" + COORDS[p.x] + (p.y + 1) + ((p.y + 1 < 10) ? " " : "");
    if (visible)
      repaint();
  }

  public void setComment(String comments) {
    reachTheEnd = false;
    offset = 0;
    if (comments == null) {
      lines = null;
      pause();
    } else {
      lines = Util.lineSplitter(comments, width, initTextWidth(), Gome.singleton.options.getScrollerFont());

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
    this.paused = true;
  }

  private void resume() {
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
    this.visible = visible;
    if (!visible)
      pause();
  }

  public void setFileIndex(int i) {
    if (i == 0)
      fileIndex = null;
    else
      fileIndex = String.valueOf(i);
  }

}