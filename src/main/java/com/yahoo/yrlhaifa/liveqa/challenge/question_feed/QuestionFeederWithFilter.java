// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.question_feed;

import java.util.LinkedHashSet;
import java.util.Set;

import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.QuestionFeedFatalException;
import com.yahoo.yrlhaifa.liveqa.challenge.question_feed.filter.QuestionFilter;


/**
 * An abstract implementation of {@link QuestionFeeder} that returns only those questions that pass a given
 * {@link QuestionFilter}.
 *
 * Date: Jan 15, 2015
 * 
 * @author Asher Stern
 *
 */
public abstract class QuestionFeederWithFilter implements QuestionFeeder {
    public QuestionFeederWithFilter(QuestionFilter questionFilter) {
        super();
        this.questionFilter = questionFilter;
    }


    @Override
    public final QuestionFeederNextQuestion next() throws QuestionFeedFatalException {
        Set<String> excludeIDs = new LinkedHashSet<String>();
        QuestionFeederNextQuestion ret = null;
        while (null == ret) {
            QuestionFeederNextQuestion candidate = nextBeforeFiltring(excludeIDs);
            if (null == candidate) // no more candidates
            {
                ret = new QuestionFeederNextQuestion(null, null,
                                "No sufficiently-fresh question that passes the filter(s) could be retrieved.");
            } else if (candidate.isFeedSucceeded()) {
                if (questionFilter.questionOK(candidate.getQuestion())) {
                    ret = candidate;
                } else {
                    logger.debug("Question " + candidate.getId() + " is filtered out.");
                    // ret is null, and loop continues.
                    // Ask the next iteration not to return the same question again.
                    excludeIDs.add(candidate.getId());
                }
            } else {
                ret = candidate;
            }
        }

        return ret;
    }


    /**
     * May return null if no more questions are available.
     * 
     * @return
     */
    protected abstract QuestionFeederNextQuestion nextBeforeFiltring(final Set<String> excludeIDs)
                    throws QuestionFeedFatalException;


    protected final QuestionFilter questionFilter;

    private static final org.apache.log4j.Logger logger =
                    org.apache.log4j.Logger.getLogger(QuestionFeederWithFilter.class);
}
