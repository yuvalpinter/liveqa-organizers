// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge;

import java.util.List;

import com.yahoo.yrlhaifa.liveqa.challenge.configuration.Configuration;
import com.yahoo.yrlhaifa.liveqa.challenge.configuration.RequestGeneralParameters;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.ChallengeSystemException;
import com.yahoo.yrlhaifa.liveqa.challenge.question_feed.QuestionFeederFactory;
import com.yahoo.yrlhaifa.liveqa.challenge.question_feed.QuestionFeederFactoryParameters;
import com.yahoo.yrlhaifa.liveqa.challenge.storage.StorageParameters;
import com.yahoo.yrlhaifa.liveqa.common.ExceptionUtilities;
import com.yahoo.yrlhaifa.liveqa.common.HoursMinutesSeconds;
import com.yahoo.yrlhaifa.liveqa.common.LoggerInitializer;
import com.yahoo.yrlhaifa.liveqa.common.ThreadUtilities;


/**
 * An entry point ({@link #main(String[])} function) for the {@link ChallengeSystem}.
 * <P>
 * Configuration file reading, logger intialization, fatal-exception handling, and other stuff are programmed in this
 * class.
 *
 * Date: Jan 14, 2015
 * 
 * @author Asher Stern
 *
 */
public class ChallengeSystemMain {

    public static void main(String[] args) {
        try {
            LoggerInitializer.init();
            logger.info("start");

            if (args.length < 1) {
                throw new ChallengeSystemException(
                                "Configuration file not given (should be given as the first command-line parameter).");
            }
            Configuration configuration = new Configuration(args[0]);


            final List<Participant> participants = configuration.getParticipants();
            // participants.add(new Participant("bar ilan", "system 1", "http://localhost:8080/asher/servlet4",
            // "hello@world.com"));
            // participants.add(new Participant("bar ilan", "system 2", "http://localhost:8080/asher/servletget",
            // "hello@world.com"));
            final StorageParameters storageParameters = configuration.getStorageParameters();
            // new StorageParameters("jdbc:derby:/Users/asherst/work/derby_workdir/challenge", false);

            final RequestGeneralParameters requestGeneralParameters = configuration.getRequestGeneralParameters();

            final NextQuestionTimingPolicy nextQuestionTimingPolicy = configuration.getNextQuestionTimingPolicy();
            // final long waitBetweenQuestionsMilliseconds = 10*1000;

            final QuestionFeederFactoryParameters feederParameters = configuration.getQuestionFeederFactoryParameters();
            // final QuestionFeederFactoryParameters feederParameters = new
            // QuestionFeederFactoryParameters("https://answers.yahoo.com/rss/allq", new HoursMinutesSeconds(0, 1, 0));

            final HoursMinutesSeconds challengeDuration = configuration.getChallengeOverallDuration(); // = new
                                                                                                       // HoursMinutesSeconds(0,
                                                                                                       // 1, 0);

            final ShutdownSignal shutDownSignal = new ShutdownSignal(configuration.getShutdownFile());


            if (!shutDownSignal.isSignaled()) {
                ChallengeSystem system = new ChallengeSystem(shutDownSignal, challengeDuration, participants,
                                storageParameters, new QuestionFeederFactory(feederParameters),
                                new QuestionOperatorFactory(requestGeneralParameters), nextQuestionTimingPolicy,
                                Constants.MAXIMUM_NUMBER_OF_QUESTION_OPERATION_THREADS);
                system.run();
            } else {
                logger.error("Shut-down signal has been detected. Challenge does not start.");
            }

            logger.info("About to exit. Current threads in system:\n" + ThreadUtilities.threadsInSystem());
            logger.info("end");
        } catch (Throwable t) {
            try {
                ExceptionUtilities.logException(t, logger);
            } catch (Throwable tt) {
            }
            ExceptionUtilities.outputException(t, System.out);
        }
    }

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(ChallengeSystemMain.class);
}
