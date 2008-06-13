//#condition BT
package com.indigonauts.gome.multiplayer.bt;

import java.io.IOException;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.LocalDevice;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;
import javax.microedition.io.StreamConnectionNotifier;

import com.indigonauts.gome.common.Util;
import com.indigonauts.gome.multiplayer.MultiplayerCallback;
import com.indigonauts.gome.multiplayer.P2PConnector;

public class BluetoothServiceConnector extends P2PConnector {
  //#if DEBUG
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("BluetoothServiceConnector");
  //#endif

  public static final String GOME_UUID = "A02E18764158444dA4A1A8900165E25A";
  private static final String serviceURL = "btspp://localhost:" + GOME_UUID;

  public BluetoothServiceConnector(MultiplayerCallback callback) throws BluetoothStateException {
    super(callback);
    ourselvesFriendlyName = LocalDevice.getLocalDevice().getFriendlyName() + " (Server)";
  }

  private StreamConnectionNotifier notifier;
  private StreamConnection connection;

  private void registerService() throws IOException {
    //#if DEBUG
    log.debug("Create server connection");
    //#endif
    notifier = (StreamConnectionNotifier) Connector.open(serviceURL);
  }

  protected void connect() throws IOException {
    registerService();
    System.out.println("Server wait for connection");
    connection = notifier.acceptAndOpen();
    System.out.println("Bluetooth Connection arrived");
    input = connection.openDataInputStream();
    output = connection.openDataOutputStream();
    otherFriendlyName = input.readUTF();
    callback.connectedBTEvent(this);
  }

  public void disconnect() {
    try {
      super.disconnect(); // this close input & output
      connection.close();
    } catch (IOException e) {
      Util.errorNotifier(e);
    }
  }

}
