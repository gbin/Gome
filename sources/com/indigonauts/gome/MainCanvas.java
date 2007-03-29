/*
 * (c) 2006 Indigonauts
 */

package com.indigonauts.gome;

import java.util.Timer;
import java.util.Vector;

import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import com.indigonauts.gome.common.Util;
import com.indigonauts.gome.ui.BoardPainter;
import com.indigonauts.gome.ui.ClockPainterTask;
import com.indigonauts.gome.ui.GameController;
import com.indigonauts.gome.ui.GraphicRectangle;
import com.indigonauts.gome.ui.MenuEngine;
import com.indigonauts.gome.ui.Showable;

public class MainCanvas extends Canvas implements CommandListener, Showable {
  //#ifdef DEBUG
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("MainCanvas");
  //#endif
  
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

  private int KEY_SCROLLUP;
  private int KEY_SCROLLDOWN;
  public int KEY_10NEXTMOVES;
  public int KEY_10PREVMOVES;
  private Scroller scroller;
  private BoardPainter boardPainter;
  private String currentComment;
  private String splashInfo;
  private ClockController clockControl;
  private ClockPainterTask clockPainter;
  private int bottomMode = NOTHING_TO_DISPLAY_MODE;

  public static final byte NOTHING_TO_DISPLAY_MODE = 0;
  public static final byte COMMENT_MODE = 1;
  public static final byte CLOCK_MODE = 2;

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
          KEY_10NEXTMOVES = ((Integer) keys.elementAt(2)).intValue();
          if (size > 3) {
            KEY_10PREVMOVES = ((Integer) keys.elementAt(3)).intValue();
          }
        }
      }
    }

  }

  public MainCanvas() {
    //#ifdef MIDP2
    setFullScreenMode(true);
    //#endif

    assignUnassignedKey();
    addCommand(MenuEngine.NEW);
    addCommand(MenuEngine.LOAD);
    addCommand(MenuEngine.SAVE);
    addCommand(MenuEngine.GAME_STATUS);
    
    //#ifdef IGS
    switchToIGSOfflineMenu();
    //#endif
    
    addCommand(MenuEngine.OPTIONS);
    //#ifdef DEBUG
    addCommand(MenuEngine.CONSOLE);
    //#endif
    addCommand(MenuEngine.HELP);
    addCommand(MenuEngine.ABOUT);
    addCommand(MenuEngine.LIBRARY);
    addCommand(MenuEngine.EXIT);
    setCommandListener(this);
    scroller = new Scroller(this);
    clockPainter = new ClockPainterTask(this);
    bottomMode = NOTHING_TO_DISPLAY_MODE;
    //#ifdef DEBUG
    log.debug("Main Canvas Loaded");
    //#endif
  }

  public void assignClockController(ClockController c) {
    clockControl = c;
  }

  private void scroll(int keyCode) {
    if (keyCode == KEY_SCROLLUP && scroller.isStarted()) {
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
    scroll(keyCode);
    boolean refreshNeeded = false;
    char playMode = gc.getPlayMode();
    boolean review = playMode == GameController.REVIEW_MODE || playMode == GameController.JOSEKI_MODE || playMode == GameController.OBSERVE_MODE;

    switch (getGameAction(keyCode)) {
    case ACTION_UP:
    case ACTION_DOWN:
    case ACTION_LEFT:
    case ACTION_RIGHT:
      splashInfo = null;
      if (gc.isCountMode()) {
        refreshNeeded = gc.doMoveCursor(keyCode);
      } else {
        if (review)
          refreshNeeded = gc.doReviewMove(keyCode);
        else
          refreshNeeded = gc.doMoveCursor(keyCode);
      }
      break;

    default:
      if (review) {
        refreshNeeded = gc.doAction(keyCode) | gc.doReviewAction(keyCode);
      } else {
        refreshNeeded = gc.doAction(keyCode);
      }
    }
    if (refreshNeeded) {
      // log.debug("repaint() called from keyPressed");
      refresh();
    }
  }

  private void clearOptionalItems() {
    removeCommand(MenuEngine.REVIEW_MODE);
    removeCommand(MenuEngine.PLAY_MODE);
    removeCommand(MenuEngine.PASS);
    removeCommand(MenuEngine.LAST_MOVE);
    removeCommand(MenuEngine.FIRST_MOVE);
    removeCommand(MenuEngine.FINISHED_COUNTING);
    removeCommand(MenuEngine.RESIGN);
    //#ifdef IGS
    removeCommand(MenuEngine.IGS_MESSAGE);
    removeCommand(MenuEngine.IGS_GAMELIST);
    removeCommand(MenuEngine.IGS_USERLIST);
    removeCommand(MenuEngine.IGS_DISCONNECT);
    removeCommand(MenuEngine.IGS_CONNECT);
    removeCommand(MenuEngine.IGS_RESET_DEADS_TONE);
    removeCommand(MenuEngine.IGS_DONE_SCORE);
    removeOnlineSetKomiAndHandicapMenuItem();
    //#endif
  }

  public void switchToPlayMenu() {
    clearOptionalItems();
    //#ifdef IGS
    if (!Gome.singleton.options.igsLogin.equals("") && !Gome.singleton.options.igsPassword.equals("")) {

      addCommand(MenuEngine.IGS_CONNECT);
    }
    //#endif

    addCommand(MenuEngine.REVIEW_MODE);
    addCommand(MenuEngine.PASS);
    addCommand(MenuEngine.RESIGN);
  }

  //#ifdef IGS
  public void switchToOnlinePlayMenu() {
    clearOptionalItems();
    removeCommand(MenuEngine.IGS_CONNECT);
    addCommand(MenuEngine.REQUEST_KOMI);
    addCommand(MenuEngine.CHANGE_ONLINE_HANDICAP);
    addCommand(MenuEngine.PASS);
    addCommand(MenuEngine.RESIGN);
    addCommand(MenuEngine.IGS_MESSAGE);
  }

  //#endif
  //#ifdef IGS
  public void removeOnlineSetKomiAndHandicapMenuItem() {
    removeCommand(MenuEngine.REQUEST_KOMI);
    removeCommand(MenuEngine.CHANGE_ONLINE_HANDICAP);
  }

  //#endif
  //#ifdef IGS
  public void removeOnlineSetHandicapMenuItem() {
    removeCommand(MenuEngine.CHANGE_ONLINE_HANDICAP);
  }

  //#endif
  public void switchToReviewMenu() {

    clearOptionalItems();
    //#ifdef IGS
    if (!Gome.singleton.options.igsLogin.equals("") && !Gome.singleton.options.igsPassword.equals("")) {
      addCommand(MenuEngine.IGS_CONNECT);
    }
    //#endif
    addCommand(MenuEngine.FIRST_MOVE);
    addCommand(MenuEngine.LAST_MOVE);
    addCommand(MenuEngine.PLAY_MODE);
  }

  public void switchToJosekiMenu() {
    clearOptionalItems();
    addCommand(MenuEngine.FIRST_MOVE);
  }

  public void switchToProblemMenu() {
    clearOptionalItems();
    addCommand(MenuEngine.FIRST_MOVE);
  }

  //#ifdef IGS
  public void switchToObservePlayMenu() {
    clearOptionalItems();
    addCommand(MenuEngine.FIRST_MOVE);
    addCommand(MenuEngine.LAST_MOVE);
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
  //#ifdef IGS
  public void switchToIGSOfflineMenu() {
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
    addCommand(MenuEngine.FINISHED_COUNTING);
  }

  //#ifdef IGS
  public void switchToOnlineCountingMenu() {
    removeCommand(MenuEngine.PASS);
    addCommand(MenuEngine.FINISHED_COUNTING);
    addCommand(MenuEngine.IGS_DONE_SCORE);
    addCommand(MenuEngine.IGS_RESET_DEADS_TONE);
  }

  //#endif

  public void show(Display dis) {
    // log.debug("show");
    

    if (gc.hasNextInCollection()) {
      this.addCommand(MenuEngine.NEXT);
    } else {
      this.removeCommand(MenuEngine.NEXT);
    }

    if (!Gome.singleton.checkLicense()) return;
    dis.setCurrent(this);
  }

  public void hide() {
    // nothing to do
  }

  public void setGameController(GameController p_gc) {
    gc = p_gc;
  }

  protected void pointerReleased(int x, int y) {
    gc.pointerReleased(x, y);
    super.pointerPressed(x, y);
  }

  public void commandAction(Command c, Displayable d) {
    if (c == MenuEngine.HELP) {
      displayHelp();
    } else if (c == MenuEngine.GAME_STATUS) {
      displayGameStatus();
    } else if (c == MenuEngine.PLAY_MODE) {
      gc.setPlayMode(GameController.GAME_MODE);
      setSplashInfo(Gome.singleton.bundle.getString("ui.switchToPlayEditMode"));
      if (clockControl != null) {
        clockControl.resumeClock();
      }
      setClockAndCommentMode(CLOCK_MODE);
    } else if (c == MenuEngine.REVIEW_MODE) {
      gc.setPlayMode(GameController.REVIEW_MODE);
      setSplashInfo(Gome.singleton.bundle.getString("ui.switchToReviewMode"));
      if (clockControl != null)
        clockControl.pauseClock();
      setClockAndCommentMode(COMMENT_MODE);
    } else
      Gome.singleton.menuEngine.commandAction(c, d);
  }

  public void paint(Graphics g) {

    // log.debug("Paint " + g.getClipX() + "/" + g.getClipY() + "/" +
    // g.getClipWidth() + "/" + g.getClipHeight());

    if (scroller.getX() == g.getClipX() && scroller.getY() == g.getClipY() && scroller.getWidth() == g.getClipWidth() && scroller.getHeight() == g.getClipHeight()) {
      // log.debug("Refresh only the bottom");
      drawStatusBar(g);
    } else if (bottomMode == CLOCK_MODE && clockPainter.getX() == g.getClipX() && clockPainter.getY() == g.getClipY() && clockPainter.getWidth() == g.getClipWidth()
            && clockPainter.getHeight() == g.getClipHeight()) {
      // log.debug("Refresh only the TIME");
      drawStatusBar(g);
    } else {
      // log.debug("Refresh all");
      if (boardPainter != null) {
        boardPainter.drawMe(g);
      } /*
         * else { log.debug("Board is null "); }
         */

      if (splashInfo != null) {
        drawSplashInfo(g);
      }

      // draw Status bar clip, so put it at the end
      drawStatusBar(g);
    }
    // log.debug("End Paint " + g.getClipX() + "/" + g.getClipY() + "/" +
    // g.getClipWidth() + "/" + g.getClipHeight());

  }

  private void drawSplashInfo(Graphics g) {

    Util.renderSplash(g, splashInfo, getWidth(), getHeight(), Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_ITALIC, Font.SIZE_SMALL), Util.COLOR_BLACK, 0xDCFF84);
  }

  public void drawStatusBar(Graphics g) {
    switch (bottomMode) {
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
    Font scrollerFont = Gome.singleton.options.getScrollerFont();

    scroller.setSpeed(Gome.singleton.options.getScrollerSpeed());
    scroller.setBigStep(scrollerFont.getHeight() / 2);

    // log.debug("Scroller height = " + scroller.getHeight());

    Image img;

    img = Util.renderOffScreenScrollableText(currentComment != null ? currentComment : "#" + gc.getMoveNb(), getWidth(), scroller.getHeight(), scrollerFont, Util.COLOR_BLACK, //$NON-NLS-1$ //$NON-NLS-2$
            Util.COLOR_LIGHT_BACKGROUND);

    scroller.setImg(img);
    if (scroller.isStarted())
      scroller.stop();

    scroller.start();
  }

  public void stopScroller() {
    synchronized (SCROLLER_SYNC) {
      scroller.stop();
    }
  }

  /**
   * @param g
   */
  private void drawSquiggles(Graphics g) {
    int squiggleSize = 4;
    int nbSquiggles = getWidth() / squiggleSize;
    boolean up = true;
    g.setColor(Util.COLOR_GREEN);
    for (int i = 0; i < nbSquiggles; i++) {
      if (up) {
        g.drawLine(i * squiggleSize, getHeight(), i * squiggleSize + squiggleSize, getHeight() - squiggleSize);
      } else {
        g.drawLine(i * squiggleSize, getHeight() - squiggleSize, i * squiggleSize + squiggleSize, getHeight());
      }
      up = !up;

    }
  }

  public void displayHelp() {
    StringBuffer buf = new StringBuffer();
    char playMode = gc.getPlayMode();
    if (playMode == GameController.JOSEKI_MODE || playMode == GameController.OBSERVE_MODE || playMode == GameController.REVIEW_MODE) {
      buf.append(Gome.singleton.bundle.getString("ui.help.pointerReview1")); //$NON-NLS-1$
      buf.append('\n');
      buf.append(Gome.singleton.bundle.getString("ui.help.pointerReview2")); //$NON-NLS-1$
      buf.append('\n');
      buf.append(Gome.singleton.bundle.getString("ui.help.pointerReview3")); //$NON-NLS-1$
      buf.append('\n');
      buf.append(Gome.singleton.bundle.getString("ui.help.pointerReview4")); //$NON-NLS-1$
    } else {
      buf.append(Gome.singleton.bundle.getString("ui.help.pointer")); //$NON-NLS-1$
    }
    buf.append('\n');
    buf.append(Util.getActionKeyName(this, getKeyCode(ACTION_COMMENT)));
    buf.append(' ');
    buf.append(Gome.singleton.bundle.getString("ui.help.comment")); //$NON-NLS-1$
    buf.append('\n');
    buf.append(Util.getActionKeyName(this, getKeyCode(ACTION_ZOOM)));
    buf.append(' ');
    buf.append(Gome.singleton.bundle.getString("ui.help.zoom")); //$NON-NLS-1$
    buf.append('\n');
    buf.append(Util.getActionKeyName(this, getKeyCode(ACTION_UNDO)));
    buf.append(' ');
    buf.append(Gome.singleton.bundle.getString("ui.help.undo")); //$NON-NLS-1$
    buf.append('\n');
    buf.append(Util.getActionKeyName(this, getKeyCode(ACTION_HINT)));
    buf.append(' ');
    buf.append(Gome.singleton.bundle.getString("ui.help.hint")); //$NON-NLS-1$

    buf.append('\n');
    buf.append(getKeyName(KEY_SCROLLUP));
    buf.append(' ');
    buf.append(Gome.singleton.bundle.getString("ui.help.scrollUp")); //$NON-NLS-1$
    buf.append('\n');
    buf.append(getKeyName(KEY_SCROLLDOWN));
    buf.append(' ');
    buf.append(Gome.singleton.bundle.getString("ui.help.scrollDown")); //$NON-NLS-1$
    buf.append('\n');
    buf.append(getKeyName(KEY_10NEXTMOVES));
    buf.append(' ');
    buf.append(Gome.singleton.bundle.getString("ui.help.next10Moves")); //$NON-NLS-1$
    buf.append('\n');
    buf.append(getKeyName(KEY_10PREVMOVES));
    buf.append(' ');
    buf.append(Gome.singleton.bundle.getString("ui.help.prev10Moves")); //$NON-NLS-1$

    Util.messageBox(Gome.singleton.bundle.getString("ui.help"), buf.toString(), AlertType.INFO); //$NON-NLS-1$

  }

  private void displayGameStatus() {
    gc.doPopGameStatus();
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

      if (bottomMode == COMMENT_MODE) {
        startScroller();
      }
    }
  }

  public void setSplashInfo(String splash) {
    this.splashInfo = splash;
    refresh();
  }

  protected void showNotify() {
    // log.debug("Show notify");
    refresh();
  }

  public void recalculateLayout() {
    int minimalBottomHeight = 0;
    if (bottomMode == COMMENT_MODE) {
      minimalBottomHeight += scroller.getMinimumHeight();
    }
    if (bottomMode == CLOCK_MODE) {
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

    // log.debug("ExtraSpace = " + extraSpace);

    if (bottomMode == CLOCK_MODE) {
      // log.debug("clock height = " + (clockPainter.getMinimumHeight() +
      // extraSpace));
      clockPainter.setPosition(0, boardHeight, getWidth(), extraSpace);
    } else if (bottomMode == COMMENT_MODE) {
      // log.debug("scroller height = " + (scroller.getMinimumHeight() +
      // extraSpace));
      scroller.setPosition(0, boardHeight, getWidth(), extraSpace);
    } else {
      // log.debug("Board height = " + getHeight());
      boardHeight = getHeight();
    }
    if (boardPainter != null) {
      boardPainter.setDrawArea(new GraphicRectangle(0, 0, getWidth(), boardHeight));
      gc.tuneBoardPainter();
    }
  }


  public void refresh() {
    repaint();
    serviceRepaints();
  }

  public BoardPainter getBoardPainter() {
    return boardPainter;
  }

  public void setBoardPainter(BoardPainter boardPainter) {
    // log.debug("Set Board Painter - redo the layout");
    this.boardPainter = boardPainter;
    recalculateLayout();
    refresh();
  }

  public void stopClockPainting() {
    if (clockPainter != null)
      clockPainter.cancel();
  }

  public void setClockAndCommentMode(int mode) {
    bottomMode = mode;
    recalculateLayout();
    if (bottomMode == COMMENT_MODE) {
      startScroller();
    } else
      stopScroller();
  }

}
