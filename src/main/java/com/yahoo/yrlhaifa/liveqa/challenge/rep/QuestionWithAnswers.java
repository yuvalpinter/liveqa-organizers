// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.rep;

import java.util.Collections;
import java.util.Map;

public class QuestionWithAnswers {
    
    public QuestionWithAnswers(Question question, Map<String, ParticipantAnswer> answers) {
        this.question = question;
        this.answers = answers;
    }
    
    private Question question;
    private Map<String, ParticipantAnswer> answers;
    
    public Question getQuestion() {
        return question;
    }
    
    public Map<String, ParticipantAnswer> getAllAnswers() {
        return Collections.unmodifiableMap(answers);
    }
    
    public ParticipantAnswer getAnswer(String partId) {
        return answers.get(partId);
    }

}
