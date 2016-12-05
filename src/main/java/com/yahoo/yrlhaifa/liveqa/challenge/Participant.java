// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge;

/**
 * A representation of a participant in the challenge. A participant is a system that answers the questions being asked
 * by the challenge system.
 * <P>
 * Each participant has an ID and a system ID. Thus, if a researcher or a team have several versions of their systems,
 * eahc should be registered as a separate {@link Participant}.
 * <P>
 * The team name / researcher name or organization name is provided as "organization id", while the system name is
 * "system id". Together, "organization name"+"system name" is unique. There must not be two {@link Participant} objects
 * with the same combination of "organization name"+"system name".
 *
 * Date: January, 2015
 * 
 * @author Asher Stern
 *
 */
public class Participant {
    public static final String UNIQUE_STRING_SEPARATOR = "-";

    public Participant(String participantOrganizationId, String participantSystemId, String participantServerUrl,
                    String participantEmail) {
        super();
        this.participantOrganizationId = participantOrganizationId;
        this.participantSystemId = participantSystemId;
        this.participantServerUrl = participantServerUrl;
        this.participantEmail = participantEmail;
    }



    public String getParticipantOrganizationId() {
        return participantOrganizationId;
    }

    public String getParticipantSystemId() {
        return participantSystemId;
    }

    public String getParticipantServerUrl() {
        return participantServerUrl;
    }

    public String getParticipantEmail() {
        return participantEmail;
    }

    /**
     * Returns a combination of the "participant name" + "system name".
     * 
     * @return a combination of the "participant name" + "system name".
     */
    public String getUniqueSystemId() {
        if (null == uniqueSystemId) {
            synchronized (this) {
                if (null == uniqueSystemId) {
                    uniqueSystemId = participantOrganizationId + UNIQUE_STRING_SEPARATOR + participantSystemId;
                }
            }
        }
        return uniqueSystemId;
    }



    // equals() and hashCode() are based on participant-organization ID and participatn-system ID


    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((participantOrganizationId == null) ? 0 : participantOrganizationId.hashCode());
        result = prime * result + ((participantSystemId == null) ? 0 : participantSystemId.hashCode());
        return result;
    }



    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Participant other = (Participant) obj;
        if (participantOrganizationId == null) {
            if (other.participantOrganizationId != null)
                return false;
        } else if (!participantOrganizationId.equals(other.participantOrganizationId))
            return false;
        if (participantSystemId == null) {
            if (other.participantSystemId != null)
                return false;
        } else if (!participantSystemId.equals(other.participantSystemId))
            return false;
        return true;
    }



    private final String participantOrganizationId;
    private final String participantSystemId;
    private final String participantServerUrl;
    private final String participantEmail;

    private transient volatile String uniqueSystemId = null; // I think I could give up the volatile declaration, since
                                                             // String is immutable with final fields. But to be on the
                                                             // safe side, let's keep it volatile.
}
