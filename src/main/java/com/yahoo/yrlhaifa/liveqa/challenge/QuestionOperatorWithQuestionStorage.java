// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.ChallengeSystemException;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.QuestionOperationException;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.StorageFatalException;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.StorageNonFatalException;
import com.yahoo.yrlhaifa.liveqa.challenge.rep.Question;
import com.yahoo.yrlhaifa.liveqa.challenge.storage.QuestionStorageOperator;

/**
 * An abstract {@link QuestionOperator} which stores the question (sent to the participants) in a storage.<BR>
 * The main role of {@link QuestionOperator} is to send the question to the participants, collect their answers and
 * store them. This {@link QuestionOperator} performs, in addition, the task of storing the question itself in a given
 * storage.
 * <P>
 * Note that the task of storing the question is performed in a separate thread, such that it does not delay the task of
 * sending the question to the participants.
 *
 * <P>
 * Date: Jan 20, 2015
 * 
 * @author Asher Stern
 *
 */
public abstract class QuestionOperatorWithQuestionStorage extends QuestionOperator {
    protected QuestionOperatorWithQuestionStorage(List<Participant> participants,
                    QuestionStorageOperator questionStorageOperator) throws ChallengeSystemException {
        super(participants);
        this.questionStorageOperator = questionStorageOperator;
    }


    @Override
    public final void operate(final Question question) throws QuestionOperationException, StorageFatalException {
        // Create a new thread which stores the question
        // This exceptionReference holds exception captured by question-store thread.
        final AtomicReference<StorageFatalException> exceptionReference =
                        new AtomicReference<StorageFatalException>(null);
        Thread questionStoreThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    logger.debug("Store question thread starts.");
                    storeQuestion(question);
                    logger.debug("Store question thread ends successfully.");
                } catch (StorageFatalException e) {
                    logger.error("Store-question thread failed. This error will be propagated to the thread that has activated the current thread.",
                                    e);
                    exceptionReference.set(e);
                }
            }
        }, "QuestionStore-" + ((question.getId() == null) ? "" : question.getId()));
        questionStoreThread.start();
        try {
            // Here we are on "our" thread, not the thread that was created for the question storage.
            // Here we proceed with the processing of this question (sending it to all participants and collecting their
            // answers).
            operate_sendToParticipants(question);
        } finally {
            // Join the thread which stored the question.
            // If that thread had a fatal exception - throw it.
            try {
                logger.debug("Join question store thread...");
                questionStoreThread.join();
                logger.debug("Joined to question store thread.");
                StorageFatalException questionStroageException = exceptionReference.get();
                if (questionStroageException != null) {
                    throw questionStroageException;
                }
            } catch (InterruptedException e) {
                logger.error("Thread was interrupted. Stop immediately.", e);
                Thread.currentThread().interrupt();
                throw new QuestionOperationException("Thread was interrupted. Stop immediately.", e);
            }
        }
    }

    protected void storeQuestion(Question question) throws StorageFatalException {
        try {
            questionStorageOperator.storeQuestion(question);
        } catch (StorageNonFatalException e) // Note - fatal exception is propagated to the caller. Non-fatal is
                                             // swallowed here.
        {
            // TODO Improve this error handling
            logger.error("Failed to store a question in the question table. It is assumed that this error is not a fatal error, so the program continues running.",
                            e);
        }
    }

    protected abstract void operate_sendToParticipants(Question question)
                    throws QuestionOperationException, StorageFatalException;


    protected final QuestionStorageOperator questionStorageOperator;

    private static final org.apache.log4j.Logger logger =
                    org.apache.log4j.Logger.getLogger(QuestionOperatorWithQuestionStorage.class);
}
