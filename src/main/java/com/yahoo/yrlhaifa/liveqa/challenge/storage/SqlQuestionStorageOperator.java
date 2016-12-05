// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.ChallengeCloseException;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.StorageFatalException;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.StorageNonFatalException;
import com.yahoo.yrlhaifa.liveqa.challenge.rep.Question;

/**
 * An implementation of {@link QuestionStorageOperator} which stores the questions in an SQL database.
 *
 * Date: Jan 21, 2015
 * 
 * @author Asher Stern
 *
 */
public class SqlQuestionStorageOperator implements QuestionStorageOperator {
    public SqlQuestionStorageOperator(Connection connection, StorageParameters storageParameters)
                    throws StorageFatalException {
        super();
        this.connection = connection;
        this.storageParameters = storageParameters;

        try {
            init();
        } catch (RuntimeException | SQLException e) {
            throw new StorageFatalException("Failed to initialize " + SqlQuestionStorageOperator.class.getName(), e);
        }

        if (!storageParameters.isIncrementalDatabaseAllowed()) {
            try {
                final Statement validateStatement = connection.createStatement();
                StorageUtilities.validateTableEmpty(validateStatement, "QUESTIONS");
            } catch (RuntimeException | SQLException e) {
                throw new StorageFatalException("Failed to validate that the question table is empty.", e);
            }
        }
    }


    @Override
    public synchronized void storeQuestion(Question question) throws StorageFatalException, StorageNonFatalException {
        try {
            int index = 1;
            statement.setString(index++, question.getId());
            statement.setString(index++, question.getTitle());
            statement.setString(index++, question.getBody());
            statement.setString(index++, question.getCategory());
            statement.setLong(index++, question.getPublishedDate().getTimeInMillis());

            int rowsAffected = statement.executeUpdate();
            if (rowsAffected != 1) {
                throw new StorageNonFatalException(
                                "Unexpected number of affected rows when inserting a new question into the question table. (expected number = 1, actual number = "
                                                + rowsAffected + ").");
            }

        } catch (RuntimeException | SQLException e) {
            throw new StorageNonFatalException("Failed to store the given question.", e);
        }
    }


    @Override
    public void close() throws ChallengeCloseException {
        try {
            statement.close();
        } catch (RuntimeException | SQLException e) {
            throw new ChallengeCloseException("Failed to close insert statement to question table.", e);
        }
    }

    private void init() throws SQLException {
        statement = connection.prepareStatement(STATEMENT_SQL_STRING);
    }

    private final Connection connection;
    @SuppressWarnings("unused")
    private final StorageParameters storageParameters;

    private static final String STATEMENT_SQL_STRING = "INSERT INTO QUESTIONS VALUES (?,?,?,?,?)";
    private PreparedStatement statement;

    @SuppressWarnings("unused")
    private static final org.apache.log4j.Logger logger =
                    org.apache.log4j.Logger.getLogger(SqlQuestionStorageOperator.class);
}
