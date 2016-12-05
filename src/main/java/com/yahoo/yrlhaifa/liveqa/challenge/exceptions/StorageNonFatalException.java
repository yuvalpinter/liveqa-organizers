// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.exceptions;

/**
 * Indicates a failure in storing a question or an answer, but it is assumed that this error is local, and future
 * questions and answers will be stored successfully.
 *
 * Date: Jan 13, 2015
 * 
 * @author Asher Stern
 *
 */
public class StorageNonFatalException extends ChallengeSystemNonFatalException {
    private static final long serialVersionUID = -2491969086553485371L;

    public StorageNonFatalException(String message, Throwable cause) {
        super(message, cause);
    }

    public StorageNonFatalException(String message) {
        super(message);
    }
}
