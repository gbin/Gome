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

  public void drawMe(Graphics g) // should be callbacked from the paint
  {
    waitingRepaint = false;
    if (img != null) {
      g.setClip(x, y, width, height);
      g.drawImage(img, 0, y - offset, Graphics.LEFT | Graphics.TOP);
    }
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
    if (img != null) {
      if (waitingRepaint)
        return;
      offset++;
      if (offset >= img.getHeight() - height) {
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
    if (img != null) {
      offset--;
      if (offset <= 0)
        offset = img.getHeight() - height;
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
  }

  public void setImg(Image img) {
    reachTheEnd = false;
    offset = 0;
    //#ifdef DEBUG
    log.debug("set img " + img);
    //#endif
    this.img = img;
    if (img != null && img.getHeight() > height) {
      //log.debug("image too large start the scroll");
      resume();
    } else {
      //log.debug("image is null or small pause");
      pause();
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