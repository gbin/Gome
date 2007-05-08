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
//#ifndef JSR75
//# import javax.microedition.rms.RecordStoreException;
//#endif
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

public class FileBrowser implements CommandListener, Showable {
  //#ifdef DEBUG
  //# private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("FileBrowser");
  //#endif

  private Vector entries = new Vector(10);
  MenuEngine listener;
  private Showable parent;
  private List uiFolder;
  private List uiFileBlock;
  private List uiFile;
  private static final int BLOCK_SIZE = 20;
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
  
  static 
  {
    OPEN = new Command(Gome.singleton.bundle.getString("ui.open"), Command.SCREEN, 2); //$NON-NLS-1$
    OPEN_REVIEW = new Command(Gome.singleton.bundle.getString("ui.openReview"), Command.SCREEN, 2); //$NON-NLS-1$
    DELETE = new Command(Gome.singleton.bundle.getString("ui.delete"), Command.SCREEN, 3); //$NON-NLS-1$
    IMPORT = new Command(Gome.singleton.bundle.getString("ui.import"), Command.SCREEN, 2); //$NON-NLS-1$
    SEND_BY_EMAIL = new Command(Gome.singleton.bundle.getString("ui.sendByEmail"), Command.SCREEN, 2); //$NON-NLS-1$
    RANDOM = new Command(Gome.singleton.bundle.getString("ui.random"), Command.SCREEN, 2); //$NON-NLS-1$

  }

  //#ifdef MENU_IMAGES
  private Image dirImg;
  private Image remoteDirImg;
  private Image fileImg;
  private Image remoteFileImg;
  private Image textFileImg;

  private static final int DEFAULT_ILLUSTRATIVE_SIZE = 32;
  int bestImageWidth = DEFAULT_ILLUSTRATIVE_SIZE;
  int bestImageHeight = DEFAULT_ILLUSTRATIVE_SIZE;
  private GraphicRectangle illustrativeRectangle;
  //#endif

  private boolean saveMode;

  private String currentDirectory;
  private Form saveGame;

  private FileBrowser(Showable parent, MenuEngine listener, boolean saveMode) {
    
    this.parent = parent;
    this.listener = listener;
    this.saveMode = saveMode;

    //#ifdef MENU_IMAGES
    //#ifdef MIDP2
    bestImageWidth = Gome.singleton.display.getBestImageWidth(Display.CHOICE_GROUP_ELEMENT);
    bestImageHeight = Gome.singleton.display.getBestImageHeight(Display.CHOICE_GROUP_ELEMENT);
    //#endif
    illustrativeRectangle = new GraphicRectangle(1, 1, bestImageWidth, bestImageHeight);

    try {
      dirImg = Util.renderIcon(Image.createImage("/dir.png"), bestImageWidth, bestImageHeight);
      remoteDirImg = Util.renderIcon(Image.createImage("/rdir.png"), bestImageWidth, bestImageHeight); //$NON-NLS-1$
      fileImg = Util.renderIcon(Image.createImage("/file.png"), bestImageWidth, bestImageHeight); //$NON-NLS-1$
      remoteFileImg = Util.renderIcon(Image.createImage("/rfile.png"), bestImageWidth, bestImageHeight); //$NON-NLS-1$
      textFileImg = Util.renderIcon(Image.createImage("/text.png"), bestImageWidth, bestImageHeight); //$NON-NLS-1$
    } catch (IOException e) {
      // Nothing we can do
    }
    //#endif
    reset();
  }

  public FileBrowser(Showable parent, MenuEngine listener, Vector entries, String path, boolean saveMode) {
    this(parent, listener, saveMode);
    this.entries = entries;
    this.currentDirectory = path;
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
    uiFolder = new List(Gome.singleton.bundle.getString("ui.filesIn", new String[] { currentDirectory }), Choice.IMPLICIT);

    Enumeration all = entries.elements();
    while (all.hasMoreElements()) {
      FileEntry current = (FileEntry) all.nextElement();
      Image image = null;
      if (current instanceof CollectionEntry) {
        CollectionEntry file = (CollectionEntry) current;
        int fileNum = file.getCollectionSize();

        String description = file.getDescription();
        //#ifdef MENU_IMAGES
        if (file.hasAnIllustration()) {
          image = generateIllustrativePosition(file.getIllustrativeBoardArea(), file.getIllustrativeBlackPosition(), file.getIllustrativeWhitePosition());
        } else if (file.getPlayMode() == GameController.TEXT_MODE) {
          image = textFileImg;
        } else {
          image = file.isRemote() ? remoteFileImg : fileImg;
        }
        //#endif
        uiFolder.append(description + (fileNum != 1 ? " [" + fileNum + "]" : ""), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                image);
      } else if (current instanceof IndexEntry) {
        IndexEntry file = (IndexEntry) current;
        //#ifdef MENU_IMAGES
        if (file.hasAnIllustration()) {
          image = generateIllustrativePosition(file.getIllustrativeBoardArea(), file.getIllustrativeBlackPosition(), file.getIllustrativeWhitePosition());
        } else {
          image = file.isRemote() ? remoteDirImg : dirImg;
        }
        //#endif

        uiFolder.append(file.getDescription(), image);

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

    //#ifdef MIDP2 
    Font font = Font.getFont(Font.FACE_SYSTEM, Font.STYLE_PLAIN, Font.SIZE_SMALL);
    int size = uiFolder.size();
    for (int i = 0; i < size; i++) {
      uiFolder.setFont(i, font);
    }
    //#endif

    display.setCurrent(uiFolder);
  }

  //#ifdef MENU_IMAGES
  private Image generateIllustrativePosition(String boardArea, String black, String white) {
    StringVector blackPoints = new StringVector(black, ';');
    StringVector whitePoints = new StringVector(white, ';');
    Image generated = Image.createImage(bestImageWidth, bestImageHeight);
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
            .createFromSgf((String) boardAreaSplitted.elementAt(1))));

    Graphics g = generated.getGraphics();
    g.setColor(Util.COLOR_LIGHTGREY);
    g.drawRect(0, 0, bestImageWidth, bestImageHeight);
    bp.drawBoard(g, null);

    return Image.createImage(generated);

  }

  //#endif

  public void commandAction(Command c, Displayable s) {
    if (s == uiFolder) {

      if (c == OPEN || c == OPEN_REVIEW || c == List.SELECT_COMMAND) {

        indexFolder = uiFolder.getSelectedIndex();
        Object entry = entries.elementAt(indexFolder);

        if (entry instanceof IndexEntry) {

          new IndexLoader((IndexEntry) entry, this).show(Gome.singleton.display);
          return;

        }
        if (saveMode)
          return; //don't load any file in save mode
        if (entry instanceof CollectionEntry && ((CollectionEntry) entry).getCollectionSize() == 1) {
          indexBlock = 0;
          selectedNum = 0;
          if (c == OPEN_REVIEW) {
            ((CollectionEntry) entry).setPlayMode(GameController.REVIEW_MODE);
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
        parent = null;
      } else if (c == DELETE) {
        indexFolder = uiFolder.getSelectedIndex();
        Object obj = entries.elementAt(indexFolder);
        if (!(obj instanceof LocalFileEntry)) {
          Util.messageBox(Gome.singleton.bundle.getString("ui.error"), Gome.singleton.bundle.getString("ui.error.onlyLocal"), AlertType.ERROR);
          return;
        }
        LocalFileEntry entry = (LocalFileEntry) obj;
        listener.deleteFile(entry);
        entries.removeElementAt(indexFolder);

        show(Gome.singleton.display);
      } else if (c == IMPORT) {
        FileEntry selectedFile = getSelectedFile();
        importFile(selectedFile);
      } else if (c == SEND_BY_EMAIL) {
        indexFolder = uiFolder.getSelectedIndex();
        Object obj = entries.elementAt(indexFolder);
        IOManager.singleton.sendFileByMail((FileEntry) obj, Gome.singleton.options.email);
      }

    } else if (s == uiFileBlock) {
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
        FileEntry selectedFile = getSelectedFile();
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

    uiFileBlock = new List(getSelectedFile().getUrl(), Choice.IMPLICIT);

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

    display.setCurrent(uiFileBlock);
  }

  private void showFile() {
    uiFile = new List(getSelectedFile().getUrl(), Choice.IMPLICIT);
    uiFile.addCommand(MenuEngine.BACK);
    uiFile.addCommand(OPEN);
    uiFile.setCommandListener(this);

    int min = indexBlock * BLOCK_SIZE + 1;

    int all = ((CollectionEntry) entries.elementAt(indexFolder)).getCollectionSize();
    int max = Math.min((indexBlock + 1) * BLOCK_SIZE, all);

    for (int i = min; i <= max; ++i)
      uiFile.append(String.valueOf(i), null);
    display.setCurrent(uiFile);

  }

  public final FileEntry getSelectedFile() {
    return (FileEntry) entries.elementAt(indexFolder);
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
    FileBrowser son = new FileBrowser(this, listener, files, path, saveMode);
    son.show(Gome.singleton.display);
  }

  public void downloadFailure(Exception reason) {
    Util.messageBox(Gome.singleton.bundle.getString("ui.error"), reason.getMessage(), AlertType.ERROR); //$NON-NLS-1$
  }

  void done() {
    listener.commandAction(MenuEngine.FILES, Gome.singleton.mainCanvas);
  }

  public void failed(Exception reason) {
    Util.messageBox(Gome.singleton.bundle.getString("ui.failure"), Gome.singleton.bundle.getString(reason.getMessage()), AlertType.ERROR); //$NON-NLS-1$
  }
}