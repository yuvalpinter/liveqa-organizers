// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.storage;

import java.util.List;
import java.util.Map;

import com.yahoo.yrlhaifa.liveqa.challenge.Participant;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.ChallengeCloseException;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.StorageFatalException;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.StorageNonFatalException;
import com.yahoo.yrlhaifa.liveqa.challenge.rep.Question;


/**
 * Invokes several {@link AnswerStorageOperator}s to store the answers.
 *
 * <P>
 * Date: Feb 16, 2015
 * 
 * @author Asher Stern
 *
 */
public class AggregatedAnswerStorageOperator implements AnswerStorageOperator {
    public AggregatedAnswerStorageOperator(List<AnswerStorageOperator> operators) {
        super();
        this.operators = operators;
    }

    @Override
    public void storeAnswers(Question question, Map<Participant, ParticipantResponse> mapParticipantToResponse)
                    throws StorageFatalException, StorageNonFatalException {
        for (AnswerStorageOperator storage : operators) {
            storage.storeAnswers(question, mapParticipantToResponse);
        }
    }

    @Override
    public void close() throws ChallengeCloseException {
        for (AnswerStorageOperator storage : operators) {
            storage.close();
        }
    }

    private List<AnswerStorageOperator> operators;

}
