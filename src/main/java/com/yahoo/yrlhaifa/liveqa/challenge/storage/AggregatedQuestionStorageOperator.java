// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.storage;

import java.util.List;

import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.ChallengeCloseException;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.StorageFatalException;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.StorageNonFatalException;
import com.yahoo.yrlhaifa.liveqa.challenge.rep.Question;

/**
 * Invokes several {@link QuestionStorageOperator}s to store the question.
 *
 * <P>
 * Date: Feb 16, 2015
 * 
 * @author Asher Stern
 *
 */
public class AggregatedQuestionStorageOperator implements QuestionStorageOperator {
    public AggregatedQuestionStorageOperator(List<QuestionStorageOperator> operators) {
        super();
        this.operators = operators;
    }

    @Override
    public void storeQuestion(Question question) throws StorageFatalException, StorageNonFatalException {
        for (QuestionStorageOperator storage : operators) {
            storage.storeQuestion(question);
        }
    }

    @Override
    public void close() throws ChallengeCloseException {
        for (QuestionStorageOperator storage : operators) {
            storage.close();
        }
    }

    private List<QuestionStorageOperator> operators;
}
