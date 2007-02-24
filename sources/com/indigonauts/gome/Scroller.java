/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

class Scroller implements Runnable {
    //private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("Scroller");
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
    private Thread scrolling;

    boolean waitingRepaint = true;

    public Scroller(Canvas target) {
        this.target = target;
        //log.debug("Instanciate scroller");
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
        try {
            running = false;
            if (scrolling != null) {
                synchronized (scrolling) {
                    scrolling.notifyAll();
                    scrolling.join();
                    scrolling = null;
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
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

    public int getMinimumHeight() {
        return Gome.singleton.options.getScrollerSize();
    }

    public void setImg(Image img) {
        this.img = img;
    }

    public synchronized void start() {
        reachTheEnd = false;

        offset = 0;
        if (img.getHeight() > height) {
            scrolling = new Thread(this);
            scrolling.start();
        }
        running = true;
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