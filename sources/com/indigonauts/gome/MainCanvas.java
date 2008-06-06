/*
 * (c) 2006 Indigonauts
 */

package com.indigonauts.gome;

import java.util.Timer;
import java.util.Vector;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;

import com.indigonauts.gome.common.Rectangle;
import com.indigonauts.gome.common.Util;
import com.indigonauts.gome.i18n.I18N;
import com.indigonauts.gome.sgf.SgfNode;
import com.indigonauts.gome.ui.BoardPainter;
import com.indigonauts.gome.ui.ClockPainterTask;
import com.indigonauts.gome.ui.GameController;
import com.indigonauts.gome.ui.MenuEngine;
import com.indigonauts.gome.ui.Showable;

public class MainCanvas extends Canvas implements CommandListener, Showable {
  //#ifdef DEBUG
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("MainCanvas");
  //#endif
  public static final Font SMALL_FONT = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL);

  private GameController gc;

  public static final int ACTION_UNDO = Canvas.GAME_A;
  public static final int ACTION_HINT = Canvas.GAME_B;
  public static final int ACTION_COMMENT = Canvas.GAME_C;
  public static final int ACTION_ZOOM = Canvas.GAME_D;
  public static final int ACTION_UP = Canvas.UP;
  public static final int ACTION_DOWN = Canvas.DOWN;
  public static final int ACTION_LEFT = Canvas.LEFT;
  public static final int ACTION_RIGHT = Canvas.RIGHT;
  public static final int ACTION_FIRE = Canvas.FIRE;

  public int KEY_SCROLLUP;
  public int KEY_SCROLLDOWN;
  public int KEY_10NEXTMOVES;
  public int KEY_10PREVMOVES;
  public int KEY_NEXTCORNER;
  public int KEY_PREVCORNER;

  public Scroller scroller;
  private BoardPainter boardPainter;
  private String currentComment;
  private String splashInfo;
  private ClockController clockControl;
  private ClockPainterTask clockPainter;
  public int clockAndCommentMode = NOTHING_TO_DISPLAY_MODE;

  public static final byte NOTHING_TO_DISPLAY_MODE = 0;
  public static final byte COMMENT_MODE = 1;
  public static final byte CLOCK_MODE = 2;

  private Rectangle fullCanvas;

  private void assignUnassignedKey() {
    Vector keys = new Vector();
    keys.addElement(new Integer(KEY_POUND));
    keys.addElement(new Integer(KEY_STAR));
    keys.addElement(new Integer(KEY_NUM0));
    keys.addElement(new Integer(KEY_NUM1));
    keys.addElement(new Integer(KEY_NUM2));
    keys.addElement(new Integer(KEY_NUM3));
    keys.addElement(new Integer(KEY_NUM4));
    keys.addElement(new Integer(KEY_NUM5));
    keys.addElement(new Integer(KEY_NUM6));
    keys.addElement(new Integer(KEY_NUM7));
    keys.addElement(new Integer(KEY_NUM8));
    keys.addElement(new Integer(KEY_NUM9));
    int[] allActions = { Canvas.GAME_A, Canvas.GAME_B, Canvas.GAME_C, Canvas.GAME_D, Canvas.UP, Canvas.DOWN, Canvas.LEFT, Canvas.RIGHT, Canvas.FIRE };
    for (int j = 0; j < allActions.length; j++) {
      keys.removeElement(new Integer(getKeyCode(allActions[j])));
    }

    int size = keys.size();
    if (size > 0) {
      KEY_SCROLLUP = ((Integer) keys.elementAt(0)).intValue();
      if (size > 1) {
        KEY_SCROLLDOWN = ((Integer) keys.elementAt(1)).intValue();
        if (size > 2) {
          KEY_NEXTCORNER = KEY_10NEXTMOVES = ((Integer) keys.elementAt(2)).intValue();
          if (size > 3) {
            KEY_PREVCORNER = KEY_10PREVMOVES = ((Integer) keys.elementAt(3)).intValue();
          }
        }
      }
    }

  }

  public MainCanvas(GameController gc) {
    super();
    this.gc = gc;
    assignUnassignedKey();
    addCommand(MenuEngine.NEW);
    addCommand(MenuEngine.FILES);
    addCommand(MenuEngine.SAVE);
    addCommand(MenuEngine.GAME_STATUS);

    //#ifdef IGS
    switchToIGSOfflineMenu();
    //#endif

    //#ifdef BT
    switchToBTOfflineMenu();
    //#endif

    addCommand(MenuEngine.OPTIONS);
    //#ifdef DEBUG
    addCommand(MenuEngine.CONSOLE);
    //#endif
    addCommand(MenuEngine.HELP);
    addCommand(MenuEngine.EXIT);
    setCommandListener(this);
    clockPainter = new ClockPainterTask(this);
    scroller = new Scroller(this);
    clockAndCommentMode = NOTHING_TO_DISPLAY_MODE;
    setFullScreenMode(true);
    //#ifdef DEBUG
    log.debug("Main Canvas constructed");
    //#endif

  }

  public Rectangle getFullCanvas() {
    if (fullCanvas == null)
      fullCanvas = new Rectangle(0, 0, getWidth(), getHeight());
    return fullCanvas;
  }

  public void assignClockController(ClockController c) {
    clockControl = c;
  }

  private void scroll(int keyCode) {
    if (keyCode == KEY_SCROLLUP && scroller != null) {
      scroller.bigStepUp();
    } else if (keyCode == KEY_SCROLLDOWN && scroller != null) {
      scroller.bigStepDown();
    }
  }

  protected void keyRepeated(int keyCode) {
    scroll(keyCode);
    if (gc.getPlayMode() != GameController.REVIEW_MODE && gc.getPlayMode() != GameController.JOSEKI_MODE) {
      switch (getGameAction(keyCode)) {
      case ACTION_UP:
      case ACTION_DOWN:
      case ACTION_LEFT:
      case ACTION_RIGHT:
        keyPressed(keyCode);
        break;
      }
    }
  }

  protected void keyPressed(int keyCode) {
    //#ifdef DEBUG
    long actionTime = System.currentTimeMillis();
    //#endif

    scroll(keyCode);
    Rectangle refreshNeeded = null;

    if (splashInfo != null) {
      splashInfo = null;
      refreshNeeded = getBoardPainter().getDrawArea();

    }

    char playMode = gc.getPlayMode();
    boolean review = playMode == GameController.REVIEW_MODE || playMode == GameController.JOSEKI_MODE || playMode == GameController.OBSERVE_MODE;

    if (!review && keyCode == KEY_NEXTCORNER) {
      gc.nextCorner();
      scroller.setCoordinates(gc.getCursor());
    } else if (!review && keyCode == KEY_PREVCORNER) {
      gc.prevCorner();
      scroller.setCoordinates(gc.getCursor());
    } else
      switch (getGameAction(keyCode)) {
      case ACTION_UP:
      case ACTION_DOWN:
      case ACTION_LEFT:
      case ACTION_RIGHT:

        if (gc.isCountMode()) {
          refreshNeeded = Rectangle.union(refreshNeeded, gc.doMoveCursor(keyCode));
        } else {
          if (review)
            refreshNeeded = Rectangle.union(refreshNeeded, gc.doReviewMove(keyCode));
          else
            refreshNeeded = Rectangle.union(refreshNeeded, gc.doMoveCursor(keyCode));
        }
        scroller.setCoordinates(gc.getCursor());
        break;

      default:
        if (review) {
          refreshNeeded = Rectangle.union(refreshNeeded, Rectangle.union(gc.doAction(keyCode), gc.doReviewAction(keyCode)));
        } else {
          refreshNeeded = Rectangle.union(refreshNeeded, gc.doAction(keyCode));
        }
      }
    if (refreshNeeded != null) {
      refresh(refreshNeeded);
    }
    //#ifdef DEBUG
    log.debug("->" + (System.currentTimeMillis() - actionTime));
    //#endif

  }

  private void clearOptionalItems() {
    removeCommand(MenuEngine.REVIEW_MODE);
    removeCommand(MenuEngine.PLAY_MODE);
    removeCommand(MenuEngine.PASS);
    removeCommand(MenuEngine.LAST_MOVE);
    removeCommand(MenuEngine.FIRST_MOVE);
    removeCommand(MenuEngine.FINISHED_COUNTING);
    removeCommand(MenuEngine.EVALUATE);
    removeCommand(MenuEngine.RESIGN);
    removeCommand(MenuEngine.EDIT_NODE);
    //#ifdef IGS
    removeCommand(MenuEngine.IGS_MESSAGE);
    removeCommand(MenuEngine.IGS_GAMELIST);
    removeCommand(MenuEngine.IGS_USERLIST);
    removeCommand(MenuEngine.IGS_DISCONNECT);
    removeCommand(MenuEngine.IGS_CONNECT);
    removeCommand(MenuEngine.IGS_RESET_DEAD_STONES);
    removeCommand(MenuEngine.IGS_DONE_SCORE);
    removeOnlineSetKomiAndHandicapMenuItem();
    //#endif

    removeCommand(MenuEngine.COMMENT);
    removeCommand(MenuEngine.ZOOM);
    removeCommand(MenuEngine.UNDO);
    removeCommand(MenuEngine.HINT);
    removeCommand(MenuEngine.NEXT10MOVES);
    removeCommand(MenuEngine.PREV10MOVES);
  }

  public void switchToPlayMenu() {
    clearOptionalItems();
    addCommand(MenuEngine.PASS);
    addCommand(MenuEngine.RESIGN);
    addCommand(MenuEngine.ZOOM);
    addCommand(MenuEngine.UNDO);
    addCommand(MenuEngine.EVALUATE);
    addCommand(MenuEngine.EDIT_NODE);
    //#ifdef IGS
    if (!Gome.singleton.options.igsLogin.equals("") && !Gome.singleton.options.igsPassword.equals("")) {

      addCommand(MenuEngine.IGS_CONNECT);
    }
    //#endif

    addCommand(MenuEngine.REVIEW_MODE);

  }

  //#ifdef IGS
  public void switchToOnlinePlayMenu() {
    clearOptionalItems();
    addCommand(MenuEngine.PASS);
    addCommand(MenuEngine.RESIGN);
    removeCommand(MenuEngine.IGS_CONNECT);
    addCommand(MenuEngine.IGS_REQUEST_KOMI);
    addCommand(MenuEngine.IGS_CHANGE_HANDICAP);
    addCommand(MenuEngine.IGS_MESSAGE);
    addCommand(MenuEngine.ZOOM);
  }

  //#endif
  //#ifdef IGS
  public void removeOnlineSetKomiAndHandicapMenuItem() {
    removeCommand(MenuEngine.IGS_REQUEST_KOMI);
    removeCommand(MenuEngine.IGS_CHANGE_HANDICAP);
  }

  //#endif
  //#ifdef IGS
  public void removeOnlineSetHandicapMenuItem() {
    removeCommand(MenuEngine.IGS_CHANGE_HANDICAP);
  }

  //#endif
  public void switchToReviewMenu() {

    clearOptionalItems();
    addCommand(MenuEngine.COMMENT);
    addCommand(MenuEngine.ZOOM);
    addCommand(MenuEngine.FIRST_MOVE);
    addCommand(MenuEngine.LAST_MOVE);
    addCommand(MenuEngine.NEXT10MOVES);
    addCommand(MenuEngine.PREV10MOVES);
    addCommand(MenuEngine.PLAY_MODE);
    addCommand(MenuEngine.EVALUATE);
    //#ifdef IGS
    if (!Gome.singleton.options.igsLogin.equals("") && !Gome.singleton.options.igsPassword.equals("")) {
      addCommand(MenuEngine.IGS_CONNECT);
    }
    //#endif

  }

  public void switchToJosekiMenu() {
    clearOptionalItems();
    addCommand(MenuEngine.FIRST_MOVE);
    addCommand(MenuEngine.HINT);
  }

  public void switchToProblemMenu() {
    clearOptionalItems();
    addCommand(MenuEngine.FIRST_MOVE);
    addCommand(MenuEngine.COMMENT);
    addCommand(MenuEngine.HINT);

  }

  //#ifdef IGS
  public void switchToObservePlayMenu() {
    clearOptionalItems();
    addCommand(MenuEngine.FIRST_MOVE);
    addCommand(MenuEngine.LAST_MOVE);
    addCommand(MenuEngine.NEXT10MOVES);
    addCommand(MenuEngine.PREV10MOVES);
    addCommand(MenuEngine.ZOOM);
    addCommand(MenuEngine.COMMENT);
    addCommand(MenuEngine.IGS_GAMELIST);
    addCommand(MenuEngine.IGS_USERLIST);
    addCommand(MenuEngine.IGS_DISCONNECT);
  }

  //#endif
  //#ifdef IGS
  public void switchToIGSOnlineMenu() {
    clearOptionalItems();
    removeCommand(MenuEngine.IGS_CONNECT);
    addCommand(MenuEngine.IGS_GAMELIST);
    addCommand(MenuEngine.IGS_USERLIST);
    addCommand(MenuEngine.IGS_DISCONNECT);
  }

  //#endif

  //#ifdef BT
  private void switchToBTOfflineMenu() {
    addCommand(MenuEngine.BT_CONNECT);
  }

  //#endif

  //#ifdef BT
  public void switchToBTOnlineMenu() {
    removeCommand(MenuEngine.BT_CONNECT);
    addCommand(MenuEngine.BT_CHALLENGE);
    addCommand(MenuEngine.BT_DISCONNECT);
  }

  //#endif

  //#ifdef IGS
  private void switchToIGSOfflineMenu() {
    if (!Gome.singleton.options.igsLogin.equals("") && !Gome.singleton.options.igsPassword.equals("")) {
      addCommand(MenuEngine.IGS_CONNECT);
    }
    removeCommand(MenuEngine.IGS_GAMELIST);
    removeCommand(MenuEngine.IGS_USERLIST);
    removeCommand(MenuEngine.IGS_DISCONNECT);
  }

  //#endif

  public void switchToCountingMenu() {
    removeCommand(MenuEngine.PASS);
    removeCommand(MenuEngine.EVALUATE);
    addCommand(MenuEngine.FINISHED_COUNTING);

  }

  //#ifdef IGS
  public void switchToOnlineCountingMenu() {
    removeCommand(MenuEngine.PASS);
    addCommand(MenuEngine.FINISHED_COUNTING);
    addCommand(MenuEngine.IGS_DONE_SCORE);
    addCommand(MenuEngine.IGS_RESET_DEAD_STONES);
  }

  //#endif

  public void show(Display dis) {
    if (gc.hasNextInCollection()) {
      this.addCommand(MenuEngine.NEXT);
    } else {
      this.removeCommand(MenuEngine.NEXT);
    }
    if (gc.hasPreviousInCollection()) {
      this.addCommand(MenuEngine.PREVIOUS);
    } else {
      this.removeCommand(MenuEngine.PREVIOUS);
    }

    if (!Gome.singleton.checkLicense())
      return;
    dis.setCurrent(this);
  }

  protected void pointerReleased(int x, int y) {
    gc.pointerReleased(x, y);
    super.pointerReleased(x, y);
  }

  public void commandAction(Command c, Displayable d) {
    if (c == MenuEngine.PLAY_MODE) {
      gc.setPlayMode(GameController.GAME_MODE);
      setSplashInfo(I18N.switchToPlayEditMode);
      if (clockControl != null) {
        clockControl.resumeClock();
      }
      updateClockAndCommentMode(NOTHING_TO_DISPLAY_MODE);
    } else if (c == MenuEngine.REVIEW_MODE) {
      gc.setPlayMode(GameController.REVIEW_MODE);

      setSplashInfo(I18N.switchToReviewMode);
      if (clockControl != null)
        clockControl.pauseClock();
      updateClockAndCommentMode(COMMENT_MODE);
    } else
      Gome.singleton.menuEngine.commandAction(c, d);
  }

  public void paint(Graphics g) {
    g.setColor(0xccccff);
    g.fillRect(0, 0, getWidth(), getHeight());

    if (scroller.isVisible() && scroller.getX() == g.getClipX() && scroller.getY() == g.getClipY() && scroller.getWidth() == g.getClipWidth() && scroller.getHeight() == g.getClipHeight()) {
      //#ifdef DEBUG
      log.debug("status bar only");
      //#endif
      drawStatusBar(g);
      return;
    }

    if (clockAndCommentMode == CLOCK_MODE && clockPainter.getX() == g.getClipX() && clockPainter.getY() == g.getClipY() && clockPainter.getWidth() == g.getClipWidth()
            && clockPainter.getHeight() == g.getClipHeight()) {
      drawStatusBar(g);
      return;
    }

    if (boardPainter != null) {
      char playMode = gc.getPlayMode();
      boolean passiveMode = (playMode == GameController.JOSEKI_MODE || playMode == GameController.OBSERVE_MODE || playMode == GameController.REVIEW_MODE) && !gc.isCountMode();
      boardPainter.drawMe(g, passiveMode ? null : gc.getCursor(), gc.getCurrentPlayerColor(), gc.getShowHints(), passiveMode, gc.getCurrentNode(), gc.getSgfModel());
    }

    if (splashInfo != null) {
      drawSplashInfo(g);
    }

    // draw Status bar clip, so put it at the end
    drawStatusBar(g);

  }

  private void drawSplashInfo(Graphics g) {

    Util.renderSplash(g, splashInfo, getWidth(), getHeight(), SMALL_FONT, Util.COLOR_BLACK, 0xDCFF84);
  }

  public void drawStatusBar(Graphics g) {
    switch (clockAndCommentMode) {
    case COMMENT_MODE:
      if (clockPainter != null) {
        clockPainter.cancel();
        // clockPainter = null;
      }

      scroller.drawMe(g);
      break;
    case CLOCK_MODE:
      clockPainter.drawClock(g, clockControl);
      if (currentComment != null)// there are some real comment, so show
        // the squiggles
        drawSquiggles(g);

      break;
    case NOTHING_TO_DISPLAY_MODE:
      if (clockPainter != null) {
        clockPainter.cancel();
      }
      if (currentComment != null)// there are some real comment, so show
        // the squiggles
        drawSquiggles(g);
      break;
    }

  }

  private void startScroller() {
    //#ifdef DEBUG
    log.debug("set scroller");
    //#endif
    synchronized (SCROLLER_SYNC) {
      Font scrollerFont = Gome.singleton.options.getScrollerFont();
      scroller.setSpeed(Gome.singleton.options.getScrollerSpeed());
      scroller.setBigStep(scrollerFont.getHeight() / 2);

      // log.debug("Scroller height = " + scroller.getHeight());
      //Image img = Util.renderOffScreenScrollableText(currentComment != null ? currentComment : "#" + gc.getMoveNb(), getWidth(), scroller.getHeight(), scrollerFont, Util.COLOR_BLACK, //$NON-NLS-1$ //$NON-NLS-2$
      //        Util.COLOR_LIGHT_BACKGROUND);
      //scroller.setImg(img);
      scroller.setMoveNb(gc.getMoveNb());
      scroller.setComment(currentComment);
      scroller.setCoordinates(gc.getCursor());
      SgfNode currentNode = gc.getCurrentNode();
      scroller.setBrothers(currentNode.getOlder() != null, currentNode.getYounger() != null);
      scroller.setVisible(true);
      scroller.repaint();
    }
  }

  public void stopScroller() {
    //#ifdef DEBUG
    log.debug("Pause scroller");
    //#endif
    synchronized (SCROLLER_SYNC) {
      scroller.setVisible(false);
      //scroller.setImg(null);
      scroller.setComment(null);
    }
  }

  /**
   * @param g
   */
  private void drawSquiggles(Graphics g) {
    int height = getHeight();
    int squiggleSize = height / 30;
    int nbSquiggles = getWidth() / squiggleSize + 1;
    boolean up = true;
    g.setColor(Util.COLOR_GREEN);
    for (int i = 0; i < nbSquiggles; i++) {
      int x = i * squiggleSize;
      int x0 = x + squiggleSize;
      if (up) {
        g.drawLine(x, height, x0, height - squiggleSize);
        g.drawLine(x + 1, height, x0 + 1, height - squiggleSize);
      } else {
        g.drawLine(x, height - squiggleSize, x0, height);
        g.drawLine(x + 1, height - squiggleSize, x0 + 1, height);
      }
      up = !up;

    }
  }

  private static final Object SCROLLER_SYNC = new Object();

  /**
   * @param currentComment
   *          The currentComment to set.
   */
  public void setCurrentComment(String currentComment) {
    synchronized (SCROLLER_SYNC) { // Others just lock
      // log.debug("Set current comment to " + currentComment);
      this.currentComment = currentComment;

      if (clockAndCommentMode == COMMENT_MODE) {
        startScroller();
      }
    }
  }

  public void setSplashInfo(String splash) {
    this.splashInfo = splash;
    //log.debug("repaint() called from setSplashInfo with " + splash);
    refresh(boardPainter.getDrawArea());
  }

  public void recalculateLayout() {
    //#ifdef DEBUG
    log.debug("recalcultate Layout");
    //#endif
    int minimalBottomHeight = 0;
    if (clockAndCommentMode == COMMENT_MODE) {
      minimalBottomHeight += Gome.singleton.options.getScrollerSize();
    }
    if (clockAndCommentMode == CLOCK_MODE) {
      if (clockPainter != null) {
        clockPainter.cancel();
        clockPainter = null;
      }

      if (clockPainter == null) {
        clockPainter = new ClockPainterTask(this);
      }

      if (clockControl != null)
        if (clockControl.thereIsClock()) {
          new Timer().schedule(clockPainter, 0, 1000);
        }
      minimalBottomHeight += clockPainter.getMinimumHeight();
    }

    // log.debug("MinimalBottomHeight = " + minimalBottomHeight);

    int boardHeight = 0;
    if (boardPainter != null)
      boardHeight = boardPainter.getEffectiveHeight(getWidth(), getHeight() - minimalBottomHeight);
    // log.debug("EffectiveBoardHeight = " + boardHeight);

    int extraSpace = getHeight() - boardHeight;

    // reduce extra space to give some room for a small margin
    int margin = (extraSpace - minimalBottomHeight) / 2;
    extraSpace -= margin;
    boardHeight += margin;

    // log.debug("ExtraSpace = " + extraSpace);

    if (clockAndCommentMode == CLOCK_MODE) {
      // log.debug("clock height = " + (clockPainter.getMinimumHeight() +
      // extraSpace));
      clockPainter.setPosition(0, boardHeight, getWidth(), extraSpace);
    } else if (clockAndCommentMode == COMMENT_MODE) {
      scroller.setPosition(0, boardHeight, getWidth(), extraSpace);
    } else {
      // log.debug("Board height = " + getHeight());
      boardHeight = getHeight();
    }
    if (boardPainter != null) {
      boardPainter.setDrawArea(new Rectangle(0, 0, getWidth(), boardHeight));
      gc.tuneBoardPainter();
    }
  }

  public void refresh() {
    //#ifdef DEBUG
    log.debug("total refresh");
    //#endif
    repaint();
    //serviceRepaints();
  }

  public void refresh(Rectangle grArea) {
    //#ifdef DEBUG
    //# log.debug("refresh called on " + grArea);
    //#endif
    repaint(grArea.x0, grArea.y0, grArea.getWidth(), grArea.getHeight());
    serviceRepaints();
  }

  public BoardPainter getBoardPainter() {
    return boardPainter;
  }

  public void setBoardPainter(BoardPainter boardPainter) {
    // log.debug("Set Board Painter - redo the layout");
    this.boardPainter = boardPainter;
    //#ifdef DEBUG
    log.debug("recalcultate Layout by setBoardPainter");
    //#endif
    recalculateLayout();
    //log.debug("repaint() called from setBoardPainter");
    refresh(boardPainter.getDrawArea());
  }

  public void stopClockPainting() {
    if (clockPainter != null)
      clockPainter.cancel();
  }

  public void updateClockAndCommentMode(int mode) {
    clockAndCommentMode = mode;
    //#ifdef DEBUG
    log.debug("recalcultate Layout by setClockAndCommentMode");
    //#endif

    recalculateLayout();
    if (clockAndCommentMode == COMMENT_MODE) {
      startScroller();
    } else
      stopScroller();
    refresh();
  }

  public void cycleClockAndCommentMode(boolean withClock) {
    if (withClock)
      updateClockAndCommentMode((clockAndCommentMode + 1) % 3);
    else
      updateClockAndCommentMode((clockAndCommentMode + 1) % 2);

  }

  //protected void sizeChanged(int x, int y) {
  //#ifdef DEBUG
  // log.debug("sizeChanged called");
  //#endif
  //recalculateLayout();
  //}

}
