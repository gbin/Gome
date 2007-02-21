/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome.io;

public class FileEntry {
    private String path;
    private char playMode;

    FileEntry(String path, char playMode) {
        this.path = path;
        this.playMode = playMode;
    }

    /**
     * @return Returns the path.
     */
    public String getPath() {
        return path;
    }

    public boolean isRemote() {
        return path.startsWith("http://"); //$NON-NLS-1$
    }

    public char getPlayMode() {
        return playMode;
    }

}