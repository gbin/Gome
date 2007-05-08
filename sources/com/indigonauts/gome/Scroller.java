/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome;

import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import com.indigonauts.gome.common.Util;

class Scroller extends Thread {
  //#ifdef DEBUG
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("Scroller");
  //#endif
  private boolean running = true;
  private Canvas target;
  private int x;
  private int y;
  private int width;
  private int height;
  private Image img;
  private int offset = 0;
  private int speed;
  private int bigStep;
  private boolean reachTheEnd = false;
  boolean waitingRepaint = true;

  public Scroller(Canvas target, int x, int y, int width, int height) {
    this.target = target;
    setPosition(x, y, width, height);
  }

  public void run() {
    if (speed == -1)// manual
      return; // don't do anything wait for the events

    try {
      while (running) {
        synchronized (this) {
          this.wait(speed);
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

  public void drawMe(Graphics g) // should be callbacked from the paint
  {
    waitingRepaint = false;
    g.setClip(x, y, width, height);
    g.drawImage(img, 0, y - offset, Graphics.LEFT | Graphics.TOP);

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
    if (waitingRepaint)
      return;
    offset++;
    if (offset >= img.getHeight() - height) {
      offset = 0;
      reachTheEnd = true;
    }
    waitingRepaint = true;
    target.repaint(x, y, width, height);
  }

  public void stepDown() {
    if (waitingRepaint)
      return;
    offset--;
    if (offset <= 0)
      offset = img.getHeight() - height;
    waitingRepaint = true;
    target.repaint(x, y, width, height);
  }

  public void stop() {
    running = false;
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

  private void setPosition(int x, int y, int width, int height) {
    this.x = x;
    this.y = y;
    this.width = width;
    this.height = height;
  }

  public int getMinimumHeight() {
    return Gome.singleton.options.getScrollerSize();
  }

  public void setImg(Image img) {
    //#ifdef DEBUG
    log.debug("set img " + img);
    //#endif
    this.img = img;
  }

  public synchronized void start() {
  
    reachTheEnd = false;

    offset = 0;
    running = true;
    if (img.getHeight() > height) {
      setPriority(Thread.MIN_PRIORITY);
      super.start();
    }
  }

  public void setBigStep(int bigStep) {
    this.bigStep = bigStep - 1;
  }

  public void setSpeed(int speed) {
    this.speed = speed;
  }

  public synchronized boolean isStarted() {
    return running;
  }

  public boolean endOfScroll() {
    return reachTheEnd;
  }

}