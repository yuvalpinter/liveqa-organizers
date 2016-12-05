// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.storage;

import java.util.Map;

import com.yahoo.yrlhaifa.liveqa.challenge.ChallengeAutoCloseable;
import com.yahoo.yrlhaifa.liveqa.challenge.Participant;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.StorageFatalException;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.StorageNonFatalException;
import com.yahoo.yrlhaifa.liveqa.challenge.rep.Question;

/**
 * Stores a question and the answers provided by participant for the given question. This interface is the main
 * interface for handling the storage of questions and answers.
 * <P>
 * <B>A note about concurrency:<B> implementations must be aware that the same instance of this interface can be used
 * concurrently by multiple threads. It is a good practice to implement the method {@link #storeAnswers(Question, Map)}
 * with a synchronized declaration.
 *
 * Date: January, 2015
 * 
 * @author Asher Stern
 *
 */
public interface AnswerStorageOperator extends ChallengeAutoCloseable {
    /**
     * Store the given question and the participants' answers.
     * 
     * @param question A question that was sent to participants.
     * @param mapSystemIdToResponse The participants' answers to the given question. The map is a mapping from system-id
     *        to the answer which was answered by that system. Note that system-id is a combination of the
     *        participant-id and its system-name, and is unique.
     * @throws StorageFatalException Storage failed, and the failure indicates an inherent problem that makes the whole
     *         challenge corrupted. Program must exit.
     * @throws StorageNonFatalException Storage failed, but this is a local failure, which should be logged, but should
     *         not end the challenge.
     */
    public void storeAnswers(Question question, Map<Participant, ParticipantResponse> mapParticipantToResponse)
                    throws StorageFatalException, StorageNonFatalException;

}
