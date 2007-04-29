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
    callback.downloadFinished(entry.getUrl(), fileList);
  }

  protected void download() throws IOException {

    if (entry.getUrl().equals(IOManager.LOCAL_NAME)) {
      //#ifdef JSR75
      fileList = IOManager.singleton.getJSR75Roots();
      //#else
      //# fileList = IOManager.singleton.getLocalGamesList();
      //#endif
    } else {
      //#ifdef JSR75
      fileList = IOManager.singleton.loadJSR75Index(((IndexEntry) entry).getPath(), ((IndexEntry) entry).getName());
      //#else
      //# fileList = IOManager.singleton.getFileList(((IndexEntry) entry).getUrl(), this);
      //#endif

    }
  }

  protected void downloadFailed(Exception reason) {
    callback.downloadFailure(reason);
  }

}