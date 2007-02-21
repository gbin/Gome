/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome.ui;

import java.io.IOException;

import javax.microedition.lcdui.AlertType;
import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.ChoiceGroup;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Image;
import javax.microedition.lcdui.TextField;

import com.indigonauts.gome.Gome;
import com.indigonauts.gome.GomeOptions;
import com.indigonauts.gome.common.Util;

public class Options extends Form {
    private ChoiceGroup lang;

    private ChoiceGroup scrollerFont;

    private ChoiceGroup gobanColor;

    private ChoiceGroup scrollerSpeed;

    private ChoiceGroup scrollerSize;

    private TextField igsLogin;

    private TextField igsPassword;

    private ChoiceGroup igsSize;

    private TextField igsMinutes;

    private TextField igsByoyomi;

    private TextField email;

    private TextField user;

    private TextField key;

    private boolean registrationOnly;

    public Options(String title, CommandListener parent, boolean registrationOnly) throws IOException {
        super(title);
        this.registrationOnly = registrationOnly;
        Image smallLetter = Image.createImage(Util
                .renderOffScreenText("abc", 32, GomeOptions.FONT_SMALL, Util.COLOR_BLACK, Util.COLOR_LIGHT_BACKGROUND)); //$NON-NLS-1$
        Image mediumLetter = Image
                .createImage(Util
                        .renderOffScreenText(
                                             "abc", 32, GomeOptions.FONT_MEDIUM, Util.COLOR_BLACK, Util.COLOR_LIGHT_BACKGROUND)); //$NON-NLS-1$
        Image largeLetter = Image.createImage(Util
                .renderOffScreenText("abc", 32, GomeOptions.FONT_LARGE, Util.COLOR_BLACK, Util.COLOR_LIGHT_BACKGROUND)); //$NON-NLS-1$

        lang = new ChoiceGroup(Gome.singleton.bundle.getString("ui.option.lang"), Choice.EXCLUSIVE); //$NON-NLS-1$
        lang.append("", Image.createImage("/en.png"));

        lang.append("", Image.createImage("/fr.png")); //$NON-NLS-1$ //$NON-NLS-2$
        lang.append("", Image.createImage("/jp.png")); //$NON-NLS-1$ //$NON-NLS-2$
        lang.setSelectedIndex(Gome.singleton.options.getLocaleByte(), true);

        scrollerFont = new ChoiceGroup(Gome.singleton.bundle.getString("ui.option.scrollerFont"), Choice.EXCLUSIVE); //$NON-NLS-1$
        scrollerFont.append("", smallLetter); //$NON-NLS-1$
        scrollerFont.append("", mediumLetter); //$NON-NLS-1$
        scrollerFont.append("", largeLetter); //$NON-NLS-1$
        scrollerFont.setSelectedIndex(Gome.singleton.options.getScrollerFontByte(), true);
        gobanColor = new ChoiceGroup(Gome.singleton.bundle.getString("ui.option.gobanColor"), Choice.EXCLUSIVE); //$NON-NLS-1$
        gobanColor.append("", Image.createImage("/glight.png")); //$NON-NLS-1$//$NON-NLS-2$
        gobanColor.append("", Image.createImage("/gmedium.png")); //$NON-NLS-1$ //$NON-NLS-2$
        gobanColor.append("", Image.createImage("/gdark.png")); //$NON-NLS-1$ //$NON-NLS-2$
        gobanColor.setSelectedIndex(Gome.singleton.options.getGobanColorByte(), true);
        scrollerSpeed = new ChoiceGroup(Gome.singleton.bundle.getString("ui.option.scrollerSpeed"), Choice.EXCLUSIVE); //$NON-NLS-1$
        scrollerSpeed.append(Gome.singleton.bundle.getString("ui.option.slow"), null); //$NON-NLS-1$ //$NON-NLS-2$
        scrollerSpeed.append(Gome.singleton.bundle.getString("ui.option.medium"), null); //$NON-NLS-1$ //$NON-NLS-2$
        scrollerSpeed.append(Gome.singleton.bundle.getString("ui.option.fast"), null); //$NON-NLS-1$//$NON-NLS-2$
        scrollerSpeed.append(Gome.singleton.bundle.getString("ui.option.manual"), null); //$NON-NLS-1$ //$NON-NLS-2$
        scrollerSpeed.setSelectedIndex(Gome.singleton.options.scrollerSpeed, true);

        scrollerSize = new ChoiceGroup(Gome.singleton.bundle.getString("ui.option.scrollerSize"), Choice.EXCLUSIVE); //$NON-NLS-1$
        scrollerSize.append(Gome.singleton.bundle.getString("ui.option.oneLiner"), null); //$NON-NLS-1$ //$NON-NLS-2$
        scrollerSize.append(Gome.singleton.bundle.getString("ui.option.oneHalf"), null); //$NON-NLS-1$ //$NON-NLS-2$
        scrollerSize.append(Gome.singleton.bundle.getString("ui.option.twoLiner"), null); //$NON-NLS-1$//$NON-NLS-2$
        scrollerSize.append(Gome.singleton.bundle.getString("ui.option.twoHalf"), null); //$NON-NLS-1$ //$NON-NLS-2$
        scrollerSize.setSelectedIndex(Gome.singleton.options.scrollerSize, true);

        igsLogin = new TextField(Gome.singleton.bundle.getString("ui.option.igsLogin"),
                                 Gome.singleton.options.igsLogin, 10, TextField.ANY);

        igsPassword = new TextField(Gome.singleton.bundle.getString("ui.option.igsPassword"),
                                    Gome.singleton.options.igsPassword, 10, TextField.ANY);

        igsSize = new ChoiceGroup(Gome.singleton.bundle.getString("ui.option.igsSize"), Choice.EXCLUSIVE); //$NON-NLS-1$
        igsSize.append("9x9", null); //$NON-NLS-1$ //$NON-NLS-2$
        igsSize.append("13x13", null); //$NON-NLS-1$ //$NON-NLS-2$
        igsSize.append("19x19", null); //$NON-NLS-1$ //$NON-NLS-2$

        igsSize.setSelectedIndex(Gome.singleton.options.getIGSGobanSizeByte(), true);

        igsMinutes = new TextField(Gome.singleton.bundle.getString("ui.option.igsMinutes"), String
                .valueOf(Gome.singleton.options.igsMinutes), 3, TextField.NUMERIC);

        igsByoyomi = new TextField(Gome.singleton.bundle.getString("ui.option.igsByoyomi"), String
                .valueOf(Gome.singleton.options.igsByoyomi), 2, TextField.NUMERIC);

        email = new TextField(Gome.singleton.bundle.getString("ui.option.email"), String
                .valueOf(Gome.singleton.options.email), 80, TextField.EMAILADDR);

        user = new TextField(Gome.singleton.bundle.getString("ui.option.user"), Gome.singleton.options.user, 30,
                             TextField.ANY);

        key = new TextField(Gome.singleton.bundle.getString("ui.option.key"), Gome.singleton.options.key, 32,
                            TextField.ANY);

        if (!registrationOnly) {
            append(lang);
            append(scrollerFont);
            append(gobanColor);
            append(scrollerSpeed);
            append(scrollerSize);
            append(Gome.singleton.bundle.getString("ui.option.igs"));
            append(igsLogin);
            append(igsPassword);
            append(Gome.singleton.bundle.getString("ui.option.igsChallenge"));
            append(igsSize);
            append(igsMinutes);
            append(igsByoyomi);
            append(email);
        }
        append(Gome.singleton.bundle.getString("ui.option.register"));
        append(user);
        append(key);
        addCommand(MenuEngine.BACK);
        addCommand(MenuEngine.SAVE);
        setCommandListener(parent);
    }

    public boolean save() {
        String prev_locale = Gome.singleton.options.locale;
        Gome.singleton.options.setLocaleFromByte((byte) lang.getSelectedIndex());
        Gome.singleton.options.setGobanColorFromByte((byte) gobanColor.getSelectedIndex());
        Gome.singleton.options.setScrollerFontFromByte((byte) scrollerFont.getSelectedIndex());
        Gome.singleton.options.scrollerSize = (byte) scrollerSize.getSelectedIndex();
        Gome.singleton.options.scrollerSpeed = (byte) scrollerSpeed.getSelectedIndex();
        Gome.singleton.options.igsLogin = igsLogin.getString();
        Gome.singleton.options.igsPassword = igsPassword.getString();
        Gome.singleton.options.igsMinutes = Integer.parseInt(igsMinutes.getString());
        Gome.singleton.options.igsByoyomi = Integer.parseInt(igsByoyomi.getString());
        Gome.singleton.options.email = email.getString();
        Gome.singleton.options.user = user.getString().trim();
        Gome.singleton.options.key = key.getString().toLowerCase();

        if (Gome.singleton.options.user.length() != 0
            && !Util.keygen(Gome.singleton.options.user).equals(Gome.singleton.options.key)) {
            Util
                    .messageBox(
                                Gome.singleton.bundle.getString("ui.option.invalidKey"), Gome.singleton.bundle.getString("ui.option.invalidKeyExplanation"), AlertType.ERROR); //$NON-NLS-1$ //$NON-NLS-2$
            return false;
        }
        
        if (!registrationOnly) {
            Gome.singleton.options.setIGSGobanSizeFromByte((byte) igsSize.getSelectedIndex());
            Gome.singleton.mainCanvas.stopScroller();// stop the scroller in
            // order to
            // change its font
            Gome.singleton.mainCanvas.recalculateLayout();// change the
            // proportions
            Gome.singleton.gameController.refreshPainter(); // the painter can
            // affected
            Gome.singleton.gameController.paintBackBuffer();// to change the
                                                            // color
            if (!Gome.singleton.options.locale.equals(prev_locale)) {
                Util
                        .messageBox(
                                    Gome.singleton.bundle.getString("ui.needReboot"), Gome.singleton.bundle.getString("ui.changedLanguage"), AlertType.WARNING); //$NON-NLS-1$ //$NON-NLS-2$
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
