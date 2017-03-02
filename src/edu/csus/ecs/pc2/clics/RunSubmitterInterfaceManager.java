package edu.csus.ecs.pc2.clics;

import java.io.File;
import java.io.IOException;

import edu.csus.ecs.pc2.core.CommandVariableReplacer;
import edu.csus.ecs.pc2.core.IInternalController;
import edu.csus.ecs.pc2.core.Utilities;
import edu.csus.ecs.pc2.core.execute.ExecutionData;
import edu.csus.ecs.pc2.core.log.Log;
import edu.csus.ecs.pc2.core.model.ContestInformation;
import edu.csus.ecs.pc2.core.model.IInternalContest;
import edu.csus.ecs.pc2.core.model.Problem;
import edu.csus.ecs.pc2.core.model.ProblemDataFiles;
import edu.csus.ecs.pc2.core.model.Run;
import edu.csus.ecs.pc2.core.model.RunFiles;
import edu.csus.ecs.pc2.core.model.SerializedFile;
import edu.csus.ecs.pc2.ui.UIPlugin;

/**
 * Manages sending of Run Submissions via Run Submission Interface.
 * 
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */

// $HeadURL$
public class RunSubmitterInterfaceManager implements UIPlugin {

    private static final long serialVersionUID = -7193057846912064771L;

    private IInternalContest contest;

    private IInternalController controller;

    private CommandVariableReplacer commandVariableReplacer = new CommandVariableReplacer();

    /**
     * Location where run files are stored and sent from.
     */
    private String stagingDirectory = "rsi";

    public void setContestAndController(IInternalContest inContest, IInternalController inController) {
        this.contest = inContest;
        this.controller = inController;

        ContestInformation contestInformation = inContest.getContestInformation();

        if (contestInformation == null) {
            info("No Run Submission Interface defined");
        } else if (contestInformation.getRsiCommand() == null) {
            info("No Run Submission Interface defined");
        } else {
            info("Run Submission Interface command is: " + contestInformation.getRsiCommand());
        }
    }

    private void info(String string) {
        if (controller != null && controller.getLog() != null) {
            controller.getLog().info(string);
        } else if (Utilities.isDebugMode()) {
            System.err.println(string);
        }
    }

    private void warn(String string) {
        if (controller != null && controller.getLog() != null) {
            controller.getLog().log(Log.WARNING, string);
        } else if (Utilities.isDebugMode()) {
            System.err.println(string);
        }
    }

    private void warn(String string, Exception ex) {
        if (controller != null && controller.getLog() != null) {
            controller.getLog().log(Log.WARNING, string, ex);
        } else if (Utilities.isDebugMode()) {
            System.err.println(string);
            ex.printStackTrace(System.err);
        }
    }

    public String getPluginTitle() {
        return "Run Submitter Interface Manager";
    }

    /**
     * 
     * @param run
     * @param runFiles
     * @throws Exception
     */
    public void sendRun(Run run, RunFiles runFiles) throws Exception {

        String command = contest.getContestInformation().getRsiCommand();

        if (command == null || command.trim().length() == 0) {
            /**
             * No command, nothing to run.
             */
            return;
        }

        if (command.trim().startsWith("#")) {
            if (Utilities.isDebugMode()) {
                info("RSI ignoring command: " + command);
            }
            return;
        }

        info("RSI sending run " + run);

        info("RSI Command before: " + command);

        Utilities.insureDir(stagingDirectory);

        /**
         * Create per run directory
         */

        String runDir = stagingDirectory + File.separator + "rsis" + run.getSiteNumber() + "r" + run.getNumber();
        Utilities.insureDir(runDir);

        if (new File(runDir).isDirectory()) {

            try {

                String mainfileName = commandVariableReplacer.getMainFileName(runDir, runFiles);

                createFile(runFiles.getMainFile(), mainfileName);
                info("RSI wrote " + mainfileName);
                
                if (runFiles.getOtherFiles() != null) {
                    for (SerializedFile file : runFiles.getOtherFiles()) {
                        String outfilename = runDir + File.separator + file.getName();
                        createFile(file, outfilename);
                        info("RSI wrote addnl file:  " + outfilename);
                    }
                }
                
                Problem problem = contest.getProblem(run.getProblemId());
                ProblemDataFiles problemDataFiles = contest.getProblemDataFile(problem);

                ExecutionData executionData = null;
                
                String newCommand = commandVariableReplacer.substituteVariables(command, contest, run, runFiles, runDir, executionData, problemDataFiles);

                info("RSI Command  after: " + newCommand);

                String[] env = null;
                File directory = new File(".");
                info("RSI execute dir: " + Utilities.getCurrentDirectory());
                info("RSI execute cmd: " + newCommand);
                // Process process = Runtime.getRuntime().exec(newCommand, env, directory);
                Runtime.getRuntime().exec(newCommand, env, directory);

                // process.waitFor();

                info("RSI sent run " + run);

            } catch (IOException e) {
                info("RSI run NOT sent " + run);
                warn(e.getMessage(), e);
            }
        } else {
            warn("Unable to create directory for run " + runDir);
        }
    }


    /**
     * Create disk file for input SerializedFile.
     * 
     * Returns true if file is written to disk and is not null.
     * 
     * @param file
     * @param outputFileName
     * @return true if file written to disk.
     * @throws IOException
     */
    boolean createFile(SerializedFile file, String outputFileName) throws IOException {
        if (file != null && outputFileName != null) {
            file.writeFile(outputFileName);
            return new File(outputFileName).isFile();
        }

        return false;
    }

  
    /**
     * Return string minus last extension. <br>
     * Finds last . (period) in input string, strips that period and all other characters after that last period. If no period is found in string, will return a copy of the original string. <br>
     * Unlike the Unix basename program, no extension is supplied.
     * 
     * @param original
     *            the input string
     * @return a string with all text after last . removed
     */
    public String removeExtension(String original) {
        String outString = new String(original);

        // Strip off all text after and including final dot

        int dotIndex = outString.lastIndexOf('.', outString.length() - 1);
        if (dotIndex != -1) {
            outString = outString.substring(0, dotIndex);
        }

        return outString;
    }
}
