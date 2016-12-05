// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.haifa_utils.utils;


import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import org.apache.log4j.FileAppender;



/**
 * Like org.apache.log4j.FileAppender, but first it backups the existing log file. The backup name is the log file name
 * with post-fix indicates the current date and time, and optionally appended by a UUID.
 * 
 * @author Asher Stern
 * @since Feb 28, 2011
 *
 */
public class BackupOlderFileAppender extends FileAppender {
    public static final String BACKUP_PREFIX = "_Backed_up_at_";

    public BackupOlderFileAppender() {
        super();
    }

    @Override
    public synchronized void setFile(String fileName, boolean append, boolean bufferedIO, int bufferSize)
                    throws IOException {
        if (!alreadyCopiedSet.contains(fileName)) {
            File file = new File(fileName);
            if (file.exists()) {
                if (file.isFile()) {
                    Calendar c = Calendar.getInstance();
                    StringBuffer sb = new StringBuffer();
                    sb.append(BACKUP_PREFIX);
                    sb.append(c.get(Calendar.YEAR));
                    sb.append('_');
                    sb.append(1 + c.get(Calendar.MONTH)); // Months start from zero, need to add 1
                    sb.append('_');
                    sb.append(c.get(Calendar.DAY_OF_MONTH));
                    sb.append('_');
                    sb.append(c.get(Calendar.HOUR_OF_DAY));
                    sb.append('_');
                    sb.append(c.get(Calendar.MINUTE));
                    sb.append('_');
                    sb.append(c.get(Calendar.SECOND));

                    File backup = new File(fileName + sb.toString() + ".log");
                    if (backup.exists()) {
                        UUID uuid = UUID.randomUUID();
                        backup = new File(fileName + sb.toString() + "_" + uuid.toString() + ".log");
                    }
                    try {
                        FileUtils.copy(file, backup);
                    } catch (Exception e) {
                        StringBuffer sbException = new StringBuffer();


                        sbException.append("Could not copy to a backup file: ");
                        sbException.append(backup.getAbsolutePath());
                        sbException.append(" due to the following exception:\n");
                        // sbException.append(ExceptionUtilities.getMessages(e));
                        sbException.append(e.getStackTrace());
                        throw new IOException(sbException.toString());
                    }
                }
            }
            alreadyCopiedSet.add(fileName);
        }

        super.setFile(fileName, append, bufferedIO, bufferSize);
    }

    /**
     * The copy should be done only once in the process life-time.
     */


    private static final Set<String> alreadyCopiedSet = Collections.synchronizedSet(new HashSet<String>());
}
