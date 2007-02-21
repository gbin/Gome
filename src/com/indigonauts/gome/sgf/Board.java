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

    public static final byte[][] direction = { { 0, -1 }, { 1, 0 }, { 0, 1 }, { -1, 0 } };

    byte[][] board; // use as board[x][y]

    byte boardSize;

    private int nbCapturedBlack;

    private int nbCapturedWhite;

    private Point ko;

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
        // firstPlayer=currentPlayer=BLACK;
        boardSize = newBoardSize;
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
        int x = playedMove.getX();
        int y = playedMove.getY();

        int captx = capturedStone.getX();
        int capty = capturedStone.getY();

        // System.out.println("player =" + player );
        // System.out.println("x=" +x );
        // System.out.println("y=" +y );
        // System.out.println("cx=" +captx );
        // System.out.println("cy=" +capty );
        //        
        // System.out.println("board[x+1][y]=" +board[x+1][y] );
        // System.out.println("board[x-1][y]=" +board[x-1][y] );
        //        
        // System.out.println("board[x][y+1]=" +board[x][y+1] );
        // System.out.println("board[x][y-1]=" +board[x][y-1] );

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

        int x = point.getX();
        int y = point.getY();
        if (x >= board.length || y >= board.length)
            return;
        board[x][y] = color;
    }

    public boolean isValidMove(Point pt, byte color) {
        if (this.getPosition(pt) != EMPTY)
            return false;

        if (ko != null && pt.getX() == ko.getX() && pt.getY() == ko.getY())
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
        return board[pt.getX()][pt.getY()];
    }

    public Rectangle getBoardArea() {
        return new Rectangle((byte) 0, (byte) 0, (byte) (boardSize - 1), (byte) (boardSize - 1));
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

        Rectangle area = this.getBoardArea();

        int[][] history = new int[boardSize][boardSize];
        for (int i = 0; i < direction.length; ++i) {
            Point nextpt = new Point(lastmove);
            nextpt.move(direction[i][0], direction[i][1]);

            if (area.contains(nextpt) && (this.getPosition(nextpt) * lastplayer == -1)
                    && history[nextpt.getX()][nextpt.getY()] == 0) {
                Vector tempVec = this.findDeadGroup(nextpt, history);

                // vec.addAll(tempVec);
                for (Enumeration e = tempVec.elements(); e.hasMoreElements();) {
                    vec.addElement(e.nextElement());
                }
            }

        }

        // if no opponent is killed, check if it's a suicide
        // not implemented
        // if (vec.size()==0)
        // {

        // }

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
        Rectangle area = this.getBoardArea();

        stack.push(pt);
        history[pt.getX()][pt.getY()] = 1;

        while (!stack.isEmpty()) {
            Point curpt = (Point) stack.peek();
            int status = history[curpt.getX()][curpt.getY()];

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
                history[curpt.getX()][curpt.getY()] = status + 1;
                nextpt.move(direction[status - 1][0], direction[status - 1][1]);
            }

            if (!area.contains(nextpt))
                continue;

            if (history[nextpt.getX()][nextpt.getY()] > 0)
                continue;

            int nextcolor = this.getPosition(nextpt);
            if (nextcolor * deadcolor == -1)
                continue;
            if (nextcolor == deadcolor) {
                stack.push(nextpt);
                history[nextpt.getX()][nextpt.getY()] = 1;
                continue;
            }
            // here, nextcolor==0
            {
                for (Enumeration e = deadStones.elements(); e.hasMoreElements();) {
                    Point deadpt = (Point) (e.nextElement());
                    history[deadpt.getX()][deadpt.getY()] = 0;
                }
                for (Enumeration e = stack.elements(); e.hasMoreElements();) {
                    Point stackpt = (Point) (e.nextElement());
                    history[stackpt.getX()][stackpt.getY()] = 0;
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

    private boolean[][] dead;

    private boolean[][] markers;

    private boolean ressucite = false;

    /**
     * Recursively mark all unmarked places with the given color & empty
     */
    private void recurseMarkDead(int i, int j, byte color) {
        if (markers[i][j] || (board[i][j] == -color))
            return;
        markers[i][j] = true;
        if (board[i][j] == color)
            dead[i][j] = !ressucite;
        if (i > 0)
            recurseMarkDead(i - 1, j, color);
        if (j > 0)
            recurseMarkDead(i, j - 1, color);
        if (i < boardSize - 1)
            recurseMarkDead(i + 1, j, color);
        if (j < boardSize - 1)
            recurseMarkDead(i, j + 1, color);
    }

    private void recurseOnlineMarkDead(int i, int j, byte color) {
        if (markers[i][j] || (board[i][j] == -color))
            return;
        markers[i][j] = true;
        if (board[i][j] == color)
            dead[i][j] = !ressucite;
        if (i > 0 && board[i - 1][j] == color)
            recurseOnlineMarkDead(i - 1, j, color);
        if (j > 0 && board[i][j - 1] == color)
            recurseOnlineMarkDead(i, j - 1, color);
        if (i < boardSize - 1 && board[i + 1][j] == color)
            recurseOnlineMarkDead(i + 1, j, color);
        if (j < boardSize - 1 && board[i][j + 1] == color)
            recurseOnlineMarkDead(i, j + 1, color);
    }

    public void switchToCounting(boolean b) {
        if (b)
            dead = new boolean[boardSize][boardSize];
        else {
            dead = null;
            markers = null;
        }
    }

    /**
     * cancel all markings
     */
    private void unMark() {
        markers = new boolean[boardSize][boardSize];
    }

    /**
     * Mark a group at (n,m)
     */
    public void markDeadGroup(int n, int m) {
        unMark();
        ressucite = dead[n][m];
        recurseMarkDead(n, m, board[n][m]);
    }

    public void onlineMarkDeadGroup(int n, int m) {
        unMark();
        ressucite = dead[n][m];
        recurseOnlineMarkDead(n, m, board[n][m]);
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

    // /**
    // * mark and count
    // */
    // public int count(int i, int j) {
    // unMark();
    // markDeadGroup(i, j);
    // int count = 0;
    // for (i = 0; i < boardSize; i++)
    // for (j = 0; j < boardSize; j++)
    // if (markers[i][j])
    // count++;
    // return count;
    // }

    private static final byte UNDETERMINED_TERRITORY = -2;

    /**
     * Find all B and W territory. Sets the territory flags to 0, 1 or -1. -2 is
     * an intermediate state for unchecked points.
     */
    public byte[][] guessTerritory(byte mode) {
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
                            if (mode == GAME_MODE)
                                markDeadGroup(i, j);
                            else
                                onlineMarkDeadGroup(i, j);
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
        byte[][] territory = guessTerritory((byte) GAME_MODE);
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
}
