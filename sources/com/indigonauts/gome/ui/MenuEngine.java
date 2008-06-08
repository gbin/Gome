package com.indigonauts.gome.ui;

import java.io.IOException;
import java.util.Calendar;

import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextField;
import javax.microedition.rms.RecordStoreException;

import org.apache.log4j.LogCanvas;
import org.apache.log4j.Logger;

import com.indigonauts.gome.Gome;
import com.indigonauts.gome.common.QuickSortable;
import com.indigonauts.gome.common.Util;
import com.indigonauts.gome.i18n.I18N;
import com.indigonauts.gome.io.CollectionEntry;
import com.indigonauts.gome.io.FileEntry;
import com.indigonauts.gome.io.IOManager;
import com.indigonauts.gome.io.IndexEntry;
import com.indigonauts.gome.io.LocalFileEntry;
import com.indigonauts.gome.multiplayer.Challenge;
import com.indigonauts.gome.multiplayer.Game;
import com.indigonauts.gome.multiplayer.User;
import com.indigonauts.gome.multiplayer.bt.BluetoothClientConnector;
import com.indigonauts.gome.multiplayer.igs.IGSConnector;
import com.indigonauts.gome.sgf.Board;

public class MenuEngine implements CommandListener {
  //#ifdef DEBUG
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("MenuEngine");
  //#endif

  private final GameController gc;

  // constants
  public static final Command NEXT = new Command(I18N.nextInCollection, Command.SCREEN, 1);
  public static final Command PREVIOUS = new Command(I18N.previousInCollection, Command.SCREEN, 1);
  public static final Command NEW = new Command(I18N.new_, Command.SCREEN, 2);
  public static final Command FILES = new Command(I18N.fileselect, Command.SCREEN, 2);
  public static final Command SAVE = new Command(I18N.save, Command.SCREEN, 2);
  public static final Command PLAY_MODE = new Command(I18N.playMode, Command.SCREEN, 5);
  public static final Command PASS = new Command(I18N.pass, Command.SCREEN, 5);
  public static final Command LAST_MOVE = new Command(I18N.lastMove, Command.SCREEN, 5);
  public static final Command FIRST_MOVE = new Command(I18N.firstMove, Command.SCREEN, 5);
  public static final Command REVIEW_MODE = new Command(I18N.reviewMode, Command.SCREEN, 5);

  //#ifdef BT
  public static final Command BT_CONNECT = new Command(I18N.bt.connect, Command.SCREEN, 2);
  public static final Command BT_CHALLENGE = new Command(I18N.online.challenge, Command.SCREEN, 2);
  public static final Command BT_DISCONNECT = new Command(I18N.bt.disconnect, Command.SCREEN, 2);
  //#endif

  //#ifdef IGS
  public static final Command IGS_CONNECT = new Command(I18N.online.connect, Command.SCREEN, 5);
  public static final Command IGS_GAMELIST = new Command(I18N.online.gameList, Command.SCREEN, 5);
  public static final Command IGS_USERLIST = new Command(I18N.online.userlist, Command.SCREEN, 5);
  public static final Command IGS_DISCONNECT = new Command(I18N.online.disconnect, Command.SCREEN, 5);
  public static final Command IGS_OBSERVE = new Command(I18N.online.observe, Command.SCREEN, 5);
  public static final Command IGS_CHALLENGE = new Command(I18N.online.challenge, Command.SCREEN, 5);
  public static final Command IGS_DECLINE = new Command(I18N.online.decline, Command.SCREEN, 5);
  public static final Command IGS_MESSAGE = new Command(I18N.online.message, Command.SCREEN, 2);
  public static final Command IGS_SORT_BY_RANK = new Command(I18N.online.sortRank, Command.SCREEN, 5);
  public static final Command IGS_SORT_BY_NICK = new Command(I18N.online.sortNick, Command.SCREEN, 5);
  public static final Command IGS_SORT_BY_WATCH = new Command(I18N.online.sortWatch, Command.SCREEN, 5);

  public static final Command IGS_DONE_SCORE = new Command(I18N.done, Command.SCREEN, 5);
  public static final Command IGS_REQUEST_KOMI = new Command(I18N.online.requestKomi, Command.SCREEN, 5);
  public static final Command IGS_CHANGE_HANDICAP = new Command(I18N.online.changeHandicap, Command.SCREEN, 5);
  public static final Command IGS_RESET_DEAD_STONES = new Command(I18N.count.undoMarkDeadStone, Command.SCREEN, 5);
  //#endif

  public static final Command GAME_STATUS = new Command(I18N.gameStatus, Command.SCREEN, 8); //$NON-NLS-1$
  public static final Command OPTIONS = new Command(I18N.options, Command.SCREEN, 8); //$NON-NLS-1$

  //#ifdef DEBUG
  public static final Command CONSOLE = new Command("Console", Command.SCREEN, 9); //$NON-NLS-1$
  //#endif
  public static final Command HELP = new Command(I18N.help.help, Command.SCREEN, 9); //$NON-NLS-1$
  public static final Command BACK = new Command(I18N.back, Command.BACK, 0); // BACK
  public static final Command OK = new Command(I18N.ok, Command.BACK, 5); // BACK
  public static final Command START = new Command(I18N.start, Command.SCREEN, 10); //$NON-NLS-1$
  public static final Command EXIT = new Command(I18N.exit, Command.EXIT, 9); // EXIT

  public static final Command FINISHED_COUNTING = new Command(I18N.count.endCounting, Command.SCREEN, 4); //$NON-NLS-1$
  public static final Command EVALUATE = new Command(I18N.count.evaluate, Command.SCREEN, 5); //$NON-NLS-1$
  public static final Command ACCEPT = new Command(I18N.accept, Command.SCREEN, 5); //$NON-NLS-1$
  public static final Command DECLINE = new Command(I18N.online.decline, Command.SCREEN, 5); //$NON-NLS-1$
  public static final Command RESIGN = new Command(I18N.resign, Command.SCREEN, 5); //$NON-NLS-1$
  public static final Command REQUEST = new Command(I18N.request, Command.SCREEN, 5); //$NON-NLS-1$

  public static final Command COMMENT = new Command(I18N.comment, Command.SCREEN, 5); //$NON-NLS-1$
  public static final Command EDIT_NODE = new Command(I18N.editNode, Command.SCREEN, 5); //$NON-NLS-1$
  public static final Command ZOOM = new Command(I18N.zoom, Command.SCREEN, 5); //$NON-NLS-1$
  public static final Command UNDO = new Command(I18N.undo, Command.SCREEN, 5); //$NON-NLS-1$
  public static final Command HINT = new Command(I18N.hint, Command.SCREEN, 5); //$NON-NLS-1$
  public static final Command NEXT10MOVES = new Command(I18N.next10Moves, Command.SCREEN, 5); //$NON-NLS-1$
  public static final Command PREV10MOVES = new Command(I18N.prev10Moves, Command.SCREEN, 5); //$NON-NLS-1$

  private static final Font FIXED_FONT = Font.getFont(Font.FACE_MONOSPACE, Font.STYLE_PLAIN, Font.SIZE_SMALL);


  private FileBrowser fileBrowser;

  private Options optionsForm = null;

  private Form newGameForm;
  private Form saveGameForm;

  private EditNodeForm editNodeForm;

  private List igsGameList;
  private ChoiceGroup newGameSize, setKomi;
  private TextField newGameHandicap, newGameKomi;
  public TextField gameFileName;

  //#ifdef DEBUG
  private LogCanvas logCanvas;
  //#endif

  private Form challengeForm;
  private Form oppRequestKomiForm;
  private Form requestOnlineKomiForm;
  private Form onlineChangeHandicapForm;
  private Chat chat;

  private List igsUserList;
  private Challenge currentChallenge;

  private byte komi;

  private List btPeerList;

  public MenuEngine(GameController gc) {
    this.gc = gc;
  }

  public Form createNewGameMenu() {
    Form game = new Form(I18N.new_); //$NON-NLS-1$

    newGameSize = new ChoiceGroup(I18N.goban, Choice.EXCLUSIVE); //$NON-NLS-1$
    newGameSize.append("9", null); //$NON-NLS-1$
    newGameSize.append("13", null); //$NON-NLS-1$
    newGameSize.append("19", null); //$NON-NLS-1$
    boolean[] flagsNew = { false, false, true };
    newGameSize.setSelectedFlags(flagsNew);
    game.append(newGameSize);
    newGameHandicap = new TextField(I18N.handicap, "0", 1, TextField.NUMERIC); //$NON-NLS-1$ //$NON-NLS-2$
    game.append(newGameHandicap);
    game.addCommand(BACK);
    game.addCommand(START);
    game.setCommandListener(this);

    return game;
  }

  public Form createSaveGameMenu(CommandListener cmd, String name, String defaultName) {

    String defaultFN;
    if (defaultName != null) {
      defaultFN = defaultName;
    } else {
      Calendar cal = Calendar.getInstance();
      int y = cal.get(Calendar.YEAR);
      int m = cal.get(Calendar.MONTH) + 1;
      int d = cal.get(Calendar.DAY_OF_MONTH);
      defaultFN = I18N.defaultFilename + String.valueOf(y) + (m < 10 ? "0" + m : String.valueOf(m)) + (d < 10 ? "0" + d : String.valueOf(d)) + ".sgf";
    }
    Form createForm = new Form(Util.expandString(I18N.saveIn, new String[] { name })); //$NON-NLS-1$
    gameFileName = new TextField(I18N.filename, defaultFN, 28, TextField.ANY); //$NON-NLS-1$
    createForm.append(gameFileName);
    createForm.addCommand(BACK);
    createForm.addCommand(SAVE);
    createForm.setCommandListener(cmd);
    return createForm;
  }

  public void updateLastBrowser(FileBrowser browser) {
    fileBrowser = browser;
  }

  public void commandAction(Command c, Displayable d) {
    try {
      Gome.singleton.mainCanvas.setSplashInfo(null);
      if (d == Gome.singleton.mainCanvas) {
        if (c == NEW) {
          if (gc.getPlayMode() != GameController.ONLINE_MODE) {
            //#ifdef DEBUG
            log.info("Game Mode: " + gc.getPlayMode());
            //#endif
            newGameForm = createNewGameMenu();
            Gome.singleton.display.setCurrent(newGameForm);
          }
        } else if (c == FILES) {
          try {
            if (fileBrowser == null) {
              fileBrowser = new FileBrowser(Gome.singleton.mainCanvas, this, IOManager.singleton.getRootBundledGamesList(), "/", false);
            }
            fileBrowser.show(Gome.singleton.display);
          } catch (IOException e1) {
            Util.messageBox(I18N.error.error, e1.getMessage(), AlertType.ERROR); //$NON-NLS-1$
          }
        }
        //#ifdef IGS
        else if (c == IGS_CONNECT) {
          gc.connectToIGS();
        } else if (c == IGS_GAMELIST) {
          gc.getServerGameList();
        } else if (c == IGS_USERLIST) {
          gc.getServerUserList();
        } else if (c == IGS_DISCONNECT) {
          gc.disconnectFromServer();
        } else if (c == IGS_MESSAGE) {
          chat.sendMessage(currentChallenge != null ? currentChallenge.nick : "TODO", null, null, Gome.singleton.mainCanvas);
        } else if (c == IGS_REQUEST_KOMI) {
          gomeOnlineWantKomi();
        } else if (c == IGS_CHANGE_HANDICAP) {
          gomeOnlineChangeHandicap();
        }
        //#endif
        //#ifdef BT
        else if (c == BT_CONNECT) {
          gc.connectToBT();
        } else if (c == BT_CHALLENGE) {
          gc.challengeBT();
        }
        //#endif
        else if (c == NEXT) {
          gc.loadAndPlayNextInCollection(false);
        } else if (c == PREVIOUS) {
          gc.loadAndPlayNextInCollection(true);
        } else if (c == FIRST_MOVE) {
          gc.goToFirstMove();
        } else if (c == LAST_MOVE) {
          gc.goToLastMove();
        } else if (c == EVALUATE) {
          gc.startCountMode(true);
        } else if (c == FINISHED_COUNTING) {
          gc.doScore();
        } else if (c == PASS) {
          gc.pass();
        } else if (c == SAVE) {
          FileBrowser fb = new FileBrowser(Gome.singleton.mainCanvas, this, IOManager.singleton.getRootBundledGamesList(), "/", true);
          new IndexLoader(new IndexEntry(IOManager.LOCAL_NAME, null, ""), fb).show(Gome.singleton.display);

        } else if (c == OPTIONS) {
          optionsForm = new Options(I18N.options, this, false);
          Gome.singleton.display.setCurrent(optionsForm);
        }

        //#ifdef DEBUG
        else if (c == CONSOLE) {
          logCanvas = Logger.getLogCanvas();
          logCanvas.addCommand(BACK);
          logCanvas.setCommandListener(this);
          Gome.singleton.display.setCurrent(logCanvas);
        }
        //#endif
        else if (c == COMMENT) {
          gc.doCycleBottom();
          gc.tuneBoardPainter();
          Gome.singleton.mainCanvas.refresh();
        } else if (c == ZOOM) {
          gc.setZoomIn(!gc.isZoomIn());
          gc.tuneBoardPainter();
          Gome.singleton.mainCanvas.refresh();
        } else if (c == UNDO) {
          if (gc.doUndo()) {
            gc.tuneBoardPainter();
            Gome.singleton.mainCanvas.refresh();
          }
        } else if (c == NEXT10MOVES) {
          gc.do10NextMoves();
          Gome.singleton.mainCanvas.refresh();
        } else if (c == PREV10MOVES) {
          gc.do10PrevMoves();
          Gome.singleton.mainCanvas.refresh();
        }

        else if (c == HINT) {
          gc.reverseShowHint();
          gc.tuneBoardPainter();
          Gome.singleton.mainCanvas.refresh();
        } else if (c == EDIT_NODE) {
          editNodeForm = new EditNodeForm(null, Gome.singleton.gameController.getBoard(), Gome.singleton.gameController.getCurrentNode());
          editNodeForm.addCommand(OK);
          editNodeForm.addCommand(BACK);
          editNodeForm.setCommandListener(this);
          editNodeForm.show(Gome.singleton.display);
        }

        else if (c == HELP) {
          //#ifdef DEBUG
          log.debug("help");
          //#endif
          Info help = new Info(Gome.singleton.mainCanvas);
          help.show(Gome.singleton.display);
        } else if (c == MenuEngine.GAME_STATUS) {
          Info help = new Info(Gome.singleton.mainCanvas, MenuEngine.GAME_STATUS);
          help.show(Gome.singleton.display);
        }

        else if (c == EXIT) {
          Gome.singleton.notifyDestroyed();
        } else if (c == RESIGN) {
          gc.resign();
        }
        //#ifdef IGS
        else if (c == IGS_RESET_DEAD_STONES) {
          gc.gomeRestoreGameForCounting();
        } else if (c == IGS_DONE_SCORE) {
          gc.doneWithScore();
        }
        //#endif
      } else if (d == optionsForm) {
        if (c == SAVE) {
          if (!optionsForm.save())
            return;
          gc.setPlayMode(gc.getPlayMode());
        }
        optionsForm = null;
        Gome.singleton.mainCanvas.show(Gome.singleton.display);

      } else if (d == newGameForm) {

        if (c == START) {
          startNewGame();
        }
        newGameForm = null;
        Gome.singleton.mainCanvas.show(Gome.singleton.display);

      } else if (d == saveGameForm) {
        if (c == SAVE) {
          try {
            IOManager.singleton.saveLocalGame(gameFileName.getString(), gc.getSgfModel());
          } catch (RecordStoreException e) {
            Util.messageBox(I18N.error.error, e.getMessage(), AlertType.ERROR); //$NON-NLS-1$
          }
        }
        saveGameForm = null;
        Gome.singleton.mainCanvas.show(Gome.singleton.display);
      }

      //#ifdef BT
      else if (d == btPeerList) {
        if (c == BT_CONNECT || c == List.SELECT_COMMAND) {
          log.debug("Connecting to BT");
          ((BluetoothClientConnector) gc.multiplayerConnector).connectToPeer(btPeerList.getSelectedIndex());
        }
        btPeerList = null;
        Gome.singleton.mainCanvas.show(Gome.singleton.display);
      }
      //#endif

      //#ifdef IGS
      else if (d == igsGameList) {
        if (c == IGS_OBSERVE || c == List.SELECT_COMMAND) {
          gc.observeServerGame(igsGameList.getSelectedIndex());
        }
        igsGameList = null;
        Gome.singleton.mainCanvas.show(Gome.singleton.display);
      } else if (d == igsUserList) {
        if (c == IGS_CHALLENGE || c == List.SELECT_COMMAND) {
          gc.challengeServerUser(igsUserList.getSelectedIndex());
          igsUserList = null;

        } else if (c == IGS_MESSAGE) {
          chat.sendMessage(gc.getNick(igsUserList.getSelectedIndex()), "", "", Gome.singleton.mainCanvas);
          return;
        } else if (c == IGS_SORT_BY_RANK) {
          if (User.sortCriteria == User.RANK) {
            User.sortOrder = !User.sortOrder;
          } else {
            User.sortCriteria = User.RANK;
          }
          refreshUserList(((IGSConnector) gc.multiplayerConnector).getUserList());
          return;
        } else if (c == IGS_SORT_BY_NICK) {
          if (User.sortCriteria == User.NICK) {
            User.sortOrder = !User.sortOrder;
          } else {
            User.sortCriteria = User.NICK;
          }
          refreshUserList(((IGSConnector) gc.multiplayerConnector).getUserList());
          return;
        } else if (c == IGS_SORT_BY_WATCH) {
          if (User.sortCriteria == User.WATCH) {
            User.sortOrder = !User.sortOrder;
          } else {
            User.sortCriteria = User.WATCH;
          }
          refreshUserList(((IGSConnector) gc.multiplayerConnector).getUserList());
          return;
        }
        igsUserList = null;
        Gome.singleton.mainCanvas.show(Gome.singleton.display);

      } else if (d == challengeForm) {
        if (c == ACCEPT) {
          gc.acceptChallenge(currentChallenge);
        } else if (c == IGS_DECLINE) {
          gc.declineChallenge(currentChallenge);
        }
        challengeForm = null;
        Gome.singleton.mainCanvas.show(Gome.singleton.display);
      } else if (d == onlineChangeHandicapForm) {
        if (c == ACCEPT) {
          gc.gomeSetOnlineHandicap(Byte.parseByte(newGameHandicap.getString()));
        }
        onlineChangeHandicapForm = null;
        Gome.singleton.mainCanvas.show(Gome.singleton.display);
      } else if (d == oppRequestKomiForm) {
        if (c == ACCEPT) {
          gc.onlineSetKomi(komi);
          gc.setKomi(komi);
        }
        oppRequestKomiForm = null;
        Gome.singleton.mainCanvas.show(Gome.singleton.display);
      } else if (d == requestOnlineKomiForm) {
        if (c == REQUEST) {
          int i = setKomi.getSelectedIndex();
          byte k = Byte.parseByte(newGameKomi.getString());

          gc.gomeWantToSetOnlineKomi((byte) (k * 2), i);
        }
        requestOnlineKomiForm = null;
        Gome.singleton.mainCanvas.show(Gome.singleton.display);
      }
      //#endif
      else if (d == editNodeForm) {
        if (c == OK) {
          gc.setCurrentNodeComment(((EditNodeForm) d).getComment());
        }
        editNodeForm = null;
        Gome.singleton.mainCanvas.show(Gome.singleton.display);
      }
      //#ifdef DEBUG
      else if (d == logCanvas) {
        logCanvas = null;
        Gome.singleton.mainCanvas.show(Gome.singleton.display);
      }
      //#endif
    } catch (Throwable t) {
      //#ifdef DEBUG
      log.error(t);
      logCanvas = Logger.getLogCanvas();
      logCanvas.addCommand(BACK);
      logCanvas.setCommandListener(this);
      Gome.singleton.display.setCurrent(logCanvas);
      //#endif
    }
  }

  //#ifdef BT
  public void showBTPeers(String[] friendyNames) {
    //#ifdef DEBUG
    log.debug("Show bt peers");
    //#endif
    btPeerList = new List(I18N.bt.peerList, Choice.IMPLICIT);
    for (int i = 0; i < friendyNames.length; i++) {
      btPeerList.append(friendyNames[i], null);
    }
    btPeerList.setFitPolicy(Choice.TEXT_WRAP_ON);
    for (int i = 0; i < friendyNames.length; i++)
      btPeerList.setFont(i, FIXED_FONT);
    btPeerList.addCommand(BACK);
    btPeerList.addCommand(BT_CONNECT);
    btPeerList.setCommandListener(this);
    Gome.singleton.display.setCurrent(btPeerList);
  }

  //#endif

  //#ifdef IGS
  public void showIgsGameList(Game[] games) {
    //#ifdef DEBUG
    log.debug("Show igs gamelist");
    //#endif
    igsGameList = new List(I18N.online.gameList, Choice.IMPLICIT);

    for (int i = 0; i < games.length; i++) {
      igsGameList.append(games[i].toString(), null);
    }
    igsGameList.setFitPolicy(Choice.TEXT_WRAP_ON);
    for (int i = 0; i < igsGameList.size(); i++)
      igsGameList.setFont(i, FIXED_FONT);
    igsGameList.addCommand(BACK);
    igsGameList.addCommand(IGS_OBSERVE);
    igsGameList.setCommandListener(this);
    Gome.singleton.display.setCurrent(igsGameList);
  }

  public void refreshUserList(User[] users) {
    //#ifdef DEBUG
    log.debug("refreshUserList");
    //#endif
    QuickSortable.quicksort(users);
    if (igsUserList != null) {
      igsUserList.deleteAll();
      for (int i = 0; i < users.length; i++) {
        igsUserList.append(users[i].toString(), null);

      }
      for (int i = 0; i < igsUserList.size(); i++)
        igsUserList.setFont(i, FIXED_FONT);
    }

  }

  private void setUpUserList() {
    igsUserList = new List(I18N.online.userlist, Choice.IMPLICIT);
    igsUserList.addCommand(BACK);
    igsUserList.addCommand(IGS_CHALLENGE);
    igsUserList.addCommand(IGS_MESSAGE);
    igsUserList.addCommand(IGS_SORT_BY_RANK);
    igsUserList.addCommand(IGS_SORT_BY_NICK);
    igsUserList.addCommand(IGS_SORT_BY_WATCH);
    igsUserList.setCommandListener(this);
  }

  public void showIgsUserList(User[] users) {
    //#ifdef DEBUG
    log.debug("Show igs userlist");
    //#endif
    setUpUserList();
    refreshUserList(users);
    Gome.singleton.display.setCurrent(igsUserList);
  }

  //#endif

  public void startNewGame() {
    byte size = 19;
    int handi = 0;

    try {
      size = Byte.parseByte(newGameSize.getString(newGameSize.getSelectedIndex()));
      handi = Integer.parseInt(newGameHandicap.getString());
    } catch (Exception e) {
      size = 19;
      handi = 0;
    }
    gc.newGame(size, handi, GameController.GAME_MODE);
  }

  public void loadFile(CollectionEntry file, int filenum) {
    gc.reset(Gome.singleton.mainCanvas, GameController.GAME_MODE);
    gc.loadAndPlay(file, filenum);
  }

  public void deleteFile(FileEntry file) {
    if (file instanceof LocalFileEntry) {
      //#ifdef JSR75
      IOManager.singleton.deleteJSR75(file.getUrl());
      //#else

      //# try {
      //#   IOManager.singleton.deleteLocalStore(file.getName());
      //# } catch (RecordStoreException e) {
      //#   Util.messageBox(I18N.error.error, I18N.error.delete, AlertType.ERROR); //$NON-NLS-1$ //$NON-NLS-2$
      //# }
      //#endif
    } else {
      Util.messageBox(I18N.error.error, I18N.error.wrongtype, AlertType.ERROR); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  //#ifdef IGS
  public void showIgsChallenge(Challenge challenge) {
    currentChallenge = challenge;
    String colors = ((challenge.color == Board.BLACK) ? I18N.game.blackLong : I18N.game.whiteLong);
    challengeForm = new Form(challenge.nick + I18N.online.challengesYou);
    String byoORnot = ((challenge.min_per25moves != -1) ? " + " + challenge.min_per25moves + I18N.clock.min25stones : I18N.online.noByo);
    String args[] = { colors, String.valueOf(challenge.size), String.valueOf(challenge.size), String.valueOf(challenge.time_minutes), byoORnot };
    String message = Util.expandString(I18N.online.challengeMessage, args);

    challengeForm.append(message);
    challengeForm.addCommand(ACCEPT);
    challengeForm.addCommand(IGS_DECLINE);
    challengeForm.setCommandListener(this);
    Gome.singleton.display.setCurrent(challengeForm);
  }

  public void showOppWantKomi(byte k) {
    this.komi = k;
    oppRequestKomiForm = new Form(I18N.online.komiChangeForm);
    String temp = Util.komi2String(k);
    oppRequestKomiForm.append(I18N.online.opponentWantsToChangeKomi + temp);
    oppRequestKomiForm.addCommand(ACCEPT);
    oppRequestKomiForm.addCommand(DECLINE);
    oppRequestKomiForm.setCommandListener(this);
    Gome.singleton.display.setCurrent(oppRequestKomiForm);
  }

  public void switchToOffline() {
    // some cleanup after disconnection
    chat = null;
  }

  public void switchToOnline() {
    chat = new Chat(Gome.singleton.display);
  }

  public void incomingChatMessage(String nick, String message) {
    chat.addMessage(nick, message);
    chat.showMessageHistory();
  }

  public void gomeOnlineWantKomi() {
    requestOnlineKomiForm = new Form(I18N.online.komiChangeForm);
    newGameKomi = new TextField(I18N.komi, "5", 2, TextField.NUMERIC); //$NON-NLS-1$ //$NON-NLS-2$        
    requestOnlineKomiForm.append(I18N.online.youWantToChangeKomi);

    requestOnlineKomiForm.addCommand(REQUEST);
    requestOnlineKomiForm.addCommand(BACK);

    setKomi = new ChoiceGroup(I18N.komi, Choice.EXCLUSIVE); //$NON-NLS-1$
    setKomi.append(I18N.online.blackGives, null); //$NON-NLS-1$
    setKomi.append(I18N.online.whiteGives, null); //$NON-NLS-1$        
    boolean[] flagsNew = { true, false };
    setKomi.setSelectedFlags(flagsNew);
    requestOnlineKomiForm.append(setKomi);
    requestOnlineKomiForm.append(newGameKomi);
    requestOnlineKomiForm.setCommandListener(this);

    Gome.singleton.display.setCurrent(requestOnlineKomiForm);
  }

  public void gomeOnlineChangeHandicap() {
    onlineChangeHandicapForm = new Form(I18N.online.handicapChangeForm);
    onlineChangeHandicapForm.append(I18N.online.youWantToChangeHandicap);

    onlineChangeHandicapForm.addCommand(ACCEPT);
    onlineChangeHandicapForm.addCommand(BACK);

    newGameHandicap = new TextField(I18N.handicap, "2", 1, TextField.NUMERIC);
    onlineChangeHandicapForm.append(newGameHandicap);
    onlineChangeHandicapForm.setCommandListener(this);

    Gome.singleton.display.setCurrent(onlineChangeHandicapForm);
  }
  //#endif

}
