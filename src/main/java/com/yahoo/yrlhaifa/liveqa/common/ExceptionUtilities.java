// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.common;


import java.io.IOException;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;

import org.apache.log4j.Logger;

import com.yahoo.yrlhaifa.haifa_utils.utils.StringUtils;


/**
 * Collections of utilities (public static methods) for handling exceptions.
 * <P>
 * Copied from Excitement-open-platform.
 * 
 * @author Asher Stern
 *
 */
public class ExceptionUtilities {
    public static final String TITLE_PROBLEMS = "Summary of problems:";
    public static final String HEADER_PROBLEMS = StringUtils.generateStringOfCharacter('*', 20) + TITLE_PROBLEMS
                    + StringUtils.generateStringOfCharacter('*', 20);
    public static final String FOOTER_PROBLEMS = StringUtils.generateStringOfCharacter('*', HEADER_PROBLEMS.length());

    /**
     * Returns the exception's message plug all nested exceptions messages.
     * 
     * @param throwable an Exception (or Error)
     * 
     * @return The exception's message plug all nested exceptions messages.
     */
    public static String getMessages(Throwable throwable) {
        StringBuffer buffer = new StringBuffer();
        while (throwable != null) {
            if (throwable.getMessage() != null) {
                buffer.append(throwable.getClass().getSimpleName()).append(": ").append(throwable.getMessage())
                                .append("\n");
            }
            throwable = throwable.getCause();
        }
        return buffer.toString();
    }

    /**
     * Prints a string that describes the given exception into the given PrintStream
     * 
     * @param throwable
     * @param printStream
     */
    public static void outputException(Throwable throwable, PrintStream printStream) {
        throwable.printStackTrace(printStream);
        printStream.println();
        printStream.println(HEADER_PROBLEMS);
        printStream.println(ExceptionUtilities.getMessages(throwable));
        printStream.println(FOOTER_PROBLEMS);
    }

    /**
     * Prints a string that describes the given exception into the given PrintWriter
     * 
     * @param throwable
     * @param printWriter
     */
    public static void outputException(Throwable throwable, PrintWriter printWriter) {
        throwable.printStackTrace(printWriter);
        printWriter.println();
        printWriter.println(HEADER_PROBLEMS);
        printWriter.println(ExceptionUtilities.getMessages(throwable));
        printWriter.println(FOOTER_PROBLEMS);
    }

    public static void logException(Throwable throwable, Logger logger) {
        logger.error("Exception/Error:\n", throwable);
        logger.error("\n" + HEADER_PROBLEMS + "\n" + ExceptionUtilities.getMessages(throwable) + "\n"
                        + FOOTER_PROBLEMS);
    }

    public static String getStackTrace(Throwable t) {
        StringWriter stringWriter = new StringWriter();
        try {
            t.printStackTrace(new PrintWriter(stringWriter));
            String ret = stringWriter.toString();
            return ret;
        } finally {
            try {
                stringWriter.close();
            } catch (IOException e) {
            }
        }
    }


}
