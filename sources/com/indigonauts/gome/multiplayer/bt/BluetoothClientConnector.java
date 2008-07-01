//#condition BT
package com.indigonauts.gome.multiplayer.bt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.ServiceRecord;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import com.indigonauts.gome.Gome;
import com.indigonauts.gome.common.Util;
import com.indigonauts.gome.i18n.I18N;
import com.indigonauts.gome.multiplayer.MultiplayerCallback;
import com.indigonauts.gome.multiplayer.P2PConnector;

public class BluetoothClientConnector extends P2PConnector {
  //#if DEBUG
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("BluetoothClientConnector");

  //#endif

  public BluetoothClientConnector(MultiplayerCallback callback) throws BluetoothStateException {
    super(callback);
    ourselvesFriendlyName = LocalDevice.getLocalDevice().getFriendlyName() + " (Client)";

  }

  private Discoverer discoverer;
  private int index;

  protected boolean connect() throws IOException {
    try {
      discoverer = new Discoverer();
      Vector others = discoverer.findOtherGome();
      if (others == null || others.size() == 0) {
        callback.setSplashInfo(I18N.bt.noPeerFound);
        return false;
      }
      String[] peers = new String[others.size()];
      Enumeration elements = others.elements();
      int i = 0;
      while (elements.hasMoreElements()) {
        peers[i] = ((ServiceRecord) elements.nextElement()).getHostDevice().getFriendlyName(true);
      }
      Gome.singleton.menuEngine.showBTPeers(peers);

      // Wait for the user to select the peer he wants
      synchronized (this) {
        try {
          wait();
        } catch (InterruptedException e) {
          // Nothing to do
        }
      }
      //#if DEBUG
      log.debug("Woke up");
      //#endif
      String serverConnectionString = discoverer.getServerConnectionString(index);
      otherFriendlyName = discoverer.getServerFriendlyName(index);
      //#if DEBUG
      log.debug("ConnectionString = " + serverConnectionString);
      //#endif
      StreamConnection connection = (StreamConnection) Connector.open(serverConnectionString);
      //#if DEBUG
      log.debug("Connection opened");
      //#endif
      input = new DataInputStream(connection.openInputStream());
      output = new DataOutputStream(connection.openOutputStream());
      output.writeUTF(ourselvesFriendlyName);
      callback.connectedBTEvent(this);
      return true;
    } catch (Exception e) {
      Util.errorNotifier(e);
      return false;
    }
  }

  public synchronized void connectToPeer(int nb) {
    //#if DEBUG
    log.debug("Connect to peer #" + nb);
    //#endif
    this.index = nb;
    this.notifyAll();
  }

}
