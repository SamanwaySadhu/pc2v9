package edu.csus.ecs.pc2.validator.pc2Validator;

import java.io.File;
import java.util.Hashtable;
import java.util.UUID;

import edu.csus.ecs.pc2.core.execute.IResultsParser;
import edu.csus.ecs.pc2.core.execute.XMLResultsParser;
import edu.csus.ecs.pc2.core.util.AbstractTestCase;
import edu.csus.ecs.pc2.validator.clicsValidator.ClicsValidator;

/**
 * JUnit test cases for Validator.
 * 
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */

// $HeadURL$
public class PC2ValidatorTest extends AbstractTestCase {

    private boolean unitDebug = true;

    private String testDataDirectoryName = null;

    private String testOutputDirectoryName = null;

    private static final String DATA_DIR_PATH = "validator";

//    unused private static final String OUT_DIR_PATH = "testing" + File.separator + "validator" + File.separator + "output";

    public PC2ValidatorTest(String name) {
        super(name);
        
        //insure no "exitcode" file exists
        File runtimeErrorExitCodeFile = new File(ClicsValidator.PC2_RUNTIME_ERROR_EXITCODE_FILE_NAME);
        if (runtimeErrorExitCodeFile.exists()) {
            runtimeErrorExitCodeFile.delete();
        }
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();

        // Directory where test data is
//        String testDir = "testdata";
//        String projectPath = JUnitUtilities.locate(testDir);
//        if (projectPath == null) {
//            throw new Exception("Unable to locate " + testDir);
//        }

//        testDataDirectoryName = projectPath + File.separator + DATA_DIR_PATH;
//        testOutputDirectoryName = projectPath + File.separator + OUT_DIR_PATH;

        testDataDirectoryName = getRootInputTestDataDirectory() + File.separator + DATA_DIR_PATH;
        testOutputDirectoryName = getOutputDataDirectory();
        ensureOutputDirectory();

        assertDirectoryExists(testDataDirectoryName);
        assertDirectoryExists(testOutputDirectoryName);
        
    }

    /**
     * Return full path and filename to test data directory.
     * 
     * @param filename
     */
    private String getFullPathName(String filename) {
        return testDataDirectoryName + File.separator + filename;
    }

    /**
     * return a random filename (no path).
     * 
     * @param prefix
     *            an optional string prefixed to the filename.
     * @return
     */
    protected String randomFileName(String prefix) {
        if (prefix == null) {
            prefix = "";
        }

        UUID uuid = UUID.randomUUID();

        return prefix + uuid.toString();
    }

    protected String randomOutputFileName(String prefix) {
        return testOutputDirectoryName + File.separator + randomFileName(prefix);
    }

    /**
     * Test all validator options.
     */
    public void testAll() {

        // -pc2 test options
        // 1 - diff
        // 2 - ignore whitespace at start of file
        // 3 - ignore leading whitespace on lines
        // 4 - ignore all whitespace on lines
        // 5 - ignore empty lines
        // 6 2 & 3
        // 7 2 & 4
        // 8 2 & 5
        // 9 3 & 5
        // 10 4 & 5

        int[] pc2TestNumbers = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

        for (int i = 0; i < pc2TestNumbers.length; i++) {
            int pc2TestNumber = pc2TestNumbers[i];

            String outcome = PC2Validator.JUDGEMENT_YES;

            validateOneLineIdentical(pc2TestNumber, outcome);
            validateTwoEmptyFiles(pc2TestNumber, outcome);
            validateFileSame(pc2TestNumber, outcome);

            outcome = PC2Validator.JUDGEMENT_NO_WRONG_ANSWER;

            validateOneLineDifferent(pc2TestNumber, outcome);
            validateOneLineEmpty(pc2TestNumber, outcome);
            validateOneLineBlank(pc2TestNumber, outcome);
            validateFileDiff1stChar(pc2TestNumber, outcome);
            validateFileDiffMissLine(pc2TestNumber, outcome);
            validateFileDiff(pc2TestNumber, outcome);
        }
    }

    /**
     * Test all validator options.
     */
    public void testNegativeCases() {
        int[] pc2TestNumbers = { 1, 2, 3, 4, 5, 6, 7, 8, 9, 10 };

        for (int i = 0; i < pc2TestNumbers.length; i++) {
            int pc2TestNumber = pc2TestNumbers[i];

            String expectedOutcome = "Yes";

            String outputFilename = getFullPathName("testfile.txt");
            String answerFilename = getFullPathName("testfile.txt");
            runNegativeValidatorTest(outputFilename, answerFilename, expectedOutcome, pc2TestNumber);

            expectedOutcome = PC2Validator.JUDGEMENT_YES;

            outputFilename = getFullPathName("testfile.txt");
            answerFilename = getFullPathName("testdatain.txt");
            runNegativeValidatorTest(outputFilename, answerFilename, expectedOutcome, pc2TestNumber);
        }
    }

    /**
     * Test two identical files, one line per file.
     */
    public void validateOneLineIdentical(int pc2TestNumber, String expectedOutcome) {

        String outputFilename = getFullPathName("oneline1.txt");

        runValidatorTest(outputFilename, outputFilename, expectedOutcome, pc2TestNumber);
    }

    /**
     * Test two files, single line, different contents.
     */
    public void validateOneLineDifferent(int pc2TestNumber, String expectedOutcome) {

        String outputFilename = getFullPathName("oneline1.txt");

        String answerFilename = getFullPathName("oneline2.txt");

        runValidatorTest(outputFilename, answerFilename, expectedOutcome, pc2TestNumber);
    }

    /**
     * Test two files, both empty (0 byte) files.
     */
    public void validateTwoEmptyFiles(int pc2TestNumber, String expectedOutcome) {

        String outputFilename = getFullPathName("emptyfile");

        String answerFilename = getFullPathName("emptyfile");

        runValidatorTest(outputFilename, answerFilename, expectedOutcome, pc2TestNumber);
    }

    /**
     * Test two files, first file empty, second file one line.
     */
    public void validateOneLineEmpty(int pc2TestNumber, String expectedOutcome) {

        String outputFilename = getFullPathName("emptyfile");

        String answerFilename = getFullPathName("oneline1.txt");

        runValidatorTest(outputFilename, answerFilename, expectedOutcome, pc2TestNumber);
    }

    /**
     * Test two files, firs file one empty line, second file one line.
     */
    public void validateOneLineBlank(int pc2TestNumber, String expectedOutcome) {

        String outputFilename = getFullPathName("oneline0.txt");

        String answerFilename = getFullPathName("oneline1.txt");

        runValidatorTest(outputFilename, answerFilename, expectedOutcome, pc2TestNumber);
    }

    /**
     * Test two identical multi line files.
     */
    public void validateFileSame(int pc2TestNumber, String expectedOutcome) {

        String outputFilename = getFullPathName("testfile.txt");

        String answerFilename = getFullPathName("testfile.txt");

        runValidatorTest(outputFilename, answerFilename, expectedOutcome, pc2TestNumber);
    }

    /**
     * Test a multi line output and answer, same contents
     */
    public void validateFileDiff(int pc2TestNumber, String expectedOutcome) {

        String outputFilename = getFullPathName("testfile.txt");

        String answerFilename = getFullPathName("testdatain.txt");

        runValidatorTest(outputFilename, answerFilename, expectedOutcome, pc2TestNumber);
    }

    /**
     * Test only the first character is different.
     */
    public void validateFileDiff1stChar(int pc2TestNumber, String expectedOutcome) {

        String outputFilename = getFullPathName("testfile.txt");

        String answerFilename = getFullPathName("testfile2.txt");

        runValidatorTest(outputFilename, answerFilename, expectedOutcome, pc2TestNumber);
    }

    /**
     * Test only the last character is different.
     */
    public void validateFileDiffLastChar(int pc2TestNumber, String expectedOutcome) {

        String outputFilename = getFullPathName("testfile.txt");

        String answerFilename = getFullPathName("testfileLastCh.txt");

        runValidatorTest(outputFilename, answerFilename, expectedOutcome, pc2TestNumber);
    }

    /**
     * Test if a single line is missing.
     */
    public void validateFileDiffMissLine(int pc2TestNumber, String expectedOutcome) {

        String outputFilename = getFullPathName("testfile.txt");

        String answerFilename = getFullPathName("testfileML.txt");

        runValidatorTest(outputFilename, answerFilename, expectedOutcome, pc2TestNumber);
    }

    /**
     * Runs the Validator comparing contents of two files.
     * 
     * Runs the validator against outputFileName and answerFileName with expected outcome/judgement of judgementString.
     * 
     * @param outputFileName
     * @param answerFileName
     * @param judgementString
     *            expected judgement text
     */
    protected void runValidatorTest(String outputFileName, String answerFileName, String judgementString, int pc2TestNumber) {

        String inFilename = getFullPathName("testdatain.txt");

        String resultFilename = randomOutputFileName("result");

        String results = validatorResultXML(inFilename, outputFileName, answerFileName, resultFilename, pc2TestNumber, false);

        if (isUnitDebug() && !judgementString.equals(results)) {
            System.err.println("Failed " + judgementString + " vs (validator) " + results);
            System.err.println("  Out: " + outputFileName);
            System.err.println("  Val: " + answerFileName);
            System.err.println("  Out: " + resultFilename);
            System.err.println("  Cmd: validator " + inFilename + " " + outputFileName + " " + answerFileName + " " + resultFilename + " -pc2 " + pc2TestNumber + " " + false);
        }

        assertEquals("Failed -pc2 " + pc2TestNumber, results, judgementString);
    }

    /**
     * Negative test, judgementString should NOT match validator results.
     * 
     * Test will fail if validator results match judgementString.
     * 
     * @param outputFileName
     * @param answerFileName
     * @param judgementString
     * @param pc2TestNumber
     */
    protected void runNegativeValidatorTest(String outputFileName, String answerFileName, String judgementString, int pc2TestNumber) {

        String inFilename = getFullPathName("testdatain.txt");

        String resultFilename = randomOutputFileName("result");

        String results = validatorResultXML(inFilename, outputFileName, answerFileName, resultFilename, pc2TestNumber, false);

        if (isUnitDebug() && judgementString.equals(results)) {
            System.err.println("Failed Negative test " + judgementString + " vs (validator) " + results);
            System.err.println("  Out: " + outputFileName);
            System.err.println("  Val: " + answerFileName);
            System.err.println("  Out: " + resultFilename);
            System.err.println("  Cmd: validator " + inFilename + " " + outputFileName + " " + answerFileName + " " + resultFilename + " -pc2 " + pc2TestNumber + " " + false);
        }

        assertNotSame("Failed negative test -pc2 " + pc2TestNumber, results, judgementString);
    }

    /**
     * Run Validator and return results.
     * 
     * 
     * @param inName
     * @param outName
     * @param ansName
     * @param resFile
     * @param pc2TestNumber
     * @param ignoreCase
     * @return null or outcome string from XML generated by validator.
     */
    protected String validatorResultXML(String inputFileName, String outputFileName, String answerFileName, String resultsFileName, int pc2TestNumber, boolean ignoreCase) {

        /*
         * Validator command line syntax: Validator <inputfile name> <outputfile name> <answerfile name> <resultfile name> <-pc2> [options] icflag
         */

        try {

            String[] args = { inputFileName, outputFileName, answerFileName, resultsFileName, "-pc2", "" + pc2TestNumber, "" + ignoreCase };
            PC2Validator.main(args);

            IResultsParser parser = new XMLResultsParser();
            // parser.setLog(log);
            boolean done = parser.parseValidatorResultsFile(resultsFileName);
            
            if ( parser.getException() != null){
                parser.getException().printStackTrace();
                throw parser.getException();
            }
            Hashtable<String, String> results = parser.getResults();

            if (done && results != null && results.containsKey("outcome")) {
                return results.get("outcome");
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public boolean isUnitDebug() {
        return unitDebug;
    }

    public void setUnitDebug(boolean unitDebug) {
        this.unitDebug = unitDebug;
    }
}
