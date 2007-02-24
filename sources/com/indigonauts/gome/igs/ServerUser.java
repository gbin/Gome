/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome.igs;

import java.io.DataInputStream;
import java.io.IOException;

public class ServerUser {
    public String nick;
    public String rank;
    public int play;
    public int watch;

    public static ServerUser unmarshal(DataInputStream in) throws IOException {
        ServerUser user = new ServerUser();
        user.nick = in.readUTF();
        user.rank = in.readUTF();
        user.play = in.readInt();
        user.watch = in.readInt();
        return user;
    }

    public String toString() {
        return nick + "(" + rank + ")";
    }

}
