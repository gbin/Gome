/*
 * (c) 2006 Indigonauts
 */

package com.indigonauts.gome.ui;

//#ifdef MIDP2
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Canvas;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.CustomItem;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.List;

import com.indigonauts.gome.Gome;
import com.indigonauts.gome.common.Rectangle;
import com.indigonauts.gome.common.StringVector;
import com.indigonauts.gome.common.Util;
import com.indigonauts.gome.io.BundledFileEntry;
import com.indigonauts.gome.io.CollectionEntry;
import com.indigonauts.gome.io.FileEntry;
import com.indigonauts.gome.io.IOManager;
import com.indigonauts.gome.io.IndexEntry;
import com.indigonauts.gome.io.LocalFileEntry;
import com.indigonauts.gome.sgf.Board;
import com.indigonauts.gome.sgf.SgfPoint;

//#ifndef JSR75
//# import javax.microedition.rms.RecordStoreException;
//#endif

/**
 * Fetcher extention is just for the text files
 * @author gbin
 *
 */
public class FileBrowserV2 implements CommandListener, Showable, Runnable, DownloadCallback {
  //#ifdef DEBUG
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("FileBrowser");
  //#endif

  private Vector entries = new Vector(10);
  private MenuEngine listener;
  private Showable parent;
  private Form uiFolder;
  private List uiFileBlock;
  private List uiFile;

  private int indexFolder;
  private int indexBlock;
  private int selectedNum;
  private Display display;

  public static Command OPEN;
  public static Command OPEN_REVIEW;
  public static Command DELETE;
  public static Command IMPORT;
  public static Command SEND_BY_EMAIL;
  private static Command RANDOM;

  private static final Font ITEM_FONT = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_SMALL);

  private static Image dirImg;
  private static Image remoteDirImg;
  private static Image fileImg;
  private static Image remoteFileImg;
  private static Image textFileImg;
  private static final int BLOCK_SIZE = 20;
  private static final int DEFAULT_ILLUSTRATIVE_SIZE = 48;
  private static GraphicRectangle illustrativeRectangle;

  static {
    illustrativeRectangle = new GraphicRectangle(0, 0, DEFAULT_ILLUSTRATIVE_SIZE, DEFAULT_ILLUSTRATIVE_SIZE);

    try {
      dirImg = Image.createImage("/dir.png");
      remoteDirImg = Image.createImage("/rdir.png");
      fileImg = Image.createImage("/game.png");
      remoteFileImg = Image.createImage("/rgame.png");
      textFileImg = Image.createImage("/text.png");

    } catch (IOException e) {
      // Nothing we can do
    }

    OPEN = new Command(Gome.singleton.bundle.getString("ui.open"), Command.SCREEN, 2); //$NON-NLS-1$
    OPEN_REVIEW = new Command(Gome.singleton.bundle.getString("ui.openReview"), Command.SCREEN, 2); //$NON-NLS-1$
    DELETE = new Command(Gome.singleton.bundle.getString("ui.delete"), Command.SCREEN, 3); //$NON-NLS-1$
    IMPORT = new Command(Gome.singleton.bundle.getString("ui.import"), Command.SCREEN, 2); //$NON-NLS-1$
    SEND_BY_EMAIL = new Command(Gome.singleton.bundle.getString("ui.sendByEmail"), Command.SCREEN, 2); //$NON-NLS-1$
    RANDOM = new Command(Gome.singleton.bundle.getString("ui.random"), Command.SCREEN, 2); //$NON-NLS-1$

  }

  private boolean saveMode;

  private String currentDirectory;
  private Form saveGame;

  public FileBrowserV2(Showable parent, MenuEngine listener, Vector entries, String path, boolean saveMode) {
    this(parent, listener, saveMode);
    this.entries = entries;
    this.currentDirectory = path;
  }

  private FileBrowserV2(Showable parent, MenuEngine listener, boolean saveMode) {

    this.parent = parent;
    this.listener = listener;
    this.saveMode = saveMode;

    reset();
  }

  public void addFile(FileEntry entry) {
    entries.addElement(entry);
  }

  /**
   * delete all contents
   */
  public void reset() {
    entries.removeAllElements();
  }

  public void show(Display disp) {
    uiFolder = new Form(Gome.singleton.bundle.getString("ui.filesIn", new String[] { currentDirectory }));
    ended = false;
    ticker = new Thread(this);
    ticker.start();
    Enumeration all = entries.elements();
    while (all.hasMoreElements()) {
      FileEntry current = (FileEntry) all.nextElement();
      Image image = null;
      if (current instanceof CollectionEntry) {
        CollectionEntry file = (CollectionEntry) current;
        int fileNum = file.getCollectionSize();

        String description = file.getDescription();
        if (file.hasAnIllustration()) {
          image = generateIllustrativePosition(file.getIllustrativeBoardArea(), file.getIllustrativeBlackPosition(), file.getIllustrativeWhitePosition());
        } else if (file.getPlayMode() == GameController.TEXT_MODE) {
          image = textFileImg;
        } else {
          image = file.isRemote() ? remoteFileImg : fileImg;
        }
        IllustratedItem illustratedItem = new IllustratedItem(current, image, description + (fileNum != 1 ? " [" + fileNum + "]" : ""));
        uiFolder.append(illustratedItem);
      } else if (current instanceof IndexEntry) {
        IndexEntry file = (IndexEntry) current;
        if (file.hasAnIllustration()) {
          image = generateIllustrativePosition(file.getIllustrativeBoardArea(), file.getIllustrativeBlackPosition(), file.getIllustrativeWhitePosition());
        } else {
          image = file.isRemote() ? remoteDirImg : dirImg;
        }
        IllustratedItem illustratedItem = new IllustratedItem(current, image, file.getDescription());
        uiFolder.append(illustratedItem);
      }

    }

    uiFolder.addCommand(MenuEngine.BACK);
    if (saveMode) {
      uiFolder.addCommand(MenuEngine.SAVE);
    } else {
      uiFolder.addCommand(OPEN);
      uiFolder.addCommand(SEND_BY_EMAIL);
      if (currentDirectory.startsWith(IOManager.LOCAL_NAME)) {
        uiFolder.addCommand(OPEN_REVIEW);
        uiFolder.addCommand(DELETE);
      } else {
        uiFolder.addCommand(IMPORT);
      }

    }

    uiFolder.setCommandListener(this);
    display = disp;
    if (!saveMode)
      listener.updateLastBrowser(this);
    display.setCurrent(uiFolder);
  }

  private Image generateIllustrativePosition(String boardArea, String black, String white) {
    StringVector blackPoints = new StringVector(black, ';');
    StringVector whitePoints = new StringVector(white, ';');
    Image generated = Image.createImage(DEFAULT_ILLUSTRATIVE_SIZE, DEFAULT_ILLUSTRATIVE_SIZE);
    Board position = new Board();
    Enumeration bpoints = blackPoints.elements();
    while (bpoints.hasMoreElements()) {
      position.placeStone(SgfPoint.createFromSgf((String) bpoints.nextElement()), Board.BLACK);
    }
    Enumeration wpoints = whitePoints.elements();
    while (wpoints.hasMoreElements()) {
      position.placeStone(SgfPoint.createFromSgf((String) wpoints.nextElement()), Board.WHITE);
    }

    StringVector boardAreaSplitted = new StringVector(boardArea, ';');
    BoardPainter bp = new BoardPainter(position, illustrativeRectangle, new Rectangle(SgfPoint.createFromSgf((String) boardAreaSplitted.elementAt(0)), SgfPoint
            .createFromSgf((String) boardAreaSplitted.elementAt(1))), false);

    Graphics g = generated.getGraphics();
    g.setColor(Util.COLOR_LIGHTGREY);
    g.drawRect(0, 0, DEFAULT_ILLUSTRATIVE_SIZE, DEFAULT_ILLUSTRATIVE_SIZE);
    bp.drawBoard(g, null);

    return Image.createImage(generated);

  }

  public void commandAction(Command c, Displayable s) {
    ended = true;
    if (s == uiFolder) {
      if (c == OPEN || c == OPEN_REVIEW || c == List.SELECT_COMMAND) {
        FileEntry entry = currentItem.getEntry();
        if (entry instanceof IndexEntry) {

          new IndexLoader((IndexEntry) entry, this).show(Gome.singleton.display);
          return;

        }
        if (saveMode)
          return; //don't load any file in save mode

        if (entry instanceof CollectionEntry && ((CollectionEntry) entry).getCollectionSize() == 1) {
          CollectionEntry collectionEntry = ((CollectionEntry) entry);
          if (collectionEntry.getPlayMode() == GameController.TEXT_MODE) {
            new Info(this, collectionEntry.getName(), collectionEntry.getUrl());
            return;
          }

          indexBlock = 0;
          selectedNum = 0;
          if (c == OPEN_REVIEW) {
            collectionEntry.setPlayMode(GameController.REVIEW_MODE);
          }
          listener.loadFile((CollectionEntry) entry, 0);
          return;
        }
        showFileBlock();

      } else if (c == MenuEngine.SAVE) {
        saveGame = listener.createSaveGameMenu(this, currentDirectory);
        Gome.singleton.display.setCurrent(saveGame);
      }

      else if (c == MenuEngine.BACK) {
        parent.show(display);
        // You cannot put it at null as it is cached now
      } else if (c == DELETE) {
        FileEntry entry = currentItem.getEntry();
        if (!(entry instanceof LocalFileEntry)) {
          Util.messageBox(Gome.singleton.bundle.getString("ui.error"), Gome.singleton.bundle.getString("ui.error.onlyLocal"), AlertType.ERROR);
          return;
        }
        listener.deleteFile(entry);
        int nb = uiFolder.size();
        for (int i = 0; i < nb; i++) {
          if (uiFolder.get(i) == currentItem) {
            uiFolder.delete(i);
            break;
          }
        }
        show(Gome.singleton.display);
      } else if (c == IMPORT) {
        FileEntry selectedFile = currentItem.getEntry();
        importFile(selectedFile);
      } else if (c == SEND_BY_EMAIL) {
        IOManager.singleton.sendFileByMail(currentItem.getEntry(), Gome.singleton.options.email);
      }

    }
    if (s == uiFileBlock) {
      if (c == MenuEngine.BACK) {
        display.setCurrent(uiFolder);
      }

      else if (c == List.SELECT_COMMAND) {
        int index = uiFileBlock.getSelectedIndex();
        indexBlock = index;
        showFile();
      } else if (c == RANDOM) {
        Object entry = entries.elementAt(indexFolder);
        int num = ((CollectionEntry) entry).getCollectionSize();
        selectedNum = Util.rnd(num);
        listener.loadFile((CollectionEntry) entry, selectedNum);

      }
    } else if (s == uiFile) {
      if (c == MenuEngine.BACK) {
        display.setCurrent(uiFileBlock);
      }

      else if (c == List.SELECT_COMMAND || c == OPEN) {

        int indexFile = uiFile.getSelectedIndex();
        selectedNum = indexBlock * BLOCK_SIZE + indexFile + 1;
        FileEntry selectedFile = currentItem.getEntry();
        listener.loadFile((CollectionEntry) selectedFile, selectedNum);
      }
    }

    else if (s == saveGame) {
      if (c == MenuEngine.SAVE) {
        String name = Gome.singleton.menuEngine.gameFileName.getString();
        //#ifdef JSR75
        try {
          IOManager.singleton.saveJSR75(currentDirectory, name, Gome.singleton.gameController.getSgfModel());
        } catch (IOException e) {
          Util.messageBox(Gome.singleton.bundle.getString("ui.error"), Gome.singleton.bundle.getString(e.getMessage()), AlertType.ERROR); //$NON-NLS-1$
        }
        //#else
        //# try {
        //#  IOManager.singleton.saveLocalGame(name, Gome.singleton.gameController.getSgfModel());
        //# } catch (RecordStoreException rse) {
        //#  Util.messageBox(Gome.singleton.bundle.getString("ui.error"), Gome.singleton.bundle.getString(rse.getMessage()), AlertType.ERROR); //$NON-NLS-1$ 
        //# }
        //#endif
        boolean alreadyThere = false;
        Enumeration elements = entries.elements();
        while (elements.hasMoreElements()) {
          if (((FileEntry) elements.nextElement()).getName().equals(name)) {
            alreadyThere = true;
            break;
          }
        }
        if (!alreadyThere)
          addFile(new LocalFileEntry(currentDirectory, name, name));
        this.show(display);

      } else if (c == MenuEngine.BACK) {
        Gome.singleton.display.setCurrent(uiFolder);
      }
    }

  }

  /**
   * i.e. 1-20, 21-40, 41-55...
   */
  private void showFileBlock() {

    uiFileBlock = new List(currentItem.getEntry().getUrl(), Choice.IMPLICIT);

    uiFileBlock.addCommand(MenuEngine.BACK);
    uiFileBlock.setCommandListener(this);
    int all = ((CollectionEntry) entries.elementAt(indexFolder)).getCollectionSize();
    int remain = all;
    int n = 0;

    if (remain > 0) {
      uiFileBlock.addCommand(RANDOM);
    }

    while (remain > 0) {
      int min = n * BLOCK_SIZE + 1;
      int max = (n + 1) * BLOCK_SIZE;
      max = Math.min(all, max);
      uiFileBlock.append(String.valueOf(min) + " - " + max, null); //$NON-NLS-1$
      remain -= BLOCK_SIZE;
      ++n;
    }
    for (int i = 0; i < uiFileBlock.size(); i++) {
      uiFileBlock.setFont(i, ITEM_FONT);
    }

    display.setCurrent(uiFileBlock);
  }

  private void showFile() {
    uiFile = new List(currentItem.getEntry().getUrl(), Choice.IMPLICIT);
    uiFile.addCommand(MenuEngine.BACK);
    uiFile.addCommand(OPEN);
    uiFile.setCommandListener(this);

    int min = indexBlock * BLOCK_SIZE + 1;

    int all = ((CollectionEntry) entries.elementAt(indexFolder)).getCollectionSize();
    int max = Math.min((indexBlock + 1) * BLOCK_SIZE, all);

    for (int i = min; i <= max; ++i)
      uiFile.append(String.valueOf(i), null);

    for (int i = 0; i < uiFile.size(); i++) {
      uiFile.setFont(i, ITEM_FONT);
    }
    display.setCurrent(uiFile);

  }

  public final int getSelectedNum() {
    return selectedNum;// m_indexBlock*m_blockSize+m_indexFile+1;
  }

  public void importFile(FileEntry selectedFile) {
    boolean typeok = true;
    if (selectedFile instanceof LocalFileEntry || selectedFile instanceof BundledFileEntry) {
      typeok = false;
    } else if (selectedFile instanceof IndexEntry) {
      IndexEntry ie = (IndexEntry) selectedFile;
      if (!ie.getPath().startsWith("http://")) //$NON-NLS-1$
        typeok = false;
    }
    if (!typeok) {
      Util.messageBox(Gome.singleton.bundle.getString("ui.error"), Gome.singleton.bundle.getString("ui.error.onlyOnline"), AlertType.ERROR); //$NON-NLS-1$ //$NON-NLS-2$
      return;
    }

    FileImporter fileLoader = new FileImporter(this, selectedFile);
    fileLoader.show(display);
    fileLoader.start();
  }

  public void downloadFinished(String path, Vector files) {
    FileBrowserV2 son = new FileBrowserV2(this, listener, files, path, saveMode);
    son.show(Gome.singleton.display);
  }

  public void downloadFailure(Exception reason) {
    Util.messageBox(Gome.singleton.bundle.getString("ui.error"), reason.getMessage(), AlertType.ERROR); //$NON-NLS-1$
  }

  public void done() {
    listener.commandAction(MenuEngine.FILES, Gome.singleton.mainCanvas);
  }

  public void failed(Exception reason) {
    Util.messageBox(Gome.singleton.bundle.getString("ui.failure"), Gome.singleton.bundle.getString(reason.getMessage()), AlertType.ERROR); //$NON-NLS-1$
  }

  private IllustratedItem currentItem;

  class IllustratedItem extends CustomItem {
    private Image icon;
    private FileEntry entry;
    private String text;

    public IllustratedItem(FileEntry entry, Image icon, String text) {
      super(null);
      this.icon = icon;
      this.entry = entry;
      this.text = text == null ? "" : text;
    }

    //public IllustratedItem(String sgf, String text) {
    //  super(null);
    // }

    protected boolean traverse(int dir, int viewportWidth, int viewportHeight, int[] visRect_inout) {
      //log.debug("traverse " + text);
      //log.debug("dir = " + dir);
      if (currentItem == this)
        return false;
      currentItem = this;
      tooLarge = icon.getWidth() + ITEM_FONT.charWidth(' ') + ITEM_FONT.stringWidth(text) > uiFolder.getWidth();
      return true;

    }

    protected void traverseOut() {
      //log.debug("traverse out" + text);
      ticked = -2;
      this.repaint();
    }

    protected int getMinContentHeight() {
      return ITEM_FONT.getHeight();
    }

    protected int getMinContentWidth() {
      return uiFolder.getWidth();
    }

    protected int getPrefContentHeight(int arg0) {
      return Math.max(icon.getHeight(), ITEM_FONT.getHeight());
    }

    protected int getPrefContentWidth(int arg0) {
      return uiFolder.getWidth();
      //return icon.getHeight() + ITEM_FONT.charWidth(' ') + ITEM_FONT.stringWidth(text);
    }

    protected void paint(Graphics g, int w, int h) {
      if (currentItem == this) {
        g.setColor(Gome.singleton.display.getColor(Display.COLOR_HIGHLIGHTED_BACKGROUND));
        g.fillRect(0, 0, uiFolder.getWidth(), uiFolder.getHeight());
        g.setColor(Gome.singleton.display.getColor(Display.COLOR_HIGHLIGHTED_FOREGROUND));
      } else {
        g.setColor(Gome.singleton.display.getColor(Display.COLOR_FOREGROUND));
      }
      g.setFont(ITEM_FONT);

      g.drawImage(icon, 0, 0, Graphics.TOP | Graphics.LEFT);

      String showText;

      if (ticked > 0) {
        showText = text.substring(ticked) + "-" + text;
        if (ticked == text.length()) {
          ticked = 0;
        }

      } else {
        showText = text;
      }
      g.drawString(showText, icon.getWidth() + ITEM_FONT.charWidth(' '), 0, Graphics.TOP | Graphics.LEFT);

    }

    public FileEntry getEntry() {
      return entry;
    }

    protected void keyPressed(int keyCode) {
      if (Gome.singleton.mainCanvas.getGameAction(keyCode) == Canvas.FIRE) {
        (FileBrowserV2.this).commandAction(OPEN, uiFolder);
      } else {
        super.keyPressed(keyCode);
      }
    }

    boolean tooLarge;

    public boolean isTooLarge() {
      return tooLarge;

    }

    int ticked = -2;

    public void tick() {
      ticked++;
      repaint();
    }

  }

  boolean ended = false;
  Thread ticker;

  public void run() {
    while (!ended) {
      synchronized (this) {
        try {
          wait(500);
        } catch (InterruptedException e) {

        }
      }
      if (currentItem != null) {
        if (currentItem.isTooLarge())
          currentItem.tick();
      }
    }
    //#ifdef DEBUG
    log.debug("end of ticker");
    //#endif
  }
}
// MIDP1 case, just do nothing
//#else
// public class FileBrowserV2 {}
//#endif
