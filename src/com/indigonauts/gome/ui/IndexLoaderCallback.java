/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome.ui;

import java.util.Vector;

public interface IndexLoaderCallback {
    public void downloadFinished(Vector files);

    public void downloadFailure(Exception reason);
}
