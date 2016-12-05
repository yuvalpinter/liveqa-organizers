// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.configuration;

import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.yahoo.yrlhaifa.haifa_utils.utils.ConfParams;
import com.yahoo.yrlhaifa.haifa_utils.utils.ConfParams.ConfParamsException;
import com.yahoo.yrlhaifa.liveqa.challenge.Constants;
import com.yahoo.yrlhaifa.liveqa.challenge.NextQuestionTimingPolicy;
import com.yahoo.yrlhaifa.liveqa.challenge.Participant;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.ChallengeSystemException;
import com.yahoo.yrlhaifa.liveqa.challenge.question_feed.QuestionFeederFactoryParameters;
import com.yahoo.yrlhaifa.liveqa.challenge.storage.StorageParameters;
import com.yahoo.yrlhaifa.liveqa.common.HoursMinutesSeconds;


/**
 * A method to read several configuration parameters required by the challenge-system.
 * 
 * Date: Jan 21, 2015
 * 
 * @author Asher Stern
 * 
 */
public class Configuration {
    public static final String HOURS_MINUTE_SECOND_DELIMITER = ":";
    public static final String PARTICIPANT_FILE_PARAMETER_NAME = "participant-file";
    public static final String DATABASE_CONNECTION_STRING_PARAMETER_NAME = "database-connection-string";
    public static final String TEXT_FILE_QUESTIONS_PARAMETER_NAME = "question-storage-text-file";
    public static final String TEXT_FILE_ANSWERS_PARAMETER_NAME = "answer-storage-text-file";
    public static final String ALLOW_INCREMENTAL_STORAGE_PARAMETER_NAME = "allow-incremental";
    public static final String ANSWER_TIME_PARAMETER_NAME = "answer-time-milliseconds";
    public static final String ASNWER_REQUEST_RESPOND_EXTRA_TIME = "request-respond-extra-time-milliseconds";
    public static final String ANSWER_MAXIMUM_LENGTH = "answer-maximum-length";
    public static final String SUMMARY_MAXIMUM_LENGTH = "summary-maximum-length";
    public static final String RSS_URL_PARAMETER_NAME = "rss-url";
    public static final String MAXIMUM_QUESTION_AGE_PARAMETER_NAME = "maximum-question-age";
    public static final String ENGLISH_CLASSIFIER_LANGUAGE_MODEL_FILE_PARAMETER_NAME =
                    "english-classifier-language-model";
    public static final String ENGLISH_CLASSIFIER_MODEL_FILE_PARAMETER_NAME = "english-classifier-model";
    public static final String TIMING_POLICY_WAIT_BETWEEN_QUESTIONS_PARAMETER_NAME =
                    "timing-policy-wait-between-questions-milliseconds";
    public static final String CHALLENGE_DURATION_PARAMETER_NAME = "challenge-duration";
    public static final String BAD_WORDS_FILE_PARAMETER_NAME = "bad-words-file";
    public static final String FILTER_CATEGORIES_PARAMETER_NAME = "filter-categories";
    public static final String FILTER_CATEGORIES_SEPARATOR = ",";
    public static final String CATEGORY_BLACKLIST_FLAG_PARAMETER_NAME = "category-filter-is-blacklist";



    public Configuration(String configurationFileName) throws ChallengeSystemException {
        super();
        this.configurationFileName = configurationFileName;
        try {
            parameters = new ConfParams(configurationFileName);
        } catch (ConfParamsException | IOException e) {
            throw new ChallengeSystemException("Failed to read configuration file" + configurationFileName, e);
        }
    }

    public List<Participant> getParticipants() throws ChallengeConfigurationException {
        ParticipantFileReader reader = new ParticipantFileReader(getFile(PARTICIPANT_FILE_PARAMETER_NAME).getPath());
        List<Participant> participants = reader.read();
        if (logger.isDebugEnabled()) {
            logger.debug("Number of participants = " + participants.size());
        }
        return participants;
    }

    public StorageParameters getStorageParameters() throws ChallengeConfigurationException {
        final String connectionString = getString(DATABASE_CONNECTION_STRING_PARAMETER_NAME);
        final String questionFile = getString(TEXT_FILE_QUESTIONS_PARAMETER_NAME);
        final String answerFile = getString(TEXT_FILE_ANSWERS_PARAMETER_NAME);
        final boolean allowIncremental = parameters.getBoolean(ALLOW_INCREMENTAL_STORAGE_PARAMETER_NAME);
        if (logger.isDebugEnabled()) {
            logger.debug("Connection string = " + connectionString);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("allowIncremental = " + allowIncremental);
        }
        return new StorageParameters(connectionString, allowIncremental, questionFile, answerFile);
    }

    public RequestGeneralParameters getRequestGeneralParameters() throws ChallengeSystemException {
        final long timeForAnswerMilliseconds = getLong(ANSWER_TIME_PARAMETER_NAME);
        final long extraTimeForRequestResponseMilliseconds = getLong(ASNWER_REQUEST_RESPOND_EXTRA_TIME);
        final long slackTimeForRequestExecutorTimeOutMilliseconds = Constants.FUTURE_GET_SLACK_TIME_MILLISECONDS;
        final int maximumAnswerLength = getInt(ANSWER_MAXIMUM_LENGTH);
        final int maximumSummaryLength = getInt(SUMMARY_MAXIMUM_LENGTH);

        return new RequestGeneralParameters(timeForAnswerMilliseconds, extraTimeForRequestResponseMilliseconds,
                        slackTimeForRequestExecutorTimeOutMilliseconds, maximumAnswerLength, maximumSummaryLength);
    }


    public QuestionFeederFactoryParameters getQuestionFeederFactoryParameters() throws ChallengeConfigurationException {
        final String rssUrl = getString(RSS_URL_PARAMETER_NAME);
        final HoursMinutesSeconds questionMaximumAge =
                        parseHoursMinutesSeconds(getString(MAXIMUM_QUESTION_AGE_PARAMETER_NAME));
        if (logger.isDebugEnabled()) {
            logger.debug("RSS URL = " + rssUrl);
        }
        if (logger.isDebugEnabled()) {
            logger.debug("Question maximum age = " + questionMaximumAge);
        }

        File badWordsFile = getFile(BAD_WORDS_FILE_PARAMETER_NAME);

        return new QuestionFeederFactoryParameters(rssUrl, questionMaximumAge, badWordsFile, getFilterCategories(),
                        getCategoryFilterIsBlacklist());
    }

    private boolean getCategoryFilterIsBlacklist() throws ChallengeConfigurationException {
        boolean blacklistFlag = getBoolean(CATEGORY_BLACKLIST_FLAG_PARAMETER_NAME);
        if (logger.isDebugEnabled()) {
            logger.debug("Using category list as " + (blacklistFlag ? "blacklist" : "whitelist"));
        }
        return blacklistFlag;
    }

    public NextQuestionTimingPolicy getNextQuestionTimingPolicy() throws ChallengeConfigurationException {
        final long waitBetweenQuestions = getLong(TIMING_POLICY_WAIT_BETWEEN_QUESTIONS_PARAMETER_NAME);
        if (logger.isDebugEnabled()) {
            logger.debug("Wait between questions (milliseconds) = " + waitBetweenQuestions);
        }
        return new NextQuestionTimingPolicy(waitBetweenQuestions);
    }

    public HoursMinutesSeconds getChallengeOverallDuration() throws ChallengeConfigurationException {
        HoursMinutesSeconds duration = parseHoursMinutesSeconds(getString(CHALLENGE_DURATION_PARAMETER_NAME));
        if (logger.isDebugEnabled()) {
            logger.debug("Challenge duration = " + duration);
        }
        return duration;
    }

    public File getShutdownFile() throws ChallengeConfigurationException {
        // This is not configurable
        return new File(Constants.SHUTDOWN_SIGNAL_FILE_NAME);
    }



    private Set<String> getFilterCategories() throws ChallengeConfigurationException {
        Set<String> ret = null;
        if (parameters.containsKey(FILTER_CATEGORIES_PARAMETER_NAME)) {
            String filterString = getString(FILTER_CATEGORIES_PARAMETER_NAME);
            String[] categoriesArray = filterString.split(FILTER_CATEGORIES_SEPARATOR);
            ret = new LinkedHashSet<>();
            for (String category : categoriesArray) {
                ret.add(category);
            }

            logger.info("Filter categories = " + filterString);
        } else {
            ret = null;
            logger.info("Filter categories were not specified.");
        }

        return ret;
    }



    private static HoursMinutesSeconds parseHoursMinutesSeconds(final String str)
                    throws ChallengeConfigurationException {
        if (null == str) {
            throw new ChallengeConfigurationException("Null string given for hours-minutes-seconds.");
        }

        String[] components = str.split(HOURS_MINUTE_SECOND_DELIMITER);

        int index = 0;

        if (!(index < components.length)) {
            throw new ChallengeConfigurationException("Failed to parse hours-minutes-seconds string +" + str);
        }
        final String hours = components[index].trim();
        ++index;

        if (!(index < components.length)) {
            throw new ChallengeConfigurationException("Failed to parse hours-minutes-seconds string +" + str);
        }
        final String minutes = components[index].trim();
        ++index;

        if (!(index < components.length)) {
            throw new ChallengeConfigurationException("Failed to parse hours-minutes-seconds string +" + str);
        }
        final String seconds = components[index].trim();
        ++index;

        return new HoursMinutesSeconds(parseIntegerAndVerifyNonNegative(hours, str),
                        parseIntegerAndVerifyNonNegative(minutes, str), parseIntegerAndVerifyNonNegative(seconds, str));
    }

    private static int parseIntegerAndVerifyNonNegative(final String str, final String fullParameterValue)
                    throws ChallengeConfigurationException {
        try {
            int ret = Integer.parseInt(str);
            if (ret < 0) {
                throw new ChallengeConfigurationException("Illegal negative number detected: " + ret
                                + " in parameter-value " + fullParameterValue);
            }
            return ret;
        } catch (NumberFormatException e) {
            throw new ChallengeConfigurationException("Given string is not a number: " + str
                            + ". Detected in parameter-value " + fullParameterValue, e);
        }
    }

    private String getString(final String parameterName) throws ChallengeConfigurationException {
        final String ret = parameters.getString(parameterName);
        if (null == ret) {
            throw new ChallengeConfigurationException("Parameter \"" + parameterName + "\" is missing.");
        }
        return ret;
    }

    private File getFile(final String parameterName) throws ChallengeConfigurationException {
        final File ret = parameters.getFile(parameterName);
        if (null == ret) {
            throw new ChallengeConfigurationException("Parameter \"" + parameterName + "\" is missing.");
        }
        return ret;
    }

    private boolean getBoolean(final String parameterName) throws ChallengeConfigurationException {
        try {
            return parameters.getBoolean(parameterName);
        } catch (RuntimeException e) {
            throw new ChallengeConfigurationException("Failed to retrieve boolean value for parameter " + parameterName
                            + ". Make sure that the parameter exists in the configuration file.");
        }
    }

    private long getLong(final String parameterName) throws ChallengeConfigurationException {
        try {
            return parameters.getLong(parameterName);
        } catch (RuntimeException e) {
            throw new ChallengeConfigurationException("Failed to retrieve numerical value for parameter "
                            + parameterName + ". Make sure that the parameter exists in the configuration file.");
        }
    }

    private int getInt(final String parameterName) throws ChallengeConfigurationException {
        try {
            return parameters.getInt(parameterName);
        } catch (RuntimeException e) {
            throw new ChallengeConfigurationException("Failed to retrieve numerical value for parameter "
                            + parameterName + ". Make sure that the parameter exists in the configuration file.");
        }
    }


    @SuppressWarnings("unused")
    private final String configurationFileName;
    private final ConfParams parameters;

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(Configuration.class);
}
