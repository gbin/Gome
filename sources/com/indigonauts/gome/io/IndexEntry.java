/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome.io;

public class IndexEntry extends FileEntry {
  private String description;
  private String illustrativeBoardArea;
  private String illustrativeBlackPosition;
  private String illustrativeWhitePosition;
  private boolean jsr75;

  public IndexEntry(String path, String description, boolean jsr75) {
    super(path, 'I');
    this.description = description;
    this.jsr75 = jsr75;
  }

  public boolean isJsr75() {
    return jsr75;
  }

  public IndexEntry(String path, String description, String illustrativeBoardArea, String illustrativeBlackPosition, String illustrativeWhitePosition) {
    this(path, description, false);
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
