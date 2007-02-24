/*
 * (c) 2006 Indigonauts
 */
package com.indigonauts.gome.ui;

public class GraphicRectangle {
    public int x0;
    public int x1;
    public int y0;
    public int y1;

    public GraphicRectangle(int x0, int y0, int x1, int y1) {
        this.x0 = x0;
        this.x1 = x1;
        this.y0 = y0;
        this.y1 = y1;
    }

    public int getWidth() {
        return x1 - x0;
    }

    public int getHeight() {
        return y1 - y0;
    }

}
