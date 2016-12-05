// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.question_feed.filter;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;

import com.yahoo.yrlhaifa.haifa_utils.utils.StringUtils;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.ChallengeSystemException;
import com.yahoo.yrlhaifa.liveqa.challenge.rep.Question;

/**
 * Filters out question which contain bad words, specified by the given set of words. This filter can be used to filter
 * question with harassment intent.
 *
 * <P>
 * Date: Feb 3, 2015
 * 
 * @author Asher Stern
 *
 */
public class BadWordsQuestionFilter implements QuestionFilter {
    /**
     * Creates this filter, where the bad-words are taken from a text file, in which each line is a bad word.
     * 
     * @param filename
     * @return
     * @throws ChallengeSystemException
     */
    public static BadWordsQuestionFilter createInstanceFromFile(final String filename) throws ChallengeSystemException {
        try {
            return new BadWordsQuestionFilter(loadFileOfWords(filename));
        } catch (IOException e) {
            throw new ChallengeSystemException("Could not read file of bad words.");
        }
    }

    public BadWordsQuestionFilter(Set<String> badWordsLowerCase) {
        super();
        this.badWordsLowerCase = badWordsLowerCase;
    }

    @Override
    public boolean questionOK(Question question) {
        return (!(stringContainsBadWord(question.getTitle()) || stringContainsBadWord(question.getBody())));
    }

    private boolean stringContainsBadWord(final String str) {
        Set<String> words = splitStringIntoLowercaseTrimmedWords(str);
        for (String word : words) {
            if (badWordsLowerCase.contains(word)) {
                return true;
            }
        }
        return false;
    }

    private Set<String> splitStringIntoLowercaseTrimmedWords(final String str) {
        Set<String> words = new LinkedHashSet<String>();
        if (str != null) {
            for (String word : str.split("\\s+")) {
                String normalized = StringUtils.trimNeitherLetterNorDigit(word);
                words.add(normalized.trim().toLowerCase());
            }
        }
        return words;
    }


    private static Set<String> loadFileOfWords(final String filename) throws IOException {
        Set<String> words = new LinkedHashSet<String>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim().toLowerCase();
                if (line.length() > 0) {
                    words.add(line);
                }
            }
        }
        return words;
    }

    private final Set<String> badWordsLowerCase;
}
