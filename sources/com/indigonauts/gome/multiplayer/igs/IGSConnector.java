/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome.multiplayer.igs;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.lcdui.AlertType;

import com.indigonauts.gome.common.Util;
import com.indigonauts.gome.i18n.I18N;
import com.indigonauts.gome.multiplayer.Challenge;
import com.indigonauts.gome.multiplayer.Game;
import com.indigonauts.gome.multiplayer.Move;
import com.indigonauts.gome.multiplayer.MultiplayerCallback;
import com.indigonauts.gome.multiplayer.MultiplayerConnector;
import com.indigonauts.gome.multiplayer.User;

public class IGSConnector extends MultiplayerConnector {
  private static final String SERVER_INCOMING = "socket://gome.indigonauts.com:1402";

  private static final String SERVER_OUTGOING = "socket://gome.indigonauts.com:1403";
  //#ifdef DEBUG
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("ServerConnector");
  //#endif

  private final String password;

  private final String login;

  private Game[] gameList;

  private User[] userList;

  private static final byte SERVER_VERSION = 3;

  private static final byte LOGIN = 0x00;

  private static final byte GAMELIST = 0x01;

  private static final byte OBSERVE = 0x02;

  private static final byte USERLIST = 0x03;

  private static final byte GET_SCORE = 0x14;

  private static final byte LOGGED_EVENT = 0x40;

  private static final byte GAME_LIST_EVENT = 0x41;

  private static final byte OBSERVE_EVENT = 0x42;

  private static final byte USER_LIST_EVENT = 0x44;

  public IGSConnector(String login, String password, MultiplayerCallback callback) {
    super(callback);
    this.login = login;
    this.password = password;
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

  public void getScore() throws IOException {
    output.writeByte(GET_SCORE);
    output.flush();
  }

  protected void connect() throws IOException {
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
      Util.messageBox(I18N.error.error, I18N.online.versionError, AlertType.ERROR);
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

  }

  protected boolean handleEvent(byte event) throws IOException {
    if (super.handleEvent(event))
      return true;
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
      gameList = new Game[nbGames];
      for (int i = 0; i < nbGames; i++) {
        gameList[i] = Game.unmarshal(input);
      }
      callback.gameListEvent(gameList);
      break;
    case OBSERVE_EVENT:
      int nbMoves = input.readInt();
      //#ifdef DEBUG
      log.debug("Observe event with " + nbMoves + " moves");
      //#endif
      Move[] moveList = new Move[nbMoves];
      for (int i = 0; i < nbMoves; i++) {
        moveList[i] = Move.unmarshal(input);
      }
      callback.observeEvent(moveList);
      break;

    case USER_LIST_EVENT:
      int nbUsers = input.readInt();
      userList = new User[nbUsers];
      for (int i = 0; i < nbUsers; i++) {
        userList[i] = User.unmarshal(input);
      }
      callback.userListEvent();
      break;
    }
    return true;

  }


  public Game[] getGameList() {
    return gameList;
  }

  public User[] getUserList() {
    return userList;
  }

  public void acceptChallenge(Challenge challenge) throws IOException {
    log.debug("accept challenge");
    output.writeByte(CHALLENGE);
    challenge.marshall(output);
    output.flush();
    
  }

  public String getCurrentOpponent() {
    return getCurrentChallenge().nick;
  }

  

}
