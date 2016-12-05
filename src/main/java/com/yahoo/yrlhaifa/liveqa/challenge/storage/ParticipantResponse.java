// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.storage;

import com.yahoo.yrlhaifa.liveqa.challenge.rep.ParticipantAnswer;

/**
 * Holds the core data of the participant's response to a given question. This response is either
 * <UL>
 * <LI>the answer (what the participant answered),</LI>
 * <LI>or some human-readable string explaining why the participant did not answer (this is optional, and might be
 * empty),</LI>
 * <LI>or an exception described an error in parsing the participant's answer.</LI>
 * </UL>
 * 
 * <P>
 * Date: Jan 19, 2015
 * 
 * @author Asher Stern
 *
 */
public class ParticipantResponse {
    public ParticipantResponse(ParticipantAnswer answer) {
        this(answer, null, null);
    }

    public ParticipantResponse(ReasonWhyQuestionDiscardByParticipant whyNotAnswered) {
        this(null, whyNotAnswered, null);
    }

    public ParticipantResponse(Exception wrongResponseException) {
        this(null, null, wrongResponseException);
    }


    public void setResponseOperationInformation(ResponseOperationInformation responseOperationInformation) {
        this.responseOperationInformation = responseOperationInformation;
    }

    public ResponseOperationInformation getResponseOperationInformation() {
        return responseOperationInformation;
    }


    public ParticipantAnswer getAnswer() {
        return answer;
    }

    public ReasonWhyQuestionDiscardByParticipant getWhyNotAnswered() {
        return whyNotAnswered;
    }

    public Exception getWrongResponseException() {
        return wrongResponseException;
    }



    @Override
    public String toString() {
        return "ParticipantResponse [answer=" + answer + ", whyNotAnswered=" + whyNotAnswered
                        + ", wrongResponseException=" + wrongResponseException + "]";
    }

    protected ParticipantResponse(ParticipantAnswer answer, ReasonWhyQuestionDiscardByParticipant whyNotAnswered,
                    Exception wrongResponseException) {
        super();
        this.answer = answer;
        this.whyNotAnswered = whyNotAnswered;
        this.wrongResponseException = wrongResponseException;
    }


    private final ParticipantAnswer answer;
    private final ReasonWhyQuestionDiscardByParticipant whyNotAnswered;
    private final Exception wrongResponseException;

    private ResponseOperationInformation responseOperationInformation;
}
