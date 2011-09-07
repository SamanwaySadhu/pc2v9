package edu.csus.ecs.pc2.ui.team;

import java.util.Arrays;
import java.util.Date;

import edu.csus.ecs.pc2.api.IClient;
import edu.csus.ecs.pc2.api.IContest;
import edu.csus.ecs.pc2.api.ILanguage;
import edu.csus.ecs.pc2.api.IProblem;
import edu.csus.ecs.pc2.api.IRun;
import edu.csus.ecs.pc2.api.IRunComparator;
import edu.csus.ecs.pc2.api.ServerConnection;
import edu.csus.ecs.pc2.api.exceptions.LoginFailureException;
import edu.csus.ecs.pc2.api.listener.IRunEventListener;
import edu.csus.ecs.pc2.core.InternalController;
import edu.csus.ecs.pc2.core.ParseArguments;
import edu.csus.ecs.pc2.core.Utilities;
import edu.csus.ecs.pc2.core.model.ClientId;

/**
 * Command line submit run.
 * 
 * Uses the API.
 * 
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */

// TODO CCS - add ability to submit additional source files.
// TODO CCS - add -test

// $HeadURL$
public class Submitter {

    private ServerConnection serverConnection = null;

    private String login;

    private String password;

    private String languageTitle;

    private String problemTitle;

    private IContest contest;

    private IProblem submittedProblem;

    private ILanguage submittedLanguage;

    private IClient submittedUser;

    /**
     * --check option.
     * 
     */
    private boolean checkArg = false;

    /**
     * Filename for source to be submitted.
     */
    private String submittedFileName;

    protected Submitter() {

    }

    public Submitter(String login) {
        super();
        this.login = login;
        setLoginPassword(login);
    }

    /**
     * Login with login and password.
     * 
     * @param login
     * @param password
     */
    public Submitter(String login, String password) {
        super();
        this.login = login;
        this.password = password;
        setLoginPassword(login);
    }

    public Submitter(String[] args) {
        loadVariables(args);
    }

    /**
     * Expand shortcut names.
     * 
     * @param loginName
     */
    private void setLoginPassword(String loginName) {

        ClientId id = InternalController.loginShortcutExpansion(1, loginName);
        if (id != null) {

            login = id.getName();

            if (password == null) {
                password = login;
            }

        }
    }

    private void loadVariables(String[] args) {

        if (args.length == 0 || args[0].equals("--help")) {
            usage();
            System.exit(4);
        }

        String[] opts = { "--login", "--password" };
        ParseArguments arguments = new ParseArguments(args, opts);

        checkArg = arguments.isOptPresent("--check");

        String cmdLineLogin = null;
        String cmdLinePassword = null;

        if (arguments.isOptPresent("--login")) {
            cmdLineLogin = arguments.getOptValue("--login");
        }

        if (arguments.isOptPresent("--password")) {
            cmdLinePassword = arguments.getOptValue("--password");
        }

        if (cmdLineLogin != null) {
            password = cmdLinePassword;
            setLoginPassword(cmdLineLogin);
        }

        if (arguments.isOptPresent("--list")) {
            listInfo();
            System.exit(0);
        } else if (arguments.isOptPresent("--listruns")) {
            listRuns();
            System.exit(0);
        } else {

            submittedFileName = arguments.getArg(0);
            if (submittedFileName == null) {
                System.err.println("Error - missing filename");
                System.exit(4);
            }

            if (arguments.getArgCount() > 1) {
                problemTitle = arguments.getArg(1);
            }

            if (arguments.getArgCount() > 2) {
                languageTitle = arguments.getArg(2);
            }

        }

        if (password == null) {
            password = login;
        }
    }

    private String getLanguageFromFilename(String filename2) {

        if (filename2.endsWith(".java")) {
            return findLanguageName("Java");
        } else if (filename2.endsWith(".cpp")) {
            return findLanguageName("C++");
        } else if (filename2.endsWith(".c")) {
            return findLanguageName("C");
        } else {
            return languageTitle;
        }
    }

    protected String findLanguageName(String string) {

        for (ILanguage language : contest.getLanguages()) {
            if (language.getName().equalsIgnoreCase(string)) {
                return language.getName();
            } else if (language.getName().indexOf(string) > -1) {
                return language.getName();
            }
        }
        return string;
    }

    protected String getProblemNameFromFilename(String filename) {

        String baseName = Utilities.basename(filename);

        // Strip extension
        int lastIndex = baseName.lastIndexOf('.');
        if (lastIndex > 1) {
            baseName = baseName.substring(0, lastIndex - 1);
        }

        IProblem problem = matchProblem(baseName);
        if (problem != null) {
            return problem.getName();
        } else {
            return baseName;
        }
    }

    private void usage() {
        String[] usage = { //
        "Usage Submitter [--help|--list|--listruns|--check] --login loginname [--password password] filename [problem [language]]", //
                "Usage Submitter [-F propfile] [--help|--list|--listruns|--check] filename [problem [language]]", //
                "", //
                "Submit filename for problem and language.  If problem or language", //
                "not specified the program will guess which problem and language", //
                "based on the file name.", //
                "", //
                "--help   this listing", //
                "", //
                "--check  check parameters, list filename, problem and language", //
                "", //
                "--list   list problem and languages", //
                "", //
                "--listruns list run info for the user", //
                "", //
                "On success exit code will be 0", //
                "On failure exit code will be non-zero", //
                "", //
                "$Id$", //
        };

        for (String s : usage) {
            System.out.println(s);
        }
    }

    public void submitRun(String mainfilename, String problemName, String languageName) {
        submittedFileName = mainfilename;
        problemTitle = problemName;
        languageTitle = languageName;
        submitRun();
    }

    private String getProblemNameFromLetter(char letter) {
        try {
            letter = Character.toUpperCase(letter);
            int idx = letter - 'A';
            IProblem[] problems = contest.getProblems();
            return problems[idx].getName();
        } catch (Exception e) {
            return new Character(letter).toString();
        }
    }

    /**
     * Submit a run.
     */
    public void submitRun() {

        boolean success = false;

        try {

            checkParams();

            serverConnection = new ServerConnection();

            contest = serverConnection.login(login, password);
            
            System.out.println("For: "+contest.getMyClient().getDisplayName()+" ("+contest.getMyClient().getLoginName()+")");
            System.out.println();

            try {

                // Register for run event.

                RunEventListener runliEventListener = new RunEventListener();
                contest.addRunListener(runliEventListener);

                submitTheRun(problemTitle, languageTitle, submittedFileName);

                waitForRunSubmissionConfirmation(runliEventListener, 3);

                IRun run = runliEventListener.getRun();

                if (runliEventListener.getRun() != null) {
                    // got a run
                    success = true;

                    System.out.println("Submission confrmation: Run " + run.getNumber() + " problem " + run.getProblem().getName() + " for team " + run.getTeam().getLoginName());

                } // no else
                
                serverConnection.logoff();

            } catch (Exception e) {
                System.err.println(e.getMessage());
            }
            
        } catch (LoginFailureException e) {
            System.out.println("Unable to login: " + e.getMessage());
        }

        if (success) {
            System.exit(0);
        } else {
            System.exit(4);
        }
    }

    /**
     * Check that they have supplied required parameters.
     * 
     * @throws LoginFailureException
     */
    private void checkParams() throws LoginFailureException {

        if (login == null) {
            throw new LoginFailureException("No login specified");
        }
        if (password == null) {
            throw new LoginFailureException("No password specified");
        }
    }

    /**
     * Waits for run confirmation.
     * 
     * @param listener
     * @throws Exception
     */
    private void waitForRunSubmissionConfirmation(RunEventListener listener, int seconds) throws Exception {

        boolean done = false;

        long waittime = seconds * 1000;

        long startTime = new Date().getTime();

        long timeLimit = startTime + waittime;

        while (!done) {

            if (listener.getRun() != null) {
                done = true;
            }

            if (new Date().getTime() > timeLimit) {
                Thread.sleep(500);
                break;
            }
        }

        long totalTime = new Date().getTime() - startTime;

        System.out.println(totalTime + " ms");

        if (!done) {
            throw new Exception("Timed out waiting for run submission confirm ");
        }
    }

    /**
     * List who logged in, problems and languages.
     */
    public void listInfo() {

        try {

            checkParams();

            serverConnection = new ServerConnection();

            contest = serverConnection.login(login, password);

            try {
                listInfo(contest);
                
                serverConnection.logoff();

            } catch (Exception e) {
                e.printStackTrace();
            }

        } catch (LoginFailureException e1) {
            System.out.println("Unable to login: " + e1.getMessage());
        }

    }

    /**
     * Login and output runs for login.
     * 
     */
    public void listRuns() {

        try {
            checkParams();

            serverConnection = new ServerConnection();

            contest = serverConnection.login(login, password);
            
            System.out.println("For: "+contest.getMyClient().getDisplayName()+" ("+contest.getMyClient().getLoginName()+")");
            System.out.println();

            IRun[] runs = contest.getRuns();
            if (runs.length == 0) {
                System.out.println("No runs submitted");
            } else {
                System.out.println(runs.length + " runs for " + contest.getMyClient().getDisplayName() + " (" + contest.getMyClient().getLoginName() + ")");
                System.out.println();
                Arrays.sort(runs, new IRunComparator());
                for (IRun run : runs) {
                    System.out.println("Run " + run.getNumber() + " at " + run.getSubmissionTime() + " by " + contest.getMyClient().getLoginName() + //
                            " " + run.getJudgementName() + //
                            " " + run.getProblem().getName() + " " + run.getLanguage().getName());
                }
            }
        } catch (LoginFailureException e1) {
            System.out.println("Unable to login: " + e1.getMessage());
        }

    }

    /**
     * List who logged in, problems and languages.
     * 
     * @param contest2
     */
    private void listInfo(IContest contest2) {

        System.out.println("Logged in as: " + contest2.getMyClient().getDisplayName());

        System.out.println();

        char let = 'A';

        System.out.println("Problems");
        for (IProblem problem : contest.getProblems()) {
            System.out.println(let + " - " + problem.getName());
            let++;
        }

        System.out.println();

        System.out.println("Languages");
        for (ILanguage language : contest.getLanguages()) {
            System.out.println(language.getName());
        }

        System.out.println();
    }

    private void submitTheRun(String problemTitle2, String languageTitle2, String filename2) throws Exception {

        submittedProblem = null;

        submittedLanguage = null;

        if (languageTitle2 == null) {
            languageTitle2 = getLanguageFromFilename(filename2);
        }

        ILanguage language = matchLanguage(languageTitle2);

        if (languageTitle2 == null) {
            throw new Exception("Could not determine Language based on filename '" + filename2 + "'");
        }

        if (language == null) {
            throw new Exception("Could not match language '" + languageTitle2 + "'");
        }

        if (problemTitle2 == null) {
            problemTitle2 = getProblemNameFromFilename(filename2);
        }
        if (problemTitle2 != null && problemTitle2.length() == 1) {
            problemTitle2 = getProblemNameFromLetter(problemTitle2.charAt(0));
        }

        IProblem problem = matchProblem(problemTitle2);

        if (problemTitle2 == null) {
            throw new Exception("Could not determine Problem based on filename '" + filename2 + "'");
        }

        if (problem == null) {
            throw new Exception("Could not match problem '" + problemTitle2 + "'");
        }

        if (checkArg) {

            System.out.println("For   : " + contest.getMyClient().getLoginName() + " - " + contest.getMyClient().getDisplayName());
            System.out.println("File  : " + filename2);
            System.out.println("Prob  : " + problem.getName());
            System.out.println("Lang  : " + language.getName());
            System.exit(0);

        } else {

            serverConnection.submitRun(problem, language, filename2, new String[0]);

            submittedProblem = problem;
            submittedLanguage = language;
            submittedUser = contest.getMyClient();
        }
    }

    /**
     * Find IProblem that matches the title.
     * 
     * Will look for an exact match, then look for a single letter used for problem, then looks for a problem title that starts with the input problem title.
     * 
     * @param problemTitle2
     *            title, letter or partial title.
     * @return
     */
    private IProblem matchProblem(String problemTitle2) {

        for (IProblem problem : contest.getProblems()) {
            if (problem.getName().equalsIgnoreCase(problemTitle2)) {
                return problem;
            }
        }

        char let = 'A';

        for (IProblem problem : contest.getProblems()) {
            if (problem.getName().equalsIgnoreCase(Character.toString(let))) {
                return problem;
            }
            let++;
        }

        for (IProblem problem : contest.getProblems()) {
            if (problem.getName().startsWith(problemTitle2)) {
                return problem;
            }
            let++;
        }

        return null;
    }

    private ILanguage matchLanguage(String languageTitle2) {
        for (ILanguage language : contest.getLanguages()) {
            if (language.getName().equalsIgnoreCase(languageTitle2)) {
                return language;
            }
        }
        return null;
    }

    public ILanguage getSubmittedLanguage() {
        return submittedLanguage;
    }

    public IClient getSubmittedUser() {
        return submittedUser;
    }

    public IProblem getSubmittedProblem() {
        return submittedProblem;
    }

    /**
     * Listen for run events.
     * 
     * @author pc2@ecs.csus.edu
     * @version $Id$
     */

    // $HeadURL$
    protected class RunEventListener implements IRunEventListener, Runnable {

        private IRun submittedRun = null;

        public void runSubmitted(IRun run) {
            this.submittedRun = run;
        }

        public void runDeleted(IRun run) {
            // ignore

        }

        public void runCheckedOut(IRun run, boolean isFinal) {
            // ignore

        }

        public void runJudged(IRun run, boolean isFinal) {
            // ignore

        }

        public void runUpdated(IRun run, boolean isFinal) {
            // ignore

        }

        public void runCompiling(IRun run, boolean isFinal) {
            // ignore
        }

        public void runExecuting(IRun run, boolean isFinal) {
            // ignore
        }

        public void runValidating(IRun run, boolean isFinal) {
            // ignore
        }

        public void runJudgingCanceled(IRun run, boolean isFinal) {
            // ignore
        }

        public void run() {
            // ignore
        }

        public IRun getRun() {
            return submittedRun;
        }
    }

    public static void main(String[] args) {
        Submitter submitter = new Submitter(args);
        submitter.submitRun();
    }
}
