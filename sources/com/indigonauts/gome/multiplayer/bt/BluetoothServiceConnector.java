package com.indigonauts.gome.multiplayer.bt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.LocalDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import com.indigonauts.gome.multiplayer.Challenge;
import com.indigonauts.gome.multiplayer.Move;
import com.indigonauts.gome.multiplayer.MultiplayerCallback;
import com.indigonauts.gome.multiplayer.MultiplayerConnector;
import com.indigonauts.gome.multiplayer.P2PConnector;
import com.indigonauts.gome.ui.GameController;

public class BluetoothServiceConnector extends P2PConnector {
  //#ifdef DEBUG
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("BluetoothServiceConnector");
  //#endif

  public BluetoothServiceConnector(MultiplayerCallback callback) throws BluetoothStateException {
    super(callback);
    ourselvesFriendlyName = LocalDevice.getLocalDevice().getFriendlyName() + " (Server)";
  }

  public static final String GOME_UUID = "A02E18764158444dA4A1A8900165E25A";

  protected void connect() throws IOException {
    String serviceURL = "btspp://localhost:" + GOME_UUID;
    System.out.println("Create server connection");
    StreamConnectionNotifier notifier = (StreamConnectionNotifier) Connector.open(serviceURL);
    System.out.println("Server wait for connection");
    StreamConnection connection = notifier.acceptAndOpen();
    
    System.out.println("Bluetooth Connection arrived");
    input = connection.openDataInputStream();
    output = connection.openDataOutputStream();
    otherFriendlyName = input.readUTF();
    callback.connectedBTEvent(this);
  }

}
