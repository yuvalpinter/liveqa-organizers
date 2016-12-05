// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.exceptions;

/**
 * Indicates a fatal problem, that cannot be recovered at runtime, which prevents feeding new question from this point
 * on. The program cannot continue, since new questions will not be fed, so the program (the challenge system) should
 * exit.
 *
 * Date: Jan 18, 2015
 * 
 * @author Asher Stern
 *
 */
public class QuestionFeedFatalException extends ChallengeSystemException {
    private static final long serialVersionUID = 2657781851758064471L;

    public QuestionFeedFatalException(String message, Throwable cause) {
        super(message, cause);
    }

    public QuestionFeedFatalException(String message) {
        super(message);
    }
}
