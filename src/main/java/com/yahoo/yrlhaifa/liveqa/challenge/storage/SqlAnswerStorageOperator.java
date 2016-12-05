// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.storage;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Map;

import com.yahoo.yrlhaifa.liveqa.challenge.Participant;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.ChallengeCloseException;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.StorageFatalException;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.StorageNonFatalException;
import com.yahoo.yrlhaifa.liveqa.challenge.rep.ParticipantAnswer;
import com.yahoo.yrlhaifa.liveqa.challenge.rep.Question;
import com.yahoo.yrlhaifa.liveqa.common.ExceptionUtilities;

import static com.yahoo.yrlhaifa.liveqa.challenge.storage.StorageUtilities.validateTableEmpty;

/**
 * An implementation of {@link AnswerStorageOperator} which stores the answers (returned from participants) in an SQL
 * database.
 *
 * Date: Jan 20, 2015
 * 
 * @author Asher Stern
 *
 */
public class SqlAnswerStorageOperator extends AnswerStorageOperatorByResponseType {
    public SqlAnswerStorageOperator(Connection connection, StorageParameters storageParameters)
                    throws StorageFatalException {
        super();
        this.connection = connection;
        this.storageParameters = storageParameters;

        try {
            init();
        } catch (SQLException e) {
            throw new StorageFatalException("Cannot create an insert statement. "
                            + SqlAnswerStorageOperator.class.getName() + " failed.", e);
        }

        try {
            validateAllTablesAreEmpty();
        } catch (SQLException e) {
            throw new StorageFatalException(
                            "Cannot validate preconditions. " + SqlAnswerStorageOperator.class.getName() + " failed.",
                            e);
        }

    }


    @Override
    public void close() throws ChallengeCloseException {
        try {
            insertStatement.close();
        } catch (RuntimeException | SQLException e) {
            throw new ChallengeCloseException("Failed to close INSERT INTO statement.", e);
        }
    }



    @Override
    protected synchronized void storeInit(Question question,
                    Map<Participant, ParticipantResponse> mapParticipantToResponse)
                                    throws StorageFatalException, StorageNonFatalException {}

    @Override
    protected synchronized void storeEnd(Question question,
                    Map<Participant, ParticipantResponse> mapParticipantToResponse)
                                    throws StorageFatalException, StorageNonFatalException {}

    @Override
    protected synchronized void storeAnswer(Question question, Participant participant,
                    ParticipantAnswer participantAnswer,
                    final ResponseOperationInformation responseOperationInformation)
                                    throws StorageFatalException, StorageNonFatalException {

        final String qid = question.getId();
        final String systemUniqueId = participant.getUniqueSystemId();

        logger.info("Store answer for question " + qid + " for participant " + systemUniqueId);

        try {
            int index = 1;
            insertStatement.setString(index++, question.getId());
            insertStatement.setString(index++, participant.getParticipantOrganizationId());
            insertStatement.setString(index++, participant.getParticipantSystemId());
            insertStatement.setString(index++, participant.getUniqueSystemId());
            insertStatement.setString(index++, participantAnswer.getAnswer());
            insertStatement.setString(index++, listToString(participantAnswer.getReportedResources()));
            insertStatement.setString(index++, participantAnswer.getTitleFocusSpans());
            insertStatement.setString(index++, participantAnswer.getBodyFocusSpans());
            insertStatement.setString(index++, participantAnswer.getQuestionSummary());
            insertStatement.setLong(index++, responseOperationInformation.getDurationOfRequestResponseInMilliseconds());
            insertStatement.setLong(index++, participantAnswer.getReportedAnsweringTime());

            int rowsAffected = insertStatement.executeUpdate();
            if (rowsAffected != 1) {
                throw new StorageNonFatalException(
                                "Insert answer into DB table did not operate propertly, since rowsAffected != 1");
            }

        } catch (RuntimeException | SQLException e) {
            throw new StorageNonFatalException("Insert answer into table failed.", e);
        }
    }

    @Override
    protected synchronized void storeWhyNotAnswered(Question question, Participant participant,
                    ReasonWhyQuestionDiscardByParticipant whyNotAnswered,
                    final ResponseOperationInformation responseOperationInformation)
                                    throws StorageFatalException, StorageNonFatalException {
        try {
            if (whyNotAnswered != null) {
                final String reason = whyNotAnswered.getReason();
                if (reason != null) {
                    int index = 1;
                    discardInsertStatement.setString(index++, question.getId());
                    discardInsertStatement.setString(index++, participant.getParticipantOrganizationId());
                    discardInsertStatement.setString(index++, participant.getParticipantSystemId());
                    discardInsertStatement.setString(index++, participant.getUniqueSystemId());
                    discardInsertStatement.setString(index++, reason);

                    int rowsAffected = discardInsertStatement.executeUpdate();
                    if (rowsAffected != 1) {
                        throw new StorageNonFatalException(
                                        "Insert discard-reason into DB table did not operate propertly, since rowsAffected != 1");
                    }
                }
            }
        } catch (RuntimeException | SQLException e) {
            throw new StorageNonFatalException("Insert discard-reason into table failed.", e);
        }
    }

    @Override
    protected synchronized void storeException(Question question, Participant participant, Exception exception,
                    final ResponseOperationInformation responseOperationInformation)
                                    throws StorageFatalException, StorageNonFatalException {
        if (exception != null) {
            try {
                int index = 1;
                exceptionInsertStatement.setString(index++, question.getId());
                exceptionInsertStatement.setString(index++, participant.getParticipantOrganizationId());
                exceptionInsertStatement.setString(index++, participant.getParticipantSystemId());
                exceptionInsertStatement.setString(index++, participant.getUniqueSystemId());
                exceptionInsertStatement.setString(index++, ExceptionUtilities.getMessages(exception));

                int rowsAffected = exceptionInsertStatement.executeUpdate();
                if (rowsAffected != 1) {
                    throw new StorageNonFatalException(
                                    "Insert exception message into DB table did not operate propertly, since rowsAffected != 1");
                }
            } catch (RuntimeException | SQLException e) {
                throw new StorageNonFatalException("Insert exception message into table failed.", e);
            }
        }
    }



    private void init() throws SQLException {
        insertStatement = connection.prepareStatement(INSERT_STATEMENT_STRING);
        discardInsertStatement = connection.prepareStatement(DISCARD_INSERT_STATEMENT_STRING);
        exceptionInsertStatement = connection.prepareStatement(EXCEPTION_INSERT_STATEMENT_STRING);
    }

    private void validateAllTablesAreEmpty() throws StorageFatalException, SQLException {
        if (storageParameters.isIncrementalDatabaseAllowed()) {
            logger.warn("Note: incremental database is allowed. If the challenge starts now, this means that the results might be malformed.");
        } else {
            Statement statement = connection.createStatement();
            try {
                validateTableEmpty(statement, "ANSWERS");
                validateTableEmpty(statement, "WHYNOTANSWERED");
                validateTableEmpty(statement, "BADRESPONSE");
            } finally {
                statement.close();
            }
        }
    }



    private final StorageParameters storageParameters;

    private final Connection connection;

    private PreparedStatement insertStatement = null;
    private PreparedStatement discardInsertStatement = null;
    private PreparedStatement exceptionInsertStatement = null;

    private static final String INSERT_STATEMENT_STRING =
                    "INSERT INTO ANSWERS (QID,PARTICIPANTNAME,SYSTEMNAME,SYSTEMUNIQUEID,ANSWERCONTENT,RESOURCES,TITLESPANS,BODYSPANS,QUESTIONSUMMARY,RESPONSEDURATION,REPORTEDDURATION) VALUES (?,?,?,?,?,?,?,?,?,?,?)";
    private static final String DISCARD_INSERT_STATEMENT_STRING =
                    "INSERT INTO WHYNOTANSWERED (QID,PARTICIPANTNAME,SYSTEMNAME,SYSTEMUNIQUEID,DISCARDREASON) VALUES (?,?,?,?,?)";
    private static final String EXCEPTION_INSERT_STATEMENT_STRING =
                    "INSERT INTO BADRESPONSE (QID,PARTICIPANTNAME,SYSTEMNAME,SYSTEMUNIQUEID,EXCEPTIONMESSAGES) VALUES (?,?,?,?,?)";



    private static final org.apache.log4j.Logger logger =
                    org.apache.log4j.Logger.getLogger(SqlAnswerStorageOperator.class);



}
