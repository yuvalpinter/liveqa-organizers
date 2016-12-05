// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.exceptions;

/**
 * Indicates a problem, which cannot be ignored (it must be handled somehow), but that should not cause the system to
 * exit.
 *
 * Date: Jan 14, 2015
 * 
 * @author Asher Stern
 *
 */
public class ChallengeSystemNonFatalException extends Exception {
    private static final long serialVersionUID = 6326842783612815983L;

    public ChallengeSystemNonFatalException() {
        super();
    }

    public ChallengeSystemNonFatalException(String message, Throwable cause, boolean enableSuppression,
                    boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public ChallengeSystemNonFatalException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChallengeSystemNonFatalException(String message) {
        super(message);
    }

    public ChallengeSystemNonFatalException(Throwable cause) {
        super(cause);
    }


}
