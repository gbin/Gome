/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome.io;

import com.indigonauts.gome.ui.GameController;

public class LocalFileEntry extends CollectionEntry {
  private boolean jsr75;

  public LocalFileEntry(String path, String description, boolean jsr75) {
    super(path, GameController.GAME_MODE, description, 1);
    this.jsr75 = jsr75;
  }

  public boolean isJsr75() {
    return jsr75;
  }

}
