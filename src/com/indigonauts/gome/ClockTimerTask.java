/*
 * (c) 2006 Indigonauts
 */

package com.indigonauts.gome;

import java.util.TimerTask;

import com.indigonauts.gome.sgf.Board;

public class ClockTimerTask extends TimerTask {

    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("ClockTimerTask");    

    private int remainingMaintimeBlack;

    private int remainingMaintimeWhite;

    private int remainingByoTimeBlack;

    private int remainingByoTimeWhite;

    private int nbRemainingByoStonesBlack;

    private int nbRemainingByoStonesWhite;

    private int totalByo;

    private int nbStonesPerByo;

    private byte currentColor;

    private boolean blackTimeUp = false;

    private boolean whiteTimeUp = false;

    private ClockController cc;

    public ClockTimerTask(ClockController c) {
        cc = c;
        remainingMaintimeBlack = 10;
        remainingMaintimeWhite = 10;
        nbStonesPerByo = nbRemainingByoStonesBlack = nbRemainingByoStonesWhite = 25;
        totalByo = remainingByoTimeBlack = remainingByoTimeWhite = 150;
        currentColor = Board.BLACK;
    }

    public ClockTimerTask(int time, int byoTime, int nbStonesPerByo1, ClockController c) {
        cc = c;
        remainingMaintimeBlack = time;
        remainingMaintimeWhite = time;
        nbStonesPerByo = nbRemainingByoStonesBlack = nbRemainingByoStonesWhite = nbStonesPerByo1;
        totalByo = remainingByoTimeBlack = remainingByoTimeWhite = byoTime;
        currentColor = Board.BLACK;
    }

    public ClockTimerTask(ClockTimerTask c) {
        cc = c.cc;
        remainingMaintimeBlack = c.remainingMaintimeBlack;
        remainingMaintimeWhite = c.remainingMaintimeWhite;
        nbStonesPerByo = c.nbStonesPerByo;
        nbRemainingByoStonesBlack = c.nbRemainingByoStonesBlack;
        nbRemainingByoStonesWhite = c.nbRemainingByoStonesWhite;
        totalByo = c.totalByo;
        remainingByoTimeBlack = c.remainingByoTimeBlack;
        remainingByoTimeWhite = c.remainingByoTimeWhite;
        currentColor = c.currentColor;
    }

    public int remainingTimeBlack() {
        if (isBlackOnByo())
            return remainingByoTimeBlack;
        return remainingMaintimeBlack;
    }

    public int remainingTimeWhite() {
        if (isWhiteOnByo())
            return remainingByoTimeWhite;
        return remainingMaintimeWhite;
    }

    public int remaingByoStoneBlack() {
        return nbRemainingByoStonesBlack;
    }

    public int remaingByoStoneWhite() {
        return nbRemainingByoStonesWhite;
    }

    public boolean isBlackOnByo() {
        return remainingMaintimeBlack == 0;
    }

    public boolean isWhiteOnByo() {
        return remainingMaintimeWhite == 0;
    }

    public void run() {

        switch (currentColor) {
        case Board.BLACK:
            if (!isBlackOnByo()) {
                remainingMaintimeBlack--;
            } else {
                if (remainingByoTimeBlack > 0)
                    remainingByoTimeBlack--;
            }
            if (remainingByoTimeBlack == 0 && remainingMaintimeBlack == 0) {
                log.debug("Black time is up");
                cc.timeIsUP(Board.BLACK);
            }
            break;

        case Board.WHITE:
            if (!isWhiteOnByo()) {
                remainingMaintimeWhite--;
            } else {
                if (remainingByoTimeWhite > 0)
                    remainingByoTimeWhite--;
            }

            if (remainingByoTimeWhite == 0 && remainingMaintimeWhite == 0) {
                log.debug("White time is up");
                cc.timeIsUP(Board.WHITE);
            }
            break;
        case Board.EMPTY:
            break;
        }
    }

    public void switchColor(byte color) {
        switch (currentColor) {
        case Board.BLACK:
            if (isBlackOnByo()) {
                if (nbRemainingByoStonesBlack < 0) {
                    nbRemainingByoStonesBlack = 0;
                }
                nbRemainingByoStonesBlack--;
                if (nbRemainingByoStonesBlack == 0) {
                    nbRemainingByoStonesBlack = nbStonesPerByo;
                    remainingByoTimeBlack = totalByo;
                }
            }
            break;

        case Board.WHITE:
            if (isWhiteOnByo()) {
                if (nbRemainingByoStonesWhite < 0) {
                    nbRemainingByoStonesWhite = 0;
                }
                nbRemainingByoStonesWhite--;
                if (nbRemainingByoStonesWhite == 0) {
                    nbRemainingByoStonesWhite = nbStonesPerByo;
                    remainingByoTimeWhite = totalByo;
                }
            }
            break;
        }
        currentColor = color;
    }

    public void onlineSwitchColor(byte color) {
        switch (currentColor) {
        case Board.BLACK:
            if (isBlackOnByo()) {
                if (nbRemainingByoStonesBlack < 0) {
                    nbRemainingByoStonesBlack = 0;
                }
                if (nbRemainingByoStonesBlack == 0) {
                    nbRemainingByoStonesBlack = nbStonesPerByo;
                    remainingByoTimeBlack = totalByo;
                }
            }
            break;

        case Board.WHITE:
            if (isWhiteOnByo()) {
                if (nbRemainingByoStonesWhite < 0) {
                    nbRemainingByoStonesWhite = 0;
                }
                if (nbRemainingByoStonesWhite == 0) {
                    nbRemainingByoStonesWhite = nbStonesPerByo;
                    remainingByoTimeWhite = totalByo;
                }
            }
            break;
        }
        currentColor = color;
    }

    public void synchronizeOnlineBlackClock(int time, int stones) {
        if (stones == -1) {
            log.debug("previous black remaining time is: " + remainingMaintimeBlack + " new is:" + time);
            remainingMaintimeBlack = time;
        } else {
            log.debug("previous black remaining time is: " + remainingByoTimeBlack + " new is:" + time);
            log.debug("previous black remaining stone is: " + nbRemainingByoStonesBlack + " new is:" + stones);
            remainingMaintimeBlack = 0;
            remainingByoTimeBlack = time;
            nbRemainingByoStonesBlack = stones;
        }
    }

    public void synchronizeOnlineWhiteClock(int time, int stones) {
        if (stones == -1) {
            log.debug("previous white remaining time is: " + remainingMaintimeWhite + " new is:" + time);
            remainingMaintimeWhite = time;
        } else {
            log.debug("previous white remaining time is: " + remainingByoTimeWhite + " new is:" + time);
            log.debug("previous white remaining stone is: " + nbRemainingByoStonesWhite + " new is:" + stones);
            remainingMaintimeWhite = 0;
            remainingByoTimeWhite = time;
            nbRemainingByoStonesWhite = stones;
        }
    }

    public boolean isBlackTimeUp() {
        return blackTimeUp;
    }

    public boolean isWhiteTimeUp() {
        return whiteTimeUp;
    }
}
