// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.configuration;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.yahoo.yrlhaifa.liveqa.challenge.Participant;


/**
 * Reads a text file that contains the list of participants. This text file is a tab-separated file, where each line
 * contains the following entries: participant-organization-id, participant-system-id, participant-server-url
 * participant-email. <BR>
 * Note that the server-url is the URL into which the HTTP requests, sent by the system, will be delivered.
 *
 * Date: Jan 21, 2015
 * 
 * @author Asher Stern
 *
 */
public class ParticipantFileReader {
    public ParticipantFileReader(String participantFileName) {
        super();
        this.participantFileName = participantFileName;
    }

    public List<Participant> read() throws ChallengeConfigurationException {
        try (BufferedReader reader = new BufferedReader(new FileReader(participantFileName))) {
            ArrayList<Participant> participants = new ArrayList<Participant>();
            String line;
            while ((line = reader.readLine()) != null) {
                Participant participant = parseLine(line);
                if (participant != null) {
                    participants.add(participant);
                }
            }

            participants.trimToSize();
            return participants;
        } catch (IOException e) {
            throw new ChallengeConfigurationException("Failed to read participant file " + participantFileName, e);
        }
    }

    private Participant parseLine(String line) throws ChallengeConfigurationException {
        Participant ret = null;
        line = line.trim();
        if (line.length() > 0) {
            String[] components = line.split("\\t");
            int index = 0;

            if (!(index < components.length))
                throw new ChallengeConfigurationException(
                                "Cannot parse participant line from the participant file. line=" + line);
            String participantOrganizationId = components[index].trim();
            ++index;

            if (!(index < components.length))
                throw new ChallengeConfigurationException(
                                "Cannot parse participant line from the participant file. line=" + line);
            String participantSystemId = components[index].trim();
            ++index;

            if (!(index < components.length))
                throw new ChallengeConfigurationException(
                                "Cannot parse participant line from the participant file. line=" + line);
            String participantServerUrl = components[index].trim();
            ++index;

            if (!(index < components.length))
                throw new ChallengeConfigurationException(
                                "Cannot parse participant line from the participant file. line=" + line);
            String participantEmail = components[index].trim();
            ++index;

            ret = new Participant(participantOrganizationId, participantSystemId, participantServerUrl,
                            participantEmail);
        }
        return ret;
    }

    private final String participantFileName;
}
