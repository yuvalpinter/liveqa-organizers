// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.common;

/**
 * 
 *
 * <P>
 * Date: Apr 1, 2015
 * 
 * @author Asher Stern
 *
 */
public class CommonUtilities {
    /**
     * TODO: Can be replaced by <code>str = str.replaceAll("\\s+", " ");</code>
     * 
     * @param str
     * @return
     */
    public static String mergeSpaces(final String str) {
        char[] array = str.toCharArray();
        char[] merged = new char[array.length];
        boolean inSpace = false;
        int mergedIndex = 0;
        for (int index = 0; index < array.length; ++index) {
            char c = array[index];
            if (Character.isWhitespace(c)) {
                if (inSpace) {
                } else {
                    inSpace = true;
                    merged[mergedIndex] = ' ';
                    ++mergedIndex;
                }
            } else {
                inSpace = false;
                merged[mergedIndex] = c;
                ++mergedIndex;
            }
        }
        return new String(merged, 0, mergedIndex);
    }
}
