// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.exceptions;

import com.yahoo.yrlhaifa.liveqa.challenge.ChallengeAutoCloseable;

/**
 * Indicates a problem in the close() method of {@link ChallengeAutoCloseable}.
 *
 * Date: Jan 13, 2015
 * 
 * @author Asher Stern
 *
 */
public class ChallengeCloseException extends ChallengeSystemException {
    private static final long serialVersionUID = 1241247004829767118L;

    public ChallengeCloseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChallengeCloseException(String message) {
        super(message);
    }

}
