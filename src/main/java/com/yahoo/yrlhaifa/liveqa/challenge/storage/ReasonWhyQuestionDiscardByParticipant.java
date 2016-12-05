// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.storage;

/**
 * Holds the reason, reported by a participant, as for why the participant decided not to answer a given question. This
 * reason is an arbitrary string, and is expected to be a human readable explanation. This reported reason is not used
 * in evaluations. It merely might help researchers who are interested in that kind of data (who will look at the data
 * in a retrospect manner).
 *
 * Date: Jan 20, 2015
 * 
 * @author Asher Stern
 *
 */
public class ReasonWhyQuestionDiscardByParticipant {
    public ReasonWhyQuestionDiscardByParticipant(String reason) {
        super();
        this.reason = reason;
    }


    public String getReason() {
        return reason;
    }



    @Override
    public String toString() {
        return "ReasonWhyQuestionDiscardByParticipant [reason=" + reason + "]";
    }



    private final String reason;
}
