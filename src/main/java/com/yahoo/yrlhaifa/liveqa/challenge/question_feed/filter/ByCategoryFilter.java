// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.question_feed.filter;

import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;

import com.yahoo.yrlhaifa.liveqa.challenge.rep.Question;
import com.yahoo.yrlhaifa.liveqa.common.CommonUtilities;

/**
 * 
 *
 * <P>
 * Date: Apr 1, 2015
 * 
 * @author Asher Stern
 *
 */
public class ByCategoryFilter implements QuestionFilter {
    public ByCategoryFilter(Set<String> filterCategories, boolean categoryFilterIsBlacklist) {
        this.categoryFilterIsBlacklist = categoryFilterIsBlacklist;
        if (filterCategories != null) {
            this.filterCategories = new LinkedHashSet<>();
            for (String category : filterCategories) {
                this.filterCategories.add(normalizeCategory(category));
            }
        } else {
            this.filterCategories = null;
        }
    }

    @Override
    public boolean questionOK(Question question) {
        if (null == filterCategories)
            return true;
        if (categoryFilterIsBlacklist) {
            return (!filterCategories.contains(normalizeCategory(question.getCategory())));
        }
        return (filterCategories.contains(normalizeCategory(question.getCategory())));
    }


    private static String normalizeCategory(final String category) {
        return CommonUtilities.mergeSpaces(category).trim().toLowerCase(Locale.US);
    }

    private Set<String> filterCategories;
    private boolean categoryFilterIsBlacklist;

}
