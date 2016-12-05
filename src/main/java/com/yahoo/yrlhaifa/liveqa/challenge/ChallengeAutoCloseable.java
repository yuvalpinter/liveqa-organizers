// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge;

import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.ChallengeCloseException;

/**
 * An extension of java.lang.AutoCloseable, which narrows the exception thrown by the {@link #close()} method to
 * {@link ChallengeCloseException} only.
 * <P>
 * A class that implements this interface can be constructed in a try-with-resources statement.
 *
 * Date: Jan 13, 2015
 * 
 * @author Asher Stern
 *
 */
public interface ChallengeAutoCloseable extends AutoCloseable {
    @Override
    void close() throws ChallengeCloseException;
}
