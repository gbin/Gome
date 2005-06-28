/*
 * (c) 2006  Indigonauts
 */
package com.indigonauts.gome.ui;

import java.io.IOException;
import java.util.Vector;

import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Display;

import com.indigonauts.gome.ClockController;
import com.indigonauts.gome.Gome;
import com.indigonauts.gome.MainCanvas;
import com.indigonauts.gome.common.Point;
import com.indigonauts.gome.common.QuickSortable;
import com.indigonauts.gome.common.Rectangle;
import com.indigonauts.gome.common.Util;
import com.indigonauts.gome.igs.ServerCallback;
import com.indigonauts.gome.igs.ServerChallenge;
import com.indigonauts.gome.igs.ServerConnector;
import com.indigonauts.gome.igs.ServerGame;
import com.indigonauts.gome.igs.ServerMove;
import com.indigonauts.gome.igs.ServerUser;
import com.indigonauts.gome.io.CollectionEntry;
import com.indigonauts.gome.sgf.Board;
import com.indigonauts.gome.sgf.SgfModel;
import com.indigonauts.gome.sgf.SgfNode;
import com.indigonauts.gome.sgf.SgfPoint;

//#ifdef IGS
public class GameController implements ServerCallback
//#else
//# public class GameController
//#endif
{
  //#ifdef DEBUG
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("GameController");
  //#endif

  public static final char GAME_MODE = 'G';

  public static final char JOSEKI_MODE = 'J';

  public static final char REVIEW_MODE = 'R';

  public static final char PROBLEM_MODE = 'P';

  public static final char ONLINE_MODE = 'O';

  public static final char OBSERVE_MODE = 'B';

  public static final char TEXT_MODE = 'T';

  private SgfModel model;

  private Board board;

  private Rectangle playArea;

  private SgfPoint cursor = new SgfPoint((byte) 0, (byte) 0);

  private SgfNode currentNode;

  public Display display;

  private FileLoader fileLoader;

  private MainCanvas canvas;

  private boolean gameHasEnded = false;

  // options
  private boolean bShowHint;

  private char playMode = GAME_MODE;

  private boolean bZoomIn;

  private boolean countMode = false;
  private boolean evaluationMode = false;

  private CollectionEntry currentCollection;

  private int currentIndexInCollection;

  private int moveNb = 0;

  //#ifdef IGS
  ServerConnector igs;
  //#endif

  //#ifdef IGS
  ServerGame currentIgsGame = null;

  //#endif

  //#ifdef IGS
  private byte onlineColor;

  //#endif

  private ClockController clock;

  private boolean tryingToConnect = false;

  private boolean connectToIgs = false;

  private byte gomeWantOnlineKomi = 0;

  public GameController(Display display) {
    this.display = display;
    model = new SgfModel();
    moveNb = 0;
    bZoomIn = false;
    // log.debug("Game Controller instantiated");
  }

  public boolean hasNextInCollection() {
    if (currentCollection == null)
      return false;
    return currentIndexInCollection < currentCollection.getCollectionSize() - 1;
  }

  public void reset(MainCanvas c) {
    this.canvas = c;
    currentNode = null;
    model = new SgfModel();
    moveNb = 0;

    playArea = null;
    cursor.x = (byte) 0;
    cursor.y = (byte) 0;
    fileLoader = null;

    bShowHint = false;
    countMode = false;
    setPlayMode(GAME_MODE);
    canvas.assignClockController(clock);
  }

  public char getPlayMode() {
    return playMode;
  }

  public void setPlayMode(char playMode) {
    // if ((this.playMode == GAME_ENDED_MODE) && (playMode != REVIEW_MODE))
    // Gome.singleton.mainCanvas.switchToReviewMenu();

    this.playMode = playMode;

    switch (this.playMode) {
    case GAME_MODE:
      // log.info("Switch to standard play mode");
      Gome.singleton.mainCanvas.switchToPlayMenu();
      break;
    case PROBLEM_MODE:
      // log.info("Switch to problem Mode");
      Gome.singleton.mainCanvas.switchToProblemMenu();
      break;
    case JOSEKI_MODE:
      // log.info("Switch to joseki mode");
      Gome.singleton.mainCanvas.switchToJosekiMenu();
      break;
    case REVIEW_MODE:
      // log.info("Switch to game review mode");
      Gome.singleton.mainCanvas.switchToReviewMenu();
      break;
    //#ifdef IGS
    case ONLINE_MODE:
      // log.info("Switch to Online play mode");
      Gome.singleton.mainCanvas.switchToOnlinePlayMenu();
      break;
    case OBSERVE_MODE:
      // log.info("Switch to Observe play mode");
      Gome.singleton.mainCanvas.switchToObservePlayMenu();
      break;
    //#endif
    }
  }

  public void loadAndPlayNextInCollection() {

    model = new SgfModel(); // free up some memory before reloading
    // something else.
    currentNode = new SgfNode();
    board = new Board();
    fileLoader = new FileLoader(this, currentCollection, ++currentIndexInCollection);
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
    if(currentCollection.getCollectionSize() > 1)
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
    case OBSERVE_MODE:
      playArea = board.getFullBoardArea();
      break;
    case JOSEKI_MODE:
    case PROBLEM_MODE:
      playArea = model.getStoneArea();
      playArea.grow((byte) 3, (byte) 3);
      break;
    }
    //        

    if (playArea == null || model.getViewArea().isValid()) {
      playArea = Rectangle.union(playArea, model.getViewArea());
    }
    playArea = Rectangle.intersect(playArea, board.getFullBoardArea());

    if (playArea == null || !playArea.isValid()) {
      playArea = new Rectangle((byte) 0, (byte) 0, (byte) (board.getBoardSize() - 1), (byte) (board.getBoardSize() - 1));
    }

    cursor.x = (byte) ((playArea.x0 + playArea.x1) / 2);
    cursor.y = (byte) ((playArea.y0 + playArea.y1) / 2);
    switchCurrentNode(model.getFirstNode());
    playNode(currentNode);
    initPainter(); // setclockandcomment need the definitive painter for the layout
    if (model.isCommented()) {
      canvas.updateClockAndCommentMode(MainCanvas.COMMENT_MODE);
    } else {
      canvas.updateClockAndCommentMode(MainCanvas.NOTHING_TO_DISPLAY_MODE);
    }

  }

  private int normalDelta;

  void initPainter() {
    // log.debug("initPainter");
    Rectangle drawArea = new Rectangle(0, 0, canvas.getWidth(), canvas.getHeight());
    BoardPainter boardPainter = new BoardPainter(board, drawArea, playArea, true);
    canvas.setBoardPainter(boardPainter);
    tuneBoardPainter();
    normalDelta = boardPainter.getDelta();
  }

  void switchToNormalPainter() {
    //#ifdef DEBUG
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
    Rectangle area = canvas.getBoardPainter().getPlayArea();
    byte x = cursor.x;
    byte y = cursor.y;
    switch (canvas.getGameAction(keyCode)) {
    case MainCanvas.ACTION_UP:

      if (y > 0) {
        y--;
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
        if (x > area.x1) {
          area.x1 = x;
          area.x0++;
          refreshPainter = true;
        }
      }
      break;
    default:
    }
    if (refreshPainter) {
      tuneBoardPainter();
      canvas.getBoardPainter().setPlayArea(area); // Otherwise it will not be notified of the change
    }

    boolean refreshNeeded = (x != cursor.x) || (y != cursor.y);
    if (refreshNeeded) {
      areaToRefresh.x0 = canvas.getBoardPainter().getCellX(Math.min(x, cursor.x) - 1);
      areaToRefresh.x1 = canvas.getBoardPainter().getCellX(Math.max(x, cursor.x) + 1);
      areaToRefresh.y0 = canvas.getBoardPainter().getCellY(Math.min(y, cursor.y) - 1);
      areaToRefresh.y1 = canvas.getBoardPainter().getCellY(Math.max(y, cursor.y) + 1);
      //#ifdef DEBUG
      //log.debug("Area to refresh = (" + areaToRefresh.x0 + "," + areaToRefresh.y0 + ")-(" + areaToRefresh.x1 + "," + areaToRefresh.y1 + ")");
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
      if (!doGoBack())
        canvas.setSplashInfo(Gome.singleton.bundle.getString("ui.noMoreMove"));
      break;
    case MainCanvas.ACTION_RIGHT:
      if (!doGoNext())
        canvas.setSplashInfo(Gome.singleton.bundle.getString("ui.noMoreMove"));
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
    Rectangle displayArea = canvas.getBoardPainter().getPlayArea();
    if (bZoomIn && (cursor.x > displayArea.x1 || cursor.x < displayArea.x0 || cursor.y < displayArea.y0 || cursor.y > displayArea.y1)) {

      switchToZoomedPainter();
    }

    return refreshNeeded ? canvas.getBoardPainter().getDrawArea() : null;
  }

  /**
   * 
   * @param keyCode
   * @return a graphic rectangle to refresh or null if nothing need to be refreshed
   */
  public Rectangle doReviewAction(int keyCode) {
    if (keyCode == canvas.KEY_10PREVMOVES) {
      for (int i = 0; i < 10; i++)
        doGoBack();
      tuneBoardPainter();
      return canvas.getBoardPainter().getDrawArea();
    } else if (keyCode == canvas.KEY_10NEXTMOVES) {
      for (int i = 0; i < 10; i++)
        doGoNext();
      tuneBoardPainter();
      return canvas.getBoardPainter().getDrawArea();
    }
    return null;
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
        //#ifdef IGS
        if (playMode == ONLINE_MODE) {
          try {
            if (!(board.isValidMove(cursor, Board.BLACK) | board.isValidMove(cursor, Board.WHITE)) && !board.hasBeenRemove(cursor.x, cursor.y)) {
              igs.removeDeadStone(cursor.x, cursor.y);
            }
          } catch (Exception e) {
            e.printStackTrace();
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
    if (playMode != ONLINE_MODE && playMode != OBSERVE_MODE) {
      doGoBack();
      if (playMode == PROBLEM_MODE && model.getFirstPlayer() == currentNode.getPlayerColor()) {
        doGoBack();
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
    byte color = getCurrentPlayerColor();

    switch (playMode) {
    //#ifdef IGS
    case ONLINE_MODE:
      // warnPassed(color);
      if (color == onlineColor) {
        try {
          if (!countMode)
            igs.playMove(new ServerMove(moveNb, color, Point.PASS, Point.PASS));

        } catch (Exception e) {
          Util.messageBox(Gome.singleton.bundle.getString("ui.failure"), e.getMessage(), AlertType.ERROR);
        }
      }

      break;
    //#endif
    case GAME_MODE:
      // warnPassed(color);
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
    //#ifdef IGS
    case ONLINE_MODE:
      if (!gameHasEnded) {
        if (color == onlineColor && board.isValidMove(cursor, color)) {
          // next = playNewMove(color, cursor.getX(), cursor.getY());
          try {
            igs.playMove(new ServerMove(moveNb, color, cursor.x, cursor.y));
          } catch (Exception e) {
            Util.messageBox(Gome.singleton.bundle.getString("ui.failure"), e.getMessage(), AlertType.ERROR);
          }
        } else {
          if (!board.isValidMove(cursor, color))
            canvas.setSplashInfo(Gome.singleton.bundle.getString("ui.notValidMove"));
          else
            canvas.setSplashInfo(Gome.singleton.bundle.getString("online.notYourTurn"));
        }
      } else
        canvas.setSplashInfo(Gome.singleton.bundle.getString("ui.gameHadEnded"));
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
        canvas.setSplashInfo(Gome.singleton.bundle.getString("online.noStartedGame"));
      } else
        canvas.setSplashInfo(Gome.singleton.bundle.getString("ui.gameHadEnded"));
      break;
    case PROBLEM_MODE:
      if (next != null) {
        playNode(next);
        if (next.getSon() != null && playMode == PROBLEM_MODE) {
          playNode(next.getSon());
        }
      } else {
        canvas.setSplashInfo(Gome.singleton.bundle.getString("ui.wrongMove"));
      }
      if (currentNode.getSon() == null) // no more move
      {
        if (model.isCorrectNode(currentNode)) {
          canvas.setSplashInfo(Gome.singleton.bundle.getString("ui.rightMove"));
        } else {
          canvas.setSplashInfo(Gome.singleton.bundle.getString("ui.wrongMove"));
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
        canvas.setSplashInfo(Gome.singleton.bundle.getString("online.movePlayed"));
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

    if (playMode == ONLINE_MODE) {
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

    if (bShowHint) {
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

  public boolean doGoBack() {
    if (countMode && !gameHasEnded) {
      resumeFromCounting();
      return true;
    }
    SgfNode prev = currentNode.searchFather();
    if (prev != null) {
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
      canvas.setSplashInfo((color == 1 ? Gome.singleton.bundle.getString("game.blackLong") : Gome.singleton.bundle.getString("game.whiteLong")) + " " + Gome.singleton.bundle.getString("game.passed"));
  }

  public void doGoPrevBrother() {
    SgfNode current = currentNode;
    if (!doGoBack()) // if there is no parent ignore
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
    if (!doGoBack()) // if there is no parent ignore
      return;
    Vector children = currentNode.getChildren();
    int index = children.indexOf(current) + 1;
    if (index == children.size())
      index = 0; // roll around
    SgfNode nextOne = (SgfNode) children.elementAt(index);
    playNode(nextOne);
  }

  public void reverseShowHint() {
    bShowHint = !bShowHint;
  }

  private void switchCurrentNode(SgfNode node) {
    currentNode = node;
    String comment = currentNode.getComment();
    if ((playMode == PROBLEM_MODE) && currentNode.getFather() != null && currentNode.getFather().getComment() != null) {
      canvas.setCurrentComment( currentNode.getFather().getComment() + "\n--\n" + comment);

    } else {
      if (comment != null)
        canvas.setCurrentComment(comment);
      else
        canvas.setCurrentComment(null);
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
    this.bZoomIn = bZoomIn;
    refreshPainter();

  }

  /**
   * @param bZoomIn
   */
  public void refreshPainter() {
    if (bZoomIn) {
      switchToZoomedPainter();
    } else {
      switchToNormalPainter();
    }
  }

  /**
   * @return Returns the bZoomIn.
   */
  boolean isZoomIn() {
    return bZoomIn;
  }

  /**
   * @return Returns the currentNode.
   */
  public SgfNode getCurrentNode() {
    return currentNode;
  }

  public void downloadFailure(Exception reason) {
    Util.messageBox(Gome.singleton.bundle.getString("ui.failure"), Gome.singleton.bundle.getString(reason.getMessage()), AlertType.ERROR); //$NON-NLS-1$
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
    SgfModel game = SgfModel.createNewModel(size, handi, Gome.singleton.bundle.getString("game.whiteLong"), Gome.singleton.bundle.getString("game.blackLong")); //$NON-NLS-1$ //$NON-NLS-2$

    if (playMode == GAME_MODE) {
      stopClockAndStopPainingClock();

      clock = new ClockController(false, this);

      if (handi > 0)
        clock.clockSwitcher(Board.BLACK);
      else
        clock.clockSwitcher(Board.EMPTY);
    }

    reset(Gome.singleton.mainCanvas);
    downloadFinished(game, gameMode);
  }

  //#ifdef IGS
  public void connectToServer() {
    try {
      if (!tryingToConnect) {
        tryingToConnect = true;
        // log.debug("Connect to IGS");

        igs = new ServerConnector(Gome.singleton.options.igsLogin, Gome.singleton.options.igsPassword, this);
        // log.debug(" - start thread");
        canvas.setSplashInfo(Gome.singleton.bundle.getString("online.connecting"));
        igs.start();
      }
    } catch (Exception e) {
      tryingToConnect = false;
      canvas.setSplashInfo(Gome.singleton.bundle.getString("online.connectionError"));
      Util.messageBox(Gome.singleton.bundle.getString("ui.failure"), e.getMessage(), AlertType.ERROR);
    }
  }

  //#endif

  //#ifdef IGS
  public void getServerGameList() {
    canvas.setSplashInfo(Gome.singleton.bundle.getString("online.gettingGameList"));
    try {
      igs.getGames();
    } catch (IOException e) {
      Util.messageBox(Gome.singleton.bundle.getString("ui.failure"), e.getMessage(), AlertType.ERROR);
    }
  }

  //#endif
  //#ifdef IGS
  public void getServerUserList() {
    canvas.setSplashInfo(Gome.singleton.bundle.getString("online.getUserList"));
    try {
      igs.getUsers();
    } catch (IOException e) {
      Util.messageBox(Gome.singleton.bundle.getString("ui.failure"), e.getMessage(), AlertType.ERROR);
    }
  }

  //#endif
  //#ifdef IGS
  public void observeServerGame(int selectedIndex) {
    canvas.setSplashInfo(Gome.singleton.bundle.getString("online.connecting"));
    try {
      currentIgsGame = igs.getGameList()[selectedIndex];
      igs.observe(currentIgsGame.nb);

      newGame(currentIgsGame.size, currentIgsGame.handi, OBSERVE_MODE);

      clock = new ClockController(true, this);

      canvas.assignClockController(clock);
      canvas.updateClockAndCommentMode(MainCanvas.CLOCK_MODE);
    } catch (Exception e) {
      Util.messageBox(Gome.singleton.bundle.getString("ui.failure"), e.getMessage(), AlertType.ERROR);
    }
  }

  //#endif
  //#ifdef IGS
  public void acceptChallenge(ServerChallenge challenge) {
    canvas.setSplashInfo(Gome.singleton.bundle.getString("online.acceptChallenge"));
    try {
      igs.challenge(challenge);
    } catch (Exception e) {
      Util.messageBox(Gome.singleton.bundle.getString("ui.failure"), e.getMessage(), AlertType.ERROR);
    }
  }

  //#endif
  //#ifdef IGS
  public void declineChallenge(ServerChallenge currentChallenge) {
    try {
      igs.decline(currentChallenge.nick);
    } catch (Exception e) {
      Util.messageBox(Gome.singleton.bundle.getString("ui.failure"), e.getMessage(), AlertType.ERROR);
    }

  }

  //#endif
  //#ifdef IGS
  public void challengeServerUser(int selectedIndex) {
    canvas.setSplashInfo(Gome.singleton.bundle.getString("online.sendChallenge"));
    try {
      // log.debug("challenge " + igs.getUserList()[selectedIndex]);
      ServerChallenge challenge = new ServerChallenge();
      challenge.nick = igs.getUserList()[selectedIndex].nick;
      // TODO: remove defaults
      challenge.color = Board.BLACK;
      challenge.time_minutes = 25;
      challenge.min_per25moves = 10;
      challenge.size = 19;
      igs.challenge(challenge);
    } catch (Exception e) {
      Util.messageBox(Gome.singleton.bundle.getString("ui.failure"), e.getMessage(), AlertType.ERROR);
    }
  }

  //#endif
  //#ifdef IGS
  public void loggedEvent() {
    tryingToConnect = false;
    // log.debug("GC: IGS Login Successful");
    stopClockAndStopPainingClock();
    Gome.singleton.mainCanvas.switchToIGSOnlineMenu();
    tuneBoardPainter();
    canvas.setSplashInfo(Gome.singleton.bundle.getString("online.connectedToIgs"));
    Gome.singleton.menuEngine.switchToOnline();
    connectToIgs = true;
    // set splash will repaint
  }

  //#endif
  //#ifdef IGS
  public void gameListEvent(ServerGame[] games) {
    Gome.singleton.menuEngine.showIgsGameList(games);
  }

  //#endif
  //#ifdef IGS
  public void observeEvent(ServerMove[] moves) {
    try {

      ServerMove move = null;
      for (int i = 0; i < moves.length; i++) {
        move = moves[i];
        playNewMove(move.color, move.x, move.y);
      }
      tuneBoardPainter();
      Gome.singleton.mainCanvas.setSplashInfo(null);
      canvas.refresh(canvas.getBoardPainter().getDrawArea());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  //#endif
  //#ifdef IGS
  public void moveEvent(ServerMove move) {
    // log.debug("Move received from server");
    Gome.singleton.mainCanvas.setSplashInfo(null);
    if (move.x == Point.PASS) {
      pass();
      // return;
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
  //#ifdef IGS
  public void userListEvent() {
    Gome.singleton.mainCanvas.setSplashInfo(null);
    ServerUser[] userList = igs.getUserList();
    QuickSortable.quicksort(userList); //inplace sort, external criteria
    Gome.singleton.menuEngine.showIgsUserList(userList);
  }

  //#endif
  //#ifdef IGS
  public void challenge(ServerChallenge challenge) {
    // log.debug("Received Challenge from user " + challenge.nick);
    Gome.singleton.menuEngine.showIgsChallenge(challenge);
  }

  //#endif
  //#ifdef IGS
  public void message(byte type, String nick, String message) {
    switch (type) {
    case ServerCallback.MESSAGE_CHAT_TYPE:
      Gome.singleton.menuEngine.incomingChatMessage(nick, message);
      break;

    case ServerCallback.MESSAGE_ERROR_TYPE:
      if (tryingToConnect) {
        tryingToConnect = false;
      }
      connectToIgs = false;
      canvas.setSplashInfo(Gome.singleton.bundle.getString(message));
      stopClockAndStopPainingClock();
      setPlayMode(REVIEW_MODE);
      break;

    case ServerCallback.MESSAGE_KIBITZING_TYPE:
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
  //#ifdef IGS
  public void startGame(ServerChallenge challenge) {
    canvas.setSplashInfo(Gome.singleton.bundle.getString("online.onlineGameStarted"));
    // log.debug("Start online game");

    // log.debug("Your color " + challenge.color);
    onlineColor = challenge.color;
    int time = challenge.time_minutes * 60;
    int byoStone = 25;
    int byoTime = challenge.min_per25moves * 60;

    clock = new ClockController(time, byoTime, byoStone, this);

    canvas.assignClockController(clock);
    canvas.updateClockAndCommentMode(MainCanvas.CLOCK_MODE);

    newGame(challenge.size, 0, ONLINE_MODE); // we don't know yet the
    // handi
    if (playMode == ONLINE_MODE) {
      if (onlineColor != Board.BLACK) {
        Gome.singleton.mainCanvas.removeOnlineSetHandicapMenuItem();
      }
    }

  }

  //#endif
  //#ifdef IGS
  public void disconnectFromServer() {
    connectToIgs = false;
    // log.debug("disconnect from IGS");
    igs.disconnect();
    canvas.setSplashInfo(Gome.singleton.bundle.getString("online.disconnected"));
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

  //#ifdef IGS
  public String getNick(int userIndex) {
    return igs.getUserList()[userIndex].nick;
  }

  public void sendOnlineMessage(String nickToSend, String message) {
    try {
      igs.sendMessage(nickToSend, message);
    } catch (IOException e) {
      Util.messageBox(Gome.singleton.bundle.getString("ui.failure"), e.getMessage(), AlertType.ERROR);
    }
  }

  //#endif
  public void doScore() {
    if (playMode == GAME_MODE || playMode == REVIEW_MODE) {
      if (!evaluationMode) {
        gameHasEnded = true;
        stopClockAndStopPainingClock();
      }
      countMode = false;
      int[] scores = board.getScore();
      int white = scores[0];
      int black = scores[1];
      byte komi = model.getByteKomi();
      white += komi / 2;
      boolean dotfivedKomi = (komi % 2) != 0;
      int diff = white - black;
      StringBuffer scoreSentence = new StringBuffer(Gome.singleton.bundle.getString("game.score"));
      if (diff > 0 || (diff == 0 && dotfivedKomi)) {
        scoreSentence.append(Gome.singleton.bundle.getString("game.whiteWin"));
        scoreSentence.append(diff);
      } else {
        if (diff < 0) {
          diff = -diff;
          scoreSentence.append(Gome.singleton.bundle.getString("game.blackWin"));
          if (dotfivedKomi && komi > 0) {
            scoreSentence.append(diff - 1);
          } else {
            scoreSentence.append(diff);
          }
        } else if (diff == 0 && !dotfivedKomi)
          scoreSentence.append(Gome.singleton.bundle.getString("game.jigo"));
      }
      if (dotfivedKomi) {
        scoreSentence.append(".5");
      }
      setPlayMode(playMode);
      canvas.setSplashInfo(scoreSentence.toString());
    }
    //#ifdef IGS
    else if (playMode == ONLINE_MODE) {
      askIgsForScore();
    }
    //#endif
  }

  //#ifdef IGS
  public void askIgsForScore() {
    try {
      igs.getScore();
      canvas.setSplashInfo(Gome.singleton.bundle.getString("online.gettingScore"));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void doneWithScore() {
    try {
      igs.doneWithTheCounting();
      // log.info("DONE WITH Scoring");
      canvas.setSplashInfo(Gome.singleton.bundle.getString("count.doneWithScoring"));
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  //#endif

  public void startCountMode(boolean evaluationMode) {
    clock.pauseClock();
    canvas.stopClockPainting();
    countMode = true;
    this.evaluationMode = evaluationMode;
    board.startCounting(evaluationMode, (playMode != ONLINE_MODE) && !evaluationMode);
    Gome.singleton.mainCanvas.updateClockAndCommentMode(MainCanvas.NOTHING_TO_DISPLAY_MODE);
    canvas.setSplashInfo(Gome.singleton.bundle.getString("count.markDeadStone"));
    //#ifdef IGS
    if (playMode == ONLINE_MODE)
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
    return bShowHint;
  }

  //#ifdef IGS
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
        canvas.setSplashInfo(Gome.singleton.bundle.getString("clock.blackTimeUp"));
        break;

      case Board.WHITE:
        canvas.setSplashInfo(Gome.singleton.bundle.getString("clock.whiteTimeUp"));
        break;

      }
      setPlayMode(REVIEW_MODE);
    }
  }

  public void timesUP(String name) {
    canvas.setSplashInfo(name + Gome.singleton.bundle.getString("clock.timesUp"));
    currentNode.setComment(name + Gome.singleton.bundle.getString("clock.timesUp"));
    gameHasEnded = true;
    //#ifdef IGS
    canvas.switchToIGSOnlineMenu();
    //#endif
    playMode = REVIEW_MODE;
  }

  public void endGame() {
    startCountMode(false);
    canvas.setSplashInfo(Gome.singleton.bundle.getString("count.endGameMarkStone"));
    // setPlayMode(ONLINE_MODE);
  }

  public void restoreGameForCounting() {
    startCountMode(false);
    gameHasEnded = false;
    //#ifdef IGS
    canvas.removeOnlineSetKomiAndHandicapMenuItem();
    //#endif
    canvas.setSplashInfo(Gome.singleton.bundle.getString("count.restoreCounting"));
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

  //#ifdef IGS
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
      out = name1 + " (" + v1 + ") " + name2 + " (" + v2 + ") " + name1 + Gome.singleton.bundle.getString("game.winBy") + v3;
    }
    if (value1 < value2) {
      String v3 = String.valueOf(value2 - value1);
      v3 = v3.substring(0, v3.length() - 1) + "." + v3.substring(v3.length() - 1);

      out = name2 + " (" + v2 + ") " + name1 + " (" + v1 + ") " + name2 + Gome.singleton.bundle.getString("game.winBy") + v3;
    }

    currentNode.setComment(out);

    canvas.setSplashInfo(out);
    canvas.refresh(canvas.getBoardPainter().getDrawArea());

    playMode = GAME_MODE;
    Gome.singleton.mainCanvas.switchToIGSOnlineMenu();
  }

  //#endif
  //#ifdef IGS
  public void oppSetHandicap(byte handicap) {
    if (model != null && playMode == ONLINE_MODE) {
      // log.debug("Setting handicap: " + handicap);
      newGame(model.getBoardSize(), handicap, playMode);

      Gome.singleton.mainCanvas.removeOnlineSetHandicapMenuItem();

      clock.clockSwitcher(Board.BLACK);
      canvas.refresh(canvas.getBoardPainter().getDrawArea());
    }
  }

  //#endif

  //#ifdef IGS
  public void oppWantToSetNewKomi(byte k) {
    if (model != null && playMode == ONLINE_MODE) {
      if (k != gomeWantOnlineKomi) {
        Gome.singleton.menuEngine.showOppWantKomi(k);
        gomeWantOnlineKomi = k;
      } else {
        setKomi(k);
        canvas.setSplashInfo(Gome.singleton.bundle.getString("online.opponentAgreedNewKomi"));
      }
    }
  }

  //#endif

  //#ifdef IGS
  public void setKomi(byte k) {
    if (model != null && playMode == ONLINE_MODE) {
      String temp = Util.komi2String(k);
      canvas.setSplashInfo(Gome.singleton.bundle.getString("online.newKomi") + temp);
      model.setNewKomi(k);
    }
  }

  public void onlineSetKomi(byte k) {
    if (model != null && playMode == ONLINE_MODE) {
      try {
        igs.setKomi(k);
      } catch (IOException e) {
        Util.messageBox(Gome.singleton.bundle.getString("ui.failure"), e.getMessage(), AlertType.ERROR);
      }
    }
  }

  public void onlineResigned(String name) {
    String color = Gome.singleton.bundle.getString("game.whiteLong");

    if (currentNode.getPlayerColor() == Board.BLACK)
      color = Gome.singleton.bundle.getString("game.whiteLong");
    else
      color = Gome.singleton.bundle.getString("game.blackLong");
    String message = name + " (" + color + ") " + Gome.singleton.bundle.getString("ui.resigned");
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

      String message = Gome.singleton.bundle.getString("ui.whiteResigned");
      if (getCurrentPlayerColor() == Board.BLACK) {
        message = Gome.singleton.bundle.getString("ui.blackResigned");
      }
      currentNode.setComment(message);

      stopClockAndStopPainingClock();
      // log.info(message);
      canvas.setSplashInfo(message);

      setPlayMode(REVIEW_MODE);
      gameHasEnded = true;
    }
    //#ifdef IGS
    else if (playMode == ONLINE_MODE) {
      if (getCurrentPlayerColor() == onlineColor || countMode == true) {
        gameHasEnded = true;
        try {
          igs.resign();
        } catch (IOException e) {
          Util.messageBox(Gome.singleton.bundle.getString("ui.failure"), e.getMessage(), AlertType.ERROR);
        }
      } else {
        canvas.setSplashInfo(Gome.singleton.bundle.getString("online.notYourTurn"));
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

  //#ifdef IGS
  public void gomeWantToSetOnlineKomi(byte komi, int mode) {
    if (playMode == ONLINE_MODE) {

      String komiString = Util.komi2String(komi);
      if (mode == 0) {
        canvas.setSplashInfo(Gome.singleton.bundle.getString("online.requestingBlackKomi") + komiString);
        onlineSetKomi(komi);
        gomeWantOnlineKomi = komi;
      } else {
        canvas.setSplashInfo(Gome.singleton.bundle.getString("online.requestingWhiteKomi") + komiString);
        onlineSetKomi((byte) -komi);
        gomeWantOnlineKomi = (byte) -komi;
      }

    }
  }

  public void gomeSetOnlineHandicap(byte h) {
    if (playMode == ONLINE_MODE) {
      if (h > 1) {
        canvas.setSplashInfo(Gome.singleton.bundle.getString("ui.settingHandicapTo") + h);
        try {
          igs.setHandicap(h);
        } catch (IOException e) {
          Util.messageBox(Gome.singleton.bundle.getString("ui.failure"), e.getMessage(), AlertType.ERROR);
        }
      }
    }
  }

  public void gomeRestoreGameForCounting() {
    if (model != null) {
      try {
        igs.resetDeadStone();
      } catch (IOException e) {
        Util.messageBox(Gome.singleton.bundle.getString("ui.failure"), e.getMessage(), AlertType.ERROR);
      }
    }
  }

  public void onlineScore(int whiteScore, int blackScore) {
    String black = Gome.singleton.bundle.getString("game.blackLong");
    String white = Gome.singleton.bundle.getString("game.whiteLong");

    String whiteScoreString = String.valueOf(whiteScore);
    whiteScoreString = whiteScoreString.substring(0, whiteScoreString.length() - 1) + "." + whiteScoreString.substring(whiteScoreString.length() - 1);

    String blackScoreString = String.valueOf(blackScore);
    blackScoreString = blackScoreString.substring(0, blackScoreString.length() - 1) + "." + blackScoreString.substring(blackScoreString.length() - 1);

    canvas.setSplashInfo(Gome.singleton.bundle.getString("game.score") + black + ":" + blackScoreString + " " + white + ": " + whiteScoreString);
  }

  //#endif

  public void winByValue(String name, int winByValue) {
    String v1 = String.valueOf(winByValue);
    v1 = v1.substring(0, v1.length() - 1) + "." + v1.substring(v1.length() - 1);

    canvas.setSplashInfo(name + Gome.singleton.bundle.getString("game.winBy") + v1);
  }

  public Board getBoard() {
    return board;
  }

}