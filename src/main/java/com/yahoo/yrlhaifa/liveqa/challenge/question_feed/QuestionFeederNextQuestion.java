// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.question_feed;

import com.yahoo.yrlhaifa.liveqa.challenge.rep.Question;

/**
 * Encapsulates a question, retrieved by {@link QuestionFeeder}, to be used by the system. <BR>
 * In case that {@link QuestionFeeder} failed to retrieve a new question, then no question is encapsulated, but rather a
 * human-readable string which describes why the feeder failed is provided.
 * 
 *
 * Date: Jan 15, 2015
 * 
 * @author Asher Stern
 *
 */
public class QuestionFeederNextQuestion {
    public QuestionFeederNextQuestion(String id, Question question, String whyFeedFailed) {
        super();
        this.id = id;
        this.question = question;
        this.whyFeedFailed = whyFeedFailed;
    }


    public boolean isFeedSucceeded() {
        return (question != null);
    }


    public String getId() {
        return id;
    }

    public Question getQuestion() {
        return question;
    }

    public String getWhyFeedFailed() {
        return whyFeedFailed;
    }

    /**
     * Even though the question contains the ID, it is safer to put it here, in case that the question ID is
     * manipulated. It is assumed that this field is the original ID, with no manipulation. Note, currently no
     * manipulation is performed anywhere. This is just a precaution against unexpected future code changes.
     */
    private final String id;
    private final Question question;
    private final String whyFeedFailed;
}
