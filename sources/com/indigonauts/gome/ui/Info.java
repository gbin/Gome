package com.indigonauts.gome.ui;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Enumeration;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.Item;
import javax.microedition.lcdui.StringItem;

import com.indigonauts.gome.Gome;
import com.indigonauts.gome.MainCanvas;
import com.indigonauts.gome.common.Rectangle;
import com.indigonauts.gome.common.ResourceBundle;
import com.indigonauts.gome.common.StringVector;
import com.indigonauts.gome.common.Util;
import com.indigonauts.gome.io.IOManager;
import com.indigonauts.gome.sgf.Board;
import com.indigonauts.gome.sgf.SgfModel;
import com.indigonauts.gome.sgf.SgfNode;

public class Info implements CommandListener, Showable {
  //#ifdef DEBUG
  //# private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("Info");
  //#endif

  private Showable parent;
  private Form current = getKeys();

  private static final Command RULES;
  private static final Command KEYS;
  private static final Command HELP;
  private static final Command ABOUT;
  private boolean inSubMenu = false;

  static {
    RULES = new Command(Gome.singleton.bundle.getString("ui.help.rules"), Command.SCREEN, 1); //$NON-NLS-1$
    HELP = new Command(Gome.singleton.bundle.getString("ui.help.help"), Command.SCREEN, 1); //$NON-NLS-1$
    KEYS = new Command(Gome.singleton.bundle.getString("ui.help.key"), Command.SCREEN, 1); //$NON-NLS-1$
    ABOUT = new Command(Gome.singleton.bundle.getString("ui.about"), Command.SCREEN, 9); //$NON-NLS-1$
  }

  public Info(Showable parent) {
    this.parent = parent;
    current = getKeys();
    setUpCurrent();
  }

  public Info(MainCanvas mainCanvas, Command def) {
    this(mainCanvas);
    commandAction(def, null);
    inSubMenu = false; // so it jumps back directly
  }

  private void setUpCurrent() {
    current.addCommand(KEYS);
    current.addCommand(HELP);
    current.addCommand(RULES);
    current.addCommand(MenuEngine.GAME_STATUS);
    current.addCommand(ABOUT);
    current.addCommand(MenuEngine.BACK);
    current.setCommandListener(this);
  }

  public void commandAction(Command command, Displayable disp) {
    if (command == MenuEngine.BACK) {
      if (inSubMenu) {
        current = getKeys();
        inSubMenu = false;
      } else {
        parent.show(Gome.singleton.display);
        return;
      }
    } else if (command == RULES) {
      current = getRules();
      inSubMenu = true;
    } else if (command == KEYS) {
      current = getKeys();
      inSubMenu = false;
    } else if (command == HELP) {
      current = getHelp();
      inSubMenu = true;
    } else if (command == MenuEngine.GAME_STATUS) {
      current = getGameInfo();
      inSubMenu = true;
    } else if (command == ABOUT) {
      current = getAbout();
      inSubMenu = true;
    }

    setUpCurrent();
    show(Gome.singleton.display);
  }

  public void show(Display destination) {

    destination.setCurrent(current);
  }

  private Form getKeys() {
    ResourceBundle bundle = Gome.singleton.bundle;
    MainCanvas canvas = Gome.singleton.mainCanvas;
    Form help = new Form(bundle.getString("ui.help"));
    StringBuffer buf = new StringBuffer();
    buf.append(bundle.getString("ui.help.pointerReview1")); //$NON-NLS-1$
    buf.append('\n');
    buf.append(bundle.getString("ui.help.pointerReview2")); //$NON-NLS-1$
    buf.append('\n');
    buf.append(bundle.getString("ui.help.pointerReview3")); //$NON-NLS-1$
    buf.append('\n');
    buf.append(bundle.getString("ui.help.pointerReview4")); //$NON-NLS-1$
    buf.append(bundle.getString("ui.help.pointer")); //$NON-NLS-1$
    buf.append('\n');
    buf.append(Util.getActionKeyName(canvas, canvas.getKeyCode(MainCanvas.ACTION_COMMENT)));
    buf.append(' ');
    buf.append(bundle.getString("ui.help.comment")); //$NON-NLS-1$
    buf.append('\n');
    buf.append(Util.getActionKeyName(canvas, canvas.getKeyCode(MainCanvas.ACTION_ZOOM)));
    buf.append(' ');
    buf.append(bundle.getString("ui.help.zoom")); //$NON-NLS-1$
    buf.append('\n');
    buf.append(Util.getActionKeyName(canvas, canvas.getKeyCode(MainCanvas.ACTION_UNDO)));
    buf.append(' ');
    buf.append(bundle.getString("ui.help.undo")); //$NON-NLS-1$
    buf.append('\n');
    buf.append(Util.getActionKeyName(canvas, canvas.getKeyCode(MainCanvas.ACTION_HINT)));
    buf.append(' ');
    buf.append(bundle.getString("ui.help.hint")); //$NON-NLS-1$

    buf.append('\n');
    buf.append(canvas.getKeyName(canvas.KEY_SCROLLUP));
    buf.append(' ');
    buf.append(bundle.getString("ui.help.scrollUp")); //$NON-NLS-1$
    buf.append('\n');
    buf.append(canvas.getKeyName(canvas.KEY_SCROLLDOWN));
    buf.append(' ');
    buf.append(bundle.getString("ui.help.scrollDown")); //$NON-NLS-1$
    buf.append('\n');
    buf.append(canvas.getKeyName(canvas.KEY_10NEXTMOVES));
    buf.append(' ');
    buf.append(bundle.getString("ui.help.next10Moves")); //$NON-NLS-1$
    buf.append('\n');
    buf.append(canvas.getKeyName(canvas.KEY_10PREVMOVES));
    buf.append(' ');
    buf.append(bundle.getString("ui.help.prev10Moves")); //$NON-NLS-1$
    StringItem si = new StringItem("", buf.toString());
    //#ifdef MIDP2
    si.setFont(MainCanvas.SMALL_FONT);
    //#endif
    help.append(si);
    return help;
  }

  private Form getGameInfo() {
    ResourceBundle bundle = Gome.singleton.bundle;
    Form form = new Form(bundle.getString("game.info"));
    GameController gc = Gome.singleton.gameController;
    SgfModel model = gc.getSgfModel();

    StringBuffer info = new StringBuffer();
    info.append(bundle.getString("game.captured")); //$NON-NLS-1$
    info.append(gc.getBoard().getNbCapturedBlack());
    info.append('/');
    info.append(gc.getBoard().getNbCapturedWhite());
    info.append('\n');
    if (model.getName() != null) {
      info.append(bundle.getString("game.name")); //$NON-NLS-1$
      info.append(model.getName());
      info.append('\n');
    }
    if (model.getEvent() != null) {
      info.append(bundle.getString("game.event")); //$NON-NLS-1$
      info.append(model.getEvent());
      info.append('\n');
    }
    if (model.getRound() != null) {
      info.append(bundle.getString("game.round")); //$NON-NLS-1$
      info.append(model.getRound());
      info.append('\n');
    }
    if (model.getDate() != null) {
      info.append(" "); //$NON-NLS-1$
      info.append(model.getDate());
    }
    if (model.getBlackPlayer() != null && model.getWhitePlayer() != null) {
      info.append('\n');
      info.append(model.getWhitePlayer());
      if (model.getWhiteRank() != null) {
        info.append(bundle.getString("game.whiteShort")); //$NON-NLS-1$
        info.append('[');
        info.append(model.getWhiteRank());
        info.append(']');
      }
      info.append(' ');
      info.append(bundle.getString("game.versus")); //$NON-NLS-1$
      info.append(' ');
      info.append(model.getBlackPlayer());
      if (model.getBlackRank() != null) {
        info.append(bundle.getString("game.blackShort")); //$NON-NLS-1$
        info.append('[');
        info.append(model.getBlackRank());
        info.append(']');
      }
      info.append('\n');
    }
    if (model.getBlackTeam() != null & model.getWhiteTeam() != null) {
      info.append(model.getWhiteTeam());
      info.append(' ');
      info.append(bundle.getString("game.versus")); //$NON-NLS-1$
      info.append(' ');
      info.append(model.getBlackTeam());
      info.append('\n');
    }
    if (model.getKomi() != null) {
      info.append(bundle.getString("game.komi")); //$NON-NLS-1$
      info.append(model.getKomi());
      info.append('\n');
    }
    if (model.getResult() != null) {
      info.append(bundle.getString("game.result")); //$NON-NLS-1$
      info.append(model.getResult());
      info.append('\n');
    }
    if (model.getOpening() != null) {
      info.append(bundle.getString("game.opening")); //$NON-NLS-1$
      info.append(model.getOpening());
      info.append('\n');
    }
    if (model.getPlace() != null) {
      info.append(bundle.getString("game.place")); //$NON-NLS-1$
      info.append(model.getPlace());
      info.append('\n');
    }
    if (model.getContext() != null) {
      info.append(bundle.getString("game.context")); //$NON-NLS-1$
      info.append(model.getContext());
      info.append('\n');
    }
    if (model.getScribe() != null) {
      info.append(bundle.getString("game.scribe")); //$NON-NLS-1$
      info.append(model.getScribe());
      info.append('\n');
    }
    if (model.getSource() != null) {
      info.append(bundle.getString("game.source")); //$NON-NLS-1$
      info.append(model.getSource());
      info.append('\n');
    }
    if (model.getApplication() != null) {
      info.append(bundle.getString("game.application")); //$NON-NLS-1$
      info.append(model.getApplication());
      info.append('\n');
    }
    if (model.getCopyright() != null) {
      info.append(bundle.getString("game.copyright")); //$NON-NLS-1$
      info.append(model.getCopyright());
      info.append('\n');
    }
    StringItem si = new StringItem("", info.toString());
    //#ifdef MIDP2
    si.setFont(MainCanvas.SMALL_FONT);
    //#endif
    form.append(si);
    return form;
  }

  private Form getAbout() {
    ResourceBundle bundle = Gome.singleton.bundle;
    Form form = new Form(bundle.getString("ui.about"));
    StringBuffer buf = new StringBuffer("GOME v");
    buf.append(Gome.VERSION);
    buf.append("\n\n");
    buf.append("(c) 2005-2007 Indigonauts");
    buf.append("\n\n");
    //#ifdef MIDP2
    StringItem url = new StringItem("", "http://www.indigonauts.com/gome", Item.HYPERLINK);
    url.setFont(MainCanvas.SMALL_FONT);
    //#else
    //# StringItem url = new StringItem("", "http://www.indigonauts.com/gome");
    //#endif
    form.append(buf.toString());
    form.append(url);

    return form;

  }

  private Image generatePosition(String sgf) {
    SgfModel model = SgfModel.parse(new InputStreamReader(new ByteArrayInputStream(sgf.getBytes())));
    Rectangle viewArea = model.getViewArea();
    byte boardSize = model.getBoardSize();
    Board board = new Board(boardSize);
    int grsize = boardSize * 10 + 1;
    GraphicRectangle imgArea = new GraphicRectangle(1, 1, grsize, grsize);
    BoardPainter illustrativeBoard = new BoardPainter(board, imgArea, viewArea.isValid() ? viewArea : null);
    Image img = Image.createImage(grsize + 2, grsize + 2);
    SgfNode firstNode = model.getFirstNode();
    board.placeStones(firstNode.getAB(), Board.BLACK);
    board.placeStones(firstNode.getAW(), Board.WHITE);
    illustrativeBoard.drawMe(img.getGraphics(), null, 0, false, firstNode, model);
    return Image.createImage(img);
  }

  private static final Font TITLE_FONT = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_BOLD, Font.SIZE_MEDIUM);
  private static final Font UNDERLINED_FONT = Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_UNDERLINED, Font.SIZE_MEDIUM);

  private Form formatHelp(String name, String url) {
    Form form = new Form(name);
    try {
      byte[] file = IOManager.singleton.loadFile(url, null);
      StringVector list = new StringVector(new String(file), '\n');
      Enumeration all = list.elements();

      while (all.hasMoreElements()) {
        String element = (String) all.nextElement();
        if (element.startsWith("(;")) {
          form.append(generatePosition(element));
        } else if (element.startsWith("*")) {
          StringItem si = new StringItem("", element.substring(1));
          //#ifdef MIDP2
          si.setFont(TITLE_FONT);
          //#endif
          form.append(si);
        } else if (element.startsWith("_")) {
          StringItem si = new StringItem("", element.substring(1));
          //#ifdef MIDP2
          si.setFont(UNDERLINED_FONT);
          //#endif
          form.append(si);
        } else {
          StringItem si = new StringItem("", element);
          //#ifdef MIDP2
          si.setFont(MainCanvas.SMALL_FONT);
          //#endif
          form.append(si);
        }
        form.append("\n");
      }
    } catch (IOException ioe) {// TODO: error handling
    }
    return form;
  }

  private Form getHelp() {
    return formatHelp(Gome.singleton.bundle.getString("ui.help.help"), "jar:/com/indigonauts/gome/i18n/help/general_US.hlp");
  }

  private Form getRules() {
    return formatHelp(Gome.singleton.bundle.getString("ui.help.rules"), "jar:/com/indigonauts/gome/i18n/help/rules_US.hlp");
  }

}
