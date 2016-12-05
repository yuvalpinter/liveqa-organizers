// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.rep;

import java.util.List;


/**
 * Represents the answer returned by a participant for a given question.
 * 
 *
 * Date: Jan 13, 2015
 * 
 * @author Asher Stern
 *
 */
public class ParticipantAnswer {
    
    public ParticipantAnswer(String answer, long reportedAnsweringTime, List<String> reportedResources,
                    String titleFocusSpans, String bodyFocusSpans, String questionSummary) {
        super();
        this.reportedAnsweringTime = reportedAnsweringTime;
        this.reportedResources = reportedResources;
        this.answer = answer;
        this.titleFocusSpans = titleFocusSpans;
        this.bodyFocusSpans = bodyFocusSpans;
        this.questionSummary = questionSummary;
    }



    public String getAnswer() {
        return answer;
    }

    public long getReportedAnsweringTime() {
        return reportedAnsweringTime;
    }

    public List<String> getReportedResources() {
        return reportedResources;
    }

    public String getTitleFocusSpans() {
        return titleFocusSpans;
    }

    public String getBodyFocusSpans() {
        return bodyFocusSpans;
    }

    public String getQuestionSummary() {
        return questionSummary;
    }
    


    @Override
    public String toString() {
        return "ParticipantAnswer [answer=" + answer + "]";
    }


    private final String answer;
    private final long reportedAnsweringTime;
    private final List<String> reportedResources;
    private final String titleFocusSpans;
    private final String bodyFocusSpans;
    private final String questionSummary;

}
