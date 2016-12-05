// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.ChallengeSystemException;

/**
 * Common general utilities for the challenge system.
 *
 * Date: Jan 14, 2015
 * 
 * @author Asher Stern
 *
 */
public class ChallengeSystemUtilities {
    /**
     * Returns null is all OK.
     * 
     * @param paticipants
     * @return
     */
    public static void validateListParticipants(final List<Participant> paticipants) throws ChallengeSystemException {
        validateListParticipantsEqualsMethod(paticipants);
        validateListParticipantsUniqueIds(paticipants);
    }

    public static boolean httpStatusCodeOK(final int statusCode) {
        return ((statusCode / 100) == 2);
    }


    ////////////// PRIVATE & PROTECTED ///////////////


    private static void validateListParticipantsEqualsMethod(final List<Participant> paticipants)
                    throws ChallengeSystemException {
        Set<Participant> set = new LinkedHashSet<Participant>();
        for (Participant participant : paticipants) {
            set.add(participant);
        }
        if (set.size() != paticipants.size()) {
            throw new ChallengeSystemException(
                            "In the given participant list, at least two participants are equal, according to their equals() method.");
        }
    }

    private static void validateListParticipantsUniqueIds(final List<Participant> paticipants)
                    throws ChallengeSystemException {
        Set<String> participantNames = new LinkedHashSet<String>();
        for (Participant participant : paticipants) {
            final String uniqueId = participant.getUniqueSystemId();
            if (null == uniqueId) {
                throw new ChallengeSystemException("null unique id");
            }
            if (uniqueId.length() == 0) {
                throw new ChallengeSystemException("zero length id");
            }
            participantNames.add(uniqueId);
        }
        if (participantNames.size() != paticipants.size()) {
            throw new ChallengeSystemException("Number of unique names = " + participantNames.size()
                            + ", while number of participants = " + paticipants.size());
        }

        Set<String> secondTime = new LinkedHashSet<String>();
        for (Participant participant : paticipants) {
            secondTime.add(participant.getUniqueSystemId());
        }
        if (!(secondTime.equals(participantNames))) {
            throw new ChallengeSystemException("Getting participant unique IDs twice yields different names");
        }

    }

}
