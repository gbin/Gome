/*
 * (c) 2006 Indigonauts
 */

package com.indigonauts.gome;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import javax.microedition.lcdui.Font;

import com.indigonauts.gome.common.Util;
import com.indigonauts.gome.sgf.Board;

public class GomeOptions {
  //#ifdef DEBUG
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("GomeOptions");
  //#endif

  private static final byte SMALL = 0;
  public static final byte MEDIUM = 1;
  private static final byte LIGHT = 0;

  private static final byte DARK = 2;
  private static final byte LARGE = 2;
  private static final byte EXTRA_LARGE = 3;

  public static final byte MANUAL = 3;
  public static final byte SLOW = 0;

  public static final byte FAST = 2;

  public int gobanColor = Util.GOBAN_COLOR_MEDIUM;
  public byte stoneBug = (Util.S60_FLAG) ? Util.FILL_BUG1 : Util.FILL_NORMAL;
  public byte optimize = Util.FOR_SPEED;

  public int fontSize = Font.SIZE_MEDIUM;

  public byte scrollerSpeed = MEDIUM;

  public byte scrollerSize = SMALL;

  public String igsLogin = "";
  public String igsPassword = "";
  
  // TODO : make the GUI for the color
  public byte igsColor = Board.BLACK;
  public byte igsSize = 19;
  public int igsMinutes = 25;
  public int igsByoyomi = 10;

  public String email = "";

  public String user = "";

  public String key = "";
  
  public String defaultDirectory = "file:///root1/";

  public long expiration = 0;
  public byte ghostStone = 3;

  public Font getScrollerFont() {
    return Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, fontSize);
  }

  public GomeOptions(DataInputStream is) {
    try {
      is.readByte(); // ex language kept for backward compatibility
      setGobanColorFromByte(is.readByte());
      setScrollerFontFromByte(is.readByte());
      scrollerSpeed = is.readByte();
      scrollerSize = is.readByte();
      igsLogin = is.readUTF();
      igsPassword = is.readUTF();
      igsSize = is.readByte();
      igsMinutes = is.readInt();
      igsByoyomi = is.readInt();
      email = is.readUTF();
      user = is.readUTF();
      key = is.readUTF();
      expiration = is.readLong();
      stoneBug = is.readByte();
      optimize = is.readByte();
      ghostStone = is.readByte();
      defaultDirectory = is.readUTF();

    } catch (IOException ioe) {
      //#ifdef DEBUG
      log.warn("Error loading the options", ioe);
      //#endif
      // probably ended before the end, leav the remaining defaults for
      // compatiibility
    }
  }

  public GomeOptions() {
    //nothing to do
  }

  /**
   * @param fontByte
   */
  public void setScrollerFontFromByte(byte fontByte) {
    // Scroller Font
    switch (fontByte) {
    case SMALL:
      fontSize = Font.SIZE_SMALL;
      break;
    case MEDIUM:
      fontSize = Font.SIZE_MEDIUM;
      break;
    case LARGE:
      fontSize = Font.SIZE_LARGE;
      break;
    }
  }

  /**
   * @param colorByte
   */
  public void setGobanColorFromByte(byte colorByte) {
    // goban Color
    switch (colorByte) {
    case LIGHT:
      gobanColor = Util.GOBAN_COLOR_LIGHT;
      break;
    case MEDIUM:
      gobanColor = Util.GOBAN_COLOR_MEDIUM;
      break;
    case DARK:
      gobanColor = Util.GOBAN_COLOR_DARK;
      break;
    }
  }

  public void setIGSGobanSizeFromByte(byte sizeByte) {
    switch (sizeByte) {
    case SMALL:
      igsSize = 9;
      break;
    case MEDIUM:
      igsSize = 13;
      break;
    case LARGE:
      igsSize = 19;
      break;
    }
  }

  public byte getGobanColorByte() {
    switch (gobanColor) {
    case Util.GOBAN_COLOR_LIGHT:
      return LIGHT;

    case Util.GOBAN_COLOR_MEDIUM:
      return MEDIUM;

    case Util.GOBAN_COLOR_DARK:
      return DARK;
    }
    return -1;

  }

  public byte getIGSGobanSizeByte() {
    switch (igsSize) {
    case 9:
      return SMALL;
    case 13:
      return MEDIUM;
    case 19:
      return LARGE;
    }
    return -1;
  }

  public byte getScrollerFontByte() {
    switch (fontSize) {
    case Font.SIZE_SMALL:
      return SMALL;
    case Font.SIZE_MEDIUM:
      return MEDIUM;
    case Font.SIZE_LARGE:
      return LARGE;
    }
    return -1;
  }

  public void marshalOut(DataOutputStream out) throws IOException {
    out.writeByte(0); // ex language, kept for backward compatibility
    out.writeByte(getGobanColorByte());
    out.writeByte(getScrollerFontByte());
    out.writeByte(scrollerSpeed);
    out.writeByte(scrollerSize);
    out.writeUTF(igsLogin);
    out.writeUTF(igsPassword);
    out.writeByte(igsSize);
    out.writeInt(igsMinutes);
    out.writeInt(igsByoyomi);
    out.writeUTF(email);
    out.writeUTF(user);
    out.writeUTF(key);
    out.writeLong(expiration);
    out.writeByte(stoneBug);
    out.writeByte(optimize);
    out.writeByte(ghostStone);
    out.writeUTF(defaultDirectory);
  }

  public int getScrollerSpeed() {
    switch (scrollerSpeed) {
    case SLOW:
      return 300;
    default:
    case MEDIUM:
      return 200;
    case FAST:
      return 100;
    case MANUAL:
      return -1;
    }

  }

  public int getScrollerSize() {
    int size = getScrollerFont().getHeight();
    switch (scrollerSize) {
    default:
    case SMALL:
      return size;
    case MEDIUM:
      return (size * 3) / 2;
    case LARGE:
      return size * 2;
    case EXTRA_LARGE:
      return (size * 5) / 2;
    }
  }

}
