// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.utils;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.yahoo.yrlhaifa.haifa_utils.utils.FileUtils;
import com.yahoo.yrlhaifa.liveqa.challenge.rep.ParticipantAnswer;
import com.yahoo.yrlhaifa.liveqa.challenge.rep.Question;
import com.yahoo.yrlhaifa.liveqa.challenge.rep.QuestionWithAnswers;

public class TextualLogScraper implements Iterator<QuestionWithAnswers> {

    private static final String ANSWERER_SEPARATOR = "--------------------------------------------------";
    private static final String ANSWERS_FOR_QUESTION_SEPARATOR = "==================================================";

    private Iterator<String> questionLineFeed;
    private Iterator<String> answerLineFeed;
    private boolean includeQuestionsWithNoAnswers;

    private QuestionWithAnswers next = null;

    public TextualLogScraper(String questionFile, String answerFile, boolean includeQuestionsWithNoAnswers)
                    throws IOException {
        questionLineFeed = FileUtils.iterateLines(questionFile).iterator();
        answerLineFeed = FileUtils.iterateLines(answerFile).iterator();
        this.includeQuestionsWithNoAnswers = includeQuestionsWithNoAnswers;
        extractNext();
    }

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

        String qid = null, title = null, category = null, body = "", participant = "", answer = null, resources = null;
        Question question = null;
        Map<String, ParticipantAnswer> answers = new HashMap<>();
        String q = questionLineFeed.next(), a = answerLineFeed.next();

        while (questionLineFeed.hasNext() && answerLineFeed.hasNext()) {

            // get question parts
            while (!q.equals(ANSWERER_SEPARATOR)) {
                if (q.startsWith("YA:")) {
                    qid = q;
                    q = questionLineFeed.next();
                }
                if (q.startsWith("Category: ")) {
                    category = q.substring(10);
                    q = questionLineFeed.next();
                }
                if (q.startsWith("Title: ")) {
                    title = q.substring(7) + "\t";
                    q = questionLineFeed.next();
                }
                q = q.replace("Body: ", "");
                while (!q.isEmpty() && !q.startsWith(ANSWERER_SEPARATOR)) {
                    body += (q + "\t");
                    q = questionLineFeed.next();
                }
                title = title.trim();
                body = body.trim();
                if (q.isEmpty()) {
                    q = questionLineFeed.next();
                }
            }

            // create question
            a = a.replace("Question id = ", "");
            if (a.startsWith("YA:")) {
                if (!a.equals(qid)) {
                    if (includeQuestionsWithNoAnswers) {
                        next = new QuestionWithAnswers(new Question(qid, title, body, category, null), new HashMap<>());
                        q = questionLineFeed.next();
                        return;
                    } else {
                        // essentially discard question
                        System.err.println("Question not matched to any answers: " + qid);
                        q = questionLineFeed.next();
                        continue;
                    }
                }
            }
            question = new Question(qid, title, body, category, null);

            // start reading answers for this question
            a = answerLineFeed.next();

            // get responses
            while (!a.equals(ANSWERS_FOR_QUESTION_SEPARATOR)) {
                // loop over responses
                if (a.startsWith("Participant unique id = ")) {
                    participant = a.substring(24);
                    a = answerLineFeed.next();
                }
                if (a.startsWith("Answer: ")) {
                    answer = a.substring(8) + "\t";
                    a = answerLineFeed.next();
                } else {
                    while (!a.equals(ANSWERER_SEPARATOR) && !a.equals(ANSWERS_FOR_QUESTION_SEPARATOR)) {
                        a = answerLineFeed.next();
                    }
                    if (a.equals(ANSWERER_SEPARATOR)) {
                        a = answerLineFeed.next();
                    }
                    continue;
                }
                while (!a.startsWith("Reported answer time: ")) {
                    answer += (a + "\t");
                    a = answerLineFeed.next();
                }
                a = answerLineFeed.next(); // ignore reported time TODO don't ignore it?
                if (a.startsWith("Resources: ")) {
                    resources = a.substring(11);
                    a = answerLineFeed.next();
                    // TODO handle newline-delimited resource lists
                }

                a = answerLineFeed.next(); // TODO Title foci
                a = answerLineFeed.next(); // TODO Body foci
                a = answerLineFeed.next(); // TODO Question summary

                // create response
                answers.put(participant, new ParticipantAnswer(answer, -1, Arrays.asList(resources.split(",")), null,
                                null, null));

                // check if last for question
                if (a.equals(ANSWERER_SEPARATOR)) {
                    a = answerLineFeed.next();
                }
            }

            // wrap it up
            next = new QuestionWithAnswers(question, answers);

            q = questionLineFeed.next();
            a = answerLineFeed.next();

            return;
        }
        next = null;
    }

}
