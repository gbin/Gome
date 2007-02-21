/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome.sgf;

import com.indigonauts.gome.common.Point;

public class SgfPoint extends Point {

    public SgfPoint() {
        super();
    }

    public SgfPoint(SgfPoint pt) {
        super(pt);
    }

    public SgfPoint(byte nx, byte ny) {
        super(nx, ny);
    }

    public String toString() {
        if (x == Point.PASS)
            return ""; // nothing between the [] is Pass
        StringBuffer buf = new StringBuffer();

        buf.append((char) ('a' + x));
        buf.append((char) ('a' + y));

        return buf.toString();
    }

    public static final SgfPoint createFromSgf(String str) {
        return createFromSgf(str, 0);
    }

    public static final SgfPoint createFromSgf(String str, int index) {
        byte x = coordFromSgf(str.charAt(index));
        byte y = coordFromSgf(str.charAt(index + 1));

        return new SgfPoint(x, y);
    }

    public static final byte coordFromSgf(char c) {
        return (byte) (c - 'a');
    }

    public SgfPoint clone() {
        return new SgfPoint(x, y);
    }

}
