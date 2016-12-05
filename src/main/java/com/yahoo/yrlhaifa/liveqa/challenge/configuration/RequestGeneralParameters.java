// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.configuration;


/**
 * Encapsulates several parameters which mainly specify constraints on the responses and answers returned by
 * participants.
 *
 * Date: Jan 14, 2015
 * 
 * @author Asher Stern
 *
 */
public class RequestGeneralParameters {
    public RequestGeneralParameters(long timeForAnswerMilliseconds, long extraTimeForRequestResponseMilliseconds,
                    long slackTimeForRequestExecutorTimeOutMilliseconds, int maximumAnswerLength, int maximumSummaryLength) {
        super();
        this.timeForAnswerMilliseconds = timeForAnswerMilliseconds;
        this.extraTimeForRequestResponseMilliseconds = extraTimeForRequestResponseMilliseconds;
        this.slackTimeForRequestExecutorTimeOutMilliseconds = slackTimeForRequestExecutorTimeOutMilliseconds;
        this.maximumAnswerLength = maximumAnswerLength;
        this.maximumSummaryLength = maximumSummaryLength;
    }



    public long getTimeForAnswerMilliseconds() {
        return timeForAnswerMilliseconds;
    }

    public long getExtraTimeForRequestResponseMilliseconds() {
        return extraTimeForRequestResponseMilliseconds;
    }

    public long getSlackTimeForRequestExecutorTimeOutMilliseconds() {
        return slackTimeForRequestExecutorTimeOutMilliseconds;
    }

    public int getMaximumAnswerLength() {
        return maximumAnswerLength;
    }

    public int getMaximumSummaryLength() {
        return maximumSummaryLength;
    }



    private final long timeForAnswerMilliseconds;
    private final long extraTimeForRequestResponseMilliseconds;
    private final long slackTimeForRequestExecutorTimeOutMilliseconds;
    private final int maximumAnswerLength;
    private final int maximumSummaryLength;
}
