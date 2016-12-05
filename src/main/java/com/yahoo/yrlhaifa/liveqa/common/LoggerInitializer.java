// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.liveqa.common;

import java.io.File;
import java.io.PrintWriter;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.PropertyConfigurator;

import com.yahoo.yrlhaifa.haifa_utils.utils.BackupOlderFileAppender;

/**
 * Initializes log4j. The logger is initialized by the parameters specified in "log4j.properties" file in the working
 * directory. However, if that file does not exist, the {@link LoggerInitializer} creates a default one (that file will
 * not be deleted, so it will be used the next time the system runs).
 *
 * Date: Jan 14, 2015
 * 
 * @author Asher Stern
 *
 */
public class LoggerInitializer {
    public static final String DEFAULT_PROPERTIES_FILE_CONTENTS = "" +
                    "log4j.rootLogger=INFO, stdout, logfile\n"
                    + "log4j.appender.stdout=org.apache.log4j.ConsoleAppender\n"
                    + "log4j.appender.stdout.layout=org.apache.log4j.PatternLayout\n"
                    + "log4j.appender.stdout.layout.ConversionPattern = %-5p %d{HH:mm:ss} [%t]: %m%n\n" + "\n"
                    + "log4j.appender.logfile=" + BackupOlderFileAppender.class.getCanonicalName() + "\n"
                    + "log4j.appender.logfile.append=false\n"
                    + "log4j.appender.logfile.layout = org.apache.log4j.PatternLayout\n"
                    + "log4j.appender.logfile.layout.ConversionPattern = %-5p %d{HH:mm:ss} [%t]: %m%n\n"
                    + "log4j.appender.logfile.File = logfile.log\n";



    public static final String LOG4J_PROPERTIES_FILE_NAME = "log4j.properties";

    public static void init() // throws no exception, not even RuntimeException
    {
        try {
            File propertiesFile = new File(LOG4J_PROPERTIES_FILE_NAME);
            if (!propertiesFile.exists()) {
                try (PrintWriter writer = new PrintWriter(propertiesFile)) {
                    writer.println(DEFAULT_PROPERTIES_FILE_CONTENTS);
                }
            }
            PropertyConfigurator.configure(LOG4J_PROPERTIES_FILE_NAME);
        } catch (Exception e) {
            try {
                BasicConfigurator.configure();
                logger.error("Log4j Initialization failed. Using BasicConfigurator instead.");
            } catch (Throwable t) {
            } // swallow
        }
    }

    private static final org.apache.log4j.Logger logger = org.apache.log4j.Logger.getLogger(LoggerInitializer.class);

}
