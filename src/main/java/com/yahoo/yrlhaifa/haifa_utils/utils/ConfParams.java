// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.haifa_utils.utils;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.Reader;
import java.lang.reflect.Array;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Properties file that support hadoop utilities and include of inner files.
 * 
 * @author nadavg (17-Sep-2015: some modifications by Asher Stern)
 * 
 */
public class ConfParams {

    private static final String INCLUDE_PREAMBLE = "%include ";
    private static final String COMMENT_PREAMBLE = "#";
    private static final String REMOVE_PREAMBLE = "-";
    private static final String VARIABLE_PREAMBLE = "$";
    private static final String NEWLINE = "<NL/>";
    private static final Pattern VARIABLE_USE = Pattern.compile("\\$\\{([^\\}]+)\\}");
    public static final String MAIN_CONFIG_FILE_PATH_PREFIX = "main_conf_file_path_prefix";
    public static final String MULTI_VALUES_DELIMITER = ",";

    private final HashMap<String, String> variables = new HashMap<String, String>();
    private final HashMap<String, String> attributes = new HashMap<>();

    /**
     * Whether the configuration should throw NoSuchElementExceptions or simply return null when a property does not
     * exist. Defaults to throw exception.
     */
    private boolean throwExceptionOnMissing = true;


    protected String m_mainConfFilePathPrefix = null;

    /**
     * @author nadavg
     * 
     */
    public class ConfParamsException extends Exception {
        /**
         * @param message message
         */
        public ConfParamsException(String message) {
            super(message);
        }

        private static final long serialVersionUID = -2045324902893017255L;
    }

    /**
     * 
     */
    public ConfParams() {
        super();
    }

    /**
     * copy ctor
     */
    public ConfParams(ConfParams other) {
        m_mainConfFilePathPrefix = other.m_mainConfFilePathPrefix;
        variables.putAll(other.variables);
        attributes.putAll(other.attributes);
    }

    public static String escape(String iValue) {
        return iValue.replaceAll("" + '\n', NEWLINE);
    }

    public static String unescape(String iValue) {
        return iValue.replaceAll(NEWLINE, "" + '\n');
    }

    protected String getCanonicalSourceName(String source) throws IOException {
        File f = new File(source);

        // we first check if the provided path does exist
        // if not, we try to add a prefix of the main config
        if (!f.isFile() && !source.startsWith("/") && m_mainConfFilePathPrefix != null) {
            File parent = new File(m_mainConfFilePathPrefix);
            f = new File(parent, source);
        }

        return f.getCanonicalPath();
    }

    protected Iterable<String> getConfLines(String source) throws IOException {
        File sourceFile = new File(source);
        if (!sourceFile.exists()) {
            sourceFile = ResourceDataLoader.getFile(source);
        }
        return FileUtils.iterateLines(sourceFile);
    }

    protected void initPathPrefix(String source) {
        File paramsFile = new File(source);
        File parent = paramsFile.getParentFile();
        if (parent != null)
            m_mainConfFilePathPrefix = parent.getAbsolutePath();
    }

    /**
     * loading properties from a line iterator. Each line may be: <br>
     * - empty or starts with a comment (#), and then it is ignored <br>
     * - starts with an %include preamble, which marks another configuration file to be included in the loaded conf <br>
     * - starts with a removal sign (-), which marks the parameter to be removed from the configuration <br>
     * - a key val pair separated by '=', which will be loaded into the configuration <br>
     * 
     * @param iLines
     * @param ioLoadedSources
     * @param iHadoopPath
     * @param iFS
     * @throws ConfParamsException
     * @throws IOException
     */
    protected void load(Iterable<String> iLines, HashSet<String> ioLoadedSources, boolean iWithVariables)
                    throws ConfParamsException, IOException {
        String[] cols;
        String varName, varValue;

        for (String l : iLines) {
            if (l == FileUtils.IOEXCEPTION_MARK)
                throw new IOException("IOException happened in FileUtils iterator");

            l = l.trim();
            if (l.length() == 0 || l.startsWith(COMMENT_PREAMBLE))
                continue;

            if (l.startsWith(INCLUDE_PREAMBLE)) {
                String sourcePath = getValue(l.substring(INCLUDE_PREAMBLE.length()));

                if (!ResourceDataLoader.resourceExists(sourcePath)) {
                    sourcePath = getCanonicalSourceName(sourcePath);
                }

                if (sourcePath != null && !ioLoadedSources.contains(sourcePath)) {
                    ioLoadedSources.add(sourcePath);
                    Iterable<String> includedLines = getConfLines(sourcePath);
                    if (includedLines != null)
                        load(includedLines, ioLoadedSources, iWithVariables);
                }
            } else if (l.startsWith(REMOVE_PREAMBLE)) {
                if (l.length() > REMOVE_PREAMBLE.length()) {
                    l = l.substring(REMOVE_PREAMBLE.length());
                    if (l.startsWith(VARIABLE_PREAMBLE)) {
                        varName = l.substring(VARIABLE_PREAMBLE.length());
                        variables.remove(varName);
                    } else
                        attributes.remove(l);
                }
            } else {
                cols = l.split("=", 2);
                if (cols.length < 2)
                    throw new ConfParamsException("invalid conf line: " + l);

                if (!iWithVariables) {
                    attributes.put(cols[0], unescape(cols[1]));
                } else if (cols[0].startsWith(VARIABLE_PREAMBLE)) {
                    varName = cols[0].substring(VARIABLE_PREAMBLE.length());
                    varValue = JavaUtils.escapeForPatternReplacement(getValue(cols[1]));
                    variables.put(varName, varValue);
                } else {
                    attributes.put(cols[0], getValue(cols[1]));
                }
            }
        }

        // finally store main config prefix in the configuration
        if (m_mainConfFilePathPrefix != null) {
            attributes.put(MAIN_CONFIG_FILE_PATH_PREFIX, m_mainConfFilePathPrefix);
        }
    }

    private String getValue(String rValue) throws ConfParamsException {
        StringBuffer s = new StringBuffer();
        Matcher m = VARIABLE_USE.matcher(rValue);
        String varName;
        String varValue;
        while (m.find()) {
            varName = m.group(1);
            varValue = variables.get(varName);
            if (varValue == null)
                throw new ConfParamsException("undefined variable '" + varName + "': " + rValue);
            m.appendReplacement(s, varValue);
        }
        m.appendTail(s);
        return unescape(s.toString());
    }

    public ConfParams(String source) throws ConfParamsException, IOException {
        this(source, true);
    }

    public ConfParams(File paramsFile) throws ConfParamsException, IOException {
        this(paramsFile, true);
    }

    public ConfParams(File paramsFile, boolean iAnalyzeVariables) throws ConfParamsException, IOException {
        this(paramsFile.getCanonicalPath(), iAnalyzeVariables);
    }

    /**
     * Instantiate the ConfParam object based on the info of the iParamsFile java File.
     * 
     * @param paramsFile param file
     * @throws ConfParamsException exception
     * @throws IOException exception
     */
    public ConfParams(String source, boolean iAnalyzeVariables) throws ConfParamsException, IOException {
        super();

        HashSet<String> loadedFiles = new HashSet<String>();

        initPathPrefix(source);

        source = getCanonicalSourceName(source);
        loadedFiles.add(source);
        Iterable<String> lines = getConfLines(source);
        if (lines != null)
            load(lines, loadedFiles, iAnalyzeVariables);
    }

    public ConfParams(Reader reader) throws ConfParamsException, IOException {
        this(reader, true);
    }

    /**
     * Instantiate the ConfParam object based on the info of the iParamsFile java File.
     * 
     * @param paramsFile param file
     * @throws ConfParamsException exception
     * @throws IOException exception
     */
    public ConfParams(Reader reader, boolean iAnalyzeVariables) throws ConfParamsException, IOException {
        super();

        HashSet<String> loadedFiles = new HashSet<String>();

        Iterable<String> lines = FileUtils.iterateLines(reader);
        if (lines != null)
            load(lines, loadedFiles, iAnalyzeVariables);
    }

    private final String rawGet(String key) {
        return attributes.get(key);
    }

    private String get(String key, boolean throwWhenMissing) {
        String ret = rawGet(key);
        if ((null == ret) && throwWhenMissing) {
            throw new NoSuchElementException("Parameter " + key + " does not appear in the configuration parameters.");
        }
        return ret;
    }


    /**
     * Allows to set the {@code throwExceptionOnMissing} flag. This flag controls the behavior of getter methods that
     * return objects if the requested property is missing. If the flag is set to <b>false</b>, these methods will
     * return <b>null</b>. If set to <b>true</b> (which is the default value), they will throw a
     * {@code NoSuchElementException} exception. Note that getter methods for primitive data types are not affected by
     * this flag.
     *
     * @param throwExceptionOnMissing The new value for the flag
     */
    public void setThrowExceptionOnMissing(boolean throwExceptionOnMissing) {
        this.throwExceptionOnMissing = throwExceptionOnMissing;
    }

    /**
     * Returns true if missing values throw Exceptions, when retrieved as some java objects, and no default value is
     * specified. Note that retrieval of primitive types will always throw an exception if they do not exist, unless a
     * default value is specified.
     *
     * @return true if missing parameters throw Exceptions
     */
    public boolean isThrowExceptionOnMissing() {
        return throwExceptionOnMissing;
    }


    public String get(String key) {
        return get(key, throwExceptionOnMissing);
    }

    public String get(String key, String defaultVal) {
        String ret = get(key, false);
        if (null == ret) {
            ret = defaultVal;
        }
        return ret;
    }

    public String set(String key, String val) {
        return attributes.put(key, val);
    }

    public String remove(String key) {
        return attributes.remove(key);
    }

    public String put(String key, String val) {
        return set(key, val);
    }

    public String getString(String key, String defaultVal) {
        return get(key, defaultVal);
    }

    public String getString(String key) {
        return get(key);
    }

    public String setString(String key, String val) {
        return attributes.put(key, val);
    }

    public File getFile(String key) {
        String s = getString(key);
        if (s == null)
            return null;

        return new File(s);
    }

    public File[] getFilesByRegExp(String baseKey, String regexpKey) {
        String base = getString(baseKey);
        String regexp = getString(regexpKey);
        File dir = new File(base);
        return dir.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return Pattern.matches(regexp, name);
            }
        });
    }
    
    public File getFile(String key, File defaultF) {
        String s = getString(key, null);
        if (s == null)
            return defaultF;

        return new File(s);
    }

    public File[] getFiles(String key) {
        String[] strings = getStrings(key);
        if (null == strings) {
            return null;
        } else {
            File[] files = new File[strings.length];

            for (int i = 0; i < strings.length; i++)
                files[i] = new File(strings[i]);

            return files;
        }
    }

    public long getLong(String key) {
        return Long.parseLong(get(key, true));
    }

    public long getLong(String key, long defaultVal) {
        String value = get(key, false);
        if (null == value) {
            return defaultVal;
        } else {
            return Long.parseLong(value);
        }
    }

    public int getInt(String key) {
        return Integer.parseInt(get(key, true));
    }

    public int getInt(String key, int defaultVal) {
        String value = get(key, false);
        if (null == value) {
            return defaultVal;
        } else {
            return Integer.parseInt(value);
        }
    }

    public void setInt(String key, int val) {
        attributes.put(key, Integer.toString(val));
    }

    public void setBoolean(String key, boolean val) {
        attributes.put(key, Boolean.toString(val));
    }

    public boolean getBoolean(String key) {
        return Boolean.parseBoolean(get(key, true));
    }

    public boolean getBoolean(String key, boolean defaultVal) {
        String value = get(key, false);
        if (null == value) {
            return defaultVal;
        } else {
            return Boolean.parseBoolean(value);
        }
    }

    public float getFloat(String key) {
        return Float.parseFloat(get(key, true));
    }

    public float getFloat(String key, float defaultVal) {
        String value = get(key, false);
        if (null == value) {
            return defaultVal;
        } else {
            return Float.parseFloat(value);
        }
    }

    public double getDouble(String key) {
        return Double.parseDouble(get(key, true));
    }

    public double getDouble(String key, double defaultVal) {
        String value = get(key, false);
        if (null == value) {
            return defaultVal;
        } else {
            return Double.parseDouble(value);
        }
    }

    public String[] getStrings(String key) {
        String v = get(key);
        if (v == null)
            return null;

        return v.split(MULTI_VALUES_DELIMITER);
    }

    public String[] getStrings(String key, String[] defaultVal) {
        String v = get(key, false);
        if (v == null)
            return defaultVal;

        return v.split(MULTI_VALUES_DELIMITER);
    }

    /**
     * Returns an array of enum-constants of the given enum-class. The constants should be specified as a
     * comma-separated string, and are case-sensitive.
     * 
     * @param key a parameter key (name)
     * @param enumClass the class of the expected enum
     * @return An arrya of enum-constants specified as the value of the given key.
     */
    public <T extends Enum<T>> T[] getEnumValues(String key, Class<T> enumClass) {
        String[] stringValues = getStrings(key);
        if (null == stringValues) {
            return null;
        }
        @SuppressWarnings("unchecked")
        T[] ret = (T[]) Array.newInstance(enumClass, stringValues.length); // new T[stringValues.length];
        for (int index = 0; index < stringValues.length; ++index) {
            String value = stringValues[index];
            T enumConstant = Enum.valueOf(enumClass, value.trim());
            ret[index] = enumConstant;
        }
        return ret;
    }

    /**
     * Returns an array of enum-constants of the given enum-class. The constants should be specified as a
     * comma-separated string, and are case-sensitive.
     * 
     * @param key a parameter key (name)
     * @param enumClass the class of the expected enum
     * @param defaultValues a default array with enum-constants to be returned if the key does not appear in the
     *        configuration file.
     * @return An arrya of enum-constants specified as the value of the given key, or <code>defaultValues</code> if the
     *         key does not appear in the configuration file.
     */
    public <T extends Enum<T>> T[] getEnumValues(String key, Class<T> enumClass, T[] defaultValues) {
        if (containsKey(key)) {
            return getEnumValues(key, enumClass);
        } else {
            return defaultValues;
        }
    }

    public Iterable<Entry<String, String>> entrySet() {
        return attributes.entrySet();
    }

    public boolean containsKey(String key) {
        return (rawGet(key) != null);
    }

    /**
     * Step over the properties and update the given hadoop config object
     * 
     * @param hadoopConf configuration
     */
    public void fill(ConfParams params) {
        for (Map.Entry<String, String> e : params.entrySet()) {
            set(e.getKey(), e.getValue());
        }
    }

    /**
     * Step over the properties and update the given hadoop config object with all the properties start with the given
     * keyPrefix
     * 
     * @param hadoopConf configuration
     * @param keyPrefix key prefix
     */
    public void fill(ConfParams params, String keyPrefix) {
        for (Map.Entry<String, String> e : params.entrySet()) {
            final String key = e.getKey();
            if (key.startsWith(keyPrefix)) {
                set(key, e.getValue());
            }
        }
    }
}
