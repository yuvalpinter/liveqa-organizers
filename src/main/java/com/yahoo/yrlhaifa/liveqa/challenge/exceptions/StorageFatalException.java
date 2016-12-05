// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.exceptions;

/**
 * Indicates a fatal error in the storage (either question-storage or answer-storage) which should trigger the challenge
 * system to exit.
 *
 * Date: Jan 13, 2015
 * 
 * @author Asher Stern
 *
 */
public class StorageFatalException extends ChallengeSystemException {
    private static final long serialVersionUID = 4920590131784310697L;

    public StorageFatalException(String message, Throwable cause) {
        super(message, cause);
    }

    public StorageFatalException(String message) {
        super(message);
    }
}
