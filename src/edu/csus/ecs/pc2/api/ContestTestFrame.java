package edu.csus.ecs.pc2.api;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Rectangle;
import java.io.Serializable;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import edu.csus.ecs.pc2.VersionInfo;
import edu.csus.ecs.pc2.api.exceptions.LoginFailureException;
import edu.csus.ecs.pc2.api.exceptions.NotLoggedInException;
import edu.csus.ecs.pc2.api.implementation.RunsFrame;
import edu.csus.ecs.pc2.api.implementation.ScrollyFrame;
import edu.csus.ecs.pc2.api.listener.ContestEvent;
import edu.csus.ecs.pc2.api.listener.IConfigurationUpdateListener;
import edu.csus.ecs.pc2.api.listener.IConnectionEventListener;
import edu.csus.ecs.pc2.api.listener.IRunEventListener;
import edu.csus.ecs.pc2.api.reports.APIAbstractTest;
import edu.csus.ecs.pc2.api.reports.APIPrintReports;
import edu.csus.ecs.pc2.api.reports.PrintClarification;
import edu.csus.ecs.pc2.api.reports.PrintClarificationCategories;
import edu.csus.ecs.pc2.api.reports.PrintClarifications;
import edu.csus.ecs.pc2.api.reports.PrintMyClient;
import edu.csus.ecs.pc2.api.reports.PrintProblems;
import edu.csus.ecs.pc2.api.reports.PrintRun;
import edu.csus.ecs.pc2.api.reports.PrintStandings;
import edu.csus.ecs.pc2.api.reports.PrintTeams;
import edu.csus.ecs.pc2.core.InternalController;
import edu.csus.ecs.pc2.core.ParseArguments;
import edu.csus.ecs.pc2.core.model.ClientId;
import edu.csus.ecs.pc2.ui.FrameUtilities;
import edu.csus.ecs.pc2.ui.IntegerDocument;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * API 'contest' Test Frame.
 * 
 * Shows output of 'get' and 'is' methods for API Contest and ServerConnection classes.
 * 
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */

// $HeadURL$
public class ContestTestFrame extends JFrame {

    /**
     * 
     */
    private static final long serialVersionUID = -3146831894495294017L;

    private static final String DEFAULT_TITLE = "PC^2 API Test Frame [NOT LOGGED IN]";
    
    private JPanel mainPain = null;

    private JPanel centerPane = null;

    private JPanel buttonPane = null;

    private JButton exitButton = null;

    private JButton getRunButton = null;

    private JButton loginButton = null;

    private JTextField loginTextField = null;

    private JTextField passwordTextField = null;

    private ServerConnection serverConnection = new ServerConnection();

    private IContest contest = null;

    private JButton logoffButton = null;

    private JCheckBox runListenerCheckBox = null;

    private JCheckBox configListenerCheckBox = null;

    private RunListener runListener = null;

    private ConnectionEventListener connectionEventListener = null;

    private ScrollyFrame scrollyFrame = new ScrollyFrame();

    private String line = "";

    private JButton standingsButton = null;

    private JButton printAllButton = null;

    private JButton oneRunTest = null;

    private JCheckBox clarListenerCheckBox = null;

    private ClarificationListener clarificationListener = null;

    private ConfigUpdateListener configurationUpdateListener = null;

    private JButton runContestButton = null;

    private JScrollPane reportScrollPane = null;

    private JList<APIAbstractTest> reportJList = null;

    private DefaultListModel<APIAbstractTest> listModel = new DefaultListModel<APIAbstractTest>();

    private JPanel topPane = null;

    private JLabel loginLabel = null;

    private JLabel passwordLabel = null;

    private JPanel eastPane = null;

    private JPanel mainPanel = null;

    private JTextField numberTextField = null;

    private JLabel jLabel = null;

    private JPanel eastButtonPane = null;

    private APIAbstractTest[] reportsList = new APIPrintReports().getReportsList();

    private JLabel jLabel1 = null;

    private JComboBox<ISiteWrapper> siteComboBox = null;

    private JLabel runsOnSite = null;

    private JButton getClarificationButton = null;

    private JLabel clarificationsOnSite = null;

    private JCheckBox connectionListenerCheckBox = null;

    private JButton viewRunGrid = null;

    private RunsFrame runsFrame = null;

    /**
     * This method initializes
     * 
     */
    public ContestTestFrame() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     * 
     */
    private void initialize() {
        this.setSize(new java.awt.Dimension(650, 552));
        this.setDefaultCloseOperation(javax.swing.JFrame.EXIT_ON_CLOSE);
        this.setContentPane(getMainPain());
        this.setTitle(DEFAULT_TITLE);

        FrameUtilities.setFramePosition(this, FrameUtilities.HorizontalPosition.LEFT, FrameUtilities.VerticalPosition.CENTER);

        loadReportList();

    }

    /**
     * Comparer for APIAbstractTestComparator title.
     * 
     * @author pc2@ecs.csus.edu
     * @version $Id$
     */
    protected class APIAbstractTestComparator implements Comparator<APIAbstractTest>, Serializable {

        /**
         * 
         */
        private static final long serialVersionUID = 2262035579714795746L;

        public int compare(APIAbstractTest abstractTest1, APIAbstractTest abstractTest2) {
            return abstractTest1.getTitle().compareTo(abstractTest2.getTitle());
        }
    }

    /**
     * Load report list
     * 
     */
    private void loadReportList() {

        Arrays.sort(reportsList, new APIAbstractTestComparator());

        for (APIAbstractTest abstractTest : reportsList) {
            listModel.addElement(abstractTest);
        }
    }

    protected void printAllJudgements(IRun run) {
        IRunJudgement[] judgements = run.getRunJudgements();
        if (judgements.length == 0) {
            println("   No judgements for this run.");
        }
        for (IRunJudgement judgement : judgements) {
            println("   Judgement " + judgement.getJudgement().getName() + " compJudged=" + judgement.isComputerJudgement() + " solved=" + judgement.isSolved() + " prelim="
                    + judgement.isPreliminaryJudgement() + " toTeams=" + judgement.isSendToTeam());
        }
    }

    /**
     * Run Listener for ContestTestFrame.
     * 
     * @author pc2@ecs.csus.edu
     * @version $Id$
     */

    // $HeadURL$
    protected class RunListener implements IRunEventListener {

        public void runJudged(IRun run, boolean isFinal) {
            println("Run judged Site " + run.getSiteNumber() + " Run " + run.getNumber() + " from " + run.getTeam().getLoginName() + " at " + run.getSubmissionTime());
            printAllJudgements(run);
        }

        public void runUpdated(IRun run, boolean isFinal) {
            println("Run updated Site " + run.getSiteNumber() + " Run " + run.getNumber() + " from " + run.getTeam().getLoginName() + " at " + run.getSubmissionTime());
            printAllJudgements(run);
        }

        public void runExecuting(IRun run, boolean isFinal) {
            println("Run executing Site " + run.getSiteNumber() + " Run " + run.getNumber() + " from " + run.getTeam().getLoginName() + " at " + run.getSubmissionTime());
        }

        public void runValidating(IRun run, boolean isFinal) {
            println("Run validating Site " + run.getSiteNumber() + " Run " + run.getNumber() + " from " + run.getTeam().getLoginName() + " at " + run.getSubmissionTime());
        }

        public void runSubmitted(IRun run) {
            println("Run submitted Site " + run.getSiteNumber() + " Run " + run.getNumber() + " from " + run.getTeam().getLoginName() + " at " + run.getSubmissionTime());
        }

        public void runDeleted(IRun run) {
            println("Run deleted Site " + run.getSiteNumber() + " Run " + run.getNumber() + " from " + run.getTeam().getLoginName() + " at " + run.getSubmissionTime());
        }

        public void runCompiling(IRun run, boolean isFinal) {
            println("Run compiling Site " + run.getSiteNumber() + " Run " + run.getNumber() + " from " + run.getTeam().getLoginName() + " at " + run.getSubmissionTime());
        }

        public void runJudgingCanceled(IRun run, boolean isFinal) {
            println("Run judging canceled Site " + run.getSiteNumber() + " Run " + run.getNumber() + " from " + run.getTeam().getLoginName() + " at " + run.getSubmissionTime());
        }

        public void runCheckedOut(IRun run, boolean isFinal) {
            println("Run checked out Site " + run.getSiteNumber() + " Run " + run.getNumber() + " from " + run.getTeam().getLoginName() + " at " + run.getSubmissionTime());

        }
    }

    /**
     * Connection Listener for ContestTestFrame.
     * 
     * @author pc2@ecs.csus.edu
     * @version $Id$
     */
    protected class ConnectionEventListener implements IConnectionEventListener {

        public void connectionDropped() {
            println("Connection Dropped, logged in " + serverConnection.isLoggedIn());
        }
    }

    /**
     * Clar Listener for ContestTestFrame.
     * 
     * @author pc2@ecs.csus.edu
     * @version $Id$
     */
    protected class ClarificationListener implements IClarificationEventListener {

        public void clarificationAdded(IClarification clarification) {
            println("Clar added Site " + clarification.getSiteNumber() + " Clarification " + clarification.getNumber() + " from " + clarification.getTeam().getLoginName() + " at "
                    + clarification.getSubmissionTime() + " Problem " + clarification.getProblem().getName());
        }

        public void clarificationRemoved(IClarification clarification) {
            println("Clar removed Site " + clarification.getSiteNumber() + " Clarification " + clarification.getNumber() + " from " + clarification.getTeam().getLoginName() + " at "
                    + clarification.getSubmissionTime() + " Problem " + clarification.getProblem().getName());
        }

        public void clarificationAnswered(IClarification clarification) {
            println("Clar answered Site " + clarification.getSiteNumber() + " Clarification " + clarification.getNumber() + " from " + clarification.getTeam().getLoginName() + " at "
                    + clarification.getSubmissionTime() + " Problem " + clarification.getProblem().getName());
        }

        public void clarificationUpdated(IClarification clarification) {
            println("Clar updated Site " + clarification.getSiteNumber() + " Clarification " + clarification.getNumber() + " from " + clarification.getTeam().getLoginName() + " at "
                    + clarification.getSubmissionTime() + " Problem " + clarification.getProblem().getName());
        }
    }

    /**
     * Listener for IConfigurationUpdateListener.
     * 
     * @author pc2@ecs.csus.edu
     * @version $Id$
     */

    // $HeadURL$
    protected class ConfigUpdateListener implements IConfigurationUpdateListener {

        public void configurationItemAdded(ContestEvent contestEvent) {
            println("Config item added " + contestEvent.getEventType() + " " + getConfigInfo(contestEvent));
        }

        public void configurationItemUpdated(ContestEvent contestEvent) {
            println("Config item updated " + contestEvent.getEventType() + " " + getConfigInfo(contestEvent));

            if (contestEvent.getEventType().equals(ContestEvent.EventType.SITE)) {
                updateSiteComboBox();
            }

        }

        public void configurationItemRemoved(ContestEvent contestEvent) {
            println("Config item removed " + contestEvent.getEventType() + " " + getConfigInfo(contestEvent));
        }
    }

    /**
     * This method initializes mainPain
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getMainPain() {
        if (mainPain == null) {
            mainPain = new JPanel();
            mainPain.setLayout(new BorderLayout());
            mainPain.add(getButtonPane(), java.awt.BorderLayout.SOUTH);
            mainPain.add(getMainPanel(), java.awt.BorderLayout.CENTER);
        }
        return mainPain;
    }

    public String getConfigInfo(ContestEvent contestEvent) {
        switch (contestEvent.getEventType()) {
            case CLIENT:
                return contestEvent.getClient().toString();
            case CONTEST_CLOCK:
                return "running " + contestEvent.getContestClock().isContestClockRunning();
            case CONTEST_TITLE:
                return contestEvent.getContestTitle();
            case GROUP:
                return contestEvent.getGroup().getName();
            case JUDGEMENT:
                return contestEvent.getJudgement().getName();
            case LANGUAGE:
                return contestEvent.getLanguage().getName();
            case PROBLEM:
                return contestEvent.getProblem().getName();
            case SITE:
                return contestEvent.getSite().getName();
            default:
                return "Unknown type " + contestEvent.getEventType();
        }
    }

    /**
     * This method initializes centerPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getCenterPane() {
        if (centerPane == null) {
            clarificationsOnSite = new JLabel();
            clarificationsOnSite.setBounds(new java.awt.Rectangle(164, 239, 157, 16));
            clarificationsOnSite.setText("(clars on site count)");
            clarificationsOnSite.setHorizontalAlignment(SwingConstants.LEFT);
            runsOnSite = new JLabel();
            runsOnSite.setBounds(new java.awt.Rectangle(163, 173, 157, 16));
            runsOnSite.setText("(runs on site count)");
            runsOnSite.setHorizontalAlignment(javax.swing.SwingConstants.LEFT);
            jLabel1 = new JLabel();
            jLabel1.setBounds(new java.awt.Rectangle(48, 144, 96, 16));
            jLabel1.setText("Site");
            jLabel1.setHorizontalAlignment(SwingConstants.RIGHT);
            jLabel = new JLabel();
            jLabel.setBounds(new java.awt.Rectangle(28, 208, 119, 16));
            jLabel.setText("Run/Clar Number");
            jLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            centerPane = new JPanel();
            centerPane.setLayout(null);
            centerPane.setPreferredSize(new java.awt.Dimension(350, 350));
            centerPane.add(getGetRunButton(), null);
            centerPane.add(getRunListenerCheckBox(), null);
            centerPane.add(getConfigListenerCheckBox(), null);
            centerPane.add(getStandingsButton(), null);
            centerPane.add(getPrintAllButton(), null);
            centerPane.add(getOneRunTest(), null);
            centerPane.add(getClarListenerCheckBox(), null);
            centerPane.add(getNumberTextField(), null);
            centerPane.add(jLabel, null);
            centerPane.add(jLabel1, null);
            centerPane.add(getSiteComboBox(), null);
            centerPane.add(runsOnSite, null);
            centerPane.add(getGetClarificationButton(), null);
            centerPane.add(clarificationsOnSite, null);
            centerPane.add(getConnectionListenerCheckBox(), null);
            centerPane.add(getViewRunGrid(), null);
            
            JButton btnStartClock = new JButton();
            btnStartClock.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    stopContest();
                }
            });
            btnStartClock.setToolTipText("Start Contest Clock");
            btnStartClock.setText("Start");
            btnStartClock.setBounds(new Rectangle(246, 61, 94, 29));
            btnStartClock.setBounds(37, 103, 94, 29);
            centerPane.add(btnStartClock);
            
            JButton btnStopClock = new JButton();
            btnStopClock.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {
                    startContest();
                }
            });
            btnStopClock.setToolTipText("Stop Contest Clock");
            btnStopClock.setText("Stop");
            btnStopClock.setBounds(new Rectangle(246, 61, 94, 29));
            btnStopClock.setBounds(143, 102, 94, 29);
            centerPane.add(btnStopClock);
        }
        return centerPane;
    }

    protected void startContest() {
        if (contest == null) {
            showMessage("Not logged in");
            return;
        }
        
        if (contest.isContestClockRunning()){
            showMessage("Note: clock already started");
            println("Note: clock already started");
        }
        
        try {
            serverConnection.startContestClock();
        } catch (Exception e) {
            println(e.getMessage());
            showMessage(e.getMessage());
        }
    }

    protected void stopContest() {
        if (contest == null) {
            showMessage("Not logged in");
            return;
        }
        
        if (! contest.isContestClockRunning()){
            showMessage("Note: clock already stopped");
            println("Note: clock already stopped");
        }
        
        try {
            serverConnection.stopContestClock();
        } catch (Exception e) {
            println(e.getMessage());
            showMessage(e.getMessage());
        }
    }

    /**
     * This method initializes buttonPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getButtonPane() {
        if (buttonPane == null) {
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setHgap(55);
            buttonPane = new JPanel();
            buttonPane.setPreferredSize(new java.awt.Dimension(269, 35));
            buttonPane.setLayout(flowLayout);
            buttonPane.add(getExitButton(), null);
        }
        return buttonPane;
    }

    /**
     * This method initializes exitButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getExitButton() {
        if (exitButton == null) {
            exitButton = new JButton();
            exitButton.setText("Exit");
            exitButton.setToolTipText("Exit this program");
            exitButton.setPreferredSize(new java.awt.Dimension(90, 25));
            exitButton.setMnemonic(java.awt.event.KeyEvent.VK_X);
            exitButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    System.exit(0);
                }
            });
        }
        return exitButton;
    }

    /**
     * This method initializes testRunButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getGetRunButton() {
        if (getRunButton == null) {
            getRunButton = new JButton();
            getRunButton.setBounds(new java.awt.Rectangle(240, 202, 94, 26));
            getRunButton.setToolTipText("Print Run information list");
            getRunButton.setText("getRun");
            getRunButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    runReport(new PrintRun());
                }
            });
        }
        return getRunButton;
    }

    /**
     * This method initializes loginButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getLoginButton() {
        if (loginButton == null) {
            loginButton = new JButton();
            loginButton.setToolTipText("Login to contest");
            loginButton.setBounds(new java.awt.Rectangle(318, 8, 125, 26));
            loginButton.setMnemonic(java.awt.event.KeyEvent.VK_L);
            loginButton.setText("Login");
            loginButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    attachToContest();
                }
            });
        }
        return loginButton;
    }

    protected void attachToContest() {

        FrameUtilities.waitCursor(this);

        String login = getLoginTextField().getText();
        
        login = getShortCut(login);
        
        String password = getPasswordTextField().getText();
        if (password == null || password.length() == 0) {
            password = login;
        }

        try {
            info("Logging in as " + login);
            long startSecs = new Date().getTime();
            contest = serverConnection.login(login, password);
            long totalMs = new Date().getTime() - startSecs;
            info("Logged in at " + login + " took " + totalMs + "ms (" + (totalMs / 1000) + " seconds)");
            setTitle("Contest " + contest.getMyClient().getLoginName() + " " + contest.getSiteName());
            getLoginButton().setEnabled(false);
            setTitle("Info for " + contest.getMyClient().getLoginName() + " " + contest.getSiteName());
            scrollyFrame.setVisible(true);

            VersionInfo versionInfo = new VersionInfo();

            println("Version " + versionInfo.getVersionNumber() + " build " + versionInfo.getBuildNumber());
            runReport(new PrintMyClient());
            println(contest.getRuns().length + " runs.");
            println(contest.getClarifications().length + " clarifications");

            updateSiteComboBox();

        } catch (LoginFailureException e) {
            contest = null;
            showMessage("Unable to login " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            contest = null;
            showMessage("Unable to login " + e.getMessage());
            e.printStackTrace();
        }
        FrameUtilities.regularCursor(this);

    }

    private String getShortCut(String login) {
        ClientId id = InternalController.loginShortcutExpansion(1, login);
        if (id != null){
            return id.getName();
        } else {
            return login;
        }
    }

    private void info(String string) {
        System.out.println(new Date() + " " + Thread.currentThread().getName() + " " + string);
        System.out.flush();
    }

    private void showMessage(String string, String title) {
        JOptionPane.showMessageDialog(null, string, title, JOptionPane.WARNING_MESSAGE);
    }

    private void showMessage(String string) {
        showMessage(string, "Note");
    }

    /**
     * This method initializes loginTextField
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getLoginTextField() {
        if (loginTextField == null) {
            loginTextField = new JTextField();
            loginTextField.setText("b1");
            loginTextField.setBounds(new java.awt.Rectangle(153, 11, 127, 20));
        }
        return loginTextField;
    }

    /**
     * This method initializes passwordTextField
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getPasswordTextField() {
        if (passwordTextField == null) {
            passwordTextField = new JTextField();
            passwordTextField.setBounds(new java.awt.Rectangle(153, 36, 127, 20));
            passwordTextField.addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyPressed(java.awt.event.KeyEvent e) {
                    if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                        attachToContest();
                    }

                }
            });
        }
        return passwordTextField;
    }

    protected void printTeamsTest() {
        runReport(new PrintTeams());

    }

    protected void printProblemsTest() {
        runReport(new PrintProblems());
    }
    
    protected void printClarificationCategoriesTest() {
        runReport(new PrintClarificationCategories());
    }


    /**
     * This method initializes logoffButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getLogoffButton() {
        if (logoffButton == null) {
            logoffButton = new JButton();
            logoffButton.setToolTipText("Logoff of contest");
            logoffButton.setBounds(new java.awt.Rectangle(500, 8, 125, 26));
            logoffButton.setMnemonic(java.awt.event.KeyEvent.VK_O);
            logoffButton.setText("Logoff");
            logoffButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    try {
                        logoff();
                    } catch (NotLoggedInException e1) {
                        JOptionPane.showMessageDialog(null, "unable to logoff " + e1.getMessage());
                        e1.printStackTrace();
                    }
                }
            });
        }
        return logoffButton;
    }

    protected void logoff() throws NotLoggedInException {

        if (contest == null) {
            showMessage("Not logged in");
            return;
        }

        scrollyFrame.setVisible(false);
        if (runsFrame != null){
            runsFrame.setVisible(false);
        }

        serverConnection.logoff();
        contest = null;
        showMessage("No longer logged in");
        this.setTitle(DEFAULT_TITLE);
        getLoginButton().setEnabled(true);
    }

    /**
     * This method initializes runListenerCheckBox
     * 
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getRunListenerCheckBox() {
        if (runListenerCheckBox == null) {
            runListenerCheckBox = new JCheckBox();
            runListenerCheckBox.setBounds(new java.awt.Rectangle(17, 271, 156, 18));
            runListenerCheckBox.setToolTipText("Listen for run events");
            runListenerCheckBox.setText("View Run Listener");
            runListenerCheckBox.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    runListenerChanged(runListenerCheckBox.isSelected());
                }
            });
        }
        return runListenerCheckBox;
    }

    /**
     * Turn run listener on and off
     * 
     * @param listenerON
     *            true add listener, false no listener.
     */
    protected void runListenerChanged(boolean listenerON) {

        if (contest == null) {
            showMessage("Not logged in");
            return;
        }

        if (listenerON) {
            // turn it on
            if (runListener == null) {
                runListener = new RunListener();
            }
            contest.addRunListener(runListener);
            println("Run Listener added");
        } else {
            if (runListener != null) {
                contest.removeRunListener(runListener);
                println("Run Listener removed");
            }
        }
    }

    /**
     * Turn connection event listener on and off
     * 
     * @param listenerON
     *            true add listener, false no listener.
     */
    protected void connectionListenerChanged(boolean listenerON) {

        if (contest == null) {
            showMessage("Not logged in");
            return;
        }

        if (listenerON) {
            // turn it on
            if (connectionEventListener == null) {
                connectionEventListener = new ConnectionEventListener();
            }
            contest.addConnectionListener(connectionEventListener);
            println("Connection Listener added");
        } else {
            if (connectionEventListener != null) {
                contest.removeConnectionListener(connectionEventListener);
                println("Connection Listener removed");
            }
        }
    }

    /**
     * This method initializes configListenerCheckBox
     * 
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getConfigListenerCheckBox() {
        if (configListenerCheckBox == null) {
            configListenerCheckBox = new JCheckBox();
            configListenerCheckBox.setBounds(new java.awt.Rectangle(17, 332, 180, 21));
            configListenerCheckBox.setToolTipText("Listen for all configuration change events");
            configListenerCheckBox.setText("View Config Listener");
            configListenerCheckBox.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    configListenerChanged(configListenerCheckBox.isSelected());
                }
            });
        }
        return configListenerCheckBox;
    }

    protected void configListenerChanged(boolean listenerON) {

        if (contest == null) {
            showMessage("Not logged in");
            return;
        }

        if (listenerON) {
            // turn it on
            if (configurationUpdateListener == null) {
                configurationUpdateListener = new ConfigUpdateListener();
            }
            contest.addContestConfigurationUpdateListener(configurationUpdateListener);
            println("Configuration Listener added");
        } else {
            if (configurationUpdateListener != null) {
                contest.removeContestConfigurationUpdateListener(configurationUpdateListener);
                println("Configuration Listener removed");
            }
        }
    }

    /**
     * This method initializes jButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getStandingsButton() {
        if (standingsButton == null) {
            standingsButton = new JButton();
            standingsButton.setBounds(new java.awt.Rectangle(246, 61, 140, 29));
            standingsButton.setToolTipText("Print Standings");
            standingsButton.setText("Standings");
            standingsButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    printStandingsTest();
                }
            });
        }
        return standingsButton;
    }

    protected void printStandingsTest() {
        runReport(new PrintStandings());
    }

    /**
     * This method initializes jButton1
     * 
     * @return javax.swing.JButton
     */
    private JButton getPrintAllButton() {
        if (printAllButton == null) {
            printAllButton = new JButton();
            printAllButton.setBounds(new java.awt.Rectangle(245, 14, 140, 29));
            printAllButton.setToolTipText("Print all contest info");
            printAllButton.setMnemonic(java.awt.event.KeyEvent.VK_P);
            printAllButton.setText("Print ALL");
            printAllButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    printAll();
                }
            });
        }
        return printAllButton;
    }

    protected void printAll() {
        runReport(new PrintAllReports());
    }

    /**
     * Print all reports.
     * 
     * @author pc2@ecs.csus.edu
     * @version $Id$
     */
    protected class PrintAllReports extends APIAbstractTest {

        @Override
        public void printTest() {

            if (contest == null) {
                showMessage("Not logged in", "Can not display info");
                return;
            }

            Arrays.sort(reportsList, new APIAbstractTestComparator());

            int submissionNumber = getIntegerValue(getNumberTextField().getText());

            for (APIAbstractTest abstractTest : reportsList) {
                println("-- Report " + abstractTest.getTitle());
                abstractTest.setAPISettings(scrollyFrame, contest, serverConnection);
                abstractTest.setNumber(submissionNumber);
                abstractTest.printTest();
            }
            println();
            println();
        }

        @Override
        public String getTitle() {
            return "ALL Reports";
        }
    }

    private int getIntegerValue(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * This method initializes oneRunTest
     * 
     * @return javax.swing.JButton
     */
    private JButton getOneRunTest() {
        if (oneRunTest == null) {
            oneRunTest = new JButton();
            oneRunTest.setBounds(new java.awt.Rectangle(42, 14, 185, 29));
            oneRunTest.setToolTipText("View all Run's Source");
            oneRunTest.setText("View All Runs Source");
            oneRunTest.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    viewOneRunSource();
                }
            });
        }
        return oneRunTest;
    }

    protected void viewOneRunSource() {
        if (contest == null) {
            showMessage("Not logged in");
            return;
        }

        if (contest.getRuns().length == 0) {
            println("No runs in system");
            return;
        }

        for (IRun run : contest.getRuns()) {

            print("Run " + run.getNumber() + " Site " + run.getSiteNumber());

            print(" @ " + run.getSubmissionTime() + " by " + run.getTeam().getLoginName());
            print(" problem: " + run.getProblem().getName());
            print(" in " + run.getLanguage().getName());

            if (run.isFinalJudged()) {
                println("  Final Judgement: " + run.getJudgementName());
            } else if (run.isPreliminaryJudged()) {
                println("  Preliminary Judgement: " + run.getJudgementName());
            } else {
                println("  Judgement: not judged yet ");
            }

            println();
            println("Getting source file name(s)");

            byte[][] contents = run.getSourceCodeFileContents();
            String[] names = run.getSourceCodeFileNames();
            for (int i = 0; i < names.length; i++) {
                String s = names[i];
                println("Name[" + i + "] " + s + " " + contents[i].length);
            }

        }

        println("done");

    }

    protected void printClarTest() {
        runReport(new PrintClarifications());
    }

    /**
     * This method initializes clarListenerCheckBox
     * 
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getClarListenerCheckBox() {
        if (clarListenerCheckBox == null) {
            clarListenerCheckBox = new JCheckBox();
            clarListenerCheckBox.setBounds(new java.awt.Rectangle(17, 300, 156, 21));
            clarListenerCheckBox.setToolTipText("Listen for Clarification events");
            clarListenerCheckBox.setText("View Clar Listener");
            clarListenerCheckBox.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    clarListenerChanged(clarListenerCheckBox.isSelected());
                }
            });
        }
        return clarListenerCheckBox;
    }

    protected void clarListenerChanged(boolean listenerON) {
        if (contest == null) {
            showMessage("Not logged in");
            return;
        }

        if (listenerON) {
            // turn it on
            if (clarificationListener == null) {
                clarificationListener = new ClarificationListener();
            }
            contest.addClarificationListener(clarificationListener);
            println("Clarification Listener added");
        } else {
            if (clarificationListener != null) {
                contest.removeClarificationListener(clarificationListener);
                println("Clarification Listener removed");
            }
        }

    }

    /**
     * This method initializes runContestButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getRunContestButton() {
        if (runContestButton == null) {
            runContestButton = new JButton();
            runContestButton.setText("Run");
            runContestButton.setMnemonic(java.awt.event.KeyEvent.VK_R);
            runContestButton.setToolTipText("Run Selected API method report");
            runContestButton.setPreferredSize(new java.awt.Dimension(100, 25));
            runContestButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    if (contest == null) {
                        showMessage("Not logged in", "Can not display info");
                        return;
                    }
                    int selected = getReportJList().getSelectedIndex();
                    if (selected == -1) {
                        showMessage("No Repport Selected");
                    } else {
                        println();
                        for (Object object : getReportJList().getSelectedValuesList()) {
                            println("-- Report " + ((APIAbstractTest) object).getTitle());
                            runReport(object);
                        }
                    }
                }
            });
        }
        return runContestButton;
    }

    /**
     * This method initializes reportScrollPane
     * 
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getReportScrollPane() {
        if (reportScrollPane == null) {
            reportScrollPane = new JScrollPane();
            reportScrollPane.setViewportView(getReportJList());
        }
        return reportScrollPane;
    }

    /**
     * Run the input report.
     * 
     * @param source APIAbstractTest object
     */
    protected void runReport(Object source) {
        if (contest == null) {
            showMessage("Not logged in", "Can not display info");
            return;
        }

        int submissionNumber = getIntegerValue(getNumberTextField().getText());

        int siteNumber = getSelectedSiteNumber();

        try {
            APIAbstractTest test = (APIAbstractTest) source;
            test.setAPISettings(scrollyFrame, contest, serverConnection);
            test.setNumber(submissionNumber);
            test.setSiteNumber(siteNumber);
            test.printTest();
        } catch (Exception e) {
            println("Exception during report " + e.getLocalizedMessage() + " " + e.getStackTrace()[0].getClassName());
            e.printStackTrace();
        }
    }

    private int getSelectedSiteNumber() {
        Object object = getSiteComboBox().getSelectedItem();
        if (object == null) {
            return 0;
        }
        ISite site = ((ISiteWrapper) object).getSite();
        int siteNumber = site.getNumber();
        return siteNumber;
    }

    /**
     * This method initializes reportJList
     * 
     * @return javax.swing.JList
     */
    private JList<APIAbstractTest> getReportJList() {
        if (reportJList == null) {
            reportJList = new JList<APIAbstractTest>(listModel);
            reportJList.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent e) {
                    if (e.getClickCount() >= 2) {
                        runReport(getReportJList().getSelectedValue());
                    }
                }
            });
        }
        return reportJList;
    }

    /**
     * This method initializes topPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getTopPane() {
        if (topPane == null) {
            passwordLabel = new JLabel();
            passwordLabel.setBounds(new java.awt.Rectangle(45, 38, 100, 16));
            passwordLabel.setText("Password");
            passwordLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            loginLabel = new JLabel();
            loginLabel.setBounds(new java.awt.Rectangle(45, 13, 100, 16));
            loginLabel.setText("Login");
            loginLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            topPane = new JPanel();
            topPane.setLayout(null);
            topPane.setPreferredSize(new java.awt.Dimension(65, 65));
            topPane.add(getPasswordTextField(), null);
            topPane.add(getLoginTextField(), null);
            topPane.add(loginLabel, null);
            topPane.add(passwordLabel, null);
            topPane.add(getLoginButton(), null);
            topPane.add(getLogoffButton(), null);
        }
        return topPane;
    }

    /**
     * This method initializes eastPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getEastPane() {
        if (eastPane == null) {
            BorderLayout borderLayout = new BorderLayout();
            borderLayout.setHgap(0);
            borderLayout.setVgap(3);
            eastPane = new JPanel();
            eastPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "API Methods", javax.swing.border.TitledBorder.CENTER, javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null));
            eastPane.setLayout(borderLayout);
            eastPane.add(getReportScrollPane(), java.awt.BorderLayout.CENTER);
            eastPane.add(getEastButtonPane(), java.awt.BorderLayout.SOUTH);
        }
        return eastPane;
    }

    /**
     * This method initializes mainPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getMainPanel() {
        if (mainPanel == null) {
            mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());
            mainPanel.add(getEastPane(), java.awt.BorderLayout.EAST);
            mainPanel.add(getTopPane(), java.awt.BorderLayout.NORTH);
            mainPanel.add(getCenterPane(), java.awt.BorderLayout.CENTER);
        }
        return mainPanel;
    }

    /**
     * This method initializes numberTextField
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getNumberTextField() {
        if (numberTextField == null) {
            numberTextField = new JTextField();
            numberTextField.setDocument(new IntegerDocument());
            numberTextField.setToolTipText("Enter a Run Number or Clar Number");
            numberTextField.setText("1");
            numberTextField.setBounds(new java.awt.Rectangle(162, 205, 59, 22));
        }
        return numberTextField;
    }

    /**
     * This method initializes eastButtonPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getEastButtonPane() {
        if (eastButtonPane == null) {
            FlowLayout flowLayout1 = new FlowLayout();
            flowLayout1.setHgap(2);
            flowLayout1.setVgap(2);
            eastButtonPane = new JPanel();
            eastButtonPane.setLayout(flowLayout1);
            eastButtonPane.setPreferredSize(new java.awt.Dimension(110, 30));
            eastButtonPane.add(getRunContestButton(), null);
        }
        return eastButtonPane;
    }

    /**
     * This method initializes siteComboBox
     * 
     * @return javax.swing.JComboBox
     */
    private JComboBox<ISiteWrapper> getSiteComboBox() {
        if (siteComboBox == null) {
            siteComboBox = new JComboBox<ISiteWrapper>();
            siteComboBox.setBounds(new java.awt.Rectangle(162, 142, 159, 21));
            siteComboBox.addItemListener(new java.awt.event.ItemListener() {
                public void itemStateChanged(java.awt.event.ItemEvent e) {
                    updateSiteRunTotals();
                    updateSiteClarificationsTotals();
                }
            });
        }
        return siteComboBox;
    }

    /**
     * 
     * @author pc2@ecs.csus.edu
     * @version $Id$
     */

    // $HeadURL$
    protected class ISiteWrapper {
        private ISite site = null;

        public ISite getSite() {
            return site;
        }

        public ISiteWrapper(ISite site) {
            this.site = site;
        }

        @Override
        public String toString() {
            return site.getName();
        }

    }

    protected void updateSiteClarificationsTotals() {

        Object object = getSiteComboBox().getSelectedItem();

        if (object == null) {
            clarificationsOnSite.setText("(no site selected)");
            return;
        }

        ISite site = ((ISiteWrapper) object).getSite();
        int siteNumber = site.getNumber();

        int count = 0;
        int deleted = 0;
        for (IClarification clarification : contest.getClarifications()) {
            if (clarification.getSiteNumber() == siteNumber) {
                count++;
                if (clarification.isDeleted()) {
                    deleted++;
                }
            }
        }

        String text = "";

        if (count == 0) {
            text = "No clarifications.";
        } else if (count == 1) {
            text = "1 clarification.";
        } else {
            text = count + " clarifications.";
        }

        if (deleted > 0) {
            text = count + " deleted.";
        }

        clarificationsOnSite.setText(text);
    }

    protected void updateSiteRunTotals() {

        Object object = getSiteComboBox().getSelectedItem();

        if (object == null) {
            runsOnSite.setText("(no site selected)");
            return;
        }

        ISite site = ((ISiteWrapper) object).getSite();
        int siteNumber = site.getNumber();

        int count = 0;
        int deleted = 0;
        for (IRun run : contest.getRuns()) {
            if (run.getSiteNumber() == siteNumber) {
                count++;
                if (run.isDeleted()) {
                    deleted++;
                }
            }
        }

        String text = "";

        if (count == 0) {
            text = "No runs.";
        } else if (count == 1) {
            text = "1 run.";
        } else {
            text = count + " runs.";
        }

        if (deleted > 0) {
            text = count + " deleted.";
        }

        runsOnSite.setText(text);
    }

    /**
     * This method initializes jButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getGetClarificationButton() {
        if (getClarificationButton == null) {
            getClarificationButton = new JButton();
            getClarificationButton.setBounds(new java.awt.Rectangle(205, 265, 150, 26));
            getClarificationButton.setText("getClarification");
            getClarificationButton.setToolTipText("Print Run information list");
            getClarificationButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    runReport(new PrintClarification());
                }
            });
        }
        return getClarificationButton;
    }

    /**
     * This method initializes jCheckBox
     * 
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getConnectionListenerCheckBox() {
        if (connectionListenerCheckBox == null) {
            connectionListenerCheckBox = new JCheckBox();
            connectionListenerCheckBox.setBounds(new java.awt.Rectangle(17, 366, 220, 24));
            connectionListenerCheckBox.setText("View Connection Listener");
            connectionListenerCheckBox.setToolTipText("Listen for all connection change events");
            connectionListenerCheckBox.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    connectionListenerChanged(connectionListenerCheckBox.isSelected());
                }
            });
        }
        return connectionListenerCheckBox;
    }

    /**
     * This method initializes viewRunGrid
     * 
     * @return javax.swing.JButton
     */
    private JButton getViewRunGrid() {
        if (viewRunGrid == null) {
            viewRunGrid = new JButton();
            viewRunGrid.setBounds(new Rectangle(42, 61, 185, 29));
            viewRunGrid.setText("View Runs Grid");
            viewRunGrid.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    showRunsGridFrame();
                }
            });
        }
        return viewRunGrid;
    }

    protected void showRunsGridFrame() {

        if (contest == null) {
            showMessage("Not logged in", "Can not display runs grid");
            return;
        }

        getRunsFrame().setVisible(true);

    }

    public RunsFrame getRunsFrame() {
        if (runsFrame == null) {
            runsFrame = new RunsFrame(contest);
        }
        return runsFrame;
    }
    
    /**
     * Prints usage to stdout.
     */
    public static void usage() {

        String[] lines = { // 
                "Usage: [--help] ", //
                "", // 
                "Purpose: a API test frame", // 
                "", // 
                "--help    this message", // 
                "", // 
        };

        for (String s : lines) {
            System.out.println(s);
        }

        VersionInfo info = new VersionInfo();
        System.out.println(info.getSystemVersionInfo());

    }

    public static void main(String[] args) {
        
        ParseArguments arguments = new ParseArguments(args);

        if (arguments.isOptPresent("--help")) {
            usage();
            System.exit(0);
        }
        
        VersionInfo info = new VersionInfo();
        System.out.println(info.getSystemVersionInfo());
        
        new ContestTestFrame().setVisible(true);
    }

    private void println() {
        String s = "";
        if (line.length() > 0) {
            s = line + s;
            line = "";
        }
        scrollyFrame.addLine(s);
    }

    private void print(String s) {
        line = line + s;

    }

    private void println(String s) {
        if (line.length() > 0) {
            s = line + s;
            line = "";
        }
        scrollyFrame.addLine(s);
    }

    private void updateSiteComboBox() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                int selectedIndex = getSiteComboBox().getSelectedIndex();

                getSiteComboBox().removeAllItems();

                ISite[] sites = contest.getSites();
                Arrays.sort(sites, new ISiteComparatorBySiteNumber());

                int siteNumber = 0;

                for (int i = 0; i < sites.length; i++) {
                    if (sites[i].getNumber() == siteNumber) {
                        if (selectedIndex == -1) {
                            selectedIndex = i;
                        }
                    }
                    getSiteComboBox().addItem(new ISiteWrapper(sites[i]));
                }

                if (selectedIndex != -1) {
                    getSiteComboBox().setSelectedIndex(selectedIndex);
                }
            }
        });
    }
} // @jve:decl-index=0:visual-constraint="21,23"
