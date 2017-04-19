package edu.csus.ecs.pc2.core.util;

import java.util.Vector;

/**
 * CSV Parser.
 * 
 * @author pc2@ecs.csus.edu
 */
public final class CommaSeparatedValueParser {
    private static final char COMMA_CHAR = 44;
    private static final String COMMA_STRING = String.valueOf(COMMA_CHAR);

    /**
     * Return array of containing the Comma Separated Values from the input string.
     * 
     * @return java.lang.String[]
     * @param line
     *            java.lang.String
     * @throws Exception if there was a problem parsing the line
     */
    public static String[] parseLine(String line) throws Exception {
        
        if (line == null) {
            throw new IllegalArgumentException("null String not allowed");
        }
        
//        String[] array = new String[1];
//        array[0] = "";
//        int fieldCount = 1;
        
        /**
         * Current field content.
         */
        String currentField = "";
        Vector<String> v = new Vector<String>();
        int i;
        
        /**
         * Parsing position inside a quote.
         */
        
        boolean inQuote = false;
        char current;
        char next;
        int length = line.length();
        @SuppressWarnings("unused")
        int ignoredTerminatingQuote = 0; // SOMEDAY can this be removed?
        
        for (i = 0; i < line.length(); i++) {
            current = line.charAt(i);
            
            if (current == '"') {
                if (inQuote) {
                    if (i + 1 >= length) {
                        // terminating quote at end of line, do nothing
                        ignoredTerminatingQuote++;
                    } else {
                        next = line.charAt(i + 1);
                        if (next == COMMA_CHAR) {
                            // terminating quote of fieldCount
                            inQuote = false;
                        } else {
                            if (next == '"') {
                                // store quote
                                currentField = currentField.concat("\"");
                                i++;
                            } else {
                                new Exception("found unexpected quote at position " + i + ", but next character '" + String.valueOf(next) + "'");
                            }
                        }
                    }
                } else { // not inquote
                    inQuote = true;
                    
                    // remove all text previous to a quote, this 
                    // is field information between the , and the first "
                    currentField = "";
                }
            } else { // not a doublequote
                if (current == COMMA_CHAR) {
                    if (inQuote) {
                        currentField = currentField.concat(COMMA_STRING);
                    } else {
                        // store
                        v.addElement(currentField);
//                        fieldCount++;
                        currentField = "";
                    }
                } else { // not a comma
                    currentField = currentField.concat(new Character(current).toString());
                }
            }
        }
        
        if (inQuote){
            throw new Exception("String missing closing double quote '"+line+"'");
        }
        // store the last fieldCount
        v.addElement(currentField);
        
        return (String[]) v.toArray(new String[v.size()]);
        
      // old code to convert Vector into String[]
//        array = new String[fieldCount];
//        if (fieldCount != v.size()) {
//            // TODO review this Exception
//            new Exception("Incorrect number of fields (found " +  fieldCount +", but expected " + v.size() + ")");
//        }
//        Object o;
//        for (i = 0; i < v.size(); i++) {
//            o = v.elementAt(i);
//            if (o != null) {
//                array[i] = (String) o;
//            } else {
//                array[i] = "";
//            }
//        }
//        return array;
    }

    /**
     * Return String of the Comma Separated Values made from the input array
     * 
     * @return java.lang.String
     * @param array
     *            java.lang.String[]
     */
    public static String toString(String[] array) {
        String s = "";
        String field;
        String newField;
        int i;
        int start;
        boolean needsQuote;
        for (i = 0; i < array.length; i++) {
            newField = "";
            start = 0;
            needsQuote = false;
            field = array[i];
            if (field.indexOf("\"") > -1) {
                needsQuote = true;
                while (field.indexOf("\"", start) > -1) {
                    newField = newField.concat(field.substring(start, field.indexOf("\"", start) + 1) + "\"");
                    start = field.indexOf("\"", start) + 1;
                }
                field = newField + field.substring(start);
            }
            if (field.indexOf(String.valueOf(COMMA_STRING)) > -1 || needsQuote) {
                s = s.concat("\"" + field + "\",");
            } else {
                s = s.concat(field + COMMA_STRING);
            }
        }
        s = s.substring(0, s.length() - 1); // trim trailing comma
        return s;
    }

    /**
     * 
     */
    private CommaSeparatedValueParser() {
        super();
    }
}
