// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.question_feed;

import com.yahoo.yrlhaifa.liveqa.challenge.ChallengeAutoCloseable;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.QuestionFeedFatalException;

/**
 * Feeds questions to the system. Each call to {@link #next()} returns the next question.
 *
 * Date: Jan 13, 2015
 * 
 * @author Asher Stern
 *
 */
public interface QuestionFeeder extends ChallengeAutoCloseable {
    public QuestionFeederNextQuestion next() throws QuestionFeedFatalException;
}
