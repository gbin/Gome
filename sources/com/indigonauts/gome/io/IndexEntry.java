/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome.io;

public class IndexEntry extends FileEntry {
  private String description;
  private String illustrativeBoardArea;
  private String illustrativeBlackPosition;
  private String illustrativeWhitePosition;

  public IndexEntry(String path, String name, String description) {
    super(path, name, 'I');
    this.description = description;
  }

  public IndexEntry(String path, String name, String description, String illustrativeBoardArea, String illustrativeBlackPosition, String illustrativeWhitePosition) {
    this(path, name, description);
    this.illustrativeBoardArea = illustrativeBoardArea;
    this.illustrativeBlackPosition = illustrativeBlackPosition;
    this.illustrativeWhitePosition = illustrativeWhitePosition;

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
