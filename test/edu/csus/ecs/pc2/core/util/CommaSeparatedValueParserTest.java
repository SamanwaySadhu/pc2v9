package edu.csus.ecs.pc2.core.util;

/**
 * Unit tests 
 * 
 * @author pc2@ecs.csus.edu
 */
public class CommaSeparatedValueParserTest extends AbstractTestCase {

    private boolean debugMode = false;

    public static final String QUOTE = "\"";

    /**
     * Simple positive test cases.
     * 
     * These cases have proper syntax
     * 
     */
    public void testSimpleCSV() {

        String[] exampleStrings = { //
                // 
                "foo", // 1
                ";foo;,;bar;", // 2
                ";;,;;,;;,;;,;;", // 5 with no spaces after ,
                ";one;,;two;,;three;", // 3
        };

        int[] expectedFieldCounts = { //
                //
                1, //
                2, //
                5, //
                3, //
        };

        runTests("testSimpleCSV", exampleStrings, expectedFieldCounts);

    }
    
    /**
     * 
     * @throws Exception
     */
    public void testExtraSpaces() throws Exception {
        
        // test that does not trim field.
        
        String field2 = "   second";

        String actualString = "\"first\","+field2;
        String[] fields = CommaSeparatedValueParser.parseLine(actualString);
        
        if (debugMode) {
            if (!field2.equals(fields[1])) {
                System.err.println("Expected '" + field2 + "' got '" + fields[1] + "'");
            }
        } else {
            assertEquals("Expected fields to be identical", field2, fields[1]);
        }
        
        // There should only be the characters within the double quotes
        // so the spaces after the , should be trimmed.
        
        field2 = " the second field ";
        
        String inputString = "\"first\",      \""+field2+"\"";
        
        try {
            fields = CommaSeparatedValueParser.parseLine(inputString);
            fail("Expecting parsing Exception (expected comma) thrown for '" + inputString + "'");
        } catch (Exception e) {
            // expecting exception
            String expectedMessage = "String missing closing double quote '\"first\",      \" the second field \"'";
            assertEquals(expectedMessage, e.getMessage());
        }
    }

    /**
     * Tests parser for "" (quote within a field) in input.
     * 
     * "" is the method to put a " inside a CSV field. T
     * 
     * @throws Exception
     */
    public void testCSVQuoted() throws Exception {

        // note that runTests will change ; to " in strings.

        String[] exampleStrings = { //
        ";;;Quoted String;;;", // 1
                ";;;Quoted String;;;, Second, ;Finally ;;it;; is done;", // 3
        };

        int[] expectedFieldCounts = { //
                //
                1, //
                3, //
        };

        runTests("testCSVQuoted", exampleStrings, expectedFieldCounts);

    }

    public void testCSVFormal() throws Exception {

        String[] exampleStrings = { //
                //
                "foo", // 1
                ";foo;,;bar;", // 2
                "foo,bar", // 2
                "foo,;bar;", // 2
                ";foo;,bar", // 3
                "foo,bar,", // 3
                ",foo,bar,", // 4
                ",,,", // 4
                ";;, ;;, ;;, ;;, ;;, ;;", // 6 with spaces after ,
                ";;,;;,;;,;;,;;,;;", // 6 with no spaces after ,
                ",,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,,last", // 46
                ",;foo,;,", // 3 missing trailing
                "foo;", // 1 Trailing double quote ignored
        };

        int[] expectedFieldCounts = { //
        1, //
                2, //
                2, //
                2, //
                2, //
                3, //
                4, //
                4, //
                6, //
                6, //
                46, //
                3, //
                1, //
        };

        runTests("testCSVFormal", exampleStrings, expectedFieldCounts);
    }

    /**
     * run tests on parsing.
     * 
     * This will replace all ; in input exampleStrings with "
     * 
     * @param exampleStrings
     *            Strings to be parsed
     * @param expectedFieldCounts
     *            expected field counts after parsed.
     */
    private void runTests(String methodName, String[] exampleStrings, int[] expectedFieldCounts) {
        for (int i = 0; i < exampleStrings.length; i++) {

            String actualString = exampleStrings[i].replaceAll(";", QUOTE);

            try {
                String[] parsed = CommaSeparatedValueParser.parseLine(actualString);

                if (debugMode) {
                    System.err.flush();
                    System.out.println();
                    System.out.println(methodName + " Input: '" + actualString + "' expected " + expectedFieldCounts[i] + " fields.");
                    if (parsed.length == expectedFieldCounts[i]) {
                        System.out.println("     PASSED Test");
                    } else {
                        System.out.println("     failed Test, off by " + (expectedFieldCounts[i] - parsed.length));
                    }
                    int idx = 1;
                    for (String field : parsed) {
                        System.out.println("   " + (idx++) + " <" + field + ">");
                    }
                    System.out.flush();
                }

                customAssert("Improper parse, field counts do not match for ' " + actualString + "'", expectedFieldCounts[i], parsed.length);

            } catch (Exception e) {
                assertTrue("Unexpected error in parse " + e.getMessage() + " for for '" + actualString + "'", true);
            }
        }
    }

    /**
     * Test whether equals.
     * 
     * @param message
     * @param expected
     * @param actual
     */
    private void customAssert(String message, int expected, int actual) {

        if (expected != actual) {
            if (debugMode) {
                System.err.println(message + " expected: " + expected + " got: " + actual);
                System.err.flush();
            } else {
                assertSame(message, expected, actual);
            }
        }
    }

    /**
     * Test missing comma in input string.
     * 
     * @throws Exception
     */
    public void testMissingComma()  {

        // Missing comma between first and section fields

        String actualString = "\"first\"second";
        try {
            CommaSeparatedValueParser.parseLine(actualString);
            fail("Improper parse, expecting Exception, missing comma for "+actualString);
        } catch (Exception e) {
            String expectedMessage = "String missing closing double quote '\"first\"second'";
            assertEquals(expectedMessage, e.getMessage());
        }

        // Missing command between first and second fields
        
        actualString = "first\"second\"";
        try {
            CommaSeparatedValueParser.parseLine(actualString);
            fail("Improper parse, expecting Exception, missing comma for "+actualString);
        } catch (Exception e) {
            String expectedMessage = "String missing closing double quote 'first\"second\"'";
            assertEquals(expectedMessage, e.getMessage());
        }
    }

    /**
     * Missing double quote on field.
     * 
     * @throws Exception
     */
    public void testMissingClosingQuotes() throws Exception {

        // Missing closing double quote for second field.

        String actualString = "\"first\",\"second , \"third field\", \"fourth field\"";
        
        try {
            CommaSeparatedValueParser.parseLine(actualString);
            fail("Expecting Exception thrown for no closing double quote '" + actualString + "'");
        } catch (Exception e) {
            String expectedMessage = "String missing closing double quote '\"first\",\"second , \"third field\", \"fourth field\"'";
            assertEquals(expectedMessage, e.getMessage());
        }

        actualString = "abc,\"fignaught , ponter"; // missing trailing double quote
        try {
            CommaSeparatedValueParser.parseLine(actualString);
            fail("Expecting Exception thrown for no closing double quote '" + actualString + "'");
        } catch (Exception e) {
            
            String expectedMessage = "String missing closing double quote 'abc,\"fignaught , ponter'";
            assertEquals(expectedMessage, e.getMessage());
        }
    }

    /**
     * Missing commas between fields.
     */
    public void testMissingClosingQuotesTwo() {

        String[] sampleStrings = { //
                "first;second;,;third;", // missing comma between first and second
                "first,;second;third", // missing comma between second and third
        };
        
        String [] expected = { //
                //
                "String missing closing double quote 'first\"second\",\"third\"'",
                "String missing closing double quote 'first,\"second\"third'", //
        };

        for (int i = 0; i < sampleStrings.length; i++) {

            String inputString = sampleStrings[i].replaceAll(";", QUOTE);
            
            try {
                CommaSeparatedValueParser.parseLine(inputString);
                fail("Expecting parsing Exception (expected comma) thrown for '" + inputString + "'");
            } catch (Exception e) {
                String expectedMessage = expected[i];
//                writeQuotedString("", e.getMessage());
                assertEquals(expectedMessage, e.getMessage());
            }
        }
    }

    protected void writeQuotedString(String string, String message) {
        System.out.println(string + "\""+message.replaceAll("\"", "\\\\\"")+"\"");
    }

}
