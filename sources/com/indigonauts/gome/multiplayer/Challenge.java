/*
 * (c) 2006 Indigonauts
 */

package com.indigonauts.gome.multiplayer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import com.indigonauts.gome.sgf.Board;

public class Challenge {
  public String nick;
  public byte color;
  public byte size;
  public int time_minutes;
  public int min_per25moves;

  public void marshall(DataOutputStream output) throws IOException {
    output.writeUTF(nick);
    output.writeByte(color);
    output.writeByte(size);
    output.writeInt(time_minutes);
    output.writeInt(min_per25moves);
  }

  public Challenge reverse() {
    Challenge t = new Challenge();
    t.nick = nick;
    t.color = color == Board.BLACK ? Board.WHITE : Board.BLACK;
    t.size = size;
    t.time_minutes = time_minutes;
    t.min_per25moves = min_per25moves;
    return t;
  }

  public static Challenge unmarshal(DataInputStream input) throws IOException {
    Challenge challenge = new Challenge();
    challenge.nick = input.readUTF();
    challenge.color = input.readByte();
    challenge.size = input.readByte();
    challenge.time_minutes = input.readInt();
    challenge.min_per25moves = input.readInt();
    return challenge;
  }

}
