/*
 * (c) 2006  Indigonauts
 */
package com.indigonauts.gome.ui;

import com.indigonauts.gome.sgf.SgfModel;

public interface FileLoaderCallback {
    public void downloadFinished(SgfModel model, char playMode);

    public void downloadFailure(Exception reason);
}
