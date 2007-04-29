/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome.io;

public class CollectionEntry extends FileEntry {
    private int collectionSize;

    private String description;

    private String illustrativeBoardArea;

    private String illustrativeBlackPosition;

    private String illustrativeWhitePosition;

    public CollectionEntry(String path, String name, char playMode, String description, int collectionSize) {
        super(path, name, playMode);
        this.collectionSize = collectionSize;
        this.description = description;
    }

    public CollectionEntry(String path, String name, char playMode, String description, int collectionSize,
            String illustrativeBoardArea, String illustrativeBlackPosition, String illustrativeWhitePosition) {
        this(path, name, playMode, description, collectionSize);
        this.illustrativeBoardArea = illustrativeBoardArea;
        this.illustrativeBlackPosition = illustrativeBlackPosition;
        this.illustrativeWhitePosition = illustrativeWhitePosition;

    }

    /**
     * @return Returns the fileNum.
     */
    public int getCollectionSize() {
        return collectionSize;
    }

    public String getDescription() {
        return description;
    }

    /**
     * @return Returns the illustrativeBlackPosition.
     */
    public String getIllustrativeBlackPosition() {
        return illustrativeBlackPosition;
    }

    /**
     * @return Returns the illustrativeBoardArea.
     */
    public String getIllustrativeBoardArea() {
        return illustrativeBoardArea;
    }

    /**
     * @return Returns the illustrativeWhitePosition.
     */
    public String getIllustrativeWhitePosition() {
        return illustrativeWhitePosition;
    }

    public boolean hasAnIllustration() {
        return illustrativeBoardArea != null;
    }

}
