/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome.ui;

import java.io.IOException;
import java.util.Vector;

import com.indigonauts.gome.io.IOManager;

public class IndexLoader extends Fetcher {

	FileBrowser callback;
    Vector fileList;

    public IndexLoader(String url, FileBrowser callback) {
        super(url);
        this.callback = callback;
        start();
    }

    protected void downloadFinished() {
        callback.downloadFinished(fileList);
    }

    protected void download() throws IOException {

        fileList = IOManager.getSingleton().getFileList(url, this);

    }

    protected void downloadFailed(Exception reason) {
        callback.downloadFailure(reason);
    }

}