/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome.io;

public class BundledFileEntry extends CollectionEntry {

    public BundledFileEntry(String path, String name, char playMode, String description, int collectionSize) {
        super(path, name, playMode, description, collectionSize);

    }

    public BundledFileEntry(String path, String name, char playMode, String description, int collectionSize,
            String illustrativeBoardArea, String illustrativeBlackPosition, String illustrativeWhitePosition) {
        super(path, name, playMode, description, collectionSize, illustrativeBoardArea, illustrativeBlackPosition,
                illustrativeWhitePosition);
    }

}
