/*
 * (c) 2006 Indigonauts
 */

package com.indigonauts.gome.common;

public class Rectangle {
    byte x0;

    byte y0;

    byte x1;

    byte y1;

    public Rectangle(byte nx0, byte ny0, byte nx1, byte ny1) {
        x0 = nx0;
        y0 = ny0;
        x1 = nx1;
        y1 = ny1;
    }

    public Rectangle(Point a, Point b) {
        x0 = a.getX();
        y0 = a.getY();
        x1 = b.getX();
        y1 = b.getY();
    }

    public Rectangle() {
        // nothing
    }

    /**
     * if the point is not in the area, resize the rectangle to include it
     * 
     * @param pt
     */
    public void resizeForPoint(byte px, byte py) {
        x0 = (byte) Math.min(x0, px);
        x1 = (byte) Math.max(x1, px);
        y0 = (byte) Math.min(y0, py);
        y1 = (byte) Math.max(y1, py);
    }

    public void resizeForPoint(Point pt) {
        resizeForPoint(pt.getX(), pt.getY());
    }

    public final byte getX0() {
        return x0;
    }

    public final void setX0(byte x0) {
        this.x0 = x0;
    }

    public final byte getX1() {
        return x1;
    }

    public final void setX1(byte x1) {
        this.x1 = x1;
    }

    public final byte getY0() {
        return y0;
    }

    public final void setY0(byte y0) {
        this.y0 = y0;
    }

    public final byte getY1() {
        return y1;
    }

    public final void setY1(byte y1) {
        this.y1 = y1;
    }

    public boolean isValid() {
        if (x0 > x1 || y0 > y1)
            return false;
        return true;
    }

    public static Rectangle union(Rectangle r1, Rectangle r2) {
        if (r1 == null || r2 == null)
            return null;

        Rectangle result = new Rectangle();

        result.x0 = (byte) Math.min(r1.x0, r2.x0);
        result.y0 = (byte) Math.min(r1.y0, r2.y0);
        result.x1 = (byte) Math.max(r1.x1, r2.x1);
        result.y1 = (byte) Math.max(r1.y1, r2.y1);

        return result;

    }

    public byte getWidth() {
        return (byte) (x1 - x0 + 1);
    }

    public byte getHeight() {
        return (byte) (y1 - y0 + 1);
    }

    public void grow(byte dx, byte dy) {
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

        r.x0 = (byte) Math.max(r1.x0, r2.x0);
        r.y0 = (byte) Math.max(r1.y0, r2.y0);
        r.x1 = (byte) Math.min(r1.x1, r2.x1);
        r.y1 = (byte) Math.min(r1.y1, r2.y1);

        return r;
    }

    public String toString() {
        return "(=" + x0 + "," + y0 + ")-(" + x1 + "," + y1 + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
    }

    public boolean contains(Point pt) {
        if (pt.getX() >= x0 && pt.getX() <= x1 && pt.getY() >= y0 && pt.getY() <= y1)
            return true;

        return false;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Rectangle))
            return false;

        Rectangle other = (Rectangle) obj;
        return this.x0 == other.x0 && this.y0 == other.y0 && this.x1 == other.x1 && this.y1 == other.y1;
    }
}