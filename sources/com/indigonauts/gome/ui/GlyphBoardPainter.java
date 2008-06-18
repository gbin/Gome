//#condition AA
package com.indigonauts.gome.ui;

import java.io.IOException;
import java.io.InputStream;

import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import com.indigonauts.gome.Gome;
import com.indigonauts.gome.common.Point;
import com.indigonauts.gome.common.Rectangle;
import com.indigonauts.gome.common.Util;
import com.indigonauts.gome.sgf.Board;
import com.indigonauts.gome.sgf.SgfNode;
import com.indigonauts.gome.sgf.SgfPoint;
import com.indigonauts.gome.sgf.SymbolAnnotation;

public class GlyphBoardPainter extends BoardPainter {
  //#if DEBUG
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("GlyphBoardPainter");
  //#endif
  private static final byte[] availStoneSizes = { 6, 8, 9, 10, 12, 14, 15, 16, 17, 18, 22, 24, 26, 27, 30, 39 };
  private static final byte[] availGlyphSizes = { 6, 8, 10, 12, 16, 18 };
  private static final String symbols = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

  public GlyphBoardPainter(Board newBoard, Rectangle imageArea, Rectangle newBoardArea, boolean doubleBuffered) {
    super(newBoard, imageArea, newBoardArea, doubleBuffered);
  }

  Image whiteStone;
  Image blackStone;
  Image stoneMarker;

  int[] buffer;
  int[] backbuffer;

  protected void calcDrawingPosition() {
    super.calcDrawingPosition();
    try {
      readStones(delta);
      stoneMarker = null; // reset it so it will reread it if needed
    } catch (IOException e) {
      Util.errorNotifier(e);
      whiteStone = null;
      blackStone = null;
    }
  }

  private static int bestFitFor(byte[] sizes, int size) {
    int as = sizes.length;
    int i = 0;
    while (i < as - 1 && sizes[i] < size)
      i++;
    return sizes[i];
  }

  protected void drawStone(Graphics g, int cx, int cy, int size, int playerColor) {
    if (whiteStone != null)
      g.drawImage(playerColor == Board.WHITE ? whiteStone : blackStone, cx, cy, Graphics.HCENTER | Graphics.VCENTER);
    else
      drawVectStone(g, cx - halfdelta, cy - halfdelta, size, playerColor == Board.WHITE ? Util.COLOR_WHITE : Util.COLOR_BLACK);
  }

  public void drawTextAnnotation(Graphics g, Point pt, String text, boolean erasebg, int color) {
    int tlx = getCellX(pt.x) - halfdelta;
    int tly = getCellY(pt.y) - halfdelta;
    int[] readSymbol;
    try {
      readSymbol = readSymbol(text.charAt(0), delta, color);
      if (erasebg) {
        int minx = Integer.MAX_VALUE;
        int miny = Integer.MAX_VALUE;
        int maxx = Integer.MIN_VALUE;
        int maxy = Integer.MIN_VALUE;

        for (int i = 0; i < delta * delta; i++) {
          if ((readSymbol[i] & 0xff000000) != 0) {
            int x = i % delta;
            int y = i / delta;
            if (x < minx)
              minx = x;
            else if (x > maxx)
              maxx = x;
            if (y < miny)
              miny = y;
            else if (y > maxy)
              maxy = y;
          }
        }
        g.setColor(Gome.singleton.options.gobanColor);
        g.fillRect(tlx + minx - 1, tly + miny - 1, maxx - minx + 3, maxy - miny + 3);
      }
      g.drawRGB(readSymbol, 0, delta, tlx, tly, delta, delta, true);
    } catch (IOException e) {
      Util.errorNotifier(e);
    }

  }

  private int[] readSymbol(char c, int size, int color) throws IOException {
    int chrIndex = symbols.indexOf(c);
    int srcsize = bestFitFor(availGlyphSizes, size);
    InputStream input = GlyphBoardPainter.class.getResourceAsStream("/gfx/a" + srcsize + ".r");
    int[] arrayRep = readGfx(input, chrIndex, srcsize, color);
    input.close();
    return size != srcsize ? resize(arrayRep, srcsize, size) : arrayRep;
  }

  private void readStones(int size) throws IOException {
    int srcsize = bestFitFor(availStoneSizes, size);
    InputStream input = GlyphBoardPainter.class.getResourceAsStream("/gfx/" + srcsize + ".r");

    final int intSize = srcsize * srcsize;
    if (backbuffer == null || backbuffer.length < intSize) {
      //#if DEBUG
      log.debug("Realloc backbuffer to " + intSize);
      //#endif
      backbuffer = new int[intSize];
    }

    int[] arrayRep = backbuffer;

    for (int i = 0; i < intSize; i++) {
      arrayRep[i] = input.read() + (input.read() << 8) + (input.read() << 16) + (input.read() << 24);
    }
    whiteStone = Image.createRGBImage(size != srcsize ? resize(arrayRep, srcsize, size) : arrayRep, size, size, true);

    for (int i = 0; i < intSize; i++) {
      arrayRep[i] = input.read() + (input.read() << 8) + (input.read() << 16) + (input.read() << 24);
    }
    blackStone = Image.createRGBImage(size != srcsize ? resize(arrayRep, srcsize, size) : arrayRep, size, size, true);
    input.close();
  }

  private int[] readGfx(InputStream input, int index, int size, int color) throws IOException {

    color &= 0x00FFFFFF; // don't touch trnasparency
    final int intSize = size * size;

    int toSkip = intSize * index * 4;
    while (toSkip > 0) {
      toSkip -= input.skip(toSkip);
    }

    if (backbuffer == null || backbuffer.length < intSize) {
      //#if DEBUG
      log.debug("Realloc backbuffer to " + intSize);
      //#endif
      backbuffer = new int[intSize];
    }

    int[] arrayRep = backbuffer;
    for (int i = 0; i < intSize; i++) {
      arrayRep[i] = (input.read() + (input.read() << 8) + (input.read() << 16) + (input.read() << 24)) | color;
    }
    return arrayRep;
  }

  private int[] resize(int[] source, int oldSize, int newSize) {
    int intSize = newSize * newSize;
    if (buffer == null || buffer.length < intSize) {
      //#if DEBUG
      log.debug("Realloc buffer to " + intSize);
      //#endif
      buffer = new int[intSize];
    }

    int[] result = buffer;
    float srcCenterFP = oldSize / 2.0f;
    float dstCenterFP = newSize / 2.0f - 0.5f;
    float scaleFP = ((float) newSize) / oldSize;
    dstCenterFP += scaleFP / 2.0f;
    float xsFP, ysFP;
    float limitFP = oldSize - 1.0f;
    float limit2FP = oldSize - 1.001f;

    int index = 0;
    for (int y = 0; y < newSize; y++) {
      ysFP = (y - dstCenterFP) / scaleFP + srcCenterFP;
      if (ysFP < 0f) {
        ysFP = 0f;
      }
      if (ysFP >= limitFP) {
        ysFP = limit2FP;
      }

      float yFractionFP = ysFP - (int) ysFP;
      for (int x = 0; x < newSize; x++) {
        xsFP = (x - dstCenterFP) / scaleFP + srcCenterFP;
        if (xsFP < 0f) {
          xsFP = 0f;
        }
        if (xsFP >= limitFP) {
          xsFP = limit2FP;
        }

        float xFractionFP = xsFP - (int) xsFP;
        int offset = ((int) ysFP) * oldSize + (int) xsFP;

        int upperLeft = source[offset];
        int upperRight = source[offset + 1];
        int lowerRight = source[offset + oldSize + 1];
        int lowerLeft = source[offset + oldSize];
        int upperAverage = average(upperLeft, upperRight, xFractionFP);
        int lowerAverage = average(lowerLeft, lowerRight, xFractionFP);
        result[index++] = average(upperAverage, lowerAverage, yFractionFP);
      }
    }
    return result;
  }

  private static int average(int left, int right, float fraction) {
    int leftAlpha = left >>> 24;
    int rightAlpha = right >>> 24;
    int alpha = ((leftAlpha) + (int) (fraction * ((rightAlpha) - (leftAlpha)))) << 24;
    if (leftAlpha == 0)
      return (right & 0x00FFFFFF) | alpha;
    if (rightAlpha == 0)
      return (left & 0x00FFFFFF) | alpha;

    int avg = alpha;
    avg |= ((left & 0x00FF0000) + (int) (fraction * ((right & 0x00FF0000) - (left & 0x00FF0000)))) & 0x00FF0000;
    avg |= ((left & 0x0000FF00) + (int) (fraction * ((right & 0x0000FF00) - (left & 0x0000FF00)))) & 0x0000FF00;
    avg |= ((left & 0x000000FF) + (int) (fraction * ((right & 0x000000FF) - (left & 0x000000FF)))) & 0x000000FF;
    return avg;
  }

  public void drawSymbolAnnotation(Graphics g, SymbolAnnotation annotation, int color) {
    try {
      int cx = getCellX(annotation.x);
      int cy = getCellY(annotation.y);
      int[] arrayRep = readAnnotation(annotation, color);
      g.drawRGB(arrayRep, 0, delta, cx - halfdelta, cy - halfdelta, delta, delta, true);
    } catch (IOException e) {
      //#if DEBUG
      e.printStackTrace();
      //#endif
      Util.errorNotifier(e);
    }

  }

  private int[] readAnnotation(SymbolAnnotation annotation, int color) throws IOException {
    int srcsize = bestFitFor(availGlyphSizes, delta);
    InputStream input = GlyphBoardPainter.class.getResourceAsStream("/gfx/g" + srcsize + ".r");
    int[] arrayRep = readGfx(input, annotation.getType(), srcsize, color);
    input.close();
    arrayRep = delta != srcsize ? resize(arrayRep, srcsize, delta) : arrayRep;
    return arrayRep;
  }

  protected void renderStoneMarker(Graphics g, SgfNode currentNode) {
    try {
      SgfPoint point = currentNode.getPoint();
      if (point != null) {
        if (stoneMarker == null) {
          stoneMarker = Image.createRGBImage(readAnnotation(new SymbolAnnotation(point, SymbolAnnotation.FILLED_CIRCLE), Util.COLOR_RED), delta, delta, true);
        }
        int cx = getCellX(point.x);
        int cy = getCellY(point.y);
        g.drawImage(stoneMarker, cx, cy, Graphics.HCENTER | Graphics.VCENTER);
      }
    } catch (IOException e) {
      Util.errorNotifier(e);
    }

  }

}
