package edu.csus.ecs.pc2.core.security;

import java.io.File;
import java.io.IOException;

import edu.csus.ecs.pc2.core.util.AbstractTestCase;

/**
 * JUnit test for FileSecurity.
 * 
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */

// $HeadURL$
public class FileSecurityTest extends AbstractTestCase {

    private String passwordString = "ThisPassword";

    public FileSecurityTest() {
        super();
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

   

    /**
     * Removes all files and subdirectories.
     * 
     * @param dirName
     *            directory to start removing files from.
     * @return true if all files removed, else false
     */
    protected static boolean insureDirRemoved(String dirName) {
        File dir = null;
        boolean result = true;

        dir = new File(dirName);
        if (!dir.exists()) {
            return true; // nothing there, all done!!
        }

        String[] filesToRemove = dir.list();
        for (String dirEntryName : filesToRemove) {

            File file = new File(dirName + File.separator + dirEntryName);

            if (file.isDirectory()) {
                // recurse through any directories
                result &= insureDirRemoved(dirName + File.separator + dirEntryName);
            }
            result &= file.delete();
        }
        return (result);
    }

    public void testSaveWriteRead() {
        
        String testDir = getOutputDataDirectory(this.getName());
        
        FileSecurity fileSecurity = new FileSecurity(testDir);

        try {
            fileSecurity.saveSecretKey(passwordString.toCharArray());
        } catch (FileSecurityException e) {
            // 
            e.printStackTrace();
        }

        String cryptedFileName = testDir + File.separator + "secure.sld";

        try {
            fileSecurity.store(cryptedFileName, "SECRETINFORMATION");
        } catch (FileSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            String st = (String) fileSecurity.load(cryptedFileName);
            assertEquals("SECRETINFORMATION", st);
        } catch (FileSecurityException e) {
            e.printStackTrace();
            assert (false);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void testVerifyPassword() {

        String dirname = getOutputDataDirectory(this.getName()); 

        FileSecurity fileSecurity = new FileSecurity(dirname);

        String cryptedFileName = dirname + File.separator + "secure.fil";

        String password = "foobar";

        try {

            fileSecurity.saveSecretKey(password.toCharArray());

            fileSecurity.verifyPassword(password.toCharArray());
        } catch (FileSecurityException exception) {
            failTest("Exception writeSealedFile " + cryptedFileName, exception);
            // System.out.println("debug Exception "+exception.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            failTest("Exception writeSealedFile " + cryptedFileName, e);
        }
    }

    public void testVerifyPasswordNegative() {

        /**
         * Purposefully bad directory do not change name
         */
        String dirname = "/baddirname";

        FileSecurity fileSecurity = new FileSecurity(dirname);

        String cryptedFileName = dirname + File.separator + "secure.fil";

        String password = "foobar";

        // Negative Test

        try {
            fileSecurity.verifyPassword(password.toCharArray());
        } catch (FileSecurityException exception) {
            assert (true);
            // System.out.println("debug Exception "+exception.getMessage());
        } catch (Exception e) {
            e.printStackTrace();
            failTest("Exception writeSealedFile " + cryptedFileName, e);
        }

    }

    public void testWriteSealedFileNegative() {

        /**
         * Purposefully bad directory do not change name
         */
        String badDirName = "/baddirname"; // save to bad directory

        FileSecurity fileSecurity = new FileSecurity(badDirName);

        String cryptedFileName = badDirName + File.separator + "secure.fil";

        try {
            fileSecurity.writeSealedFile(cryptedFileName, "SECRETINFORMATION");
        } catch (FileSecurityException exception) {
            assert (true); // bad dir does exist - this is what should happen
        } catch (Exception e) {
            e.printStackTrace();
            failTest("Exception writeSealedFileNegative " + cryptedFileName, e);
        }
    }

    /*
     * Test method for 'edu.csus.ecs.pc2.core.security.FileSecurity.getContestDirectory()'
     */
    public void testGetContestDirectory() {
        String dirname = getOutputDataDirectory(this.getName());

        FileSecurity security = new FileSecurity(dirname);
        assertEquals("getContestDirectory", dirname + File.separator, security.getContestDirectory());

    }

    /*
     * Test method for 'edu.csus.ecs.pc2.core.security.FileSecurity.saveSecretKey(PublicKey, String)'
     */
    public void testSaveSecretKeyPublicKeyString() {

    }

    /*
     * Test method for 'edu.csus.ecs.pc2.core.security.FileSecurity.saveSecretKey(char[])'
     */
    public void testSaveSecretKeyCharArray() {

    }

    /*
     * Test method for 'edu.csus.ecs.pc2.core.security.FileSecurity.getPassword()'
     */
    public void testGetPassword() {

        String dirname = getOutputDataDirectory(this.getName());

        try {
            FileSecurity security = new FileSecurity(dirname);

            String password = "miwok";

            security.saveSecretKey(password.toCharArray());

            String readPassword = security.getPassword();

            assertEquals(password, readPassword);

        } catch (Exception e) {
            failTest("Unexpected exception in getPassword  ", e);
        }

    }

}
