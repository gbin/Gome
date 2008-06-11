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

  public void reverse() {
    color = (color == Board.BLACK) ? Board.WHITE : Board.BLACK;
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
