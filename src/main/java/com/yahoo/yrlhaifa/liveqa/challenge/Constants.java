// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Constants used in this project.
 *
 * Date: Jan 14, 2015
 * 
 * @author Asher Stern
 *
 */
public class Constants {
    public static final int MAXIMUM_NUMBER_OF_QUESTION_OPERATION_THREADS = 10;

    public static final String QUESTION_ID_YAHOO_ANSWERS_NAMESPACE_PREFIX = "YA:";
    public static final String QUESTION_ID_PARAMETER_NAME = "qid";
    public static final String QUESTION_TITLE_PARAMETER_NAME = "title";
    public static final String QUESTION_BODY_PARAMETER_NAME = "body";
    public static final String QUESTION_CATEGORY_PARAMETER_NAME = "category";
    public static final Locale WORKING_LOCALE = Locale.US;
    public static final String WORKING_TIME_ZONE_ID = "UTC";
    public static final TimeZone WORKING_TIME_ZONE = TimeZone.getTimeZone(WORKING_TIME_ZONE_ID);
    public static final Charset WORKING_CHARSET = StandardCharsets.UTF_8;

    // public static final long WAIT_BETWEEN_QUESTIONS_MILLISECONDS = 1000;
    public static final long SAFE_SIDE_SLEEP_MILLISECONDS = 500;

    public static final long EXTRA_ADD_TIMEOUT_FOR_EACH_THREAD_MILLISECONDS = 200;

    // public static final String ANSWERS_RSS_FEED_URL = "https://answers.yahoo.com/rss/allq";
    public static final String ANSWER_URL_PREFIX = "https://answers.yahoo.com/question/index?qid=";

    public static final String QUESTION_TITLE_PREFIX_TO_FILTER = "Open Question :";

    public static final String SHUTDOWN_SIGNAL_FILE_NAME = "shutdown";

    public static final int ANSWER_XML_LENGTH_IN_ADDITION_TO_ANSWER_LENGTH = 2048;
    public static final long FUTURE_GET_SLACK_TIME_MILLISECONDS = 250;



}
