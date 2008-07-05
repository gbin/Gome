/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome.ui;

import javax.microedition.lcdui.Display;

public interface ModalDialog {
  /**
   * shows it self, also remebers the previous screen
   * 
   * @param dis
   */
  public void show(Display dis);

  /**
   * hide it self, and switch back to the previous screen
   * 
   */
  public void hide();

}
