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
    if (url.equals(IOManager.LOCAL_NAME))
      fileList = IOManager.singleton.getLocalGamesList();
    else
      fileList = IOManager.singleton.getFileList(url, this);

  }

  protected void downloadFailed(Exception reason) {
    callback.downloadFailure(reason);
  }

}