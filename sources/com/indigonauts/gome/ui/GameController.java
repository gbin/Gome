/*
 * (c) 2006  Indigonauts
 */
package com.indigonauts.gome.ui;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Graphics;

import com.indigonauts.gome.ClockController;
import com.indigonauts.gome.Gome;
import com.indigonauts.gome.MainCanvas;
import com.indigonauts.gome.common.Point;
import com.indigonauts.gome.common.QuickSortable;
import com.indigonauts.gome.common.Rectangle;
import com.indigonauts.gome.common.Util;
import com.indigonauts.gome.i18n.I18N;
import com.indigonauts.gome.io.CollectionEntry;
import com.indigonauts.gome.multiplayer.Challenge;
import com.indigonauts.gome.multiplayer.Game;
import com.indigonauts.gome.multiplayer.Move;
import com.indigonauts.gome.multiplayer.MultiplayerCallback;
import com.indigonauts.gome.multiplayer.MultiplayerConnector;
import com.indigonauts.gome.multiplayer.User;
import com.indigonauts.gome.multiplayer.bt.BluetoothClientConnector;
import com.indigonauts.gome.multiplayer.igs.IGSConnector;
import com.indigonauts.gome.sgf.Board;
import com.indigonauts.gome.sgf.SgfModel;
import com.indigonauts.gome.sgf.SgfNode;
import com.indigonauts.gome.sgf.SgfPoint;
import com.indigonauts.gome.sgf.SymbolAnnotation;

//#if IGS || BT
public class GameController implements MultiplayerCallback
//#else
//# public class GameController
//#endif
{
  //#if DEBUG
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("GameController");
  //#endif

  public static final char GAME_MODE = 'G';

  public static final char JOSEKI_MODE = 'J';

  public static final char REVIEW_MODE = 'R';

  public static final char PROBLEM_MODE = 'P';

  public static final char ONLINE_MODE = 'O';

  public static final char OBSERVE_MODE = 'B';

  public static final char TEXT_MODE = 'T';

  public static final char P2P_MODE = '2';

  private SgfModel model;

  private Board board;

  // play area is where it makes sense to play :
  // full board for a review or a normal game
  // around the present stones in the problems
  private Rectangle playArea;

  private SgfPoint cursor = new SgfPoint((byte) 0, (byte) 0);

  private SgfNode currentNode;

  public Display display;

  private FileLoader fileLoader;

  private MainCanvas canvas;

  private boolean gameHasEnded = false;

  // options
  private boolean showHint;

  private char playMode = GAME_MODE;

  private boolean zoomedIn;

  private boolean countMode = false;
  private boolean evaluationMode = false;

  private CollectionEntry currentCollection;

  private int currentIndexInCollection;

  private int moveNb = 0;

  //#if IGS || BT
  MultiplayerConnector multiplayerConnector;
  //#endif

  //#if IGS
  Game currentIgsGame = null;
  //#endif

  //#if IGS || BT
  private byte onlineColor;
  private boolean tryingToConnect = false;
  private boolean connectToIgs = false;
  private byte gomeWantOnlineKomi = 0;
  //#endif

  private ClockController clock;

  public GameController(Display display) {
    this.display = display;
    model = new SgfModel();
    moveNb = 0;
    zoomedIn = false;
    // log.debug("Game Controller instantiated");
  }

  public boolean hasNextInCollection() {
    if (currentCollection == null)
      return false;
    return currentIndexInCollection < currentCollection.getCollectionSize() - 1;
  }

  public boolean hasPreviousInCollection() {
    if (currentCollection == null)
      return false;
    return currentIndexInCollection > 1;
  }

  public void reset(MainCanvas c, char mode) {
    this.canvas = c;
    currentNode = null;
    model = new SgfModel();
    moveNb = 0;

    playArea = null;
    cursor.x = (byte) 0;
    cursor.y = (byte) 0;
    fileLoader = null;

    showHint = false;
    countMode = false;
    setPlayMode(mode);
    canvas.assignClockController(clock);
  }

  public char getPlayMode() {
    return playMode;
  }

  public void setPlayMode(char playMode) {

    this.playMode = playMode;

    switch (this.playMode) {
    case GAME_MODE:
      //#if DEBUG
      log.info("Switch to standard play mode");
      //#endif
      Gome.singleton.mainCanvas.switchToPlayMenu();
      break;
    case PROBLEM_MODE:
      //#if DEBUG
      log.info("Switch to problem Mode");
      //#endif
      Gome.singleton.mainCanvas.switchToProblemMenu();
      break;
    case JOSEKI_MODE:
      //#if DEBUG
      log.info("Switch to joseki mode");
      //#endif
      Gome.singleton.mainCanvas.switchToJosekiMenu();
      break;
    case REVIEW_MODE:
      //#if DEBUG
      log.info("Switch to game review mode");
      //#endif
      Gome.singleton.mainCanvas.switchToReviewMenu();
      break;
    //#if IGS
    case ONLINE_MODE:
      //#if DEBUG
      log.info("Switch to Online play mode");
      //#endif
      Gome.singleton.mainCanvas.switchToOnlinePlayMenu();
      break;
    case OBSERVE_MODE:
      //#if DEBUG
      log.info("Switch to Observe play mode");
      //#endif
      Gome.singleton.mainCanvas.switchToObservePlayMenu();
      break;
    //#endif
    //#if BT
    case P2P_MODE:
      //#if DEBUG
      log.info("Switch to P2P play mode");
      //#endif
      Gome.singleton.mainCanvas.switchToP2PPlayMenu();
      break;
    //#endif
    }
  }

  public void loadAndPlayNextInCollection(boolean reverse) {

    model = new SgfModel(); // free up some memory before reloading
    // something else.
    currentNode = new SgfNode();
    board = new Board();
    fileLoader = new FileLoader(this, currentCollection, reverse ? --currentIndexInCollection : ++currentIndexInCollection);
    fileLoader.show(display);
    canvas.scroller.setFileIndex(currentIndexInCollection);
    fileLoader.start();
  }

  public void loadAndPlay(CollectionEntry file, int fileIndex) {
    model = new SgfModel(); // free up some memory before reloading
    // something else.
    currentNode = new SgfNode();
    board = new Board();

    // log.debug("loadAndPlay initial " + fileIndex);
    currentCollection = file;
    currentIndexInCollection = fileIndex;
    fileLoader = new FileLoader(this, file, fileIndex);
    fileLoader.show(display);
    if (currentCollection.getCollectionSize() > 1)
      canvas.scroller.setFileIndex(currentIndexInCollection);
    else
      canvas.scroller.setFileIndex(0);

    fileLoader.start();
  }

  public void downloadFinished(SgfModel m, char mode) {
    countMode = false;
    this.model = m;
    setPlayMode(mode);
    notifyLoadReady();

    if (mode == GAME_MODE)
      goToLastMove();
    canvas.show(display);
    fileLoader = null;
  }

  public SgfModel getSgfModel() {
    return model;
  }

  public void tuneBoardPainter() {
    // log.debug("paintBackBuffer");
    BoardPainter painter = canvas.getBoardPainter();
    painter.setCountingMode(countMode);
    painter.setKo(currentNode.getKo());
  }

  public void notifyLoadReady() {
    // log.debug("notifyLoadReady ");
    // by default show the comment window for commented games

    moveNb = 0;

    board = model.getStartingBoard();
    // isolate the interesting part of the game for a problem
    switch (playMode) {
    case GAME_MODE:
    case REVIEW_MODE:
    case ONLINE_MODE:
    case P2P_MODE:
    case OBSERVE_MODE:
      playArea = board.getFullBoardArea();
      // by politeness put the cursor at the top right corner
      cursor.x = (byte) (board.getBoardSize() - CORNER_DEF - 1);
      cursor.y = CORNER_DEF;
      break;
    case JOSEKI_MODE:
    case PROBLEM_MODE:
      playArea = model.getStoneArea();
      playArea.grow((byte) 3, (byte) 3);
      // center out the cursor
      cursor.x = (byte) ((playArea.x1 + playArea.x0) / 2);
      cursor.y = (byte) ((playArea.y1 + playArea.y0) / 2);
      break;
    }
    //        

    if (playArea == null || model.getViewArea().isValid()) {
      playArea = Rectangle.union(playArea, model.getViewArea());
      // center out even furthur the cursor
      cursor.x = (byte) ((playArea.x1 + playArea.x0) / 2);
      cursor.y = (byte) ((playArea.y1 + playArea.y0) / 2);
    }
    playArea = Rectangle.intersect(playArea, board.getFullBoardArea());

    if (playArea == null || !playArea.isValid()) {
      playArea = new Rectangle((byte) 0, (byte) 0, (byte) (board.getBoardSize() - 1), (byte) (board.getBoardSize() - 1));
    }

    switchCurrentNode(model.getFirstMove());
    playNode(currentNode);
    initPainter(); // setclockandcomment need the definitive painter for the layout
    if (model.isCommented()) {
      canvas.updateClockAndCommentMode(MainCanvas.COMMENT_MODE);
    } else {
      canvas.updateClockAndCommentMode(MainCanvas.NOTHING_TO_DISPLAY_MODE);
    }

  }

  private int normalDelta;

  public void initPainter() {
    // log.debug("initPainter");
    Rectangle drawArea = new Rectangle(0, 0, canvas.getWidth(), canvas.getHeight());

    //#if AA
    BoardPainter boardPainter = new GlyphBoardPainter(board, drawArea, playArea, true);
    //#else
    //# BoardPainter boardPainter = new BoardPainter(board, drawArea, playArea, true);
    //#endif
    canvas.setBoardPainter(boardPainter);
    tuneBoardPainter();
    normalDelta = boardPainter.getDelta();
  }

  void switchToNormalPainter() {
    //#if DEBUG
    log.debug("switchToNormalPainter");
    //#endif
    canvas.getBoardPainter().setPlayArea(playArea);
    // tuneBoardPainter();
  }

  void switchToZoomedPainter() {
    byte w = (byte) (canvas.getBoardPainter().getWidth() / (normalDelta * 2));
    byte h = (byte) (canvas.getBoardPainter().getHeight() / (normalDelta * 2));
    byte left = (byte) (cursor.x - w / 2);
    byte top = (byte) (cursor.y - h / 2);
    Rectangle pArea = new Rectangle(left, top, (byte) (left + w), (byte) (top + h));

    if (pArea.x0 < 0) {
      pArea.move(0 - pArea.x0, 0);
    }
    if (pArea.y0 < 0) {
      pArea.move(0, 0 - pArea.y0);
    }
    Rectangle fullBoardArea = board.getFullBoardArea();
    if (pArea.x1 > fullBoardArea.x1) {
      pArea.move(fullBoardArea.x1 - pArea.x1, 0);
    }
    if (pArea.y1 > fullBoardArea.y1) {
      pArea.move(0, fullBoardArea.y1 - pArea.y1);
    }
    Rectangle zoomedPlayArea = Rectangle.intersect(pArea, fullBoardArea);

    canvas.getBoardPainter().setPlayArea(zoomedPlayArea);
    // tuneBoardPainter();
  }

  private Rectangle areaToRefresh = new Rectangle(0, 0, 0, 0);

  public Rectangle doMoveCursor(int keyCode) {
    boolean refreshPainter = false;
    Rectangle area = canvas.getBoardPainter().getViewedArea();
    byte x = cursor.x;
    byte y = cursor.y;
    switch (canvas.getGameAction(keyCode)) {
    case MainCanvas.ACTION_UP:

      if (y > 0) {
        y--;
        if (!playArea.contains(x, y)) {
          return null;
        }
        if (y < area.y0) {
          area.y0 = y;
          area.y1--;
          refreshPainter = true;
        }
      }
      break;
    case MainCanvas.ACTION_DOWN:

      if (y < board.getBoardSize() - 1) {
        y++;
        if (!playArea.contains(x, y)) {
          return null;
        }

        if (y > area.y1) {
          area.y1 = y;
          area.y0++;
          refreshPainter = true;
        }
      }

      break;
    case MainCanvas.ACTION_LEFT:
      if (x > 0) {
        x--;
        if (!playArea.contains(x, y)) {
          return null;
        }

        if (x < area.x0) {
          area.x0 = x;
          area.x1--;
          refreshPainter = true;
        }
      }
      break;
    case MainCanvas.ACTION_RIGHT:
      if (x < board.getBoardSize() - 1) {
        x++;
        if (!playArea.contains(x, y)) {
          return null;
        }

        if (x > area.x1) {
          area.x1 = x;
          area.x0++;
          refreshPainter = true;
        }
      }
      break;
    default:
    }
    // do a full refresh because we scrolled
    if (refreshPainter) {
      tuneBoardPainter();
      canvas.getBoardPainter().setPlayArea(area); // Otherwise it will not be notified of the change
      cursor.x = x;
      cursor.y = y;
      return canvas.getBoardPainter().getDrawArea();
    }
    // do a partial refresh as we moved inside the visible area
    boolean refreshNeeded = (x != cursor.x) || (y != cursor.y);
    if (refreshNeeded) {
      areaToRefresh.x0 = canvas.getBoardPainter().getCellX(Math.min(x, cursor.x) - 1);
      areaToRefresh.x1 = canvas.getBoardPainter().getCellX(Math.max(x, cursor.x) + 1);
      areaToRefresh.y0 = canvas.getBoardPainter().getCellY(Math.min(y, cursor.y) - 1);
      areaToRefresh.y1 = canvas.getBoardPainter().getCellY(Math.max(y, cursor.y) + 1);
      //#if DEBUG
      //# log.debug("Area to refresh = (" + areaToRefresh.x0 + "," + areaToRefresh.y0 + ")-(" + areaToRefresh.x1 + "," + areaToRefresh.y1 + ")");
      //#endif
    }

    cursor.x = x;
    cursor.y = y;
    return refreshNeeded ? areaToRefresh : null;
  }

  /*
   * return true if the action needs a repaint
   */
  public Rectangle doReviewMove(int keyCode) {
    boolean refreshNeeded = true;
    switch (canvas.getGameAction(keyCode)) {
    case MainCanvas.ACTION_UP:
      doGoPrevBrother();
      break;
    case MainCanvas.ACTION_DOWN:
      doGoNextBrother();
      break;
    case MainCanvas.ACTION_LEFT:
      if (!doGoBack(true)) // root allowed in review
        canvas.setSplashInfo(I18N.noMoreMove);
      break;
    case MainCanvas.ACTION_RIGHT:
      if (!doGoNext())
        canvas.setSplashInfo(I18N.noMoreMove);
      break;
    default:
      refreshNeeded = false;
    }
    if (refreshNeeded) {

      if (currentNode.getPoint() != null) {
        // make the cursor follow the last move only if it is a valid
        // move
        cursor = currentNode.getPoint();
      }
      tuneBoardPainter();
    }
    spanInZoomedModeIfTheCursorIsOut();

    return refreshNeeded ? canvas.getBoardPainter().getDrawArea() : null;
  }

  private void spanInZoomedModeIfTheCursorIsOut() {
    Rectangle displayArea = canvas.getBoardPainter().getViewedArea();
    if (zoomedIn && (cursor.x > displayArea.x1 || cursor.x < displayArea.x0 || cursor.y < displayArea.y0 || cursor.y > displayArea.y1)) {

      switchToZoomedPainter();
    }
  }

  /**
   * 
   * @param keyCode
   * @return a graphic rectangle to refresh or null if nothing need to be refreshed
   */
  public Rectangle doReviewAction(int keyCode) {
    if (keyCode == canvas.KEY_10PREVMOVES) {
      do10PrevMoves();
      return canvas.getBoardPainter().getDrawArea();
    } else if (keyCode == canvas.KEY_10NEXTMOVES) {
      do10NextMoves();
      return canvas.getBoardPainter().getDrawArea();
    }
    return null;
  }

  public void do10NextMoves() {
    for (int i = 0; i < 10; i++)
      doGoNext();
    tuneBoardPainter();
  }

  public void do10PrevMoves() {
    for (int i = 0; i < 10; i++)
      doGoBack(true); // root allowed in review
    tuneBoardPainter();
  }

  /**
   * 
   * @param keyCode
   * @return a graphic rectangle to refresh or null if nothing need to be refreshed
   */
  public Rectangle doAction(int keyCode) {
    if (currentNode == null)
      return null;
    boolean refreshNeeded = true;

    switch (canvas.getGameAction(keyCode)) {
    case MainCanvas.ACTION_FIRE:
      if (countMode && !gameHasEnded) {
        //#if IGS
        if (playMode == ONLINE_MODE || playMode == P2P_MODE) {
          try {
            if (!(board.isValidMove(cursor, Board.BLACK) | board.isValidMove(cursor, Board.WHITE)) && !board.hasBeenRemove(cursor.x, cursor.y)) {
              multiplayerConnector.removeDeadStone(cursor.x, cursor.y);
            }
          } catch (Exception e) {
            Util.errorNotifier(e);
          }
        }
        //#endif
        board.markDeadGroup(cursor.x, cursor.y);

      } else if (playMode == REVIEW_MODE || playMode == JOSEKI_MODE)
        doCycleBottom();
      else
        doClick();
      break;
    case MainCanvas.ACTION_UNDO:
      refreshNeeded = doUndo();
      break;
    case MainCanvas.ACTION_HINT:
      reverseShowHint();
      break;
    case MainCanvas.ACTION_COMMENT:
      doCycleBottom();
      break;
    case MainCanvas.ACTION_ZOOM:
      setZoomIn(!isZoomIn());
      break;
    default:
      refreshNeeded = false;
    }

    if (refreshNeeded) {
      tuneBoardPainter();
    }
    return refreshNeeded ? canvas.getFullCanvas() : null;
  }

  /* return refreshNeded */
  public boolean doUndo() {
    if (playMode != ONLINE_MODE && playMode != P2P_MODE && playMode != OBSERVE_MODE) {
      doGoBack(false);
      if (playMode == PROBLEM_MODE && model.getFirstPlayer() == currentNode.getPlayerColor()) {
        doGoBack(false);
      }
      return true;
    }
    return false;
  }

  /**
   * 
   */
  void doCycleBottom() {
    switch (playMode) {

    case GAME_MODE:
    case JOSEKI_MODE:
    case REVIEW_MODE:
    case PROBLEM_MODE:
      canvas.cycleClockAndCommentMode(false);
      break;
    case OBSERVE_MODE:
    case ONLINE_MODE:
    case P2P_MODE:
      canvas.cycleClockAndCommentMode(true);
      break;

    }
    refreshPainter();
  }

  public void pointerReleased(int x, int y) {
    canvas.setSplashInfo(null);
    moveCursor(canvas.getBoardPainter().getBoardX(x), canvas.getBoardPainter().getBoardY(y));
    if (countMode && !gameHasEnded)
      board.markDeadGroup(cursor.x, cursor.y);
    else
      doClick();
  }

  public void moveCursor(byte x, byte y) {
    cursor.x = x;
    cursor.y = y;
  }

  public void pass() {
    //#if DEBUG
    log.debug("pass");
    //#endif
    byte color = getCurrentPlayerColor();

    switch (playMode) {
    //#if IGS || BT
    case ONLINE_MODE:
    case P2P_MODE:
      if (color != onlineColor) {
        canvas.setSplashInfo(I18N.online.notYourTurn);
        break;
      }
      try {
        if (!countMode)
          multiplayerConnector.playMove(new Move(moveNb, color, Point.PASS, Point.PASS));
      } catch (IOException e) {
        Util.messageBox(I18N.failure, e.getMessage(), AlertType.ERROR);
      }
      if (playMode == P2P_MODE) {
        SgfNode father = currentNode.getFather();
        if (father != null && father.isPass() && !gameHasEnded) {
          startCountMode(false); // exact mode
        }
      }

      break;

    //#endif

    case GAME_MODE:
      if (currentNode.isPass() && !gameHasEnded) {
        startCountMode(false); // exact mode
      }
      playNewMove(color, Point.PASS, Point.PASS);
      break;
    }

  }

  public void doClick() {
    SgfNode next = currentNode.searchChildren(cursor);
    byte color = getCurrentPlayerColor();

    switch (playMode) {
    //#if IGS || BT
    case ONLINE_MODE:
    case P2P_MODE:
      if (!gameHasEnded) {
        if (color == onlineColor && board.isValidMove(cursor, color)) {
          // next = playNewMove(color, cursor.getX(), cursor.getY());
          try {
            multiplayerConnector.playMove(new Move(moveNb, color, cursor.x, cursor.y));
          } catch (IOException e) {
            Util.messageBox(I18N.failure, e.getMessage(), AlertType.ERROR);
          }
        } else {
          if (!board.isValidMove(cursor, color))
            canvas.setSplashInfo(I18N.notValidMove);
          else
            canvas.setSplashInfo(I18N.online.notYourTurn);
        }
      } else
        canvas.setSplashInfo(I18N.gameHadEnded);
      break;
    //#endif
    case GAME_MODE:
      if (!gameHasEnded && !connectToIgs) {
        if (next != null) {
          playNode(next);
        } else if (board.isValidMove(cursor, color)) {
          next = playNewMove(color, cursor.x, cursor.y);
        }
      } else if (connectToIgs) {
        canvas.setSplashInfo(I18N.online.noStartedGame);
      } else
        canvas.setSplashInfo(I18N.gameHadEnded);
      break;
    case PROBLEM_MODE:
      if (next != null) {
        playNode(next);
        if (next.getSon() != null && playMode == PROBLEM_MODE) {
          playNode(next.getSon());
        }
      } else {
        canvas.setSplashInfo(I18N.wrongMove);
      }
      if (currentNode.getSon() == null) // no more move
      {
        if (model.isCorrectNode(currentNode)) {
          canvas.setSplashInfo(I18N.rightMove);
        } else {
          canvas.setSplashInfo(I18N.wrongMove);
        }

      }
      break;
    }
    //log.debug("repaint() called from doClick");
    canvas.refresh(canvas.getBoardPainter().getDrawArea());
  }

  private SgfNode getLeaf(SgfNode node) {
    SgfNode son = node;
    while (son.getSon() != null) {
      son = son.getSon();
    }
    return son;
  }

  /**
   * @param color
   * @return
   */
  public SgfNode playNewMove(byte color, byte x, byte y) {
    if (playMode == OBSERVE_MODE) {
      if (clock != null) {
        clock.clockOnlineSwitcher(color);
      }
      SgfNode lastNode = getLeaf(currentNode);
      if (lastNode != currentNode) {
        canvas.setSplashInfo(I18N.online.movePlayed);
        SgfNode next = lastNode.addBranch(new SgfPoint(x, y));
        next.setPlayerColor(color);

        return next;
      }
    }

    if (playMode == GAME_MODE && !connectToIgs) {
      if (clock != null) {
        clock.clockSwitcher(getCurrentPlayerColor());
      }
    }

    if (playMode == ONLINE_MODE || playMode == P2P_MODE) {
      if (clock != null) {
        clock.clockOnlineSwitcher(color);
      }

    }

    SgfNode next = currentNode.addBranch(new SgfPoint(x, y));
    next.setPlayerColor(color);

    playNode(next);
    if (!next.isPass())
      moveCursor(x, y);
    else
      warnPassed(color);
    return next;
  }

  /**
   * @return
   */
  public byte getCurrentPlayerColor() {

    if (currentNode == null)
      return Board.BLACK;

    byte color = (byte) -currentNode.getPlayerColor(); // color=who is

    // going to play
    if (color == 0)
      color = model.getFirstPlayer(); // take the first move from
    // the model if we have no
    // clue
    return color;
  }

  /**
   * @param node
   */
  private void playNode(final SgfNode node) {
    if (node.getPoint() != null) {
      Vector vec = board.play(node.getPoint(), node.getPlayerColor());
      if (vec.size() == 0)
        vec = null;
      node.setDeadStones(vec); // the player color is reversed inside
      node.setKo(board.getKo());
      moveNb++;
    }

    if (node.isPass())
      moveNb++;

    board.placeStones(node.getAB(), Board.BLACK);
    board.placeStones(node.getAW(), Board.WHITE);

    switchCurrentNode(node);

    if (showHint) {
      SgfNode son = node.getSon();

      if (son != null && !son.isPass() && (playMode == REVIEW_MODE || playMode == JOSEKI_MODE)) { // Make
        // the
        // cursor
        // follow
        // the next move if it is a
        // game watch
        if (son.getPoint() != null)
          cursor = son.getPoint().clone();
      }
    }

  }

  public boolean doGoBack(boolean rootAllowed) {
    if (countMode && !gameHasEnded) {
      resumeFromCounting();
      return true;
    }
    SgfNode prev = currentNode.searchFather();

    if ((rootAllowed && prev != null) || (!rootAllowed && prev != null && model.getRoot() != prev)) {
      board.placeStone(currentNode.getPoint(), Board.EMPTY);
      Vector deadStones = currentNode.getDeadStones();
      if (deadStones != null) {
        board.placeStones(deadStones, currentNode.getDeadColor());
        board.compensateScore(deadStones.size(), currentNode.getDeadColor());

      }
      if (moveNb > 0)
        moveNb--;

      board.placeStones(currentNode.getAW(), Board.EMPTY);
      board.placeStones(currentNode.getAB(), Board.EMPTY);

      switchCurrentNode(prev);
      if (prev.isPass())
        warnPassed(prev.getPlayerColor());

      return true;
    }
    return false;
  }

  public boolean doGoNext() {
    SgfNode next;

    next = currentNode.getSon();
    if (next != null) {
      if (next.isPass() && !countMode) {
        warnPassed(next.getPlayerColor());
        if (currentNode.isPass())
          startCountMode(false);
      }
      playNode(next);
      return true;
    }
    return false;
  }

  private void warnPassed(byte color) {
    if (!countMode)
      canvas.setSplashInfo((color == 1 ? I18N.game.blackLong : I18N.game.whiteLong) + " " + I18N.game.passed);
  }

  public void doGoPrevBrother() {
    SgfNode current = currentNode;
    if (!doGoBack(false)) // if there is no parent ignore
      return;
    Vector children = currentNode.getChildren();
    int index = children.indexOf(current) - 1;
    if (index == -1)
      index = children.size() - 1; // roll around
    SgfNode nextOne = (SgfNode) children.elementAt(index);
    playNode(nextOne);

  }

  public void doGoNextBrother() {
    SgfNode current = currentNode;
    if (!doGoBack(false)) // if there is no parent ignore
      return;
    Vector children = currentNode.getChildren();
    int index = children.indexOf(current) + 1;
    if (index == children.size())
      index = 0; // roll around
    SgfNode nextOne = (SgfNode) children.elementAt(index);
    playNode(nextOne);
  }

  public void reverseShowHint() {
    showHint = !showHint;
  }

  public void setCurrentNodeComment(String comment) {
    if (comment != null && comment.length() == 0) {
      comment = null;
    }
    currentNode.setComment(comment);
    canvas.setCurrentComment(comment);
  }

  private void switchCurrentNode(SgfNode node) {
    currentNode = node;
    String comment = currentNode.getComment();
    if ((playMode == PROBLEM_MODE) && currentNode.getFather() != null && currentNode.getFather().getComment() != null) {
      canvas.setCurrentComment(currentNode.getFather().getComment() + "\n--\n" + comment);

    } else {
      canvas.setCurrentComment(comment);
    }
  }

  public boolean hasMoreNode() {
    return (currentNode.getSon() != null);
  }

  /**
   * @param bZoomIn
   *          The bZoomIn to set.
   */
  void setZoomIn(boolean bZoomIn) {
    this.zoomedIn = bZoomIn;
    refreshPainter();

  }

  /**
   * @param zoomedIn
   */
  public void refreshPainter() {
    if (zoomedIn) {
      switchToZoomedPainter();
    } else {
      switchToNormalPainter();
    }
  }

  /**
   * @return Returns the bZoomIn.
   */
  boolean isZoomIn() {
    return zoomedIn;
  }

  /**
   * @return Returns the currentNode.
   */
  public SgfNode getCurrentNode() {
    return currentNode;
  }

  public void downloadFailure(Exception reason) {
    Util.messageBox(I18N.failure, reason.getMessage(), AlertType.ERROR); //$NON-NLS-1$
  }

  /**
   * @return Returns the moveNb.
   */
  public int getMoveNb() {
    return moveNb;
  }

  public void newGame(byte size, int handi, char gameMode) {
    gameHasEnded = false;
    playMode = gameMode;
    SgfModel game = SgfModel.createNewModel(size, handi, I18N.game.whiteLong, I18N.game.blackLong); //$NON-NLS-1$ //$NON-NLS-2$

    if (playMode == GAME_MODE) {
      stopClockAndStopPainingClock();

      clock = new ClockController(false, this);

      if (handi > 0)
        clock.clockSwitcher(Board.BLACK);
      else
        clock.clockSwitcher(Board.EMPTY);
    }

    reset(Gome.singleton.mainCanvas, gameMode);
    downloadFinished(game, gameMode);
  }

  //#if IGS
  public void connectToIGS() {
    try {
      if (!tryingToConnect) {
        tryingToConnect = true;
        // log.debug("Connect to IGS");

        multiplayerConnector = new IGSConnector(Gome.singleton.options.igsLogin, Gome.singleton.options.igsPassword, this);
        // log.debug(" - start thread");
        canvas.setSplashInfo(I18N.online.connecting);
        multiplayerConnector.start();
      }
    } catch (Exception e) {
      tryingToConnect = false;
      canvas.setSplashInfo(I18N.online.connectionError);
      Util.messageBox(I18N.failure, e.getMessage(), AlertType.ERROR);
    }
  }

  //#endif

  //#if BT
  public void connectToBT() {
    try {
      if (!tryingToConnect) {
        tryingToConnect = true;
        //#if DEBUG
        log.debug("Connect to BT");
        //#endif
        canvas.setSplashInfo(I18N.bt.connecting);
        multiplayerConnector = new BluetoothClientConnector(this);
        //#if DEBUG
        log.debug(" - start thread");
        //#endif
        multiplayerConnector.start();
      }
    } catch (Exception e) {
      tryingToConnect = false;
      canvas.setSplashInfo(I18N.bt.connectionError);
      Util.messageBox(I18N.failure, e.getMessage(), AlertType.ERROR);
    }
  }

  //#endif

  public void setSplashInfo(String message) {
    canvas.setSplashInfo(message);
  }

  //#if IGS
  public void getServerGameList() {
    canvas.setSplashInfo(I18N.online.gettingGameList);
    try {
      ((IGSConnector) multiplayerConnector).getGames();
    } catch (IOException e) {
      Util.messageBox(I18N.failure, e.getMessage(), AlertType.ERROR);
    }
  }

  //#endif
  //#if IGS
  public void getServerUserList() {
    canvas.setSplashInfo(I18N.online.getUserList);
    try {
      ((IGSConnector) multiplayerConnector).getUsers();
    } catch (IOException e) {
      Util.messageBox(I18N.failure, e.getMessage(), AlertType.ERROR);
    }
  }

  //#endif
  //#if IGS
  public void observeServerGame(int selectedIndex) {
    canvas.setSplashInfo(I18N.online.connecting);
    try {
      currentIgsGame = ((IGSConnector) multiplayerConnector).getGameList()[selectedIndex];
      ((IGSConnector) multiplayerConnector).observe(currentIgsGame.nb);

    } catch (Exception e) {
      Util.messageBox(I18N.failure, e.getMessage(), AlertType.ERROR);
    }
  }

  //#endif
  //#if IGS || BT
  public void acceptChallenge(Challenge challenge) {
    canvas.setSplashInfo(I18N.online.acceptChallenge);
    try {
      multiplayerConnector.acceptChallenge(challenge);
    } catch (Exception e) {
      Util.messageBox(I18N.failure, e.getMessage(), AlertType.ERROR);
    }
  }

  //#endif
  //#if IGS
  public void declineChallenge(Challenge currentChallenge) {
    try {
      multiplayerConnector.decline(currentChallenge.nick);
    } catch (Exception e) {
      Util.messageBox(I18N.failure, e.getMessage(), AlertType.ERROR);
    }

  }

  //#endif
  //#if IGS
  public void challengeServerUser(int selectedIndex) {
    canvas.setSplashInfo(I18N.online.sendChallenge);
    try {
      // log.debug("challenge " + igs.getUserList()[selectedIndex]);
      Challenge challenge = new Challenge();
      challenge.nick = ((IGSConnector) multiplayerConnector).getUserList()[selectedIndex].nick;

      challenge.color = Gome.singleton.options.igsColor;
      challenge.time_minutes = Gome.singleton.options.igsMinutes;
      challenge.min_per25moves = Gome.singleton.options.igsByoyomi;
      challenge.size = Gome.singleton.options.igsSize;
      multiplayerConnector.challenge(challenge);
    } catch (Exception e) {
      Util.messageBox(I18N.failure, e.getMessage(), AlertType.ERROR);
    }
  }

  //#endif
  //#if BT
  public void challengeBT() {
    canvas.setSplashInfo(I18N.online.sendChallenge);
    try {
      Challenge challenge = new Challenge();
      challenge.nick = ""; // unused 
      challenge.color = Gome.singleton.options.igsColor;
      challenge.time_minutes = Gome.singleton.options.igsMinutes;
      challenge.min_per25moves = Gome.singleton.options.igsByoyomi;
      challenge.size = Gome.singleton.options.igsSize;
      multiplayerConnector.challenge(challenge);
    } catch (Exception e) {
      Util.messageBox(I18N.failure, e.getMessage(), AlertType.ERROR);
    }
  }

  //#endif

  //#if IGS
  public void loggedEvent() {
    tryingToConnect = false;
    // log.debug("GC: IGS Login Successful");
    stopClockAndStopPainingClock();
    Gome.singleton.mainCanvas.switchToIGSOnlineMenu();
    tuneBoardPainter();
    canvas.setSplashInfo(I18N.online.connectedToIgs);
    Gome.singleton.menuEngine.switchToOnline();
    connectToIgs = true;
    // set splash will repaint
  }

  //#endif

  //#if BT
  public void connectedBTEvent(MultiplayerConnector connector) {
    this.multiplayerConnector = connector;
    tryingToConnect = false;
    stopClockAndStopPainingClock();
    Gome.singleton.mainCanvas.switchToBTOnlineMenu();
    tuneBoardPainter();
    canvas.setSplashInfo(I18N.bt.connected);
    Gome.singleton.menuEngine.switchToOnline();
    connectToIgs = true;
    // set splash will repaint
  }

  //#endif

  //#if IGS
  public void gameListEvent(Game[] games) {
    Gome.singleton.menuEngine.showIgsGameList(games);
  }

  //#endif
  //#if IGS
  public void observeEvent(Move[] moves) {
    try {
      newGame(currentIgsGame.size, currentIgsGame.handi, OBSERVE_MODE);

      clock = new ClockController(true, this);

      canvas.assignClockController(clock);
      canvas.updateClockAndCommentMode(MainCanvas.CLOCK_MODE);

      Move move = null;
      SgfNode current = currentNode;
      for (int i = 0; i < moves.length; i++) {
        move = moves[i];
        current = current.addBranch(new SgfPoint(move.x, move.y));
        current.setPlayerColor(move.color);
      }
      tuneBoardPainter();
      Gome.singleton.mainCanvas.setSplashInfo(null);
      //canvas.refresh(canvas.getBoardPainter().getDrawArea());
      goToLastMove();
    } catch (Exception e) {
      Util.errorNotifier(e);
    }
  }

  //#endif
  //#if IGS || BT
  public void moveEvent(Move move) {
    // log.debug("Move received from server");
    Gome.singleton.mainCanvas.setSplashInfo(null);

    if (move.x == Point.PASS) {
      if (playMode == P2P_MODE && currentNode.isPass() && !gameHasEnded) {
        startCountMode(false); // exact mode
      }

    }
    //
    if (moveNb == 1) {
      Gome.singleton.mainCanvas.removeOnlineSetHandicapMenuItem();
    } else if (moveNb == 20) {
      Gome.singleton.mainCanvas.removeOnlineSetKomiAndHandicapMenuItem();
    }

    playNewMove(move.color, move.x, move.y);
    tuneBoardPainter();
    canvas.refresh(canvas.getBoardPainter().getDrawArea());
  }

  //#endif
  //#if IGS
  public void userListEvent() {
    Gome.singleton.mainCanvas.setSplashInfo(null);
    User[] userList = ((IGSConnector) multiplayerConnector).getUserList();
    QuickSortable.quicksort(userList); //inplace sort, external criteria
    Gome.singleton.menuEngine.showIgsUserList(userList);
  }

  //#endif
  //#if IGS || BT
  public void challenge(Challenge challenge) {
    //#if DEBUG
    log.debug("Received Challenge from user " + challenge.nick);
    //#endif
    Gome.singleton.menuEngine.showIgsChallenge(challenge);
  }

  //#endif
  //#if IGS
  public void message(byte type, String nick, String message) {
    switch (type) {
    case MultiplayerCallback.MESSAGE_CHAT_TYPE:
      Gome.singleton.menuEngine.incomingChatMessage(nick, message);
      break;

    case MultiplayerCallback.MESSAGE_ERROR_TYPE:
      if (tryingToConnect) {
        tryingToConnect = false;
      }
      connectToIgs = false;
      canvas.setSplashInfo(message);
      stopClockAndStopPainingClock();
      setPlayMode(REVIEW_MODE);
      break;

    case MultiplayerCallback.MESSAGE_KIBITZING_TYPE:
      String comment = currentNode.getComment();
      StringBuffer buffer;
      if (comment == null) {
        buffer = new StringBuffer();
      } else {
        buffer = new StringBuffer(comment);
      }
      buffer.append('\n');
      buffer.append(nick);
      buffer.append(": ");
      buffer.append(message);
      currentNode.setComment(buffer.toString());
      switchCurrentNode(currentNode);
      break;
    }

  }

  //#endif
  //#if IGS
  public void startGame(Challenge challenge, char mode) {
    canvas.setSplashInfo(I18N.online.onlineGameStarted);
    //#if DEBUG
    log.debug("Start online game");
    log.debug("Your color " + challenge.color);
    //#endif
    onlineColor = challenge.color;
    int time = challenge.time_minutes * 60;
    int byoStone = 25;
    int byoTime = challenge.min_per25moves * 60;

    clock = new ClockController(time, byoTime, byoStone, this);

    canvas.assignClockController(clock);
    canvas.updateClockAndCommentMode(MainCanvas.CLOCK_MODE);

    newGame(challenge.size, 0, mode); // we don't know yet the
    // handi
    if (playMode == ONLINE_MODE) {
      if (onlineColor != Board.BLACK) {
        Gome.singleton.mainCanvas.removeOnlineSetHandicapMenuItem();
      }
    }

  }

  //#endif
  //#if IGS
  public void disconnectFromServer() {
    connectToIgs = false;
    // log.debug("disconnect from IGS");
    multiplayerConnector.disconnect();
    canvas.setSplashInfo(I18N.online.disconnected);
    Gome.singleton.menuEngine.switchToOffline();

    stopClockAndStopPainingClock();

    clock = new ClockController(false, this);

    setPlayMode(REVIEW_MODE);
  }

  //#endif

  public void goToFirstMove() {
    notifyLoadReady();
    tuneBoardPainter();
    canvas.refresh(canvas.getBoardPainter().getDrawArea());
  }

  public void goToLastMove() {

    SgfNode node = currentNode;
    SgfPoint point;
    while ((node = node.getSon()) != null) {
      if (node.getSon() == null)
        break; //zap the last one to play it fully
      point = node.getPoint();
      if (point != null) {
        Vector vec = board.play(point, node.getPlayerColor());
        if (vec.size() == 0)
          vec = null;
        node.setDeadStones(vec); // the player color is reversed inside
        node.setKo(board.getKo());
        moveNb++;
      }

      if (node.isPass())
        moveNb++;
    }
    if (node != null)
      playNode(node);// play correctly the last one
    tuneBoardPainter();
    canvas.refresh(canvas.getBoardPainter().getDrawArea());
  }

  //#if IGS
  public String getNick(int userIndex) {
    return ((IGSConnector) multiplayerConnector).getUserList()[userIndex].nick;
  }

  public void sendOnlineMessage(String nickToSend, String message) {
    try {
      multiplayerConnector.sendMessage(nickToSend, message);
    } catch (IOException e) {
      Util.messageBox(I18N.failure, e.getMessage(), AlertType.ERROR);
    }
  }

  private void displayScore(int white, int black) {
    byte komi = model.getByteKomi();
    white += komi / 2;
    boolean dotfivedKomi = (komi % 2) != 0;
    int diff = white - black;
    StringBuffer scoreSentence = new StringBuffer(I18N.game.score);
    if (diff > 0 || (diff == 0 && dotfivedKomi)) {
      scoreSentence.append(I18N.game.whiteWin);
      scoreSentence.append(diff);
    } else {
      if (diff < 0) {
        diff = -diff;
        scoreSentence.append(I18N.game.blackWin);
        if (dotfivedKomi && komi > 0) {
          scoreSentence.append(diff - 1);
        } else {
          scoreSentence.append(diff);
        }
      } else if (diff == 0 && !dotfivedKomi)
        scoreSentence.append(I18N.game.jigo);
    }
    if (dotfivedKomi) {
      scoreSentence.append(".5");
    }
    canvas.setSplashInfo(scoreSentence.toString());
  }

  //#endif
  public void doScore() {
    //#if DEBUG
    log.debug("doScore");
    //#endif
    if (playMode == GAME_MODE || playMode == REVIEW_MODE || playMode == P2P_MODE) {
      if (!evaluationMode) {
        gameHasEnded = true;
        stopClockAndStopPainingClock();
      }
      countMode = false;
      int[] scores = board.getScore();
      int white = scores[0];
      int black = scores[1];
      setPlayMode(playMode);
      displayScore(white, black);
      //#if BT
      if (playMode == P2P_MODE) {
        try {
          multiplayerConnector.doneWithTheCounting(white, black);
        } catch (IOException e) {
          // TODO Error Handling

        }
      }
      //#endif
    }

    //#if IGS
    else if (playMode == ONLINE_MODE) { // TODO P2P ?
      askIgsForScore();
    }
    //#endif
  }

  //#if IGS
  public void askIgsForScore() {
    try {
      ((IGSConnector) multiplayerConnector).getScore();
      canvas.setSplashInfo(I18N.online.gettingScore);
    } catch (Exception e) {
      Util.errorNotifier(e);
    }
  }

  public void doneWithScore() {
    try {
      //#if DEBUG
      log.info("DONE WITH Scoring");
      //#endif
      multiplayerConnector.doneWithTheCounting(-1, -1); // scores are irrelevent there
      canvas.setSplashInfo(I18N.count.doneWithScoring);
    } catch (Exception e) {
      Util.errorNotifier(e);
    }
  }

  //#endif

  public void startCountMode(boolean evaluationMode1) {
    clock.pauseClock();
    canvas.stopClockPainting();
    countMode = true;
    this.evaluationMode = evaluationMode1;
    board.startCounting(evaluationMode1, (playMode != ONLINE_MODE) && !evaluationMode1);
    Gome.singleton.mainCanvas.updateClockAndCommentMode(MainCanvas.NOTHING_TO_DISPLAY_MODE);
    canvas.setSplashInfo(I18N.count.markDeadStone);
    //#if IGS || BT
    if (playMode == ONLINE_MODE || playMode == P2P_MODE)
      canvas.switchToOnlineCountingMenu();
    else
      //#endif
      canvas.switchToCountingMenu();
    tuneBoardPainter();
  }

  public void resumeFromCounting() {
    countMode = false;
    clock.resumeClock();
    canvas.recalculateLayout();
    setPlayMode(playMode); // reput the menu as normal 
    tuneBoardPainter();
  }

  public void endCounting() {
    countMode = false;
    board.endCounting();
    setPlayMode(playMode); // reput the menu as normal 
    tuneBoardPainter();
  }

  public boolean isCountMode() {
    return countMode;
  }

  public Point getCursor() {
    return cursor;
  }

  public boolean getShowHints() {
    return showHint;
  }

  //#if IGS
  public void synOnlineTime(int whiteTime, int whiteByoStone, int blackTime, int blackByoStone) {
    if (clock.thereIsClock()) {
      clock.synTime(whiteTime, whiteByoStone, blackTime, blackByoStone);
    }
  }

  //#endif

  public void gameIsFinished(byte color) {
    if (playMode == GAME_MODE) {
      switch (color) {
      case Board.BLACK:
        canvas.setSplashInfo(I18N.clock.blackTimeUp);
        break;

      case Board.WHITE:
        canvas.setSplashInfo(I18N.clock.whiteTimeUp);
        break;

      }
      setPlayMode(REVIEW_MODE);
    }
  }

  public void timesUP(String name) {
    canvas.setSplashInfo(name + I18N.clock.timesUp);
    currentNode.setComment(name + I18N.clock.timesUp);
    gameHasEnded = true;
    //#if IGS
    canvas.switchToIGSOnlineMenu();
    //#endif
    playMode = REVIEW_MODE;
  }

  //#if IGS
  public void endGame() {
    startCountMode(false);
    canvas.setSplashInfo(I18N.count.endGameMarkStone);
  }

  //#endif

  public void restoreGameForCounting() {
    startCountMode(false);
    gameHasEnded = false;
    //#if IGS
    canvas.removeOnlineSetKomiAndHandicapMenuItem();
    //#endif
    canvas.setSplashInfo(I18N.count.restoreCounting);
    canvas.refresh(canvas.getBoardPainter().getDrawArea());
  }

  public void oppRemoveDeadStone(byte x, byte y) {
    if (countMode == true) {
      if (!board.hasBeenRemove(x, y)) {
        board.markDeadGroup(x, y);
        // log.debug("opp has mark dead stone x: " + x + " y:" + y);
        tuneBoardPainter();
        canvas.refresh(canvas.getBoardPainter().getDrawArea());
      }
    }
  }

  //#if IGS
  public void gameIsDone(String name1, int value1, String name2, int value2) {
    endCounting();
    String out = "";
    gameHasEnded = true;

    String v1 = String.valueOf(value1);
    v1 = v1.substring(0, v1.length() - 1) + "." + v1.substring(v1.length() - 1);

    String v2 = String.valueOf(value2);
    v2 = v2.substring(0, v2.length() - 1) + "." + v2.substring(v2.length() - 1);

    if (value1 > value2) {
      String v3 = String.valueOf(value1 - value2);
      v3 = v3.substring(0, v3.length() - 1) + "." + v3.substring(v3.length() - 1);
      out = name1 + " (" + v1 + ") " + name2 + " (" + v2 + ") " + name1 + I18N.game.winBy + v3;
    }
    if (value1 < value2) {
      String v3 = String.valueOf(value2 - value1);
      v3 = v3.substring(0, v3.length() - 1) + "." + v3.substring(v3.length() - 1);

      out = name2 + " (" + v2 + ") " + name1 + " (" + v1 + ") " + name2 + I18N.game.winBy + v3;
    }

    currentNode.setComment(out);

    canvas.setSplashInfo(out);
    canvas.refresh(canvas.getBoardPainter().getDrawArea());

    playMode = GAME_MODE;
    Gome.singleton.mainCanvas.switchToIGSOnlineMenu();
  }

  //#endif
  //#if IGS
  public void oppSetHandicap(byte handicap) {
    if (model != null && playMode == ONLINE_MODE) {
      //#if DEBUG
      log.debug("Setting handicap: " + handicap);
      //#endif
      newGame(model.getBoardSize(), handicap, playMode);

      Gome.singleton.mainCanvas.removeOnlineSetHandicapMenuItem();

      clock.clockSwitcher(Board.BLACK);
      canvas.refresh(canvas.getBoardPainter().getDrawArea());
    }
  }

  //#endif

  //#if IGS
  public void oppWantToSetNewKomi(byte k) {
    if (model != null && playMode == ONLINE_MODE) {
      if (k != gomeWantOnlineKomi) {
        Gome.singleton.menuEngine.showOppWantKomi(k);
        gomeWantOnlineKomi = k;
      } else {
        setKomi(k);
        canvas.setSplashInfo(I18N.online.opponentAgreedNewKomi);
      }
    }
  }

  //#endif

  //#if IGS
  public void setKomi(byte k) {
    if (model != null && playMode == ONLINE_MODE) {
      String temp = Util.komi2String(k);
      canvas.setSplashInfo(I18N.online.newKomi + temp);
      model.setNewKomi(k);
    }
  }

  public void onlineSetKomi(byte k) {
    if (model != null && playMode == ONLINE_MODE) {
      try {
        multiplayerConnector.setKomi(k);
      } catch (IOException e) {
        Util.messageBox(I18N.failure, e.getMessage(), AlertType.ERROR);
      }
    }
  }

  public void onlineResigned(String name) {
    String color = null;

    if (currentNode.getPlayerColor() == Board.BLACK)
      color = I18N.game.whiteLong;
    else
      color = I18N.game.blackLong;
    String message = name + " (" + color + ") " + I18N.resigned;
    canvas.setSplashInfo(message);
    stopClockAndStopPainingClock();
    currentNode.setComment(message);
    gameHasEnded = true;

    playMode = REVIEW_MODE;
    Gome.singleton.mainCanvas.switchToIGSOnlineMenu();
  }

  //#endif
  public void resign() {
    if (playMode == GAME_MODE && !gameHasEnded) {

      String message = I18N.whiteResigned;
      if (getCurrentPlayerColor() == Board.BLACK) {
        message = I18N.blackResigned;
      }
      currentNode.setComment(message);

      stopClockAndStopPainingClock();
      // log.info(message);
      canvas.setSplashInfo(message);

      setPlayMode(REVIEW_MODE);
      gameHasEnded = true;
    }
    //#if IGS || BT
    else if (playMode == ONLINE_MODE || playMode == P2P_MODE) {
      if (getCurrentPlayerColor() == onlineColor || countMode == true) {
        gameHasEnded = true;
        try {
          multiplayerConnector.resign();
        } catch (IOException e) {
          Util.messageBox(I18N.failure, e.getMessage(), AlertType.ERROR);
        }
      } else {
        canvas.setSplashInfo(I18N.online.notYourTurn);
      }
    }
    //#endif
  }

  private void stopClockAndStopPainingClock() {
    if (clock != null) {
      clock.stopClock();
      canvas.stopClockPainting();
    }
  }

  //#if IGS
  public void gomeWantToSetOnlineKomi(byte komi, int mode) {
    if (playMode == ONLINE_MODE) {

      String komiString = Util.komi2String(komi);
      if (mode == 0) {
        canvas.setSplashInfo(I18N.online.requestingBlackKomi + komiString);
        onlineSetKomi(komi);
        gomeWantOnlineKomi = komi;
      } else {
        canvas.setSplashInfo(I18N.online.requestingWhiteKomi + komiString);
        onlineSetKomi((byte) -komi);
        gomeWantOnlineKomi = (byte) -komi;
      }

    }
  }

  public void gomeSetOnlineHandicap(byte h) {
    if (playMode == ONLINE_MODE) {
      if (h > 1) {
        canvas.setSplashInfo(I18N.settingHandicapTo + h);
        try {
          multiplayerConnector.setHandicap(h);
        } catch (IOException e) {
          Util.messageBox(I18N.failure, e.getMessage(), AlertType.ERROR);
        }
      }
    }
  }

  public void gomeRestoreGameForCounting() {
    if (model != null) {
      try {
        multiplayerConnector.resetDeadStone();
      } catch (IOException e) {
        Util.messageBox(I18N.failure, e.getMessage(), AlertType.ERROR);
      }
    }
  }

  public void onlineScore(int whiteScore, int blackScore) {
    displayScore(whiteScore, blackScore);
    if (playMode == P2P_MODE) {
      // No server round trip in P2P so we can end the game now
      gameHasEnded = true;
      countMode = false;
      setPlayMode(playMode);
    }
  }

  //#endif

  public void winByValue(String name, int winByValue) {
    String v1 = String.valueOf(winByValue);
    v1 = v1.substring(0, v1.length() - 1) + "." + v1.substring(v1.length() - 1);

    canvas.setSplashInfo(name + I18N.game.winBy + v1);
  }

  public Board getBoard() {
    return board;
  }

  private static final int CORNER_DEF = 3;

  private int currentQuadrant() {
    boolean left = cursor.x < board.getBoardSize() / 2;
    boolean top = cursor.y < board.getBoardSize() / 2;
    return (left ? Graphics.LEFT : Graphics.RIGHT) | (top ? Graphics.TOP : Graphics.BOTTOM);
  }

  public void nextCorner() {
    int boardSize = board.getBoardSize();
    byte other = (byte) (boardSize - CORNER_DEF - 1);
    byte x = 0;
    byte y = 0;

    switch (currentQuadrant()) {
    case Graphics.TOP | Graphics.LEFT:
      x = other;
      y = CORNER_DEF;
      break;
    case Graphics.TOP | Graphics.RIGHT:
      x = other;
      y = other;
      break;
    case Graphics.BOTTOM | Graphics.RIGHT:
      x = CORNER_DEF;
      y = other;
      break;
    case Graphics.BOTTOM | Graphics.LEFT:
      x = CORNER_DEF;
      y = CORNER_DEF;
      break;
    }
    if (playArea.contains(x, y)) {
      cursor.x = x;
      cursor.y = y;
      spanInZoomedModeIfTheCursorIsOut();
      canvas.refresh(canvas.getBoardPainter().getDrawArea());
    }
  }

  public void prevCorner() {
    int boardSize = board.getBoardSize();
    byte other = (byte) (boardSize - CORNER_DEF - 1);
    byte x = 0;
    byte y = 0;
    switch (currentQuadrant()) {
    case Graphics.TOP | Graphics.LEFT:
      x = CORNER_DEF;
      y = other;
      break;
    case Graphics.TOP | Graphics.RIGHT:
      x = CORNER_DEF;
      y = CORNER_DEF;
      break;
    case Graphics.BOTTOM | Graphics.RIGHT:
      x = other;
      y = CORNER_DEF;
      break;
    case Graphics.BOTTOM | Graphics.LEFT:
      x = other;
      y = other;
      break;
    }
    if (playArea.contains(x, y)) {
      cursor.x = x;
      cursor.y = y;
      spanInZoomedModeIfTheCursorIsOut();
      canvas.refresh(canvas.getBoardPainter().getDrawArea());
    }
  }

  //#if BT
  public void disconnectBT() {
    multiplayerConnector.disconnect();
    multiplayerConnector = null;
  }

  //#endif

  public void rollCurrentMark() {
    // whatever happens, refresh the current position
    board.getChangeMask()[cursor.x][cursor.y] = false;

    // switch the annotation
    Vector annotations = currentNode.getAnnotations();
    if (annotations != null) {
      int size = annotations.size();
      for (int i = 0; i < size; i++) {
        Object elementAt = annotations.elementAt(i);
        if (elementAt instanceof SymbolAnnotation) {
          SymbolAnnotation symbol = (SymbolAnnotation) elementAt;
          if (symbol.x == cursor.x && symbol.y == cursor.y) {
            int type = symbol.getType();
            if (type == SymbolAnnotation.MAX_TYPE) { // after maxtype remove it
              annotations.removeElementAt(i);
              // ask for a refresh of the area
              canvas.refresh(canvas.getBoardPainter().getDrawArea());
              return;
            }
            symbol.setType(++type); // switch it to the next type
            // ask for a refresh of the area
            canvas.refresh(canvas.getBoardPainter().getDrawArea());
            return;
          }
        }
      }
    }
    // else create it...
    SymbolAnnotation symbol = new SymbolAnnotation(cursor, 0); // the first type
    currentNode.addAnnotation(symbol);
    // ask for a refresh of the area
    canvas.refresh(canvas.getBoardPainter().getDrawArea());
  }
}