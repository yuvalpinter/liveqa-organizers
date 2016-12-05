// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge;

import java.util.List;

import com.yahoo.yrlhaifa.liveqa.challenge.configuration.RequestGeneralParameters;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.ChallengeSystemException;
import com.yahoo.yrlhaifa.liveqa.challenge.storage.AnswerStorageOperator;
import com.yahoo.yrlhaifa.liveqa.challenge.storage.QuestionStorageOperator;

/**
 * Creates a question operator, which sends the questions to the participants and stores their answers by the given
 * storage-operators.
 *
 * Date: Jan 14, 2015
 * 
 * @author Asher Stern
 *
 */
public class QuestionOperatorFactory {
    public QuestionOperatorFactory(RequestGeneralParameters requestGeneralParameters) {
        super();
        this.requestGeneralParameters = requestGeneralParameters;
    }

    public QuestionOperator createQuestionOperator(final List<Participant> participants,
                    final QuestionStorageOperator questionStorageOperator,
                    final AnswerStorageOperator answerStorageOperator) throws ChallengeSystemException {
        return new QuestionOperatorByHttpRequest(participants, questionStorageOperator, answerStorageOperator,
                        requestGeneralParameters);
    }

    private final RequestGeneralParameters requestGeneralParameters;
}
