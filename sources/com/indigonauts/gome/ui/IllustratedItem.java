package com.indigonauts.gome.ui;

import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.CustomItem;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;

import com.indigonauts.gome.Gome;
import com.indigonauts.gome.io.BundledFileEntry;
import com.indigonauts.gome.io.FileEntry;
import com.indigonauts.gome.io.IndexEntry;
import com.indigonauts.gome.io.LocalFileEntry;
import com.indigonauts.gome.io.URLFileEntry;

public class IllustratedItem extends CustomItem {
  //#if DEBUG
  //# private org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("FileBrowser");
  //#endif
  private Image icon;
  private FileEntry entry;
  private String text;
  private boolean repaint, traversed;
  private boolean tooLarge;
  private int ticked = 0;
  private FileBrowser parent;

  public IllustratedItem(FileEntry entry, Image icon, String text, FileBrowser parent) {
    super(null);
    this.icon = icon;
    this.entry = entry;
    this.text = text == null ? "" : text;

    this.tooLarge = (icon != null ? icon.getWidth() : 0) + FileBrowser.ITEM_FONT.charWidth(' ') + FileBrowser.ITEM_FONT.stringWidth(text) > parent.getWidth();

    this.parent = parent;
  }

  protected boolean traverse(int dir, int viewportWidth, int viewportHeight, int[] visRect_inout) {
    if (parent.getCurrentItem() != this) {
      parent.setCurrentItem(this);
      repaint = !traversed;
      traversed = true;
      repaint();
      visRect_inout[0] = 0;
      visRect_inout[1] = 0;
      visRect_inout[2] = parent.getWidth();
      if (icon != null)
        visRect_inout[3] = Math.max(icon.getHeight(), FileBrowser.ITEM_FONT.getHeight());
      else
        visRect_inout[3] = FileBrowser.ITEM_FONT.getHeight();
      return true;
    }

    if (tooLarge) {
      if (dir == Canvas.RIGHT) {
        tickRight();
        return true;
      } else if (dir == Canvas.LEFT) {
        tickLeft();
        return true;
      }
    }
    return false;

  }

  protected void traverseOut() {
    traversed = false;
    parent.traverseOut();
  }

  protected void showNotify() {
    parent.showNotify(this);
  }

  protected void hideNotify() {
    parent.hideNotify(this);
  }

  protected int getMinContentHeight() {
    return FileBrowser.ITEM_FONT.getHeight();
  }

  protected int getMinContentWidth() {
    return parent.getWidth();
  }

  protected int getPrefContentHeight(int height) {
    if (icon != null)
      return Math.max(icon.getHeight(), FileBrowser.ITEM_FONT.getHeight());

    return FileBrowser.ITEM_FONT.getHeight();
  }

  protected int getPrefContentWidth(int width) {
    return parent.getWidth();
  }

  protected void paint(Graphics g, int w, int h) {
    if (repaint) {
      repaint();
      repaint = false;
    }

    if (parent.getCurrentItem() == this) {
      g.setColor(Gome.singleton.display.getColor(Display.COLOR_HIGHLIGHTED_BACKGROUND));
      g.fillRect(0, 0, w, h);
      g.setColor(Gome.singleton.display.getColor(Display.COLOR_HIGHLIGHTED_FOREGROUND));
    } else {
      //g.setColor(Gome.singleton.display.getColor(Display.COLOR_BACKGROUND));
      //g.fillRect(0, 0, w, h);
      g.setColor(Gome.singleton.display.getColor(Display.COLOR_FOREGROUND));
    }
    g.setFont(FileBrowser.ITEM_FONT);

    if (icon != null)
      g.drawImage(icon, 0, 0, Graphics.TOP | Graphics.LEFT);

    String showText;

    if (ticked > 0) {
      showText = text.substring(ticked);
    }

    else {
      showText = text;
    }
    if (icon != null)
      g.drawString(showText, icon.getWidth() + FileBrowser.ITEM_FONT.charWidth(' '), (h - FileBrowser.ITEM_FONT.getHeight()) / 2 + 1, Graphics.TOP | Graphics.LEFT);
    else {
      String id;
      if (entry instanceof IndexEntry)
        id = "[DIR]";
      else if (entry instanceof URLFileEntry)
        id = "[Int]";
      else if (entry instanceof LocalFileEntry)
        id = "[Loc]";
      else if (entry instanceof BundledFileEntry)
        id = "[Bun]";
      else
        id = "[???]";
      g.drawString(id + showText, 0, (h - FileBrowser.ITEM_FONT.getHeight()) / 2 + 1, Graphics.TOP | Graphics.LEFT);
    }

  }

  public FileEntry getEntry() {
    return entry;
  }

  protected void keyPressed(int keyCode) {
    if (Gome.singleton.mainCanvas.getGameAction(keyCode) == Canvas.FIRE) {
      parent.open();
    } else {
      super.keyPressed(keyCode);
    }
  }

  public boolean isTooLarge() {
    return tooLarge;

  }

  protected void pointerReleased(int x, int y) {
    parent.clicked(this);
  }

  public void tickRight() {
    if (icon != null)
      if (icon.getWidth() + FileBrowser.ITEM_FONT.charWidth(' ') + FileBrowser.ITEM_FONT.stringWidth(text.substring(ticked)) > parent.getWidth())
        ticked++;
      else if (FileBrowser.ITEM_FONT.stringWidth("?????") + FileBrowser.ITEM_FONT.stringWidth(text.substring(ticked)) > parent.getWidth())
        ticked++;
    repaint();
  }

  public void tickLeft() {
    if (ticked > 0)
      ticked--;
    repaint();
  }

  public void forceRedraw() {
    repaint();
  }
}
