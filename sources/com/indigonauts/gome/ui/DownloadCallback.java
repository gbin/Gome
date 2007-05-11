package com.indigonauts.gome.ui;

import java.util.Vector;

import com.indigonauts.gome.sgf.SgfModel;

public interface DownloadCallback {
  void downloadFinished(String path, Vector files);
  void downloadFailure(Exception reason);
  void done();
}
