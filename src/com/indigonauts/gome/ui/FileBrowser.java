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
import com.indigonauts.gome.io.StoreFileEntry;
import com.indigonauts.gome.sgf.Board;
import com.indigonauts.gome.sgf.SgfPoint;

public class FileBrowser implements CommandListener, Showable, IndexLoaderCallback {
    private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("FileBrowser");

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

    private boolean managementMode;

    private Display display;

    public static Command OPEN;

    public static Command DELETE;

    public static Command IMPORT;

    public static Command SEND_BY_EMAIL;

    private static Command RANDOM;

    private static Image DIR;

    private static Image REMOTE_DIR;

    private static Image FILE;

    private static Image REMOTE_FILE;

    private static final int ILLUSTRATIVE_SIZE = 64;

    private static final GraphicRectangle ILLUSTRATIVE_RECTANGLE = new GraphicRectangle(1, 1, ILLUSTRATIVE_SIZE - 2,
                                                                                        ILLUSTRATIVE_SIZE - 2);

    static {
        try {
            DIR = Image.createImage("/dir.png"); //$NON-NLS-1$
            REMOTE_DIR = Image.createImage("/rdir.png"); //$NON-NLS-1$
            FILE = Image.createImage("/file.png"); //$NON-NLS-1$
            REMOTE_FILE = Image.createImage("/rfile.png"); //$NON-NLS-1$
        } catch (IOException e) {
            // ignore, we can't do anything
        }
    }

    public FileBrowser(Showable parent, MenuEngine listener, boolean managementMode) {
        log.debug("Create FileBrowser");
        OPEN = new Command(Gome.singleton.bundle.getString("ui.open"), Command.SCREEN, 2); //$NON-NLS-1$

        DELETE = new Command(Gome.singleton.bundle.getString("ui.delete"), Command.SCREEN, 3); //$NON-NLS-1$

        IMPORT = new Command(Gome.singleton.bundle.getString("ui.import"), Command.SCREEN, 2); //$NON-NLS-1$

        SEND_BY_EMAIL = new Command(Gome.singleton.bundle.getString("ui.sendByEmail"), Command.SCREEN, 2); //$NON-NLS-1$

        RANDOM = new Command(Gome.singleton.bundle.getString("ui.random"), Command.SCREEN, 2); //$NON-NLS-1$

        this.managementMode = managementMode;
        this.parent = parent;
        this.listener = listener;

        reset();
    }

    public FileBrowser(Showable parent, MenuEngine listener, Vector entries, boolean managementMode) {
        this(parent, listener, managementMode);
        this.entries = entries;
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
        uiFolder = new List(Gome.singleton.bundle.getString("ui.fileselect"), Choice.IMPLICIT);

        Enumeration all = entries.elements();
        while (all.hasMoreElements()) {
            FileEntry current = (FileEntry) all.nextElement();
            log.debug("Current Element = " + current.getPath());
            if (managementMode && !(current instanceof StoreFileEntry))
                continue;
            Image image = null;
            if (current instanceof CollectionEntry) {
                CollectionEntry file = (CollectionEntry) current;
                int fileNum = file.getCollectionSize();
                log.debug("Collection Size = " + fileNum);
                String description = file.getDescription();
                log.debug("Description = " + description);
                if (file.hasAnIllustration()) {
                    image = generateIllustrativePosition(file.getIllustrativeBoardArea(), file
                            .getIllustrativeBlackPosition(), file.getIllustrativeWhitePosition());
                } else
                    image = file.isRemote() ? REMOTE_FILE : FILE;
                uiFolder.append(description + (fileNum != 1 ? " [" + fileNum + "]" : ""), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                image);
            } else if (current instanceof IndexEntry) {
                IndexEntry file = (IndexEntry) current;
                if (file.hasAnIllustration()) {
                    image = generateIllustrativePosition(file.getIllustrativeBoardArea(), file
                            .getIllustrativeBlackPosition(), file.getIllustrativeWhitePosition());
                } else
                    image = file.isRemote() ? REMOTE_DIR : DIR;

                uiFolder.append(file.getDescription(), image);

            }

        }
        uiFolder.addCommand(MenuEngine.BACK);

        if (managementMode) {
            uiFolder.addCommand(DELETE);
            String email = Gome.singleton.options.email;
            if (email != null && email.length() != 0)
                uiFolder.addCommand(SEND_BY_EMAIL);
        } else {
            uiFolder.addCommand(OPEN);
            uiFolder.addCommand(IMPORT);
        }
        uiFolder.setCommandListener(this);
        display = disp;
        display.setCurrent(uiFolder);
    }

    private Image generateIllustrativePosition(String boardArea, String black, String white) {
        StringVector blackPoints = new StringVector(black, ';');
        StringVector whitePoints = new StringVector(white, ';');
        Image generated = Image.createImage(ILLUSTRATIVE_SIZE, ILLUSTRATIVE_SIZE);
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
        BoardPainter bp = new BoardPainter(position, ILLUSTRATIVE_RECTANGLE, new Rectangle(SgfPoint
                .createFromSgf((String) boardAreaSplitted.elementAt(0)), SgfPoint
                .createFromSgf((String) boardAreaSplitted.elementAt(1))));

        Graphics g = generated.getGraphics();
        g.setColor(Util.COLOR_LIGHTGREY);
        g.drawRect(0, 0, ILLUSTRATIVE_SIZE - 1, ILLUSTRATIVE_SIZE - 1);
        bp.drawBoard(g);

        return Image.createImage(generated);

    }

    public void commandAction(Command c, Displayable s) {

        if (s == uiFolder) {

            if (c == OPEN || (!managementMode && c == List.SELECT_COMMAND)) {

                indexFolder = uiFolder.getSelectedIndex();
                Object entry = entries.elementAt(indexFolder);

                if (entry instanceof IndexEntry) {
                    IndexEntry index = (IndexEntry) entry;
                    new IndexLoader(index.getPath(), this).show(Gome.singleton.display);
                    return;

                } else if (entry instanceof CollectionEntry && ((CollectionEntry) entry).getCollectionSize() == 1) {
                    indexBlock = 0;
                    selectedNum = 0;

                    listener.loadFile((CollectionEntry) entry, 0);
                    return;
                }
                showFileBlock();

            } else if (c == MenuEngine.BACK) {
                parent.show(display);
                parent = null;
            } else if (c == DELETE) {
                indexFolder = uiFolder.getSelectedIndex();
                Object obj = entries.elementAt(indexFolder);
                StoreFileEntry entry = (StoreFileEntry) obj;
                listener.deleteFile(entry);
                entries.removeElementAt(indexFolder);

                show(Gome.singleton.display);
            } else if (c == IMPORT) {
                FileEntry selectedFile = getSelectedFile();
                importFile(selectedFile);
            } else if (c == SEND_BY_EMAIL) {
                indexFolder = uiFolder.getSelectedIndex();
                Object obj = entries.elementAt(indexFolder);
                IOManager.sendFileByMail((FileEntry) obj, Gome.singleton.options.email);
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
    }

    /**
     * i.e. 1-20, 21-40, 41-55...
     */
    private void showFileBlock() {

        uiFileBlock = new List(getSelectedFile().getPath(), Choice.IMPLICIT);

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
        uiFile = new List(getSelectedFile().getPath(), Choice.IMPLICIT);
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
        if (selectedFile instanceof StoreFileEntry || selectedFile instanceof BundledFileEntry) {
            typeok = false;
        } else if (selectedFile instanceof IndexEntry) {
            IndexEntry ie = (IndexEntry) selectedFile;
            if (!ie.getPath().startsWith("http://")) //$NON-NLS-1$
                typeok = false;
        }
        if (!typeok) {
            Util.messageBox("ui.error", Gome.singleton.bundle.getString("ui.error.onlyOnline"), AlertType.ERROR); //$NON-NLS-1$ //$NON-NLS-2$
            return;
        }

        FileImporter fileLoader = new FileImporter(new ImportCallback(this), selectedFile);
        fileLoader.show(display);
        fileLoader.start();
    }

    public void downloadFinished(Vector files) {
        log.debug("Index Download finished");
        FileBrowser son = new FileBrowser(this, listener, files, managementMode);
        log.debug("Show File Browser");
        son.show(Gome.singleton.display);
    }

    public void downloadFailure(Exception reason) {
        Util.messageBox(Gome.singleton.bundle.getString("ui.failure"), reason.getMessage(), AlertType.ERROR); //$NON-NLS-1$
    }
}