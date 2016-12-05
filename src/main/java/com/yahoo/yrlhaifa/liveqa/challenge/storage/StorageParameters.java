// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.storage;

/**
 * General parameters for storage. The storage is used for storing the questions and the answers (answers returned from
 * participants). Two actual storages are implemented: One is based on an SQL database, and the other one stores into
 * text files.
 * 
 * @see Storage
 *
 *      Date: Jan 20, 2015
 * @author Asher Stern
 *
 */
public class StorageParameters {
    public StorageParameters(String connectionString, boolean incrementalDatabaseAllowed, String questionStorageFile,
                    String answerStorageFile) {
        super();
        this.connectionString = connectionString;
        this.incrementalDatabaseAllowed = incrementalDatabaseAllowed;
        this.questionStorageFile = questionStorageFile;
        this.answerStorageFile = answerStorageFile;
    }

    public String getConnectionString() {
        return connectionString;
    }

    public boolean isIncrementalDatabaseAllowed() {
        return incrementalDatabaseAllowed;
    }

    public String getQuestionStorageFile() {
        return questionStorageFile;
    }

    public String getAnswerStorageFile() {
        return answerStorageFile;
    }


    private final String connectionString;
    private final boolean incrementalDatabaseAllowed;
    private final String questionStorageFile;
    private final String answerStorageFile;
}
