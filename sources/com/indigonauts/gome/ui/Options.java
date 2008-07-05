/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome.ui;

import java.io.IOException;

import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Font;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Graphics;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.TextField;

import com.indigonauts.gome.Gome;
import com.indigonauts.gome.GomeOptions;
import com.indigonauts.gome.common.Util;
import com.indigonauts.gome.i18n.I18N;
import com.indigonauts.gome.sgf.Board;

public class Options extends Form {
  //#if DEBUG
  //private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("Options");
  //#endif

  private ChoiceGroup scrollerFont;

  private ChoiceGroup gobanColor;

  private ChoiceGroup ghostStone;

  private ChoiceGroup stoneBug;

  private ChoiceGroup scrollerSpeed;

  private ChoiceGroup scrollerSize;

  private ChoiceGroup optimize;

  //#if IGS 
  private TextField igsLogin;

  private TextField igsPassword;
  //#endif
  //#if IGS || BT
  private ChoiceGroup igsSize;

  private TextField igsMinutes;

  private TextField igsByoyomi;

  //#endif
  //#if IGS || BT
  private ChoiceGroup btService;
  //#endif

  private TextField email;

  private TextField user;

  private TextField key;

  private boolean registrationOnly;

  private static Image generateFillTest(byte mode, int width, int height) {
    Image temp = Image.createImage(width, height);
    int w = width / 2;
    int h = height / 2;
    int x = width / 4;
    int y = height / 4;
    Graphics g = temp.getGraphics();
    g.setColor(Util.GOBAN_COLOR_LIGHT);
    g.fillRect(0, 0, width, height);
    g.setColor(Util.COLOR_RED);
    g.drawArc(x, y, w, h, 0, 360);

    g.setColor(Util.COLOR_WHITE);
    switch (mode) {
    case Util.FILL_NORMAL:
      g.fillArc(x, y, w, h, 0, 360);
      break;
    case Util.FILL_BUG1:
      g.fillArc(x + 1, y + 1, w - 1, h - 1, 0, 360);
      break;

    }
    return Image.createImage(temp);
  }

  private static final int CHOICE_TYPE = Choice.POPUP;

  public Options(String title, CommandListener parent, boolean registrationOnly) throws IOException {
    super(title);
    this.registrationOnly = registrationOnly;
    Image smallLetter = null;
    Image mediumLetter = null;
    Image largeLetter = null;
    Image light = null;
    Image medium = null;
    Image dark = null;
    Image[] bugStones = new Image[2];
    Image[] transparentStones = new Image[6];

    //#if MENU_IMAGES
    int bestImageWidth = Gome.singleton.display.getBestImageWidth(Display.CHOICE_GROUP_ELEMENT);
    int bestImageHeight = Gome.singleton.display.getBestImageHeight(Display.CHOICE_GROUP_ELEMENT);
    smallLetter = Util.renderOffScreenTextIcon(
            "abc", bestImageWidth, bestImageHeight, Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL), Util.COLOR_BLACK, Util.COLOR_LIGHT_BACKGROUND); //$NON-NLS-1$
    mediumLetter = Util.renderOffScreenTextIcon(
            "abc", bestImageWidth, bestImageHeight, Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_MEDIUM), Util.COLOR_BLACK, Util.COLOR_LIGHT_BACKGROUND); //$NON-NLS-1$
    largeLetter = Util.renderOffScreenTextIcon(
            "abc", bestImageWidth, bestImageHeight, Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_LARGE), Util.COLOR_BLACK, Util.COLOR_LIGHT_BACKGROUND); //$NON-NLS-1$

    light = Util.renderIcon(Image.createImage("/glight.png"), bestImageWidth, bestImageHeight);
    medium = Util.renderIcon(Image.createImage("/gmedium.png"), bestImageWidth, bestImageHeight);
    dark = Util.renderIcon(Image.createImage("/gdark.png"), bestImageWidth, bestImageHeight);
    for (byte i = 0; i < 2; i++)
      bugStones[i] = generateFillTest(i, bestImageWidth, bestImageHeight);

    Image cachedStone = Image.createImage(bestImageWidth, bestImageHeight);
    Graphics g = cachedStone.getGraphics();
    g.setColor(Gome.singleton.options.gobanColor);
    g.fillRect(0, 0, bestImageWidth, bestImageHeight);
    BoardPainter.drawVectStone(g, bestImageWidth / 4, bestImageWidth / 4, bestImageWidth / 2, Board.BLACK);
    for (byte i = 0; i < 6; i++)
      transparentStones[i] = generateTransparencyTest(cachedStone, i * 32, bestImageWidth, bestImageHeight);
    //#endif

    scrollerFont = new ChoiceGroup(I18N.option.scrollerFont, CHOICE_TYPE); //$NON-NLS-1$
    scrollerFont.append(I18N.option.small, smallLetter); //$NON-NLS-1$
    scrollerFont.append(I18N.option.medium, mediumLetter); //$NON-NLS-1$
    scrollerFont.append(I18N.option.large, largeLetter); //$NON-NLS-1$
    scrollerFont.setSelectedIndex(Gome.singleton.options.getScrollerFontByte(), true);
    gobanColor = new ChoiceGroup(I18N.option.gobanColor, CHOICE_TYPE); //$NON-NLS-1$
    gobanColor.append(I18N.option.light, light); //$NON-NLS-1$//$NON-NLS-2$
    gobanColor.append(I18N.option.medium, medium); //$NON-NLS-1$ //$NON-NLS-2$
    gobanColor.append(I18N.option.dark, dark); //$NON-NLS-1$ //$NON-NLS-2$
    gobanColor.setSelectedIndex(Gome.singleton.options.getGobanColorByte(), true);

    ghostStone = new ChoiceGroup(I18N.option.ghostStone, CHOICE_TYPE); //$NON-NLS-1$

    for (byte i = 0; i < 6; i++)
      ghostStone.append("" + i * 10 + "%", transparentStones[i]); //$NON-NLS-1$//$NON-NLS-2$
    ghostStone.setSelectedIndex(Gome.singleton.options.ghostStone, true);
    stoneBug = new ChoiceGroup(I18N.option.stoneBug, CHOICE_TYPE); //$NON-NLS-1$
    for (int i = 0; i < 2; i++)
      stoneBug.append(I18N.option.stone + " " + (i + 1), bugStones[i]); //$NON-NLS-1$//$NON-NLS-2$
    stoneBug.setSelectedIndex(Gome.singleton.options.stoneBug, true);
    optimize = new ChoiceGroup(I18N.option.optimize, CHOICE_TYPE); //$NON-NLS-1$
    optimize.append(I18N.option.speed, null);
    optimize.append(I18N.option.memory, null);
    optimize.setSelectedIndex(Gome.singleton.options.optimize, true);

    scrollerSpeed = new ChoiceGroup(I18N.option.scrollerSpeed, CHOICE_TYPE); //$NON-NLS-1$
    scrollerSpeed.append(I18N.option.slow, null); //$NON-NLS-1$ //$NON-NLS-2$
    scrollerSpeed.append(I18N.option.medium, null); //$NON-NLS-1$ //$NON-NLS-2$
    scrollerSpeed.append(I18N.option.fast, null); //$NON-NLS-1$//$NON-NLS-2$
    scrollerSpeed.append(I18N.option.manual, null); //$NON-NLS-1$ //$NON-NLS-2$
    scrollerSpeed.setSelectedIndex(Gome.singleton.options.scrollerSpeed, true);

    scrollerSize = new ChoiceGroup(I18N.option.scrollerSize, CHOICE_TYPE); //$NON-NLS-1$
    scrollerSize.append(I18N.option.oneLiner, null); //$NON-NLS-1$ //$NON-NLS-2$
    scrollerSize.append(I18N.option.oneHalf, null); //$NON-NLS-1$ //$NON-NLS-2$
    scrollerSize.append(I18N.option.twoLiner, null); //$NON-NLS-1$//$NON-NLS-2$
    scrollerSize.append(I18N.option.twoHalf, null); //$NON-NLS-1$ //$NON-NLS-2$
    scrollerSize.setSelectedIndex(Gome.singleton.options.scrollerSize, true);
    //#if IGS
    igsLogin = new TextField(I18N.login, Gome.singleton.options.igsLogin, 10, TextField.ANY);

    igsPassword = new TextField(I18N.password, Gome.singleton.options.igsPassword, 10, TextField.ANY);
    //#endif

    //#if IGS || BT
    igsSize = new ChoiceGroup(I18N.option.igsSize, CHOICE_TYPE); //$NON-NLS-1$
    igsSize.append("9x9", null); //$NON-NLS-1$ //$NON-NLS-2$
    igsSize.append("13x13", null); //$NON-NLS-1$ //$NON-NLS-2$
    igsSize.append("19x19", null); //$NON-NLS-1$ //$NON-NLS-2$

    igsSize.setSelectedIndex(Gome.singleton.options.getIGSGobanSizeByte(), true);

    igsMinutes = new TextField(I18N.option.igsMinutes, String.valueOf(Gome.singleton.options.igsMinutes), 3, TextField.NUMERIC);

    igsByoyomi = new TextField(I18N.option.igsByoyomi, String.valueOf(Gome.singleton.options.igsByoyomi), 2, TextField.NUMERIC);
    //#endif

    //#if BT
    btService = new ChoiceGroup(I18N.option.bluetooth, CHOICE_TYPE); //$NON-NLS-1$
    btService.append(I18N.option.start, null); //$NON-NLS-1$ //$NON-NLS-2$
    btService.append(I18N.option.dontstart, null); //$NON-NLS-1$ //$NON-NLS-2$
    btService.setSelectedIndex(Gome.singleton.options.bluetooth, true);
    //#endif
    email = new TextField(I18N.option.email, String.valueOf(Gome.singleton.options.email), 80, TextField.EMAILADDR);
    user = new TextField(I18N.option.user, Gome.singleton.options.user, 30, TextField.ANY);
    key = new TextField(I18N.option.key, Gome.singleton.options.key, 32, TextField.ANY);

    if (!registrationOnly) {
      append(I18N.option.aspect);
      append(scrollerFont);
      append(gobanColor);
      append(ghostStone);
      append(scrollerSpeed);
      append(scrollerSize);
      append(I18N.option.compatibility);
      append(stoneBug);
      append(optimize);

      //#if IGS
      append(I18N.option.igs);
      append(igsLogin);
      append(igsPassword);
      //#endif
      //#if IGS || BT
      append(I18N.option.igsChallenge);
      append(igsSize);
      append(igsMinutes);
      append(igsByoyomi);
      //#endif
      //#if BT
      append(btService);
      //#endif
      append(email);
    }
    append(I18N.option.register);
    append(user);
    append(key);
    addCommand(MenuEngine.BACK);
    addCommand(MenuEngine.SAVE);
    setCommandListener(parent);
  }

  private Image generateTransparencyTest(Image cachedStone, int trans, int bestImageWidth, int bestImageHeight) {
    Image background = Image.createImage(bestImageWidth, bestImageHeight);
    Graphics g = background.getGraphics();
    g.setColor(Gome.singleton.options.gobanColor);
    g.fillRect(0, 0, bestImageWidth, bestImageHeight);
    g.setColor(Util.COLOR_DARKGREY);
    g.drawLine(bestImageWidth / 2, 0, bestImageWidth / 2, bestImageWidth);
    g.drawLine(0, bestImageWidth / 2, bestImageWidth, bestImageWidth / 2);
    int[] blackStoneRGB = new int[bestImageWidth * bestImageHeight];

    cachedStone.getRGB(blackStoneRGB, 0, bestImageWidth, 0, 0, bestImageWidth, bestImageHeight);
    int len = blackStoneRGB.length;
    for (int i = 0; i < len; i++) {
      blackStoneRGB[i] = trans << 24 | (blackStoneRGB[i] & 0x00FFFFFF); // get the color of the pixel.
    }
    g.drawRGB(blackStoneRGB, 0, bestImageWidth, 0, 0, bestImageWidth, bestImageHeight, true);
    return background;
  }

  public boolean save() {
    Gome singleton = Gome.singleton;
    GomeOptions options = singleton.options;
    options.setGobanColorFromByte((byte) gobanColor.getSelectedIndex());
    options.setScrollerFontFromByte((byte) scrollerFont.getSelectedIndex());
    options.scrollerSize = (byte) scrollerSize.getSelectedIndex();
    options.scrollerSpeed = (byte) scrollerSpeed.getSelectedIndex();
    options.stoneBug = (byte) stoneBug.getSelectedIndex();
    options.ghostStone = (byte) ghostStone.getSelectedIndex();
    options.optimize = (byte) optimize.getSelectedIndex();
    //#if IGS
    options.igsLogin = igsLogin.getString();
    options.igsPassword = igsPassword.getString();
    options.igsMinutes = Integer.parseInt(igsMinutes.getString());
    options.igsByoyomi = Integer.parseInt(igsByoyomi.getString());
    //#endif

    //#if BT
    options.bluetooth = (byte) btService.getSelectedIndex();
    if (options.bluetooth == 0 && singleton.bluetoothServiceConnector == null)
      singleton.startBTService();
    //#endif

    options.email = email.getString();
    options.user = user.getString().trim();
    options.key = key.getString().toLowerCase();

    if (options.user.length() != 0 && !Util.keygen(options.user).equals(options.key)) {
      Util.messageBox(I18N.option.invalidKey, I18N.option.invalidKeyExplanation, AlertType.ERROR); //$NON-NLS-1$ //$NON-NLS-2$
      return false;
    }

    if (!registrationOnly) {
      //#if IGS
      options.setIGSGobanSizeFromByte((byte) igsSize.getSelectedIndex());
      //#endif
      singleton.mainCanvas.stopScroller();// stop the scroller in
      // order to
      // change its font
      singleton.mainCanvas.recalculateLayout();// change the
      // proportions
      singleton.gameController.refreshPainter(); // the painter can
      // affected
      singleton.gameController.tuneBoardPainter();// to change the
    }

    try {
      singleton.saveOptions();
    } catch (Exception e) {
      Util.messageBox(I18N.error.error, e.getMessage(), AlertType.ERROR); //$NON-NLS-1$
    }
    return true;
  }

}
