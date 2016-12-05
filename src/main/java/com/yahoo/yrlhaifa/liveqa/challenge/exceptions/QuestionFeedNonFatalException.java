// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.exceptions;

/**
 * Indicates a problem, occurred while trying to feed new questions to the system, but it is assumed that new question
 * can be fed in the future, during the current system-run, so the program can continue running.
 *
 * Date: Jan 15, 2015
 * 
 * @author Asher Stern
 *
 */
public class QuestionFeedNonFatalException extends ChallengeSystemNonFatalException {
    private static final long serialVersionUID = -4547869105216697318L;

    public QuestionFeedNonFatalException(String message, Throwable cause) {
        super(message, cause);
    }

    public QuestionFeedNonFatalException(String message) {
        super(message);
    }
}
