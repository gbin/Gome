/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome.io;

import com.indigonauts.gome.ui.GameController;

public class StoreFileEntry extends CollectionEntry {

    public StoreFileEntry(String path, String description) {
        super(path, GameController.GAME_MODE, description, 1);
    }

}
