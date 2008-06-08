/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome.multiplayer;

import java.io.DataInputStream;
import java.io.IOException;

import com.indigonauts.gome.common.Point;
import com.indigonauts.gome.sgf.Board;

public class Move {
    public int nb;
    public byte color;
    public byte x;
    public byte y;

    /**
     * @param color
     * @param x
     * @param y
     */
    public Move(int nb, byte color, byte x, byte y) {
        this.nb = nb;
        this.color = color;
        this.x = x;
        this.y = y;
    }

    private Move() {
        // private constructor
    }

    public static Move unmarshal(DataInputStream in) throws IOException {
        Move newOne = new Move();

        newOne.nb = in.readInt();
        newOne.color = in.readByte();
        newOne.x = in.readByte();
        newOne.y = in.readByte();
        return newOne;
    }
    //#ifdef DEBUG
    public String toString() {
      return "Move #"+ nb +" " + ((color == Board.BLACK) ? "black":"white") + " " + ((x == Point.PASS) ? " Pass" : "") + x + "-" + y; 
      
    }
    //#endif
}
