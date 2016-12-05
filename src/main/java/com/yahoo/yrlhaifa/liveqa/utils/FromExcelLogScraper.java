// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.utils;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import com.google.common.collect.Lists;
import com.yahoo.yrlhaifa.haifa_utils.utils.FileUtils;
import com.yahoo.yrlhaifa.haifa_utils.utils.StringUtils;
import com.yahoo.yrlhaifa.liveqa.challenge.rep.ParticipantAnswer;
import com.yahoo.yrlhaifa.liveqa.challenge.rep.Question;
import com.yahoo.yrlhaifa.liveqa.challenge.rep.QuestionWithAnswers;

public class FromExcelLogScraper implements Iterator<QuestionWithAnswers> {

    /**
     * Format: QID \t Title \t Body \t Category \t Date
     */
    private Iterator<String> questionLineFeed;

    /**
     * Format: QID \t Participant \t Answer \t (Resource[,;])*
     */
    private Iterator<String> answerLineFeed;

    private boolean includeQuestionsWithNoAnswers;

    private QuestionWithAnswers next = null;
    private String leftoverAnswer = null;

    public FromExcelLogScraper(String questionFile, String answerFile, boolean includeQuestionsWithNoAnswers)
                    throws IOException {
        questionLineFeed = FileUtils.iterateLines(questionFile).iterator();
        answerLineFeed = FileUtils.iterateLines(answerFile).iterator();
        this.includeQuestionsWithNoAnswers = includeQuestionsWithNoAnswers;
        extractNext();
    }

    @Override
    public QuestionWithAnswers next() {
        QuestionWithAnswers ret = next;
        extractNext();
        return ret;
    }

    @Override
    public boolean hasNext() {
        return next != null;
    }

    private void extractNext() {
        String q = questionLineFeed.next();
        if (q == null) {
            next = null;
            return;
        }
        
        String[] parts = StringUtils.split(q, "\t", true).toArray(new String[0]);
        String qid = parts[0];
        Calendar cal = null;
        if (parts.length > 4) {
            getCal(parts[4]);
        }
        Question qObj = new Question(parts[0], parts[1], parts[2], parts[3], cal);
        Map<String, ParticipantAnswer> answers = new HashMap<>();
        String a = leftoverAnswer == null ? answerLineFeed.next() : leftoverAnswer;
        while (a != null && parts[0].equals(qid)) {
            parts = a.split("\\t");
            answers.put(parts[1],
                            new ParticipantAnswer(parts[2], -1, resources(parts), null, null, null));

            a = answerLineFeed.next();
        }
        leftoverAnswer = a;

        if (includeQuestionsWithNoAnswers || !answers.isEmpty()) {
            next = new QuestionWithAnswers(qObj, answers);
        }
    }

    protected List<String> resources(String[] parts) {
        if (parts.length < 4) {
            return Lists.newArrayList();
        }
        return Arrays.asList(parts[3].split(",;"));
    }

    protected Calendar getCal(String time) {
        SimpleDateFormat sdf = new SimpleDateFormat("M/dd/YY H:mm", Locale.US); // 5/31/16 0:00
        Calendar cal = Calendar.getInstance();
        try {
            cal.setTime(sdf.parse(time));
        } catch (ParseException e) {
            cal.set(2016, 5, 31, 0, 0);
        }
        return cal;
    }

}
