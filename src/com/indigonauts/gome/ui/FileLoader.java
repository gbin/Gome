/*
 * (c) 2006 Indigonauts
 */

package com.indigonauts.gome.ui;

import java.io.IOException;

import com.indigonauts.gome.io.FileEntry;
import com.indigonauts.gome.io.GamesIOManager;
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
        super(file.getPath());
        this.fileIndex = fileIndex;
        this.callback = callback;
        this.mode = file.getPlayMode();
    }

    protected void download() throws IOException {
        try {
            model = GamesIOManager.extractGameFromCollection(url, fileIndex, this);
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
