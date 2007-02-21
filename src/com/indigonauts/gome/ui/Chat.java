/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome.ui;

import java.util.Vector;

import javax.microedition.lcdui.Choice;
import javax.microedition.lcdui.Command;
import javax.microedition.lcdui.CommandListener;
import javax.microedition.lcdui.Display;
import javax.microedition.lcdui.Displayable;
import javax.microedition.lcdui.List;
import javax.microedition.lcdui.TextBox;
import javax.microedition.lcdui.TextField;

import com.indigonauts.gome.Gome;

public class Chat implements CommandListener {
    public static Command REPLY;
    public static Command SEND;

    private String[] premade = { "OK", "Yes", "No", "Hi!", "Enjoy the game!" };

    private Vector nicks = new Vector();

    private Vector messages = new Vector();

    private Display display;

    private TextBox chatBox;

    private List history;

    private Displayable messageReturnTo;

    public Chat(Display display) {
        this.display = display;
        REPLY = new Command(Gome.singleton.bundle.getString("ui.reply"), Command.SCREEN, 0); //$NON-NLS-1$
        SEND = new Command(Gome.singleton.bundle.getString("ui.send"), Command.SCREEN, 0); //$NON-NLS-1$

    }

    public synchronized void addMessage(String from, String message) {
        nicks.addElement(from);
        messages.addElement(message);
        if (nicks.size() > 5) {
            messages.removeElementAt(0);
            nicks.removeElementAt(0);
        }
    }

    public void commandAction(Command command, Displayable displayable) {
        if (displayable == history && (command == REPLY || command == List.SELECT_COMMAND)) {
            sendMessage((String) nicks.elementAt(history.getSelectedIndex()), (String) messages.elementAt(history
                    .getSelectedIndex()), "", history);
        } else if (displayable == chatBox && command == SEND) {
            Gome.singleton.gameController.sendOnlineMessage(nickToSend, chatBox.getString());
            display.setCurrent(Gome.singleton.mainCanvas);
            Gome.singleton.mainCanvas.setSplashInfo(Gome.singleton.bundle.getString("online.messageSent"));
        } else if (displayable == chatBox && command == MenuEngine.BACK) {
            display.setCurrent(messageReturnTo);
        } else if (displayable == history && command == MenuEngine.BACK) {
            display.setCurrent(Gome.singleton.mainCanvas);
        } else if (displayable == history) {
            sendMessage((String) nicks.elementAt(history.getSelectedIndex()), (String) messages.elementAt(history
                    .getSelectedIndex()), removeQuote(command.getLabel()), history);
        } else if (displayable == chatBox) {
            chatBox.setString(removeQuote(command.getLabel()));
        }
    }

    private String removeQuote(String label) {
        return label.substring(1, label.length() - 1);
    }

    public void showMessageHistory() {
        history = new List("Chat History", Choice.IMPLICIT);

        for (int i = 0; i < messages.size(); i++) {
            history.append(nicks.elementAt(i) + ": " + messages.elementAt(i), null);

        }
        history.addCommand(REPLY);
        history.addCommand(MenuEngine.BACK);
        for (int i = 0; i < premade.length; i++) {
            history.addCommand(new Command("\"" + premade[i] + "\"", Command.SCREEN, 5));
        }

        history.setCommandListener(this);
        history.setSelectedIndex(history.size() - 1, true);
        display.setCurrent(history);

    }

    private String nickToSend;

    public void sendMessage(String nick, String lastMessage, String prevalue, Displayable returnTo) {
        nickToSend = nick;
        messageReturnTo = returnTo;
        String prompt = lastMessage != null ? nick + ": " + lastMessage : "To " + nick;
        chatBox = new TextBox(prompt, prevalue, 255, TextField.ANY);
        chatBox.addCommand(SEND);
        chatBox.addCommand(MenuEngine.BACK);
        for (int i = 0; i < premade.length; i++) {
            chatBox.addCommand(new Command("\"" + premade[i] + "\"", Command.SCREEN, 5));
        }
        chatBox.setCommandListener(this);
        display.setCurrent(chatBox);
    }

}
