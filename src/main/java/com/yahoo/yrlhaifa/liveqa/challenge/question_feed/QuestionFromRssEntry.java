// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.question_feed;

import static com.yahoo.yrlhaifa.liveqa.challenge.Constants.WORKING_LOCALE;
import static com.yahoo.yrlhaifa.liveqa.challenge.Constants.QUESTION_ID_YAHOO_ANSWERS_NAMESPACE_PREFIX;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import com.sun.syndication.feed.synd.SyndEntry;
import com.yahoo.yrlhaifa.liveqa.challenge.Constants;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.QuestionFeedBugException;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.QuestionFeedNonFatalException;
import com.yahoo.yrlhaifa.liveqa.challenge.rep.Question;

/**
 * Reads a single Yahoo-Answers question from an RSS entry (one entry from the list of entries that reside in an RSS
 * feed).
 *
 * Date: Jan 15, 2015
 * 
 * @author Asher Stern
 *
 */
public class QuestionFromRssEntry {
    public static final char CATEGORY_START = '[';
    public static final char CATEGORY_END = ']';


    public static QuestionFromRssEntry createFromRssEntry(final SyndEntry rssSyndEntry)
                    throws QuestionFeedNonFatalException, QuestionFeedBugException {
        if (null == rssSyndEntry) {
            throw new QuestionFeedBugException("Received null rssSyncEntry.");
        }
        String id = null;
        String title = null;
        String body = null;
        Calendar publishedDate = null;
        String category = null;

        final String grossTitle = rssSyndEntry.getTitleEx().getValue();
        boolean titleAndCategoryExtractionOK = false;
        int categoryStartIndex = grossTitle.indexOf(CATEGORY_START);
        if (categoryStartIndex >= 0) {
            int categoryEndIndex = grossTitle.indexOf(CATEGORY_END, categoryStartIndex + 1);
            if (categoryEndIndex >= 0) {
                if (categoryEndIndex < grossTitle.length()) {
                    titleAndCategoryExtractionOK = true;
                    title = grossTitle.substring(categoryEndIndex + 1, grossTitle.length()).trim();
                    title = normalizeQuestionTitle(title);
                    category = grossTitle.substring(categoryStartIndex + 1, categoryEndIndex).trim();
                }
            }
        }
        if (!titleAndCategoryExtractionOK) {
            throw new QuestionFeedNonFatalException(
                            "Failed to extract title and category. The gross title is: \"" + grossTitle + "\".");
        }

        id = extractIdFromLink(rssSyndEntry.getLink());
        if (null == id) {
            throw new QuestionFeedNonFatalException("Failed to extract ID. line = " + rssSyndEntry.getLink());
        }
        body = rssSyndEntry.getDescription().getValue();
        publishedDate = Calendar.getInstance(TimeZone.getTimeZone("UTC"), WORKING_LOCALE);
        final Date publishedDateObject = rssSyndEntry.getPublishedDate();
        if (null == publishedDateObject) {
            throw new QuestionFeedNonFatalException("\"published date\" is null.");
        }
        publishedDate.setTime(publishedDateObject);


        return new QuestionFromRssEntry(id, title, body, publishedDate, category);
    }



    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getBody() {
        return body;
    }

    public Calendar getPublishedDate() {
        return publishedDate;
    }

    public String getCategory() {
        return category;
    }

    public Question createQuestion() {
        return new Question(getId(), getTitle(), getBody(), getCategory(), getPublishedDate());
    }



    protected QuestionFromRssEntry(String id, String title, String body, Calendar publishedDate, String category) {
        super();
        this.id = id;
        this.title = title;
        this.body = body;
        this.publishedDate = publishedDate;
        this.category = category;
    }



    private static String extractIdFromLink(final String link) {
        if (null == link) {
            return null;
        }
        String ret = null;
        final String prefix = Constants.ANSWER_URL_PREFIX; // "https://answers.yahoo.com/question/index?qid=";
        if (link.startsWith(prefix)) {
            ret = link.substring(prefix.length());
            ret = QUESTION_ID_YAHOO_ANSWERS_NAMESPACE_PREFIX + ret;
        } else {
            ret = null;
        }


        return ret;
    }


    private static String normalizeQuestionTitle(final String title) {
        String normalized = title;
        if (title.startsWith(Constants.QUESTION_TITLE_PREFIX_TO_FILTER)) {
            if (title.length() <= Constants.QUESTION_TITLE_PREFIX_TO_FILTER.length()) {
                normalized = "";
            } else {
                normalized = title.substring(Constants.QUESTION_TITLE_PREFIX_TO_FILTER.length(), title.length());
            }
        } else {
            normalized = title;
        }
        return normalized;
    }



    private final String id;
    private final String title;
    private final String body;
    private final Calendar publishedDate;
    private final String category;
}
