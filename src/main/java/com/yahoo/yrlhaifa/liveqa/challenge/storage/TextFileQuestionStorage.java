// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.storage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import com.yahoo.yrlhaifa.haifa_utils.utils.StringUtils;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.ChallengeCloseException;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.StorageFatalException;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.StorageNonFatalException;
import com.yahoo.yrlhaifa.liveqa.challenge.rep.Question;

/**
 * This question-storage prints the question into a text file.
 *
 * <P>
 * Date: Feb 16, 2015
 * 
 * @author Asher Stern
 *
 */
public class TextFileQuestionStorage implements QuestionStorageOperator {
    public TextFileQuestionStorage(String fileName, boolean allowIncremental)
                    throws IOException, StorageFatalException {
        super();
        this.fileName = fileName;
        this.allowIncremental = allowIncremental;
        if (!allowIncremental) {
            File file = new File(fileName);
            if (file.exists()) {
                throw new StorageFatalException("The given file " + file.getAbsolutePath() + " already exist.");
            }
        }
        writer = new PrintWriter(new FileWriter(fileName, allowIncremental), true);
    }


    @Override
    public void storeQuestion(Question question) throws StorageFatalException, StorageNonFatalException {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append(question.getId()).append("\n");
            sb.append("Category: ").append(question.getCategory()).append("\n");
            sb.append("Title: ").append(question.getTitle()).append("\n");
            sb.append("Body: ").append(question.getBody()).append("\n");
            sb.append(separator).append("\n");

            synchronized (this) {
                writer.println(sb.toString());

            }
        } catch (RuntimeException e) {
            throw new StorageNonFatalException("Failed to store the given question.", e);
        }
    }


    @Override
    public void close() throws ChallengeCloseException {
        if (writer != null) {
            writer.close();
            logger.info("Question storage text file has been closed.");
        }
    }

    @SuppressWarnings("unused")
    private final String fileName;
    @SuppressWarnings("unused")
    private final boolean allowIncremental;

    private PrintWriter writer;
    private static final String separator = StringUtils.generateStringOfCharacter('-', 50);

    private static final org.apache.log4j.Logger logger =
                    org.apache.log4j.Logger.getLogger(TextFileQuestionStorage.class);
}
