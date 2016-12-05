// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.storage;

/**
 * Technical information about the participant response. This information includes the start time of the the
 * request-respond operation, the end time of the request-respond operation, and the duration (which is end-start).
 *
 * Date: Jan 20, 2015
 * 
 * @author Asher Stern
 *
 */
public class ResponseOperationInformation {
    public ResponseOperationInformation(long startTimeInMilliseconds, long endTimeInMilliseconds,
                    long durationOfRequestResponseInMilliseconds) {
        super();
        this.startTimeInMilliseconds = startTimeInMilliseconds;
        this.endTimeInMilliseconds = endTimeInMilliseconds;
        this.durationOfRequestResponseInMilliseconds = durationOfRequestResponseInMilliseconds;
    }



    public long getStartTimeInMilliseconds() {
        return startTimeInMilliseconds;
    }

    public long getEndTimeInMilliseconds() {
        return endTimeInMilliseconds;
    }

    public long getDurationOfRequestResponseInMilliseconds() {
        return durationOfRequestResponseInMilliseconds;
    }



    private final long startTimeInMilliseconds;
    private final long endTimeInMilliseconds;
    private final long durationOfRequestResponseInMilliseconds;
}
