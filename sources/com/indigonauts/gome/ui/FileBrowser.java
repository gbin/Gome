/*
 * (c) 2006 Indigonauts
 */

package com.indigonauts.gome.ui;

import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
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
import com.indigonauts.gome.i18n.I18N;
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
public class FileBrowser implements CommandListener, Showable, DownloadCallback {
  //#if DEBUG
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("FileBrowser");
  //#endif

  private static final Vector visibleItems = new Vector();
  public static Command OPEN;
  public static Command SAVE_AS;
  public static Command OPEN_REVIEW;
  public static Command DELETE;
  public static Command BOOKMARK;
  public static Command GOTO_BOOKMARK;
  public static Command IMPORT;
  public static Command SEND_BY_EMAIL;
  private static Command RANDOM;

  public static final Font ITEM_FONT = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_SMALL);

  private static Image dirImg;
  private static Image remoteDirImg;
  private static Image fileImg;
  private static Image remoteFileImg;
  private static Image textFileImg;
  private static final int BLOCK_SIZE = 20;
  private static final int DEFAULT_ILLUSTRATIVE_SIZE = 48;
  private static Rectangle illustrativeRectangle;

  private static Vector pathStack = new Vector();
  private static Vector entriesStack = new Vector();

  private Vector entries;
  private MenuEngine listener;
  private Showable parent;
  private Form uiFolder = new Form("");
  private List uiFileBlock;
  private List uiFile;

  private int indexBlock;
  private int selectedNum;
  private Display display;

  private boolean saveMode;

  private String currentDirectory;
  private Form saveGame;

  private IllustratedItem currentItem;
  private IllustratedItem lastClickedItem;

  static {
    illustrativeRectangle = new Rectangle(0, 0, DEFAULT_ILLUSTRATIVE_SIZE, DEFAULT_ILLUSTRATIVE_SIZE);

    try {
      dirImg = Image.createImage("/dir.png");
      remoteDirImg = Image.createImage("/rdir.png");
      fileImg = Image.createImage("/game.png");
      remoteFileImg = Image.createImage("/rgame.png");
      textFileImg = Image.createImage("/text.png");

    } catch (IOException e) {
      // Nothing we can do
    }

    OPEN = new Command(I18N.open, Command.SCREEN, 2);
    SAVE_AS = new Command(I18N.saveAs, Command.SCREEN, 2);
    OPEN_REVIEW = new Command(I18N.openReview, Command.SCREEN, 2);
    DELETE = new Command(I18N.delete, Command.SCREEN, 3);
    IMPORT = new Command(I18N.import_, Command.SCREEN, 2);
    SEND_BY_EMAIL = new Command(I18N.sendByEmail, Command.SCREEN, 2);
    RANDOM = new Command(I18N.random, Command.SCREEN, 2);
    BOOKMARK = new Command(I18N.bookmark, Command.SCREEN, 2);
    GOTO_BOOKMARK = new Command(I18N.gotoBookmark, Command.SCREEN, 2);

  }

  public FileBrowser(Showable parent, MenuEngine listener, Vector entries, String path, boolean saveMode) {
    this(parent, listener, saveMode);
    this.entries = entries;
    this.currentDirectory = path;
  }

  private FileBrowser(Showable parent, MenuEngine listener, boolean saveMode) {

    this.parent = parent;
    this.listener = listener;
    this.saveMode = saveMode;
  }

  public void addFile(FileEntry entry) {
    entries.addElement(entry);
  }

  public void show(Display disp) {
    //uiFolder = new Form(Util.expandString(I18N.filesIn, new String[] { currentDirectory }));
    visibleItems.removeAllElements();
    uiFolder.deleteAll();
    uiFolder.setTitle(Util.expandString(I18N.filesIn, new String[] { currentDirectory }));

    uiFileBlock = null;
    uiFile = null;
    saveGame = null;

    Enumeration all = entries.elements();
    while (all.hasMoreElements()) {
      FileEntry current = (FileEntry) all.nextElement();
      //#if DEBUG
      log.debug("Entry name: " + current.getName());
      log.debug("Entry url: " + current.getUrl());
      log.debug("Entry path: " + current.getPath());
      //#endif
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
        IllustratedItem illustratedItem = new IllustratedItem(current, image, description + (fileNum != 1 ? " [" + fileNum + "]" : ""), this);
        uiFolder.append(illustratedItem);
      } else if (current instanceof IndexEntry) {
        IndexEntry file = (IndexEntry) current;
        if (file.hasAnIllustration()) {
          image = generateIllustrativePosition(file.getIllustrativeBoardArea(), file.getIllustrativeBlackPosition(), file.getIllustrativeWhitePosition());
        } else {
          image = file.isRemote() ? remoteDirImg : dirImg;
        }
        IllustratedItem illustratedItem = new IllustratedItem(current, image, file.getDescription(), this);
        uiFolder.append(illustratedItem);
      }

    }

    uiFolder.addCommand(MenuEngine.BACK);
    if (saveMode) {
      uiFolder.addCommand(SAVE_AS);
      uiFolder.addCommand(MenuEngine.SAVE);
      uiFolder.addCommand(SEND_BY_EMAIL);
      uiFolder.addCommand(DELETE);
    } else {
      uiFolder.addCommand(OPEN);
      uiFolder.addCommand(SEND_BY_EMAIL);
      uiFolder.addCommand(BOOKMARK);
      uiFolder.addCommand(GOTO_BOOKMARK);
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

  private static Image generateIllustrativePosition(String boardArea, String black, String white) {
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
    //#if AA
    BoardPainter bp = new GlyphBoardPainter(position, illustrativeRectangle, new Rectangle(SgfPoint.createFromSgf((String) boardAreaSplitted.elementAt(0)), SgfPoint
            .createFromSgf((String) boardAreaSplitted.elementAt(1))), false);
    //#else
    //# BoardPainter bp = new BoardPainter(position, illustrativeRectangle, new Rectangle(SgfPoint.createFromSgf((String) boardAreaSplitted.elementAt(0)), SgfPoint
    //#       .createFromSgf((String) boardAreaSplitted.elementAt(1))), false);
    //#endif
    

    Graphics g = generated.getGraphics();
    g.setColor(Util.COLOR_LIGHTGREY);
    g.drawRect(0, 0, DEFAULT_ILLUSTRATIVE_SIZE, DEFAULT_ILLUSTRATIVE_SIZE);
    bp.drawBoard(g, null);

    return Image.createImage(generated);

  }

  public void commandAction(Command c, Displayable s) {
    if (s == uiFolder) {
      if (c == OPEN || c == OPEN_REVIEW || c == List.SELECT_COMMAND) {
        FileEntry entry = currentItem.getEntry();
        if (entry instanceof IndexEntry) {
          new IndexLoader((IndexEntry) entry, this).show(display);
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
          // by default the click is the review mode
          if (c == OPEN_REVIEW || c == List.SELECT_COMMAND) {
            collectionEntry.setPlayMode(GameController.REVIEW_MODE);
          }
          listener.loadFile((CollectionEntry) entry, 0);
          return;
        }
        showFileBlock();

      } else if (c == MenuEngine.SAVE) {
        saveGame = listener.createSaveGameMenu(this, currentDirectory, currentItem.getEntry().getName());
        Gome.singleton.display.setCurrent(saveGame);
      } else if (c == SAVE_AS) {
        saveGame = listener.createSaveGameMenu(this, currentDirectory, null);
        Gome.singleton.display.setCurrent(saveGame);
      } else if (c == BOOKMARK) {
        Gome.singleton.options.defaultDirectory = currentDirectory;
        try {
          Gome.singleton.saveOptions();
          Util.messageBox(I18N.bookmark, I18N.bookmarkSet, AlertType.INFO);
        } catch (Exception e) {
          Util.messageBox(I18N.error.error, I18N.error.error + " " + e, AlertType.ERROR);
        }
        return;
      } else if (c == GOTO_BOOKMARK) {
        IndexEntry bookmark = new IndexEntry(Gome.singleton.options.defaultDirectory, "", "");
        new IndexLoader(bookmark, this).show(display);
        return;
      }

      else if (c == MenuEngine.BACK) {
        int size = pathStack.size();
        if (size == 0)
          parent.show(display);
        else {
          this.currentDirectory = (String) pathStack.lastElement();
          this.entries = (Vector) entriesStack.lastElement();
          pathStack.removeElementAt(size - 1);
          entriesStack.removeElementAt(size - 1);
          show(display);
        }

      } else if (c == DELETE) {
        FileEntry entry = currentItem.getEntry();
        if (!(entry instanceof LocalFileEntry)) {
          Util.messageBox(I18N.error.error, I18N.error.onlyLocal, AlertType.ERROR);
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

        Gome.singleton.display.setCurrent(uiFolder); // don't reparse everthing with "show"
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
        CollectionEntry entry = (CollectionEntry) currentItem.getEntry();
        int num = entry.getCollectionSize();
        selectedNum = Util.rnd(num);
        listener.loadFile(entry, selectedNum);

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
        boolean alreadyThere = false;
        Enumeration elements = entries.elements();
        while (elements.hasMoreElements()) {
          if (((FileEntry) elements.nextElement()).getName().equals(name)) {
            alreadyThere = true;
            break;
          }
        }
        // TODO confirmation

        //#if JSR75
        try {
          IOManager.singleton.saveJSR75(currentDirectory, name, Gome.singleton.gameController.getSgfModel());
        } catch (IOException e) {
          Util.messageBox(I18N.error.error, e.getMessage(), AlertType.ERROR); //$NON-NLS-1$
        }
        //#else
        //# try {
        //#   IOManager.singleton.saveLocalGame(name, Gome.singleton.gameController.getSgfModel());
        //# } catch (RecordStoreException rse) {
        //#  Util.messageBox(I18N.error.error, rse.getMessage(), AlertType.ERROR); //$NON-NLS-1$ 
        //# }
        //#endif

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

    FileEntry entry = currentItem.getEntry();
    uiFileBlock = new List(entry.getUrl(), Choice.IMPLICIT);

    uiFileBlock.addCommand(MenuEngine.BACK);
    uiFileBlock.setCommandListener(this);
    int all = ((CollectionEntry) entry).getCollectionSize();
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
    FileEntry entry = currentItem.getEntry();
    uiFile = new List(entry.getUrl(), Choice.IMPLICIT);
    uiFile.addCommand(MenuEngine.BACK);
    uiFile.addCommand(OPEN);
    uiFile.setCommandListener(this);

    int min = indexBlock * BLOCK_SIZE + 1;

    int all = ((CollectionEntry) entry).getCollectionSize();
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
      Util.messageBox(I18N.error.error, I18N.error.onlyOnline, AlertType.ERROR); //$NON-NLS-1$ //$NON-NLS-2$
      return;
    }

    FileImporter fileLoader = new FileImporter(this, selectedFile);
    fileLoader.show(display);
    fileLoader.start();
  }

  public void downloadFinished(String path, Vector files) {
    pathStack.addElement(currentDirectory);
    entriesStack.addElement(entries);
    this.currentDirectory = path;
    this.entries = files;
    show(Gome.singleton.display);
  }

  public void downloadFailure(Exception reason) {
    Util.messageBox(I18N.error.error, reason.getMessage(), AlertType.ERROR); //$NON-NLS-1$
  }

  public void done() {
    listener.commandAction(MenuEngine.FILES, Gome.singleton.mainCanvas);
  }

  public void failed(Exception reason) {
    Util.messageBox(I18N.failure, reason.getMessage(), AlertType.ERROR); //$NON-NLS-1$
  }

  public IllustratedItem getCurrentItem() {
    return currentItem;
  }

  public void setCurrentItem(IllustratedItem currentItem) {
    this.currentItem = currentItem;
  }

  public void traverseOut() {
    for (int i = 0, size = visibleItems.size(); i < size; i++)

      ((IllustratedItem) visibleItems.elementAt(i)).forceRedraw();

  }

  public void showNotify(IllustratedItem illustratedItem) {
    visibleItems.addElement(illustratedItem);
  }

  public void hideNotify(IllustratedItem illustratedItem) {
    visibleItems.removeElement(illustratedItem);
  }

  public int getWidth() {
    return uiFolder.getWidth();
  }

  public void open() {
    commandAction(OPEN, uiFolder);
  }

  public void clicked(IllustratedItem illustratedItem) {
    if (lastClickedItem == illustratedItem) {
      open();
    } else {
      lastClickedItem = illustratedItem;
    }

  }
}
