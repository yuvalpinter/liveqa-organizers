// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.haifa_utils.ds;

import java.io.Serializable;

/**
 * A StringBuffer that automatically adds a separator before each element appended to it
 */
public class SeparatorStringBuilder implements Serializable {
    private static final long serialVersionUID = 6982447262568791231L;


    private final StringBuilder m_s = new StringBuilder();
    private final String m_separator;
    private int numberOfElements = 0;



    /**
     * Constructor
     * 
     * @param iSeparator
     */
    public SeparatorStringBuilder(String iSeparator) {
        m_separator = iSeparator;
    }



    /**
     * Constructor
     * 
     * @param iSeparator
     */
    public SeparatorStringBuilder(char iSeparator) {
        m_separator = "" + iSeparator;
    }



    /**
     * Appends the string representing an object to the buffer, adding a separator before it
     * 
     * @param iItem
     * @return the buffer's content
     */
    public SeparatorStringBuilder append(Object iItem) {
        if (numberOfElements > 0) {
            m_s.append(m_separator);
        }

        if (iItem == null)
            m_s.append("null");
        else
            m_s.append(iItem.toString());

        this.numberOfElements++;
        return this;
    }



    /**
     * Appends the string representing the objects to the buffer, adding a separator before each
     * 
     * @param iItems
     * @return the buffer's content
     */
    public SeparatorStringBuilder appendAll(Iterable<? extends Object> iItems) {
        for (Object item : iItems) {
            if (numberOfElements > 0) {
                m_s.append(m_separator);
            }

            if (item == null)
                m_s.append("null");
            else
                m_s.append(item.toString());

            this.numberOfElements++;
        }

        return this;
    }



    /**
     * Appends the string representing the objects to the buffer, adding a separator before each
     * 
     * @param iItems
     * @return the buffer's content
     */
    public SeparatorStringBuilder appendAll(Object[] iItems) {
        for (Object item : iItems) {
            if (numberOfElements > 0) {
                m_s.append(m_separator);
            }

            if (item == null)
                m_s.append("null");
            else
                m_s.append(item.toString());

            this.numberOfElements++;
        }

        return this;
    }



    /**
     * @return the buffer's length
     */
    public int length() {
        return m_s.length();
    }



    @Override
    public String toString() {
        return m_s.toString();
    }



    public int getNumberOfElements() {
        return numberOfElements;
    }



    /**
     * this method returns false if the SSB is empty
     * 
     * @return boolean true if the SSB is empty
     */
    public boolean isEmpty() {
        if (this.numberOfElements == 0) {
            return true;
        }
        return false;
    }

}
