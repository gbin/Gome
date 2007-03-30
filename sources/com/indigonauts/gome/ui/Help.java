package com.indigonauts.gome.ui;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;

import com.indigonauts.gome.Gome;
import com.indigonauts.gome.MainCanvas;
import com.indigonauts.gome.common.ResourceBundle;
import com.indigonauts.gome.common.Util;

public class Help implements CommandListener, Showable {
  private Showable parent;
  private Form help;

  public Help(Showable parent) {
    this.parent = parent;
    ResourceBundle bundle = Gome.singleton.bundle;
    help = new Form(bundle.getString("ui.help"));

    char playMode = Gome.singleton.gameController.getPlayMode();
    help.append(getHelp(playMode));
    help.addCommand(MenuEngine.BACK);
    help.setCommandListener(this);
  }

  public void commandAction(Command command, Displayable disp) {
    if (command == MenuEngine.BACK)
      parent.show(Gome.singleton.display);
  }

  public void show(Display destination) {

    destination.setCurrent(help);
  }

  private String getHelp(char playMode) {
    MainCanvas canvas = Gome.singleton.mainCanvas;
    StringBuffer buf = new StringBuffer();
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
    buf.append(Util.getActionKeyName(canvas, canvas.getKeyCode(MainCanvas.ACTION_COMMENT)));
    buf.append(' ');
    buf.append(Gome.singleton.bundle.getString("ui.help.comment")); //$NON-NLS-1$
    buf.append('\n');
    buf.append(Util.getActionKeyName(canvas, canvas.getKeyCode(MainCanvas.ACTION_ZOOM)));
    buf.append(' ');
    buf.append(Gome.singleton.bundle.getString("ui.help.zoom")); //$NON-NLS-1$
    buf.append('\n');
    buf.append(Util.getActionKeyName(canvas, canvas.getKeyCode(MainCanvas.ACTION_UNDO)));
    buf.append(' ');
    buf.append(Gome.singleton.bundle.getString("ui.help.undo")); //$NON-NLS-1$
    buf.append('\n');
    buf.append(Util.getActionKeyName(canvas, canvas.getKeyCode(MainCanvas.ACTION_HINT)));
    buf.append(' ');
    buf.append(Gome.singleton.bundle.getString("ui.help.hint")); //$NON-NLS-1$

    buf.append('\n');
    buf.append(canvas.getKeyName(canvas.KEY_SCROLLUP));
    buf.append(' ');
    buf.append(Gome.singleton.bundle.getString("ui.help.scrollUp")); //$NON-NLS-1$
    buf.append('\n');
    buf.append(canvas.getKeyName(canvas.KEY_SCROLLDOWN));
    buf.append(' ');
    buf.append(Gome.singleton.bundle.getString("ui.help.scrollDown")); //$NON-NLS-1$
    buf.append('\n');
    buf.append(canvas.getKeyName(canvas.KEY_10NEXTMOVES));
    buf.append(' ');
    buf.append(Gome.singleton.bundle.getString("ui.help.next10Moves")); //$NON-NLS-1$
    buf.append('\n');
    buf.append(canvas.getKeyName(canvas.KEY_10PREVMOVES));
    buf.append(' ');
    buf.append(Gome.singleton.bundle.getString("ui.help.prev10Moves")); //$NON-NLS-1$
    return buf.toString();

  }

}
