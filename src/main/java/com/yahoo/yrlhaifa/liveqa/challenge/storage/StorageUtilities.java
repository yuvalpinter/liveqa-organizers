// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.challenge.storage;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import com.yahoo.yrlhaifa.liveqa.challenge.exceptions.StorageFatalException;

/**
 * General utilities for storage.
 *
 * Date: Jan 21, 2015
 * 
 * @author Asher Stern
 *
 */
public class StorageUtilities {
    public static void validateTableEmpty(final Statement statement, final String tableName)
                    throws StorageFatalException, SQLException {
        ResultSet resultSet = statement.executeQuery("SELECT * FROM " + tableName);
        if (resultSet.next()) {
            throw new StorageFatalException(tableName + " table is not empty.");
        }
    }



}
