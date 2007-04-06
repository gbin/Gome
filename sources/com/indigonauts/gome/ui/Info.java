package com.indigonauts.gome.ui;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;

import com.indigonauts.gome.Gome;
import com.indigonauts.gome.MainCanvas;
import com.indigonauts.gome.common.Point;
import com.indigonauts.gome.common.Rectangle;
import com.indigonauts.gome.common.ResourceBundle;
import com.indigonauts.gome.common.Util;
import com.indigonauts.gome.sgf.Board;
import com.indigonauts.gome.sgf.SgfModel;
import com.indigonauts.gome.sgf.SymbolAnnotation;

public class Info implements CommandListener, Showable {
  private Showable parent;
  private Form current = getKeys();

  private static final Command RULES;
  private static final Command KEYS;
  private static final Command ABOUT;
  private boolean inSubMenu = false;

  static {
    RULES = new Command(Gome.singleton.bundle.getString("ui.help.rules"), Command.SCREEN, 1); //$NON-NLS-1$
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

  private Form getRules() {
    Board illustrativeBoard = new Board((byte) 5);
    GraphicRectangle imgArea = new GraphicRectangle(1, 1, 51, 51);
    Rectangle boardArea = new Rectangle((byte) 0, (byte) 0, (byte) 4, (byte) 4);
    BoardPainter bp = new BoardPainter(illustrativeBoard, imgArea, boardArea);
    ResourceBundle bundle = Gome.singleton.bundle;
    Form help = new Form(bundle.getString("ui.help"));
    Image img = Image.createImage(53, 53);
    bp.drawBoard(img.getGraphics());
    help
            .append("The board is a grid of horizontal and vertical lines.\n The board used here is small (5x5) compared to the sizes you will find in clubs, tournaments and online (typically 19x19), but the rules are the same.");
    help.append(Image.createImage(img));

    help
            .append("The lines of the board have intersections wherever they cross or touch each other. Each intersection is called a point. That includes the four corners, and the edges of the board.\nThe example board has 25 points. The red circle shows one particular point. The red square in the corner shows another point.");
    SymbolAnnotation sa = new SymbolAnnotation(new Point((byte) 4, (byte) 0), SymbolAnnotation.SQUARE);
    bp.drawSymbolAnnotation(img.getGraphics(), sa, 0xFF0000);
    SymbolAnnotation sa2 = new SymbolAnnotation(new Point((byte) 1, (byte) 2), SymbolAnnotation.CIRCLE);
    bp.drawSymbolAnnotation(img.getGraphics(), sa2, 0xFF0000);
    help.append(Image.createImage(img));
    return help;
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
    help.append(buf.toString());
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
    form.append(info.toString());
    return form;
  }

  private Form getAbout() {
    ResourceBundle bundle = Gome.singleton.bundle;
    Form form = new Form(bundle.getString("ui.about"));
    form.append("GOME v" + Gome.VERSION);
    form.append("(c) 2005-2007 Indigonauts");
    form.append("www.indigonauts.com/gome");
    return form;

  }
}
