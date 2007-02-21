/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome.ui;

import java.io.IOException;

import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.Form;
import javax.microedition.lcdui.Gauge;
import javax.microedition.lcdui.TextField;

import com.indigonauts.gome.Gome;
import com.indigonauts.gome.io.DownloadStatus;

public abstract class Fetcher extends Form implements CommandListener, DownloadStatus {
    public static final byte TERMINATED = -100;

    public static final byte READY = 0;

    public static final byte WORKING = 1;

    public static final byte FAIL = 50;

    public static final byte SUCCESS = 100;

    protected Gauge gauge;

    int status = -1;

    protected String url;

    private Displayable prevWindow;

    private Display display;

    private Command ok;
    private TextField login;
    private TextField password;

    public Fetcher(String url) {
        super(Gome.singleton.bundle.getString("ui.download.inprogress")); //$NON-NLS-1$
        gauge = new Gauge(url, false, 100, 0);
        status = READY;

        this.append(gauge);
        this.addCommand(MenuEngine.BACK);
        this.setCommandListener(this);
        this.url = url;
    }

    protected void start() {
        new FetcherDownloadThread(this).start();
    }

    public void setPercent(int percent) {
        gauge.setValue(percent);
    }

    protected abstract void download() throws IOException;

    protected abstract void downloadFinished();

    public void requestLoginPassword() {
        ok = new Command(Gome.singleton.bundle.getString("ui.download.login"), Command.OK, 1);
        login = new TextField(Gome.singleton.bundle.getString("ui.download.login"), "", 32, TextField.ANY);
        password = new TextField(Gome.singleton.bundle.getString("ui.download.password"), "", 32, TextField.PASSWORD);
        this.delete(0);
        this.addCommand(ok);
        this.append(login);
        this.append(password);

    }

    public String getLogin() {
        return login.getString();
    }

    public String getPassword() {
        return password.getString();
    }

    protected abstract void downloadFailed(Exception reason);

    public void commandAction(Command c, Displayable d) {
        if (c == MenuEngine.BACK) {
            status = TERMINATED;
            hide();
        } else if (c == ok) {
            synchronized (this) {
                this.notify();
            }
        }
    }

    public void show(Display dis) {
        display = dis;
        prevWindow = dis.getCurrent();
        dis.setCurrent(this);
    }

    public void hide() {
        display.setCurrent(prevWindow);
    }

}
