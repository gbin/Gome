package com.indigonauts.gome.multiplayer.bt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import com.indigonauts.gome.multiplayer.Challenge;
import com.indigonauts.gome.multiplayer.Move;
import com.indigonauts.gome.multiplayer.MultiplayerCallback;
import com.indigonauts.gome.multiplayer.MultiplayerConnector;

public class BluetoothServiceConnector extends MultiplayerConnector {
  //#ifdef DEBUG
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("BluetoothServiceConnector");
  //#endif

  
  public BluetoothServiceConnector(MultiplayerCallback callback) {
    super(callback);
  }

  public static final String GOME_UUID = "A02E18764158444dA4A1A8900165E25A";

  protected void connect() throws IOException {
    String serviceURL = "btspp://localhost:" + GOME_UUID;
    System.out.println("Create server connection");
    StreamConnectionNotifier notifier = (StreamConnectionNotifier) Connector.open(serviceURL);
    System.out.println("Server wait for connection");
    StreamConnection connection = notifier.acceptAndOpen();
    System.out.println("Bluetooth Connection arrived");
    input = new DataInputStream(connection.openInputStream());
    output = new DataOutputStream(connection.openOutputStream());
    callback.connectedBTEvent(this);
  }
  
   public void acceptChallenge(Challenge challenge) throws IOException {
    log.debug("Accept challenge");
    output.writeByte(GAME_EVENT);
    challenge.marshall(output);
    output.flush();
    callback.startGame(challenge.reverse());
  }
   
  public void playMove(Move move) throws IOException {
    super.playMove(move);
    callback.moveEvent(move);
  }
}
