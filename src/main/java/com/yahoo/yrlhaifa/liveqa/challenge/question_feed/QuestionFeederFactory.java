// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.question_feed;

import java.util.LinkedList;
import java.util.List;

import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.ChallengeSystemException;
import com.yahoo.yrlhaifa.liveqa.challenge.question_feed.filter.AggregatedQuestionFilter;
import com.yahoo.yrlhaifa.liveqa.challenge.question_feed.filter.BadWordsQuestionFilter;
import com.yahoo.yrlhaifa.liveqa.challenge.question_feed.filter.ByCategoryFilter;
import com.yahoo.yrlhaifa.liveqa.challenge.question_feed.filter.QuestionFilter;
import com.yahoo.yrlhaifa.liveqa.challenge.rep.Question;

/**
 * Factory of {@link QuestionFeeder}.
 * 
 * Date: Jan 13, 2015
 * 
 * @author Asher Stern
 * 
 */
public class QuestionFeederFactory {
    public QuestionFeederFactory(QuestionFeederFactoryParameters feederParameters) {
        super();
        this.feederParameters = feederParameters;
    }

    public QuestionFeeder createQuestionFeeder() throws ChallengeSystemException {
        List<QuestionFilter> filters = new LinkedList<>();
        filters.add(new DummyEnglishClassifierQuestionFilter());
        filters.add(BadWordsQuestionFilter.createInstanceFromFile(feederParameters.getBadWordsFile().getPath()));
        if (feederParameters.getFilterCategories() != null) {
            filters.add(new ByCategoryFilter(feederParameters.getFilterCategories(),
                            feederParameters.getCategoryFilterIsBlacklist()));
        }

        QuestionFilter filter = new AggregatedQuestionFilter(filters);

        QuestionFeederFromRss feeder = new QuestionFeederFromRss(filter, feederParameters.getRssUrl(),
                        feederParameters.getMaximumAgeOfQuestion());

        return feeder;
    }


    private class DummyEnglishClassifierQuestionFilter implements QuestionFilter {

        private EnglishClassifier classifier;

        public DummyEnglishClassifierQuestionFilter() throws ChallengeSystemException {
            try {
                // TODO initialize English language classifier, change log message
                logger.warn("No English classifier implemented, using dummy classifier.");
            } catch (Exception e) {
                throw new ChallengeSystemException("Could not construct English classifier.", e);
            }

        }

        @Override
        public boolean questionOK(Question question) {
        	if (classifier == null) {
        		logger.warn("No English classifier implemented, using dummy classifier.");
        		return true;
            }
        	
        	String questionString = (question.getTitle() + " " + question.getBody()).replaceAll("\\s+", " ");
            boolean ok = classifier.classify(questionString).label();
            if (ok) {
                logger.info("Question " + question.getId() + " was classified as an English question.");
            } else {
                logger.info("Question " + question.getId() + " was classified as a non-English question.");
            }
            return ok;
        }
    }

    private final QuestionFeederFactoryParameters feederParameters;

    private static final org.apache.log4j.Logger logger =
                    org.apache.log4j.Logger.getLogger(QuestionFeederFactory.class);
}
