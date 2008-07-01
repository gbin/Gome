//#condition BT
package com.indigonauts.gome.multiplayer;

import java.io.IOException;

import com.indigonauts.gome.ui.GameController;

public abstract class P2PConnector extends MultiplayerConnector {
  //#if DEBUG
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("P2PConnector");
  //#endif

  protected String ourselvesFriendlyName = "Undef";
  protected String otherFriendlyName = "Undef";

  public P2PConnector(MultiplayerCallback callback) {
    super(callback);

  }

  public void acceptChallenge(Challenge challenge) throws IOException {
    //#if DEBUG
    log.debug("Accept challenge");
    //#endif
    output.writeByte(GAME_EVENT);
    challenge.nick = ourselvesFriendlyName;
    challenge.marshall(output);
    output.flush();
    currentChallenge = challenge;
    callback.startGame(challenge, GameController.P2P_MODE);
  }

  public void playMove(Move move) throws IOException {
    super.playMove(move);
    callback.moveEvent(move);
  }

  public void doneWithTheCounting(int whiteScore, int blackScore) throws IOException {
    //#if DEBUG
    log.debug(" P2P done with counting");
    //#endif
    output.writeByte(SCORE_EVENT);
    output.writeInt(whiteScore);
    output.writeInt(blackScore);
    output.flush();
  }

  public void removeDeadStone(byte posX, byte posY) throws IOException {
    //#if DEBUG
    log.debug("Remove dead stone");
    //#endif
    output.writeByte(MARK_STONE_EVENT);
    output.writeByte(posX);
    output.writeByte(posY);
    output.flush();
  }

  public void sendMessage(String nickToSend, String message) throws IOException {
    //#if DEBUG
    log.debug("Send message");
    //#endif
    output.writeByte(MESSAGE_EVENT);
    output.writeByte(MultiplayerCallback.MESSAGE_CHAT_TYPE);
    output.writeUTF(ourselvesFriendlyName);
    output.writeUTF(message);
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
    case GAME_EVENT:
      //#if DEBUG
      log.debug("Start game event");
      //#endif
      Challenge challenge = Challenge.unmarshal(input);
      challenge.reverse();
      currentChallenge = challenge;
      callback.startGame(challenge, GameController.P2P_MODE);
      return true;

    }
    return super.handleEvent(event);
  }

  public String getCurrentOpponent() {
    return otherFriendlyName;
  }
}
