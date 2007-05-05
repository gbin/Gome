/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome.sgf;

import java.util.Enumeration;
import java.util.Stack;
import java.util.Vector;

import com.indigonauts.gome.common.Point;
import com.indigonauts.gome.common.Rectangle;

/**
 * Represents the game board (2D array)
 */
public class Board {
  public static final byte BLACK = 1;

  public static final byte WHITE = -1;

  public static final byte EMPTY = 0;

  public static final char ONLINE_MODE = 'O';

  public static final char GAME_MODE = 'G';
  private static final byte UNDETERMINED_TERRITORY = -2;

  public static final byte[][] DIRECTIONS = { { 0, -1 }, { 1, 0 }, { 0, 1 }, { -1, 0 } };

  private byte[][] board; // use as board[x][y]

  private byte boardSize;

  private Rectangle fullBoardArea;

  private int nbCapturedBlack;

  private int nbCapturedWhite;

  private Point ko;

  private boolean[][] dead;

  private boolean[][] markers;

  /**
   * When evaluationCountingMode is true, 
   * It will use an evaluation algo for the territory and not an exact counting
   */
  private boolean evaluationCountingMode = false;

  /**
   * When smartmarking is true, it will find the entire dead stones within a territory when marking
   */
  private boolean smartMarking = false;

  private boolean ressucite = false;

  public Board() {
    reset((byte) 19);
  }

  public Board(byte newBoardSize) {
    reset(newBoardSize);
  }

  /**
   * 
   * @param newBoardSize
   */
  public void reset(byte newBoardSize) {
    boardSize = newBoardSize;
    fullBoardArea = new Rectangle((byte) 0, (byte) 0, (byte) (boardSize - 1), (byte) (boardSize - 1));
    board = new byte[boardSize][boardSize];
  }

  /**
   * place the stone as current player on given point, it will not check if
   * this move legal, the caller should guarentee to call isLegalMove before.
   * 
   * it will reverse the current player after placing the stone
   * 
   * @param point
   * @return the dead stones, e.g. a vector of Point objects
   */
  public Vector play(Point point, byte player) {
    ko = null; // reset the ko
    placeStone(point, player);
    Vector vec = this.getDeadStonesFromMove(point, player);
    if (vec != null) {
      placeStones(vec, EMPTY);
      if (player == 1)
        nbCapturedWhite += vec.size();
      else
        nbCapturedBlack += vec.size();
    }
    if (vec.size() == 1) // only one stone captured ? perhaps it is a ko
    // ?
    {
      Point capturedStone = (Point) vec.elementAt(0);
      if (isItAKo(point, capturedStone, player)) {
        ko = capturedStone;
      }
    }

    return vec;
  }

  private boolean isItAKo(Point playedMove, Point capturedStone, int player) {
    int x = playedMove.x;
    int y = playedMove.y;

    int captx = capturedStone.x;
    int capty = capturedStone.y;

    if (!(x + 1 == captx && y == capty) && x + 1 < boardSize && board[x + 1][y] != -player) {
      return false;
    }

    if (!(x - 1 == captx && y == capty) && x - 1 >= 0 && board[x - 1][y] != -player) {
      return false;
    }

    if (!(x == captx && y + 1 == capty) && y + 1 < boardSize && board[x][y + 1] != -player) {
      return false;
    }

    if (!(x == captx && y - 1 == capty) && y - 1 >= 0 && board[x][y - 1] != -player) {
      return false;
    }
    return true;

  }

  public void placeStone(Point point, byte color) {
    if (point == null)
      return;

    int x = point.x;
    int y = point.y;
    if (x >= board.length || y >= board.length)
      return;
    board[x][y] = color;
  }

  public boolean isValidMove(Point pt, byte color) {
    if (this.getPosition(pt) != EMPTY)
      return false;

    if (ko != null && pt.x == ko.x && pt.y == ko.y)
      return false;

    boolean result = false;

    // place a stone just for testing suicide, it will be removed at the
    // end of this function
    this.placeStone(pt, color);

    Vector vec = this.getDeadStonesFromMove(pt, color);
    if (vec.size() > 0) {
      result = true; // it's valid if any enemy can be killed
    } else {
      int[][] history = new int[boardSize][boardSize];
      vec = this.findDeadGroup(pt, history);

      // if (vec.size()>0) result= false; else result=true;
      result = !(vec.size() > 0);

      // if it kills no enemy but generate a dead group it self, it's
      // invalid
      // i.e. it's suiciding
    }

    this.placeStone(pt, EMPTY);

    return result;

  }

  public byte getBoardSize() {
    return boardSize;
  }

  public int getPosition(Point pt) {
    return board[pt.x][pt.y];
  }

  /**
   * suppose *lastplayer* just played at *lastmove* (the board is already
   * changed) find out the opponendt's dead groups (but not set them to EMPTY
   * yet)
   * 
   * @param lastmove
   * @param lastplayer
   * @return
   */
  public Vector getDeadStonesFromMove(Point lastmove, int lastplayer) {
    Vector vec = new Vector();

    Rectangle area = fullBoardArea;

    int[][] history = new int[boardSize][boardSize];
    for (int i = 0; i < DIRECTIONS.length; ++i) {
      Point nextpt = new Point(lastmove);
      nextpt.move(DIRECTIONS[i][0], DIRECTIONS[i][1]);

      if (area.contains(nextpt) && (this.getPosition(nextpt) * lastplayer == -1) && history[nextpt.x][nextpt.y] == 0) {
        Vector tempVec = this.findDeadGroup(nextpt, history);
        for (Enumeration e = tempVec.elements(); e.hasMoreElements();) {
          vec.addElement(e.nextElement());
        }
      }

    }
    return vec;
  }

  /**
   * start from the given point, test if this is a dead group
   * 
   * It's a dead group if 1. no liberty is found (the dead groupis returned)
   * or 2. it's connected to a certain known dead group that is stored in
   * history (empty is returned because it's already in the history)
   * 
   * @param pt
   *            the point need to test
   * @param history
   *            a board that contains only the already known dead stones.
   * @return the dead group only if it's not in the history yet.
   */
  public Vector findDeadGroup(Point pt, int[][] history) {
    Stack stack = new Stack();
    Vector deadStones = new Vector();
    int deadcolor = this.getPosition(pt);
    Rectangle area = fullBoardArea;

    stack.push(pt);
    history[pt.x][pt.y] = 1;

    while (!stack.isEmpty()) {
      Point curpt = (Point) stack.peek();
      int status = history[curpt.x][curpt.y];

      // if all 4 directions are searched, mark dead stone, pop the stack,
      // and skip the loop
      if (status == 5) {
        deadStones.addElement(curpt);
        stack.pop();
        continue;
      }

      // calc next point
      Point nextpt = new Point(curpt);
      if (status >= 1 && status <= 4) {
        history[curpt.x][curpt.y] = status + 1;
        nextpt.move(DIRECTIONS[status - 1][0], DIRECTIONS[status - 1][1]);
      }

      if (!area.contains(nextpt))
        continue;

      if (history[nextpt.x][nextpt.y] > 0)
        continue;

      int nextcolor = this.getPosition(nextpt);
      if (nextcolor * deadcolor == -1)
        continue;
      if (nextcolor == deadcolor) {
        stack.push(nextpt);
        history[nextpt.x][nextpt.y] = 1;
        continue;
      }
      // here, nextcolor==0
      {
        for (Enumeration e = deadStones.elements(); e.hasMoreElements();) {
          Point deadpt = (Point) (e.nextElement());
          history[deadpt.x][deadpt.y] = 0;
        }
        for (Enumeration e = stack.elements(); e.hasMoreElements();) {
          Point stackpt = (Point) (e.nextElement());
          history[stackpt.x][stackpt.y] = 0;
        }
        deadStones.removeAllElements();
        break;
      }

    }// while

    return deadStones;
  }

  public void placeStones(Vector vec, byte color) {
    if (vec == null)
      return;

    for (Enumeration e = vec.elements(); e.hasMoreElements();) {
      Point pt = (Point) (e.nextElement());

      this.placeStone(pt, color);
    }
  }

  /**
   * @return Returns the nbCapturedBlack.
   */
  public int getNbCapturedBlack() {
    return nbCapturedBlack;
  }

  /**
   * @return Returns the nbCapturedWhite.
   */
  public int getNbCapturedWhite() {
    return nbCapturedWhite;
  }

  /**
   * Used when we go back in a game
   */
  public void compensateScore(int nb, int deadColor) {
    if (deadColor == 1) {
      nbCapturedBlack -= nb;
    } else {
      nbCapturedWhite -= nb;
    }
  }

  /**
   * @return Returns the ko.
   */
  public Point getKo() {
    return ko;
  }

  /**
   * Recursively mark all unmarked places with the given color & empty
   */
  private void recurseSmartMarkDead(int i, int j, byte color) {
    if (markers[i][j] || (board[i][j] == -color))
      return;
    markers[i][j] = true;
    if (board[i][j] == color)
      dead[i][j] = !ressucite;
    if (i > 0)
      recurseSmartMarkDead(i - 1, j, color);
    if (j > 0)
      recurseSmartMarkDead(i, j - 1, color);
    if (i < boardSize - 1)
      recurseSmartMarkDead(i + 1, j, color);
    if (j < boardSize - 1)
      recurseSmartMarkDead(i, j + 1, color);
  }

  private void recurseDumbMarkDead(int i, int j, byte color) {
    if (markers[i][j] || (board[i][j] == -color))
      return;
    markers[i][j] = true;
    if (board[i][j] == color)
      dead[i][j] = !ressucite;
    if (i > 0 && board[i - 1][j] == color)
      recurseDumbMarkDead(i - 1, j, color);
    if (j > 0 && board[i][j - 1] == color)
      recurseDumbMarkDead(i, j - 1, color);
    if (i < boardSize - 1 && board[i + 1][j] == color)
      recurseDumbMarkDead(i + 1, j, color);
    if (j < boardSize - 1 && board[i][j + 1] == color)
      recurseDumbMarkDead(i, j + 1, color);
  }

  public void startCounting(boolean evaluationMode, boolean smartMarking) {
    dead = new boolean[boardSize][boardSize];
    evaluationCountingMode = evaluationMode;
    this.smartMarking = smartMarking;
  }

  public void endCounting() {
    dead = null;
    markers = null;
  }

  /**
   * cancel all markings
   */
  private void unMark() {
    markers = new boolean[boardSize][boardSize];
  }

  /**
   * mark/unmark a group at (x,y)
   */
  public void markDeadGroup(int x, int y) {
    unMark();
    ressucite = dead[x][y];
    if (smartMarking) {
      recurseSmartMarkDead(x, y, board[x][y]);
    } else {
      recurseDumbMarkDead(x, y, board[x][y]);
    }
  }

  /**
   * Recursively mark a group of color c starting from (i,j) with the main
   * goal to determine, if there is a neighbor of state ct to this group. If
   * yes abandon the mark and return true.
   */
  private boolean recurseMarkTest(int i, int j, byte c, byte ct) {
    if (markers[i][j])
      return false;
    byte realColor = dead[i][j] ? EMPTY : board[i][j];
    if (realColor != c) {
      return realColor == ct;
    }
    markers[i][j] = true;
    if (i > 0) {
      if (recurseMarkTest(i - 1, j, c, ct))
        return true;
    }
    if (j > 0) {
      if (recurseMarkTest(i, j - 1, c, ct))
        return true;
    }
    if (i < boardSize - 1) {
      if (recurseMarkTest(i + 1, j, c, ct))
        return true;
    }
    if (j < boardSize - 1) {
      if (recurseMarkTest(i, j + 1, c, ct))
        return true;
    }
    return false;
  }

  /**
   * Test if the group at (n,m) has a neighbor of state ct. If yes, mark all
   * elements of the group. Else return false.
   */
  private boolean markTest(int x, int y, byte ct) {
    unMark();
    return recurseMarkTest(x, y, board[x][y], ct);
  }

  /**
   * Find all B and W territory. Sets the territory flags to 0, 1 or -1. -2 is
   * an intermediate state for unchecked points.
   * Smart territory marks the entire region as territory when you select a stone
   * otherwise it is a group by group remove (useful for IGS compatibility) 
   */
  public byte[][] calculateTerritory() {
    byte[][] territory = new byte[boardSize][boardSize];

    int i, j, ii, jj;
    for (i = 0; i < boardSize; i++) {
      for (j = 0; j < boardSize; j++) {
        territory[i][j] = UNDETERMINED_TERRITORY;
      }
    }
    for (i = 0; i < boardSize; i++) {
      for (j = 0; j < boardSize; j++) {
        if (board[i][j] == EMPTY) {
          if (territory[i][j] == UNDETERMINED_TERRITORY) {
            if (!markTest(i, j, BLACK)) {
              for (ii = 0; ii < boardSize; ii++) {
                for (jj = 0; jj < boardSize; jj++) {
                  if (markers[ii][jj])
                    territory[ii][jj] = WHITE;
                }
              }
            } else if (!markTest(i, j, WHITE)) {
              for (ii = 0; ii < boardSize; ii++) {
                for (jj = 0; jj < boardSize; jj++) {
                  if (markers[ii][jj])
                    territory[ii][jj] = BLACK;
                }
              }
            } else {
              markDeadGroup(i, j);
              for (ii = 0; ii < boardSize; ii++) {
                for (jj = 0; jj < boardSize; jj++) {
                  if (markers[ii][jj])
                    territory[ii][jj] = EMPTY;
                }
              }
            }
          }
        }
      }
    }
    return territory;
  }

  public int[] getScore() {
    int[] scores = new int[2];
    int white = nbCapturedBlack;
    int black = nbCapturedWhite;
    byte[][] territory = getTerritory();
    for (int i = 0; i < boardSize; i++) {
      for (int j = 0; j < boardSize; j++) {
        if (territory[i][j] == WHITE) {
          white++;
          if (board[i][j] == BLACK)
            white++;
        } else if (territory[i][j] == BLACK) {
          black++;
          if (board[i][j] == WHITE)
            black++;
        }
      }
    }
    scores[0] = white;
    scores[1] = black;
    return scores;

  }

  public boolean hasBeenRemove(byte x, byte y) {
    return dead[x][y];
  }

  public Rectangle getFullBoardArea() {
    return fullBoardArea;
  }

  public byte[][] guessTerritory() {
    return guessTerritory(5, 5);
  }

  /**
   * Evaluate Bousy map.
   * @param delate   number of delations
   * @param erode    number of erosions
   */
  public byte[][] guessTerritory(int delate, int erode) {
    int size = boardSize;
    int[][] data = new int[size][size];
    int[][] buffer = new int[size][size];
    for (int x = 0; x < size; x++)
      for (int y = 0; y < size; y++)
        if (!dead[x][y]) {
          switch (board[x][y]) {
          case WHITE:
            data[x][y] = -128;
            break;
          case BLACK:
            data[x][y] = 128;
            break;
          }
        }

    while (delate-- > 0) {
      for (int x = 0; x < size; x++)
        for (int y = 0; y < size; y++) {
          int w = 0;
          int b = 0;

          if (x > 0)
            if (data[x - 1][y] > 0)
              b++;
            else if (data[x - 1][y] < 0)
              w++;
          if (x < size - 1)
            if (data[x + 1][y] > 0)
              b++;
            else if (data[x + 1][y] < 0)
              w++;
          if (y > 0)
            if (data[x][y - 1] > 0)
              b++;
            else if (data[x][y - 1] < 0)
              w++;
          if (y < size - 1)
            if (data[x][y + 1] > 0)
              b++;
            else if (data[x][y + 1] < 0)
              w++;

          if (w == 0 && data[x][y] >= 0)
            buffer[x][y] += b;
          else if (b == 0 && data[x][y] <= 0)
            buffer[x][y] -= w;
        }
      for (int x = 0; x < size; x++)
        for (int y = 0; y < size; y++) {
          data[x][y] += buffer[x][y];
          buffer[x][y] = 0;
        }
    }

    while (erode-- > 0) {
      for (int x = 0; x < size; x++)
        for (int y = 0; y < size; y++) {
          int w = 0;
          int b = 0;

          if (x > 0) {
            if (data[x - 1][y] >= 0)
              b++;
            if (data[x - 1][y] <= 0)
              w++;
          }
          if (x < size - 1) {
            if (data[x + 1][y] >= 0)
              b++;
            if (data[x + 1][y] <= 0)
              w++;
          }
          if (y > 0) {
            if (data[x][y - 1] >= 0)
              b++;
            if (data[x][y - 1] <= 0)
              w++;
          }
          if (y < size - 1) {
            if (data[x][y + 1] >= 0)
              b++;
            if (data[x][y + 1] <= 0)
              w++;
          }

          if (data[x][y] > 0)
            buffer[x][y] -= w;
          else if (data[x][y] < 0)
            buffer[x][y] += b;
        }
      for (int x = 0; x < size; x++)
        for (int y = 0; y < size; y++) {
          int newVal = data[x][y] + buffer[x][y];
          if (data[x][y] > 0)
            data[x][y] = Math.max(newVal, 0);
          else if (data[x][y] < 0)
            data[x][y] = Math.min(newVal, 0);
          buffer[x][y] = 0;
        }
    }
    byte[][] result = new byte[size][size];
    for (int x = 0; x < size; x++)
      for (int y = 0; y < size; y++) {
        if (board[x][y] == EMPTY || dead[x][y]) {
          if (data[x][y] > 0)
            result[x][y] = BLACK;
          else if (data[x][y] < 0)
            result[x][y] = WHITE;
        }
      }

    return result;
  }

  public byte[][] getTerritory() {
    if (evaluationCountingMode)
      return guessTerritory();
    else
      return calculateTerritory();
  }

}
