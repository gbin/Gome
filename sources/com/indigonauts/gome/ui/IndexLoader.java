/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome.ui;

import java.io.IOException;
import java.util.Vector;

import com.indigonauts.gome.Gome;
import com.indigonauts.gome.io.IOManager;
import com.indigonauts.gome.io.IndexEntry;

public class IndexLoader extends Fetcher {

  DownloadCallback callback;
  Vector fileList;

  public IndexLoader(IndexEntry entry, DownloadCallback callback) {
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
      if (entry.getUrl().startsWith(IOManager.LOCAL_NAME)) {
        fileList = IOManager.singleton.loadJSR75Index(((IndexEntry) entry).getPath(), ((IndexEntry) entry).getName());
      } else
        //#endif
        fileList = IOManager.singleton.getFileList(((IndexEntry) entry).getUrl(), this);
      // watch out  the weird conditional compilation here !

    }
  }

  protected void downloadFailed(Exception reason) {
    callback.downloadFailure(reason);
  }

}