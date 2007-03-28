/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome.io;

public interface DownloadStatus {
    void setPercent(int percent);
    void requestLoginPassword(String pwdFile);
    String getLogin();
    String getPassword();
}
