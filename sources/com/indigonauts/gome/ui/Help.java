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
import com.indigonauts.gome.sgf.SymbolAnnotation;

public class Help implements CommandListener, Showable {
  private Showable parent;
  private Form current = getKeys();

  private static final Command RULES;
  private static final Command KEYS;
  private boolean inSubMenu = false;

  static {
    RULES = new Command(Gome.singleton.bundle.getString("ui.help.rules"), Command.SCREEN, 1); //$NON-NLS-1$
    KEYS = new Command(Gome.singleton.bundle.getString("ui.help.key"), Command.SCREEN, 1); //$NON-NLS-1$
  }

  public Help(Showable parent) {
    this.parent = parent;
    current = getKeys();
    setUpCurrent();
  }

  private void setUpCurrent() {
    current.addCommand(KEYS);
    current.addCommand(RULES);
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
    SymbolAnnotation sa = new SymbolAnnotation(new Point((byte)4,(byte)0), SymbolAnnotation.SQUARE);
    bp.drawSymbolAnnotation(img.getGraphics(), sa, 0xFF0000);
    SymbolAnnotation sa2 = new SymbolAnnotation(new Point((byte)1,(byte)2), SymbolAnnotation.CIRCLE);
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

}
