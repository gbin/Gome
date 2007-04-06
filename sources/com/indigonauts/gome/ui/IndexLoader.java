/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome.ui;

import java.io.IOException;
import java.util.Vector;

import com.indigonauts.gome.io.IOManager;
import com.indigonauts.gome.io.IndexEntry;

public class IndexLoader extends Fetcher {

  FileBrowser callback;
  Vector fileList;

  public IndexLoader(IndexEntry entry, FileBrowser callback) {
    super(entry);
    this.callback = callback;
    start();
  }

  protected void downloadFinished() {
    callback.downloadFinished(entry.getPath(), fileList);
  }

  protected void download() throws IOException {

    if (entry.getPath().equals(IOManager.LOCAL_NAME)) {
      if (IOManager.jsr75Mode) {
        fileList = IOManager.singleton.getJSR75Roots();
      } else
        fileList = IOManager.singleton.getLocalGamesList();
    } else {
      if (((IndexEntry) entry).isJsr75())
        fileList = IOManager.singleton.loadJSR75Index(((IndexEntry) entry).getPath());
      else
        fileList = IOManager.singleton.getFileList(((IndexEntry) entry).getPath(), this);

    }
  }

  protected void downloadFailed(Exception reason) {
    callback.downloadFailure(reason);
  }

}