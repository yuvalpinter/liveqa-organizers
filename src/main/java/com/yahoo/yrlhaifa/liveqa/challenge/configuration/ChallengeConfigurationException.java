// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.configuration;

import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.ChallengeSystemException;

/**
 * Indicates an error to retrieve some configuration parameters. For example, a parameter is missing in a configuration
 * file.
 * 
 *
 * Date: Jan 21, 2015
 * 
 * @author Asher Stern
 *
 */
public class ChallengeConfigurationException extends ChallengeSystemException {
    private static final long serialVersionUID = -5237947365505830118L;

    public ChallengeConfigurationException(String message, Throwable cause) {
        super(message, cause);
    }

    public ChallengeConfigurationException(String message) {
        super(message);
    }
}
