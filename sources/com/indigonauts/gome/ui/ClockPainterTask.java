/*
 * (c) 2006 Indigonauts
 */

package com.indigonauts.gome.ui;

import java.util.TimerTask;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import com.indigonauts.gome.ClockController;
import com.indigonauts.gome.common.Util;
import com.indigonauts.gome.sgf.Board;

public class ClockPainterTask extends TimerTask {
    private int width = 0;

    private int height = 0;

    private int x = 0;

    private int y = 0;

    private Canvas canvas;

    private int blinkingColorForBlack = NORMAL_COLOR;

    private int blinkingColorForWhite = NORMAL_COLOR;

    private static final int HIGHLIGHTED_COLOR = Util.COLOR_RED;
    private static final int NORMAL_COLOR = Util.COLOR_BLUE;

    
    private static final int LARGE_HEIGHT = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_LARGE).getHeight();
    private static final int MEDIUM_HEIGHT = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_MEDIUM).getHeight();
    private static final int SMALL_HEIGHT = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_SMALL).getHeight();
    
    private int textFontSize = Font.SIZE_SMALL;
    private int byoFontSize = Font.SIZE_SMALL;

    public ClockPainterTask(Canvas c) {
        this.canvas = c;
    }

    public void drawClock(Graphics g, ClockController clock) {
    	Font textFont = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, textFontSize);
    	Font byoFont = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, byoFontSize);


        g.setColor(Util.COLOR_LIGHT_BACKGROUND);
        g.fillRect(x, y, width, height);

        if (clock.thereIsClock()) {
            int sizeOfCircle = textFont.getHeight() / 2;

            int byoStoneBlack = clock.getByoStoneBlack();
            int byoStoneWhite = clock.getByoStoneWhite();
            if (clock.isBlackOnByo() && (byoStoneBlack==0 || (clock.remainingTimeBlack() / byoStoneBlack < 5))
                    && clock.getColor() == Board.BLACK)
                blinkingColorForBlack = blinkColor(blinkingColorForBlack);
            else
                blinkingColorForBlack = NORMAL_COLOR;
            
            if ((clock.isWhiteOnByo()) && (byoStoneWhite == 0 || (clock.remainingTimeWhite() / byoStoneWhite < 5))
                    && clock.getColor() == Board.WHITE)
                blinkingColorForWhite = blinkColor(blinkingColorForWhite);
            else
                blinkingColorForWhite = NORMAL_COLOR;

            String blackTime = clock.getBlackTime();
            String whiteTime = clock.getWhiteTime();
            String blackByoStones = String.valueOf(byoStoneBlack);
            String whiteByoStones = String.valueOf(byoStoneWhite);

            g.setColor(blinkingColorForBlack);

            int dis = textFont.stringWidth(blackTime);

            int xPos = x + 1;

            g.setFont(textFont);
            g.drawString(blackTime, xPos, y + height, Graphics.LEFT | Graphics.BOTTOM);

            g.setColor(Util.COLOR_BLACK);
            xPos += dis + 1;
            int stoneYpos = y + (height - sizeOfCircle) / 2;
            g.fillArc(xPos, stoneYpos, sizeOfCircle, sizeOfCircle, 0, 360);
            xPos += sizeOfCircle + 3;
            if (clock.isBlackOnByo()) {
                g.setColor(Util.COLOR_RED);
                g.setFont(byoFont);
                g.drawString(blackByoStones, xPos, y + height, Graphics.LEFT | Graphics.BOTTOM);
            }

            xPos = x + width;
            g.setFont(textFont);
            dis = textFont.stringWidth(whiteTime);
            xPos -= dis + 1;
            g.setColor(blinkingColorForWhite);
            g.drawString(whiteTime, xPos, y + height, Graphics.LEFT | Graphics.BOTTOM);

            g.setColor(Util.COLOR_WHITE);
            xPos -= sizeOfCircle + 3;
            g.fillArc(xPos, stoneYpos, sizeOfCircle, sizeOfCircle, 0, 360);

            if (clock.isWhiteOnByo()) {
                g.setColor(Util.COLOR_RED);
                g.setFont(byoFont);
                dis = byoFont.stringWidth(whiteByoStones);
                xPos -= dis + 1;
                g.drawString(whiteByoStones, xPos, y + height, Graphics.LEFT | Graphics.BOTTOM);
            }

        }
    }

    public void setPosition(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;

        if (height >= LARGE_HEIGHT)
            textFontSize = Font.SIZE_LARGE;
        else if ((height < LARGE_HEIGHT) && (height >= MEDIUM_HEIGHT))
        	textFontSize = Font.SIZE_MEDIUM;
        else
        	textFontSize = Font.SIZE_SMALL;

        if (height >= ((3 * SMALL_HEIGHT)) / 2) {
            byoFontSize = Font.SIZE_SMALL;
        } else {
        	byoFontSize = textFontSize;
        }
    }

    public void run() {
        canvas.repaint(x, y, width, height);
    }

    private int blinkColor(int color) {
        if (color == NORMAL_COLOR)
            return HIGHLIGHTED_COLOR;
        return NORMAL_COLOR;
    }

    public void getImage(Graphics g, ClockController c, int w, int h) {
        setPosition(0, 0, w, h);
        drawClock(g, c);
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public int getMinimumHeight() {
        return SMALL_HEIGHT + 1;
    }
}
