// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.ChallengeSystemException;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.QuestionOperationException;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.StorageFatalException;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.StorageNonFatalException;
import com.yahoo.yrlhaifa.liveqa.challenge.rep.Question;
import com.yahoo.yrlhaifa.liveqa.challenge.storage.AnswerStorageOperator;
import com.yahoo.yrlhaifa.liveqa.challenge.storage.ParticipantResponse;
import com.yahoo.yrlhaifa.liveqa.challenge.storage.QuestionStorageOperator;


/**
 * Sends that questions to the participants and stores their answers, by using a {@link ConcurrentMap} to temporarily
 * store participants' answers. For each participant a new thread is started, and the map is shared between all threads.
 * Each thread puts the answer of its participant into the map, where the key is the participant's system-id, and the
 * value is the answer. Later, {@link AnswerStorageOperator} stores the map's contents into the permanent storage.
 *
 * Date: Jan 13, 2015
 * 
 * @author Asher Stern
 *
 */
public abstract class QuestionOperatorUsingAnswerMap extends QuestionOperatorWithQuestionStorage {
    public QuestionOperatorUsingAnswerMap(List<Participant> participants,
                    QuestionStorageOperator questionStorageOperator, AnswerStorageOperator storageOperator)
                                    throws ChallengeSystemException {
        super(participants, questionStorageOperator);
        this.storageOperator = storageOperator;
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.yrlhaifa.liveqa.challenge.QuestionOperator#operate(com.yahoo.yrlhaifa.liveqa.challenge.Question)
     */
    @Override
    protected final void operate_sendToParticipants(final Question question)
                    throws QuestionOperationException, StorageFatalException {
        ConcurrentMap<Participant, ParticipantResponse> mapSystemIdToAnswer =
                        new ConcurrentHashMap<Participant, ParticipantResponse>();
        try {
            operateByFillingMap(question, mapSystemIdToAnswer);
            try {
                storageOperator.storeAnswers(question, mapSystemIdToAnswer);
            } catch (StorageFatalException e) {
                throw e;
            } catch (StorageNonFatalException e) {
                // TODO add additional exception handling here.
                logger.error("Failed to store answers.", e);
            }
        } catch (InterruptedException e) {
            // I was interrupted. Someone wants me to stop immediately. Well, that's what I do. Let's stop immediately,
            // while indicating this
            // in the thread's interrupt flag.
            Thread.currentThread().interrupt();
            throw new QuestionOperationException(
                            "Execution stops due to interrupt request. More details in the nested exception.", e);
        }


    }

    /**
     * Send the question and temporarily store the answers in the given map. Then, read the map and store the answers in
     * the permanent storage.
     * 
     * @param question A question to be sent to all participants.
     * @param mapSystemIdToAnswer A map from system-id to the answer that was answered by that system.
     * @throws QuestionOperationException Indicates a fatal problem in sending the questions or in storing them in the
     *         permanent storage.
     * @throws InterruptedException Indicates that the thread that sends the questions was interrupted, and the program
     *         should exit immediately (but gracefully).
     */
    protected abstract void operateByFillingMap(final Question question,
                    final ConcurrentMap<Participant, ParticipantResponse> mapParticipantToAnswer)
                                    throws QuestionOperationException, InterruptedException;


    protected final AnswerStorageOperator storageOperator;

    private static final org.apache.log4j.Logger logger =
                    org.apache.log4j.Logger.getLogger(QuestionOperatorUsingAnswerMap.class);
}
