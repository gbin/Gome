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
import javax.microedition.rms.RecordStoreException;

import com.indigonauts.gome.i18n.I18N;
import com.indigonauts.gome.io.DownloadStatus;
import com.indigonauts.gome.io.FileEntry;
import com.indigonauts.gome.io.IOManager;

public abstract class Fetcher extends Form implements CommandListener, DownloadStatus {
  public static final byte TERMINATED = -100;

  public static final byte READY = 0;

  public static final byte WORKING = 1;

  public static final byte FAIL = 50;

  public static final byte SUCCESS = 100;

  protected Gauge gauge;

  protected int status = -1;

  protected FileEntry entry;

  private Displayable prevWindow;

  private Display display;

  private Command ok;
  private TextField login;
  private TextField password;
  private String pwdFile;
  protected FetcherDownloadThread thread;

  public Fetcher()
  {
    super(I18N.download_inprogress); //$NON-NLS-1$
  }
  
  public Fetcher(FileEntry entry) {
    super(I18N.download_inprogress); //$NON-NLS-1$
    this.entry = entry;
    setup();
  }
  
  protected void setup()
  {
    gauge = new Gauge(entry.getUrl(), false, 100, 0);
    status = READY;

    this.append(gauge);
    this.addCommand(MenuEngine.BACK);
    this.setCommandListener(this);
  }

  protected void start() {
    thread = new FetcherDownloadThread(this);
    thread.start();
  }

  public void setPercent(int percent) {
    gauge.setValue(percent);
  }

  protected abstract void download() throws IOException;

  protected abstract void downloadFinished();

  public void requestLoginPassword(String pwdFile1) {
    ok = new Command(I18N.login, Command.OK, 1);
    login = new TextField(I18N.login, "", 32, TextField.ANY);
    password = new TextField(I18N.password, "", 32, TextField.PASSWORD);
    this.pwdFile = pwdFile1;
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
    if (c == ok) {
      try {
        IOManager.singleton.storeLoginPassword(pwdFile, login.getString(), password.getString());
      } catch (RecordStoreException e) {
       // nothing to do it will loop
      }
      this.delete(0);
      this.delete(0);
      this.append(gauge);
      this.removeCommand(ok);
      start();  
      return;
    }
    status = TERMINATED;
    hide();
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
