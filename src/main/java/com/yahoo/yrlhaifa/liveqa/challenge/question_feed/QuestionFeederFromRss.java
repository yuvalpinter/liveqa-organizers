// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.question_feed;

import java.io.IOException;
import java.net.URL;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Set;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndFeed;
import com.sun.syndication.io.FeedException;
import com.sun.syndication.io.SyndFeedInput;
import com.sun.syndication.io.XmlReader;
import com.yahoo.yrlhaifa.liveqa.challenge.Constants;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.ChallengeCloseException;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.QuestionFeedBugException;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.QuestionFeedFatalException;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.QuestionFeedNonFatalException;
import com.yahoo.yrlhaifa.liveqa.challenge.question_feed.filter.QuestionFilter;
import com.yahoo.yrlhaifa.liveqa.common.HoursMinutesSeconds;


/**
 * A {@link QuestionFeeder} which provides the question be reading them from Yahoo-Answers RSS feed.
 *
 * Date: Jan 15, 2015
 * 
 * @author Asher Stern
 *
 */
public class QuestionFeederFromRss extends QuestionFeederWithFilter {
    public QuestionFeederFromRss(QuestionFilter questionFilter, String rssUrl,
                    HoursMinutesSeconds maximumAgeOfQuestion) {
        super(questionFilter);
        this.rssUrl = rssUrl;
        this.maximumAgeOfQuestion = maximumAgeOfQuestion;
    }


    @Override
    public void close() throws ChallengeCloseException {}


    @Override
    protected QuestionFeederNextQuestion nextBeforeFiltring(final Set<String> excludeIDs)
                    throws QuestionFeedFatalException {
        if (excludeIDs.isEmpty()) {
            previousSentTime = previousCandidateTime;
        }
        try {
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(new URL(rssUrl)));

            QuestionFromRssEntry fromRss = null;
            QuestionFeederNextQuestion ret = null;
            @SuppressWarnings("unchecked")
            Iterator<? extends Object> entryObjectsIterator = feed.getEntries().iterator();
            while (entryObjectsIterator.hasNext() && (null == ret)) {
                Object entryObj = entryObjectsIterator.next();
                if (entryObj instanceof SyndEntry) {
                    try {
                        SyndEntry entry = (SyndEntry) entryObj;
                        fromRss = QuestionFromRssEntry.createFromRssEntry(entry);
                        if (!(excludeIDs.contains(fromRss.getId()))) {
                            ret = createQuestionFromRssQuestion(fromRss);
                        } else {
                            // else - ret is null, and loop continues.
                            logger.info("Encountered a filtered question \"" + fromRss.getId()
                                            + "\". Searching for another question.");
                        }
                    } catch (QuestionFeedNonFatalException e) {
                        logger.error("Reading an RSS feed has failed. Assuming this is not a permanent problem, the program continues and tries to read the next feed.",
                                        e);
                    }
                } else {
                    logger.error("One of the entries in the list of RSS feed entries is not a \"SyndEntry\". Program continues, with the hope that other entries are fine.");
                }
            } // end of while
              // Note: it is OK to return null here. If iterator.hasNext()==false, then null is returned.
            return ret;
        } catch (IllegalArgumentException | FeedException | IOException e) {
            logger.error("Failed to read RSS feed. It is assumed that this is a local problem and not an inherent failure.\n"
                            + "This failure will be dealt with by higher levels, but is not considered as fatal. Problem is:",
                            e);
            return new QuestionFeederNextQuestion(null, null, e.getClass().getName() + ": " + e.getMessage());
        }
    }

    private QuestionFeederNextQuestion createQuestionFromRssQuestion(final QuestionFromRssEntry fromRss)
                    throws QuestionFeedBugException {
        QuestionFeederNextQuestion ret = null;
        final Calendar publishedDate = fromRss.getPublishedDate();

        Calendar current = Calendar.getInstance(Constants.WORKING_TIME_ZONE, Constants.WORKING_LOCALE);
        current.setTime(new Date());

        Calendar publishedPlusMaximumAge = Calendar.getInstance(Constants.WORKING_TIME_ZONE, Constants.WORKING_LOCALE);
        publishedPlusMaximumAge.setTime(publishedDate.getTime());
        publishedPlusMaximumAge.add(Calendar.HOUR, maximumAgeOfQuestion.getHours());
        publishedPlusMaximumAge.add(Calendar.MINUTE, maximumAgeOfQuestion.getMinutes());
        publishedPlusMaximumAge.add(Calendar.SECOND, maximumAgeOfQuestion.getSeconds());

        if (publishedPlusMaximumAge.before(current)) {
            logger.info("Cound not find sufficiently-fresh question.");
            ret = new QuestionFeederNextQuestion(null, null,
                            "Freshest question is too old. Current time = " + printTime(current)
                                            + ", while freshest question publication time = "
                                            + printTime(publishedDate));
        } else {
            boolean isNew = true;
            if (previousSentTime != null) {
                if (previousSentTime.before(publishedDate)) {
                    isNew = true;
                } else {
                    isNew = false;
                }
            }
            if (isNew) {
                ret = new QuestionFeederNextQuestion(fromRss.getId(), fromRss.createQuestion(), null);
            } else {
                logger.info("Could not find a question that is newer than the one previously sent.");
                ret = new QuestionFeederNextQuestion(null, null,
                                "Newest question is not newer than previous question (i.e., no new question was asked since the last question was sent).");
            }
        }

        previousCandidateTime = publishedDate;
        debug_assertNotNull(ret);
        return ret;
    }

    private static void debug_assertNotNull(Object object) throws QuestionFeedBugException {
        if (null == object) {
            throw new QuestionFeedBugException("BUG: Unexpected null");
        }
    }

    private static String printTime(final Calendar calendar) {
        return calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE) + ":"
                        + calendar.get(Calendar.SECOND);
    }



    // input
    protected final String rssUrl;
    protected final HoursMinutesSeconds maximumAgeOfQuestion;


    // internals
    private Calendar previousSentTime = null;
    private Calendar previousCandidateTime = null;

    private static final org.apache.log4j.Logger logger =
                    org.apache.log4j.Logger.getLogger(QuestionFeederFromRss.class);
}
