// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.exceptions;

/**
 * Indicates a systematic failure, in the organizers' side, in delivering questions to participants.
 *
 * Date: Jan 13, 2015
 * 
 * @author Asher Stern
 *
 */
public class QuestionOperationException extends ChallengeSystemException {
    private static final long serialVersionUID = -8874121738206801007L;

    public QuestionOperationException(String message, Throwable cause) {
        super(message, cause);
    }

    public QuestionOperationException(String message) {
        super(message);
    }

}
