/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome.igs;

import java.io.DataInputStream;
import java.io.IOException;

public class ServerMove {
    public int nb;
    public byte color;
    public byte x;
    public byte y;

    /**
     * @param color
     * @param x
     * @param y
     */
    public ServerMove(int nb, byte color, byte x, byte y) {
        this.nb = nb;
        this.color = color;
        this.x = x;
        this.y = y;
    }

    private ServerMove() {
        // private constructor
    }

    public static ServerMove unmarshal(DataInputStream in) throws IOException {
        ServerMove newOne = new ServerMove();

        newOne.nb = in.readInt();
        newOne.color = in.readByte();
        newOne.x = in.readByte();
        newOne.y = in.readByte();
        return newOne;
    }
}
