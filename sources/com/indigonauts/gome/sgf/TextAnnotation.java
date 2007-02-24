/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome.sgf;

import com.indigonauts.gome.common.Point;

public class TextAnnotation extends Point {
    private String text;

    TextAnnotation(Point p, String text) {
        super(p);
        this.text = text;
    }

    /**
     * @return Returns the text.
     */
    public String getText() {
        return text;
    }
}