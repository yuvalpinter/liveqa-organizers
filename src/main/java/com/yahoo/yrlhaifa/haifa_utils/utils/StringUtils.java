// Copyright 2016, Yahoo Inc.
// Licensed under the terms of the New BSD License. Please see associated LICENSE file for terms.

package com.yahoo.yrlhaifa.haifa_utils.utils;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;



/**
 * String encoding and HTML escaping utilities
 * 
 * @author nadavg
 */
public class StringUtils {


    /**
     * Accept or reject a character. Used by {@link StringUtilities#trimByCharacterAccept(String, CharacterAccept)}.
     */
    public static interface CharacterAccept {
        public boolean accept(char c);
    }

    // ----------------------------------------------------------------------


    private static class SplitIterator implements Iterator<String>, Iterable<String> {
        public SplitIterator(String iStr, String iDelimiters, boolean iEachDelimiterSplits) {
            if (iDelimiters == null)
                iDelimiters = " \r\n\t";

            for (int i = 0; i < iDelimiters.length(); i++)
                m_delimiters.add(iDelimiters.charAt(i));

            m_tokenizer = new StringTokenizer(iStr, iDelimiters, iEachDelimiterSplits);
            m_eachDelimiterSplits = iEachDelimiterSplits;

            m_last = false;
            getNextColumn(true);
        }



        @Override
        public Iterator<String> iterator() {
            return this;
        }



        private void getNextColumn(boolean iFirst) {
            String token;

            if (m_last) {
                m_nextColumn = null;
                return;
            }

            if (m_tokenizer.hasMoreTokens()) {
                token = m_tokenizer.nextToken();
                if (m_eachDelimiterSplits && token.length() == 1 && m_delimiters.contains(token.charAt(0)))
                    m_nextColumn = "";
                else {
                    m_nextColumn = token;
                    if (m_eachDelimiterSplits) {
                        if (m_tokenizer.hasMoreTokens()) // if non-empty column
                                                         // found, skip the
                                                         // next 'token'
                                                         // which is a
                                                         // delimiter marking
                                                         // the end of the
                                                         // found column
                            m_tokenizer.nextToken();
                        else
                            m_last = true;
                    }
                }
            } else if (m_eachDelimiterSplits || iFirst) {
                m_nextColumn = "";
                m_last = true;
            } else
                m_nextColumn = null;
        }



        @Override
        public boolean hasNext() {
            return (m_nextColumn != null);
        }



        @Override
        public String next() {
            String res = m_nextColumn;

            if (m_nextColumn != null)
                getNextColumn(false);

            return res;
        }



        @Override
        public void remove() {}



        private final boolean m_eachDelimiterSplits;
        private final StringTokenizer m_tokenizer;
        private String m_nextColumn;
        private boolean m_last;
        private final HashSet<Character> m_delimiters = new HashSet<Character>();
    }



    // ----------------------------------------------------------------------



    private static Pattern escapedPattern = Pattern.compile("&#?([a-zA-Z0-9]+);");
    private static HashMap<String, String> specialCharactersMap;

    static {
        specialCharactersMap = new HashMap<>(6);
        specialCharactersMap.put("quot", "\"");
        specialCharactersMap.put("lt", "<");
        specialCharactersMap.put("gt", ">");
        specialCharactersMap.put("amp", "&");
        specialCharactersMap.put("apos", "'");
        specialCharactersMap.put("ndash", "-");
    }



    // Suppresses default constructor, ensuring non-instantiability.
    private StringUtils() {}



    /**
     * Returns the orig string after escaping HTML special characters.
     * 
     * @param orig original string
     * @return the orig string after escaping HTML special characters.
     */
    public static String escapeForHtml(String orig) {
        char c;
        StringBuffer s = new StringBuffer();

        for (int i = 0; i < orig.length(); i++) {
            c = orig.charAt(i);
            if (Character.isLetterOrDigit(c) || Character.isWhitespace(c) || c == '_' || c == '-' || c == '+')
                s.append(c);
            else
                s.append("&#").append(Integer.toString(c)).append(";");
        }

        return s.toString();
    }



    /**
     * Escape all the characters in the string to match the HTML format.
     * 
     * @param orig original string
     * @return orig string after escaping all the characters.
     */
    public static String escapeEverythingForHtml(String orig) {
        char c;
        StringBuffer s = new StringBuffer();

        for (int i = 0; i < orig.length(); i++) {
            c = orig.charAt(i);
            s.append("&#").append(Integer.toString(c)).append(";");
        }
        return s.toString();
    }



    /**
     * Returns the orig string after escaping Hadoop special characters.
     * 
     * @param orig original string
     * @return the orig string after escaping Hadoop special characters.
     */
    public static String escapeForHadoop(String orig) {
        char c;
        StringBuffer s = new StringBuffer();

        for (int i = 0; i < orig.length(); i++) {
            c = orig.charAt(i);
            if (Character.isLetterOrDigit(c) || c == '_' || c == '-' || c == '+')
                s.append(c);
            else
                s.append("&#").append(Integer.toString(c)).append(";");
        }

        return s.toString();
    }



    /**
     * Returns the escaped string after cleaning the special characters.
     * 
     * @param escaped escaped string
     * @return the escaped string after cleaning the special characters.
     */
    public static String unescape(String escaped) {
        String v;
        StringBuffer s = new StringBuffer();
        Matcher m = escapedPattern.matcher(escaped);
        int c;
        boolean num;
        boolean hex;

        while (m.find()) {
            m.appendReplacement(s, "");
            v = m.group(1);
            num = true;
            hex = false;
            for (int i = 0; i < v.length(); i++) {
                c = v.charAt(i) - '0';
                if (c < 0 || c > 9) {
                    if (i > 0 || c + '0' != 'x') {
                        int ch = c + '0' - 'A';
                        int cH = c + '0' - 'a';
                        if (!hex || ((ch < 0 || ch > 5) && (cH < 0 || cH > 5))) {
                            num = false;
                            break;
                        }
                    } else
                        hex = true;
                }
            }

            if (num) {
                if (v.length() <= 8) {
                    if (hex) {
                        if (v.length() > 1)
                            s.append((char) Integer.parseInt(v.substring(1), 16));
                        else
                            s.append(m.group()); // the match was for &x;, which is not an escape sequence
                    } else
                        s.append((char) Integer.parseInt(v));
                } else
                    s.append(m.group());
            } else {
                v = specialCharactersMap.get(v);
                if (v != null)
                    s.append(v);
                else
                    s.append(m.group());
            }
        }
        m.appendTail(s);

        return s.toString();
    }



    public static String decodeUrl(String iText) {
        return iText.replaceAll("%20", " ")
                        .replaceAll("%21", "!")
                        .replaceAll("%22", "\"")
                        .replaceAll("%23", "#")
                        .replaceAll("%24", "\\$")
                        // special replaceAll behaviour. Thank you regex.
                        .replaceAll("%25", "%").replaceAll("%26", "&").replaceAll("%27", "'").replaceAll("%28", "(")
                        .replaceAll("%29", ")").replaceAll("%2A", "*").replaceAll("%2B", "+").replaceAll("%2C", ",")
                        .replaceAll("%2D", "-").replaceAll("%2E", ".").replaceAll("%2F", "/").replaceAll("%30", "0")
                        .replaceAll("%31", "1").replaceAll("%32", "2").replaceAll("%33", "3").replaceAll("%34", "4")
                        .replaceAll("%35", "5").replaceAll("%36", "6").replaceAll("%37", "7").replaceAll("%38", "8")
                        .replaceAll("%39", "9").replaceAll("%3A", ":").replaceAll("%3B", ";").replaceAll("%3C", "<")
                        .replaceAll("%3D", "=").replaceAll("%3E", ">").replaceAll("%3F", "?").replaceAll("%40", "@")
                        .replaceAll("%41", "A").replaceAll("%42", "B").replaceAll("%43", "C").replaceAll("%44", "D")
                        .replaceAll("%45", "E").replaceAll("%46", "F").replaceAll("%47", "G").replaceAll("%48", "H")
                        .replaceAll("%49", "I").replaceAll("%4A", "J").replaceAll("%4B", "K").replaceAll("%4C", "L")
                        .replaceAll("%4D", "M").replaceAll("%4E", "N").replaceAll("%4F", "O").replaceAll("%50", "P")
                        .replaceAll("%51", "Q").replaceAll("%52", "R").replaceAll("%53", "S")
                        .replaceAll("%54", "T")
                        .replaceAll("%55", "U")
                        .replaceAll("%56", "V")
                        .replaceAll("%57", "W")
                        .replaceAll("%58", "X")
                        .replaceAll("%59", "Y")
                        .replaceAll("%5A", "Z")
                        .replaceAll("%5B", "[")
                        .replaceAll("%5C", "\\\\")
                        // special replaceAll behaviour. Thank you regex.
                        .replaceAll("%5D", "]").replaceAll("%5E", "^").replaceAll("%5F", "_").replaceAll("%60", "`")
                        .replaceAll("%61", "a").replaceAll("%62", "b").replaceAll("%63", "c").replaceAll("%64", "d")
                        .replaceAll("%65", "e").replaceAll("%66", "f").replaceAll("%67", "g").replaceAll("%68", "h")
                        .replaceAll("%69", "i").replaceAll("%6A", "j").replaceAll("%6B", "k").replaceAll("%6C", "l")
                        .replaceAll("%6D", "m").replaceAll("%6E", "n").replaceAll("%6F", "o").replaceAll("%70", "p")
                        .replaceAll("%71", "q").replaceAll("%72", "r").replaceAll("%73", "s").replaceAll("%74", "t")
                        .replaceAll("%75", "u").replaceAll("%76", "v").replaceAll("%77", "w").replaceAll("%78", "x")
                        .replaceAll("%79", "y").replaceAll("%7A", "z").replaceAll("%7B", "{").replaceAll("%7C", "|")
                        .replaceAll("%7D", "}").replaceAll("%7E", "~").replaceAll("%80", "`").replaceAll("%82", ",");
    }



    /**
     * Returns the longest common prefix for the specified strings
     * 
     * @param s the first string.
     * @param t the second string.
     * @return the longest common prefix for the specified strings
     */
    public static final String longestCommonPrefix(String s, String t) {
        int n = Math.min(s.length(), t.length());
        int i;
        for (i = 0; i < n; ++i) {
            if (s.charAt(i) != t.charAt(i)) {
                break;
            }
        }
        return s.substring(0, i);
    }



    /**
     * Returns the longest common prefix for the specified strings.
     * 
     * @param stringArray an array of strings.
     * @return the longest common prefix for the specified strings.
     */
    public static final String longestCommonPrefix(String[] stringArray) {
        return longestCommonPrefix(Arrays.asList(stringArray));
    }



    /**
     * Returns the longest common prefix for the specified strings.
     * 
     * @param stringList a list of strings.
     * @return the longest common prefix for the specified strings.
     */
    public static final String longestCommonPrefix(List<String> stringList) {
        final int n = stringList.size();
        if (n < 1) {
            throw new IllegalArgumentException();
        }
        String lcpString = stringList.get(0);
        for (String string : stringList) {
            lcpString = longestCommonPrefix(lcpString, string);
        }
        return lcpString;
    }



    /**
     * Decode the given byte array to a String
     * 
     * @param byteArray byte representation of the string
     * @return Decoding of the given byte array to a String
     */
    public static String decodeByteArray(byte[] byteArray) {
        StringBuilder s = new StringBuilder(byteArray.length);
        for (byte b : byteArray) {
            s.append((char) b);
        }
        return s.toString();
    }



    /**
     * Encode the given string to a byte array
     * 
     * @param string the string to be encoded
     * @return Encoding of the given string to a byte array
     */
    public static byte[] encodeByteArray(String string) {
        byte[] array = new byte[string.length()];
        for (int i = 0; i < array.length; i++) {
            array[i] = (byte) string.charAt(i);
        }
        return array;
    }



    /**
     * Returns an iterable over columns in iStr, separated by one of the characters in iDelimiters. <br>
     * If iEachDelimiterOccurrenceSplitsColumns is true then \t\t44\t\t\t565\t\t defines 8 columns, 6 of them empty:<br>
     * "" \t "" \t 44 \t "" \t "" \t 565 \t "" \t "" <br>
     * If the flag is false, it defines only 2 columns: <br>
     * 44 \t 565 <br>
     * 
     * @param iStr - the input string to be split
     * @param iDelimiters - the list of delimiters. If null, the default delimiter list is used (\r \n \t " ")
     * @param iEachDelimiterOccurrenceSplitsColumns - whether each delimiter splits a column
     * @return an iteration over the split columns
     */
    public static Iterable<String> iterate(String iStr, String iDelimiters,
                    boolean iEachDelimiterOccurrenceSplitsColumns) {
        return new SplitIterator(iStr, iDelimiters, iEachDelimiterOccurrenceSplitsColumns);
    }



    public static ArrayList<String> split(String iStr, String iDelimiters, boolean iEachDelimiterOccurrenceSplitsColumns) {
        ArrayList<String> list = new ArrayList<String>();

        for (String s : iterate(iStr, iDelimiters, iEachDelimiterOccurrenceSplitsColumns))
            list.add(s);

        return list;
    }



    /**
     * Join a list of words into a single string using the specified delimiter.
     * 
     * @param words - array of words to join into a single string.
     * @param iDelimiter - the delimiter to use between each two words.
     * @return A string constructed from the words separated with the delimiter.
     */
    public static String join(List<? extends Object> words, String iDelimiter) {
        StringBuilder str = new StringBuilder();
        if (words.size() == 0) {
            return str.toString();
        }

        int i;
        for (i = 0; i < words.size() - 1; i++) {
            str.append(words.get(i));
            str.append(iDelimiter);
        }
        str.append(words.get(i));

        return str.toString();
    }



    /**
     * Join an array of words into a single string using the specified delimiter.
     * 
     * @param words - array of words to join into a single string.
     * @param iDelimiter - the delimiter to use between each two words.
     * @return A string constructed from the words separated with the delimiter.
     */
    public static String join(String[] words, String iDelimiter) {
        StringBuilder str = new StringBuilder();
        if (words.length == 0) {
            return str.toString();
        }

        int i;
        for (i = 0; i < words.length - 1; i++) {
            str.append(words[i]);
            str.append(iDelimiter);
        }
        str.append(words[i]);

        return str.toString();
    }



    /**
     * Reads a string from the given DataInput. This method assumes that the string is stored as an integer, indicating
     * number of bytes of the string, followed by a byte array which is the string itself, encoded in UTF-8. The method
     * {@link #writeStringAsUTFByteArrayToDataOutput(DataOutput, String)} writes a string into a DataOutput in this way.
     * 
     * @param in the DataInput from which the string will be read
     * @return The string
     * @throws IOException an IO problem.
     */
    public static String readUTFByteArrayFromDataInput(final DataInput in) throws IOException {
        int length = in.readInt();
        byte[] byteArray = new byte[length];
        in.readFully(byteArray);
        return new String(byteArray, StandardCharsets.UTF_8);
    }



    /**
     * Writes a string into a DataOutput. This method writes an integer, which indicates the size of a byte-array that
     * represents the string, followed by that byte array. The byte-array is the string, encoded in UTF-8. The method
     * {@link #readUTFByteArrayFromDataInput(DataInput)} can be used to read such a string from a DataInput.
     * 
     * @param out The DataOutput into which the string will be written.
     * @param str The string to write.
     * @throws IOException an IO problem.
     */
    public static void writeStringAsUTFByteArrayToDataOutput(final DataOutput out, final String str) throws IOException {
        byte[] byteArray = str.getBytes(StandardCharsets.UTF_8);
        out.writeInt(byteArray.length);
        out.write(byteArray);
    }



    /**
     * Trims the given string, such that all non-accepted characters at the beginning and at the end of the string are
     * removed. "non-accepted" characters are defined by the given {@link CharacterAccept} object.<BR>
     * For example, assume that only letters are accepted, and the given string is "1hello2world3", then the returned
     * string would be "hello2world".
     * 
     * @param str The input string.
     * @param characterAccept Defines which characters are accepted, and which are not (and should be trimmed).
     * @return The given string, where non-accepted characters at its beginning and end are trimmed.
     */
    public static String trimByCharacterAccept(String str, CharacterAccept characterAccept) {
        if (null == str) {
            return str;
        }
        char[] array = str.toCharArray();
        int start = 0;
        int end = array.length;

        boolean startAcceptDetected = false;
        int index = 0;
        while ((index < array.length) && (!startAcceptDetected)) {
            startAcceptDetected = characterAccept.accept(array[index]);
            ++index;
        }
        if (startAcceptDetected) {
            start = index - 1;
        } else {
            start = index; // which is array.length
        }

        boolean endAcceptDetected = false;
        index = array.length;
        while ((index > start) && (!endAcceptDetected)) {
            end = index;
            --index;
            endAcceptDetected = characterAccept.accept(array[index]);
        }
        if (!endAcceptDetected) {
            end = 0;
        }

        if (start < end) {
            return str.substring(start, end);
        } else {
            return "";
        }
    }



    /**
     * Trims characters that are neither letters nor digits which appear at the beginning and the end of the given
     * string. <BR>
     * For example, for "$hello%world^", the returned string would be "hello%world".
     * 
     * @param str The input string.
     * @return The given string, where leading and trailing non-letter and non-digits character are trimmed.
     */
    public static String trimNeitherLetterNorDigit(final String str) {
        return trimByCharacterAccept(str, new CharacterAccept() {
            @Override
            public boolean accept(char c) {
                return Character.isLetterOrDigit(c);
            }
        });
    }



    /**
     * Generates a string of the given length, where all of its characters are the given character.
     * 
     * @param character A given character
     * @param length Size of the returned string
     * @return a string of the given length, where all of its characters are the given character.
     */
    public static String generateStringOfCharacter(char character, int length) {
        if (length <= 0)
            return "";

        char[] array = new char[length];
        Arrays.fill(array, character);
        return new String(array);
    }

    /**
     * Finds the first sequence of N or more repeating character. <BR>
     * For example, for "abbcccdddd", the first char sequence of length 3 is "ccc".
     * 
     * @param str The input string.
     * @param seqLen The minimal sequence length.
     * @return The index where the found sequence starts, or "-1" if no sequence found.
     */
    public static int indexOfRepeatingCharSequence(String str, int seqLen) {
        if (str == null || seqLen < 1 || str.length() < seqLen) {
            return -1;
        }

        char[] cArr = str.toCharArray();
        char candidateChar = cArr[0];
        int foundSeqLen = 0;
        int candidateSeqStartIdx = 0;
        for (int i = 0; i < cArr.length; i++) {
            if (cArr[i] == candidateChar) {
                // Continue sequence candidate
                foundSeqLen++;
                if (foundSeqLen >= seqLen) {
                    return candidateSeqStartIdx;
                }
            } else {
                // New sequence candidate;
                candidateChar = cArr[i];
                candidateSeqStartIdx = i;
                foundSeqLen = 1;
            }
        }

        // No sequence found
        return -1;
    }



    /**
     * Computes the Levenshtein distance between two strings.
     * <P>
     * <B>Note:</B> A similar method exists in ML project:
     * yr.haifa.ML.edit_distance.StringEditDistance.computeLevenshteinDistance(). However, the implementation here is
     * much faster, and is recommended. It has been found that on a typical string of 200 characters length, the
     * implementation given here is about 18 times faster.
     * <P>
     * Levenshtein distance is the number of insertion/substitution/deletion (operated on characters) that should be
     * performed to one string in order to make it identical to the other string.
     * 
     * @param firstString a string
     * @param secondString another string
     * @param caseSensitive whether the upper/lower case is taken into consideration when calculating the distance.
     *        <tt>true</tt> means that strings are case-sensitive. <tt>false</tt> means that strings are case
     *        insensitive, i.e., "abc" equals "ABC".
     * @return The Levenshtein distance between the two strings.
     * @throws RuntimeException
     */
    public static long computeLevenshteinDistance(final String firstString, final String secondString,
                    final boolean caseSensitive) throws RuntimeException {
        String firstStringInAlgorithm;
        String secondStringInAlgorithm;
        if (firstString == null) {
            throw new RuntimeException("first string is null.");
        }
        if (secondString == null) {
            throw new RuntimeException("second string is null.");
        }

        if (caseSensitive) {
            firstStringInAlgorithm = firstString;
            secondStringInAlgorithm = secondString;
        } else {
            firstStringInAlgorithm = firstString.toLowerCase();
            secondStringInAlgorithm = secondString.toLowerCase();
        }
        return computeDistanceImpl(firstStringInAlgorithm, secondStringInAlgorithm);
    }



    /**
     * Algorithm: <BR>
     * (terms: s = firstString. t = secondString)<BR>
     * 1<BR>
     * Set n to be the length of s.<BR>
     * Set m to be the length of t.<BR>
     * If n = 0, return m and exit.<BR>
     * If m = 0, return n and exit.<BR>
     * Construct a matrix containing 0..m rows and 0..n columns.<BR>
     * 2<BR>
     * Initialize the first row to 0..n.<BR>
     * Initialize the first column to 0..m.<BR>
     * 3<BR>
     * Examine each character of s (i from 1 to n)<BR>
     * 4<BR>
     * Examine each character of t (j from 1 to m)<BR>
     * 5<BR>
     * If s[i] equals t[j], the cost is 0.<BR>
     * If s[i] doesn't equal t[j], the cost is 1.<BR>
     * 6<BR>
     * Set cell d[i,j] of the matrix equal to the minimum of:<BR>
     * a. The cell immediately above plus 1: d[i-1,j] + 1.<BR>
     * b. The cell immediately to the left plus 1: d[i,j-1] + 1.<BR>
     * c. The cell diagonally above and to the left plus the cost: d[i-1,j-1] + cost.<BR>
     * 7<BR>
     * After the iteration steps (3, 4, 5, 6) are complete, the distance is found in cell d[n,m].<BR>
     * 
     * @return the distance
     * @throws RuntimeException illegal strings
     */
    private static long computeDistanceImpl(final String firstStringInAlgorithm, final String secondStringInAlgorithm)
                    throws RuntimeException {
        int firstStringLength = firstStringInAlgorithm.length();
        int secondStringLength = secondStringInAlgorithm.length();

        long matrix[][]; // matrix

        // Step 1
        if (firstStringLength == 0) {
            return secondStringLength;
        }
        if (secondStringLength == 0) {
            return firstStringLength;
        }
        matrix = new long[firstStringLength + 1][secondStringLength + 1];

        // Step 2
        for (int firstStringIndex = 0; firstStringIndex <= firstStringLength; firstStringIndex++) {
            matrix[firstStringIndex][0] = firstStringIndex;
        }

        for (int secondStringIndex = 0; secondStringIndex <= secondStringLength; secondStringIndex++) {
            matrix[0][secondStringIndex] = secondStringIndex;
        }

        // Step 3
        for (int firstStringIndex = 1; firstStringIndex <= firstStringLength; firstStringIndex++) {

            char firstStringChar = firstStringInAlgorithm.charAt(firstStringIndex - 1);

            // Step 4
            for (int secondStringIndex = 1; secondStringIndex <= secondStringLength; secondStringIndex++) {

                char secondStringChar = secondStringInAlgorithm.charAt(secondStringIndex - 1);
                long cost = 0;

                // Step 5
                if (firstStringChar == secondStringChar) {
                    cost = 0;
                } else {
                    cost = 1;
                }

                // Step 6
                matrix[firstStringIndex][secondStringIndex] =
                                minOfThree(matrix[firstStringIndex - 1][secondStringIndex] + 1,
                                                matrix[firstStringIndex][secondStringIndex - 1] + 1,
                                                matrix[firstStringIndex - 1][secondStringIndex - 1] + cost);

            }
        }

        // Step 7
        return matrix[firstStringLength][secondStringLength];
    }



    private static final long minOfThree(final long a, final long b, final long c) {
        long minimum;
        minimum = a;
        if (b < minimum) {
            minimum = b;
        }
        if (c < minimum) {
            minimum = c;
        }
        return minimum;
    }
}
