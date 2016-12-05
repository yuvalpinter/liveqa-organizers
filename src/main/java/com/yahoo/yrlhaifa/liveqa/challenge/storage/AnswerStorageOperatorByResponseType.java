// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.storage;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.yahoo.yrlhaifa.liveqa.challenge.Participant;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.StorageFatalException;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.StorageNonFatalException;
import com.yahoo.yrlhaifa.liveqa.challenge.rep.ParticipantAnswer;
import com.yahoo.yrlhaifa.liveqa.challenge.rep.Question;

/**
 * Stores a participant's response (for a given question) in a storage, by handling the response as either answer
 * response, discard-reason response, or exception (kind of) response. For each type of response, a specific store
 * method is invoked.
 *
 * Date: Jan 20, 2015
 * 
 * @author Asher Stern
 *
 */
public abstract class AnswerStorageOperatorByResponseType implements AnswerStorageOperator {

    // Note - this method must be synchronized!!! There might be several threads that handle question-operation running
    // concurrently.
    // See main comment of AnswerStorageOperator.
    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.yrlhaifa.liveqa.challenge.storage.AnswerStorageOperator#storeAnswers(com.yahoo.yrlhaifa.liveqa.
     * challenge.Question, java.util.Map)
     */
    @Override
    public final synchronized void storeAnswers(Question question,
                    Map<Participant, ParticipantResponse> mapParticipantToResponse)
                                    throws StorageFatalException, StorageNonFatalException {
        storeInit(question, mapParticipantToResponse);
        try {
            List<String> failedSystems = new LinkedList<String>();
            StorageNonFatalException exception = null;

            for (Participant participant : mapParticipantToResponse.keySet()) {
                try {

                    final ParticipantResponse response = mapParticipantToResponse.get(participant);
                    final ResponseOperationInformation responseOperationInformation =
                                    response.getResponseOperationInformation();
                    if (response.getAnswer() != null) {
                        storeAnswer(question, participant, response.getAnswer(), responseOperationInformation);
                    } else if (response.getWhyNotAnswered() != null) {
                        storeWhyNotAnswered(question, participant, response.getWhyNotAnswered(),
                                        responseOperationInformation);
                    } else if (response.getWrongResponseException() != null) {
                        storeException(question, participant, response.getWrongResponseException(),
                                        responseOperationInformation);
                    } else {
                        throw new StorageNonFatalException(
                                        "Unexpected empty response (no answer, no discard-reason, no exception)");
                    }
                } catch (StorageNonFatalException e) {
                    logger.error("Storage failure.", e);
                    failedSystems.add(participant.getUniqueSystemId());
                    exception = new StorageNonFatalException("Failed to store an answer for question <"
                                    + question.getId() + "> for systems " + listToString(failedSystems)
                                    + ". See details in the nested exception.", e);
                }
            }

            if (exception != null) {
                throw exception;
            }
        } finally {
            storeEnd(question, mapParticipantToResponse);
        }

    }


    protected static String listToString(List<String> list) {
        StringBuilder sb = new StringBuilder();
        boolean firstIteration = true;
        for (String item : list) {
            if (firstIteration) {
                firstIteration = false;
            } else
                sb.append(",");
            sb.append(item);
        }
        return sb.toString();
    }

    protected abstract void storeInit(Question question, Map<Participant, ParticipantResponse> mapParticipantToResponse)
                    throws StorageFatalException, StorageNonFatalException;

    protected abstract void storeEnd(Question question, Map<Participant, ParticipantResponse> mapParticipantToResponse)
                    throws StorageFatalException, StorageNonFatalException;


    protected abstract void storeAnswer(final Question question, final Participant participant,
                    final ParticipantAnswer participantAnswer,
                    final ResponseOperationInformation responseOperationInformation)
                                    throws StorageFatalException, StorageNonFatalException;

    protected abstract void storeWhyNotAnswered(final Question question, final Participant participant,
                    final ReasonWhyQuestionDiscardByParticipant whyNotAnswered,
                    final ResponseOperationInformation responseOperationInformation)
                                    throws StorageFatalException, StorageNonFatalException;

    protected abstract void storeException(final Question question, final Participant participant,
                    final Exception exception, final ResponseOperationInformation responseOperationInformation)
                                    throws StorageFatalException, StorageNonFatalException;


    private static final org.apache.log4j.Logger logger =
                    org.apache.log4j.Logger.getLogger(AnswerStorageOperatorByResponseType.class);
}
