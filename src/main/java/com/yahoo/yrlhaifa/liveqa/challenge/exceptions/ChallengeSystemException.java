// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.exceptions;


/**
 * Base checked exception for all the exceptions of the challenge system.
 *
 * Date: Jan 13, 2015
 * 
 * @author Asher Stern
 *
 */
public class ChallengeSystemException extends Exception {
    private static final long serialVersionUID = 8766637708970212264L;

    public ChallengeSystemException() {
        super();
    }

    public ChallengeSystemException(String message, Throwable cause, boolean enableSuppression,
                    boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ChallengeSystemException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChallengeSystemException(String message) {
        super(message);
    }

    public ChallengeSystemException(Throwable cause) {
        super(cause);
    }
}
