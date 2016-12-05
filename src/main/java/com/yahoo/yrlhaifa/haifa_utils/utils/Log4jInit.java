// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.haifa_utils.utils;

import java.io.File;
import java.io.IOException;
import java.util.Enumeration;

import org.apache.log4j.Appender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Layout;
import org.apache.log4j.Level;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.PropertyConfigurator;


/**
 * Initializes Log4j with either the configuration specified in log4j.properties (if that file exists), or with a
 * default configuration (if the log4j.properties file does not exist).
 * 
 * <P>
 * Date: Dec 22, 2014
 * 
 * @author Asher Stern
 *
 */
public class Log4jInit {

    public static final String LOG4J_PROPERTIES_FILE = "log4j.properties";

    /**
     * Initializes Log4j with a default configuration of printing to console all messages of level INFO and above, using
     * a default layout. If the file log4j.properties exists in the working-directory, it is used.
     * 
     * @throws IOException
     */
    public static void init() throws IOException {
        init(Level.INFO);
    }

    /**
     * Initializes Log4j with a default configuration of printing to console all messages of the specified level and
     * above, using a default layout. If the file log4j.properties exists in the working-directory, it is used.
     * 
     * @param level level of log records that will be printed. lower-level records will be ignored.
     * @throws IOException
     */
    public static void init(Level level) throws IOException {
        init(level, null);
    }

    /**
     * Initializes Log4j with a default configuration of printing to console, as well as to the specified file, all
     * messages of the specified level and above, using a default layout. If the file log4j.properties exists in the
     * working-directory, it is used.
     * 
     * @param level level of log records that will be printed. lower-level records will be ignored.
     * @param alsoFile a file into log messages will be logged, in addition to being printed to the console.
     * @throws IOException
     */
    public static void init(Level level, String alsoFile) throws IOException {
        if (!alreadyInitialized) {
            synchronized (Log4jInit.class) {
                if (!alreadyInitialized) { // double-check should be OK, since alreadyInitialized is declared as
                                           // volatile
                    LogManager.resetConfiguration();
                    File log4jPropertiesFile = new File(LOG4J_PROPERTIES_FILE);
                    if ((log4jPropertiesFile.exists()) && (log4jPropertiesFile.isFile())) {
                        PropertyConfigurator.configure(log4jPropertiesFile.getPath());
                    } else {
                        BasicConfigurator.configure();
                        if (alsoFile != null) {
                            Logger.getRootLogger().addAppender(new FileAppender(layout, alsoFile, false));
                        }
                        Enumeration<?> enumAppenders = Logger.getRootLogger().getAllAppenders();
                        while (enumAppenders.hasMoreElements()) {
                            Appender appender = (Appender) enumAppenders.nextElement();
                            appender.setLayout(layout);
                        }

                        Logger.getRootLogger().setLevel(level);
                    }
                    alreadyInitialized = true;
                }
            }
        }

    }

    private static volatile boolean alreadyInitialized = false;

    private static final Layout layout = new PatternLayout("%-5.5p %d{HH:mm} %c: %m%n");
}
