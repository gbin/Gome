/*
 * (c) 2006 Indigonauts
 */

package com.indigonauts.gome.ui;

import java.io.IOException;

import javax.microedition.lcdui.Alert;
import javax.microedition.lcdui.AlertType;

import com.indigonauts.gome.Gome;
import com.indigonauts.gome.io.FileEntry;
import com.indigonauts.gome.io.IOManager;
import com.indigonauts.gome.sgf.SgfModel;

/**
 * this class is run as a background thread. It loads the GameController.board,
 * and informs the gameController to refresh the progress bar
 */
public class FileLoader extends Fetcher // schedule for one-time run only!
{
  private int fileIndex;

  private GameController callback;

  private SgfModel model;
  private String text;
  private char mode;

  public FileLoader(GameController callback, FileEntry file, int fileIndex) {
    super(file);
    this.fileIndex = fileIndex;
    this.callback = callback;
    this.mode = file.getPlayMode();
  }

  protected void download() throws IOException {
    try {
      if (mode == GameController.TEXT_MODE) {
        text = new String(IOManager.singleton.loadFile(entry.getUrl(), this));
        return;
      }

      model = IOManager.singleton.extractGameFromCollection(entry.getUrl(), fileIndex, this);
      if (model == null)
        status = Fetcher.TERMINATED; // stop loading and wait for the reload
    } catch (IllegalArgumentException e) {
      new IOException(e.getMessage());
    }
  }

  protected void downloadFinished() {
    if (mode == GameController.TEXT_MODE) {

      Alert al = new Alert(entry.getUrl(), text, null, AlertType.INFO);
      al.setTimeout(Alert.FOREVER);
      al.setCommandListener(this);
      Gome.singleton.display.setCurrent(al);
      return;
    }
    callback.downloadFinished(model, mode);
  }

  protected void downloadFailed(Exception reason) {
    callback.downloadFailure(reason);
  }

}
