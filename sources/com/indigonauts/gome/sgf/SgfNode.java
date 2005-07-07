/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome.sgf;

import java.util.Vector;

import com.indigonauts.gome.common.Point;

public class SgfNode extends Point {

  private byte playerColor;

  private SgfNode father;
  private SgfNode son;
  private SgfNode older;
  private SgfNode younger;

  private String comment;
  private Vector deadStones;
  private Point ko; // localization of a played ko
  private Vector annotations;

  private Vector AB; // a vector of SgfPoints Black positions
  private Vector AW; // a vector of SgfPoints White positions

  private boolean correct = false;

  public void setPoint(SgfPoint newPoint) {
    x = newPoint.x;
    y = newPoint.y;
  }

  public SgfNode getOlder() {
    return older;
  }

  public SgfNode getFather() {
    return father;
  }

  public SgfNode searchFather() {
    if (father != null)
      return father;

    SgfNode bro = this.getOlder();

    while (bro != null) {
      if (bro.father != null)
        return bro.father;

      bro = bro.getOlder();
    }

    return null;
  }

  public void setFatherOnly(SgfNode newFather) {
    this.father = newFather;
  }

  public SgfNode getSon() {
    return son;
  }

  public void setSon(SgfNode newSon) {
    this.son = newSon;
    if (newSon != null)
      newSon.father = this;
  }

  public SgfNode getYounger() {
    return younger;
  }

  public void setYounger(SgfNode newYounger) {
    this.younger = newYounger;
    if (newYounger != null)
      newYounger.older = this;
  }

  public String getComment() {
    return comment;
  }

  public void setComment(String newComment) {
    comment = newComment;
  }

  public SgfPoint getPoint() {
    if (x == Point.PASS || x == Point.NO_POINT)
      return null;
    return new SgfPoint(x, y);
  }

  /*
   * public String getAttribute(String name) { if (attributes==null) return
   * null;
   * 
   * return (String)(attributes.get(name)); }
   * 
   * public void setAttribute(String name, String value) { if
   * (attributes==null) attributes=new Hashtable();
   * 
   * attributes.put(name, value); }
   */

  public SgfNode searchChildren(Point pt) {
    SgfNode mySon = this.getSon();

    while (mySon != null) {
      if (mySon.getPoint() != null) {
        if (mySon.getPoint().equals(pt))
          return mySon;
      }
      mySon = mySon.getYounger();
    }
    return null;
  }

  public Vector getChildren() {
    Vector vec = new Vector();

    SgfNode mySon = this.getSon();
    while (mySon != null) {
      vec.addElement(mySon);
      mySon = mySon.getYounger();
    }

    return vec;
  }

  public Vector getDeadStones() {
    return deadStones;
  }

  public byte getDeadColor() {
    return (byte) -playerColor;
  }

  public void setDeadStones(Vector deadStones) {
    this.deadStones = deadStones;
  }

  public SgfNode addBranch(SgfPoint pt) {
    SgfNode newSon = new SgfNode();
    newSon.setPoint(pt);

    if (this.son == null) {
      this.setSon(newSon);
    } else {
      SgfNode bro = this.getSon();
      while (bro.getYounger() != null) {
        bro = bro.getYounger();
      }
      bro.setYounger(newSon);
    }

    return newSon;
  }

  public byte getPlayerColor() {
    return playerColor;
  }

  public void setPlayerColor(byte playerColor) {
    this.playerColor = playerColor;
  }

  public void output(StringBuffer buf) {
    buf.append(';');

    if (x != Point.NO_POINT) {
      if (this.playerColor == 1)
        buf.append("B["); //$NON-NLS-1$
      else
        buf.append("W["); //$NON-NLS-1$

      buf.append(new SgfPoint(x, y));
      buf.append(']');
    }

    if (this.comment != null) {
      buf.append("C["); //$NON-NLS-1$
      buf.append(this.comment);
      buf.append(']');
    }
    if (AB != null) {
      buf.append("AB"); //$NON-NLS-1$
      for (int i = 0; i < AB.size(); ++i) {
        buf.append('[');
        buf.append(AB.elementAt(i));
        buf.append(']');
      }
    }

    if (AW != null) {
      buf.append("AW"); //$NON-NLS-1$
      for (int i = 0; i < AW.size(); ++i) {
        buf.append('[');
        buf.append(AW.elementAt(i));
        buf.append(']');
      }
    }
  }

  public Vector getAnnotations() {
    return annotations;
  }

  public void addAnnotation(Point a) {
    if (annotations == null) {
      annotations = new Vector();
    }
    annotations.addElement(a);
  }

  public void setKo(Point ko) {
    this.ko = ko;
  }

  /**
   * @return Returns the ko.
   */
  public Point getKo() {
    return ko;
  }

  /**
   * @return Returns the aB.
   */
  public Vector getAB() {
    return AB;
  }

  /**
   * @return Returns the aW.
   */
  public Vector getAW() {
    return AW;
  }

  public void addABElement(SgfPoint p) {
    if (AB == null)
      AB = new Vector();
    AB.addElement(p);
  }

  public void addAWElement(SgfPoint p) {
    if (AW == null)
      AW = new Vector();
    AW.addElement(p);
  }

  public String toString() {
    return "(" + x + "," + y + ")" + " c=" + playerColor; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
  }

  public boolean isPass() {
    return x == Point.PASS;
  }

  public void setPass() {
    x = Point.PASS;
  }

  public boolean isCorrect() {
    return correct;
  }

  public void setCorrect(boolean correct) {
    this.correct = correct;
  }
}
