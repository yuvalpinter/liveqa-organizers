// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge;

import java.io.File;


/**
 * Captures a signal that exists in some external means (such as files) that directs the program to exit gracefully as
 * soon as possible.
 * <P>
 * The program flow should call {@link #isSignaled()} from time to time to see whether it should stop, even though the
 * challenge has not yet been completed.
 * 
 * @see ChallengeSystem
 *
 *      Date: Jan 15, 2015
 * @author Asher Stern
 *
 */
public class ShutdownSignal {
    public ShutdownSignal(File shutdownFile) {
        super();
        this.shutdownFile = shutdownFile;
    }

    public boolean isSignaled() {
        if (!signaled) {
            signaled = shutdownFile.exists();
        }
        return signaled;
    }

    private final File shutdownFile;

    private volatile boolean signaled = false;
}
