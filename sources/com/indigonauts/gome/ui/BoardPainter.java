/*
 * (c) 2006 Indigonauts
 */

package com.indigonauts.gome.ui;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import com.indigonauts.gome.Gome;
import com.indigonauts.gome.common.Point;
import com.indigonauts.gome.common.Rectangle;
import com.indigonauts.gome.common.Util;
import com.indigonauts.gome.sgf.Board;
import com.indigonauts.gome.sgf.SgfModel;
import com.indigonauts.gome.sgf.SgfNode;
import com.indigonauts.gome.sgf.SgfPoint;
import com.indigonauts.gome.sgf.SymbolAnnotation;
import com.indigonauts.gome.sgf.TextAnnotation;

public class BoardPainter {
  //#if DEBUG
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("BoardPainter");
  //#endif

  private static final int MARGIN = 0;

  private Board board;

  /**
   * These are BOARD coordinates
   */

  private Rectangle boardArea;

  /**
   * These are GRAPHIC coordinates
   */
  private Rectangle drawArea;

  // draw positions
  protected int delta;

  private int boardX;

  private int boardY;

  private int boardWidth;

  private int boardHeight;

  // for performance
  protected int halfdelta;

  private Image backBuffer;

  private static final Font ANNOTATION_FONT_SMALL = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_SMALL);

  private static final Font ANNOTATION_FONT_MEDIUM = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_MEDIUM);

  private static final Font ANNOTATION_FONT_LARGE = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_LARGE);

  private Font currentAnnotationFont;

  private Point ko;

  private boolean counting;

  private boolean doubleBuffered;

  protected int[] ghostWhiteStoneRGB;
  protected int[] ghostBlackStoneRGB;

  public BoardPainter(Board newBoard, Rectangle imageArea, Rectangle newBoardArea, boolean doubleBuffered) {
    board = newBoard;

    boardArea = newBoardArea != null ? newBoardArea : newBoard.getFullBoardArea();
    drawArea = imageArea;

    // calc the size of each cell
    calcDrawingPosition();
    this.doubleBuffered = doubleBuffered && Gome.singleton.options.optimize == Util.FOR_SPEED;
    if (this.doubleBuffered)
      resetBackBuffer();

  }

  public void setKo(Point ko) {
    this.ko = ko;
  }

  public void setCountingMode(boolean counting) {
    this.counting = counting;
  }

  /**
   * 
   * @param playArea there are BOARD coordinates
   */
  public void setPlayArea(Rectangle playArea) {
    boardArea = playArea;
    calcDrawingPosition();
    resetBackBuffer();
  }

  /**
   * 
   * @param imageArea these are GRAPHIC coordinates
   */
  public void setDrawArea(Rectangle imageArea) {
    drawArea = imageArea;
    calcDrawingPosition();
    resetBackBuffer();
  }

  private void resetBackBuffer() {
    if (doubleBuffered) {
      if (backBuffer == null || backBuffer.getWidth() != drawArea.getWidth() || backBuffer.getHeight() != drawArea.getHeight())
        backBuffer = Image.createImage(drawArea.getWidth(), drawArea.getHeight());
      if (board != null) {
        board.setChanged(true); // force a redraw
        board.setResetted(true); // force a redraw
      }
    }
  }

  public void drawBoard(Graphics graphicsScreen, Vector annotations) {
    Graphics graphics = null;

    int clipX0 = graphicsScreen.getClipX();
    int clipY0 = graphicsScreen.getClipY();
    int clipX1 = clipX0 + graphicsScreen.getClipWidth();
    int clipY1 = clipY0 + graphicsScreen.getClipHeight();

    int clipMokuX0 = (clipX0 - boardX) / delta + boardArea.x0;
    int clipMokuY0 = (clipY0 - boardY) / delta + boardArea.y0;
    int clipMokuX1 = (clipX1 - boardX) / delta + boardArea.x0;
    int clipMokuY1 = (clipY1 - boardY) / delta + boardArea.y0;

    if (clipMokuX0 < 0)
      clipMokuX0 = 0;

    if (clipMokuX1 > boardArea.x1)
      clipMokuX1 = boardArea.x1;

    if (clipMokuY0 < 0)
      clipMokuY0 = 0;

    if (clipMokuY1 > boardArea.y1)
      clipMokuY1 = boardArea.y1;

    //#if DEBUG
    //log.debug(clipMokuX0 + "," + clipMokuY0 + " " + clipMokuX1 + "," + clipMokuY1);
    //#endif

    if (doubleBuffered) {
      graphics = backBuffer.getGraphics();
    } else {
      graphics = graphicsScreen;
    }

    if (!doubleBuffered || board.isResetted()) {
      //redraw everything
      graphics.setColor(Util.TATAMI);
      graphics.fillRect(0, 0, drawArea.getWidth() + 2, drawArea.getHeight() + 2);
      board.setResetted(false);
    }
    if (doubleBuffered) {
      if (board.isChanged()) {
        boolean[][] changeMask = board.getChangeMask();
        // draw the empty first
        for (int x = clipMokuX0; x <= clipMokuX1; x++) {
          for (int y = clipMokuY0; y <= clipMokuY1; y++) {
            if (!changeMask[x][y] && board.getPosition(x, y) == Board.EMPTY) {

              drawCell(graphics, x, y);
              changeMask[x][y] = true;
            }
          }
        }

        for (int x = clipMokuX0; x <= clipMokuX1; x++) {
          for (int y = clipMokuY0; y <= clipMokuY1; y++) {
            if (!changeMask[x][y]) {
              drawCell(graphics, x, y);
              changeMask[x][y] = true;
            }
          }
        }
        board.setChanged(false);
      }
      graphicsScreen.drawImage(backBuffer, 0, 0, Graphics.TOP | Graphics.LEFT);
    } else {
      for (int x = clipMokuX0; x <= clipMokuX1; x++) {
        for (int y = clipMokuY0; y <= clipMokuY1; y++) {
          if (board.getPosition(x, y) == Board.EMPTY) {
            drawCell(graphics, x, y);
          }
        }
      }
      for (int x = clipMokuX0; x <= clipMokuX1; x++) {
        for (int y = clipMokuY0; y <= clipMokuY1; y++) {
          if (board.getPosition(x, y) != Board.EMPTY)
            drawCell(graphics, x, y);
        }
      }
    }

    if (counting) {
      drawTerritory(graphicsScreen);
      return; // nothing else
    }
    if (ko != null)
      drawSymbolAnnotation(graphicsScreen, new SymbolAnnotation(ko, SymbolAnnotation.SQUARE), Util.COLOR_GREY);

    if (annotations != null)
      drawAnnotations(graphicsScreen, annotations);
  }

  public void drawMe(Graphics g, Point cursor, int playerColor, boolean showHints, boolean markLastMove, SgfNode currentNode, SgfModel model) {
    drawBoard(g, currentNode.getAnnotations());

    // draw cursor
    if (cursor != null) {

      drawCursor(g, cursor, playerColor, board.getPosition(cursor) == Board.EMPTY); // guess the
    }

    // next color

    if (markLastMove) {
      renderStoneMarker(g, currentNode);
    }

    // draw hints
    if (showHints) {
      Vector children = currentNode.getChildren();
      Enumeration enume = children.elements();

      while (enume.hasMoreElements()) {
        int color = Util.COLOR_BLACK;
        SgfNode node = (SgfNode) (enume.nextElement());
        if (model.isCorrectNode(node)) {
          color = Util.COLOR_RED;
        }
        Point point = node.getPoint();

        if (point != null)
          drawSymbolAnnotation(g, new SymbolAnnotation(point, SymbolAnnotation.CIRCLE), color);
      }

    }

  }

  protected void renderStoneMarker(Graphics g, SgfNode currentNode) {
    SgfPoint point = currentNode.getPoint();
    if (point != null)
      drawSymbolAnnotation(g, new SymbolAnnotation(point, SymbolAnnotation.FILLED_CIRCLE), Util.COLOR_RED);
  }

  public void drawAnnotations(Graphics g, Vector annotations) {
    Enumeration e = annotations.elements();
    while (e.hasMoreElements()) {
      Point annotation = (Point) e.nextElement();

      if (getBoardArea().contains(annotation)) {
        int position = board.getPosition(annotation);

        int color = Board.BLACK; //Util.COLOR_BLUE;
        if (position == Board.BLACK) // if black draw in white
        {
          //color = 0x0092D3A0;
          color = Board.WHITE;
        }

        if (annotation instanceof TextAnnotation) {
          TextAnnotation textAnnotation = (TextAnnotation) annotation;

          drawTextAnnotation(g, annotation, textAnnotation.getText(), position == Board.EMPTY, color);
        } else // so it is a symbol
        {
          drawSymbolAnnotation(g, (SymbolAnnotation) annotation, color);

        }
      }
    }

  }

  public void drawTerritory(Graphics g) {
    byte[][] territory = board.getTerritory();
    byte boardSize = board.getBoardSize();
    SymbolAnnotation p = new SymbolAnnotation(SymbolAnnotation.SQUARE);
    int whiteTerritoryColor = Util.blendColors(Gome.singleton.options.gobanColor, Util.COLOR_WHITE);
    int blackTerritoryColor = Util.blendColors(Gome.singleton.options.gobanColor, Util.COLOR_BLACK);
    int blackTerritoryColorOnWhiteStone = Util.blendColors(Util.COLOR_DARKGREY, Util.COLOR_WHITE);
    int whiteTerritoryColorOnBlackStone = Util.blendColors(Util.COLOR_WHITE, Util.COLOR_LIGHTGREY);

    for (int i = 0; i < boardSize; i++) {
      for (int j = 0; j < boardSize; j++) {
        p.x = (byte) i;
        p.y = (byte) j;
        if (territory[i][j] == Board.WHITE) {
          if ((board.getPosition(p) == Board.BLACK))
            drawSymbolAnnotation(g, p, whiteTerritoryColorOnBlackStone);
          else
            drawSymbolAnnotation(g, p, whiteTerritoryColor);
        } else if (territory[i][j] == Board.BLACK) {
          if ((board.getPosition(p) == Board.WHITE))
            drawSymbolAnnotation(g, p, blackTerritoryColorOnWhiteStone);
          else
            drawSymbolAnnotation(g, p, blackTerritoryColor);
        }
        if (board.hasBeenRemove(p.x, p.y) && ((territory[i][j] != Board.BLACK) && territory[i][j] != Board.WHITE)) {
          drawSymbolAnnotation(g, p, Util.COLOR_LIGHTGREY);
        }

      }
    }
  }

  /**
   * board position (0-18) to the pixel positioin
   */
  public int getCellX(int x) {
    return boardX + MARGIN + halfdelta + ((x - boardArea.x0) * delta);
  }

  public int getCellY(int y) {
    return boardY + MARGIN + halfdelta + ((y - boardArea.y0) * delta);
  }

  /**
   * pixel position to board position(0-18)
   */
  public byte getBoardX(int x) {
    return (byte) (((x - boardX - MARGIN - (delta / 4)) / delta) + boardArea.x0);
  }

  public byte getBoardY(int y) {
    return (byte) (((y - boardY - MARGIN - (delta / 4)) / delta) + boardArea.y0);
  }

  protected void calcDrawingPosition() {
    int deltax = (drawArea.getWidth() - (MARGIN * 2)) / boardArea.getWidth();
    int deltay = (drawArea.getHeight() - (MARGIN * 2)) / boardArea.getHeight();
    delta = Math.min(deltax, deltay);
    //#if DEBUG
    log.debug("delta = " + delta);
    //#endif
    halfdelta = delta / 2;

    // how big is the board actually drawn (inside the border)
    boardWidth = (delta * boardArea.getWidth()) + (MARGIN * 2);
    boardHeight = (delta * boardArea.getHeight()) + (MARGIN * 2);

    // the top left cornor that we start drawing
    boardX = drawArea.x0 + ((drawArea.getWidth() - boardWidth) / 2);
    boardY = drawArea.y0 + ((drawArea.getHeight() - boardHeight) / 2);

    // choose the biggest font possible for annotations
    if (ANNOTATION_FONT_LARGE.getHeight() < delta) {
      currentAnnotationFont = ANNOTATION_FONT_LARGE;
    } else if (ANNOTATION_FONT_MEDIUM.getHeight() < delta) {
      currentAnnotationFont = ANNOTATION_FONT_MEDIUM;
    } else {
      currentAnnotationFont = ANNOTATION_FONT_SMALL;
    }

    if (Gome.singleton.options.ghostStone != 0) {
      int size = delta + 1;
      ghostWhiteStoneRGB = new int[size * size];
      ghostBlackStoneRGB = new int[size * size];

      Image cachedStone = Image.createImage(size, size);
      Graphics g = cachedStone.getGraphics();
      g.setColor(Gome.singleton.options.gobanColor);
      g.fillRect(0, 0, size, size);
      drawVectStone(g, 0, 0, delta, Board.WHITE);
      cachedStone.getRGB(ghostWhiteStoneRGB, 0, size, 0, 0, size, size);
      drawVectStone(g, 0, 0, delta, Board.BLACK);
      cachedStone.getRGB(ghostBlackStoneRGB, 0, size, 0, 0, size, size);
      int len = ghostWhiteStoneRGB.length;
      int trans = Gome.singleton.options.ghostStone << 29;
      for (int i = 0; i < len; i++) {
        ghostWhiteStoneRGB[i] = trans | (ghostWhiteStoneRGB[i] & 0x00FFFFFF); // get the color of the pixel.
        ghostBlackStoneRGB[i] = trans | (ghostBlackStoneRGB[i] & 0x00FFFFFF); // get the color of the pixel.
      }
    }
  }

  private void drawCell(Graphics g, int x, int y) {

    int cx = getCellX(x);
    int cy = getCellY(y);

    int tlx = cx - halfdelta; // top left
    int tly = cy - halfdelta;
    g.setColor(Gome.singleton.options.gobanColor);
    g.fillRect(tlx, tly, delta, delta);
    int position = board.getPosition(x, y);

    switch (position) {
    case Board.BLACK:
    case Board.WHITE:
      drawBorder(g, x, y, 0xCCCCCC);
      drawStone(g, cx, cy, delta, position);
      break;
    default:
      drawEmpty(g, x, y);
      drawBorder(g, x, y, 0xCCCCCC);
    }
  }

  protected void drawStone(Graphics g, int cx, int cy, int size, int playerColor) {

    drawVectStone(g, cx - halfdelta, cy - halfdelta, size, playerColor == Board.WHITE ? Util.COLOR_WHITE : Util.COLOR_BLACK);
  }

  public static void drawVectStone(Graphics g, int tlx, int tly, int size, int color) {

    g.setColor(color);

    if (Gome.singleton.options.stoneBug == 1)
      g.fillArc(tlx + 1, tly + 1, size - 1, size - 1, 0, 360);
    else
      g.fillArc(tlx, tly, size, size, 0, 360);

    g.setColor(Util.COLOR_GREY);
    g.drawArc(tlx, tly, size, size, 0, 360);

  }

  private void drawBorder(Graphics g, int x, int y, int color) {
    g.setColor(color);
    int cx = getCellX(x);
    int cy = getCellY(y);

    int tlx = cx - halfdelta; // top left
    int tly = cy - halfdelta;

    if (y == 0) {
      g.drawLine(tlx, tly, tlx + delta, tly);
    } else if (y == board.getBoardSize() - 1) {
      g.drawLine(tlx, tly + delta, tlx + delta, tly + delta);
    }
    if (x == 0) {
      g.drawLine(tlx, tly, tlx, tly + delta);
    } else if (x == board.getBoardSize() - 1) {
      g.drawLine(tlx + delta, tly, tlx + delta, tly + delta);
    }

  }

  private void drawEmpty(Graphics g, int x, int y) {
    int cx = getCellX(x);
    int cy = getCellY(y);

    int tlx = cx - halfdelta; // top left
    int tly = cy - halfdelta;
    int i3 = cx; // center
    int j3 = cy;
    int brx = cx + halfdelta; // bottom right
    int bry = cy + halfdelta;

    g.setColor(Util.COLOR_DARKGREY);

    // draw up
    if (y > 0) {
      g.drawLine(i3, j3, i3, tly);
    }

    // draw left
    if (x > 0) {
      g.drawLine(i3, j3, tlx, j3);
    }

    // draw down
    if (y < (board.getBoardSize() - 1)) {
      g.drawLine(i3, j3, i3, bry);
    }

    // draw right
    if (x < (board.getBoardSize() - 1)) {
      g.drawLine(i3, j3, brx, j3);
    }

    boolean isPoint = false;

    if (board.getBoardSize() == 19) {
      if (((x == 3) || (x == 15) || (x == 9)) && ((y == 3) || (y == 15) || (y == 9))) {
        isPoint = true;
      }
    }

    if (board.getBoardSize() == 13) {
      if (((x == 3) || (x == 6) || (x == 9)) && ((y == 3) || (y == 6) || (y == 9))) {
        isPoint = true;
      }
    }

    if (board.getBoardSize() == 9) {
      if (((x == 2) || (x == 6)) && ((y == 2) || (y == 6))) {
        isPoint = true;
      } else if ((x == 4) && (y == 4)) {
        isPoint = true;
      }
    }

    if (isPoint) {
      g.drawRect(i3 - 1, j3 - 1, 2, 2);
    }

  }

  public void drawCursor(Graphics g, Point c, int playerColor, boolean phantom) {
    int cx = getCellX(c.x);
    int cy = getCellY(c.y);
    if (counting)
      g.setColor(Util.COLOR_GREEN);
    else if (playerColor == Board.WHITE) {
      g.setColor(Util.COLOR_WHITE);
      if (phantom & Gome.singleton.options.ghostStone != 0)
        g.drawRGB(ghostWhiteStoneRGB, 0, delta + 1, cx - halfdelta, cy - halfdelta, delta + 1, delta + 1, true);
    } else {
      g.setColor(Util.COLOR_DARKGREY);
      if (phantom & Gome.singleton.options.ghostStone != 0)
        g.drawRGB(ghostBlackStoneRGB, 0, delta + 1, cx - halfdelta, cy - halfdelta, delta + 1, delta + 1, true);

    }

    int upx = cx - halfdelta - 1;
    int upy = cy - halfdelta - 1;
    int downx = cx + halfdelta + 1;
    int downy = cy + halfdelta + 1;
    int q = halfdelta / 2;

    g.drawLine(upx, upy, upx + q, upy);
    g.drawLine(upx, upy, upx, upy + q);

    g.drawLine(downx, upy, downx - q, upy);
    g.drawLine(downx, upy, downx, upy + q);

    g.drawLine(downx, downy, downx - q, downy);
    g.drawLine(downx, downy, downx, downy - q);

    g.drawLine(upx, downy, upx + q, downy);
    g.drawLine(upx, downy, upx, downy - q);

    g.drawLine(upx, upy + 1, upx + q, upy + 1);
    g.drawLine(upx + 1, upy, upx + 1, upy + q);

    g.drawLine(downx, upy + 1, downx - q, upy + 1);
    g.drawLine(downx - 1, upy, downx - 1, upy + q);

    g.drawLine(downx, downy - 1, downx - q, downy - 1);
    g.drawLine(downx - 1, downy, downx - 1, downy - q);

    g.drawLine(upx, downy - 1, upx + q, downy - 1);
    g.drawLine(upx + 1, downy, upx + 1, downy - q);

  }

  public void drawTextAnnotation(Graphics g, Point pt, String text, boolean erasebg, int color) {

    int cx = getCellX(pt.x);
    int cy = getCellY(pt.y);

    g.setFont(currentAnnotationFont);

    int y = cy - (delta - currentAnnotationFont.getHeight()) / 2 + halfdelta;
    if (erasebg) {
      g.setColor(Gome.singleton.options.gobanColor);
      g.drawString(text, cx - 1, y, Graphics.BOTTOM | Graphics.HCENTER);
      g.drawString(text, cx, y - 1, Graphics.BOTTOM | Graphics.HCENTER);
      g.drawString(text, cx + 1, y, Graphics.BOTTOM | Graphics.HCENTER);
      g.drawString(text, cx, y + 1, Graphics.BOTTOM | Graphics.HCENTER);
      g.drawString(text, cx - 2, y, Graphics.BOTTOM | Graphics.HCENTER);
      g.drawString(text, cx, y - 2, Graphics.BOTTOM | Graphics.HCENTER);
      g.drawString(text, cx + 2, y, Graphics.BOTTOM | Graphics.HCENTER);
      g.drawString(text, cx, y + 2, Graphics.BOTTOM | Graphics.HCENTER);
    }

    g.setColor(color);
    g.drawString(text, cx, y, Graphics.BOTTOM | Graphics.HCENTER);
  }

  public void drawSymbolAnnotation(Graphics g, SymbolAnnotation annotation, int color) {
    int cx = getCellX(annotation.x);
    int cy = getCellY(annotation.y);
    g.setColor(color);
    int proportion;

    switch (annotation.getType()) {
    case SymbolAnnotation.CIRCLE:
      proportion = delta / 4;
      g.drawArc(cx - (proportion), cy - (proportion), proportion * 2, proportion * 2, 0, 360);
      break;
    case SymbolAnnotation.FILLED_CIRCLE:
      proportion = delta / 4;
      g.fillArc(cx - (proportion), cy - (proportion), proportion * 2 + 1, proportion * 2 + 1, 0, 360);
      break;
    case SymbolAnnotation.CROSS:
      proportion = (halfdelta * 1000) / 1414; // sqrt2
      g.drawLine(cx - proportion, cy - proportion, cx + proportion, cy + proportion);
      g.drawLine(cx - proportion, cy + proportion, cx + proportion, cy - proportion);
      break;
    case SymbolAnnotation.SQUARE:
      proportion = delta / 4;
      g.fillRect(cx - (proportion), cy - (proportion), proportion * 2 + 1, proportion * 2 + 1);
      break;
    case SymbolAnnotation.TRIANGLE:
      int halfm1 = halfdelta - 1;
      proportion = sin120(halfm1);
      g.drawLine(cx, cy - halfm1, cx - proportion, cy + halfm1 / 2);
      g.drawLine(cx - proportion, cy + halfm1 / 2, cx + proportion, cy + halfm1 / 2);
      g.drawLine(cx + proportion, cy + halfm1 / 2, cx, cy - halfm1);
      g.drawLine(cx, cy - halfm1 + 1, cx - proportion, cy + halfm1 / 2 + 1);
      g.drawLine(cx - proportion, cy + halfm1 / 2 + 1, cx + proportion, cy + halfm1 / 2 + 1);
      g.drawLine(cx + proportion, cy + halfm1 / 2 + 1, cx, cy - halfm1 + 1);

      break;

    }

  }

  int sin120(int x) {
    return (866 * x) / 1000;
  }

  Rectangle getViewedArea() {
    return boardArea;
  }

  // /**
  // * @return Returns the drawArea.
  // */
  // GraphicRectangle getDrawArea() {
  // return drawArea;
  // }

  /**
   * @return Returns the boardArea.
   */
  public Rectangle getBoardArea() {
    return boardArea;
  }

  /**
   * @return Returns the delta.
   */
  public int getDelta() {
    return delta;
  }

  // public Image getBackBuffer() {
  // return backBuffer;
  // }

  public int getEffectiveHeight(int width, int height) {

    int deltax = (width - (MARGIN * 2)) / boardArea.getWidth();
    int deltay = (height - (MARGIN * 2)) / boardArea.getHeight();
    int delta2 = Math.min(deltax, deltay);
    return (delta2 * boardArea.getHeight()) + (MARGIN * 2) + 2;

  }

  /**
   * 
   * @return GRAPHIC coordinates
   */
  public int getWidth() {
    return drawArea.getWidth();
  }

  /**
   * 
   * @return GRAPHIC coordinates
   */
  public int getHeight() {
    return drawArea.getHeight();
  }

  /**
   * 
   * @return GRAPHIC coordinates
   */
  public Rectangle getDrawArea() {
    return drawArea;
  }

}
