/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome.sgf;

import java.io.IOException;
import java.io.Reader;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Stack;
import java.util.Vector;

import com.indigonauts.gome.common.Point;
import com.indigonauts.gome.common.Rectangle;
import com.indigonauts.gome.common.Util;
import com.indigonauts.gome.i18n.I18N;

/**
 * SgfModel is used to parse a sgf string and save the game in memory as a tree
 * structure.
 * The enumeration is used to have all the node in preorder
 * 
 */
public class SgfModel extends GameInfo implements Enumeration {

  private static final String RIGHT = "RIGHT";
  //#ifdef DEBUG
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("SgfModel");
  //#endif
  private final static byte DEFAULT_BOARD_SIZE = 19;

  private SgfNode root = new SgfNode();

  private byte boardSize = DEFAULT_BOARD_SIZE;

  private int handicap;

  private String VW;

  private byte PL; // first player from the sgf file

  private boolean commented = false; // should we prepared to show a comment

  private boolean markedSolution = false; // does the sgf specifically mark a problem with "CORRECT" nodes

  private static char lastRead = (char) -1;

  private static boolean reinjectlast = false;

  private Stack nodeStack; // used for the preorder

  private static char readAndMark(Reader src) throws IOException {
    char toreturn;
    if (reinjectlast) {
      reinjectlast = false;
      return lastRead;
    }
    toreturn = (char) src.read();
    index++;
    return toreturn;
  }

  public static SgfModel createNewModel(byte size, int handi, String white, String black) {
    SgfModel model = new SgfModel();
    model.boardSize = size;
    model.handicap = handi;
    model.whitePlayer = white.replace(']', '|');
    model.blackPlayer = black.replace(']', '|');
    model.application = "Gome:1.0"; //$NON-NLS-1$
    SgfNode newnode = new SgfNode();
    model.root.setSon(newnode);

    model.komi = "0.5"; //$NON-NLS-1$

    if (handi >= 2) {
      newnode.setPlayerColor((byte) 1);
    } else {
      newnode.setPlayerColor((byte) -1);
    }
    if (handi == 9 && size == 9)
      newnode.setComment(I18N.easterEgg); //$NON-NLS-1$

    if (handi >= 1) {
      model.komi = "0.5"; //$NON-NLS-1$
      if (handi >= 2) {
        int st = handi - 2;
        model.addHandicapStones(st);

      }
    } else {
      switch (size) {
      case 19:
        model.komi = "6.5"; //$NON-NLS-1$
        break;
      case 13:
        model.komi = "8.5"; //$NON-NLS-1$
        break;
      case 9:
        model.komi = "5.5"; //$NON-NLS-1$
        break;
      }

    }
    return model;
  }

  public void addHandicapStones(int nb) {
    switch (boardSize) {
    case 19:
      addHandicapStones(HANDI_PLACEMENTS_19[nb]);
      break;
    case 13:
      addHandicapStones(HANDI_PLACEMENTS_13[nb]);
      break;
    case 9:
      addHandicapStones(HANDI_PLACEMENTS_9[nb]);
      break;
    }
  }

  //used to fetch the last index in case of error
  public static int index = 0;

  public static SgfModel parse(Reader src) throws IllegalArgumentException {
    SgfModel newModel = new SgfModel();
    try {
      // skip potential garbage before the games
      char c = (char) -1;
      char c1 = (char) -1;
      do {
        c1 = (char) src.read();
      } while (c1 != '(' && c1 != -1);

      Stack brotherStack = new Stack();
      SgfNode latestNode = newModel.root;

      String property = Util.EMPTY_STRING; // property name
      String content = Util.EMPTY_STRING; // property content, i.e. the
      // string
      // between []

      boolean flag = false;
      index = 0;
      int level = 0;
      while (c1 != (char) -1) {
        c = c1; // at the first run of this loop, c1 is read before the
        // while
        c1 = readAndMark(src); // .read() returns -1 if EOF
        if (!flag) // flag==true means it is in property content
        {
          if (c == '(') {

            level++;
            //#ifdef DEBUG
            //log.debug("Starting of ( " + level + "-");
            //#endif
            while (c1 == '\r' || c1 == '\n') {
              c1 = readAndMark(src);
            }
            if (c1 == ';') {

              SgfNode newnode = new SgfNode();
              latestNode.setSon(newnode);
              latestNode = newnode;

              brotherStack.push(newnode);
              property = Util.EMPTY_STRING;
              c1 = readAndMark(src);
            }
          } else if (c == ')') {
            level--;
            //#ifdef DEBUG
            //log.debug("Ending of ) " + level + "-");
            //#endif

            while (c1 == ')') {
              level--;
              //#ifdef DEBUG
              //log.debug("Ending again of ) " + level + "-");
              //#endif
              brotherStack.pop();
              c1 = readAndMark(src);
            }
            if (level == 0) {
              break;
            }
            while (c1 == '\r' || c1 == '\n') {
              c1 = readAndMark(src);
            }
            if (c1 == '(') {
              level++;
              //#ifdef DEBUG
              //log.debug("Starting directly  of ( " + level + "-");
              //#endif
              if (readAndMark(src) == ';') {
                SgfNode brother = (SgfNode) brotherStack.pop();
                // System.out.print(" - Frère de " +
                // brother.getPoint() + " - ");
                SgfNode newnode = new SgfNode();
                latestNode = newnode;
                brother.setYounger(newnode);
                brotherStack.push(newnode);
                property = Util.EMPTY_STRING;
                c1 = readAndMark(src);
              } else {
                lastRead = c1;
                reinjectlast = true;
              }
            }

          } else if (c1 == (char) -1) {
            //#ifdef DEBUG
            log.debug("EOF");
            //#endif
            brotherStack.pop();
            break;
          } else if (c == ')') {
            level--;
            if (level == 0) {
              brotherStack.pop();
              break;
            }
          } else if (c == ';') {
            SgfNode newnode = new SgfNode();
            latestNode.setSon(newnode);

            latestNode = newnode;
            property = Util.EMPTY_STRING;
          } else if (c == '[') {
            //#ifdef DEBUG
            //log.debug("Starting of [ " + level + "-");
            //#endif
            flag = true;
            content = Util.EMPTY_STRING;
          } else if (Character.isUpperCase(c)) {
            property = property + c;
          } else if (c == '\n' || c == '\r' || c == ' ' || c == '\t') {
            // System.out.println(" CR ignored");
          } else {
            //#ifdef DEBUG
            //log.error("Did not expect '" + c + "'");
            //#endif
            throw new IllegalArgumentException(I18N.error.sgfParsing + " at chr " + index); //$NON-NLS-1$
          }
        } else if (c != ']') {
          if (property.equals("C") || (c != -1)) { //$NON-NLS-1$
            content = content + c;
            if (c == '\\') // escape character in comment add
            // directly the next character
            {
              c = c1; // at the first run of this loop, c1 is read
              // before the
              c1 = readAndMark(src); // .read() returns
              // -1 if
              // EOF
              content = content + c;
            }
          }
        } else {
          if ("SE".equals(property) || "SZ".equals(property))
          // size of the board
          {
            try {
              newModel.boardSize = Byte.parseByte(content);
            } catch (Exception e) {
              newModel.boardSize = DEFAULT_BOARD_SIZE;
            }
          } else if (property.equals("VW"))
          // View only part of the board.
          {
            if (content.length() == 5) {
              newModel.VW = content;
            }
          } else if (property.equals("C")) // comment //$NON-NLS-1$
          {
            newModel.commented = true;
            int r = -1;
            if ((r = content.indexOf(RIGHT)) != -1) {
              // Remove the ugly word
              content = content.substring(0, r) + content.substring(r + 5);
              // switch the model into "marked solution mode"
              newModel.markedSolution = true;
              //#ifdef DEBUG
              log.debug("Marked solution  at " + latestNode + " (" + latestNode.getPoint() + ")");
              //#endif
              SgfNode current = latestNode;
              // mark all the path correct
              //#ifdef DEBUG
              Vector correctPath = new Vector();
              //#endif
              while (current.getPoint() != null) {

                //#ifdef DEBUG
                correctPath.addElement(current);
                //#endif
                current.setCorrect(true);
                current = current.searchFather();

              }
              //#ifdef DEBUG
              for (int i = correctPath.size() - 1; i >= 0; i--) {
                log.debug("Path to Solution : " + correctPath.elementAt(i) + " (" + ((SgfNode) correctPath.elementAt(i)).getPoint() + ")");
              }
              //#endif

            }
            if ((r = content.indexOf("CHOICE")) != -1) {
              // Remove the ugly word
              content = content.substring(0, r) + content.substring(r + 6);
            }
            latestNode.setComment(content);
          } else if (property.equals("AB")) // add black
          {

            if (content.length() == 2) {
              SgfPoint p = SgfPoint.createFromSgf(content);
              latestNode.addABElement(p);
              //#ifdef DEBUG
              //log.debug("AB " + p);
              //#endif
            }

          } else if (property.equals("AW")) // add white
          {
            if (content.length() == 2) {
              SgfPoint p = SgfPoint.createFromSgf(content);
              latestNode.addAWElement(p);
              //#ifdef DEBUG
              //log.debug("AW " + p);
              //#endif
            }
          } else if (property.equals("B") || property.equals("W")) { //$NON-NLS-1$ //$NON-NLS-2$
            if (property.equals("B")) //$NON-NLS-1$
              latestNode.setPlayerColor(Board.BLACK);
            else
              latestNode.setPlayerColor(Board.WHITE);

            if (content.length() == 2) {
              SgfPoint p = SgfPoint.createFromSgf(content);
              latestNode.setPoint(p);
              //#ifdef DEBUG
              //log.debug("B or W " + p);
              //#endif
            } else if (content.length() == 0) {
              // This is a PASS
              latestNode.setPass();
            }
          } else if (property.equals("LB")) { //$NON-NLS-1$
            int splitIndex = content.indexOf(":"); //$NON-NLS-1$
            String coords = content.substring(0, splitIndex);
            String annotation = content.substring(splitIndex + 1);
            // This is an annotation on the board
            latestNode.addAnnotation(new TextAnnotation(SgfPoint.createFromSgf(coords), annotation));
          } else if (property.equals("CR")) { //$NON-NLS-1$
            latestNode.addAnnotation(new SymbolAnnotation(SgfPoint.createFromSgf(content), SymbolAnnotation.CIRCLE));
          } else if (property.equals("MA")) { //$NON-NLS-1$
            latestNode.addAnnotation(new SymbolAnnotation(SgfPoint.createFromSgf(content), SymbolAnnotation.CROSS));
          } else if (property.equals("SQ")) { //$NON-NLS-1$
            latestNode.addAnnotation(new SymbolAnnotation(SgfPoint.createFromSgf(content), SymbolAnnotation.SQUARE));
          } else if (property.equals("TR")) { //$NON-NLS-1$
            latestNode.addAnnotation(new SymbolAnnotation(SgfPoint.createFromSgf(content), SymbolAnnotation.TRIANGLE));
          } else if (property.equals("HA")) { //$NON-NLS-1$
            newModel.handicap = Integer.parseInt(content);
          } else if (property.equals("PL")) { //$NON-NLS-1$
            if (content.equals("B")) { //$NON-NLS-1$
              newModel.PL = 1;
            } else
              newModel.PL = -1;
          } else if (property.equals("AP")) { //$NON-NLS-1$
            newModel.application = content;
          } else if (property.equals("AN")) { //$NON-NLS-1$
            newModel.scribe = content;
          } else if (property.equals("BR")) { //$NON-NLS-1$
            newModel.blackRank = content;
          } else if (property.equals("BT")) { //$NON-NLS-1$
            newModel.blackTeam = content;
          } else if (property.equals("CP")) { //$NON-NLS-1$
            newModel.copyright = content;
          } else if (property.equals("DT")) { //$NON-NLS-1$
            newModel.date = content;
          } else if (property.equals("EV")) { //$NON-NLS-1$
            newModel.event = content;
          } else if (property.equals("GN")) { //$NON-NLS-1$
            newModel.name = content;
          } else if (property.equals("GC")) { //$NON-NLS-1$
            newModel.context = content;
          } else if (property.equals("ON")) { //$NON-NLS-1$
            newModel.opening = content;
          } else if (property.equals("OT")) { //$NON-NLS-1$
            newModel.timingType = content;
          } else if (property.equals("PB")) { //$NON-NLS-1$
            newModel.blackPlayer = content;
          } else if (property.equals("PC")) { //$NON-NLS-1$
            newModel.place = content;
          } else if (property.equals("PW")) { //$NON-NLS-1$
            newModel.whitePlayer = content;
          } else if (property.equals("RE")) { //$NON-NLS-1$
            newModel.result = content;
          } else if (property.equals("RO")) { //$NON-NLS-1$
            newModel.round = content;
          } else if (property.equals("RU")) { //$NON-NLS-1$
            newModel.rules = content;
          } else if (property.equals("SO")) { //$NON-NLS-1$
            newModel.source = content;
          } else if (property.equals("TM")) { //$NON-NLS-1$
            newModel.timing = content;
          } else if (property.equals("US")) { //$NON-NLS-1$
            newModel.user = content;
          } else if (property.equals("WR")) { //$NON-NLS-1$
            newModel.whiteRank = content;
          } else if (property.equals("WT")) { //$NON-NLS-1$
            newModel.whiteTeam = content;
          } else if (property.equals("KM")) { //$NON-NLS-1$
            newModel.komi = content;
          }

          if (c1 == '[') {
            c1 = readAndMark(src);
            content = Util.EMPTY_STRING;
          } else {
            property = Util.EMPTY_STRING;
            flag = false;
            //#ifdef DEBUG
            //log.debug("flag = false ");
            //#endif
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
      throw new IllegalArgumentException(I18N.error.sgfParsing + e.getMessage()); //$NON-NLS-1$
    }
    // upon successful parse, disconnect the root node from the sfg tree
    SgfNode son = newModel.root.getSon();
    if (son != null) {
      son.setFatherOnly(null);
    }
    return newModel;
  }

  public byte getBoardSize() {
    return boardSize;
  }

  public SgfNode getFirstMove() {
    return root.getSon();
  }

  public SgfNode getRoot() {
    return root;
  }

  public Enumeration elements() {
    nodeStack = new Stack();

    if (root.getSon() != null) {
      nodeStack.push(root.getSon());
    }
    return this;
  }

  public boolean hasMoreElements() {
    return nodeStack.size() > 0;
  }

  public Object nextElement() {
    if (!hasMoreElements())
      throw new NoSuchElementException("empty stack"); //$NON-NLS-1$

    SgfNode top = (SgfNode) nodeStack.pop();
    SgfNode son = top.getSon(); // left
    SgfNode younger = top.getYounger(); // left

    /*
     * younger is pushed first so that it's poped up later. the access is
     * top-son-younger in a recursive way
     */
    if (younger != null)
      nodeStack.push(younger);

    if (son != null)
      nodeStack.push(son);

    return top;
  }

  /**
   * Check all the nodes to find out the max and min x,y of all the stones
   * merge with the view area as well i.e. returned = max (ABAW points,
   * playing points, VW points)
   * 
   * @return
   */
  public Rectangle getStoneArea() {
    Rectangle rect = getInvalidRect();

    if (root.getSon().getAB() != null)
      for (Enumeration e = root.getSon().getAB().elements(); e.hasMoreElements();) {
        Point pt = (Point) (e.nextElement());
        rect.resizeForPoint(pt);
      }
    if (root.getSon().getAW() != null)
      for (Enumeration e = root.getSon().getAW().elements(); e.hasMoreElements();) {
        Point pt = (Point) (e.nextElement());
        rect.resizeForPoint(pt);
      }
    for (Enumeration e = this.elements(); e.hasMoreElements();) {
      SgfNode nextElement = (SgfNode) e.nextElement();
      Point pt = nextElement.getPoint();
      if (pt != null) {
        rect.resizeForPoint(pt);
      }
    }
    nodeStack = null; // free up some memory after the preorder
    return rect;
  }

  public String getVW() {
    return VW;
  }

  public Rectangle getViewArea() {
    Rectangle rect = getInvalidRect();

    if (VW != null && VW.length() == 5) {
      rect.x0 = SgfPoint.coordFromSgf(VW.charAt(0));
      rect.y0 = SgfPoint.coordFromSgf(VW.charAt(1));
      rect.x1 = SgfPoint.coordFromSgf(VW.charAt(3));
      rect.y1 = SgfPoint.coordFromSgf(VW.charAt(4));
    }
    return rect;
  }

  public Board getStartingBoard() {
    Board board = new Board(boardSize);
    if (root.getAB() != null)
      for (Enumeration e = root.getAB().elements(); e.hasMoreElements();) {
        Point pt = (Point) (e.nextElement());
        board.placeStone(pt, Board.BLACK);
      }
    if (root.getAW() != null)
      for (Enumeration e = root.getAW().elements(); e.hasMoreElements();) {
        Point pt = (Point) (e.nextElement());
        board.placeStone(pt, Board.WHITE);
      }

    return board;
  }

  private Rectangle getInvalidRect() {
    return new Rectangle(Byte.MAX_VALUE, Byte.MAX_VALUE, Byte.MIN_VALUE, Byte.MIN_VALUE);
  }

  public boolean isCorrectNode(SgfNode node) {
    return markedSolution ? node.isCorrect() : isMainBranch(node);
  }

  /**
   * test if a node is in the main branch
   * 
   * @param node
   * @return
   */
  public boolean isMainBranch(SgfNode node) {
    SgfNode branch = root.getSon();

    while (branch != null) {
      if (branch == node)
        return true;

      branch = branch.getSon();
    }

    return false;
  }

  public byte getFirstPlayer() {
    if (PL != 0) {
      return PL; // first respect what it is written in the SFG
    }

    SgfNode node = this.getFirstMove(); // Then try to guess from the played
    // moved
    byte color = 0;

    while (node != null) {
      color = node.getPlayerColor();
      if (color != 0)
        return color;
      node = node.getSon();
    }

    return handicap != 0 ? (byte) -1 : (byte) 1; // No move has been player,
    // respect the
    // starting convention
  }

  public String toString() {
    StringBuffer buf = new StringBuffer();
    SgfNode firstNode = getFirstMove();

    if (firstNode == null)
      return "(;)"; //$NON-NLS-1$

    buf.append('(');

    firstNode.output(buf);
    outputRootProperties(buf);

    outputRecursively(buf, firstNode.getSon());

    buf.append(')');
    return buf.toString();
  }

  private void outputRecursively(StringBuffer buf, SgfNode node) {
    if (node == null)
      return;

    // output itself
    node.output(buf);

    if (node.getSon() == null)
      return;

    // output son
    SgfNode son = node.getSon();
    if (son.getYounger() == null)
      outputRecursively(buf, son);
    else {
      buf.append('(');
      outputRecursively(buf, son);
      buf.append(')');

      son = son.getYounger();
      while (son != null) {
        buf.append('(');
        outputRecursively(buf, son);
        buf.append(')');

        son = son.getYounger();
      }
    }
  }

  public void outputRootProperties(StringBuffer buf) {
    buf.append("SZ["); //$NON-NLS-1$
    buf.append(String.valueOf(boardSize));
    buf.append(']');
    buf.append("KM["); //$NON-NLS-1$
    buf.append(getKomi());
    buf.append(']');

    Rectangle view = getViewArea();
    if (view.isValid()) {
      buf.append("VW["); //$NON-NLS-1$
      buf.append(new SgfPoint((byte) view.x0, (byte) view.y0));
      buf.append(':');
      buf.append(new SgfPoint((byte) view.x1, (byte) view.y1));
      buf.append(']');
    }
    if (handicap != 0) {
      buf.append("HA[");
      buf.append(handicap);
      buf.append(']');
    }

  }

  private static final String[][] HANDI_PLACEMENTS_19 = { { "dd", "pp" }, //$NON-NLS-1$ //$NON-NLS-2$
          { "pp", "dp", "dd" }, // 3
          // stones
          // //$NON-NLS-1$
          // //$NON-NLS-2$
          // //$NON-NLS-3$
          { "dd", "pd", "dp", "pp" }, // 4
          // stones
          // //$NON-NLS-1$
          // //$NON-NLS-2$
          // //$NON-NLS-3$
          // //$NON-NLS-4$
          { "dd", "pd", "jj", "dp", "pp" }, // 5
          // stones
          // //$NON-NLS-1$
          // //$NON-NLS-2$
          // //$NON-NLS-3$
          // //$NON-NLS-4$
          // //$NON-NLS-5$
          { "dd", "pd", "dj", "pj", "dp", "pp" }, // 6
          // stones
          // //$NON-NLS-1$
          // //$NON-NLS-2$
          // //$NON-NLS-3$
          // //$NON-NLS-4$
          // //$NON-NLS-5$
          // //$NON-NLS-6$
          { "dd", "pd", "dj", "jj", "pj", "dp", "pp" }, // 7
          // stones
          // //$NON-NLS-1$
          // //$NON-NLS-2$
          // //$NON-NLS-3$
          // //$NON-NLS-4$
          // //$NON-NLS-5$
          // //$NON-NLS-6$
          // //$NON-NLS-7$
          { "dd", "jd", "pd", "dj", "pj", "dp", "jp", "pp" }, // 8
          // stones
          // //$NON-NLS-1$
          // //$NON-NLS-2$
          // //$NON-NLS-3$
          // //$NON-NLS-4$
          // //$NON-NLS-5$
          // //$NON-NLS-6$
          // //$NON-NLS-7$
          // //$NON-NLS-8$
          { "dd", "jd", "pd", "dj", "jj", "pj", "dp", "jp", "pp" } // 9
  // stones
  // //$NON-NLS-1$
  // //$NON-NLS-2$
  // //$NON-NLS-3$
  // //$NON-NLS-4$
  // //$NON-NLS-5$
  // //$NON-NLS-6$
  // //$NON-NLS-7$
  // //$NON-NLS-8$
  // //$NON-NLS-9$*/
  };

  private static final String[][] HANDI_PLACEMENTS_13 = { { "dd", "jj" }, //$NON-NLS-1$ //$NON-NLS-2$
          { "dd", "dj", "jj" }, // 3
          // stones
          // //$NON-NLS-1$
          // //$NON-NLS-2$
          // //$NON-NLS-3$
          { "dd", "jd", "dj", "jj" }, // 4
          // stones
          // //$NON-NLS-1$
          // //$NON-NLS-2$
          // //$NON-NLS-3$
          // //$NON-NLS-4$
          { "dd", "jd", "gg", "dj", "jj" }, // 5
          // stones
          // //$NON-NLS-1$
          // //$NON-NLS-2$
          // //$NON-NLS-3$
          // //$NON-NLS-4$
          // //$NON-NLS-5$
          { "dd", "jd", "dg", "jg", "dj", "jj" }, // 6
          // stones
          // //$NON-NLS-1$
          // //$NON-NLS-2$
          // //$NON-NLS-3$
          // //$NON-NLS-4$
          // //$NON-NLS-5$
          // //$NON-NLS-6$
          { "dd", "jd", "dg", "gg", "jg", "dj", "jj" }, // 7
          // stones
          // //$NON-NLS-1$
          // //$NON-NLS-2$
          // //$NON-NLS-3$
          // //$NON-NLS-4$
          // //$NON-NLS-5$
          // //$NON-NLS-6$
          // //$NON-NLS-7$
          { "dd", "gd", "jd", "dg", "jg", "dj", "gj", "jj" }, // 8
          // stones
          // //$NON-NLS-1$
          // //$NON-NLS-2$
          // //$NON-NLS-3$
          // //$NON-NLS-4$
          // //$NON-NLS-5$
          // //$NON-NLS-6$
          // //$NON-NLS-7$
          // //$NON-NLS-8$
          { "dd", "gd", "jd", "dg", "gg", "jg", "dj", "gj", "jj" } // 9
  // stones
  // //$NON-NLS-1$
  // //$NON-NLS-2$
  // //$NON-NLS-3$
  // //$NON-NLS-4$
  // //$NON-NLS-5$
  // //$NON-NLS-6$
  // //$NON-NLS-7$
  // //$NON-NLS-8$
  // //$NON-NLS-9$*/
  };

  private static final String[][] HANDI_PLACEMENTS_9 = { { "cc", "gg" }, //$NON-NLS-1$ //$NON-NLS-2$
          { "cc", "cg", "gg" }, // 3
          // stones
          // //$NON-NLS-1$
          // //$NON-NLS-2$
          // //$NON-NLS-3$
          { "cc", "gc", "cg", "gg" }, // 4
          // stones
          // //$NON-NLS-1$
          // //$NON-NLS-2$
          // //$NON-NLS-3$
          // //$NON-NLS-4$
          { "cc", "gc", "ee", "cg", "gg" }, // 5
          // stones
          // //$NON-NLS-1$
          // //$NON-NLS-2$
          // //$NON-NLS-3$
          // //$NON-NLS-4$
          // //$NON-NLS-5$
          { "cc", "gc", "ce", "ge", "cg", "gg" }, // 6
          // stones
          // //$NON-NLS-1$
          // //$NON-NLS-2$
          // //$NON-NLS-3$
          // //$NON-NLS-4$
          // //$NON-NLS-5$
          // //$NON-NLS-6$
          { "cc", "gc", "ce", "ee", "ge", "cg", "gg" }, // 7
          // stones
          // //$NON-NLS-1$
          // //$NON-NLS-2$
          // //$NON-NLS-3$
          // //$NON-NLS-4$
          // //$NON-NLS-5$
          // //$NON-NLS-6$
          // //$NON-NLS-7$
          { "cc", "ec", "gc", "ce", "ge", "cg", "eg", "gg" }, // 8
          // stones
          // //$NON-NLS-1$
          // //$NON-NLS-2$
          // //$NON-NLS-3$
          // //$NON-NLS-4$
          // //$NON-NLS-5$
          // //$NON-NLS-6$
          // //$NON-NLS-7$
          // //$NON-NLS-8$
          { "cc", "ec", "gc", "ce", "ee", "ge", "cg", "eg", "gg" } // 9
  // stones
  // //$NON-NLS-1$
  // //$NON-NLS-2$
  // //$NON-NLS-3$
  // //$NON-NLS-4$
  // //$NON-NLS-5$
  // //$NON-NLS-6$
  // //$NON-NLS-7$
  // //$NON-NLS-8$
  // //$NON-NLS-9$*/
  };

  private void addHandicapStones(String[] placements) {
    for (int i = 0; i < placements.length; i++) {
      getFirstMove().addABElement(SgfPoint.createFromSgf(placements[i]));
    }
  }

  /**
   * @return Returns the commented.
   */
  public boolean isCommented() {
    return commented;
  }

  public byte getByteKomi() {
    return Util.string2Komi(getKomi());
  }

  public void setNewKomi(byte k) {
    this.komi = Util.komi2String(k);
  }
}
