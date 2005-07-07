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
import com.indigonauts.gome.common.Util;
import com.indigonauts.gome.sgf.Board;

public class Options extends Form {
  //#ifdef DEBUG
  private static final org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger("Options");
  //#endif

  private ChoiceGroup lang;

  private ChoiceGroup scrollerFont;

  private ChoiceGroup gobanColor;

  private ChoiceGroup ghostStone;

  private ChoiceGroup stoneBug;

  private ChoiceGroup scrollerSpeed;

  private ChoiceGroup scrollerSize;

  private ChoiceGroup optimize;

  //#ifdef IGS
  private TextField igsLogin;

  private TextField igsPassword;

  private ChoiceGroup igsSize;

  private TextField igsMinutes;

  private TextField igsByoyomi;

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
    Image en = null;
    Image fr = null;
    Image light = null;
    Image medium = null;
    Image dark = null;
    Image jp = null;
    Image[] bugStones = new Image[2];
    Image[] transparentStones = new Image[6];

    //#ifdef MENU_IMAGES
    int bestImageWidth = Gome.singleton.display.getBestImageWidth(Display.CHOICE_GROUP_ELEMENT);
    int bestImageHeight = Gome.singleton.display.getBestImageHeight(Display.CHOICE_GROUP_ELEMENT);
    smallLetter = Util.renderOffScreenTextIcon(
            "abc", bestImageWidth, bestImageHeight, Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_SMALL), Util.COLOR_BLACK, Util.COLOR_LIGHT_BACKGROUND); //$NON-NLS-1$
    mediumLetter = Util.renderOffScreenTextIcon(
            "abc", bestImageWidth, bestImageHeight, Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_MEDIUM), Util.COLOR_BLACK, Util.COLOR_LIGHT_BACKGROUND); //$NON-NLS-1$
    largeLetter = Util.renderOffScreenTextIcon(
            "abc", bestImageWidth, bestImageHeight, Font.getFont(Font.FACE_PROPORTIONAL, Font.STYLE_PLAIN, Font.SIZE_LARGE), Util.COLOR_BLACK, Util.COLOR_LIGHT_BACKGROUND); //$NON-NLS-1$

    //#ifdef I18N
    en = Util.renderIcon(Image.createImage("/en.png"), bestImageWidth, bestImageHeight);
    fr = Util.renderIcon(Image.createImage("/fr.png"), bestImageWidth, bestImageHeight);
    jp = Util.renderIcon(Image.createImage("/jp.png"), bestImageWidth, bestImageHeight);
    //#endif
    light = Util.renderIcon(Image.createImage("/glight.png"), bestImageWidth, bestImageHeight);
    medium = Util.renderIcon(Image.createImage("/gmedium.png"), bestImageWidth, bestImageHeight);
    dark = Util.renderIcon(Image.createImage("/gdark.png"), bestImageWidth, bestImageHeight);
    for (byte i = 0; i < 2; i++)
      bugStones[i] = generateFillTest(i, bestImageWidth, bestImageHeight);

    Image cachedStone = Image.createImage(bestImageWidth, bestImageHeight);
    Graphics g = cachedStone.getGraphics();
    g.setColor(Gome.singleton.options.gobanColor);
    g.fillRect(0, 0, bestImageWidth, bestImageHeight);
    BoardPainter.drawStone(g, bestImageWidth / 4, bestImageWidth / 4, bestImageWidth / 2, Board.BLACK);
    for (byte i = 0; i < 6; i++)
      transparentStones[i] = generateTransparencyTest(cachedStone, i * 32, bestImageWidth, bestImageHeight);
    //#endif

    //#ifdef I18N
    lang = new ChoiceGroup(Gome.singleton.bundle.getString("ui.option.lang"), CHOICE_TYPE);

    lang.append(Gome.singleton.bundle.getString("ui.option.en"), en);
    lang.append(Gome.singleton.bundle.getString("ui.option.fr"), fr); //$NON-NLS-1$ //$NON-NLS-2$
    lang.append(Gome.singleton.bundle.getString("ui.option.jp"), jp); //$NON-NLS-1$ //$NON-NLS-2$
    lang.setSelectedIndex(Gome.singleton.options.getLocaleByte(), true);
    //#endif

    scrollerFont = new ChoiceGroup(Gome.singleton.bundle.getString("ui.option.scrollerFont"), CHOICE_TYPE); //$NON-NLS-1$
    scrollerFont.append(Gome.singleton.bundle.getString("ui.option.small"), smallLetter); //$NON-NLS-1$
    scrollerFont.append(Gome.singleton.bundle.getString("ui.option.medium"), mediumLetter); //$NON-NLS-1$
    scrollerFont.append(Gome.singleton.bundle.getString("ui.option.large"), largeLetter); //$NON-NLS-1$
    scrollerFont.setSelectedIndex(Gome.singleton.options.getScrollerFontByte(), true);
    gobanColor = new ChoiceGroup(Gome.singleton.bundle.getString("ui.option.gobanColor"), CHOICE_TYPE); //$NON-NLS-1$
    gobanColor.append(Gome.singleton.bundle.getString("ui.option.light"), light); //$NON-NLS-1$//$NON-NLS-2$
    gobanColor.append(Gome.singleton.bundle.getString("ui.option.medium"), medium); //$NON-NLS-1$ //$NON-NLS-2$
    gobanColor.append(Gome.singleton.bundle.getString("ui.option.dark"), dark); //$NON-NLS-1$ //$NON-NLS-2$
    gobanColor.setSelectedIndex(Gome.singleton.options.getGobanColorByte(), true);

    ghostStone = new ChoiceGroup(Gome.singleton.bundle.getString("ui.option.ghostStone"), CHOICE_TYPE); //$NON-NLS-1$

    for (byte i = 0; i < 6; i++)
      ghostStone.append("" + i * 10 + "%", transparentStones[i]); //$NON-NLS-1$//$NON-NLS-2$
    ghostStone.setSelectedIndex(Gome.singleton.options.ghostStone, true);
    stoneBug = new ChoiceGroup(Gome.singleton.bundle.getString("ui.option.stoneBug"), CHOICE_TYPE); //$NON-NLS-1$
    for (int i = 0; i < 2; i++)
      stoneBug.append(Gome.singleton.bundle.getString("ui.option.stone") + " " + (i + 1), bugStones[i]); //$NON-NLS-1$//$NON-NLS-2$
    stoneBug.setSelectedIndex(Gome.singleton.options.stoneBug, true);
    optimize = new ChoiceGroup(Gome.singleton.bundle.getString("ui.option.optimize"), CHOICE_TYPE); //$NON-NLS-1$
    optimize.append(Gome.singleton.bundle.getString("ui.option.speed"), null);
    optimize.append(Gome.singleton.bundle.getString("ui.option.memory"), null);
    optimize.setSelectedIndex(Gome.singleton.options.optimize, true);

    scrollerSpeed = new ChoiceGroup(Gome.singleton.bundle.getString("ui.option.scrollerSpeed"), CHOICE_TYPE); //$NON-NLS-1$
    scrollerSpeed.append(Gome.singleton.bundle.getString("ui.option.slow"), null); //$NON-NLS-1$ //$NON-NLS-2$
    scrollerSpeed.append(Gome.singleton.bundle.getString("ui.option.medium"), null); //$NON-NLS-1$ //$NON-NLS-2$
    scrollerSpeed.append(Gome.singleton.bundle.getString("ui.option.fast"), null); //$NON-NLS-1$//$NON-NLS-2$
    scrollerSpeed.append(Gome.singleton.bundle.getString("ui.option.manual"), null); //$NON-NLS-1$ //$NON-NLS-2$
    scrollerSpeed.setSelectedIndex(Gome.singleton.options.scrollerSpeed, true);

    scrollerSize = new ChoiceGroup(Gome.singleton.bundle.getString("ui.option.scrollerSize"), CHOICE_TYPE); //$NON-NLS-1$
    scrollerSize.append(Gome.singleton.bundle.getString("ui.option.oneLiner"), null); //$NON-NLS-1$ //$NON-NLS-2$
    scrollerSize.append(Gome.singleton.bundle.getString("ui.option.oneHalf"), null); //$NON-NLS-1$ //$NON-NLS-2$
    scrollerSize.append(Gome.singleton.bundle.getString("ui.option.twoLiner"), null); //$NON-NLS-1$//$NON-NLS-2$
    scrollerSize.append(Gome.singleton.bundle.getString("ui.option.twoHalf"), null); //$NON-NLS-1$ //$NON-NLS-2$
    scrollerSize.setSelectedIndex(Gome.singleton.options.scrollerSize, true);
    //#ifdef IGS
    igsLogin = new TextField(Gome.singleton.bundle.getString("ui.login"), Gome.singleton.options.igsLogin, 10, TextField.ANY);

    igsPassword = new TextField(Gome.singleton.bundle.getString("ui.password"), Gome.singleton.options.igsPassword, 10, TextField.ANY);

    igsSize = new ChoiceGroup(Gome.singleton.bundle.getString("ui.option.igsSize"), CHOICE_TYPE); //$NON-NLS-1$
    igsSize.append("9x9", null); //$NON-NLS-1$ //$NON-NLS-2$
    igsSize.append("13x13", null); //$NON-NLS-1$ //$NON-NLS-2$
    igsSize.append("19x19", null); //$NON-NLS-1$ //$NON-NLS-2$

    igsSize.setSelectedIndex(Gome.singleton.options.getIGSGobanSizeByte(), true);

    igsMinutes = new TextField(Gome.singleton.bundle.getString("ui.option.igsMinutes"), String.valueOf(Gome.singleton.options.igsMinutes), 3, TextField.NUMERIC);

    igsByoyomi = new TextField(Gome.singleton.bundle.getString("ui.option.igsByoyomi"), String.valueOf(Gome.singleton.options.igsByoyomi), 2, TextField.NUMERIC);
    //#endif
    email = new TextField(Gome.singleton.bundle.getString("ui.option.email"), String.valueOf(Gome.singleton.options.email), 80, TextField.EMAILADDR);

    user = new TextField(Gome.singleton.bundle.getString("ui.option.user"), Gome.singleton.options.user, 30, TextField.ANY);

    key = new TextField(Gome.singleton.bundle.getString("ui.option.key"), Gome.singleton.options.key, 32, TextField.ANY);

    if (!registrationOnly) {
      //#ifdef I18N
      append(lang);
      //#endif
      append(Gome.singleton.bundle.getString("ui.option.aspect"));
      append(scrollerFont);
      append(gobanColor);
      append(ghostStone);
      append(scrollerSpeed);
      append(scrollerSize);
      append(Gome.singleton.bundle.getString("ui.option.compatibility"));
      append(stoneBug);
      append(optimize);

      //#ifdef IGS
      append(Gome.singleton.bundle.getString("ui.option.igs"));
      append(igsLogin);
      append(igsPassword);
      append(Gome.singleton.bundle.getString("ui.option.igsChallenge"));
      append(igsSize);
      append(igsMinutes);
      append(igsByoyomi);
      //#endif
      append(email);
    }
    append(Gome.singleton.bundle.getString("ui.option.register"));
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
    String prev_locale = Gome.singleton.options.locale;
    //#ifdef I18N
    Gome.singleton.options.setLocaleFromByte((byte) lang.getSelectedIndex());
    //#endif
    Gome.singleton.options.setGobanColorFromByte((byte) gobanColor.getSelectedIndex());
    Gome.singleton.options.setScrollerFontFromByte((byte) scrollerFont.getSelectedIndex());
    Gome.singleton.options.scrollerSize = (byte) scrollerSize.getSelectedIndex();
    Gome.singleton.options.scrollerSpeed = (byte) scrollerSpeed.getSelectedIndex();
    Gome.singleton.options.stoneBug = (byte) stoneBug.getSelectedIndex();
    Gome.singleton.options.ghostStone = (byte) ghostStone.getSelectedIndex();
    Gome.singleton.options.optimize = (byte) optimize.getSelectedIndex();
    //#ifdef IGS
    Gome.singleton.options.igsLogin = igsLogin.getString();
    Gome.singleton.options.igsPassword = igsPassword.getString();
    Gome.singleton.options.igsMinutes = Integer.parseInt(igsMinutes.getString());
    Gome.singleton.options.igsByoyomi = Integer.parseInt(igsByoyomi.getString());
    //#endif
    Gome.singleton.options.email = email.getString();
    Gome.singleton.options.user = user.getString().trim();
    Gome.singleton.options.key = key.getString().toLowerCase();

    if (Gome.singleton.options.user.length() != 0 && !Util.keygen(Gome.singleton.options.user).equals(Gome.singleton.options.key)) {
      Util.messageBox(Gome.singleton.bundle.getString("ui.option.invalidKey"), Gome.singleton.bundle.getString("ui.option.invalidKeyExplanation"), AlertType.ERROR); //$NON-NLS-1$ //$NON-NLS-2$
      return false;
    }

    if (!registrationOnly) {
      //#ifdef IGS
      Gome.singleton.options.setIGSGobanSizeFromByte((byte) igsSize.getSelectedIndex());
      //#endif
      Gome.singleton.mainCanvas.stopScroller();// stop the scroller in
      // order to
      // change its font
      Gome.singleton.mainCanvas.recalculateLayout();// change the
      // proportions
      Gome.singleton.gameController.refreshPainter(); // the painter can
      // affected
      Gome.singleton.gameController.tuneBoardPainter();// to change the
      // color
      if (!Gome.singleton.options.locale.equals(prev_locale)) {
        Util.messageBox(Gome.singleton.bundle.getString("ui.needReboot"), Gome.singleton.bundle.getString("ui.changedLanguage"), AlertType.WARNING); //$NON-NLS-1$ //$NON-NLS-2$
      }
    }

    try {
      Gome.singleton.saveOptions();
    } catch (Exception e) {
      Util.messageBox(Gome.singleton.bundle.getString("ui.error"), e.getMessage(), AlertType.ERROR); //$NON-NLS-1$
    }
    return true;
  }

}
