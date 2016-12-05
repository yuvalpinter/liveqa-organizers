// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.question_feed.filter;

import com.yahoo.yrlhaifa.liveqa.challenge.rep.Question;

/**
 * Determines whether a question should be used by the system or not.
 *
 * Date: Jan 15, 2015
 * 
 * @author Asher Stern
 *
 */
public interface QuestionFilter {
    public boolean questionOK(Question question);
}
