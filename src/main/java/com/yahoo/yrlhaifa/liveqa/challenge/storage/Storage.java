// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.storage;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Arrays;

import com.yahoo.yrlhaifa.liveqa.challenge.ChallengeAutoCloseable;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.ChallengeCloseException;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.StorageFatalException;

/**
 * Creates {@link QuestionStorageOperator} and {@link AnswerStorageOperator}, which are used to store the questions and
 * the answers (the answers returned from participants).
 *
 * Date: Jan 20, 2015
 * 
 * @author Asher Stern
 *
 */
public class Storage implements ChallengeAutoCloseable {
    public static final String DERBY_PREFIX = "jdbc:derby:";
    public static final String DERBY_SHUTDOWN_POSTFIX = ";shutdown=true";

    public Storage(StorageParameters parameters) {
        super();
        this.parameters = parameters;
    }

    public QuestionStorageOperator constructQuestionStorageOperator() throws StorageFatalException {
        try {
            questionConnection = DriverManager.getConnection(parameters.getConnectionString());

            return new AggregatedQuestionStorageOperator(Arrays.asList(new QuestionStorageOperator[] {
                            new SqlQuestionStorageOperator(questionConnection, parameters),
                            new TextFileQuestionStorage(parameters.getQuestionStorageFile(),
                                            parameters.isIncrementalDatabaseAllowed())}));

        } catch (SQLException e) {
            throw new StorageFatalException("Cannot open connection to question storage database.", e);
        } catch (IOException e) {
            throw new StorageFatalException("Cannot open question storage text file for write.", e);
        }
    }

    public AnswerStorageOperator constructAnswerStorageOperator() throws StorageFatalException {
        try {
            answerConnection = DriverManager.getConnection(parameters.getConnectionString());

            return new AggregatedAnswerStorageOperator(Arrays.asList(
                            new AnswerStorageOperator[] {new SqlAnswerStorageOperator(answerConnection, parameters),
                                            new TextFileAnswerStorage(parameters.getAnswerStorageFile(),
                                                            parameters.isIncrementalDatabaseAllowed())}));

        } catch (SQLException e) {
            throw new StorageFatalException("Failed to connect to Data-Base.", e);
        } catch (IOException e) {
            throw new StorageFatalException("Cannot open answer storage text file for write.", e);
        }

    }

    @Override
    public void close() throws ChallengeCloseException {
        logger.info("Storage close...");
        try {
            if (answerConnection != null) {
                answerConnection.close();
            }
            if (questionConnection != null) {
                questionConnection.close();
            }
            final String connectionString = parameters.getConnectionString();
            if (connectionString.startsWith(DERBY_PREFIX)) {
                try {
                    logger.info("Shut down DERBY ...");
                    DriverManager.getConnection(connectionString + DERBY_SHUTDOWN_POSTFIX);
                    logger.info("Shut down DERBY - done.");
                } catch (java.sql.SQLException sqle) {
                    logger.info("Derby shutdown has thrown an exception. Usually it does not indicate an error. Exception class = \""
                                    + sqle.getClass().getName() + "\". Exception message = " + sqle.getMessage()
                                    + "\nProgram continues.");
                }
            }
            logger.info("Storage close - done.");
        } catch (RuntimeException | SQLException e) {
            throw new ChallengeCloseException("Failed to close Data-Base connection.", e);
        }
    }


    private final StorageParameters parameters;

    private Connection answerConnection = null;
    private Connection questionConnection = null;

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(Storage.class);
}
