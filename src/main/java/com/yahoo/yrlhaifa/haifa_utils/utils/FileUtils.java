// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.haifa_utils.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.EOFException;
import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.Reader;
import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ThreadLocalRandom;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import org.apache.commons.compress.compressors.bzip2.BZip2CompressorInputStream;
import org.apache.commons.compress.compressors.bzip2.BZip2CompressorOutputStream;

/**
 * Holds file based utils that works both on locally and on the haddop FS.
 * 
 * @author nadavg
 * 
 */
public class FileUtils {
    /**
     * 
     * An iterable over lines that can be converted into objects.
     * 
     * @author idan
     * 
     */
    public static class LineSerializedObjectIterable implements Iterable<Object> {
        private final Constructor<?> ctor;
        private final Iterable<String> it;
        private final PrintStream err;



        /**
         * ctor for the iterable, based on an iterable over lines and a class of which objects will be generated from
         * the lines
         * 
         * @param ioIt - the line iterable
         * @param classInfo - the class of the objects to be generated, which contains a specific ctor of the form
         *        ctor(String)
         * @throws NoSuchMethodException
         * @throws SecurityException
         */
        public LineSerializedObjectIterable(Iterable<String> ioIt, Class<?> classInfo) throws NoSuchMethodException,
                        SecurityException {
            this(ioIt, classInfo, System.err);
        }



        public LineSerializedObjectIterable(Iterable<String> ioIt, Class<?> classInfo, PrintStream err)
                        throws NoSuchMethodException, SecurityException {
            ctor = classInfo.getConstructor(String.class);
            it = ioIt;
            this.err = err;
        }



        @Override
        public Iterator<Object> iterator() {
            return new LineSerializedObjectIterator(it.iterator(), ctor, err);
        }
    }



    /**
     * The iterator for a single pass over the {@link LineSerializedObjectIterable} elements
     * 
     * @author idan
     * 
     */
    private static class LineSerializedObjectIterator implements Iterator<Object> {
        private final Constructor<?> ctor;
        private final Iterator<String> it;
        PrintStream err;
        private boolean failed = false;
        private String next = null;



        public LineSerializedObjectIterator(Iterator<String> it, Constructor<?> ctor, PrintStream err) {
            this.ctor = ctor;
            this.it = it;
            this.err = err;
        }



        @Override
        public boolean hasNext() {
            if (failed)
                return false;

            if (it.hasNext()) {
                next = it.next();
                if (next == null || next == IOEXCEPTION_MARK)
                    failed = true;
            } else
                failed = true;

            return !failed;
        }



        @Override
        public Object next() {
            if (failed || next == null)
                return null;

            String curr = next;
            next = null;

            try {
                return ctor.newInstance(curr);
            } catch (Exception e) {
                e.printStackTrace(err);
            }

            return null;
        }



        @Override
        public void remove() {}
    }

    //--------------------------------------------------------------
    


    /**
     * An iterable over readers, generated either from a list of Files or a list of Paths
     * 
     * @author idan
     * 
     */
    private static abstract class ReaderIterable<T> implements Iterable<T> {
        private final String charset;
        private File[] files = null;



        public ReaderIterable(File[] iFiles, String iCharset) {
            this.files = iFiles;
            this.charset = iCharset;
        }
    }



    /**
     * The iterator for a single pass over the {@link ReaderIterable} elements
     * 
     * @author idan
     * 
     */
    private abstract static class ReaderIterator<T> implements Iterator<T> {
        private final ReaderIterable<T> readers;
        private int next = 0;



        public ReaderIterator(ReaderIterable<T> it) {
            this.readers = it;
        }



        @Override
        public boolean hasNext() {
            if (readers.files != null && next >= readers.files.length)
                return false;

            return true;
        }



        @Override
        public T next() {
            if (!hasNext())
                return null;

            next++;
            try {
                if (readers.files != null)
                    return open(readers.files[this.next - 1], readers.charset);
            } catch (IOException e) {
                next = Integer.MAX_VALUE - 1;
            }

            return null;
        }



        protected abstract T open(File file, String charset) throws IOException;



        @Override
        public void remove() {
            // do nothing
        }

    }



    // -----------------------------------------------------------------------


    private static class BufferedReaderIterable extends ReaderIterable<BufferedReader> {
        public BufferedReaderIterable(File[] iFiles, String iCharset) {
            super(iFiles, iCharset);
        }



        @Override
        public Iterator<BufferedReader> iterator() {
            return new BufferedReaderIterator(this);
        }

    }



    private static class BufferedReaderIterator extends ReaderIterator<BufferedReader> {
        public BufferedReaderIterator(BufferedReaderIterable it) {
            super(it);
        }



        @Override
        protected BufferedReader open(File file, String charset) throws IOException {
            return openReader(file, charset);
        }
    }



    // -----------------------------------------------------------------------


    private static class ObjectInputStreamIterable extends ReaderIterable<ObjectInputStream> {
        public ObjectInputStreamIterable(File[] iFiles, String iCharset) {
            super(iFiles, iCharset);
        }



        @Override
        public Iterator<ObjectInputStream> iterator() {
            return new ObjectInputStreamIterator(this);
        }

    }



    private static class ObjectInputStreamIterator extends ReaderIterator<ObjectInputStream> {
        public ObjectInputStreamIterator(ObjectInputStreamIterable it) {
            super(it);
        }



        @Override
        protected ObjectInputStream open(File file, String charset) throws IOException {
            return openObjectReader(file);
        }
    }



    // -----------------------------------------------------------------------


    protected static class ObjectIterable implements Iterable<Object> {
        private final Iterable<ObjectInputStream> it;



        public ObjectIterable(Iterable<ObjectInputStream> iIt) {
            it = iIt;
        }



        @Override
        public Iterator<Object> iterator() {
            return new ObjectIterator(it.iterator());
        }

    }



    private static class ObjectIterator implements Iterator<Object> {
        private final Iterator<ObjectInputStream> it;
        private Object next;
        private boolean failed;
        private ObjectInputStream objInputStream;



        public ObjectIterator(Iterator<ObjectInputStream> iIt) {
            it = iIt;
            this.next = null;
            this.failed = false;

            if (it.hasNext()) {
                this.objInputStream = it.next();
            } else {
                this.failed = true;
            }
        }



        private Object getNextObject() throws ClassNotFoundException {
            Object res;

            if (failed)
                return null;

            try {
                res = objInputStream.readObject();
                return res;
            } catch (IOException e) {
                // May be end of file, try next file
                nextFile();
                return getNextObject();
            }
        }



        private void nextFile() {
            try {
                objInputStream.close();
            } catch (IOException e1) {
                // Nothing to do
            }

            if (it.hasNext()) {
                objInputStream = it.next();
            } else {
                failed = true;
            }
        }



        @Override
        public boolean hasNext() {
            if (next != null)
                return true;

            if (failed)
                return false;

            try {
                next = getNextObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return (next != null);
        }



        @Override
        public Object next() {
            Object res = next;

            next = null;
            if (res != null)
                return res;

            if (failed)
                return null;

            try {
                res = getNextObject();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return res;
        }



        @Override
        public void remove() {
            // do nothing
        }
    }



    // ----------------------------------------------------


    /**
     * An iterable over lines, taken from input readers
     * 
     * @author idan
     * 
     */
    protected static class LineIterable implements Iterable<String> {
        private final Iterable<BufferedReader> readers;



        public LineIterable(Iterable<BufferedReader> ioReaders) {
            readers = ioReaders;
        }



        @Override
        public Iterator<String> iterator() {
            return new LineIterator(readers.iterator());
        }
    }



    /**
     * The iterator for a single pass over the {@link LineIterable} elements
     * 
     * @author idan
     * 
     */
    protected static class LineIterator implements Iterator<String> {
        private final Iterator<BufferedReader> readers;
        private BufferedReader reader;
        private String next;
        private boolean failed;



        public LineIterator(Iterator<BufferedReader> ioReaders) {
            this.readers = ioReaders;
            this.next = null;
            this.reader = null;
            this.failed = false;

            if (this.readers.hasNext()) {
                this.reader = this.readers.next();
            } else {
                failed = true;
            }
        }



        private String getNextLine() {
            String res;

            if (failed)
                return null;

            try {
                if (reader == null) {
                    res = IOEXCEPTION_MARK;
                    failed = true;
                    return res;
                }
                res = this.reader.readLine();
                if (res == null) {
                    this.reader.close();
                    while (this.readers.hasNext()) {
                        this.reader = this.readers.next();
                        res = this.reader.readLine();
                        if (res != null) {
                            break;
                        } else {
                            this.reader.close();
                        }
                    }
                }
            } catch (IOException e) {
                res = IOEXCEPTION_MARK;
                failed = true;
            }


            if (res == null)
                failed = true;

            return res;
        }



        @Override
        public boolean hasNext() {
            if (next != null)
                return true;

            if (failed)
                return false;

            next = getNextLine();
            return (next != null);
        }



        @Override
        public String next() {
            String res = next;

            next = null;
            if (res != null)
                return res;

            if (failed)
                return null;

            res = getNextLine();
            return res;
        }



        @Override
        public void remove() {
            // do nothing
        }
    }



    // ----------------------------------------------------------------------


    public static class TeePrintStream extends PrintStream {
        private final List<PrintStream> streams;



        public TeePrintStream(List<PrintStream> streams) throws FileNotFoundException {
            super(new DevNull());

            this.streams = streams;
        }



        public TeePrintStream(PrintStream[] streams) throws FileNotFoundException {
            super(new DevNull());

            this.streams = Arrays.asList(streams);
        }



        public List<PrintStream> getStreams() {
            return streams;
        }



        @Override
        public PrintStream append(char c) {
            for (PrintStream s : streams)
                s.append(c);
            return this;
        }



        @Override
        public PrintStream append(CharSequence csq) {
            for (PrintStream s : streams)
                s.append(csq);
            return this;
        }



        @Override
        public PrintStream append(CharSequence csq, int start, int end) {
            for (PrintStream s : streams)
                s.append(csq, start, end);
            return this;
        }



        @Override
        public void flush() {
            for (PrintStream s : streams)
                s.flush();
        }



        @Override
        public PrintStream format(Locale l, String format, Object... args) {
            for (PrintStream s : streams)
                s.format(l, format, args);
            return this;
        }



        @Override
        public PrintStream format(String format, Object... args) {
            for (PrintStream s : streams)
                s.format(format, args);
            return this;
        }



        @Override
        public void print(boolean b) {
            for (PrintStream s : streams)
                s.print(b);
        }



        @Override
        public void print(char c) {
            for (PrintStream s : streams)
                s.print(c);
        }



        @Override
        public void print(char[] s) {
            for (PrintStream o : streams)
                o.print(s);
        }



        @Override
        public void print(double d) {
            for (PrintStream s : streams)
                s.print(d);
        }



        @Override
        public void print(float f) {
            for (PrintStream s : streams)
                s.print(f);
        }



        @Override
        public void print(int i) {
            for (PrintStream s : streams)
                s.print(i);
        }



        @Override
        public void print(long l) {
            for (PrintStream s : streams)
                s.print(l);
        }



        @Override
        public void print(Object obj) {
            for (PrintStream s : streams)
                s.print(obj);
        }



        @Override
        public void print(String s) {
            for (PrintStream o : streams)
                o.print(s);
        }



        @Override
        public PrintStream printf(Locale l, String format, Object... args) {
            for (PrintStream s : streams)
                s.printf(l, format, args);
            return this;
        }



        @Override
        public PrintStream printf(String format, Object... args) {
            for (PrintStream s : streams)
                s.printf(format, args);
            return this;
        }



        @Override
        public void println() {
            for (PrintStream s : streams)
                s.println();
        }



        @Override
        public void println(boolean x) {
            for (PrintStream s : streams)
                s.println(x);
        }



        @Override
        public void println(char x) {
            for (PrintStream s : streams)
                s.println(x);
        }



        @Override
        public void println(char[] x) {
            for (PrintStream s : streams)
                s.println(x);
        }



        @Override
        public void println(double x) {
            for (PrintStream s : streams)
                s.println(x);
        }



        @Override
        public void println(float x) {
            for (PrintStream s : streams)
                s.println(x);
        }



        @Override
        public void println(int x) {
            for (PrintStream s : streams)
                s.println(x);
        }



        @Override
        public void println(long x) {
            for (PrintStream s : streams)
                s.println(x);
        }



        @Override
        public void println(Object x) {
            for (PrintStream s : streams)
                s.println(x);
        }



        @Override
        public void println(String x) {
            for (PrintStream s : streams)
                s.println(x);
        }



        @Override
        public void write(byte[] b) throws IOException {
            for (PrintStream s : streams)
                s.write(b);
        }



        @Override
        public void write(byte[] buf, int off, int len) {
            for (PrintStream s : streams)
                s.write(buf, off, len);
        }



        @Override
        public void write(int b) {
            for (PrintStream s : streams)
                s.write(b);
        }
    }



    // ----------------------------------------------------------------------


    public static File[] toFileList(String iList) {
        return toFileList(iList, null);
    }



    /**
     * 
     * @param filesIn files - file list separated by ';'
     * @return the next line, or the string constant FileUtils.IOEXCEPTION_MARK if an io exception was caught in the
     *         middle of iteration
     * @throws IOException exception
     */
    public static File[] toFileList(String iList, FileFilter filter) {
        List<File> files = new ArrayList<File>();

        // add each file, but if a file is a directory, add its children files instead, without recursion (that is, do
        // not add grand-children of sub directories)
        for (String file : iList.split("[;,]")) {
            File fp = new File(file);
            addToFileList(fp, files, filter);
        }

        return files.toArray(new File[0]);
    }



    /**
     * 
     * @param filesIn files - file list
     * @return the next line, or the string constant FileUtils.IOEXCEPTION_MARK if an io exception was caught in the
     *         middle of iteration
     * @throws IOException exception
     */
    public static File[] toFileList(File[] iList) {
        return toFileList(iList, null);
    }



    public static File[] toFileList(File[] iList, FileFilter filter) {
        List<File> files = new ArrayList<File>();

        // add each file, but if a file is a directory, add its children files instead, without recursion (that is, do
        // not add grand-children of sub directories)
        for (File file : iList)
            addToFileList(file, files, filter);

        return files.toArray(new File[0]);
    }



    /**
     * 
     * @param base base - a file or a directory
     * @return the next line, or the string constant FileUtils.IOEXCEPTION_MARK if an io exception was caught in the
     *         middle of iteration
     * @throws IOException exception
     */
    public static File[] toFileList(File base) {
        return toFileList(base, null);
    }



    public static File[] toFileList(File base, FileFilter filter) {
        List<File> files = new ArrayList<File>();
        addToFileList(base, files, filter);
        return files.toArray(new File[0]);
    }



    private static void addToFileList(File base, List<File> files, FileFilter filter) {
        if (base.isFile()) {
            if (filter == null || filter.accept(base))
                files.add(base);
        } else if (base.isDirectory()) {
            for (File child : base.listFiles())
                if (child.isFile() && (filter == null || filter.accept(child)))
                    files.add(child);
        } else
            throw new IllegalArgumentException(base + " is not a file nor a directory");
    }



    // ---------------------------------------------------------------------------



    /**
     * wrap input stream with mediators, such as compressors, if needed
     * 
     * @param file
     * @return
     * @throws IOException
     */
    public static InputStream wrapInputStream(InputStream in, String resourceName) throws IOException {
        if ( in == null ){
            return null;
        }
        
        if (resourceName.endsWith(".gz")) {
            return new GZIPInputStream(in);
        } else if (resourceName.endsWith(".bz2")) {
            return new BZip2CompressorInputStream(in);
        } else {
            return in;
        }
    }



    /**
     * wrap output stream with mediators, such as compressors, if needed
     * 
     * @param file
     * @return
     * @throws IOException
     */
    public static OutputStream wrapOutputStream(OutputStream out, String resourceName) throws IOException {
        if (resourceName.endsWith(".gz")) {
            return new GZIPOutputStream(out);
        } else if (resourceName.endsWith(".bz2")) {
            return new BZip2CompressorOutputStream(out);
        } else {
            return out;
        }
    }



    /**
     * @param file file
     * @return buffer reader to the file
     * @throws IOException io exception
     */
    public static BufferedReader openReader(File file) throws IOException {
        return openReader(file, null);
    }



    /**
     * @param file file
     * @return buffer reader to the file
     * @param iCharset charset
     * @throws IOException io exception
     */
    public static BufferedReader openReader(File file, String iCharset) throws IOException {
        BufferedReader reader;

        if (iCharset == null)
            iCharset = DEFAULT_CHARSET;

        FileInputStream fis = new FileInputStream(file);
        InputStreamReader isr = new InputStreamReader(wrapInputStream(fis, file.getName()), iCharset);
        reader = new BufferedReader(isr);
        return reader;
    }



    public static BufferedReader openReader(InputStream iInputStream) throws IOException {
        return openReader(iInputStream, null);
    }



    public static BufferedReader openReader(InputStream iInputStream, String iCharset) throws IOException {
        if (iCharset == null)
            iCharset = DEFAULT_CHARSET;

        return new BufferedReader(new InputStreamReader(iInputStream, iCharset));
    }



    /**
     * @param file file
     * @return buffer writer to the file
     * @throws IOException io exception
     */
    public static BufferedWriter openWriter(File file) throws IOException {
        return openWriter(file, false);
    }



    public static BufferedWriter openWriter(File file, boolean iAppend) throws IOException {
        return openWriter(file, iAppend, null);
    }



    public static BufferedWriter openWriter(File file, boolean iAppend, String iCharset) throws IOException {
        BufferedWriter writer;

        if (iCharset == null)
            iCharset = DEFAULT_CHARSET;

        OutputStreamWriter osw = null;
        FileOutputStream fos = new FileOutputStream(file, iAppend);
        osw = new OutputStreamWriter(wrapOutputStream(fos, file.getName()), iCharset);
        writer = new BufferedWriter(osw);
        return writer;
    }



    public static ObjectInputStream openObjectReader(File file) throws IOException {
        return new ObjectInputStream(openBufferedInputStream(file));
    }



    /**
     * open buffered input stream
     * 
     * @param file
     * @return
     * @throws IOException
     */
    public static BufferedInputStream openBufferedInputStream(File file) throws IOException {
        return new BufferedInputStream(wrapInputStream(new FileInputStream(file), file.getName()));
    }



    /**
     * Create objects file, override existing file.
     * 
     * @param fs file system
     * @param filePath file path
     * @return ObjectOutputStream to the file
     * @throws IOException exception
     */
    public static ObjectOutputStream openObjectWriter(File out) throws IOException {
        return new ObjectOutputStream(openBufferedOutputStream(out));
    }



    /**
     * Create output stream, override existing file.
     * 
     * @param fs file system
     * @param filePath file path
     * @return ObjectOutputStream to the file
     * @throws IOException exception
     */
    public static BufferedOutputStream openBufferedOutputStream(File out) throws IOException {
        return new BufferedOutputStream(wrapOutputStream(new FileOutputStream(out), out.getName()));
    }



    /**
     * opens a file as a PrintStream. The file could be gzipped if its named ends with '.gz'
     * 
     * @param iFile
     * @param iCharset charset
     * @return
     * @throws IOException
     */
    public static PrintStream openPrintStream(File iFile) throws IOException {
        return openPrintStream(iFile, false, null);
    }



    /**
     * opens a file as a PrintStream. The file could be gzipped if its named ends with '.gz'
     * 
     * @param iFile
     * @return
     * @throws IOException
     */
    public static PrintStream openPrintStream(File iFile, String iCharset) throws IOException {
        return openPrintStream(iFile, false, iCharset);
    }



    /**
     * opens a file as a PrintStream. The file could be gzipped if its named ends with '.gz'
     * 
     * @param iFile
     * @return
     * @throws IOException
     */
    public static PrintStream openPrintStream(File iFile, boolean appendMode) throws IOException {
        return openPrintStream(iFile, appendMode, null);
    }



    public static PrintStream openPrintStream(File iFile, boolean appendMode, String iCharset) throws IOException {
        return openPrintStream(iFile, appendMode, iCharset, false);
    }



    /**
     * opens a file as a PrintStream. The file could be gzipped if its named ends with '.gz'
     * 
     * @param iFile
     * @return
     * @throws IOException
     */
    public static PrintStream openPrintStream(File iFile, boolean appendMode, String iCharset, boolean autoFlush)
                    throws IOException {
        PrintStream out;

        if (iCharset == null)
            iCharset = DEFAULT_CHARSET;

        OutputStream outs = wrapOutputStream(new FileOutputStream(iFile, appendMode), iFile.getName());
        out = new PrintStream(outs, autoFlush, iCharset);
        return out;
    }



    public static PrintStream openPrintStream(String iFile) throws IOException {
        return openPrintStream(new File(iFile));
    }



    public static PrintStream openPrintStream(String iFile, String iCharset) throws IOException {
        return openPrintStream(new File(iFile), iCharset);
    }



    public static PrintStream openPrintStream(String iFile, boolean appendMode) throws IOException {
        return openPrintStream(new File(iFile), appendMode, null, false);
    }



    public static PrintStream openPrintStream(String iFile, boolean appendMode, String iCharset, boolean autoFlush)
                    throws IOException {
        return openPrintStream(new File(iFile), appendMode, iCharset, false);
    }



    // ---------------------------------------------------------------------------



    /**
     * @param args arguments
     * @return array of buffer readers
     * @throws IOException exception
     */
    public static BufferedReader[] readArgs(String[] args) throws IOException {
        BufferedReader[] list;
        int i = 0;

        if (args == null || args.length == 0) {
            list = new BufferedReader[1];
            list[0] = openReader(System.in);
        } else {
            list = new BufferedReader[args.length];
            for (String f : args)
                list[i++] = openReader(new File(f));
        }

        return list;
    }



    /**
     * @param src source
     * @param dst destination
     * @throws IOException io exception
     */
    public static void copy(File src, File dst) throws IOException {
        BufferedInputStream reader = new BufferedInputStream(new FileInputStream(src));
        BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(dst));
        copy(reader, writer);
        writer.close();
        reader.close();
    }



    /**
     * @param src source
     * @param dst destination
     * @throws IOException io exception
     */
    public static void copy(InputStream src, OutputStream dst) throws IOException {
        int c;
        byte[] buff = new byte[4096 * 4];

        while ((c = src.read(buff)) >= 0)
            dst.write(buff, 0, c);
    }



    /**
     * Performs byte to byte comparison of two files and returns true if they are identical.
     */
    public static boolean compare(File leftF, File rightF) throws IOException {
        BufferedInputStream left = new BufferedInputStream(new FileInputStream(leftF));
        BufferedInputStream right = new BufferedInputStream(new FileInputStream(rightF));
        byte[] leftB = new byte[4096 * 4];
        byte[] rightB = new byte[4096 * 4];
        int lc, rc;
        boolean rs = true;

        do {
            lc = left.read(leftB);
            rc = right.read(rightB);
            if (lc != rc) {
                rs = false;
                break;
            }

            for (int i = 0; i < lc; i++) {
                if (leftB[i] != rightB[i]) {
                    rs = false;
                    break;
                }
            }

        } while (rs && lc > 0 && rc > 0);

        left.close();
        right.close();

        return rs;
    }



    /**
     * Performs line to line comparison of two files and returns true if they are identical.
     */
    public static boolean lineCompare(File leftF, File rightF) throws IOException {
        Iterator<String> leftit = FileUtils.iterateLines(leftF).iterator();
        Iterator<String> rightit = FileUtils.iterateLines(rightF).iterator();
        String left, right;

        while (leftit.hasNext() && rightit.hasNext()) {
            left = leftit.next();
            right = rightit.next();
            if (!left.equals(right))
                return false;
        }

        if (leftit.hasNext() || rightit.hasNext())
            return false;

        return true;
    }



    /**
     * Performs line to line comparison of two files, with possible exception for lines with certain prefix and/or
     * suffix, and returns true if they are identical. If either affix parameter is null, there is no exception.
     */
    public static boolean lineCompare(File leftF, File rightF, String exceptPrefix, String exceptSuffix)
                    throws IOException {
        if (exceptPrefix == null && exceptSuffix == null)
            return lineCompare(leftF, rightF);

        Iterator<String> leftLines = FileUtils.iterateLines(leftF).iterator();
        Iterator<String> rightLines = FileUtils.iterateLines(rightF).iterator();
        String left, right;

        while (leftLines.hasNext() && rightLines.hasNext()) {
            left = leftLines.next();
            right = rightLines.next();
            if (exceptPrefix != null && left.startsWith(exceptPrefix) && right.startsWith(exceptPrefix))
                continue;
            if (exceptSuffix != null && left.endsWith(exceptSuffix) && right.endsWith(exceptSuffix))
                continue;
            if (!left.equals(right))
                return false;
        }
        if (leftLines.hasNext() || rightLines.hasNext())
            return false;

        return true;
    }



    /**
     * @param dir directory
     */
    public static void rmdirRecursive(File dir) {
        for (File child : dir.listFiles()) {
            if (child.isDirectory())
                rmdirRecursive(child);
            else
                child.delete();
        }

        dir.delete();
    }



    /**
     * 
     * @param fs file system
     * @param hadoopPath directory path with one or more files
     * @return Object iterator
     * @throws IOException exception
     */
    public static Iterable<Object> iterateObjects(File file) throws IOException {
        return new ObjectIterable(new ObjectInputStreamIterable(new File[] {file}, null));
    }



    /**
     * 
     * @param inputStream inputStream
     * @return the next line, or the string constant FileUtils.IOEXCEPTION_MARK if an io exception was caught in the
     *         middle of iteration
     * @throws IOException exception
     */
    public static Iterable<String> iterateLines(InputStream inputStream) throws IOException {
        return new LineIterable(Collections.singletonList(openReader(inputStream)));
    }



    /**
     * 
     * @param inputStream inputStream
     * @param iCharset charset
     * @return the next line, or the string constant FileUtils.IOEXCEPTION_MARK if an io exception was caught in the
     *         middle of iteration
     * @throws IOException exception
     */
    public static Iterable<String> iterateLines(InputStream inputStream, String iCharset) throws IOException {
        return new LineIterable(Collections.singletonList(openReader(inputStream, iCharset)));
    }



    /**
     * 
     * @param inputStream inputStream
     * @return the next line, or the string constant FileUtils.IOEXCEPTION_MARK if an io exception was caught in the
     *         middle of iteration
     * @throws IOException exception
     */
    public static Iterable<String> iterateLines(Reader inputStream) throws IOException {
        return new LineIterable(Collections.singletonList(new BufferedReader(inputStream)));
    }



    /**
     * 
     * @param input input file
     * @return the next line, or the string constant FileUtils.IOEXCEPTION_MARK if an io exception was caught in the
     *         middle of iteration
     * @throws IOException exception
     */
    public static Iterable<String> iterateLines(File input) throws IOException {
        return new LineIterable(new BufferedReaderIterable(toFileList(input), null));
    }



    /**
     * 
     * @param input input file
     * @param iCharset charset
     * @return the next line, or the string constant FileUtils.IOEXCEPTION_MARK if an io exception was caught in the
     *         middle of iteration
     * @throws IOException exception
     */
    public static Iterable<String> iterateLines(File input, String iCharset) throws IOException {
        return new LineIterable(new BufferedReaderIterable(toFileList(input), iCharset));
    }



    /**
     * 
     * @param filesIn files - file list separated by ';'
     * @return the next line, or the string constant FileUtils.IOEXCEPTION_MARK if an io exception was caught in the
     *         middle of iteration
     * @throws IOException exception
     */
    public static Iterable<String> iterateLines(String filesIn) throws IOException {
        return new LineIterable(new BufferedReaderIterable(toFileList(filesIn), null));
    }



    /**
     * 
     * @param filesIn files - file list separated by ';'
     * @param iCharset charset
     * @return the next line, or the string constant FileUtils.IOEXCEPTION_MARK if an io exception was caught in the
     *         middle of iteration
     * @throws IOException exception
     */
    public static Iterable<String> iterateLines(String filesIn, String iCharset) throws IOException {
        return new LineIterable(new BufferedReaderIterable(toFileList(filesIn), iCharset));
    }



    /**
     * @param filesIn files - file list separated by ';'
     * @param filter file filters
     * @return the next line, or the string constant FileUtils.IOEXCEPTION_MARK if an io exception was caught in the
     *         middle of iteration
     * @throws IOException
     */
    public static Iterable<String> iterateLines(String filesIn, FileFilter filter) throws IOException {
        return new LineIterable(new BufferedReaderIterable(toFileList(filesIn, filter), null));
    }



    /**
     * @param filesIn files - file list separated by ';'
     * @param filter file filters
     * @return the next line, or the string constant FileUtils.IOEXCEPTION_MARK if an io exception was caught in the
     *         middle of iteration
     * @throws IOException
     */
    public static Iterable<String> iterateLines(String filesIn, FileFilter filter, String charset) throws IOException {
        return new LineIterable(new BufferedReaderIterable(toFileList(filesIn, filter), charset));
    }



    /**
     * 
     * @param files - list of files to iterate
     * @return the next line, or the string constant FileUtils.IOEXCEPTION_MARK if an io exception was caught in the
     *         middle of iteration
     * @throws IOException exception
     */
    public static Iterable<String> iterateLines(File[] files) throws IOException {
        return new LineIterable(new BufferedReaderIterable(toFileList(files), null));
    }



    /**
     * 
     * @param files - list of files to iterate
     * @param iCharset charset
     * @return the next line, or the string constant FileUtils.IOEXCEPTION_MARK if an io exception was caught in the
     *         middle of iteration
     * @throws IOException exception
     */
    public static Iterable<String> iterateLines(File[] files, String iCharset) throws IOException {
        return new LineIterable(new BufferedReaderIterable(toFileList(files), iCharset));
    }



    /**
     * Split a given file to numberOfSplits sub files.
     * 
     * @param inputFile input file
     * @param outputFilePrfix output prefix
     * @param numberOfSplits num of splits
     * @throws Exception exception
     */
    public static void split(String inputFile, String outputFilePrfix, int numberOfSplits) throws Exception {
        BufferedWriter[] writers;
        int c;
        File in;
        long inLen;
        long outLen;
        long partLen;

        writers = new BufferedWriter[numberOfSplits];
        for (int i = 0; i < writers.length; i++)
            writers[i] = FileUtils.openWriter(new File(outputFilePrfix + "." + (i + 1)));

        c = 0;
        outLen = 0;
        in = new File(inputFile);
        inLen = in.length();
        partLen = inLen / writers.length;
        for (String l : FileUtils.iterateLines(in)) {
            if (outLen > partLen && c < writers.length - 1) {
                c++;
                outLen = 0;
            }

            writers[c].write(l);
            writers[c].newLine();
            outLen += l.length() + 2;
        }

        for (int i = 0; i < writers.length; i++)
            writers[i].close();
    }



    /**
     * generating a tmp file name by adding the postfix at the end of the file name, or just before an ending 'gz'
     * 
     * @param iFile - the input file name
     * @return the tmp file name
     */
    public static String generateTmpFileName(String iFile, String iPostfix) {
        int p = iFile.lastIndexOf('.');

        if (p >= 0 && iFile.substring(p + 1).equals("gz")) {
            return iFile.substring(0, p + 1) + iPostfix + ".gz";
        } else if (p >= 0 && iFile.substring(p + 1).equals("bz2")) {
            return iFile.substring(0, p + 1) + iPostfix + ".bz2";
        } else
            return iFile + "." + iPostfix;
    }



    /**
     * Append to a file a postfix, but keeping the file type indicator, via that last dot-seperated component of the
     * name, intact
     * 
     * @param file
     * @param postfix
     * @return
     */
    public static File addPostfix(File file, String postfix) {
        String name = file.getName();
        String[] cols = name.split("\\.");

        if (cols.length == 1)
            name = name + "." + postfix;
        else {
            String type = cols[cols.length - 1];
            cols[cols.length - 1] = postfix;
            name = StringUtils.join(cols, ".") + "." + type;
        }

        return new File(file.getParent(), name);
    }



    /**
	 * 
	 */
    public static final String IOEXCEPTION_MARK =
                    "mark io exception by returning this const, because we cannot throw exception at this point";

    protected static final String DEFAULT_CHARSET = "UTF8";
}
