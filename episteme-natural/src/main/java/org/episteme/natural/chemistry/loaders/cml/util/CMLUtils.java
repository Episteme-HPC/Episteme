/*
 * Episteme - Java(TM) Tools and Libraries for the Advancement of Sciences.
 * Copyright (C) 2025-2026 - Silvere Martin-Michiellot and Gemini AI (Google DeepMind)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package org.episteme.natural.chemistry.loaders.cml.util;


import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * A number of miscellaneous tools. Originally devised for jumbo.sgml, now
 * rewritten for jumbo.xml. Use these at your peril - some will be phased out
 *
 * @author Silvere Martin-Michiellot
 * @author Gemini AI (Google DeepMind)
 * @since 1.0
 */

public class CMLUtils {

    /**
     * Description of the Field
     */
    public final static String FORMAT_ASCII = "ASCII";
    /**
     * Description of the Field
     */
    public final static String FORMAT_DOS = "DOS";
    /**
     * Description of the Field
     */
    public final static String FORMAT_EQUALS = "EQUALS";

    /**
     * Description of the Field
     */
    public final static String SPACE = " ";
    /**
     * Description of the Field
     */
    public final static String TAB = "\t";
    /**
     * Description of the Field
     */
    public final static String RETURN = "\r";
    /**
     * Description of the Field
     */
    public final static String NEWLINE = "\n";
    /**
     * Description of the Field
     */
    public final static String FORMFEED = "\f";
    /**
     * Description of the Field
     */
    public final static String WHITESPACE = SPACE
            + TAB
            + RETURN
            + NEWLINE
            + FORMFEED;

    /**
     * Description of the Field
     */
    public final static String LBRAK = "(";
    /**
     * Description of the Field
     */
    public final static String RBRAK = ")";
    /**
     * Description of the Field
     */
    public final static String SHRIEK = "!";
    /**
     * Description of the Field
     */
    public final static String QUOT = "\"";
    /**
     * Description of the Field
     */
    public final static String POUND = "ï¿½";
    /**
     * Description of the Field
     */
    public final static String DOLLAR = "$";
    /**
     * Description of the Field
     */
    public final static String PERCENT = "%";
    /**
     * Description of the Field
     */
    public final static String CARET = "^";
    /**
     * Description of the Field
     */
    public final static String AMP = "&";
    /**
     * Description of the Field
     */
    public final static String STAR = "*";
    /**
     * Description of the Field
     */
    public final static String UNDER = "_";
    /**
     * Description of the Field
     */
    public final static String MINUS = "-";
    /**
     * Description of the Field
     */
    public final static String PLUS = "+";
    /**
     * Description of the Field
     */
    public final static String EQUALS = "=";
    /**
     * Description of the Field
     */
    public final static String LCURLY = "{";
    /**
     * Description of the Field
     */
    public final static String RCURLY = "}";
    /**
     * Description of the Field
     */
    public final static String LSQUARE = "[";
    /**
     * Description of the Field
     */
    public final static String RSQUARE = "]";
    /**
     * Description of the Field
     */
    public final static String TILDE = "~";
    /**
     * Description of the Field
     */
    public final static String HASH = "#";
    /**
     * Description of the Field
     */
    public final static String COLON = ":";
    /**
     * Description of the Field
     */
    public final static String SEMICOLON = ";";
    /**
     * Description of the Field
     */
    public final static String ATSIGN = "@";
    /**
     * Description of the Field
     */
    public final static String APOS = "'";
    /**
     * Description of the Field
     */
    public final static String COMMA = ",";
    /**
     * Description of the Field
     */
    public final static String PERIOD = ".";
    /**
     * Description of the Field
     */
    public final static String SLASH = "/";
    /**
     * Description of the Field
     */
    public final static String QUERY = "?";
    /**
     * Description of the Field
     */
    public final static String LANGLE = "<";
    /**
     * Description of the Field
     */
    public final static String RANGLE = ">";
    /**
     * Description of the Field
     */
    public final static String PIPE = "|";
    /**
     * Description of the Field
     */
    public final static String BACKSLASH = "\\";

    /**
     * Description of the Field
     */
    public final static String NONWHITEPUNC =
            LBRAK
                    + RBRAK
                    + SHRIEK
                    + QUOT
                    + POUND
                    + DOLLAR
                    + PERCENT
                    + CARET
                    + AMP
                    + STAR
                    + UNDER
                    + MINUS
                    + PLUS
                    + EQUALS
                    + LCURLY
                    + RCURLY
                    + LSQUARE
                    + RSQUARE
                    + TILDE
                    + HASH
                    + COLON
                    + SEMICOLON
                    + ATSIGN
                    + APOS
                    + COMMA
                    + PERIOD
                    + SLASH
                    + QUERY
                    + LANGLE
                    + RANGLE
                    + PIPE
                    + BACKSLASH;

    /**
     * Description of the Field
     */
    public final static String PUNC = WHITESPACE + NONWHITEPUNC;

    // XML stuff
    // start <FOO>
    /**
     * Description of the Field
     */
    public final static String X_STAGO = LANGLE;
    /**
     * Description of the Field
     */
    public final static String X_STAGC = RANGLE;
    // end </FOO>
    /**
     * Description of the Field
     */
    public final static String X_ETAGO = LANGLE + SLASH;
    /**
     * Description of the Field
     */
    public final static String X_ETAGC = RANGLE;
    // empty <FOO/>
    /**
     * Description of the Field
     */
    public final static String X_EMPTAGO = LANGLE;
    /**
     * Description of the Field
     */
    public final static String X_EMPTAGC = SLASH + RANGLE;

    // start def <!
    /**
     * Description of the Field
     */
    public final static String X_STARTDEF = LANGLE + SHRIEK;
    // comment
    /**
     * Description of the Field
     */
    public final static String X_COMMENTO = X_STARTDEF + MINUS + MINUS;
    /**
     * Description of the Field
     */
    public final static String X_COMMENTC = MINUS + MINUS + RANGLE;
    // entity	definition <!ENTITY ...> (note following space)
    /**
     * Description of the Field
     */
    public final static String X_ENTDEFO = X_STARTDEF + "ENTITY" + SPACE;
    /**
     * Description of the Field
     */
    public final static String X_ENTDEFC = RANGLE;
    // parameter entity	definition <!ENTITY % ...> (note following space)
    /**
     * Description of the Field
     */
    public final static String X_PARAMENTDEFO = X_STARTDEF + "ENTITY" + SPACE + PERCENT + SPACE;
    /**
     * Description of the Field
     */
    public final static String X_PARAMENTDEFC = RANGLE;
    // element	definition <!ELEMENT ...> (note following space)
    /**
     * Description of the Field
     */
    public final static String X_ELEMDEFO = X_STARTDEF + "ELEMENT" + SPACE;
    /**
     * Description of the Field
     */
    public final static String X_ELEMDEFC = RANGLE;
    // param entity	dereference	" %foo; " (note spaces)
    /**
     * Description of the Field
     */
    public final static String X_PARAMENTO = SPACE + PERCENT;
    /**
     * Description of the Field
     */
    public final static String X_PARAMENTC = SEMICOLON + SPACE;
    // general entity dereference  &foo;
    /**
     * Description of the Field
     */
    public final static String X_GENENTO = AMP;
    /**
     * Description of the Field
     */
    public final static String X_GENENTC = SEMICOLON;
    // doctype <!DOCTYPE
    /**
     * Description of the Field
     */
    public final static String X_DOCTYPEO = X_STARTDEF + "DOCTYPE" + SPACE;
    /**
     * Description of the Field
     */
    public final static String X_DOCTYPEE = RANGLE;

    /**
     * general code for unset or unknown variables
     */
    public final static int UNKNOWN = -1;

    /**
     * case sensitivity flags - used throughout jumbo.xml
     */
    public final static int CASE = 0;
    /**
     * Description of the Field
     */
    public final static int IGNORECASE = 1;

    // default o/p stream
    static boolean stdout = true;
    static boolean stderr = true;

    static String blank20 = "                    ";

    /**
     * lookahead for bufferedReader
     */
    public final static int BR_LOOKAHEAD = 10000;

    /**
     *  offers a prompt on stdout and reads the input until CR
     *
     *@param  file             Description of the Parameter
     *@param  deleteDirectory  Description of the Parameter
     *@return Description of the Return Value
     */

    /**
     * delete a file If directory==true then file will be recursively deleted
     *
     * @param file            Description of the Parameter
     * @param deleteDirectory Description of the Parameter
     * @return Description of the Return Value
     */
    public static boolean deleteFile(File file, boolean deleteDirectory) {
        if (file.exists()) {
            if (file.isDirectory() && deleteDirectory) {
                String[] filenames = file.list();
                for (int i = 0; i < filenames.length; i++) {
                    File childFile = new File(file.toString() + File.separator + filenames[i]);
                    deleteFile(childFile, deleteDirectory);
                }
            }
            return file.delete();
        } else {
            return false;
        }
    }

    /**
     * copy one file to another (I suspect there is a better way
     *
     * @param inFile  Description of the Parameter
     * @param outFile Description of the Parameter
     * @throws FileNotFoundException Description of the Exception
     * @throws IOException           Description of the Exception
     */
    public static void copyFile(File inFile, File outFile)
            throws FileNotFoundException, IOException {
        try (BufferedInputStream bis = new BufferedInputStream(new FileInputStream(inFile));
             BufferedOutputStream bos = new BufferedOutputStream(new FileOutputStream(outFile))) {
            bis.transferTo(bos);
        }
    }

    /**
     * reads a stream from url and outputs it as integer values of the
     * characters and as strings. Emulates UNIX od().
     *
     * @param url Description of the Parameter
     * @return String tabular version of input (in 10-column
     *         chunks)
     * @throws Exception Description of the Exception
     */
    public static String dump(URL url) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(url.openStream()));
        int count = 0;
        StringBuffer sb = new StringBuffer();
        String s0 = "\n";
        String s1 = "";
        while (true) {

            int i = br.read();
            if (i == -1) {
                break;
            }
            String s = "   " + i;
            while (s.length() > 4) {
                s = s.substring(1);
            }
            s0 += s;
            if (i >= 32 && i < 128) {
                s1 += (char) i;
            } else {
                s1 += " ";
            }
            if (++count % 10 == 0) {
                sb.append(s0 + "   " + s1);
                s1 = "";
                s0 = "\n";
            }
        }
        if (count != 0) {
            sb.append(s0 + "   " + s1);
        }
        return sb.toString();
    }

    /**
     * output String and flush()
     *
     * @param s Description of the Parameter
     */
    public static void flush(String s) {
        System.out.println(s);
        System.out.flush();
    }

    /**
     * make a String of a given number of spaces
     *
     * @param nspace Description of the Parameter
     * @return Description of the Return Value
     */
    public static String spaces(int nspace) {
        if (nspace <= 0) {
            return "";
        } else if (nspace <= 20) {
            return blank20.substring(0, nspace);
        } else {
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < nspace; i++) {
                sb.append(" ");
            }
            return sb.toString();
        }
    }

    /**
     * gets suffix from filename
     *
     * @param filename Description of the Parameter
     * @return The suffix value
     */
    public static String getSuffix(String filename) {
        int idx = filename.lastIndexOf(CMLUtils.PERIOD);
        if (idx == -1) {
            return null;
        }
        return filename.substring(idx + 1, filename.length());
    }

    /**
     * skip white lines and end with first non-white line Leaves bReader ready
     * to read first non-white line
     *
     * @param bReader Description of the Parameter
     * @return int number of lines skipped
     * @throws Exception Description of the Exception
     */
    public static int skipWhite(BufferedReader bReader) throws Exception {
        int count = 0;
        while (true) {
            bReader.mark(CMLUtils.BR_LOOKAHEAD);
            String line = bReader.readLine();
            if (line == null || !line.trim().equals("")) {
                bReader.reset();
                return count;
            }
            ++count;
        }
    }

    /**
     * return the first n characters of a string and add ellipses if truncated
     *
     * @param s         Description of the Parameter
     * @param maxlength Description of the Parameter
     * @return String the (possibly) truncated string
     */
    public static String truncate(String s, int maxlength) {
        if (s == null) {
            return null;
        }

        int l = s.length();
        return (l <= maxlength) ? s : s.substring(0, maxlength) + " ... ";
    }

    /**
     * a list of the first few Roman numerals (for example for chapters)
     */
    public static String[] ROMAN_NUMERALS = {
            null, "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX",
            "X", "XI", "XII", "XIII", "XIV", "XV", "XVI", "XVII", "XVIII", "XIX",
            "XX", "XXI", "XXII", "XXIII", "XXIV", "XXV", "XXVI", "XXVII", "XXVIII", "XXIX",
            "XXX", "XXXI", "XXXII", "XXXIII", "XXXIV", "XXXV", "XXXVI", "XXXVII", "XXXVIII", "XXXIX",
            "XL", "XLI", "XLII", "XLIII", "XLIV", "XLV", "XLVI", "XLVII", "XLVIII", "XLIX", "L",
    };

    /**
     * translate Roman Numerals up to 50 Some normalisation is performed
     * Failure returns -1
     *
     * @param roman Description of the Parameter
     * @return The integerFromRoman value
     */
    public static int getIntegerFromRoman(String roman) {
        if (roman == null) {
            return -1;
        }
// normalise terminal "IIII", etc.
        String[] oldStrings = {"VIIII", "IIII", "LXXXX", "XXXX"};
        String[] newStrings = {"IX", "IV", "XC", "XL"};

        roman = CMLUtils.substituteStrings(roman, oldStrings, newStrings);
        return CMLUtils.indexOf(roman, ROMAN_NUMERALS, CMLUtils.CASE);
    }

    /**
     * add a property to the System ones Don't know if this is a good idea...
     *
     * @param property The new systemProperty value
     * @param value    The new systemProperty value
     */
    public static void setSystemProperty(String property, String value) {
        if (property == null || value == null) {
            return;
        }
        Properties properties = System.getProperties();
        properties.put(property, value);
        System.setProperties(properties);
    }

    /**
     * load a file/url into the system properties,
     *
     * @param urlString The feature to be added to the ToSystemProperties
     *                  attribute
     * @throws IOException Description of the Exception
     */
    public static void addToSystemProperties(String urlString) throws IOException {
        if (urlString == null) {
            return;
        }
        InputStream is = java.net.URI.create(CMLUtils.makeAbsoluteURL(urlString)).toURL().openStream();
        Properties sysProps = System.getProperties();
        sysProps.load(is);
        System.setProperties(sysProps);
    }

    static Hashtable<String, Class<?>> classTable = new Hashtable<>();

    /**
     * gets a new instance of a class from a hashtable because normal methods
     * are very slow
     *
     * @param className Description of the Parameter
     * @return The newInstance value
     * @throws Exception Description of the Exception
     */
    public static Object getNewInstance(String className) throws Exception {
        if (className == null) {
            return null;
        }
        Class<?> theClass = classTable.get(className);
        if (theClass == null) {
            theClass = Class.forName(className);
            classTable.put(className, theClass);
        }
        return theClass.getDeclaredConstructor().newInstance();
    }

    /**
     * remove balanced quotes from ends of (trimmed) string, else no action
     *
     * @param s Description of the Parameter
     * @return Description of the Return Value
     */
    public static String deQuote(String s) {
        if (s == null) {
            return null;
        }
        String ss = s.trim();
        if (ss.equals("")) {
            return ss;
        }
        char c = ss.charAt(0);
        if (c == '"' || c == '\'') {
            int l = ss.length();
            if (ss.charAt(l - 1) == c) {
                return ss.substring(1, l - 1);
            }
        }
        return s;
    }

    /**
     * remove trailing blanks
     *
     * @param s Description of the Parameter
     * @return Description of the Return Value
     */
    public static String rightTrim(String s) {
        if (s == null) {
            return null;
        }
        if (s.trim().equals("")) {
            return "";
        }
        int l = s.length();
        while (l >= 0) {
            if (s.charAt(--l) != ' ') {
                l++;
                break;
            }
        }
        return s.substring(0, l);
    }

    /**
     * remove leading blanks
     *
     * @param s Description of the Parameter
     * @return Description of the Return Value
     */
    public static String leftTrim(String s) {
        if (s == null) {
            return null;
        }
        if (s.trim().equals("")) {
            return "";
        }
        int l = s.length();
        for (int i = 0; i < l; i++) {
            if (s.charAt(i) != ' ') {
                return s.substring(i);
            }
        }
        return s;
    }

    /**
     * return index of balanced bracket -1 for none. String MUST start with '('
     *
     * @param lbrack Description of the Parameter
     * @param s      Description of the Parameter
     * @return Description of the Return Value
     */
    public static int indexOfBalancedBracket(char lbrack, String s) {
        if (s == null) {
            return -1;
        }
        if (s.charAt(0) != lbrack) {
            return -1;
        }
        char rbrack = ' ';
        if (lbrack == '(') {
            rbrack = ')';
        } else if (lbrack == '<') {
            rbrack = '>';
        } else if (lbrack == '[') {
            rbrack = ']';
        } else if (lbrack == '{') {
            rbrack = '}';
        }
        int l = s.length();
        int i = 0;
        int level = 0;
        while (i < l) {
            if (s.charAt(i) == lbrack) {
                level++;
            } else if (s.charAt(i) == rbrack) {
                level--;
                if (level == 0) {
                    return i;
                }
            }
            i++;
        }
        return -1;
    }

    /**
     * parse comma-separated Strings Note fields can be "" (as in ,,,) and
     * fields can be quoted "...". If so, embedded quotes are represented as
     * "", for example A," this is a ""B"" character",C. An unbalanced quote returns a
     * mess
     *
     * @param s Description of the Parameter
     * @return Vector the vector of Strings - any error returns
     *         null
     * @throws Exception Description of the Exception
     */
    public static Vector<String> getCommaSeparatedStrings(String s) throws Exception {
        if (s == null) {
            return null;
        }
        String s0 = s;
        s = s.trim();
        Vector<String> v = new Vector<>();
        while (!s.equals("")) {
            if (s.startsWith(CMLUtils.QUOT)) {
                String temp = "";
                s = s.substring(1);
                while (true) {
                    int idx = s.indexOf(CMLUtils.QUOT);
                    if (idx == -1) {
                        throw new Exception("Missing Quote:" + s0 + ":");
                    }
                    int idx2 = s.indexOf(CMLUtils.QUOT + CMLUtils.QUOT);
// next quote is actually ""
                    if (idx2 == idx) {
                        temp += s.substring(0, idx) + CMLUtils.QUOT;
                        s = s.substring(idx + 2);
// single quote
                    } else {
                        temp += s.substring(0, idx);
                        s = s.substring(idx + 1);
                        break;
                    }
                }
                v.addElement(temp);
                if (s.startsWith(CMLUtils.COMMA)) {
                    s = s.substring(1);
                } else if (s.equals("")) {
                } else {
                    throw new Exception("Unbalanced Quotes:" + s0 + ":");
                }
            } else {
                int idx = s.indexOf(CMLUtils.COMMA);
// end?
                if (idx == -1) {
                    v.addElement(s);
                    break;
                } else {
// another comma
                    String temp = s.substring(0, idx);
                    v.addElement(temp);
                    s = s.substring(idx + 1);
                    if (s.equals("")) {
                        v.addElement(s);
                        break;
                    }
                }
            }
        }
        return v;
    }

    /**
     * create comma-separated Strings fields include a comma or a " they are
     * wrapped with quotes ("). Note fields can be "" (as in ,,,) and fields
     * can be quoted "...". If so, embedded quotes are represented as "", for example
     * A," this is a ""B"" character",C.
     *
     * @param Vector  v vector of strings to be concatenated (null returns null)
     * @return String the concatenated string - any error returns null
     */
    static String[] _QUOT = {CMLUtils.QUOT};
    static String[] _QUOT2 = {CMLUtils.QUOT + CMLUtils.QUOT};

    /**
     * Description of the Method
     *
     * @param v Description of the Parameter
     * @return Description of the Return Value
     * @throws Exception Description of the Exception
     */
    public static String createCommaSeparatedStrings(Vector<?> v) throws Exception {
        if (v == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < v.size(); i++) {
            String s = v.elementAt(i).toString();
            s = CMLUtils.substituteStrings(s, _QUOT, _QUOT2);
// wrap in quotes to escape comma or other quotes
            if (s.indexOf(CMLUtils.COMMA) != -1 || s.indexOf(CMLUtils.QUOT) != -1) {
                s = CMLUtils.QUOT + s + CMLUtils.QUOT;
            }
            if (i > 0) {
                sb.append(CMLUtils.COMMA);
            }
            sb.append(s);
        }
        return sb.toString();
    }

    /**
     * tokenize the string including adjacent delimiters (for example "foo$$bar$", "$"
     * would contain the tokens "foo", "", "bar" and "")
     *
     * @param s     Description of the Parameter
     * @param delim Description of the Parameter
     * @return Description of the Return Value
     */
    public static Vector<String> alternativeStringTokenizer(String s, char delim) {
        Vector<String> vector = new Vector<>();
        while (true) {
            int idx = s.indexOf(delim);
            if (idx == -1) {
                vector.addElement(s);
                break;
            }
            vector.addElement(s.substring(0, idx));
            s = s.substring(idx + 1);
        }
        return vector;
    }

    /**
     * parse whitespace-separated tokens interspersed with quoted strings, for example
     * <BR>
     * this is "a quoted string" and 'another token' as well<BR>
     * parses to: <BR>
     * this/is/a quoted string/and/another token/as well <BR>
     *
     * @param s Description of the Parameter
     * @return Vector of strings (size = 0 if s is whitespace);
     */
    public static Vector<String> parseWhitespaceQuotedFields(String s) {
        String delim = CMLUtils.QUOT + CMLUtils.APOS + CMLUtils.WHITESPACE;
        StringTokenizer st = new StringTokenizer(s, delim, true);
        Vector<String> vector = new Vector<>();
        while (st.hasMoreTokens()) {
            String element = null;
            String token = st.nextToken(delim);
            if (Character.isWhitespace(token.charAt(0))) {
                continue;
            } else if (token.equals(CMLUtils.QUOT) || token.equals(CMLUtils.APOS)) {
                element = st.nextToken(token);
                st.nextToken(token);
            } else {
                element = token;
            }
            vector.addElement(element);
        }
        return vector;
    }

    /**
     * concatenate strings into quote-separated string
     *
     * @param s Description of the Parameter
     * @return String concatenated string
     */
    public static String quoteConcatenate(String[] s) {
        StringBuffer sb = new StringBuffer();

        for (int i = 0; i < s.length; i++) {
            if (i > 0) {
                sb.append(" ");
            }
            boolean quote = false;
            if (s[i].indexOf(" ") != -1) {
                sb.append("\"");
                quote = true;
            }
            sb.append(s[i]);
            if (quote) {
                sb.append("\"");
            }
        }

        return sb.toString();
    }

    /**
     * splits a whitespace-separated set of tokens into a String[]
     *
     * @param s Description of the Parameter
     * @return String[] result of splitting (null if s==null)
     */
    public static String[] split(String s) {
        if (s == null) {
            return null;
        }
        StringTokenizer st = new StringTokenizer(s, " \n\t\r");
        String[] out = new String[st.countTokens()];
        int count = 0;
        while (st.hasMoreTokens()) {
            out[count++] = (String) st.nextToken();
        }
        return out;
    }

    /**
     * get the index of a String in an array
     *
     * @param string          Description of the Parameter
     * @param strings         Description of the Parameter
     * @param caseSensitivity Description of the Parameter
     * @return index of string else -1 if not found
     */
    public static int indexOf(String string, String[] strings, int caseSensitivity) {
        if (string == null || strings == null) {
            return -1;
        }
        for (int i = 0; i < strings.length; i++) {
            if (caseSensitivity == IGNORECASE) {
                if (string.equalsIgnoreCase(strings[i])) {
                    return i;
                }
            } else {
                if (string.equals(strings[i])) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * convenience function for comparing strings using CMLUtils.CASE/IGNORECASE
     *
     * @param string1     Description of the Parameter
     * @param string2     Description of the Parameter
     * @param sensitivity Description of the Parameter
     * @return boolean true if IGNORECASE ans
     *         string1.equalsIgnoreCase(string2) or string1.equals(string2)
     */
    public static boolean equals(String string1, String string2, int sensitivity) {
        return (
                (sensitivity == IGNORECASE && string1.equalsIgnoreCase(string2)) ||
                        string1.equals(string2));
    }

    /**
     * remove balanced (well-formed) markup from a string. Crude (that is  not
     * fully XML-compliant);</BR> Example: "This is &lt;A
     * HREF="foo"&gt;bar&lt;/A&gt; and &lt;/BR&gt; a break" goes to "This is
     * bar and a break"
     *
     * @param s Description of the Parameter
     * @return Description of the Return Value
     */
    public static String removeHTML(String s) {
        int idx = s.indexOf("<");
        if (idx == -1) {
            return s;
        }
        if (s.startsWith("<")) {
            idx = s.indexOf(">");
            s = s.substring(idx + 1);
        }
        StringTokenizer st = new StringTokenizer(s);
        String ss = "";
        while (st.hasMoreTokens()) {
            ss += st.nextToken("<");
            if (!st.hasMoreTokens()) {
                break;
            }
            st.nextToken(">");
        }
        return ss;
    }

    /**
     * Warning message - nothing fancy at present
     *
     * @param s Description of the Parameter
     */
    public static void warning(String s) {
        System.out.println("WARNING: " + s);
    }

    /**
     * message - nothing fancy at present
     *
     * @param s Description of the Parameter
     */
    public static void message(String s) {
        System.out.println(s);
    }


    //	static jumbo.xml.gui.XText errorText;
    /**
     * Error message - nothing fancy at present. Display in Text frame
     *
     * @param s Description of the Parameter
     */
    public static void error(String s) {
//		if (errorText == null) {
//			errorText = new jumbo.xml.gui.XText();
//			errorText.displayInFrame();
//		}
        System.out.println("ERROR: " + s);
//		errorText.addText(s);
    }

    /**
     * record that we have hit a program bug!!!
     *
     * @param s Description of the Parameter
     */
    public static void bug(String s) {
        bug(new Exception(s));
    }

    /**
     * Description of the Method
     *
     * @param e Description of the Parameter
     */
    public static void bug(Exception e) {
        System.err.println
                ("SERIOUS PROGRAM ERROR! - please report the following:");
        e.printStackTrace(System.err);
    }


    static String FS = System.getProperty("file.separator");

    /**
     * create new file, including making directory if required This seems to be
     * a mess - f.createNewFile() doesn't seem to work A directory should have
     * a trailing file.separator
     *
     * @param fileName Description of the Parameter
     * @return Description of the Return Value
     * @throws IOException Description of the Exception
     */
    public static File createNewFile(String fileName) throws IOException {
        File f = null;
        String path = null;

        int idx = fileName.lastIndexOf(FS);
        if (idx != -1) {
            path = fileName.substring(0, idx);
//			fileN = fileName.substring(idx+1);
        }

//        try {
        if (path != null) {
            f = new File(path);
            f.mkdirs();
        }
        if (!fileName.endsWith(FS)) {
            f = new File(fileName);
            f.createNewFile();
        }
//		} catch (IOException e) {
//			System.out.println("Failed to create: "+fileName+"("+e+")");
//		}
        return f;
    }


    /*
    *  --
    *  public static void createPath(String fileName) throws IOException {
    *  String path = "";
    *  StringTokenizer st = new StringTokenizer(fileName, FS);
    *  int nTokens = st.countTokens();
    *  / discard filename
    *  for (int i = 0; i < nTokens-1; i++) {
    *  path += st.nextToken()+FS;
    *  System.out.println("Made path: "+path);
    *  / seems to require a filename
    *  File f = new File(path);
    *  f.mkdir();
    *  f.createNewFile();
    *  }
    *  }
    *  --
    */
    /**
     * get current directory
     *
     * @return The pWDName value
     */
    public static String getPWDName() {
        File f = new File(".");
        return new File(f.getAbsolutePath()).getParent();
    }

    /**
     * make substitutions in a string. If oldSubtrings = "A" and newSubstrings
     * = "aa" then count occurrences of "A" in s are replaced with "aa", etc.
     * "AAA" count=2 would be replaced by "aaaaA"
     *
     * @param s            Description of the Parameter
     * @param oldSubstring Description of the Parameter
     * @param newSubstring Description of the Parameter
     * @param count        Description of the Parameter
     * @return Description of the Return Value
     */
    public static String substituteString
            (String s, String oldSubstring, String newSubstring, int count) {
        if (count <= 0) {
            count = Integer.MAX_VALUE;
        }
        StringBuffer sb = new StringBuffer();
        int lo = oldSubstring.length();
        for (int i = 0; i < count; i++) {
            int idx = s.indexOf(oldSubstring);
            if (idx == -1) {
                break;
            }
            sb.append(s.substring(0, idx));
            sb.append(newSubstring);
            s = s.substring(idx + lo);
        }
        sb.append(s);
        return sb.toString();
    }

    /**
     * make substitutions in a string. If oldSubtrings = {"A", "BB", "C"} and
     * newSubstrings = {"aa", "b", "zz"} then every occurrence of "A" in s is
     * replaced with "aa", etc. "BBB" would be replaced by "bB"
     *
     * @param s             Description of the Parameter
     * @param oldSubstrings Description of the Parameter
     * @param newSubstrings Description of the Parameter
     * @return Description of the Return Value
     */
    public static String substituteStrings
            (String s, String[] oldSubstrings, String[] newSubstrings) {
        if (oldSubstrings.length != newSubstrings.length) {
            throw new IllegalArgumentException("CMLUtils.substituteStrings  arguments of different lengths");
        }
        for (int i = 0; i < oldSubstrings.length; i++) {
            String oldS = oldSubstrings[i];
            String newS = newSubstrings[i];
            int lo = oldS.length();
            if (s.indexOf(oldS) == -1) {
                continue;
            }
            String ss = "";
            while (true) {
                int idx = s.indexOf(oldS);
                if (idx == -1) {
                    ss += s;
                    break;
                }
                ss += s.substring(0, idx) + newS;
                s = s.substring(idx + lo);
            }
            s = ss;
        }
        return s;
    }

    /**
     * substitute characters with =Hex values. Thus "=2E" is translated to
     * char(46); A trailing EQUALS (continuation line is not affected, nor is
     * any non-hex value
     *
     * @param String  s input string
     * @return String output.
     */

    static String[] dosEquivalents = {
            "" + (char) 12,
            // ??
            "" + (char) 127,
            // ??
            "" + (char) 128,
            // Ccedil
            "" + (char) 129,
            // uuml
            "" + (char) 130,
            // eacute
            "" + (char) 131,
            // acirc
            "" + (char) 132,
            // auml
            "" + (char) 133,
            // agrave
            "" + (char) 134,
            // aring
            "" + (char) 135,
            // ccedil
            "" + (char) 136,
            // ecirc
            "" + (char) 137,
            // euml
            "" + (char) 138,
            // egrave
            "" + (char) 139,
            // iuml
            "" + (char) 140,
            // icirc
            "" + (char) 141,
            // igrave
            "" + (char) 142,
            // Auml
            "" + (char) 143,
            // Aring
            "" + (char) 144,
            // Eacute
            "" + (char) 145,
            // aelig
            "" + (char) 146,
            // ff?
            "" + (char) 147,
            // ocirc
            "" + (char) 148,
            // ouml
            "" + (char) 149,
            // ograve
            "" + (char) 150,
            // ucirc
            "" + (char) 151,
            // ugrave
            "" + (char) 152,
            // yuml
            "" + (char) 153,
            // Ouml
            "" + (char) 154,
            // Uuml
            "" + (char) 155,
            // ??
            "" + (char) 156,
            // ??
            "" + (char) 157,
            // ??
            "" + (char) 158,
            // ??
            "" + (char) 159,
            // ??
            "" + (char) 160,
            // aacute
            "" + (char) 161,
            // iacute
            "" + (char) 162,
            // oacute
            "" + (char) 163,
            // uacute
            "" + (char) 164,
            // nwave?
            "" + (char) 165,
            // Nwave?
            "" + (char) 166,
            // ??
            "" + (char) 167,
            // ??
            "" + (char) 168,
            // ??
            "" + (char) 169,
            // ??
            "" + (char) 170,
            // 170
            "" + (char) 171,
            // ??
            "" + (char) 172,
            // ??
            "" + (char) 173,
            // ??
            "" + (char) 174,
            // ??
            "" + (char) 175,
            // ??
            "" + (char) 176,
            // ??
            "" + (char) 177,
            // ??
            "" + (char) 178,
            // ??
            "" + (char) 179,
            // ??
            "" + (char) 180,
            // 180
//
            "" + (char) 192,
            // eacute
            "" + (char) 248,
            // degrees
            "" + (char) 352,
            // egrave
            "" + (char) 402,
            // acirc
            "" + (char) 710,
            // ecirc
            "" + (char) 8218,
            // eacute
            "" + (char) 8221,
            // ouml
            "" + (char) 8222,
            // auml
            "" + (char) 8225,
            // ccedil
            "" + (char) 8230,
            // agrave
            "" + (char) 8240,
            // euml
            "" + (char) 65533,
            // uuml
    };

    /**
     * Description of the Field
     */
    public final static String DOS = "DOS";

    static String[] asciiEquivalents = {
            "",
            // ??
            "" + (char) 0,
            // ??
            "" + (char) 199,
            // Ccedil
            "" + (char) 252,
            // uuml
            "" + (char) 233,
            // eacute
            "" + (char) 226,
            // acirc
            "" + (char) 228,
            // auml
            "" + (char) 224,
            // agrave
            "" + (char) 229,
            // aring
            "" + (char) 231,
            // ccedil
            "" + (char) 234,
            // ecirc
            "" + (char) 235,
            // euml
            "" + (char) 232,
            // egrave
            "" + (char) 239,
            // iuml
            "" + (char) 238,
            // icirc
            "" + (char) 236,
            // igrave
            "" + (char) 196,
            // Auml
            "" + (char) 197,
            // Aring
            "" + (char) 201,
            // Eacute
            "" + (char) 230,
            // aelig
            "" + (char) 0,
            // ff?
            "" + (char) 244,
            // ocirc
            "" + (char) 246,
            // ouml
            "" + (char) 242,
            // ograve
            "" + (char) 251,
            // ucirc
            "" + (char) 249,
            // ugrave
            "" + (char) 255,
            // yuml
            "" + (char) 214,
            // Ouml
            "" + (char) 220,
            // Uuml
            "" + (char) 0,
            // ff?
            "" + (char) 0,
            // ff?
            "" + (char) 0,
            // ff?
            "" + (char) 0,
            // ff?
            "" + (char) 0,
            // ff?
            "" + (char) 225,
            // aacute
            "" + (char) 237,
            // iacute
            "" + (char) 243,
            // oacute
            "" + (char) 250,
            // uacute
            "" + (char) 241,
            // nwave?
            "" + (char) 209,
            // Nwave?
            "" + (char) 0,
            // ??
            "" + (char) 0,
            // ??
            "" + (char) 0,
            // ??
            "" + (char) 0,
            // ??
            "" + (char) 0,
            // 170
            "" + (char) 0,
            // ??
            "" + (char) 0,
            // ??
            "" + (char) 0,
            // ??
            "" + (char) 0,
            // ??
            "" + (char) 0,
            // ??
            "" + (char) 0,
            // ??
            "" + (char) 0,
            // ??
            "" + (char) 0,
            // ??
            "" + (char) 0,
            // ??
            "" + (char) 0,
            // 180
//
            "" + (char) 233,
            // eacute
            "[degrees]",
            // degrees
            "" + (char) 232,
            // egrave
            "" + (char) 226,
            // acirc
            "" + (char) 234,
            // ecirc
            "" + (char) 233,
            // eacute
            "" + (char) 246,
            // ouml
            "" + (char) 228,
            // auml
            "" + (char) 231,
            // ccedil
            "" + (char) 224,
            // agrave
            "" + (char) 235,
            // euml
            "" + (char) 252,
            // uuml
    };

    /**
     * substitute certain DOS-compatible diacriticals by the Unicode value. Not
     * guaranteed to be correct. Example 130 is e-acute (==
     *
     * @param s Description of the Parameter
     * @return Description of the Return Value
     */
    public static String substituteDOSbyAscii(String s) {
// check for untranslated chars
        for (int i = 0; i < s.length(); i++) {
            int jj = (int) s.charAt(i);
            if (jj > 180) {
                boolean ok = false;
                for (int j = 0; j < dosEquivalents.length; j++) {
                    if (dosEquivalents[j].equals("" + s.charAt(i))) {
                        ok = true;
                        break;
                    }
                }
                if (!ok) {
                    System.err.println("==Unknown DOS character==" + jj + "//" + s);
                }
            }
        }
        String s1 = substituteStrings(s, dosEquivalents, asciiEquivalents);
        return s1;
    }

    /**
     * substitute hex representation of character, for example =2E by char(46). If
     * line ends with =, ignore that character.
     *
     * @param s Description of the Parameter
     * @return String result
     */
    public static String substituteEquals(String s) {
        if (s == null) {
            return null;
        }
        int len = s.length();
        StringBuffer sb = new StringBuffer("");
        while (true) {
            int idx = s.indexOf(CMLUtils.EQUALS);
            if (idx == -1) {
                sb.append(s);
                return sb.toString();
            }
// remove EQUALS
            sb.append(s.substring(0, idx));
            s = s.substring(idx + 1);
            len -= idx + 1;
// not enough chars
            if (len <= 1) {
                sb.append(EQUALS);
                sb.append(s);
                return sb.toString();
            }
            int hex = getIntFromHex(s.substring(0, 2));
// wasn't a hexchar
            if (hex < 0) {
                sb.append(EQUALS);
            } else {
                sb.append((char) hex);
                s = s.substring(2);
                len -= 2;
            }
        }
    }

    /**
     * Gets the allowedFormat attribute of the CMLUtils class
     *
     * @param format Description of the Parameter
     * @return The allowedFormat value
     */
    public static boolean isAllowedFormat(String format) {
        if (
                format.equals(FORMAT_ASCII) ||
                        format.equals(FORMAT_DOS) ||
                        format.equals(FORMAT_EQUALS)) {
            return true;
        }
        System.out.println("Format is:" + format + ": must be one of :" +
                FORMAT_ASCII + ":" + FORMAT_DOS + ":" + FORMAT_EQUALS + ":");
        return false;
    }

    /**
     * converts character format within a Vector of Strings. Some formats such
     * as '=' escaping may require lines to be joined. Original Vector is
     * unaltered.
     *
     * @param vector Description of the Parameter
     * @param format Description of the Parameter
     * @return Description of the Return Value
     */
    public static Vector<String> convertFormat(Vector<String> vector, String format) {
        String currentLine = "";
        Vector<String> newVector = new Vector<>();
        for (int i = 0; i < vector.size(); i++) {
            String line = (String) vector.elementAt(i);
            if (format.equals(FORMAT_EQUALS)) {
                line = substituteEquals(line);
                if (line.endsWith(EQUALS)) {
                    currentLine += line.substring(0, line.length() - 1);
                } else {
                    if (!currentLine.equals("")) {
                        newVector.addElement(currentLine + line);
                        currentLine = "";
                    } else {
                        newVector.addElement(line);
                    }
                }
            } else if (format.equals(FORMAT_DOS)) {
                newVector.addElement(substituteDOSbyAscii(line));
// no conversion for ASCII
            } else if (format.equals(FORMAT_ASCII)) {
                return vector;
            } else {
                System.out.println("Unsupported conversion format: " + format);
                return newVector;
            }
        }
        if (format.equals(FORMAT_EQUALS) && !currentLine.equals("")) {
            System.out.println("Missing continuation line in EQUALS format");
            newVector.addElement(currentLine);
        }
        return newVector;
    }

    /**
     * capitalise a String (whatever the starting case)
     *
     * @param s Description of the Parameter
     * @return Description of the Return Value
     */
    public static String capitalise(String s) {
        if (s.equals("")) {
            return "";
        }
        if (s.length() == 1) {
            return s.toUpperCase();
        } else {
            return s.substring(0, 1).toUpperCase() +
                    s.substring(1).toLowerCase();
        }
    }

    /**
     * Description of the Method
     *
     * @param s Description of the Parameter
     * @return Description of the Return Value
     */
    public static String toCamelCase(String s) {
        StringTokenizer st = new StringTokenizer(s, " \n\r\t");
        String out = "";
        while (st.hasMoreTokens()) {
            s = st.nextToken();
            if (out != "") {
                s = capitalise(s);
            }
            out += s;
        }
        return out;
    }

// note & must come first to avoid recursion
//	static String[] escapes = {"&",     "'",      "\"",     "<",    ">"};
//	static String[] escapel = {"&amp;", "&apos;", "&quot;", "&lt;", "&gt;"};
// this gave trouble with apos, so try the minimum

    static char[] _escapes = {'&',
            /*
            *  '\'',
            */
            '"', '<',
            /*
            *  '>'
            */
    };
    static String[] _escape1 = {"&amp;",
            /*
            *  "&apos;",
            */
            "&quot;", "&lt;",
            /*
            *  "&gt;"
            */
    };

    /**
     * escape characters in an XML string; also escape non-XML characters (for example
     * eacute => &#233;). If escapes==null only escape non-XML
     *
     * @param s       Description of the Parameter
     * @param escapes Description of the Parameter
     * @param escape1 Description of the Parameter
     * @return Description of the Return Value
     */
    public static String escape(String s, char[] escapes, String[] escape1) {
        if (s == null) {
            return s;
        }
        int l = s.length();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < l; i++) {
            char ch = s.charAt(i);
            boolean special = false;
            if (escapes != null) {
                for (int j = 0; j < escapes.length; j++) {
                    if (ch == escapes[j]) {
                        sb.append(escape1[j]);
                        special = true;
                        break;
                    }
                }
            }
            if (special) {
                continue;
            }
            int ich = (int) ch;
            if (ich >= 32 && ich <= 127) {
                sb.append(ch);
// tab
            } else if (ch == '\t') {
                sb.append(ch);
// nl
            } else if (ch == '\n') {
                sb.append(ch);
// cr
            } else if (ch == '\r') {
                sb.append(ch);
            } else {
                sb.append("&#" + ich + ";");
            }
        }
        s = sb.toString();
        return s;
    }

    /**
     * default escape characters in an XML string (' -> &apos; , etc); also
     * escape non-XML characters (for example eacute => &#233;)
     *
     * @param s Description of the Parameter
     * @return Description of the Return Value
     */
    public static String escape(String s) {
        return escape(s, _escapes, _escape1);
    }

    /**
     * compares two objects using equals()
     * allows for null objects.
     * if either object is null returns false
     *
     * @param obj1 Description of the Parameter
     * @param obj2 Description of the Parameter
     * @return Description of the Return Value
     */
    public static boolean equals(Object obj1, Object obj2) {
        if (obj1 == null) {
            return (obj2 == null) ? false : obj2.equals(obj1);
        } else {
            return obj1.equals(obj2);
        }
    }

    /**
     * runs the garbage collector if memory drops below mem. (I use a value of
     * 300000 - your mileage may vary). Potentially used in loops for
     * processing input and creation of objects
     *
     * @param mem Description of the Parameter
     */
    public static void freeMemory(long mem) {
        if (Runtime.getRuntime().freeMemory() < mem) {
            System.gc();
            System.out.println("Now Free " + Runtime.getRuntime().freeMemory());
        }
    }

    /**
     * adds to the classpath and resets the system property
     *
     * @param extraPath The feature to be added to the ToClasspath attribute
     * @return String the new classpath
     */
    public String addToClasspath(String extraPath) {
        String classPath = System.getProperty("java.class.path");
        classPath += System.getProperty("path.separator") + extraPath;
        CMLUtils.setSystemProperty("java.class.path", classPath);
        return classPath;
    }

    /**
     * Translates a Hex number to its int equivalent. Thus "FE" translates to
     * 254. Horrid, but I couldn't find if Java reads hex. All results are >=
     * 0. Errors return -1
     *
     * @param hex Description of the Parameter
     * @return The intFromHex value
     */
    public static int getIntFromHex(String hex) {
        hex = hex.toUpperCase();
        if (hex.startsWith("0X")) {
            hex = hex.substring(2);
        } else if (hex.startsWith("X")) {
            hex = hex.substring(1);
        }
        int result = 0;
        for (int i = 0; i < hex.length(); i++) {
            char c = hex.charAt(i);
            if (Character.isDigit(c)) {
                c -= '0';
            } else if (c < 'A' || c > 'F') {
                return -1;
            } else {
                c -= 'A';
                c += (char) 10;
            }
            result = 16 * result + c;
        }
        return result;
    }

    /**
     * reads a byte array from file, *including* line feeds
     *
     * @param filename Description of the Parameter
     * @return Description of the Return Value
     * @throws FileNotFoundException Description of the Exception
     * @throws IOException           Description of the Exception
     */
    public static byte[] readByteArray(String filename)
            throws FileNotFoundException, IOException {
        try (DataInputStream dis = new DataInputStream(new FileInputStream(filename))) {
            return CMLUtils.readByteArray(dis);
        }
    }

    /**
     * reads a byte array from DataInputStream, *including* line feeds
     *
     * @param d Description of the Parameter
     * @return Description of the Return Value
     * @throws IOException Description of the Exception
     */
    public static byte[] readByteArray(DataInputStream d)
            throws IOException {
        int len = 100;
        int count = 0;
        byte[] src = new byte[len];
        byte b;
        while (true) {
            try {
                b = d.readByte();
            } catch (EOFException e) {
                break;
            }
            src[count] = b;
            if (++count >= len) {
                len *= 2;
                byte[] temp = new byte[len];
                System.arraycopy(src, 0, temp, 0, count);
                src = temp;
            }
        }
        len = count;
        byte[] temp = new byte[len];
        System.arraycopy(src, 0, temp, 0, count);
        return temp;
    }

    /**
     * remove all control (non-printing) characters
     *
     * @param s Description of the Parameter
     * @return Description of the Return Value
     */
    public static String stripISOControls(String s) {
        if (s == null) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        int l = s.length();
        for (int i = 0; i < l; i++) {
            char ch = s.charAt(i);
            if (!Character.isISOControl(ch)) {
                sb.append(ch);
            }
        }
        return sb.toString();
    }

    /**
     * normalise whitespace in a String (all whitespace is transformed to
     * single spaces and the string is NOT trimmed
     *
     * @param s Description of the Parameter
     * @return Description of the Return Value
     */
    public static String normaliseWhitespace(String s) {
        if (s == null || s.equals("")) {
            return s;
        }
        StringTokenizer st = new StringTokenizer(s, CMLUtils.WHITESPACE);
        int l = s.length();
        String ss = "";
        if (Character.isWhitespace(s.charAt(0))) {
            ss = " ";
        }
        String end = "";
        if (Character.isWhitespace(s.charAt(l - 1))) {
            end = " ";
        }
        boolean start = true;
        while (st.hasMoreTokens()) {
            if (start) {
                ss += st.nextToken();
                start = false;
            } else {
                ss += " " + st.nextToken();
            }
        }
        return ss + end;
    }

    /**
     * strip linefeeds from a byte array
     *
     * @param b Description of the Parameter
     * @return Description of the Return Value
     */
    public static byte[] stripNewlines(byte[] b) {
        int l = b.length;
        byte[] bb = new byte[l];
        int j = 0;
        for (int i = 0; i < l; i++) {
            if (b[i] != '\n') {
                bb[j++] = b[i];
            }
        }
        byte[] bbb = new byte[j];
        System.arraycopy(bb, 0, bbb, 0, j);
        return bbb;
    }

    /**
     *  converts a string to its byte[] equivalent
     *
     *@param  event  Description of the Parameter
     *@return The rightMouseClick value
     */
//    public static byte[] string2Bytes(String s) {
//        int l = s.length();
//        byte[] b = new byte[l];
//        s.getBytes(0, l, b, 0);
//        return b;
//    }

    /**
     * a crude way of identifying a right mouse click (because I left the Java
     * book behind)
     *
     * @param event Description of the Parameter
     * @return The rightMouseClick value
     */
    public static boolean isRightMouseClick(java.awt.event.MouseEvent event) {
        String s = event.toString();
        return (s.indexOf("MOUSE_CLICKED") != -1 &&
                s.indexOf("mods=4") != -1);
    }



    /**
     * truncate filename suffix to make a directory name (without
     * file.separator)
     *
     * @param urlString Description of the Parameter
     * @return Description of the Return Value
     */
    public static String makeDirectory(String urlString) {
        if (urlString == null) {
            return null;
        }
        int idx = urlString.lastIndexOf(System.getProperty("file.separator"));
        if (idx != -1) {
            urlString = urlString.substring(0, idx);
        }
        return urlString;
    }

    /**
     * If a URL is relative, make it absolute against the current directory. If
     * url already has a protocol, return unchanged
     *
     * @param url Description of the Parameter
     * @return Description of the Return
     *         Value
     * @throws java.net.MalformedURLException Description of the Exception
     */
    public static String makeAbsoluteURL(String url)
            throws java.net.MalformedURLException {
        if (url == null) {
            throw new MalformedURLException("Null url");
        }
        URL baseURL;
        String fileSep = System.getProperty("file.separator");
// why the final slash?
//		url = url.replace(fileSep.charAt(0), '/') + '/';
        url = url.replace(fileSep.charAt(0), '/');

// is url alreday a valid URL?
        boolean ok = true;
        try {
            java.net.URI.create(url).toURL();
        } catch (Exception mue) {
            ok = false;
// DOS filenames (for example C:\foo) gives problems
            String mueString = mue.toString().trim();
            int idx = mueString.indexOf("unknown protocol:");
            if (idx != -1) {
                mueString = mueString.substring(idx + "unknown protocol:".length()).trim();
// starts with X: assume DOS filename
                if (mueString.length() == 1) {
                    url = "file:/" + url;
// throws MalformedURL if wrong
                    java.net.URI.create(url).toURL();
                    ok = true;
                }
            }
        }
        if (ok) {
            return url;
        }

        String currentDirectory = System.getProperty("user.dir");
        String file = currentDirectory.replace(fileSep.charAt(0), '/') + '/';

        if (file.charAt(0) != '/') {
            file = "/" + file;
        }
        baseURL = java.net.URI.create("file://" + file).toURL();

        String newUrl;
        try {
            newUrl = baseURL.toURI().resolve(url).toURL().toString();
        } catch (java.net.URISyntaxException e) {
            throw new MalformedURLException(e.getMessage());
        }
        return newUrl;
    }



    /**
     * get an OutputStream from a file or URL. Required (I think) because
     * strings of the sort "file:/C:\foo\bat.txt" crash FileOutputStream, so
     * this strips off the file:/ stuff for Windows-like stuff
     *
     * @param fileName Description of the Parameter
     * @return FileOutputStream a new (opened)
     *         FileOutputStream
     * @throws java.io.FileNotFoundException Description of the Exception
     */
    public static FileOutputStream getFileOutputStream(String fileName)
            throws java.io.FileNotFoundException {
        if (fileName == null) {
            return null;
        }
// W-like syntax
        if (fileName.startsWith("file:") &&
                fileName.substring(5).indexOf(CMLUtils.COLON) != -1) {
            fileName = fileName.substring(5);
            if (fileName.startsWith(CMLUtils.SLASH) ||
                    fileName.startsWith(CMLUtils.BACKSLASH)) {
                fileName = fileName.substring(1);
            }
        }
        return new FileOutputStream(fileName);
    }

    /**
     * read a Zipfile
     *
     * @param fileName Description of the Parameter
     * @throws IOException Description of the Exception
     */
    public static void readZip(String fileName) throws IOException {
        if (fileName == null) {
            throw new IOException("Null zip file name");
        }
        ZipFile zipFile = new ZipFile(fileName);
        try {
        Enumeration<? extends ZipEntry> zipEntries = zipFile.entries();
        while (zipEntries.hasMoreElements()) {
            ZipEntry zipEntry = (ZipEntry) zipEntries.nextElement();
//			ZipEntry zipEntry = zipFile.getEntry(entryName);
            BufferedReader br = new BufferedReader(new InputStreamReader
                    (zipFile.getInputStream(zipEntry)));
            while (true) {
                String line = br.readLine();
                if (line == null) {
                    break;
                }
                System.out.println(">" + line);
            }
        }
        } finally {
            try {
                zipFile.close();
            } catch (IOException e) {
                // Ignore close error
            }
        }
    }



    // cache the formats
    static Hashtable<String, DecimalFormat> formTable = new Hashtable<>();

    /**
     * this is a mess
     *
     * @param nPlaces Description of the Parameter
     * @param value   Description of the Parameter
     * @return Description of the Return Value
     * @throws IllegalArgumentException Description of the Exception
     */
    public static String outputInteger(int nPlaces, int value)
            throws IllegalArgumentException {
// cache formatter
        String f = "i" + nPlaces;
        DecimalFormat form = (DecimalFormat) formTable.get(f);
        if (form == null) {
            String pattern = "";
            for (int i = 0; i < nPlaces - 1; i++) {
                pattern += "#";
            }
            pattern += "0";
            form = (DecimalFormat) NumberFormat.getInstance();
            form.setMaximumIntegerDigits(nPlaces);
            form.applyLocalizedPattern(pattern);
            formTable.put(f, form);
        }
        String result = form.format(value).trim();
        int l = result.length();
        if (l > nPlaces) {
            throw new IllegalArgumentException("Integer too big");
        }
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < nPlaces - l; i++) {
            sb.append(" ");
        }
        return sb.append(result).toString();
    }

    /**
     * format for example f8.3 this is a mess; if cannot fit, then either
     * right-truncates or when that doesn't work, returns ****
     *
     * @param nPlaces Description of the Parameter
     * @param nDec    Description of the Parameter
     * @param value   Description of the Parameter
     * @return Description of the Return Value
     * @throws IllegalArgumentException Description of the Exception
     */
    public static String outputFloat(int nPlaces, int nDec, double value)
            throws IllegalArgumentException {
        String f = "f" + nPlaces + CMLUtils.PERIOD + nDec;
        DecimalFormat form = (DecimalFormat) formTable.get(f);
        if (form == null) {
            String pattern = "";
            for (int i = 0; i < nPlaces - nDec - 2; i++) {
                pattern += "#";
            }
            pattern += "0.";
            for (int i = nPlaces - nDec; i < nPlaces; i++) {
                pattern += "0";
            }
            form = (DecimalFormat) NumberFormat.getInstance();
            form.setMaximumIntegerDigits(nPlaces - nDec - 1);
            form.applyLocalizedPattern(pattern);
            formTable.put(f, form);
        }
        String result = form.format(value).trim();
        boolean negative = false;
        if (result.startsWith("-")) {
            result = result.substring(1);
            negative = true;
        }
// this removes leading zeroes :-(
        while (result.startsWith("0")) {
            result = result.substring(1);
        }
        if (negative) {
            result = "-" + result;
        }
        StringBuffer sb = new StringBuffer();
        int l = result.length();
        for (int i = 0; i < nPlaces - l; i++) {
            sb.append(" ");
        }
        String s = sb.append(result).toString();
        if (l > nPlaces) {
            s = s.substring(0, nPlaces);
// decimal point got truncated?
            if (s.indexOf(".") == -1) {
                s = "";
                for (int i = 0; i < nPlaces; i++) {
                    s += "*";
                }
            }
        }
        return s;
    }

    /**
     * as above, but trims trailing zeros
     *
     * @param nPlaces Description of the Parameter
     * @param nDec    Description of the Parameter
     * @param c       Description of the Parameter
     * @return Description of the Return Value
     */
    public static String outputNumber(int nPlaces, int nDec, double c) {
        String s = CMLUtils.outputFloat(nPlaces, nDec, c).trim();
        if (s.indexOf(".") != -1) {
            while (s.endsWith("0")) {
                s = s.substring(0, s.length() - 1);
            }
            if (s.endsWith(".")) {
                s = s.substring(0, s.length() - 1);
            }
        }
        return s;
    }

    /**
     * invert a Hashtable by interchanging keys and values. This assumes a 1;1
     * mapping - if not the result is probably garbage.
     *
     * @param table Description of the Parameter
     * @return Description of the Return Value
     */
    public static Hashtable<Object, Object> invert(Hashtable<Object, Object> table) {
        if (table == null) {
            return null;
        }
        Hashtable<Object, Object> newTable = new Hashtable<>();
        for (Enumeration<Object> e = table.keys(); e.hasMoreElements();) {
            Object key = e.nextElement();
            Object value = table.get(key);
            newTable.put(value, key);
        }
        return newTable;
    }

    /**
     * AND 2 Hashtables - inefficient except for small tables. Finds entries
     * with the same key and value.
     *
     * @param a Description of the Parameter
     * @param b Description of the Parameter
     * @return Hashtable contains only common entries. null if none
     */
    public static Hashtable<Object, Object> andTables(Hashtable<Object, Object> a, Hashtable<Object, Object> b) {
        if (a == null || b == null) {
            return null;
        }
        Hashtable<Object, Object> c = null;
        Enumeration<Object> e = a.keys();
        while (e.hasMoreElements()) {
            Object keya = e.nextElement();
            Object valb = b.get(keya);
            if (valb == null) {
                continue;
            }
            Object vala = a.get(keya);
            if (!(vala.equals(valb))) {
                continue;
            }
            if (c == null) {
                c = new Hashtable<>();
            }
            c.put(keya, vala);
        }
        return c;
    }

    /**
     * OR 2 Hashtables - inefficient except for small tables. Merges entries.
     * if entry with the same key and different value is found, take value from
     * first table.
     *
     * @param a Description of the Parameter
     * @param b Description of the Parameter
     * @return Hashtable contains all entries. null if none
     */
    public static Hashtable<Object, Object> orTables(Hashtable<Object, Object> a, Hashtable<Object, Object> b) {
        if (a == null || b == null) {
            return null;
        }
        Hashtable<Object, Object> c = new Hashtable<>();
        Enumeration<Object> e = b.keys();
        while (e.hasMoreElements()) {
            Object keyb = e.nextElement();
            Object valb = b.get(keyb);
            c.put(keyb, valb);
        }
        e = a.keys();
        while (e.hasMoreElements()) {
            Object keya = e.nextElement();
            Object vala = a.get(keya);
            c.put(keya, vala);
        }
        return c;
    }

    /**
     * XOR 2 Hashtables
     * inefficient except for small tables.
     * omit
     *
     * @param a Description of the Parameter
     * @param b Description of the Parameter
     * @return Hashtable contains no common entries.
     */
    public static Hashtable<Object, Object> xorTables(Hashtable<?, ?> a, Hashtable<?, ?> b) {
        if (a == null || b == null) {
            return null;
        }
        Hashtable<Object, Object> c = new Hashtable<>();
        Enumeration<?> e = a.keys();
        while (e.hasMoreElements()) {
            Object keya = e.nextElement();
            if (b.get(keya) != null) {
                continue;
            }
            c.put(keya, a.get(keya));
        }
        e = b.keys();
        while (e.hasMoreElements()) {
            Object keyb = e.nextElement();
            if (a.get(keyb) != null) {
                continue;
            }
            c.put(keyb, b.get(keyb));
        }
        return c;
    }

    /**
     * returns a vector of all repeated values in v.
     *
     * @param v Description of the Parameter
     * @return Vector containing duplicate values. If none, returns null
     */
    public static Vector<Object> getRepeatedValues(Vector<?> v) {
        Vector<Object> repeatedVector = new Vector<>();
        Hashtable<Object, String> table = new Hashtable<>();
        for (int i = 0; i < v.size(); i++) {
            Object obj = v.elementAt(i);
            if (table.get(obj) != null) {
                repeatedVector.add(obj);
            }
            table.put(obj, "");
        }
        return (repeatedVector.size() == 0) ? null : repeatedVector;
    }

    /**
     * finds elements common to 2 vectors. Very crude (O(n^2));
     *
     * @param a Description of the Parameter
     * @param b Description of the Parameter
     * @return Description of the Return Value
     */
    public static Vector<Object> andVectors(Vector<?> a, Vector<?> b) {
        Vector<Object> c = null;
        if (a == null || b == null) {
            return null;
        }
        int asize = a.size();
        for (int i = 0; i < asize; i++) {
            Object obj = a.elementAt(i);
            if (b.contains(obj)) {
                if (c == null) {
                    c = new Vector<>();
                }
                c.addElement(obj);
            }
        }
        return c;
    }

    /**
     * add the elements of an Enumeration to a Vector.
     *
     * @param v The feature to be added to the EnumerationToVector attribute
     * @param e The feature to be added to the EnumerationToVector attribute
     */
    public static void addEnumerationToVector(Vector<Object> v, Enumeration<?> e) {
        if (v == null) {
            return;
        }
        while (e.hasMoreElements()) {
            v.addElement(e.nextElement());
        }
    }

    /**
     * sort an object array - very inefficient
     *
     * @param objs Description of the Parameter
     */
    public static void sort(Object[] objs) {
        if (objs == null) {
            return;
        }
        int size = objs.length;
        boolean change = true;
        while (change) {
            change = false;
            for (int i = 0; i < size - 1; i++) {
                int comp = objs[i].toString().compareTo(objs[i + 1].toString());
                if (comp > 0) {
                    Object temp = objs[i + 1];
                    objs[i + 1] = objs[i];
                    objs[i] = temp;
                    change = true;
                }
            }
        }
    }

    /**
     * Description of the Method
     */
    public static void printChar() {
        for (int i = 0; i < 256; i++) {
            System.out.println("" + i + " " + (char) i);
        }
    }

    /**
     * sort a Vector - VERY crude and inefficient
     *
     * @param v Description of the Parameter
     */
    public static void sortVector(Vector<Object> v) {
        Object[] objs = new Object[v.size()];

        for (int i = 0; i < v.size(); i++) {
            objs[i] = v.elementAt(i);
        }
        CMLUtils.sort(objs);
        for (int i = 0; i < v.size(); i++) {
            v.setElementAt(objs[i], i);
        }
    }

    /**
     * create Vector with elements common to v1 and v2. SLOW. Comparison is
     * done with equals()
     *
     * @param v1 Description of the Parameter
     * @param v2 Description of the Parameter
     * @return Description of the Return Value
     */
    public static Vector<Object> and(Vector<Object> v1, Vector<Object> v2) {
        Vector<Object> v = new Vector<>();
        for (int i = 0; i < v1.size(); i++) {
            Object o1 = v1.elementAt(i);
            for (int j = 0; j < v2.size(); j++) {
                Object o2 = v2.elementAt(j);
                if (o1.equals(o2)) {
                    v.addElement(o1);
                }
            }
        }
        return v;
    }

    /**
     * create Vector with elements in v1 but not v2. SLOW. Comparison is done
     * with equals()
     *
     * @param v1 Description of the Parameter
     * @param v2 Description of the Parameter
     * @return Description of the Return Value
     */
    public static Vector<Object> not(Vector<Object> v1, Vector<Object> v2) {
        Vector<Object> v = new Vector<>();
        for (int i = 0; i < v1.size(); i++) {
            Object o1 = v1.elementAt(i);
            boolean found = false;
            for (int j = 0; j < v2.size(); j++) {
                Object o2 = v2.elementAt(j);
                if (o1.equals(o2)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                v.addElement(o1);
            }
        }
        return v;
    }

    /**
     * create Vector with elements in v1 but not v2. SLOW. Comparison is done
     * with equals()
     *
     * @param v1 Description of the Parameter
     * @param v2 Description of the Parameter
     * @return Description of the Return Value
     */
    public static Vector<Object> or(Vector<Object> v1, Vector<Object> v2) {
        Vector<Object> v = new Vector<>();
        for (int i = 0; i < v1.size(); i++) {
            v.addElement(v1.elementAt(i));
        }
        Vector<Object> vv = not(v2, v);
        for (int i = 0; i < vv.size(); i++) {
            v.addElement(vv.elementAt(i));
        }
        return v;
    }

    /**
     * create Hashtable with elements common to h1 and h2. The keys are taken
     * from h1. SLOW. Comparison is done with equals()
     *
     * @param h1 Description of the Parameter
     * @param h2 Description of the Parameter
     * @return Description of the Return Value
     */
    public static Hashtable<Object, Object> and(Hashtable<Object, Object> h1, Hashtable<Object, Object> h2) {
        Hashtable<Object, Object> h = new Hashtable<>();
        for (Enumeration<Object> k1 = h1.keys(); k1.hasMoreElements();) {
            Object k = k1.nextElement();
            Object o1 = h1.get(k);
            for (Enumeration<Object> e2 = h2.elements(); e2.hasMoreElements();) {
                Object o2 = e2.nextElement();
                if (o1.equals(o2)) {
                    h.put(k, o1);
                }
            }
        }
        return h;
    }

    /**
     * create Hashtable with elements in to h1 but not h2. The keys are taken
     * from h1 SLOW. Comparison is done with equals()
     *
     * @param h1 Description of the Parameter
     * @param h2 Description of the Parameter
     * @return Description of the Return Value
     */
    public static Hashtable<Object, Object> not(Hashtable<Object, Object> h1, Hashtable<Object, Object> h2) {
        Hashtable<Object, Object> h = new Hashtable<>();
        for (Enumeration<Object> k1 = h1.keys(); k1.hasMoreElements();) {
            Object k = k1.nextElement();
            Object o1 = h1.get(k);
            boolean found = false;
            for (Enumeration<Object> e2 = h2.elements(); e2.hasMoreElements();) {
                Object o2 = e2.nextElement();
                if (o1.equals(o2)) {
                    found = true;
                    break;
                }
            }
            if (!found) {
                h.put(k, o1);
            }
        }
        return h;
    }

    /**
     * create Hashtable with elements in to h1 but not h2. The keys are taken
     * from h1 SLOW. Comparison is done with equals()
     *
     * @param h1 Description of the Parameter
     * @param h2 Description of the Parameter
     * @return Description of the Return Value
     */
    public static Hashtable<Object, Object> or(Hashtable<Object, Object> h1, Hashtable<Object, Object> h2) {
        Hashtable<Object, Object> h = new Hashtable<>();
        for (Enumeration<Object> k1 = h1.keys(); k1.hasMoreElements();) {
            Object k = k1.nextElement();
            Object o1 = h1.get(k);
            h.put(k, o1);
        }
        for (Enumeration<Object> k2 = h2.keys(); k2.hasMoreElements();) {
            Object k = k2.nextElement();
            Object o2 = h2.get(k);
            if (h1.contains(o2)) {
                h.put(k, o2);
            }
        }
        return h;
    }


    /*
    *  for use with XSL
    */
    /**
     * Description of the Method
     *
     * @param fString Description of the Parameter
     * @return Description of the Return Value
     */
    public static double sin(String fString) {
        try {
            return Math.sin(Double.parseDouble(fString));
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    /**
     * Description of the Method
     *
     * @param fString Description of the Parameter
     * @return Description of the Return Value
     */
    public static double cos(String fString) {
        try {
            return Math.cos(Double.parseDouble(fString));
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    /**
     * Description of the Method
     *
     * @param fString Description of the Parameter
     * @return Description of the Return Value
     */
    public static double log(String fString) {
        try {
            return Math.log(Double.parseDouble(fString));
        } catch (NumberFormatException e) {
            return Double.NaN;
        }
    }

    /**
     * supports XSL substring
     *
     * @param string    Description of the Parameter
     * @param startPos  Description of the Parameter
     * @param lenString Description of the Parameter
     * @return Description of the Return Value
     */
    public static String substring(String string, String startPos, String lenString) {
        if (string == null) {
            return null;
        }
        int len = string.length();
        int start = -1;
        int length = -1;
        try {
            start = Integer.parseInt(startPos);
            length = Integer.parseInt(lenString);
        } catch (NumberFormatException nfe) {
            return null;
        }
        if (start > len) {
            return null;
        }
        if (start < 1) {
            start = 1;
        }
        if (length > len) {
            length = len;
        }
        return string.substring(start - 1, start - 1 + length);
    }

    /**
     * Description of the Method
     *
     * @param args Description of the Parameter
     */
    public static void main(String args[]) {
        if (args.length == 0) {
            System.out.println("Usage: java org.episteme.natural.chemistry.loaders.cml.util.CMLUtils args");
            System.out.println("Args:");
            System.out.println("    format");
            System.out.println("    vector");
            System.out.println("    properties propfile");
            System.out.println("    unzip zipfile");
            System.out.println("    DUMP dumpfile");
            System.out.println("    CSV csvfile");
            System.out.println("    EQUALS string");
            System.out.println("    COPY inFile outFile");
            System.out.println("    DELETE filename [dir]");
            System.out.println("    NEWFILE string");
            System.out.println("    CHAR");
            System.exit(0);
        }
        try {
            if (args[0].equalsIgnoreCase("format")) {
                System.out.println(">" + outputInteger(3, 12) + "<");
                System.out.println(">" + outputInteger(3, -12) + "<");
                System.out.println(">" + outputFloat(8, 3, 12.1) + "<");
                System.out.println(">" + outputFloat(8, 3, -12.1) + "<");
                System.out.println(">" + outputFloat(8, 3, 12.123456) + "<");
                System.out.println(">" + outputFloat(8, 3, 1234.1234) + "<");
                System.out.println(">" + outputInteger(3, 1234) + "<");
                System.out.println(">" + outputInteger(3, -1234) + "<");
            }
            if (args[0].equalsIgnoreCase("vector")) {
                Vector<String> a = new Vector<>();
                String[] as = {"A", "B", "C"};
                Vector<String> b = new Vector<>();
                String[] bs = {"D", "C", "Q", "B"};
                for (int i = 0; i < as.length; i++) {
                    a.addElement(as[i]);
                }
                for (int i = 0; i < bs.length; i++) {
                    b.addElement(bs[i]);
                }
                System.out.println(CMLUtils.andVectors(a, b));
            }
            if (args[0].equalsIgnoreCase("properties")) {
                CMLUtils.addToSystemProperties(args[1]);
                Enumeration<?> properties = System.getProperties().propertyNames();
                while (properties.hasMoreElements()) {
                    Object p = properties.nextElement();
                    System.out.println("Property: " + p + "=" + System.getProperties().get(p));
                }
            }
            if (args[0].equalsIgnoreCase("unzip")) {
                readZip(args[1]);
            }
            if (args[0].equalsIgnoreCase("NEWFILE")) {
                createNewFile(args[1]);
            }
            if (args[0].equalsIgnoreCase("CSV")) {
                String[] ss = {
                        "\"fred\",",
                        "\"fred jim\",",
                        "\"fred jim\",",
                        "\"fred jim\",\"sue sally\"",
                        "\"fred jim\",\"sue sally\",",
                        "\"fred jim\",\"sue (\"\") sally\",",
                        "\"fred jim\",,\"sue sally\",,",
                };
                for (int i = 0; i < ss.length; i++) {
                    System.out.println(ss[i] + "=" + getCommaSeparatedStrings(ss[i]));
                }
            }
            if (args[0].equalsIgnoreCase("DUMP")) {
                String urlString = CMLUtils.makeAbsoluteURL(args[1]);
                System.out.println(CMLUtils.dump(java.net.URI.create(urlString).toURL()));
            }
            if (args[0].equalsIgnoreCase("EQUALS") && args.length >= 2) {
                System.out.println(substituteEquals(args[1]));
            }
            if (args[0].equalsIgnoreCase("CHAR")) {
                printChar();
            }
            if (args[0].equalsIgnoreCase("DELETE")) {
                if (args.length >= 2) {
                    boolean deleteDirectory = args.length >= 3 && args[2].equalsIgnoreCase("dir");
                    CMLUtils.deleteFile(new File(args[1]), deleteDirectory);
                }
            }
            if (args[0].equalsIgnoreCase("COPY")) {
                if (args.length == 3) {
                    CMLUtils.copyFile(new File(args[1]), new File(args[2]));
                }
            }
        } catch (Exception e) {
            System.out.println("Exception: " + e);
        }
    }
}


