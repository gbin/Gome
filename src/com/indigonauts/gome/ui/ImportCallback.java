/*
 * (c) 2005 Indigonauts
 */
package com.indigonauts.gome.ui;

import javax.microedition.lcdui.AlertType;

import com.indigonauts.gome.Gome;
import com.indigonauts.gome.common.Util;

public class ImportCallback {
    /**
     * 
     */
    private final FileBrowser browser;

    /**
     * @param browser
     */
    ImportCallback(FileBrowser browser) {
        this.browser = browser;
    }

    void done() {
        this.browser.listener.commandAction(MenuEngine.LOAD, Gome.singleton.mainCanvas);
    }

    public void failed(Exception reason) {
        Util
                .messageBox(
                            Gome.singleton.bundle.getString("ui.failure"), Gome.singleton.bundle.getString(reason.getMessage()), AlertType.ERROR); //$NON-NLS-1$
    }
}