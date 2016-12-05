// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.exceptions;

/**
 * Indicates that the response returned from a participant is wrong (i.e., malformed). This is not an error in the
 * challenge-system. This is an error in participant's server.
 *
 * Date: Jan 19, 2015
 * 
 * @author Asher Stern
 *
 */
public class WrongUserResponseException extends ChallengeSystemNonFatalException {
    private static final long serialVersionUID = -3467074008796908072L;

    public WrongUserResponseException(String message) {
        super(message);
    }

    public WrongUserResponseException(String message, Throwable cause) {
        super(message, cause);
    }
}
