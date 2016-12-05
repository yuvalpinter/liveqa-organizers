// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.question_feed;

import java.io.File;
import java.util.Set;

import com.yahoo.yrlhaifa.liveqa.common.HoursMinutesSeconds;

/**
 * Parameters (typically set in configuration file) for {@link QuestionFeederFactory}.
 * 
 * Date: Jan 21, 2015
 * 
 * @author Asher Stern
 * 
 */
public class QuestionFeederFactoryParameters {
    public QuestionFeederFactoryParameters(String rssUrl, HoursMinutesSeconds maximumAgeOfQuestion, File badWordsFile,
                    Set<String> filterCategories, boolean categoryFilterIsBlacklist) {
        super();
        this.rssUrl = rssUrl;
        this.maximumAgeOfQuestion = maximumAgeOfQuestion;
        this.badWordsFile = badWordsFile;
        this.filterCategories = filterCategories;
        this.categoryFilterIsBlacklist = categoryFilterIsBlacklist;
    }


    public String getRssUrl() {
        return rssUrl;
    }

    public HoursMinutesSeconds getMaximumAgeOfQuestion() {
        return maximumAgeOfQuestion;
    }

    public File getBadWordsFile() {
        return badWordsFile;
    }

    public Set<String> getFilterCategories() {
        return filterCategories;
    }

    public boolean getCategoryFilterIsBlacklist() {
        return categoryFilterIsBlacklist;
    }



    private final String rssUrl;
    private final HoursMinutesSeconds maximumAgeOfQuestion;

    private final File badWordsFile;

    private final Set<String> filterCategories;
    private final boolean categoryFilterIsBlacklist;
}
