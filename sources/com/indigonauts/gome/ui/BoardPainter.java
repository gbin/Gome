/*
 * (c) 2006 Indigonauts
 */

package com.indigonauts.gome.ui;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import com.indigonauts.gome.Gome;
import com.indigonauts.gome.common.Point;
import com.indigonauts.gome.common.Rectangle;
import com.indigonauts.gome.common.Util;
import com.indigonauts.gome.sgf.Board;
import com.indigonauts.gome.sgf.SgfModel;
import com.indigonauts.gome.sgf.SgfNode;
import com.indigonauts.gome.sgf.SymbolAnnotation;
import com.indigonauts.gome.sgf.TextAnnotation;

public class BoardPainter {
  // private static final org.apache.log4j.Logger log =
  // org.apache.log4j.Logger.getLogger("BoardPainter");

  private static final int MARGIN = 0;

  private Board board;

  private Rectangle boardArea;

  private GraphicRectangle drawArea;
  
  // draw positions
  private int delta;

  private int boardX;

  private int boardY;

  private int boardWidth;

  private int boardHeight;

  // for performance
  private int halfdelta;

  private static final Font ANNOTATION_FONT_SMALL = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_SMALL);

  private static final Font ANNOTATION_FONT_MEDIUM = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_MEDIUM);

  private static final Font ANNOTATION_FONT_LARGE = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_LARGE);

  private Font currentAnnotationFont;

  private Point ko;

  private boolean counting;
  private boolean smartCounting;

  public BoardPainter(Board newBoard, GraphicRectangle imageArea, Rectangle newBoardArea) {
    board = newBoard;
    
    boardArea = newBoardArea != null ? newBoardArea : newBoard.getFullBoardArea();
    drawArea = imageArea;

    // calc the size of each cell
    calcDrawingPosition();

    resetBackBuffer();
  }

  /*public void setAnnotations(Vector annotations) {
    this.annotations = annotations;
  }*/

  public void setKo(Point ko) {
    this.ko = ko;
  }

  public void setCountingMode(boolean counting, boolean smartCounting) {
    this.counting = counting;
    this.smartCounting = smartCounting;
  }

  public void setPlayArea(Rectangle playArea) {
    boardArea = playArea;
    calcDrawingPosition();
  }

  public void setDrawArea(GraphicRectangle imageArea) {
    drawArea = imageArea;
    calcDrawingPosition();
    resetBackBuffer();
  }

  /**
   * Attach this drawing tool to a live game
   * 
   * @param gc
   */
  /*public void setGameController(GameController gc) {
    this.gc = gc;
  }*/

  private void resetBackBuffer() {
    /*
     * if (backBuffer == null || backBuffer.getWidth() !=
     * drawArea.getWidth() || backBuffer.getHeight() !=
     * drawArea.getHeight()) backBuffer =
     * Image.createImage(drawArea.getWidth(), drawArea .getHeight());
     */

  }

  public void drawBoard(Graphics graphics, Vector annotations) {
    graphics.setColor(Gome.singleton.options.gobanColor);

    graphics.fillRect(0, 0, drawArea.getWidth()+2, drawArea.getHeight()+2); //FIXME: ca va pas

    int ox = getCellX(0);
    int oy = getCellY(0);
    int oxx = getCellX(board.getBoardSize() - 1);
    int oyy = getCellY(board.getBoardSize() - 1);

    int ux = getCellX(boardArea.x0);
    int uy = getCellY(boardArea.y0);
    graphics.setClip(ux - halfdelta, uy - halfdelta, getCellX(boardArea.x1 + 1) - ux + 1, getCellY(boardArea.y1 + 1) - uy + 1);
    graphics.setColor(Util.COLOR_WHITE);
    graphics.drawRect(ox - halfdelta, oy - halfdelta, oxx - ox + halfdelta + halfdelta -1 , oyy - oy + halfdelta + halfdelta -1);

    graphics.setClip(0, 0, Integer.MAX_VALUE, Integer.MAX_VALUE);
    graphics.setColor(Util.COLOR_BLACK);
    for (byte x = boardArea.x0; x <= boardArea.x1; x++) {
      for (byte y = boardArea.y0; y <= boardArea.y1; y++) {
        drawCell(graphics, new Point(x, y));
      }
    }
    if (counting) {
      drawTerritory(graphics);
      return; // nothing else
    }
    if (ko != null)
      drawSymbolAnnotation(graphics, new SymbolAnnotation(ko, SymbolAnnotation.SQUARE), Util.COLOR_GREY);

    if (annotations != null)
      drawAnnotations(graphics, annotations);

  }

  
  /*public void drawMe(Graphics g) {
    Point cursor = gc.getCursor();
    int playerColor = gc.getCurrentPlayerColor();
    boolean showHints = gc.getShowHints();
    SgfNode currentNode = gc.getCurrentNode();
    SgfModel model = gc.getSgfModel();
   }*/
    
  public void drawMe(Graphics g, Point cursor, int playerColor, boolean showHints, SgfNode currentNode, SgfModel model) {
    // clone the latest buffer
    // g.drawImage(backBuffer, 0, 0, Graphics.TOP | Graphics.LEFT);
    drawBoard(g, currentNode.getAnnotations());

    // draw cursor
    if(cursor != null)
      drawCursor(g, cursor, playerColor); // guess the
    // next color

    // draw hints
    if (showHints) {
      Vector children = currentNode.getChildren();
      Enumeration enume = children.elements();

      // draw first son, red if it's in the main branch
      if (enume.hasMoreElements()) {
        int color = Util.COLOR_BLACK;
        SgfNode node = (SgfNode) (enume.nextElement());
        if (model.isCorrectNode(node)) {
          color = Util.COLOR_RED;
        }

        Point point = node.getPoint();

        if (point != null)
          drawSymbolAnnotation(g, new SymbolAnnotation(point, SymbolAnnotation.CIRCLE), color);
      }

      // draw other children
      while (enume.hasMoreElements()) {
        SgfNode node = (SgfNode) (enume.nextElement());
        Point point = node.getPoint();
        if (point != null)
          drawSymbolAnnotation(g, new SymbolAnnotation(point, SymbolAnnotation.CIRCLE), Util.COLOR_BLACK);
      }
    }

  }

  public void drawAnnotations(Graphics g, Vector annotations) {
    Enumeration e = annotations.elements();
    while (e.hasMoreElements()) {
      Point annotation = (Point) e.nextElement();

      if (getBoardArea().contains(annotation)) {
        int position = board.getPosition(annotation);

        int color = Util.COLOR_BLUE;
        if (position == 1) // if black draw in white
        {
          color = 0x0092D3A0;
        }

        if (annotation instanceof TextAnnotation) {
          TextAnnotation textAnnotation = (TextAnnotation) annotation;

          drawTextAnnotation(g, annotation, textAnnotation.getText(), position == 0, color);
        } else // so it is a symbol
        {
          drawSymbolAnnotation(g, (SymbolAnnotation) annotation, color);

        }
      }
    }

  }

  public void drawTerritory(Graphics g) {
    byte[][] territory = board.guessTerritory(smartCounting);
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
          drawSymbolAnnotation(g, p, Util.COLOR_RED);
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

  private void calcDrawingPosition() {
    int deltax = (drawArea.getWidth() - (MARGIN * 2)) / boardArea.getWidth();
    int deltay = (drawArea.getHeight() - (MARGIN * 2)) / boardArea.getHeight();
    delta = Math.min(deltax, deltay);
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

  }

  private void drawCell(Graphics g, Point pt) {
    int position = board.getPosition(pt);
    switch (position) {
    case Board.BLACK:
      drawStone(g, pt, Util.COLOR_BLACK);
      break;
    case Board.WHITE:
      drawStone(g, pt, Util.COLOR_WHITE);
      break;
    default:
      drawEmpty(g, pt);
    }
  }

  private void drawStone(Graphics g, Point pt, int color) {
    int cx = getCellX(pt.x);
    int cy = getCellY(pt.y);
    int w = delta;
    int x = cx - halfdelta;
    int y = cy - halfdelta;
    g.setColor(color);

    //#ifdef MIDP2
    g.fillArc(x + 1, y + 1, w - 1, w - 1, 0, 360);
    //#else
    //# g.fillArc(x, y, w, w, 0, 360);
    //#endif 

    g.setColor(Util.COLOR_GREY);
    g.drawArc(x, y, w, w, 0, 360);
    
  }

  private void drawEmpty(Graphics g, Point pt) {
    int cx = getCellX(pt.x);
    int cy = getCellY(pt.y);

    int k = pt.x;
    int l = pt.y;
    int k2 = cx - halfdelta; // top left
    int l2 = cy - halfdelta;
    int i3 = cx; // center
    int j3 = cy;
    int k3 = cx + halfdelta; // bottom right
    int l3 = cy + halfdelta;

    g.setColor(Util.COLOR_DARKGREY); // master.fcolorP);

    // draw up
    if (l > 0) {
      g.drawLine(i3, j3, i3, l2);
    }

    // draw left
    if (k > 0) {
      g.drawLine(i3, j3, k2, j3);
    }

    // draw down
    if (l < (board.getBoardSize() - 1)) {
      g.drawLine(i3, j3, i3, l3);
    }

    // draw right
    if (k < (board.getBoardSize() - 1)) {
      g.drawLine(i3, j3, k3, j3);
    }

    boolean isPoint = false;

    if (board.getBoardSize() == 19) {
      if (((k == 3) || (k == 15) || (k == 9)) && ((l == 3) || (l == 15) || (l == 9))) {
        isPoint = true;
      }
    }

    if (board.getBoardSize() == 13) {
      if (((k == 3) || (k == 6) || (k == 9)) && ((l == 3) || (l == 6) || (l == 9))) {
        isPoint = true;
      }
    }

    if (board.getBoardSize() == 9) {
      if (((k == 2) || (k == 6)) && ((l == 2) || (l == 6))) {
        isPoint = true;
      } else if ((k == 4) && (l == 4)) {
        isPoint = true;
      }
    }

    if (isPoint) {
      g.drawRect(i3 - 1, j3 - 1, 2, 2);
    }

  }

  public void drawCursor(Graphics g, Point c, int color) {
    int cx = getCellX(c.x);
    int cy = getCellY(c.y);

    if (color == -1)
      g.setColor(Util.COLOR_WHITE);
    else {
      g.setColor(Util.COLOR_DARKGREY);
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

    if (color == -1) {
      g.drawLine(upx, upy + 1, upx + q, upy + 1);
      g.drawLine(upx + 1, upy, upx + 1, upy + q);

      g.drawLine(downx, upy + 1, downx - q, upy + 1);
      g.drawLine(downx - 1, upy, downx - 1, upy + q);

      g.drawLine(downx, downy - 1, downx - q, downy - 1);
      g.drawLine(downx - 1, downy, downx - 1, downy - q);

      g.drawLine(upx, downy - 1, upx + q, downy - 1);
      g.drawLine(upx + 1, downy, upx + 1, downy - q);
    }

  }

  public void drawTextAnnotation(Graphics g, Point pt, String text, boolean erasebg, int color) {

    int cx = getCellX(pt.x);
    int cy = getCellY(pt.y);
    if (erasebg) {
      g.setColor(Gome.singleton.options.gobanColor);
      g.fillRect(cx - halfdelta, cy - halfdelta, delta, delta);
    }
    g.setFont(currentAnnotationFont);
    g.setColor(color);

    int offset = (delta - currentAnnotationFont.getHeight()) / 2;

    g.drawString(text, cx, cy - offset + halfdelta, Graphics.BOTTOM | Graphics.HCENTER);
  }

  public void drawSymbolAnnotation(Graphics g, SymbolAnnotation annotation, int color) {
    int cx = getCellX(annotation.x);
    int cy = getCellY(annotation.y);
    g.setColor(color);
    int proportion;

    switch (annotation.getType()) {
    case SymbolAnnotation.CIRCLE:
      proportion = delta / 4;
      g.drawArc(cx - (proportion), cy - (proportion), proportion * 2 + 1, proportion * 2 + 1, 0, 360);
      break;
    case SymbolAnnotation.CROSS:
      proportion = delta / 3;
      g.drawLine(cx - proportion, cy - proportion, cx + proportion, cy + proportion);
      g.drawLine(cx - proportion, cy + proportion, cx + proportion, cy - proportion);
      break;
    case SymbolAnnotation.SQUARE:
      proportion = delta / 4;
      g.fillRect(cx - (proportion), cy - (proportion), proportion * 2 + 1, proportion * 2 + 1);
      break;
    case SymbolAnnotation.TRIANGLE:
      proportion = (delta * 2) / 5;
      g.drawLine(cx, cy - (proportion), cx - (proportion), cy + (proportion));
      g.drawLine(cx - (proportion), cy + (proportion), cx + (proportion), cy + (proportion));
      g.drawLine(cx + (proportion), cy + (proportion), cx, cy - (proportion));
      break;

    }

  }

  Rectangle getPlayArea() {
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

  public int getWidth() {
    return drawArea.x1 - drawArea.x0;
  }

  public int getHeight() {
    return drawArea.y1 - drawArea.y0;
  }

}
