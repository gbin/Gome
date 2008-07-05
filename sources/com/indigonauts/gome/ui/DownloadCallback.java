package com.indigonauts.gome.ui;

import java.util.Vector;

public interface DownloadCallback {
  void downloadFinished(String path, Vector files);

  void downloadFailure(Exception reason);

  void done();
}
