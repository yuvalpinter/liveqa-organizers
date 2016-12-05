// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge;

import java.util.List;
import java.util.concurrent.ConcurrentMap;

import com.yahoo.yrlhaifa.liveqa.challenge.configuration.RequestGeneralParameters;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.ChallengeCloseException;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.ChallengeSystemException;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.QuestionOperationException;
import com.yahoo.yrlhaifa.liveqa.challenge.http_operation.QuestionOperationHttpRequestSender;
import com.yahoo.yrlhaifa.liveqa.challenge.rep.Question;
import com.yahoo.yrlhaifa.liveqa.challenge.storage.AnswerStorageOperator;
import com.yahoo.yrlhaifa.liveqa.challenge.storage.ParticipantResponse;
import com.yahoo.yrlhaifa.liveqa.challenge.storage.QuestionStorageOperator;

/**
 * Sends given questions to all the participants by sending HTTP requests to the participants (which are assumed to be
 * HTTP servers). See also super-classes documentation.
 * 
 * @see QuestionOperator
 *
 *      Date: Jan 14, 2015
 * @author Asher Stern
 *
 */
public class QuestionOperatorByHttpRequest extends QuestionOperatorUsingAnswerMap {

    /**
     * Constructor with list of participants, (permanent-)storage operator (which stores the questions and answers) and
     * some generic parameters regarding the maximum time for answering a question (a participant should answer within
     * that time) and some other parameters related to the answer and the HTTP request.
     * 
     * @param participants List of participants
     * @param storageOperator Stores the question and the answers in a permanent storage.
     * @param timeParameters Parameters regarding time restrictions of answers (how long can it take to answer the
     *        question), as well as other restrictions and regarding the HTTP request.
     * @throws ChallengeSystemException Thrown if a fatal error occurred, which indicates that the program should exit
     *         and the challenge should stop.
     */
    public QuestionOperatorByHttpRequest(List<Participant> participants,
                    QuestionStorageOperator questionStorageOperator, AnswerStorageOperator storageOperator,
                    RequestGeneralParameters timeParameters) throws ChallengeSystemException {
        super(participants, questionStorageOperator, storageOperator);
        this.timeParameters = timeParameters;
    }


    /*
     * (non-Javadoc)
     * 
     * @see
     * com.yahoo.yrlhaifa.liveqa.challenge.QuestionOperatorUsingAnswerMap#operateByFillingMap(com.yahoo.yrlhaifa.liveqa.
     * challenge.Question, java.util.concurrent.ConcurrentMap)
     */
    @Override
    protected void operateByFillingMap(Question question,
                    final ConcurrentMap<Participant, ParticipantResponse> mapParticipantToAnswer)
                                    throws QuestionOperationException, InterruptedException {
        QuestionOperationHttpRequestSender sender = new QuestionOperationHttpRequestSender(participants, question,
                        mapParticipantToAnswer, timeParameters);
        sender.sendRequestsAndCollectAnswers();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.yahoo.yrlhaifa.liveqa.challenge.ChallengeAutoCloseable#close()
     */
    @Override
    public void close() throws ChallengeCloseException {
        // TODO Auto-generated method stub

    }

    protected final RequestGeneralParameters timeParameters;
}
