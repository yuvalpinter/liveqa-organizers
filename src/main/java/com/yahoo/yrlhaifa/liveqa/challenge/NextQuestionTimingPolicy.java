// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge;

import java.util.Calendar;

/**
 * Defines when to send the next question to the participants. The idea is to have some delay between questions, such
 * that participants have the time to fully process one question before being asked the next question. <BR>
 * However, it is NOT guaranteed that the next question will be delayed until all participants have fully processed the
 * previous question. The policy is to make <B>a reasonable effort</B> for this, but not to guarantee it in all cases.
 * 
 * The current implementation (including the constructor parameters) is subject for changes.
 *
 * Date: Jan 15, 2015
 * 
 * @author Asher Stern
 *
 */
public class NextQuestionTimingPolicy {

    public NextQuestionTimingPolicy(long millisecondsBetweenQuestions) {
        super();
        this.millisecondsBetweenQuestions = millisecondsBetweenQuestions;
    }

    public void blockThreadUntilItIsTimeForNextQuestion(Calendar previousQuestionTime) throws InterruptedException {
        logger.info("The thread which triggers question-operations will now sleep for " + millisecondsBetweenQuestions
                        + " milliseconds.");
        Thread.sleep(millisecondsBetweenQuestions);
    }

    private final long millisecondsBetweenQuestions;

    private static final org.apache.log4j.Logger logger =
                    org.apache.log4j.Logger.getLogger(NextQuestionTimingPolicy.class);
}
