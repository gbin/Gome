package com.indigonauts.gome.multiplayer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

public abstract class MultiplayerConnector extends Thread {
  //#ifdef DEBUG
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("ServerConnector");
  //#endif
  protected MultiplayerCallback callback;

  protected DataInputStream input;
  protected DataOutputStream output;
  private boolean noneErrorDisconnect = false;

  protected static final byte CHALLENGE = 0x04;

  private static final byte DECLINE = 0x05;

  private static final byte PLAY_MOVE = 0x06;

  private static final byte SEND_MESSAGE = 0x07;
  private static final byte MARK_STONE = 0x08;

  private static final byte DONE_WITH_COUNTING = 0x09;

  private static final byte SET_KOMI = 0x10;

  private static final byte RESIGN = 0x11;

  private static final byte SET_HANDICAP = 0x12;

  private static final byte RESET_DEAD_STONE = 0x13;
  
  private static final byte MOVE_EVENT = PLAY_MOVE;

  private static final byte MESSAGE_EVENT = 0x46;

  protected static final byte GAME_EVENT = 0x47;

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

  private static final byte LOST_MESSAGE_EVENT = 0x60;
  private static final byte CHALLENGE_EVENT = CHALLENGE;

  public MultiplayerConnector(MultiplayerCallback callback) {
    this.callback = callback;
  }

  public void challenge(Challenge challenge) throws IOException {
    log.debug("send challenge");
    output.writeByte(CHALLENGE);
    challenge.marshall(output);
    output.flush();
  }
  
  public abstract void acceptChallenge(Challenge challenge) throws IOException;


  public void decline(String nick) throws IOException {
    output.writeByte(DECLINE);
    output.writeUTF(nick);
    output.flush();
  }

  public void playMove(Move move) throws IOException {
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
   * Handle an event
   * @param event
   * @return true if the event is generic and has been treated
   * @throws IOException 
   */
  protected boolean handleEvent(byte event) throws IOException {
    switch (event) {
    case MOVE_EVENT:
      Move move = Move.unmarshal(input);
      //#ifdef DEBUG
      log.debug("IGS move event " + move);
      //#endif
      callback.moveEvent(move);
      break;

    case CHALLENGE_EVENT:
      //#ifdef DEBUG
      log.debug("Challenge Event Received");
      //#endif
      Challenge challenge = new Challenge();

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
      challenge = new Challenge();
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

      int whiteTime = input.readInt();
      int whiteByoStone = input.readInt();
      int blackTime = input.readInt();
      int blackByoStone = input.readInt();
      //#ifdef DEBUG
      log.debug("W" + whiteTime + "/" + whiteByoStone + " B " + blackTime + "/" + blackByoStone);
      //#endif
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
    case LOST_MESSAGE_EVENT:
      //#ifdef DEBUG
      log.debug("igs lost message event");
      //#endif
      String winnerName = input.readUTF();
      int winValue = input.readInt();
      callback.winByValue(winnerName, winValue);
      break;
    default:
      return true;
    }

    return false;
  }

  protected abstract void connect() throws IOException;
    /**
   * @see java.lang.Thread#run()
   */
  public void run() {
    try {
      connect();
      noneErrorDisconnect = false;
      while (true)
        handleEvent(input.readByte());

    } catch (java.io.InterruptedIOException iie) {
      //#ifdef DEBUG
      log.error("Disconnected ", iie);
      //#endif
    } catch (IOException e) {
      // let the disconnected message go
      //#ifdef DEBUG
      log.error("IOException", e);
      //#endif
    } catch (Throwable t) {
      //#ifdef DEBUG
      log.error("server loop error " + t.getClass(), t);
      t.printStackTrace();
      //#endif
    }

    finally {
      if (output != null) {
        try {
          output.close();
        } catch (Throwable e) {
          // Do nothing
        }
      }
      if (input != null) {
        try {
          input.close();
        } catch (Throwable e) {
          // Do nothing
        }
      }
      if (!noneErrorDisconnect) {
        callback.message(MultiplayerCallback.MESSAGE_ERROR_TYPE, "", "online.connectionError");
        noneErrorDisconnect = true;
      }
    }
    return;
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
