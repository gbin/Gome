/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome.igs;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.lcdui.AlertType;

import org.apache.log4j.Logger;

import com.indigonauts.gome.Gome;
import com.indigonauts.gome.common.Util;

public class ServerConnector extends Thread {
  private static final String SERVER_INCOMING = "socket://gome.indigonauts.com:1402";

  private static final String SERVER_OUTGOING = "socket://gome.indigonauts.com:1403";
  //#ifdef DEBUG
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("ServerConnector");
  //#endif

  private final String password;

  private final String login;

  private DataInputStream input;

  private DataOutputStream output;

  private boolean noneErrorDisconnect = false;

  private ServerGame[] gameList;

  private ServerUser[] userList;

  private ServerCallback callback;

  private static final byte SERVER_VERSION = 3;

  private static final byte LOGIN = 0x00;

  private static final byte GAMELIST = 0x01;

  private static final byte OBSERVE = 0x02;

  private static final byte USERLIST = 0x03;

  private static final byte CHALLENGE = 0x04;

  private static final byte DECLINE = 0x05;

  private static final byte PLAY_MOVE = 0x06;

  private static final byte SEND_MESSAGE = 0x07;

  private static final byte MARK_STONE = 0x08;

  private static final byte DONE_WITH_COUNTING = 0x09;

  private static final byte SET_KOMI = 0x10;

  private static final byte RESIGN = 0x11;

  private static final byte SET_HANDICAP = 0x12;

  private static final byte RESET_DEAD_STONE = 0x13;

  private static final byte GET_SCORE = 0x14;

  private static final byte LOGGED_EVENT = 0x40;

  private static final byte GAME_LIST_EVENT = 0x41;

  private static final byte OBSERVE_EVENT = 0x42;

  private static final byte MOVE_EVENT = 0x43;

  private static final byte USER_LIST_EVENT = 0x44;

  private static final byte CHALLENGE_EVENT = 0x45;

  private static final byte MESSAGE_EVENT = 0x46;

  private static final byte GAME_EVENT = 0x47;

  private static final byte TIME_EVENT = 0x48;

  private static final byte TIMES_UP_EVENT = 0x49;

  private static final byte END_GAME_EVENT = 0x50;

  private static final byte MARK_STONE_EVENT = 0x51;

  private static final byte RESTORE_GAME_FOR_COUNING_EVENT = 0x52;

  private static final byte GAME_IS_DONE_EVENT = 0x53;

  private static final byte HANDICAP_EVENT = 0x54;

  private static final byte OPP_WANT_KOMI_EVENT = 0x55;

  private static final byte SET_KOMI_EVENT = 0x56;

  private static final byte IGS_RESIGNED_EVENT = 0x58;

  private static final byte SCORE_EVENT = 0x59;

  private static final byte IGS_LOST_MESSAGE_EVENT = 0x60;

  public ServerConnector(String login, String password, ServerCallback callback) {
    this.login = login;
    this.password = password;
    this.callback = callback;

  }

  private void login() throws IOException {
    //#ifdef DEBUG  
    log.debug("login on IGS:" + login + "/*******");
    //#endif
    output.writeByte(LOGIN);
    output.writeUTF(login);
    output.writeUTF(password);
    output.flush();
  }

  public void getGames() throws IOException {
    output.writeByte(GAMELIST);
    output.writeByte(0);
    output.writeInt(10);
    output.flush();

  }

  public void getUsers() throws IOException {
    output.writeByte(USERLIST);
    output.writeByte(0);
    output.writeInt(10);
    output.flush();

  }

  public void observe(int game) throws IOException {
    output.writeByte(OBSERVE);
    output.writeInt(game);
    output.flush();
  }

  public void challenge(ServerChallenge challenge) throws IOException {
    output.writeByte(CHALLENGE);
    output.writeUTF(challenge.nick);
    output.writeByte(challenge.color);
    output.writeByte(challenge.size);
    output.writeInt(challenge.time_minutes);
    output.writeInt(challenge.min_per25moves);
    output.flush();
  }

  public void decline(String nick) throws IOException {
    output.writeByte(DECLINE);
    output.writeUTF(nick);
    output.flush();
  }

  public void playMove(ServerMove move) throws IOException {
    output.writeByte(PLAY_MOVE);
    output.writeInt(move.nb);
    output.writeByte(move.color);
    output.writeByte(move.x);
    output.writeByte(move.y);
    output.flush();

  }

  public void sendMessage(String nickToSend, String message) throws IOException {
    output.writeByte(SEND_MESSAGE);
    output.writeUTF(nickToSend);
    output.writeUTF(message);
    output.flush();
  }

  public void removeDeadStone(byte posX, byte posY) throws IOException {
    output.writeByte(MARK_STONE);
    output.writeByte(posX);
    output.writeByte(posY);
    output.flush();
  }

  public void doneWithTheCounting() throws IOException {
    output.writeByte(DONE_WITH_COUNTING);
    output.flush();
  }

  public void getScore() throws IOException {
    output.writeByte(GET_SCORE);
    output.flush();
  }

  public void setKomi(byte k) throws IOException {
    output.writeByte(SET_KOMI);
    output.writeByte(k);
    output.flush();
  }

  public void resign() throws IOException {
    output.writeByte(RESIGN);
    output.flush();
  }

  public void setHandicap(byte h) throws IOException {
    output.writeByte(SET_HANDICAP);
    output.writeByte(h);
    output.flush();
  }

  public void resetDeadStone() throws IOException {
    output.writeByte(RESET_DEAD_STONE);
    output.flush();
  }

  /**
   * @see java.lang.Thread#run()
   */
  public void run() {
    try {
      //#ifdef DEBUG
      log.debug("Open incoming stream ...");
      //#endif
      StreamConnection connectionIncoming = (StreamConnection) Connector.open(SERVER_INCOMING);
      //#ifdef DEBUG
      log.debug("Stream  connected");
      //#endif
      input = new DataInputStream(connectionIncoming.openInputStream());
      //#ifdef DEBUG
      log.debug("Read magic");
      //#endif
      int magic = input.readInt();
      //#ifdef DEBUG
      log.debug("magic = " + magic);
      //#endif
      byte version = input.readByte();
      //#ifdef DEBUG
      log.debug("Read version " + version);
      //#endif
      if (version != SERVER_VERSION) {
        Util.messageBox(Gome.singleton.bundle.getString("ui.error"), Gome.singleton.bundle.getString("online.versionError"), AlertType.ERROR);
        connectionIncoming.close();
        return;
      }
      //#ifdef DEBUG
      log.debug("Open outgoing stream ...");
      //#endif
      StreamConnection connectionOutgoing = (StreamConnection) Connector.open(SERVER_OUTGOING);
      //#ifdef DEBUG
      log.debug("outgoing stream connected...");
      //#endif
      output = new DataOutputStream(connectionOutgoing.openOutputStream());
      //#ifdef DEBUG
      log.debug("write magic");
      //#endif
      output.writeInt(magic);
      output.flush();

      login();
      noneErrorDisconnect = false;
      while (true) {
        //#ifdef DEBUG
        try {
          //#endif
          byte event = input.readByte();
          switch (event) {
          case LOGGED_EVENT:
            byte reason = input.readByte();
            //#ifdef DEBUG
            log.debug("IGS Logged event " + reason);
            //#endif
            callback.loggedEvent();
            break;
          case GAME_LIST_EVENT:
            int nbGames = input.readInt();
            gameList = new ServerGame[nbGames];
            for (int i = 0; i < nbGames; i++) {
              gameList[i] = ServerGame.unmarshal(input);
            }
            callback.gameListEvent(gameList);
            break;
          case OBSERVE_EVENT:
            int nbMoves = input.readInt();
            ServerMove[] moveList = new ServerMove[nbMoves];
            for (int i = 0; i < nbMoves; i++) {
              moveList[i] = ServerMove.unmarshal(input);
            }
            callback.observeEvent(moveList);
            break;

          case MOVE_EVENT:
            ServerMove move = ServerMove.unmarshal(input);
            callback.moveEvent(move);
            break;

          case USER_LIST_EVENT:
            int nbUsers = input.readInt();
            userList = new ServerUser[nbUsers];
            for (int i = 0; i < nbUsers; i++) {
              userList[i] = ServerUser.unmarshal(input);
            }
            callback.userListEvent(userList);
            break;
          case CHALLENGE_EVENT:
            //#ifdef DEBUG
            log.debug("Challenge");
            //#endif
            ServerChallenge challenge = new ServerChallenge();

            challenge.nick = input.readUTF();
            //#ifdef DEBUG
            log.debug("Nick = " + challenge.nick);
            //#endif
            challenge.color = input.readByte();
            challenge.size = input.readByte();
            challenge.time_minutes = input.readInt();
            challenge.min_per25moves = input.readInt();
            callback.challenge(challenge);
            break;
          case MESSAGE_EVENT:
            //#ifdef DEBUG
            log.debug("Incoming message event");
            //#endif
            byte type = input.readByte();
            String nick = input.readUTF();
            String message = input.readUTF();
            callback.message(type, nick, message);
            break;
          case GAME_EVENT:
            //#ifdef DEBUG
            log.debug("Start game event");
            //#endif
            challenge = new ServerChallenge();
            challenge.nick = input.readUTF();
            //#ifdef DEBUG
            log.debug("Nick = " + challenge.nick);
            //#endif
            challenge.color = input.readByte();
            challenge.size = input.readByte();
            challenge.time_minutes = input.readInt();
            challenge.min_per25moves = input.readInt();
            callback.startGame(challenge);
            break;
          case TIME_EVENT:
            //#ifdef DEBUG
            log.debug("time event");
            //#endif
            int whiteTime = input.readInt();
            int whiteByoStone = input.readInt();
            int blackTime = input.readInt();
            int blackByoStone = input.readInt();
            callback.synOnlineTime(whiteTime, whiteByoStone, blackTime, blackByoStone);
            break;
          case TIMES_UP_EVENT:
            //#ifdef DEBUG
            log.debug("times up event");
            //#endif
            String name = input.readUTF();
            callback.timesUP(name);
            break;
          case END_GAME_EVENT:
            //#ifdef DEBUG
            log.debug("end game event");
            //#endif
            callback.endGame();
            break;
          case MARK_STONE_EVENT:
            //#ifdef DEBUG
            log.debug("mark stone event");
            //#endif
            byte x = input.readByte();
            byte y = input.readByte();
            callback.oppRemoveDeadStone(x, y);
            break;
          case RESTORE_GAME_FOR_COUNING_EVENT:
            //#ifdef DEBUG
            log.debug("restore game for counting event");
            //#endif
            callback.restoreGameForCounting();
            break;
          case GAME_IS_DONE_EVENT:
            //#ifdef DEBUG
            log.debug("game is done event");
            //#endif
            String name1 = input.readUTF();
            int value1 = input.readInt();
            String name2 = input.readUTF();
            int value2 = input.readInt();
            callback.gameIsDone(name1, value1, name2, value2);
            break;
          case HANDICAP_EVENT:
            //#ifdef DEBUG
            log.debug("handicap event");
            //#endif
            byte value = input.readByte();
            callback.oppSetHandicap(value);
            break;
          case OPP_WANT_KOMI_EVENT:
            //#ifdef DEBUG
            log.debug("opp want komi event");
            //#endif
            byte komi = input.readByte();
            callback.oppWantToSetNewKomi(komi);
            break;
          case SET_KOMI_EVENT:
            //#ifdef DEBUG
            log.debug("set komi event event");
            //#endif
            byte k = input.readByte();
            callback.setKomi(k);
            break;
          case IGS_RESIGNED_EVENT:
            //#ifdef DEBUG
            log.debug("resigned event");
            //#endif
            String resignedName = input.readUTF();
            callback.onlineResigned(resignedName);
            break;
          case SCORE_EVENT:
            //#ifdef DEBUG
            log.debug("score event");
            //#endif
            int whiteScore = input.readInt();
            int blackScore = input.readInt();
            callback.onlineScore(whiteScore, blackScore);
            break;
          case IGS_LOST_MESSAGE_EVENT:
            //#ifdef DEBUG
            log.debug("igs lost message event");
            //#endif
            String winnerName = input.readUTF();
            int winValue = input.readInt();
            callback.winByValue(winnerName, winValue);
            break;
          }
          //#ifdef DEBUG
        }

        catch (Throwable t) {
          log.error("server loop error", t);
          Util.messageBox("Uncaught exception ", "Uncaught exception " + t.getClass().getName() + ":" + t.getMessage(), AlertType.ERROR);
          Gome.singleton.display.setCurrent(Logger.getLogCanvas());
        }
        //#endif
      }

    } catch (java.io.InterruptedIOException iie) {
      //#ifdef DEBUG
      log.error("Diconnected ", iie);
      //#endif
    } catch (IOException e) {
      // let the disconnected message go
    }

    finally {
      if (output != null) {
        try {
          output.close();
        } catch (IOException e) {
          // Do nothing
        }
      }
      if (input != null) {
        try {
          input.close();
        } catch (IOException e) {
          // Do nothing
        }
      }
      if (!noneErrorDisconnect) {
        callback.message(ServerCallback.MESSAGE_ERROR_TYPE, "", "online.connectionError");
        noneErrorDisconnect = true;
      }
    }
    return;
  }

  public ServerGame[] getGameList() {
    return gameList;
  }

  public ServerUser[] getUserList() {
    return userList;
  }

  public void disconnect() {
    try {
      if (input != null)
        input.close();
    } catch (IOException e) {
      //#ifdef DEBUG
      log.error(e);
      //#endif
    }
    try {
      if (output != null)
        output.close();
    } catch (IOException e) {
      //#ifdef DEBUG
      log.error(e);
      //#endif
    }
    noneErrorDisconnect = true;
  }
}
