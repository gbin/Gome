/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome;

import java.util.Timer;

import com.indigonauts.gome.sgf.Board;
import com.indigonauts.gome.ui.GameController;

public class ClockController {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("ClockController");

    private boolean enable;

    private ClockTimerTask clock;

    private Timer timer;

    private boolean noClock;

    private GameController gc;

    public ClockController(boolean startRightAway, GameController gc) {
        
        noClock = !startRightAway;        
        this.gc = gc;
        enableClock(startRightAway);
    }

    public ClockController(int time, int byoTime, int nbStonesPerByo1, GameController gc) {
        noClock = false;
        clock = new ClockTimerTask(time, byoTime, nbStonesPerByo1, this);
        this.timer = new Timer();
        this.timer.scheduleAtFixedRate(clock, 0, 1000);
        this.gc = gc;
        enableClock(true);
    }

    public void clockOnlineSwitcher(byte color) {
        if (enable) {
            switch (color) {
            case Board.BLACK:
                clock.onlineSwitchColor(Board.WHITE);
                break;

            case Board.WHITE:
                clock.onlineSwitchColor(Board.BLACK);
                break;

            case Board.EMPTY:
                break;
            }
        }
    }

    public void clockSwitcher(byte color) {
        if (enable) {
            switch (color) {
            case Board.BLACK:
                clock.switchColor(Board.WHITE);
                break;

            case Board.WHITE:
                clock.switchColor(Board.BLACK);
                break;

            case Board.EMPTY:
                break;
            }
        }
    }

    public boolean isBlackOnByo() {
        if (!noClock)
            return clock.isBlackOnByo();
        return false;
    }

    public boolean isWhiteOnByo() {
        if (!noClock)
            return clock.isWhiteOnByo();
        return false;
    }

    public long remainingTimeBlack() {
        return clock.remainingTimeBlack();
    }

    public long remainingTimeWhite() {
        return clock.remainingTimeWhite();
    }

    public String timeParser(int time) {
        int seconds = time % 60;
        int minutes = (time / 60) % 60;
        return (minutes < 10 ? "0":"") + minutes + ":"
                + (seconds < 10 ? "0":"") + seconds;
    }

    public String toString() {
        return getBlackTime() + " " + getWhiteTime();
    }

    public String getBlackTime() {
        if (!noClock)
            return timeParser(clock.remainingTimeBlack());
        return "";
    }

    public String getWhiteTime() {
        if (!noClock)
            return timeParser(clock.remainingTimeWhite());
        return "";
    }

    public int getByoStoneBlack() {
        return clock.remaingByoStoneBlack();
    }

    public int getByoStoneWhite() {
        return clock.remaingByoStoneWhite();
    }

    public boolean thereIsClock() {
        return !noClock && enable;
    }

    private void enableClock(boolean enable1) {
        if (!noClock && this.enable == false && enable1) {
            this.enable = enable1;

            if (this.enable == true & timer == null) {
                if (clock == null) {
                    clock = new ClockTimerTask(this);
                } else {
                    clock = new ClockTimerTask(clock);
                }
                timer = new Timer();
                timer.scheduleAtFixedRate(clock, 0, 1000);
            }
        }
        if (!enable1) {
            this.enable = false;
            if (timer != null) {
                timer.cancel();
                timer = null;
            }
        }
    }
    
    public void pauseClock(){
        enableClock(false);
    }
    
    public void resumeClock(){
        enableClock(true);
    }

    public void synTime(int whiteTime, int whiteByoStone, int blackTime, int blackByoStone) {

        log.debug("Synchronize clocks");
        if (clock != null) {
            clock.synchronizeOnlineWhiteClock(whiteTime, whiteByoStone);
            clock.synchronizeOnlineBlackClock(blackTime, blackByoStone);
        }
    }

    public void timeIsUP(byte color) {
        if (!noClock) {
            gc.gameIsFinished(color);
        }
        timer.cancel();
    }

    public void stopClock() {
        if (timer != null)
            timer.cancel();
        timer = null;
        noClock = true;
        clock = null;
    }

    public byte getColor() {
        return gc.getCurrentPlayerColor();
    }
}
