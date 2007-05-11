/*
 * (c) 2006 Indigonauts
 */

package com.indigonauts.gome.ui;

import java.io.IOException;

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
  private char mode;

  public FileLoader(GameController callback, FileEntry file, int fileIndex) {
    super(file);
    this.fileIndex = fileIndex;
    this.callback = callback;
    this.mode = file.getPlayMode();
  }

  protected void download() throws IOException {
    try {
      model = IOManager.singleton.extractGameFromCollection(entry.getUrl(), fileIndex, this);
      if (model == null)
        status = Fetcher.TERMINATED; // stop loading and wait for the reload
    } catch (IllegalArgumentException e) {
      new IOException(e.getMessage());
    }
  }

  protected void downloadFinished() {
    callback.downloadFinished(model, mode);
  }

  protected void downloadFailed(Exception reason) {
    callback.downloadFailure(reason);
  }

}
