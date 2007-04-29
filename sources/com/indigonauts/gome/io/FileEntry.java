/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome.io;

public class FileEntry {
  private String path;
  private String name;
  private char playMode;

  FileEntry(String path, String name, char playMode) {
    this.path = path;
    this.playMode = playMode;
    this.name = name;
  }

  /**
   * @return Returns the path.
   */
  public String getPath() {
    return path;
  }

  /**
   * @return Returns the path.
   */
  public String getUrl() {
    return path + ((name != null) ? name : "");
  }

  public boolean isRemote() {
    return path.startsWith("http://"); //$NON-NLS-1$
  }

  public char getPlayMode() {
    return playMode;
  }

  public String getName() {
    return name;
  }

}