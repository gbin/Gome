/*
 * (c) 2006 Indigonauts
 */

package com.indigonauts.gome.common;

import java.util.Vector;

public class StringVector extends Vector {

    /** Creates a new instance of StringVector */
    public StringVector() {
        super();
    }

    public StringVector(String str, char separator) {
        super();
        if (str.length() == 0)
            return;
        StringBuffer temp = new StringBuffer(20);
        char c;
        for (int i = 0; i < str.length(); i++) {
            if ((c = str.charAt(i)) != separator) {
                temp.append(c);
            } else {
                this.addElement(temp.toString());
                temp = new StringBuffer(20);
            }
        }
        this.addElement(temp.toString());
    }

    public String stringElementAt(int i) {
        return (String) elementAt(i);
    }

    public int intElementAt(int i) {
        try {
            return Integer.parseInt(stringElementAt(i));
        } catch (Exception e) {
            return 0;
        }
    }

    public long longElementAt(int i) {
        try {
            return Long.parseLong(stringElementAt(i));
        } catch (Exception e) {
            return 0;
        }
    }

}
