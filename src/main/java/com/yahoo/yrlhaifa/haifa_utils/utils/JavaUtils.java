// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.haifa_utils.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintStream;

/**
 * Contains a utility class that enable to run system calls using stream gobller class that transfer input to output
 * streams
 * 
 * @author nadavg
 * 
 */
public class JavaUtils {

    public static final long MEGA = 1048576;

    private static class StreamGobbler extends Thread {

        private final InputStream in;
        private final PrintStream out;



        public StreamGobbler(InputStream iIn, PrintStream ioOut) {
            this.in = iIn;
            this.out = ioOut;
        }



        @Override
        public void run() {
            try {
                BufferedReader br = new BufferedReader(new InputStreamReader(this.in));
                String line;

                while ((line = br.readLine()) != null)
                    this.out.println(line);
            } catch (IOException ioe) {
                // do nothing
            }

            this.out.close();
        }

    }



    // -----------------------------------------------------------------------

    /**
     * Returns the current class
     * 
     * @return the current class
     */
    public static Class<?> getCurrentClass() {
        Exception e = new Exception();
        StackTraceElement[] sTrace = e.getStackTrace();
        return sTrace[1].getClass();
    }



    /**
	 */
    public static Class<?> getCallingClass() throws ClassNotFoundException {
        Exception e = new Exception();
        String utilsClass = e.getStackTrace()[0].getClassName();
        String currentClass = null;

        for (StackTraceElement f : e.getStackTrace()) {
            if (currentClass == null) {
                if (!f.getClassName().equals(utilsClass))
                    currentClass = f.getClassName();
            } else if (!f.getClassName().equals(currentClass))
                return Class.forName(f.getClassName());
        }

        return null;
    }



    /**
     * managing an executed process , reading the err and out streams of the process so it can be terminated
     * successfully even if these streams are written into
     */
    public static void manageExecution(Process process, PrintStream ioExecOutStream, PrintStream ioExecErrSteam,
                    boolean waitForTermination) throws IOException, InterruptedException {
        StreamGobbler outGobbler = null, errGobbler = null;

        if (ioExecOutStream != null) {
            outGobbler = new StreamGobbler(process.getInputStream(), ioExecOutStream);
            outGobbler.start();
        }
        if (ioExecErrSteam != null) {
            errGobbler = new StreamGobbler(process.getErrorStream(), ioExecErrSteam);
            errGobbler.start();
        }

        if (waitForTermination) {
            process.waitFor();
            if (outGobbler != null)
                outGobbler.join();
            if (errGobbler != null)
                errGobbler.join();
        }
    }



    /**
     * Execute a command line and keeping the err and out streams of the subprocess read so it can be terminated
     * successfully even if these streams are written to
     * 
     * @param cmd - the command to execute
     * @param ioExecOutStream - where to write the stdout of the subprocess to
     * @param ioExecErrSteam - where to write the stderr of the subprocess to
     * @param waitForTermination - should the call wait till the subprocess terminates
     * @return the Process object pointing at the subprocess
     * @throws IOException exception
     * @throws InterruptedException exception
     */
    public static Process exec(String cmd, PrintStream ioExecOutStream, PrintStream ioExecErrSteam,
                    boolean waitForTermination) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(cmd);
        manageExecution(process, ioExecOutStream, ioExecErrSteam, waitForTermination);
        return process;
    }



    public static Process exec(String[] args, PrintStream ioExecOutStream, PrintStream ioExecErrSteam,
                    boolean waitForTermination) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(args);
        manageExecution(process, ioExecOutStream, ioExecErrSteam, waitForTermination);
        return process;
    }



    public static String escapeForPatternReplacement(String iStr) {
        return iStr.replace("\\", "\\\\").replace("$", "\\$");
    }



    /**
     * Returns the jar file-name which contains that class, according to what that is visible to the JVM. Note that if
     * the class is not from a jar file (but exists in the file-system as a .class file), a runtime exception might be
     * thrown.
     * 
     * @param clazz A class
     * @return The name of jar containing that class.
     */
    public static String extractJarFromClassPath(Class<?> clazz) {
        return clazz.getProtectionDomain().getCodeSource().getLocation().getPath();
    }



    /**
     * Returns the amount of memory currently used by the JVM, in megabytes.
     */
    public static long memoryUsedInMB() {
        return (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / MEGA;
    }

    /**
     * Returns the amount of memory currently used by the JVM, in megabytes, as a string.
     */
    public static String stringMemoryUsedInMB() {
        String ret = String.format("%,d MB", memoryUsedInMB());
        return ret;
    }

}
