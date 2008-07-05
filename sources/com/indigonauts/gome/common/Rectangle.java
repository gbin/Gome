/*
 * (c) 2006 Indigonauts
 */

package com.indigonauts.gome.common;

public class Rectangle {

  public int x0;
  public int y0;
  public int x1;
  public int y1;

  public Rectangle(int nx0, int ny0, int nx1, int ny1) {
    x0 = nx0;
    y0 = ny0;
    x1 = nx1;
    y1 = ny1;
  }

  public Rectangle(Point a, Point b) {
    x0 = a.x;
    y0 = a.y;
    x1 = b.x;
    y1 = b.y;
  }

  public Rectangle() {
    // nothing
  }

  /**
   * if the point is not in the area, resize the rectangle to include it
   * 
   * @param pt
   */
  public void resizeForPoint(int px, int py) {
    x0 = Math.min(x0, px);
    x1 = Math.max(x1, px);
    y0 = Math.min(y0, py);
    y1 = Math.max(y1, py);
  }

  public void resizeForPoint(Point pt) {
    resizeForPoint(pt.x, pt.y);
  }

  public boolean isValid() {
    return !(x0 > x1 || y0 > y1);
  }

  public static Rectangle union(Rectangle r1, Rectangle r2) {
    if (r1 == null)
      return r2;
    if (r2 == null)
      return r1;

    Rectangle result = new Rectangle();

    result.x0 = Math.min(r1.x0, r2.x0);
    result.y0 = Math.min(r1.y0, r2.y0);
    result.x1 = Math.max(r1.x1, r2.x1);
    result.y1 = Math.max(r1.y1, r2.y1);

    return result;

  }

  public int getWidth() {
    return (x1 - x0 + 1);
  }

  public int getHeight() {
    return (y1 - y0 + 1);
  }

  public void grow(int dx, int dy) {
    x0 -= dx;
    y0 -= dy;
    x1 += dx;
    y1 += dy;
  }

  public void move(int dx, int dy) {
    x0 += dx;
    y0 += dy;
    x1 += dx;
    y1 += dy;
  }

  public static Rectangle intersect(Rectangle r1, Rectangle r2) {
    Rectangle r = new Rectangle();

    r.x0 = Math.max(r1.x0, r2.x0);
    r.y0 = Math.max(r1.y0, r2.y0);
    r.x1 = Math.min(r1.x1, r2.x1);
    r.y1 = Math.min(r1.y1, r2.y1);

    return r;
  }

  public String toString() {
    return "(=" + x0 + "," + y0 + ")-(" + x1 + "," + y1 + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
  }

  public boolean contains(Point pt) {
    return pt.x >= x0 && pt.x <= x1 && pt.y >= y0 && pt.y <= y1;
  }

  public boolean contains(int xx, int yy) {
    return xx >= x0 && xx <= x1 && yy >= y0 && yy <= y1;
  }

  public boolean equals(Object obj) {
    if (!(obj instanceof Rectangle))
      return false;

    Rectangle other = (Rectangle) obj;
    return this.x0 == other.x0 && this.y0 == other.y0 && this.x1 == other.x1 && this.y1 == other.y1;
  }
}