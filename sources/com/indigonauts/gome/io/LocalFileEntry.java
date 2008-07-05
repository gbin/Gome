/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome.io;

import com.indigonauts.gome.ui.GameController;

public class LocalFileEntry extends CollectionEntry {
  public LocalFileEntry(String path, String name, String description) {
    super(path, name, GameController.GAME_MODE, description, 1);

  }

}
