/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome.sgf;

import com.indigonauts.gome.common.Point;

public class Annotation {
    Point point;

    Annotation(Point p) {
        point = p;
    }

    /**
     * @return Returns the point.
     */
    public Point getPoint() {
        return point;
    }
}