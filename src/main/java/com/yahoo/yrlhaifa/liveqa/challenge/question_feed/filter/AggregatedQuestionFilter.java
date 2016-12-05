// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.question_feed.filter;

import java.util.List;

import com.yahoo.yrlhaifa.liveqa.challenge.rep.Question;

/**
 * A filter which passes a question if and only if it passes all the given underlying filters.
 *
 * <P>
 * Date: Feb 3, 2015
 * 
 * @author Asher Stern
 *
 */
public class AggregatedQuestionFilter implements QuestionFilter {
    public AggregatedQuestionFilter(List<QuestionFilter> actualFilters) {
        super();
        this.actualFilters = actualFilters;
    }

    @Override
    public boolean questionOK(Question question) {
        for (QuestionFilter actualFilter : actualFilters) {
            if (!(actualFilter.questionOK(question))) {
                return false;
            }
        }
        return true;
    }

    private final List<QuestionFilter> actualFilters;
}
