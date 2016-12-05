// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.storage;

import com.yahoo.yrlhaifa.liveqa.challenge.ChallengeAutoCloseable;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.StorageFatalException;
import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.StorageNonFatalException;
import com.yahoo.yrlhaifa.liveqa.challenge.rep.Question;

/**
 * Stores a given {@link Question} in a storage.
 * <P>
 * <B>A note about concurrency:<B> implementations must be aware that the same instance of this interface can be used
 * concurrently by multiple threads. It is a good practice to implement the method {@link #storeQuestion(Question)} with
 * a synchronized declaration.
 * 
 * @see Storage
 *
 *      <P>
 *      Date: Jan 20, 2015
 * @author Asher Stern
 *
 */
public interface QuestionStorageOperator extends ChallengeAutoCloseable {
    public void storeQuestion(final Question question) throws StorageFatalException, StorageNonFatalException;
}
