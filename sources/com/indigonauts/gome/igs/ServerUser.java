/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome.igs;

import java.io.DataInputStream;
import java.io.IOException;

import com.indigonauts.gome.common.QuickSortable;
import com.indigonauts.gome.common.Util;

public class ServerUser extends QuickSortable {
  public String nick;
  public String rank;
  public int play;
  public int watch;

  public static final int NICK = 0;
  public static final int RANK = 1;
  public static final int PLAY = 2;
  public static final int WATCH = 3;

  public static int sortCriteria = RANK;
  public static boolean sortOrder = false;

  private boolean isDan;
  //private boolean isStar;
  private int numRank;

  public static ServerUser unmarshal(DataInputStream in) throws IOException {
    ServerUser user = new ServerUser();
    user.nick = in.readUTF();
    user.rank = in.readUTF();
    user.play = in.readInt();
    user.watch = in.readInt();
    user.isDan = user.rank.indexOf('d') != -1;
    //user.isStar = user.rank.indexOf('*') != -1;
    char second = user.rank.charAt(1);
    char first = user.rank.charAt(0);
    if (!Character.isDigit(second)) {
      user.numRank = (int) (first - '0');
    } else {
      user.numRank = (int) (10 * (first - '0')) + (second - '0');
    }
    return user;
  }

  public boolean lessThan(QuickSortable other) {

    ServerUser otherUser;
    ServerUser meUser;
    if (sortOrder) {
      otherUser = ((ServerUser) other);
      meUser = this;
    } else {
      meUser = ((ServerUser) other);
      otherUser = this;
    }
    switch (sortCriteria) {
    case NICK:
      return meUser.nick.compareTo(otherUser.nick) < 0;
    case RANK:
      if (otherUser.isDan && !meUser.isDan)
        return true;
      else if (!otherUser.isDan && meUser.isDan)
        return false;
      else if (meUser.isDan)
        return meUser.numRank < otherUser.numRank;
      else
        return meUser.numRank > otherUser.numRank;
    case PLAY:
      return meUser.play < otherUser.play;
    case WATCH:
      return meUser.watch < otherUser.watch;
    }

    return false;
  }

  public String toString() {
    return Util.padd(nick,10,true)+ " [" + Util.padd(rank,4,true) + "] " + ((play != -1) ? " Playing #" + play : "") + ((watch != -1) ? " Watching #" + watch : "");
  }
}
