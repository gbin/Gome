package com.indigonauts.gome.multiplayer.bt;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.Enumeration;
import java.util.Vector;

import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.ServiceRecord;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

import com.indigonauts.gome.Gome;
import com.indigonauts.gome.i18n.I18N;
import com.indigonauts.gome.multiplayer.Challenge;
import com.indigonauts.gome.multiplayer.Move;
import com.indigonauts.gome.multiplayer.MultiplayerCallback;
import com.indigonauts.gome.multiplayer.MultiplayerConnector;
import com.indigonauts.gome.multiplayer.P2PConnector;
import com.indigonauts.gome.ui.GameController;
import com.indigonauts.gome.ui.MenuEngine;

public class BluetoothClientConnector extends P2PConnector {
  //#ifdef DEBUG
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("BluetoothClientConnector");
  //#endif

  public BluetoothClientConnector(MultiplayerCallback callback) {
    super(callback);

  }

  private Discoverer discoverer;
  private int index;

  protected void connect() throws IOException {
    try {
      discoverer = new Discoverer();
      Vector others = discoverer.findOtherGome();
      if (others.size() == 0) {
        callback.setSplashInfo(I18N.bt.noPeerFound);
        return;
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
      log.debug("Woke up");
      String serverConnectionString = discoverer.getServerConnectionString(index);
      log.debug("ConnectionString = " + serverConnectionString);
      StreamConnection connection = (StreamConnection) Connector.open(serverConnectionString);
      log.debug("Connection opened");
      input = new DataInputStream(connection.openInputStream());
      output = new DataOutputStream(connection.openOutputStream());
      callback.connectedBTEvent(this);

    } catch (BluetoothStateException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public synchronized void connectToPeer(int nb) {
    log.debug("Connect to peer #" + nb);
    this.index = nb;
    this.notifyAll();
  }

}
