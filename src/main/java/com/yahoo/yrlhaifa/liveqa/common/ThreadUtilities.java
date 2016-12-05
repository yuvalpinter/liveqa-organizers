// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.common;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;

/**
 * Utilities related to threads.
 *
 * <P>
 * Date: Mar 31, 2015
 * 
 * @author Asher Stern
 *
 */
public class ThreadUtilities {
    /**
     * Describes the threads running in the system.
     * 
     * @return a string describing the threads running in the system.
     */
    public static String threadsInSystem() {
        StringBuilder sb = new StringBuilder();
        ThreadMXBean tmb = ManagementFactory.getThreadMXBean();
        for (ThreadInfo ti : tmb.dumpAllThreads(true, true)) {
            sb.append(ti.getThreadState()).append(" - ").append(ti.getThreadName()).append("\n");
        }
        return sb.toString();
    }
}
