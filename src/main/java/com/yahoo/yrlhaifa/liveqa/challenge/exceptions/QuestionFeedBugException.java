// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.exceptions;

import com.yahoo.yrlhaifa.liveqa.challenge.question_feed.QuestionFeeder;

/**
 * Indicates a bug in the code of {@link QuestionFeeder} and/or related classes.
 *
 * Date: Jan 18, 2015
 * 
 * @author Asher Stern
 *
 */
public class QuestionFeedBugException extends QuestionFeedFatalException {
    private static final long serialVersionUID = 8747689387210225505L;

    public QuestionFeedBugException(String message) {
        super(message);
    }
}
