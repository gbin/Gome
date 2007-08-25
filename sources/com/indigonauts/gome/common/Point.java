/*
 * (c) 2006 Indigonauts
 */

package com.indigonauts.gome.common;

public class Point {
    public static final byte NO_POINT = -2;
    public static final byte PASS = -1;
    public byte x= NO_POINT;
    public byte y= NO_POINT;

    public Point() {
      // nothing to do
    }

    public Point(Point pt) {
        setLocation(pt.x, pt.y);
    }

    public Point(byte nx, byte ny) {
        setLocation(nx, ny);
    }

    public void setLocation(byte nx, byte ny) {
        x = nx;
        y = ny;
    }

    public void move(byte dx, byte dy) {
        x += dx;
        y += dy;
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }

        if (!(obj instanceof Point)) {
            return false;
        }

        Point pt = (Point) obj;

        if ((this.x == pt.x) && (this.y == pt.y)) {
            return true;
        }

        return false;
    }

    public String toString() {
        return "(" + x + "," + y + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
