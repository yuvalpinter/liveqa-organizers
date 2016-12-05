// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge;

import java.util.List;

import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.ChallengeSystemException;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.QuestionOperationException;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.StorageFatalException;
import com.yahoo.yrlhaifa.liveqa.challenge.rep.Question;


/**
 * Sends a question to the participants, and store their answer in a storage.
 *
 * Date: Jan 13, 2015
 * 
 * @author Asher Stern
 *
 */
public abstract class QuestionOperator implements ChallengeAutoCloseable {
    protected QuestionOperator(List<Participant> participants) throws ChallengeSystemException {
        super();
        this.participants = participants;

        ChallengeSystemUtilities.validateListParticipants(this.participants);
    }


    /**
     * Sends the question as a request to all participants, and stores their answers.
     * 
     * @param question A question to be processed (be sent to participants).
     * @throws QuestionOperationException in a system failure cases. Note that problems in participant side do not cause
     *         any exception to be thrown. The exception is thrown only if the failure is in the organizers-system's
     *         side.
     */
    public abstract void operate(Question question) throws QuestionOperationException, StorageFatalException;



    protected final List<Participant> participants;
}
