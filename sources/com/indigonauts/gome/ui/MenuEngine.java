package com.indigonauts.gome.ui;

import java.io.IOException;

import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextField;
import javax.microedition.rms.RecordStoreException;

import org.apache.log4j.LogCanvas;
import org.apache.log4j.Logger;

import com.indigonauts.gome.Gome;
import com.indigonauts.gome.MainCanvas;
import com.indigonauts.gome.common.Util;
import com.indigonauts.gome.igs.ServerChallenge;
import com.indigonauts.gome.igs.ServerGame;
import com.indigonauts.gome.igs.ServerUser;
import com.indigonauts.gome.io.CollectionEntry;
import com.indigonauts.gome.io.FileEntry;
import com.indigonauts.gome.io.IOManager;
import com.indigonauts.gome.io.StoreFileEntry;
import com.indigonauts.gome.sgf.Board;

public class MenuEngine implements CommandListener {
  //#ifdef DEBUG
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("MenuEngine");
  //#endif

  GameController gc;

  // constants
  public static Command NEXT;
  public static Command NEW;
  public static Command FILES;
  public static Command SAVE;
  public static Command PLAY_MODE;
  public static Command PASS;
  public static Command LAST_MOVE;
  public static Command FIRST_MOVE;
  public static Command REVIEW_MODE;

  //#ifdef IGS
  public static Command IGS_CONNECT;
  public static Command IGS_GAMELIST;
  public static Command IGS_USERLIST;
  public static Command IGS_DISCONNECT;
  public static Command IGS_OBSERVE;
  public static Command IGS_CHALLENGE;
  public static Command IGS_DECLINE;
  public static Command IGS_MESSAGE;
  //#endif

  public static Command REQUEST;
  public static Command REQUEST_KOMI;
  public static Command CHANGE_ONLINE_HANDICAP;
  public static Command IGS_RESET_DEADS_TONE;
  public static Command IGS_DONE_SCORE;
  public static Command GAME_STATUS;
  public static Command OPTIONS;
  //#ifdef DEBUG
  public static Command CONSOLE;
  //#endif
  public static Command HELP;
  public static Command BACK;
  public static Command ABOUT;
  public static Command START;
  public static Command EXIT;
  public static Command FINISHED_COUNTING;
  public static Command ACCEPT;
  public static Command DECLINE;
  public static Command RESIGN;

  MainCanvas mainCanvas;

  FileBrowser fileBrowser;

  Options optionsForm = null;

  Form newGameForm;
  Form saveGameForm;
  List igsGameList;
  ChoiceGroup newGameSize, setKomi;
  TextField newGameHandicap, newGameKomi;
  TextField url;
  TextField gameFileName;

  //#ifdef DEBUG
  LogCanvas logCanvas;
  //#endif

  Form challengeForm;
  Form oppRequestKomiForm;
  Form requestOnlineKomiForm;
  Form onlineChangeHandicapForm;
  Chat chat;

  private List igsUserList;
  private ServerChallenge currentChallenge;

  private byte komi;

  static {
    NEXT = new Command(Gome.singleton.bundle.getString("ui.nextInCollection"), Command.SCREEN, 1); //$NON-NLS-1$
    NEW = new Command(Gome.singleton.bundle.getString("ui.new"), Command.SCREEN, 2); //$NON-NLS-1$
    FILES = new Command(Gome.singleton.bundle.getString("ui.fileselect"), Command.SCREEN, 2); //$NON-NLS-1$
    SAVE = new Command(Gome.singleton.bundle.getString("ui.saveSGF"), Command.SCREEN, 2); //$NON-NLS-1$
    PLAY_MODE = new Command(Gome.singleton.bundle.getString("ui.playMode"), Command.SCREEN, 5); //$NON-NLS-1$
    PASS = new Command(Gome.singleton.bundle.getString("ui.pass"), Command.SCREEN, 5); //$NON-NLS-1$
    LAST_MOVE = new Command(Gome.singleton.bundle.getString("ui.lastMove"), Command.SCREEN, 5); //$NON-NLS-1$
    FIRST_MOVE = new Command(Gome.singleton.bundle.getString("ui.firstMove"), Command.SCREEN, 5); //$NON-NLS-1$
    REVIEW_MODE = new Command(Gome.singleton.bundle.getString("ui.reviewMode"), Command.SCREEN, 5); //$NON-NLS-1$

    //#ifdef IGS
    IGS_CONNECT = new Command(Gome.singleton.bundle.getString("online.connect"), Command.SCREEN, 5); //$NON-NLS-1$
    IGS_GAMELIST = new Command(Gome.singleton.bundle.getString("online.gamelist"), Command.SCREEN, 5); //$NON-NLS-1$
    IGS_USERLIST = new Command(Gome.singleton.bundle.getString("online.userlist"), Command.SCREEN, 5); //$NON-NLS-1$
    IGS_DISCONNECT = new Command(Gome.singleton.bundle.getString("online.disconnect"), Command.SCREEN, 5); //$NON-NLS-1$
    IGS_OBSERVE = new Command(Gome.singleton.bundle.getString("online.observe"), Command.SCREEN, 5); //$NON-NLS-1$
    IGS_CHALLENGE = new Command(Gome.singleton.bundle.getString("online.challenge"), Command.SCREEN, 5); //$NON-NLS-1$
    IGS_DECLINE = new Command(Gome.singleton.bundle.getString("online.decline"), Command.SCREEN, 5); //$NON-NLS-1$
    IGS_MESSAGE = new Command(Gome.singleton.bundle.getString("online.message"), Command.SCREEN, 5); //$NON-NLS-1$
    IGS_DONE_SCORE = new Command(Gome.singleton.bundle.getString("ui.done"), Command.SCREEN, 5);
    REQUEST_KOMI = new Command(Gome.singleton.bundle.getString("online.requestKomi"), Command.SCREEN, 5); //$NON-NLS-1$
    CHANGE_ONLINE_HANDICAP = new Command(Gome.singleton.bundle.getString("online.changeHandicap"), Command.SCREEN, 5); //$NON-NLS-1$
    //#endif

    GAME_STATUS = new Command(Gome.singleton.bundle.getString("ui.gameStatus"), Command.SCREEN, 8); //$NON-NLS-1$
    OPTIONS = new Command(Gome.singleton.bundle.getString("ui.options"), Command.SCREEN, 8); //$NON-NLS-1$

    //#ifdef DEBUG
    CONSOLE = new Command("Console", Command.SCREEN, 9); //$NON-NLS-1$
    //#endif

    HELP = new Command(Gome.singleton.bundle.getString("ui.help"), Command.SCREEN, 9); //$NON-NLS-1$
    BACK = new Command(Gome.singleton.bundle.getString("ui.back"), Command.BACK, 0); //$NON-NLS-1$
    ABOUT = new Command(Gome.singleton.bundle.getString("ui.about"), Command.SCREEN, 9); //$NON-NLS-1$
    START = new Command(Gome.singleton.bundle.getString("ui.start"), Command.SCREEN, 10); //$NON-NLS-1$
    EXIT = new Command(Gome.singleton.bundle.getString("ui.exit"), Command.EXIT, 9); //$NON-NLS-1$

    FINISHED_COUNTING = new Command(Gome.singleton.bundle.getString("count.endCounting"), Command.SCREEN, 5); //$NON-NLS-1$
    ACCEPT = new Command(Gome.singleton.bundle.getString("ui.accept"), Command.SCREEN, 5); //$NON-NLS-1$
    DECLINE = new Command(Gome.singleton.bundle.getString("ui.decline"), Command.SCREEN, 5); //$NON-NLS-1$
    RESIGN = new Command(Gome.singleton.bundle.getString("ui.resign"), Command.SCREEN, 5); //$NON-NLS-1$
    REQUEST = new Command(Gome.singleton.bundle.getString("ui.request"), Command.SCREEN, 5); //$NON-NLS-1$
    IGS_RESET_DEADS_TONE = new Command(Gome.singleton.bundle.getString("count.undoMarkDeadStone"), Command.SCREEN, 5);
  }

  public Form createNewGameMenu() {
    Form game = new Form(Gome.singleton.bundle.getString("ui.new")); //$NON-NLS-1$

    newGameSize = new ChoiceGroup(Gome.singleton.bundle.getString("ui.goban"), Choice.EXCLUSIVE); //$NON-NLS-1$
    newGameSize.append("9", null); //$NON-NLS-1$
    newGameSize.append("13", null); //$NON-NLS-1$
    newGameSize.append("19", null); //$NON-NLS-1$
    boolean[] flagsNew = { false, false, true };
    newGameSize.setSelectedFlags(flagsNew);
    game.append(newGameSize);
    newGameHandicap = new TextField(Gome.singleton.bundle.getString("ui.handicap"), "0", 1, TextField.NUMERIC); //$NON-NLS-1$ //$NON-NLS-2$
    game.append(newGameHandicap);
    game.addCommand(BACK);
    game.addCommand(START);
    game.setCommandListener(this);

    return game;
  }

  public Form createSaveGameMenu() {
    Form createForm = new Form(Gome.singleton.bundle.getString("ui.saveSGF")); //$NON-NLS-1$
    gameFileName = new TextField(Gome.singleton.bundle.getString("ui.filename"), Gome.singleton.bundle //$NON-NLS-1$
            .getString("ui.defaultFilename"), 28, TextField.ANY); //$NON-NLS-1$
    createForm.append(gameFileName);
    createForm.addCommand(BACK);
    createForm.addCommand(SAVE);
    createForm.setCommandListener(this);
    return createForm;
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
            fileBrowser = new FileBrowser(Gome.singleton.mainCanvas, this, IOManager.singleton.getRootBundledGamesList());
            fileBrowser.show(Gome.singleton.display);
          } catch (IOException e1) {
            Util.messageBox(Gome.singleton.bundle.getString("ui.error"), Gome.singleton.bundle.getString(e1.getMessage()), AlertType.ERROR); //$NON-NLS-1$
          }
        } 
        //#ifdef IGS
        else if (c == IGS_CONNECT) {
          gc.connectToServer();
        } else if (c == IGS_GAMELIST) {
          gc.getServerGameList();
        } else if (c == IGS_USERLIST) {
          gc.getServerUserList();
        } else if (c == IGS_DISCONNECT) {
          gc.disconnectFromServer();
        } else if (c == IGS_MESSAGE) {
          chat.sendMessage(currentChallenge.nick, null, null, mainCanvas);
        } else if (c == REQUEST_KOMI) {
          gomeOnlineWantKomi();
        } else if (c == CHANGE_ONLINE_HANDICAP) {
          gomeOnlineChangeHandicap();
        }
        //#endif
        else if (c == NEXT) {
          gc.loadAndPlayNextInCollection();
        } else if (c == FIRST_MOVE) {
          gc.goToFirstMove();
        } else if (c == LAST_MOVE) {
          gc.goToLastMove();
        } else if (c == FINISHED_COUNTING) {
          gc.doScore();
        } else if (c == PASS) {
          gc.pass();
        } else if (c == SAVE) {
          saveGameForm = createSaveGameMenu();
          Gome.singleton.display.setCurrent(saveGameForm);
        } else if (c == OPTIONS) {
          optionsForm = new Options(Gome.singleton.bundle.getString("ui.options"), this, false);
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

        else if (c == ABOUT) {
          String info = "GOME v" + Gome.VERSION + "\n\n" + "(c) 2005 Indigonauts\n\nwww.indigonauts.com/gome"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$

          Util.messageBox(Gome.singleton.bundle.getString("ui.about") + "...", info, AlertType.INFO); //$NON-NLS-1$//$NON-NLS-2$

        } else if (c == HELP) {
          Help help = new Help(Gome.singleton.mainCanvas);
          help.show(Gome.singleton.display);
        }

        else if (c == EXIT) {
          Gome.singleton.notifyDestroyed();
        } else if (c == RESIGN) {
          gc.resign();
        }
        //#ifdef IGS
        else if (c == IGS_RESET_DEADS_TONE) {
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
            Util.messageBox(Gome.singleton.bundle.getString("ui.error"), Gome.singleton.bundle.getString(e.getMessage()), AlertType.ERROR); //$NON-NLS-1$
          }
        }
        saveGameForm = null;
        Gome.singleton.mainCanvas.show(Gome.singleton.display);
      }
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
          chat.sendMessage(gc.getNick(igsUserList.getSelectedIndex()), "", "", mainCanvas);
          return;
        }
        Gome.singleton.mainCanvas.show(Gome.singleton.display);

      } else if (d == challengeForm) {
        if (c == IGS_CHALLENGE) {
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
      //#ifdef DEBUG
      else if (d == logCanvas) {
        Gome.singleton.mainCanvas.show(Gome.singleton.display);
      }
      //#endif
    } catch (Throwable t) {
      //#ifdef DEBUG
      log.error(t);
      //#endif
    }
  }

  //#ifdef IGS
  public void showIgsGameList(ServerGame[] games) {
    //#ifdef DEBUG
    log.debug("Show igs gamelist");
    //#endif
    igsGameList = new List(Gome.singleton.bundle.getString("online.gameList"), Choice.IMPLICIT);
    for (int i = 0; i < games.length; i++) {
      igsGameList.append(games[i].toString(), null);
    }
    igsGameList.addCommand(BACK);
    igsGameList.addCommand(IGS_OBSERVE);
    igsGameList.setCommandListener(this);
    Gome.singleton.display.setCurrent(igsGameList);
    //#ifdef DEBUG
    log.debug("Show igs gamelist Done ?");
    //#endif
  }

  public void showIgsUserList(ServerUser[] users) {
    //#ifdef DEBUG
    log.debug("Show igs userlist");
    //#endif
    igsUserList = new List(Gome.singleton.bundle.getString("online.userlist"), Choice.IMPLICIT);
    for (int i = 0; i < users.length; i++) {
      igsUserList.append(users[i].toString(), null);
    }
    igsUserList.addCommand(BACK);
    igsUserList.addCommand(IGS_CHALLENGE);
    igsUserList.addCommand(IGS_MESSAGE);
    igsUserList.setCommandListener(this);
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
    gc = Gome.singleton.gameController;
    gc.newGame(size, handi, GameController.GAME_MODE);
  }

  public void loadFile(CollectionEntry file, int filenum) {
    gc = Gome.singleton.gameController;
    gc.reset(Gome.singleton.mainCanvas);
    gc.loadAndPlay(file, filenum);
  }

  public void deleteFile(FileEntry file) {
    if (file instanceof StoreFileEntry) {
      try {
        IOManager.singleton.deleteLocalStore(file.getPath());
      } catch (RecordStoreException e) {
        Util.messageBox(Gome.singleton.bundle.getString("ui.error"), Gome.singleton.bundle.getString("ui.error.delete"), AlertType.ERROR); //$NON-NLS-1$ //$NON-NLS-2$
      }
    } else {
      Util.messageBox(Gome.singleton.bundle.getString("ui.error"), Gome.singleton.bundle.getString("ui.error.wrongType"), AlertType.ERROR); //$NON-NLS-1$ //$NON-NLS-2$
    }
  }

  //#ifdef IGS
  public void showIgsChallenge(ServerChallenge challenge) {
    currentChallenge = challenge;
    String colors = ((challenge.color == Board.BLACK) ? Gome.singleton.bundle.getString("game.blackLong") : Gome.singleton.bundle.getString("game.whiteLong"));
    challengeForm = new Form(challenge.nick + Gome.singleton.bundle.getString("online.challengesYou"));
    String byoORnot = ((challenge.min_per25moves != -1) ? " + " + challenge.min_per25moves + Gome.singleton.bundle.getString("clock.min25stones") : Gome.singleton.bundle.getString("online.noByo"));
    String args[] = { colors, String.valueOf(challenge.size), String.valueOf(challenge.size), String.valueOf(challenge.time_minutes), byoORnot };
    String message = Gome.singleton.bundle.getString("online.challengeMessage", args);

    challengeForm.append(message);
    challengeForm.addCommand(IGS_CHALLENGE);
    challengeForm.addCommand(IGS_DECLINE);
    challengeForm.setCommandListener(this);
    Gome.singleton.display.setCurrent(challengeForm);
  }

  public void showOppWantKomi(byte k) {
    this.komi = k;
    oppRequestKomiForm = new Form(Gome.singleton.bundle.getString("online.komiChangeForm"));
    String temp = Util.komi2String(k);
    oppRequestKomiForm.append(Gome.singleton.bundle.getString("online.opponentWantsToChangeKomi") + temp);

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
    requestOnlineKomiForm = new Form(Gome.singleton.bundle.getString("online.komiChangeForm"));
    newGameKomi = new TextField(Gome.singleton.bundle.getString("ui.komi"), "5", 2, TextField.NUMERIC); //$NON-NLS-1$ //$NON-NLS-2$        
    requestOnlineKomiForm.append(Gome.singleton.bundle.getString("online.youWantToChangeKomi"));

    requestOnlineKomiForm.addCommand(REQUEST);
    requestOnlineKomiForm.addCommand(BACK);

    setKomi = new ChoiceGroup(Gome.singleton.bundle.getString("ui.komi"), Choice.EXCLUSIVE); //$NON-NLS-1$
    setKomi.append(Gome.singleton.bundle.getString("online.blackGives"), null); //$NON-NLS-1$
    setKomi.append(Gome.singleton.bundle.getString("online.whiteGives"), null); //$NON-NLS-1$        
    boolean[] flagsNew = { true, false };
    setKomi.setSelectedFlags(flagsNew);
    requestOnlineKomiForm.append(setKomi);
    requestOnlineKomiForm.append(newGameKomi);
    requestOnlineKomiForm.setCommandListener(this);

    Gome.singleton.display.setCurrent(requestOnlineKomiForm);
  }

  public void gomeOnlineChangeHandicap() {
    onlineChangeHandicapForm = new Form(Gome.singleton.bundle.getString("online.handicapChangeForm"));
    onlineChangeHandicapForm.append(Gome.singleton.bundle.getString("online.youWantToChangeHandicap"));

    onlineChangeHandicapForm.addCommand(ACCEPT);
    onlineChangeHandicapForm.addCommand(BACK);

    newGameHandicap = new TextField(Gome.singleton.bundle.getString("ui.handicap"), "2", 1, TextField.NUMERIC);
    onlineChangeHandicapForm.append(newGameHandicap);
    onlineChangeHandicapForm.setCommandListener(this);

    Gome.singleton.display.setCurrent(onlineChangeHandicapForm);
  }
  //#endif
}
