/*
 * (c) 2006 Indigonauts
 */

package com.indigonauts.gome.sgf;

import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.Stack;

public class SgfPreorderIterator implements Enumeration {
    Stack nodeStack;

    public SgfPreorderIterator(SgfNode rootNode) {
        nodeStack = new Stack();

        if (rootNode != null) {
            nodeStack.push(rootNode);
        }
    }

    public boolean hasMoreElements() {
        return nodeStack.size() > 0;
    }

    public Object nextElement() {
        if (!hasMoreElements())
            throw new NoSuchElementException("empty stack"); //$NON-NLS-1$

        SgfNode top = (SgfNode) nodeStack.pop();
        SgfNode son = top.getSon(); // left
        SgfNode younger = top.getYounger(); // left

        /*
         * younger is pushed first so that it's poped up later. the access is
         * top-son-younger in a recursive way
         */
        if (younger != null)
            nodeStack.push(younger);

        if (son != null)
            nodeStack.push(son);

        return top;
    }
}