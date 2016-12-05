// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge;

import java.net.ProxySelector;
import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static com.yahoo.yrlhaifa.liveqa.challenge.Constants.*;

import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.ChallengeSystemException;
import com.yahoo.yrlhaifa.liveqa.challenge.question_feed.QuestionFeeder;
import com.yahoo.yrlhaifa.liveqa.challenge.question_feed.QuestionFeederFactory;
import com.yahoo.yrlhaifa.liveqa.challenge.question_feed.QuestionFeederNextQuestion;
import com.yahoo.yrlhaifa.liveqa.challenge.rep.Question;
import com.yahoo.yrlhaifa.liveqa.challenge.storage.AnswerStorageOperator;
import com.yahoo.yrlhaifa.liveqa.challenge.storage.QuestionStorageOperator;
import com.yahoo.yrlhaifa.liveqa.challenge.storage.Storage;
import com.yahoo.yrlhaifa.liveqa.challenge.storage.StorageParameters;
import com.yahoo.yrlhaifa.liveqa.common.HoursMinutesSeconds;


/**
 * The system which runs the live-QA challenge. The challenge system sends all the questions to the participants,
 * collect their answers and stores them.
 *
 * Date: Jan 13, 2015
 * 
 * @author Asher Stern
 *
 */
public class ChallengeSystem {
    public ChallengeSystem(ShutdownSignal shutdownSignal, HoursMinutesSeconds challengeDuration,
                    List<Participant> participants, StorageParameters storageParameters,
                    QuestionFeederFactory questionFeederFactory, QuestionOperatorFactory questionOperatorFactory,
                    NextQuestionTimingPolicy timingPolicy, int maximumNumberOfQuestionOperationThreads) {
        super();
        this.shutdownSignal = shutdownSignal;
        this.challengeDuration = challengeDuration;
        this.participants = participants;
        this.storageParameters = storageParameters;
        this.questionFeederFactory = questionFeederFactory;
        this.questionOperatorFactory = questionOperatorFactory;
        this.timingPolicy = timingPolicy;
        this.maximumNumberOfQuestionOperationThreads = maximumNumberOfQuestionOperationThreads;
    }

    public void run() throws ChallengeSystemException {
        // Ignore any proxy settings set by the OS.
        ProxySelector.setDefault(null);

        try {
            try (Storage storage = new Storage(storageParameters)) {
                try (AnswerStorageOperator answerStorageOperator = storage.constructAnswerStorageOperator()) {
                    try (QuestionStorageOperator questionStorageOperator = storage.constructQuestionStorageOperator()) {
                        try (QuestionOperator questionOperator = questionOperatorFactory.createQuestionOperator(
                                        participants, questionStorageOperator, answerStorageOperator)) {
                            try (QuestionFeeder questionFeeder = questionFeederFactory.createQuestionFeeder()) {

                                sendAllQuestions(questionOperator, questionFeeder);

                            } // end of try-with-resources (QuestionFeeder)
                        } // end of try-with-resources (QuestionOperator)
                    } // end of try-with-resources (QuestionStorageOperator)
                } // end try-with-resources (AnswerStorageOperator)
            } // end try-with-resources (Storage)
        } catch (InterruptedException e) {
            logger.error("Unexpected interruption. Program exits.", e);
            Thread.currentThread().interrupt();
            throw new ChallengeSystemException("Exit due to interruption. See also the nested exception.", e);
        }
    } // end of method run()


    private void sendAllQuestions(final QuestionOperator questionOperator, final QuestionFeeder questionFeeder)
                    throws ChallengeSystemException, InterruptedException {
        Calendar currentTime = constructCurrentTime();
        shouldEndTime = calculateEndTime(currentTime);
        logger.info("Planned end time is: " + printCalendar(shouldEndTime));
        while ((shouldEndTime.after(currentTime)) && (!shutdownSignal.isSignaled())) {
            currentTime = constructCurrentTime(); // time when current question starts.

            QuestionFeederNextQuestion nextQuestion = questionFeeder.next();
            if (null == exception) {
                if (nextQuestion.isFeedSucceeded()) {
                    final Question question = nextQuestion.getQuestion();
                    runTheGivenQuestion(question, questionOperator);
                } else {
                    StringBuilder sb = new StringBuilder();
                    sb.append("Feeder was not able to provide a new question for the moment. Cause: ")
                                    .append(nextQuestion.getWhyFeedFailed()).append("\n");
                    sb.append("An additional attempt will be performed in the next iteration.\n");
                    sb.append("Thread sleep for ").append(Constants.SAFE_SIDE_SLEEP_MILLISECONDS)
                                    .append(" milliseconds...\n");
                    logger.warn(sb.toString());

                    Thread.sleep(Constants.SAFE_SIDE_SLEEP_MILLISECONDS);
                    logger.info("Thread sleep is over. Proceeding to timing policy wait, and then proceeding to the next iteration.");
                }
            } else {
                logger.error("An unrecovered exception has been thrown by a previous question operation. Aborting.");
                throw exception;
            }

            timingPolicy.blockThreadUntilItIsTimeForNextQuestion(currentTime); // Use start time of current question.

            currentTime = constructCurrentTime(); // time by which it should be checked whether next question should be
                                                  // launched.
        } // end of while
        logger.info("Sending all questions - ended.");
        if (shutdownSignal.isSignaled()) {
            logger.warn("A shutdown signal has been captured. The program exits.");
        }
        boolean endOnTime = (!shouldEndTime.after(currentTime));
        if (endOnTime) {
            logger.info("Challenge ended at the planned time.");
        } else {
            // TODO Need better warning handling for this case.
            logger.warn("Note! the challenge ended before the planned end-time!");
        }
    }


    private void runTheGivenQuestion(final Question question, final QuestionOperator questionOperator)
                    throws InterruptedException {
        final int runningthreads = numberOfRunningQuestionOperationThreads.get();
        if (logger.isDebugEnabled()) {
            logger.debug("Right now, " + runningthreads + " previous question-operation threads are still active.");
        }
        logger.info("Operate on question <" + question.getId() + ">...");
        if (runningthreads >= maximumNumberOfQuestionOperationThreads) {
            logger.warn("Number of question operation running threads exceeded the maximum number of such threads allowed to run. Running = "
                            + runningthreads + ", maximum = " + maximumNumberOfQuestionOperationThreads + ".\n"
                            + "The current question is being skipped.");
            // To be on the safe side, sleep SAFE_SIDE_SLEEP_MILLISECONDS milliseconds. Anyway, this question is
            // discarded, and the next question
            // will be operated after timing-policy proceeds to the next question, so the sleep will be longer.
            Thread.sleep(SAFE_SIDE_SLEEP_MILLISECONDS);
        } else {
            logger.info("Start asynchronous operation on question <" + question.getId() + ">...");
            final String qid = (question.getId() != null) ? question.getId() : "";
            Thread QuestionOperationThread = new Thread(new QuestionOperationRunnable(question, questionOperator),
                            "QuestionOperationThread-" + qid);
            QuestionOperationThread.start();
            logger.info("asynchronous operation on question <" + question.getId() + "> - has been fired.");
        }
    }



    private class QuestionOperationRunnable implements Runnable {
        public QuestionOperationRunnable(Question question, QuestionOperator questionOperator) {
            super();
            this.question = question;
            this.questionOperator = questionOperator;
        }

        @Override
        public void run() {
            try {
                numberOfRunningQuestionOperationThreads.incrementAndGet();
                questionOperator.operate(question);
            } catch (ChallengeSystemException e) {
                logger.error("Error when operating on a question.", e);
                ChallengeSystem.this.exception = e;
            } catch (RuntimeException e) {
                logger.error("RuntimeException Error when operating on a question.", e);
                ChallengeSystem.this.exception =
                                new ChallengeSystemException("Unexpected failure when operating on a question.", e);
            } finally {
                logger.info("Operation on question " + question.getId() + " is done.");
                numberOfRunningQuestionOperationThreads.decrementAndGet();
            }

        }

        private final Question question;
        private final QuestionOperator questionOperator;
    }

    private Calendar calculateEndTime(Calendar startTime) {
        Calendar ret = Calendar.getInstance(WORKING_TIME_ZONE, WORKING_LOCALE);
        ret.setTime(startTime.getTime());
        ret.add(Calendar.HOUR, challengeDuration.getHours());
        ret.add(Calendar.MINUTE, challengeDuration.getMinutes());
        ret.add(Calendar.SECOND, challengeDuration.getSeconds());
        return ret;
    }

    private static Calendar constructCurrentTime() {
        Calendar calendar = Calendar.getInstance(WORKING_TIME_ZONE, WORKING_LOCALE);
        calendar.setTime(new Date());
        return calendar;
    }

    private static String printCalendar(final Calendar calendar) {
        DateFormat format = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL, Constants.WORKING_LOCALE);
        return format.format(calendar.getTime());
    }



    // input
    private final ShutdownSignal shutdownSignal;
    private final HoursMinutesSeconds challengeDuration;
    private final List<Participant> participants;
    private final StorageParameters storageParameters;
    private final QuestionFeederFactory questionFeederFactory;
    private final QuestionOperatorFactory questionOperatorFactory;
    private final NextQuestionTimingPolicy timingPolicy;
    private final int maximumNumberOfQuestionOperationThreads;


    // internals
    private volatile ChallengeSystemException exception = null;
    private Calendar shouldEndTime;
    private AtomicInteger numberOfRunningQuestionOperationThreads = new AtomicInteger(0);



    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ChallengeSystem.class);
}
