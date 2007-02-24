/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome.io;

public class BundledFileEntry extends CollectionEntry {

    public BundledFileEntry(String path, char playMode, String description, int collectionSize) {
        super(path, playMode, description, collectionSize);

    }

    public BundledFileEntry(String path, char playMode, String description, int collectionSize,
            String illustrativeBoardArea, String illustrativeBlackPosition, String illustrativeWhitePosition) {
        super(path, playMode, description, collectionSize, illustrativeBoardArea, illustrativeBlackPosition,
                illustrativeWhitePosition);
    }

}
