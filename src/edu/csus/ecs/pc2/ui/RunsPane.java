package edu.csus.ecs.pc2.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.ibm.webrunner.j2mclb.util.HeapSorter;
import com.ibm.webrunner.j2mclb.util.NumericStringComparator;

import edu.csus.ecs.pc2.core.IInternalController;
import edu.csus.ecs.pc2.core.Utilities;
import edu.csus.ecs.pc2.core.list.JudgementNotificationsList;
import edu.csus.ecs.pc2.core.log.Log;
import edu.csus.ecs.pc2.core.log.StaticLog;
import edu.csus.ecs.pc2.core.model.Account;
import edu.csus.ecs.pc2.core.model.AccountEvent;
import edu.csus.ecs.pc2.core.model.BalloonSettings;
import edu.csus.ecs.pc2.core.model.BalloonSettingsEvent;
import edu.csus.ecs.pc2.core.model.ClientId;
import edu.csus.ecs.pc2.core.model.ClientSettings;
import edu.csus.ecs.pc2.core.model.ClientType;
import edu.csus.ecs.pc2.core.model.ClientType.Type;
import edu.csus.ecs.pc2.core.model.ContestInformation;
import edu.csus.ecs.pc2.core.model.ContestInformationEvent;
import edu.csus.ecs.pc2.core.model.DisplayTeamName;
import edu.csus.ecs.pc2.core.model.ElementId;
import edu.csus.ecs.pc2.core.model.Filter;
import edu.csus.ecs.pc2.core.model.IAccountListener;
import edu.csus.ecs.pc2.core.model.IBalloonSettingsListener;
import edu.csus.ecs.pc2.core.model.IContestInformationListener;
import edu.csus.ecs.pc2.core.model.IInternalContest;
import edu.csus.ecs.pc2.core.model.ILanguageListener;
import edu.csus.ecs.pc2.core.model.IProblemListener;
import edu.csus.ecs.pc2.core.model.IRunListener;
import edu.csus.ecs.pc2.core.model.Judgement;
import edu.csus.ecs.pc2.core.model.JudgementRecord;
import edu.csus.ecs.pc2.core.model.Language;
import edu.csus.ecs.pc2.core.model.LanguageEvent;
import edu.csus.ecs.pc2.core.model.Problem;
import edu.csus.ecs.pc2.core.model.ProblemEvent;
import edu.csus.ecs.pc2.core.model.Run;
import edu.csus.ecs.pc2.core.model.Run.RunStates;
import edu.csus.ecs.pc2.core.model.RunEvent.Action;
import edu.csus.ecs.pc2.core.model.RunEvent;
import edu.csus.ecs.pc2.core.model.RunFiles;
import edu.csus.ecs.pc2.core.model.RunUtilities;
import edu.csus.ecs.pc2.core.model.SerializedFile;
import edu.csus.ecs.pc2.core.report.ExtractRuns;
import edu.csus.ecs.pc2.core.security.FileSecurityException;
import edu.csus.ecs.pc2.core.security.Permission;
import edu.csus.ecs.pc2.ui.EditFilterPane.ListNames;
import edu.csus.ecs.pc2.ui.judge.JudgeView;

/**
 * View runs panel.
 * 
 * @author pc2@ecs.csus.edu
 */
public class RunsPane extends JPanePlugin {

    /**
     * 
     */
    private static final long serialVersionUID = 114647004580210428L;

    private MCLB runListBox = null;

    private JPanel messagePanel = null;

    private JPanel buttonPanel = null;

    private JButton requestRunButton = null;

    private JButton filterButton = null;

    private JButton editRunButton = null;

    private JButton extractButton = null;

    private JButton giveButton = null;

    private JButton takeButton = null;

    private JButton rejudgeRunButton = null;

    private JButton viewJudgementsButton = null;

    private EditRunFrame editRunFrame = null;

    private ViewJudgementsFrame viewJudgementsFrame = null;

    private SelectJudgementFrame selectJudgementFrame = null;

    private JLabel messageLabel = null;

    private Log log = null;

    private JLabel rowCountLabel = null;

    /**
     * Show huh
     */
    private boolean showNewRunsOnly = false;
    
    /**
     * Filter that does not change.
     * 
     * Used to do things like insure only New runs or Judged runs 
     */
    private Filter requiredFilter = new Filter();

    /**
     * Show who is judging column and status.
     * 
     */
    private boolean showJudgesInfo = true;

    /**
     * User filter
     */
    private Filter filter = new Filter();

    private JButton autoJudgeButton = null;

    private AutoJudgingMonitor autoJudgingMonitor = new AutoJudgingMonitor(true);

    private boolean usingTeamColumns = false;

    private boolean usingFullColumns = false;

    private DisplayTeamName displayTeamName = null;

    private boolean makeSoundOnOneRun = false;

    private boolean bUseAutoJudgemonitor = true;
    
    private EditFilterFrame editFilterFrame = null;

    private String filterFrameTitle = "Run filter";
    
    private ExtractRuns extractRuns = null;

    private boolean teamClient = true;
    
    private JudgementNotificationsList judgementNotificationsList = null;
    
    private boolean displayConfirmation = true;
    private JButton viewSourceButton;

    private boolean serverReplied;

    private Run fetchedRun;

    private RunFiles fetchedRunFiles;

    protected int viewSourceThreadCounter;

    /**
     * This method initializes
     * 
     */
    public RunsPane() {
        super();
        initialize();
    }

    /**
     * @param useAutoJudgeMonitor
     *            if true use
     */
    public RunsPane(boolean useAutoJudgeMonitor) {
        super();
        bUseAutoJudgemonitor = useAutoJudgeMonitor;
        initialize();
    }

    /**
     * This method initializes this
     * 
     */
    private void initialize() {
        this.setLayout(new BorderLayout());
        this.setSize(new java.awt.Dimension(744, 216));
        this.add(getButtonPanel(), java.awt.BorderLayout.SOUTH);
        this.add(getRunsListBox(), java.awt.BorderLayout.CENTER);
        this.add(getMessagePanel(), java.awt.BorderLayout.NORTH);

        editRunFrame = new EditRunFrame();
        viewJudgementsFrame = new ViewJudgementsFrame();
        selectJudgementFrame = new SelectJudgementFrame();

    }

    @Override
    public String getPluginTitle() {
        return "Runs Panel";
    }

    protected Object[] buildRunRow(Run run, ClientId judgeId) {

        try {
            boolean autoJudgedRun = isAutoJudgedRun(run);

            int cols = runListBox.getColumnCount();
            Object[] s = new String[cols];

            int idx = 0;

            if (usingFullColumns) {
//              Object[] fullColumns = { "Site", "Team", "Run Id", "Time", "Status", "Suppressed", "Problem", "Judge", "Balloon", "Language", "OS" };
                s[idx++] = getSiteTitle("" + run.getSubmitter().getSiteNumber());
                s[idx++] = getTeamDisplayName(run);
                s[idx++] = new Long(run.getNumber()).toString();
                s[idx++] = new Long(run.getElapsedMins()).toString();
                s[idx++] = getJudgementResultString(run);
                s[idx++] = isJudgementSuppressed(run);
                s[idx++] = getProblemTitle(run.getProblemId());
                s[idx++] = getJudgesTitle(run, judgeId, showJudgesInfo, autoJudgedRun);
                s[idx++] = getBalloonColor(run);
                s[idx++] = getLanguageTitle(run.getLanguageId());
                s[idx++] = run.getSystemOS();
            } else if (showJudgesInfo) {
//              Object[] fullColumnsNoJudge = { "Site", "Team", "Run Id", "Time", "Status", "Problem", "Balloon", "Language", "OS" };
                s[idx++] = getSiteTitle("" + run.getSubmitter().getSiteNumber());
                s[idx++] = getTeamDisplayName(run);
                s[idx++] = new Long(run.getNumber()).toString();
                s[idx++] = new Long(run.getElapsedMins()).toString();
                s[idx++] = getJudgementResultString(run);
                s[idx++] = getProblemTitle(run.getProblemId());
                s[idx++] = getBalloonColor(run);
                s[idx++] = getLanguageTitle(run.getLanguageId());
                s[idx++] = run.getSystemOS();

            } else if (usingTeamColumns) {
//              Object[] teamColumns = { "Site", "Run Id", "Problem", "Time", "Status", "Balloon", "Language" };
                s[idx++] = getSiteTitle("" + run.getSubmitter().getSiteNumber());
                s[idx++] = new Long(run.getNumber()).toString();
                s[idx++] = getProblemTitle(run.getProblemId());
                s[idx++] = new Long(run.getElapsedMins()).toString();
                s[idx++] = getJudgementResultString(run);
                s[idx++] = getBalloonColor(run);
                s[idx++] = getLanguageTitle(run.getLanguageId());
            } else {
                log.log(Log.INFO, "In RunPanes no mclb columns set");
            }

            return s;
        } catch (Exception exception) {
            StaticLog.getLog().log(Log.INFO, "Exception in buildRunRow()", exception);
        }
        return null;
    }

    /**
     * Return balloon color if problem solved.
     * 
     * @param run
     * @return
     */
    private String getBalloonColor(Run run) {

        if (run.isSolved()) {
            BalloonSettings balloonSettings = getContest().getBalloonSettings(run.getSiteNumber());

            if (balloonSettings != null) {
                String colorName = balloonSettings.getColor(run.getProblemId());
                if (colorName != null) {
                    return colorName;
                }
            }
        }

        return "";
    }

    /**
     * Was the active judgement suppressed on the team?
     * @param run
     * @return "" or "Yes" or "Yes (EOC)"
     */
    private String isJudgementSuppressed(Run run) {
        String result = "";

        // requires run to be judged
        if (run.isJudged()) {
            if (!run.getJudgementRecord().isSendToTeam()) {
                result= "Yes";
            } else {
                if (RunUtilities.supppressJudgement(judgementNotificationsList, run, getContest().getContestTime())) {
                    result = "Yes (EOC)";
                } else {
                    if (run.getJudgementRecord().isPreliminaryJudgement()) {
                        Problem prob = getContest().getProblem(run.getProblemId());
                        if (!prob.isPrelimaryNotification()) {
                            result = "Yes";
                        }
                    }
                }
            }
        }
        return result;
    }

    private String getJudgesTitle(Run run, ClientId judgeId, boolean showJudgesInfo2, boolean autoJudgedRun) {

        String result = "";

        if (showJudgesInfo) {
            if (judgeId != null) {
                if (judgeId.equals(getContest().getClientId())) {
                    result = "Me";
                } else {
                    result = judgeId.getName() + " S" + judgeId.getSiteNumber();
                }
                if (autoJudgedRun) {
                    result = result + "/AJ";
                }
            } else {
                result = "";
            }
        }
        return result;
    }

    private boolean isAutoJudgedRun(Run run) {
        if (run.isJudged()) {
            if (!run.isSolved()) {
                JudgementRecord judgementRecord = run.getJudgementRecord();
                if (judgementRecord != null && judgementRecord.getJudgementId() != null) {
                    return judgementRecord.isUsedValidator();
                }
            }
        }
        return false;
    }

    /**
     * Return the judgement/status for the run.
     * 
     * @param run
     * @return a string that represents the state of the run
     */
    protected String getJudgementResultString(Run run) {

        String result = "";

        if (run.isJudged()) {
            
            if (run.isSolved()) {
                result = getContest().getJudgements()[0].getDisplayName();
                if (run.getStatus().equals(RunStates.MANUAL_REVIEW)) {
                    if (!isTeam(getContest().getClientId())) {
                        result = RunStates.MANUAL_REVIEW + " (" + result + ")";
                    } else {
                        // Only show to team
                        if (showPreliminaryJudgementToTeam(run)) {
                            result = "PRELIMINARY (" + result + ")";
                        } else {
                            result = RunStates.NEW.toString();
                        }
                    }
                }
                
                // only consider changing the state to new if we are a team
                if (isTeam(getContest().getClientId()) && !run.getJudgementRecord().isSendToTeam()) {
                    result = RunStates.NEW.toString();
                }
                
            } else {
                result = "No";

                JudgementRecord judgementRecord = run.getJudgementRecord();
                if (judgementRecord != null && judgementRecord.getJudgementId() != null) {
                    if (judgementRecord.isUsedValidator() && judgementRecord.getValidatorResultString() != null) {
                        result = judgementRecord.getValidatorResultString();
                    } else {
                        Judgement judgement = getContest().getJudgement(judgementRecord.getJudgementId());
                        if (judgement != null) {
                            result = judgement.toString();
                        }
                    }

                    if (run.getStatus().equals(RunStates.MANUAL_REVIEW)) {
                        if (!isTeam(getContest().getClientId())) {
                            result = RunStates.MANUAL_REVIEW + " (" + result + ")";
                        } else {
                            
                            // Only show to team
                            if (showPreliminaryJudgementToTeam(run)) {
                                result = "PRELIMINARY (" + result + ")";
                            } else {
                                result = RunStates.NEW.toString();
                            }
                        }
                    }
                    
                    if (isTeam(getContest().getClientId())) {
                        if (!judgementRecord.isSendToTeam()) {
                            result = RunStates.NEW.toString();
                        }
                    } else {
                        if (run.getStatus().equals(RunStates.BEING_RE_JUDGED)) {
                            result = RunStates.BEING_RE_JUDGED.toString();
                        }
                    }
                }
            }
            
            /**
             * 
             */
            
            if (teamClient || isAllowed(Permission.Type.RESPECT_EOC_SUPPRESSION)){
                if (RunUtilities.supppressJudgement(judgementNotificationsList, run, getContest().getContestTime())){
                    result = RunStates.NEW.toString();
                }
            }

        } else {
            if (showJudgesInfo) {
                result = run.getStatus().toString();
            } else {
                result = RunStates.NEW.toString();
            }
        }
        
        if (run.isDeleted()) {
            result = "DEL " + result;
        }
        
        
        return result;
    }

    private boolean showPreliminaryJudgementToTeam(Run run) {
        
        try {
            Problem problem = getContest().getProblem(run.getProblemId());
            return problem.isPrelimaryNotification();
        } catch (Exception e) {
            log.log(Log.WARNING, "Exception trying to get Problem  ", e);
            return false; 
        }
    }

    private boolean isTeam(ClientId clientId) {
        return clientId == null || clientId.getClientType().equals(Type.TEAM);
    }
    
//    private boolean isServer(ClientId clientId) {
//        return clientId == null || clientId.getClientType().equals(Type.SERVER);
//    }


    private boolean isJudge(ClientId clientId) {
        return clientId == null || clientId.getClientType().equals(Type.JUDGE);
    }

    private boolean isJudge() {
        return isJudge(getContest().getClientId());
    }

    private String getLanguageTitle(ElementId languageId) {
        for (Language language : getContest().getLanguages()) {
            if (language.getElementId().equals(languageId)) {
                return language.toString();
            }
        }
        return "Language ?";
    }

    private String getProblemTitle(ElementId problemId) {
        Problem problem = getContest().getProblem(problemId);
        if (problem != null) {
            return problem.toString();
        }
        return "Problem ?";
    }

    private String getSiteTitle(String string) {
        return "Site " + string;
    }

    private String getTeamDisplayName(Run run) {

        if (isJudge() && isTeam(run.getSubmitter())) {

            return displayTeamName.getDisplayName(run.getSubmitter());
        }
        return run.getSubmitter().getName();
    }

    /**
     * Run Listener
     * @author pc2@ecs.csus.edu
     * @version $Id$
     */

    // $HeadURL$
    public class RunListenerImplementation implements IRunListener {

        public void runAdded(RunEvent event) {
            updateRunRow(event.getRun(), event.getWhoModifiedRun(), true);
            // check if this is a team; if so, pop up a confirmation dialog
            if (getContest().getClientId().getClientType() == ClientType.Type.TEAM) {
                showResponseToTeam(event);
            }
        }
        
        public void refreshRuns(RunEvent event) {
            reloadRunList();
        }

        public void runChanged(RunEvent event) {
            updateRunRow(event.getRun(), event.getWhoModifiedRun(), true);

            // check if this is a team; if so, pop up a response dialog
            if (getContest().getClientId().getClientType() == ClientType.Type.TEAM) {
                showResponseToTeam(event);
            }
            
            //code copied from FetchRunService.RunListenerImplementation.runChanged():
                
                Action action = event.getAction();
                Action details = event.getDetailedAction();
                Run aRun = event.getRun();
                RunFiles aRunFiles = event.getRunFiles();
                String msg = event.getMessage();
                
                getController().getLog().log(Log.INFO, "RunsPane.RunListener: Action=" + action + "; DetailedAction=" + details + "; msg=" + msg
                                        + "; run=" + aRun + "; runFiles=" + aRunFiles);

                
                if (event.getRun() != null) {
                        
                        // RUN_NOT_AVAILABLE is undirected (sentToClient is null)
                        if (event.getAction().equals(Action.RUN_NOT_AVAILABLE)) {
                            
                            getController().getLog().log(Log.WARNING, "Reply from server: requested run not available");
                            
                        } else {
                            
                            //make sure this RunEvent was meant for me
                            if (event.getSentToClientId() != null && event.getSentToClientId().equals(getContest().getClientId())) {
                                
                                getController().getLog().log(Log.INFO, "Reply from server: " + "Run Status=" + event.getAction()
                                                        + "; run=" + event.getRun() + ";  runFiles=" + event.getRunFiles());
                                
                                fetchedRun = event.getRun();
                                fetchedRunFiles = event.getRunFiles();    
                                
                            } else {
                                
                                ClientId toClient = event.getSentToClientId() ;
                                ClientId myID = getContest().getClientId();

                                getController().getLog().log(Log.INFO, "Event not for me: sent to " + toClient + " but my ID is " + myID);

                                //TODO:  this needs to be reconsidered; why are we continuing when the event wasn't sent to us?  (Why wasn't it sent to us?)
                                fetchedRun = event.getRun();
                                fetchedRunFiles = event.getRunFiles();
                            }
                        }
                        
                } else {
                    //run from server was null
                    getController().getLog().log(Log.WARNING, "Run received from server was null");
                    fetchedRun = null;
                    fetchedRunFiles = null;
                }
                
                
                serverReplied = true;     
                
        }

        public void runRemoved(RunEvent event) {
            updateRunRow(event.getRun(), event.getWhoModifiedRun(), true);
        }

        /**
         * Show the Run Judgement.
         * 
         * Checks the run in the specified run event and (potentially) displays a results dialog. If the run has been judged, has a valid judgement record, and is the "active" judgement for scoring
         * purposes, displays a modal MessageDialog to the Team containing the judgement results. This method assumes the caller has already verified this is a TEAM client; failure to do that on the
         * caller's part will cause other clients to see the Run Response dialog...
         */
        private void showResponseToTeam(RunEvent event) {
            
            if (! displayConfirmation){
                return;
            }

            Run theRun = event.getRun();
            String problemName = getContest().getProblem(theRun.getProblemId()).toString();
            String languageName = getContest().getLanguage(theRun.getLanguageId()).toString();
            int runId = theRun.getNumber();
            // build an HTML tag that sets the font size and color for the answer
            String responseFormat = "";

            // check if the run has been judged
            if (theRun.isJudged() && (! theRun.isDeleted())) {

                // check if there's a legit judgement
                JudgementRecord judgementRecord = theRun.getJudgementRecord();
                if (judgementRecord != null) {
                    
                    if (! judgementRecord.isSendToTeam()){
                        
                        /**
                         * Do not show this judgement to the team, the judge indicated
                         * that the team should not be notified.
                         */
                        return; // ------------------------------------------------------ RETURN
                    }

                    // check if this is the scoreable judgement
                    boolean isActive = judgementRecord.isActive();
                    if (isActive) {

                        String response = getContest().getJudgement(judgementRecord.getJudgementId()).toString();

                        // it's a valid judging response (presumably to a team);
                        // get the info from the run and display it in a modal popup
                        if (judgementRecord.isSolved()) {
                            responseFormat += "<FONT COLOR=\"00FF00\" SIZE=+2>"; // green, larger
                        } else {
                            responseFormat += "<FONT COLOR=RED>"; // red, current size

                            String validatorJudgementName = judgementRecord.getValidatorResultString();
                            if (judgementRecord.isUsedValidator() && validatorJudgementName != null) {
                                if (validatorJudgementName.trim().length() == 0) {
                                    validatorJudgementName = "undetermined";
                                }
                                response = validatorJudgementName;
                            }
                        }

                        String judgeComment = theRun.getCommentsForTeam();
                        try {
                            String displayString = "<HTML><FONT SIZE=+1>Judge's Response<BR><BR>" + "Problem: <FONT COLOR=BLUE>" + Utilities.forHTML(problemName) + "</FONT><BR><BR>"
                                    + "Language: <FONT COLOR=BLUE>" + Utilities.forHTML(languageName) + "</FONT><BR><BR>" + "Run Id: <FONT COLOR=BLUE>" + runId + "</FONT><BR><BR><BR>"
                                    + "Judge's Response: " + responseFormat + Utilities.forHTML(response) + "</FONT><BR><BR><BR>";

                            if (theRun.getStatus().equals(RunStates.MANUAL_REVIEW)) {
                                displayString += "<FONT SIZE='+2'>NOTE: This is a <FONT COLOR='RED'>Preliminary</FONT> Judgement</FONT><BR><BR><BR>";
                            }
                            
                            if (judgeComment != null) {
                                if (judgeComment.length() > 0) {
                                    displayString += "Judge's Comment: " + Utilities.forHTML(judgeComment) + "<BR><BR><BR>";
                                }
                            }

                            displayString += "</FONT></HTML>";

                            FrameUtilities.showMessage(getParentFrame(), "Run Judgement Received", displayString);

                        } catch (Exception e) {
                            // TODO need to make this cleaner
                            JOptionPane.showMessageDialog(getParentFrame(), "Exception handling Run Response: " + e.getMessage());
                            log.warning("Exception handling Run Response: " + e.getMessage());
                        }
                    }
                }
            } else if (! theRun.isDeleted()) {
                // not judged
                try {
                    String displayString = "<HTML><FONT SIZE=+1>Confirmation of Run Receipt<BR><BR>" + "Problem: <FONT COLOR=BLUE>" + Utilities.forHTML(problemName) + "</FONT><BR><BR>"
                            + "Language: <FONT COLOR=BLUE>" + Utilities.forHTML(languageName) + "</FONT><BR><BR>" + "Run Id: <FONT COLOR=BLUE>" + runId + "</FONT><BR><BR><BR>";

                    displayString += "</FONT></HTML>";

                    FrameUtilities.showMessage(getParentFrame(), "Run Received", displayString);

                } catch (Exception e) {
                    JOptionPane.showMessageDialog(getParentFrame(), "Exception handling Run Confirmation: " + e.getMessage());
                    log.warning("Exception handling Run Confirmation: " + e.getMessage());
                }
            }
        }
    }

    /**
     * This method initializes runListBox
     * 
     * @return edu.csus.ecs.pc2.core.log.MCLB
     */
    private MCLB getRunsListBox() {
        if (runListBox == null) {
            runListBox = new MCLB();

            runListBox.addListboxListener(new com.ibm.webrunner.j2mclb.event.ListboxListener() {
                public void rowSelected(com.ibm.webrunner.j2mclb.event.ListboxEvent e) {
                    if (isAllowed(Permission.Type.JUDGE_RUN) && e.getClickCount() >= 2) {
                        requestSelectedRun();
                    }
                }

                public void rowDeselected(com.ibm.webrunner.j2mclb.event.ListboxEvent e) {
                }
            });
        }
        return runListBox;
    }

    public void clearAllRuns() {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                runListBox.removeAllRows();
            }
        });
    }

    private void resetRunsListBoxColumns() {

        runListBox.removeAllRows();
        runListBox.removeAllColumns();

        Object[] fullColumns = { "Site", "Team", "Run Id", "Time", "Status", "Suppressed", "Problem", "Judge", "Balloon", "Language", "OS" };
        Object[] fullColumnsNoJudge = { "Site", "Team", "Run Id", "Time", "Status", "Problem", "Balloon", "Language", "OS" };
        Object[] teamColumns = { "Site", "Run Id", "Problem", "Time", "Status", "Balloon", "Language" };

        usingTeamColumns = false;
        usingFullColumns = false;

        if (isTeam(getContest().getClientId())) {
            usingTeamColumns = true;
            runListBox.addColumns(teamColumns);
        } else if (!showJudgesInfo) {
            runListBox.addColumns(fullColumnsNoJudge);
        } else {
            usingFullColumns = true;
            runListBox.addColumns(fullColumns);
        }

        // Sorters
        HeapSorter sorter = new HeapSorter();
        HeapSorter numericStringSorter = new HeapSorter();
        numericStringSorter.setComparator(new NumericStringComparator());
        HeapSorter accountNameSorter = new HeapSorter();
        accountNameSorter.setComparator(new AccountColumnComparator());

        int idx = 0;

        if (isTeam(getContest().getClientId())) {

//            Object[] teamColumns = { "Site", "Run Id", "Problem", "Time", "Status", "Balloon", "Language" };

            runListBox.setColumnSorter(idx++, sorter, 3); // Site
            runListBox.setColumnSorter(idx++, numericStringSorter, 2); // Run Id
            runListBox.setColumnSorter(idx++, sorter, 4); // Problem
            runListBox.setColumnSorter(idx++, numericStringSorter, 1); // Time
            runListBox.setColumnSorter(idx++, sorter, 5); // Status
            runListBox.setColumnSorter(idx++, sorter, 6); // Balloon Color
            runListBox.setColumnSorter(idx++, sorter, 7); // Language

        } else if (showJudgesInfo) {

//            Object[] fullColumns = { "Site", "Team", "Run Id", "Time", "Status", "Suppressed", "Problem", "Judge", "Balloon", "Language", "OS" };

            runListBox.setColumnSorter(idx++, sorter, 3); // Site
            runListBox.setColumnSorter(idx++, accountNameSorter, 2); // Team
            runListBox.setColumnSorter(idx++, numericStringSorter, 1); // Run Id
            runListBox.setColumnSorter(idx++, numericStringSorter, 4); // Time
            runListBox.setColumnSorter(idx++, sorter, 5); // Status
            runListBox.setColumnSorter(idx++, sorter, 6); // Problem
            runListBox.setColumnSorter(idx++, accountNameSorter, 7); // Judge
            runListBox.setColumnSorter(idx++, sorter, 8); // Balloon Color
            runListBox.setColumnSorter(idx++, sorter, 9); // Language
            runListBox.setColumnSorter(idx++, sorter, 10); // OS

        } else {

//            Object[] fullColumnsNoJudge = { "Site", "Team", "Run Id", "Time", "Status", "Problem", "Balloon", "Language", "OS" };
            
            runListBox.setColumnSorter(idx++, sorter, 2); // Site
            runListBox.setColumnSorter(idx++, sorter, 3); // Team
            runListBox.setColumnSorter(idx++, numericStringSorter, 1); // Run Id
            runListBox.setColumnSorter(idx++, numericStringSorter, 4); // Time
            runListBox.setColumnSorter(idx++, sorter, 5); // Status
            runListBox.setColumnSorter(idx++, sorter, 6); // Problem
            runListBox.setColumnSorter(idx++, sorter, 7); // Balloon Color
            runListBox.setColumnSorter(idx++, sorter, 8); // Language
            runListBox.setColumnSorter(idx++, sorter, 9); // OS
        }

        runListBox.autoSizeAllColumns();
    }

    /**
     * Remove run from grid.
     * 
     * @param run
     */
    private void removeRunRow(final Run run) {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                int rowNumber = runListBox.getIndexByKey(run.getElementId());
                if (rowNumber != -1) {
                    runListBox.removeRow(rowNumber);
                    updateRowCount();
                }
            }
        });
    }

    /**
     * This updates the rowCountlabel & toolTipText. It should be called only while on the swing thread.
     */
    private void updateRowCount() {
        if (filter.isFilterOn()){
            int totalRuns = getContest().getRuns().length;
            rowCountLabel.setText(runListBox.getRowCount()+" of "+totalRuns);
            rowCountLabel.setToolTipText(runListBox.getRowCount() + " filtered runs");
        } else {
            rowCountLabel.setText("" + runListBox.getRowCount());
            rowCountLabel.setToolTipText(runListBox.getRowCount() + " runs");
        }
    }

    public void updateRunRow(final Run run, final ClientId whoModifiedId, final boolean autoSizeAndSort) {

        if (filter != null) {
            if (!filter.matches(run)) {
                // if run does not match filter, be sure to remove it from grid
                // This applies when a run is New then BEING_JUDGED and other conditions.
                removeRunRow(run);
                return;
            }
        }
        
        if (requiredFilter != null) {
            if (!requiredFilter.matches(run)) {
                // if run does not match requiredFilter, be sure to remove it from grid
                // This applies when a run is New then BEING_JUDGED and other conditions.
                removeRunRow(run);
                return;
            }
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {

                ClientId whoJudgedId = whoModifiedId;
                if (run.isJudged()) {
                    JudgementRecord judgementRecord = run.getJudgementRecord();
                    if (judgementRecord != null) {
                        whoJudgedId = judgementRecord.getJudgerClientId();
                    }
                }

                Object[] objects = buildRunRow(run, whoJudgedId);
                int rowNumber = runListBox.getIndexByKey(run.getElementId());
                if (rowNumber == -1) {
                    runListBox.addRow(objects, run.getElementId());
                } else {
                    runListBox.replaceRow(objects, rowNumber);
                }

                if (isAllowed(Permission.Type.JUDGE_RUN)) {
                    if (runListBox.getRowCount() == 1) {
                        emitSound();
                    }
                }

                if (autoSizeAndSort) {
                    updateRowCount();
                    runListBox.autoSizeAllColumns();
                    runListBox.sort();
                }
                
//                if (selectJudgementFrame != null) {
                        //TODO the selectJudgementFrame should be placed above all PC2 windows, not working when dblClicking in Windows OS
//                }
            }
        });
    }

    public void reloadRunList() {

        Run[] runs = getContest().getRuns();
        
        ContestInformation contestInformation = getContest().getContestInformation();
        judgementNotificationsList = contestInformation.getJudgementNotificationsList();

        if (isJudge()) {
            displayTeamName.setTeamDisplayMask(contestInformation.getTeamDisplayMode());
        }

        // TODO bulk load these records, this is closer only do the count,size,sort at end
        
        if (filter.isFilterOn()){
            getFilterButton().setForeground(Color.BLUE);
            getFilterButton().setToolTipText("Edit filter - filter ON");
            rowCountLabel.setForeground(Color.BLUE);
        } else {
            getFilterButton().setForeground(Color.BLACK);
            getFilterButton().setToolTipText("Edit filter");
            rowCountLabel.setForeground(Color.BLACK);
        }

        for (Run run : runs) {

            if (requiredFilter != null) {
                if (!requiredFilter.matches(run)) {
                    removeRunRow(run);
                    continue;
                }
            }

            if (filter != null) {
                if (!filter.matches(run)) {
                    removeRunRow(run);
                    continue;
                }
            }

            ClientId clientId = null;

            RunStates runStates = run.getStatus();
            if (!(runStates.equals(RunStates.NEW) || run.isDeleted())) {
                JudgementRecord judgementRecord = run.getJudgementRecord();
                if (judgementRecord != null) {
                    clientId = judgementRecord.getJudgerClientId();
                }
            }
            updateRunRow(run, clientId, false);
        }
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                updateRowCount();
                runListBox.autoSizeAllColumns();
                runListBox.sort();

            }
        });
    }

    private void emitSound() {
        if (isMakeSoundOnOneRun()) {
            java.awt.Toolkit.getDefaultToolkit().beep();
        }
    }

    private void updateGUIperPermissions() {

        if (showNewRunsOnly) {

            // Show New Runs

            getViewSourceButton().setVisible(false);
            requestRunButton.setVisible(true);
            requestRunButton.setEnabled(isAllowed(Permission.Type.JUDGE_RUN));
            editRunButton.setVisible(false);
            extractButton.setVisible(false);
            giveButton.setVisible(false);
            takeButton.setVisible(false);
            rejudgeRunButton.setVisible(false);
            viewJudgementsButton.setVisible(false);
            autoJudgeButton.setVisible(false);
        } else {

            // Show ALL Runs

            getViewSourceButton().setVisible(isAllowed(Permission.Type.ALLOWED_TO_FETCH_RUN));
            requestRunButton.setVisible(isAllowed(Permission.Type.JUDGE_RUN));
            editRunButton.setVisible(isAllowed(Permission.Type.EDIT_RUN));
            extractButton.setVisible(isAllowed(Permission.Type.EXTRACT_RUNS));
            giveButton.setVisible(isAllowed(Permission.Type.GIVE_RUN));

            takeButton.setVisible(false);
            // takeButton.setVisible(isAllowed(Permission.Type.TAKE_RUN));
            rejudgeRunButton.setVisible(isAllowed(Permission.Type.REJUDGE_RUN));
            viewJudgementsButton.setVisible(isAllowed(Permission.Type.VIEW_RUN_JUDGEMENT_HISTORIES));
            autoJudgeButton.setVisible(isAllowed(Permission.Type.ALLOWED_TO_AUTO_JUDGE));

        }

        filterButton.setVisible(true);
    }

    public void setContestAndController(IInternalContest inContest, IInternalController inController) {
        super.setContestAndController(inContest, inController);

        if (bUseAutoJudgemonitor) {
            autoJudgingMonitor.setContestAndController(getContest(), getController());
        }

        log = getController().getLog();

        displayTeamName = new DisplayTeamName();
        displayTeamName.setContestAndController(inContest, inController);
        
        teamClient = isTeam(inContest.getClientId());
        
        ContestInformation contestInformation = getContest().getContestInformation();
        judgementNotificationsList = contestInformation.getJudgementNotificationsList();

        initializePermissions();
        
        extractRuns = new ExtractRuns(inContest);

        getContest().addRunListener(new RunListenerImplementation());
        getContest().addAccountListener(new AccountListenerImplementation());
        getContest().addProblemListener(new ProblemListenerImplementation());
        getContest().addLanguageListener(new LanguageListenerImplementation());
        getContest().addContestInformationListener(new ContestInformationListenerImplementation());
        getContest().addBalloonSettingsListener(new BalloonSettingsListenerImplementation());
     
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                
                editRunFrame.setContestAndController(getContest(), getController());
                viewJudgementsFrame.setContestAndController(getContest(), getController());
                if (isAllowed(Permission.Type.JUDGE_RUN)) {
                    selectJudgementFrame.setContestAndController(getContest(), getController());
                }

                
                getEditFilterFrame().setContestAndController(getContest(), getController());
                
                updateGUIperPermissions();
                resetRunsListBoxColumns();
                reloadRunList();
                
                if (isAllowed(Permission.Type.EXTRACT_RUNS)){
                    getRunsListBox().setMultipleSelections(true);
                }
                // this will do the checking and take us off the AWT thread as needed
                // check this to prevent the admin has turned off aj message
            }
        });
    }

    /**
     * This method initializes messagePanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getMessagePanel() {
        if (messagePanel == null) {
            rowCountLabel = new JLabel();
            rowCountLabel.setText("### of ###");
            rowCountLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
            rowCountLabel.setPreferredSize(new java.awt.Dimension(100,16));
            messageLabel = new JLabel();
            messageLabel.setText("");
            messageLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 14));
            messageLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            messagePanel = new JPanel();
            messagePanel.setLayout(new BorderLayout());
            messagePanel.setPreferredSize(new java.awt.Dimension(30, 30));
            messagePanel.add(messageLabel, java.awt.BorderLayout.CENTER);
            messagePanel.add(rowCountLabel, java.awt.BorderLayout.EAST);
        }
        return messagePanel;
    }

    /**
     * This method initializes buttonPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getButtonPanel() {
        if (buttonPanel == null) {
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setHgap(5);
            buttonPanel = new JPanel();
            buttonPanel.setLayout(flowLayout);
            buttonPanel.setPreferredSize(new java.awt.Dimension(35, 35));
            buttonPanel.add(getGiveButton(), null);
            buttonPanel.add(getTakeButton(), null);
            buttonPanel.add(getEditRunButton(), null);
            buttonPanel.add(getViewSourceButton());
            buttonPanel.add(getViewJudgementsButton(), null);
            buttonPanel.add(getRequestRunButton(), null);
            buttonPanel.add(getRejudgeRunButton(), null);
            buttonPanel.add(getFilterButton(), null);
            buttonPanel.add(getAutoJudgeButton(), null);
            buttonPanel.add(getExtractButton(), null);
        }
        return buttonPanel;
    }

    /**
     * This method initializes requestRunButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getRequestRunButton() {
        if (requestRunButton == null) {
            requestRunButton = new JButton();
            requestRunButton.setText("Request Run");
            requestRunButton.setToolTipText("Request the selected Run for Judging");
            requestRunButton.setMnemonic(java.awt.event.KeyEvent.VK_R);
            requestRunButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    requestSelectedRun();
                }
            });
        }
        return requestRunButton;
    }

    protected void requestSelectedRun() {
        if (!isAllowed(Permission.Type.JUDGE_RUN)) {
            log.log(Log.WARNING, "Account does not have permission to JUDGE_RUN, cannot requestSelectedRun.");
            showMessage("Unable to request run, check log");
            return;
        }
        int[] selectedIndexes = runListBox.getSelectedIndexes();

        if (selectedIndexes.length < 1) {
            showMessage("Please select a run ");
            return;
        }

        /*
         *  we may be competing with the AJ, ensure we have the lock before
         *  checking & setting the alreadyJudgingRun boolean.
         */
        Boolean alreadyJudgingRun = JudgeView.getAlreadyJudgingRun();
        synchronized (alreadyJudgingRun) {
            if (JudgeView.isAlreadyJudgingRun()) {
                JOptionPane.showMessageDialog(this, "Already judging run");
                return;
            }

            JudgeView.setAlreadyJudgingRun(true);
        }

        try {
            ElementId elementId = (ElementId) runListBox.getKeys()[selectedIndexes[0]];
            Run runToEdit = getContest().getRun(elementId);

            if ((!(runToEdit.getStatus().equals(RunStates.NEW) || runToEdit.getStatus().equals(RunStates.MANUAL_REVIEW)))
                    || runToEdit.isDeleted()) {
                showMessage("Not allowed to request run (run not status NEW) ");
                JudgeView.setAlreadyJudgingRun(false);
                return;
            }

            selectJudgementFrame.setRun(runToEdit, false);
            selectJudgementFrame.setVisible(true);
        } catch (Exception e) {
            log.log(Log.WARNING, "Exception logged ", e);
            showMessage("Unable to request run, check log");
        }
    }

    protected void rejudgeSelectedRun() {

        int[] selectedIndexes = runListBox.getSelectedIndexes();

        if (selectedIndexes.length < 1) {
            showMessage("Please select a run ");
            return;
        }

        try {
            ElementId elementId = (ElementId) runListBox.getKeys()[selectedIndexes[0]];
            Run runToEdit = getContest().getRun(elementId);

            if (!runToEdit.isJudged()) {
                showMessage("Judge run before attempting to re-judge run");
                return;
            }

            if (runToEdit.isDeleted()) {
                showMessage("Not allowed to rejudge deleted run ");
                return;
            }

            /*
             *  we may be competing with the AJ, ensure we have the lock before
             *  checking & setting the alreadyJudgingRun boolean.
             */
            Boolean alreadyJudgingRun = JudgeView.getAlreadyJudgingRun();
            synchronized (alreadyJudgingRun) {
                if (JudgeView.isAlreadyJudgingRun()) {
                    JOptionPane.showMessageDialog(this, "Already judging run");
                    return;
                }

                JudgeView.setAlreadyJudgingRun(true);
            }

            selectJudgementFrame.setRun(runToEdit, true);
            selectJudgementFrame.setVisible(true);

        } catch (Exception e) {
            log.log(Log.WARNING, "Exception logged ", e);
            showMessage("Unable to rejudge run, check log");
        }
    }

    /**
     * This method initializes filterButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getFilterButton() {
        if (filterButton == null) {
            filterButton = new JButton();
            filterButton.setText("Filter");
            filterButton.setToolTipText("Edit Filter");
            filterButton.setMnemonic(java.awt.event.KeyEvent.VK_F);
            filterButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    showFilterRunsFrame();
                }
            });
        }
        return filterButton;
    }

    protected void showFilterRunsFrame() {
        getEditFilterFrame().addList(ListNames.PROBLEMS);
        getEditFilterFrame().addList(ListNames.JUDGEMENTS);
        getEditFilterFrame().addList(ListNames.LANGUAGES);
              
        if (! usingTeamColumns) {
            getEditFilterFrame().addList(ListNames.TEAM_ACCOUNTS);
            getEditFilterFrame().addList(ListNames.RUN_STATES);
        }

        getEditFilterFrame().addList(ListNames.SITES);
        getEditFilterFrame().setFilter(filter);
        getEditFilterFrame().validate();
        getEditFilterFrame().setVisible(true);
    }

    /**
     * This method initializes editRunButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getEditRunButton() {
        if (editRunButton == null) {
            editRunButton = new JButton();
            editRunButton.setText("Edit");
            editRunButton.setToolTipText("Edit the selected Run");
            editRunButton.setMnemonic(java.awt.event.KeyEvent.VK_E);
            editRunButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    editSelectedRun();
                }
            });
        }
        return editRunButton;
    }

    protected void editSelectedRun() {

        int[] selectedIndexes = runListBox.getSelectedIndexes();

        if (selectedIndexes.length < 1) {
            showMessage("Please select a run ");
            return;
        }

        try {
            ElementId elementId = (ElementId) runListBox.getKeys()[selectedIndexes[0]];
            Run runToEdit = getContest().getRun(elementId);

            editRunFrame.setRun(runToEdit);
            editRunFrame.setVisible(true);
        } catch (Exception e) {
            log.log(Log.WARNING, "Exception logged ", e);
            showMessage("Unable to edit run, check log");
        }

    }

    /**
     * This method initializes extractButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getExtractButton() {
        if (extractButton == null) {
            extractButton = new JButton();
            extractButton.setText("Extract");
            extractButton.setMnemonic(java.awt.event.KeyEvent.VK_X);
            extractButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    extractRuns(getRunsListBox());
                    
                }
            });
        }
        return extractButton;
    }

    protected void extractRuns(MCLB runsListBox) {

        if (runListBox.getRowCount() < 1) {
            showMessageToUser("No runs to extract");
            return;
        }

        int[] selectedRows = runListBox.getSelectedIndexes();

        try {
            if (selectedRows.length < 1) {
                // Extract all runs

                int numRuns = runsListBox.getRowCount();
                String confirmMessage = "Extract ALL (" + numRuns + ") runs listed?";
                int response = FrameUtilities.yesNoCancelDialog(this, confirmMessage, "Extract Runs");

                if (response == JOptionPane.YES_OPTION) {
                    // They said Yes, go for it.
                    FrameUtilities.waitCursor(this);
                    int numberRuns = runsListBox.getRowCount();
                    int numExtracted = extractSelectedRuns(runsListBox, getRunKeys(runsListBox));
                    FrameUtilities.regularCursor(this);
                    showMessageToUser("Extracted " + numExtracted + " of " + numberRuns + " runs to \"" + extractRuns.getExtractDirectory() + "\" dir.");
                }

            } else {

                String confirmMsg = "Extract " + selectedRows.length + " runs?";
                int response = JOptionPane.showConfirmDialog(this, confirmMsg);

                if (response == JOptionPane.YES_OPTION) {
                    // They said Yes, go for it.
                    FrameUtilities.waitCursor(this);
                    int numExtracted = extractSelectedRuns(runsListBox, getRunKeys(runsListBox, selectedRows));
                    FrameUtilities.regularCursor(this);
                    showMessageToUser("Extracted " + numExtracted + " of " + selectedRows.length + " runs to \"" + extractRuns.getExtractDirectory() + "\" dir.");
                }
            }
        } catch (Exception e) {
            log.log(Log.WARNING, "Exception logged ", e);
        } finally{
            FrameUtilities.regularCursor(this);
        }
    }

    private ElementId[] getRunKeys(MCLB runsListBox) {
        Vector<ElementId> vector = new Vector<ElementId>();
        int totalRows = runsListBox.getRowCount();
        for (int rowNumber = 0; rowNumber < totalRows; rowNumber++) {
            vector.addElement((ElementId) runsListBox.getRowKey(rowNumber));
        }
        return (ElementId[]) vector.toArray(new ElementId[vector.size()]);
    }

    private ElementId[] getRunKeys(MCLB runsListBox, int[] selectedRows) {
        Vector<ElementId> vector = new Vector<ElementId>();
        for (int rowNumber : selectedRows) {
            vector.addElement((ElementId) runsListBox.getRowKey(rowNumber));
        }
        return (ElementId[]) vector.toArray(new ElementId[vector.size()]);
    }


    private int extractSelectedRuns(MCLB runsListBox, ElementId[] runKeys) {
        int extractCount = 0;
        
        int totalRows = runKeys.length;
        
        for (int i = 0; i < runKeys.length; i++) {
            try {
                boolean extracted = extractRuns.extractRun(runKeys[i]);
                
                if (extracted){
                    extractCount ++;
                }
                
                updateRunCount (extractCount, totalRows);
                
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (FileSecurityException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return extractCount;
    }

    private void updateRunCount(int extractCount, int totalRows) {
        System.out.println("Extracted "+extractCount+" of "+totalRows);
    }

    private void showMessageToUser(String message) {
        JOptionPane.showMessageDialog(this, message);
    }

    /**
     * This method initializes giveButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getGiveButton() {
        if (giveButton == null) {
            giveButton = new JButton();
            giveButton.setText("Give");
            giveButton.setToolTipText("Give the selected Run back to Judges");
            giveButton.setMnemonic(java.awt.event.KeyEvent.VK_G);
            giveButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    giveSelectedRun();
                }
            });
        }
        return giveButton;
    }

    protected void giveSelectedRun() {

        int[] selectedIndexes = runListBox.getSelectedIndexes();

        if (selectedIndexes.length < 1) {
            showMessage("Please select a run ");
            return;
        }

        try {
            ElementId elementId = (ElementId) runListBox.getKeys()[selectedIndexes[0]];
            Run runToEdit = getContest().getRun(elementId);

            if (runToEdit.getStatus().equals(RunStates.BEING_JUDGED) || runToEdit.getStatus().equals(RunStates.NEW) || runToEdit.getStatus().equals(RunStates.BEING_RE_JUDGED)) {
                getController().cancelRun(runToEdit);
                showMessage("Gave run " + runToEdit);

            } else {
                showMessage("Can not give run with state: " + runToEdit.getStatus());
            }

        } catch (Exception e) {
            log.log(Log.WARNING, "Exception logged ", e);
            showMessage("Unable to give run, check log");
        }

    }

    /**
     * This method initializes takeButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getTakeButton() {
        if (takeButton == null) {
            takeButton = new JButton();
            takeButton.setText("Take");
            takeButton.setToolTipText("Take the selected Run from the Judges");
            takeButton.setMnemonic(java.awt.event.KeyEvent.VK_T);
            takeButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    System.out.println("TODO Take actionPerformed()");
                    // TODO code Take Run
                }
            });
        }
        return takeButton;
    }

    /**
     * This method initializes rejudgeRunButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getRejudgeRunButton() {
        if (rejudgeRunButton == null) {
            rejudgeRunButton = new JButton();
            rejudgeRunButton.setText("Rejudge");
            rejudgeRunButton.setToolTipText("Rejudge the selected Run");
            rejudgeRunButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    rejudgeSelectedRun();
                }
            });
        }
        return rejudgeRunButton;
    }

    /**
     * This method initializes viewJudgementsButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getViewJudgementsButton() {
        if (viewJudgementsButton == null) {
            viewJudgementsButton = new JButton();
            viewJudgementsButton.setText("View Judgements");
            viewJudgementsButton.setToolTipText("View Judgements for the selected Run");
            viewJudgementsButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    viewSelectedRunJudgements();
                }
            });
        }
        return viewJudgementsButton;
    }

    protected void viewSelectedRunJudgements() {

        int[] selectedIndexes = runListBox.getSelectedIndexes();

        if (selectedIndexes.length < 1) {
            showMessage("Please select a run ");
            return;
        }

        try {
            ElementId elementId = (ElementId) runListBox.getKeys()[selectedIndexes[0]];
            Run theRun = getContest().getRun(elementId);

            if (theRun != null) {
                viewJudgementsFrame.setRun(theRun);
                viewJudgementsFrame.setVisible(true);
            } else {
                showMessage("Cannot display judgements for Run");
            }

        } catch (Exception e) {
            log.log(Log.WARNING, "Exception logged ", e);
            showMessage("Unable to view run, check log");
        }

    }
    
    public boolean isDisplayConfirmation() {
        return displayConfirmation;
    }
    
    public void setDisplayConfirmation(boolean displayConfirmation) {
        this.displayConfirmation = displayConfirmation;
    }

    /**
     * Account Listener.
     * 
     * @author pc2@ecs.csus.edu
     * @version $Id$
     */
    public class AccountListenerImplementation implements IAccountListener {

        public void accountAdded(AccountEvent accountEvent) {
            // ignore doesn't affect this pane
        }

        public void accountModified(AccountEvent event) {
            // check if is this account
            Account account = event.getAccount();
            /**
             * If this is the account then update the GUI display per the potential change in Permissions.
             */
            if (getContest().getClientId().equals(account.getClientId())) {
                // They modified us!!
                initializePermissions();
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        updateGUIperPermissions();
                        reloadRunList();
                    }
                });

            } else {
                // not us, but update the grid anyways
                SwingUtilities.invokeLater(new Runnable() {
                    public void run() {
                        reloadRunList();
                    }
                });

            }

        }

        public void accountsAdded(AccountEvent accountEvent) {
            // ignore, this does not affect this class

        }

        public void accountsModified(AccountEvent accountEvent) {
            // check if it included this account
            boolean theyModifiedUs = false;
            for (Account account : accountEvent.getAccounts()) {
                /**
                 * If this is the account then update the GUI display per the potential change in Permissions.
                 */
                if (getContest().getClientId().equals(account.getClientId())) {
                    theyModifiedUs = true;
                    initializePermissions();
                }
            }
            final boolean finalTheyModifiedUs = theyModifiedUs;
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (finalTheyModifiedUs) {
                        updateGUIperPermissions();
                    }
                    reloadRunList();
                }
            });
        }

        public void accountsRefreshAll(AccountEvent accountEvent) {
            accountsModified(accountEvent);
        }
    }

    /**
     * Problem Listener.
     * 
     * @author pc2@ecs.csus.edu
     * @version $Id$
     */
    public class ProblemListenerImplementation implements IProblemListener {

        public void problemAdded(ProblemEvent event) {
            // ignore does not affect this pane
        }

        public void problemChanged(ProblemEvent event) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    reloadRunList();
                }
            });
        }

        public void problemRemoved(ProblemEvent event) {
            // ignore does not affect this pane
        }

        public void problemRefreshAll(ProblemEvent event) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    reloadRunList();
                }
            }); 
        }
    }

    /**
     * Language Listener.
     * 
     * @author pc2@ecs.csus.edu
     * @version $Id$
     */
    public class LanguageListenerImplementation implements ILanguageListener {

        public void languageAdded(LanguageEvent event) {
            // ignore does not affect this pane
        }

        public void languageChanged(LanguageEvent event) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    reloadRunList();
                }
            });
        }

        public void languageRemoved(LanguageEvent event) {
            // ignore does not affect this pane
        }

        public void languageRefreshAll(LanguageEvent event) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    reloadRunList();
                }
            });
        }

        @Override
        public void languagesAdded(LanguageEvent event) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    reloadRunList();
                }
            });
        }

        @Override
        public void languagesChanged(LanguageEvent event) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    reloadRunList();
                }
            });
        }
    }

    /**
     * Contest Information Listener.
     * 
     * @author pc2@ecs.csus.edu
     * @version $Id$
     */

    public class ContestInformationListenerImplementation implements IContestInformationListener {

        public void contestInformationAdded(ContestInformationEvent event) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    reloadRunList();
                }
            });
        }

        public void contestInformationChanged(ContestInformationEvent event) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    reloadRunList();
                }
            });
        }

        public void contestInformationRemoved(ContestInformationEvent event) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    reloadRunList();
                }
            });
        }

        public void contestInformationRefreshAll(ContestInformationEvent contestInformationEvent) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    reloadRunList();
                }
            });
        }
        public void finalizeDataChanged(ContestInformationEvent contestInformationEvent) {
            // Not used
        }


    }

    private void showMessage(final String string) {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                messageLabel.setText(string);
                messageLabel.setToolTipText(string);
            }
        });

    }

    public boolean isShowNewRunsOnly() {
        return showNewRunsOnly;
    }

    /**
     * Shows only runs that are RunStates.NEW and RunStates.MANUAL_REVIEW.
     * 
     * @param showNewRunsOnly
     */
    public void setShowNewRunsOnly(boolean showNewRunsOnly) {
        this.showNewRunsOnly = showNewRunsOnly;
    
        if (showNewRunsOnly) {
            if (requiredFilter == null) {
                requiredFilter = new Filter();
            }
            requiredFilter.addRunState(RunStates.NEW);
            requiredFilter.addRunState(RunStates.MANUAL_REVIEW);
        } else {
            requiredFilter = new Filter();
        }
    }

    public boolean isShowJudgesInfo() {
        return showJudgesInfo;
    }

    public void setShowJudgesInfo(boolean showJudgesInfo) {
        this.showJudgesInfo = showJudgesInfo;
    }

    /**
     * This method initializes autoJudgeButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getAutoJudgeButton() {

        if (autoJudgeButton == null) {
            autoJudgeButton = new JButton();
            autoJudgeButton.setText("Auto Judge");
            autoJudgeButton.setToolTipText("Enable Auto Judging");
            autoJudgeButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    // Turn auto judging on
                    if (!bUseAutoJudgemonitor) {
                        log.info("Can not show Auto Judge Monitor, not enabled");
                        return;
                    }
                    log.info("Starting Auto Judge monitor");
                    autoJudgingMonitor.setAutoJudgeDisabledLocally(false);
                    startAutoJudging();
                }
            });
        }
        return autoJudgeButton;
    }

    /**
     * Is Auto Judging turned On for this judge ?
     * 
     * @return
     */
    private boolean isAutoJudgingEnabled() {
        ClientSettings clientSettings = getContest().getClientSettings();
        if (clientSettings != null && clientSettings.isAutoJudging()) {
            return true;
        }

        return false;
    }

    public void startAutoJudging() {
        if (!bUseAutoJudgemonitor) {
            return;
        }
        if (isAutoJudgingEnabled()) {
            showMessage("");
            // Keep this off the AWT thread.
            new Thread(new Runnable() {
                public void run() {
//                  RE-enable local auto judge flag
                    autoJudgingMonitor.startAutoJudging();
                }
            }).start();
        } else {
            showMessage("Administrator has turned off Auto Judging");
        }
    }

    public boolean isMakeSoundOnOneRun() {
        return makeSoundOnOneRun;
    }

    public void setMakeSoundOnOneRun(boolean makeSoundOnOneRun) {
        this.makeSoundOnOneRun = makeSoundOnOneRun;
    }

    public EditFilterFrame getEditFilterFrame() {
        if (editFilterFrame == null){
            Runnable callback = new Runnable(){
                public void run() {
                    reloadRunList();
                }
            };
            editFilterFrame = new EditFilterFrame(filter, filterFrameTitle,  callback);
            if (displayTeamName != null){
                editFilterFrame.setDisplayTeamName(displayTeamName);
            }
        }
        return editFilterFrame;
    }
    
    /**
     * Set title for the Filter Frame.
     * 
     * @param title
     */
    public void setFilterFrameTitle (String title){
        filterFrameTitle = title;
        if (editFilterFrame != null){
            editFilterFrame.setTitle(title);
        }
    }
    
    /**
     * @author pc2@ecs.csus.edu
     *
     */
    public class BalloonSettingsListenerImplementation implements IBalloonSettingsListener {

        public void balloonSettingsAdded(BalloonSettingsEvent event) {
            reloadRunList();
        }

        public void balloonSettingsChanged(BalloonSettingsEvent event) {
            reloadRunList();
        }

        public void balloonSettingsRemoved(BalloonSettingsEvent event) {
            reloadRunList();
        }

        public void balloonSettingsRefreshAll(BalloonSettingsEvent balloonSettingsEvent) {
            reloadRunList();
        }
    }

    private JButton getViewSourceButton() {
        if (viewSourceButton == null) {
            viewSourceButton = new JButton("View Source");
            viewSourceButton.addActionListener(new ActionListener() {
                public void actionPerformed(ActionEvent e) {

//                    SwingUtilities.invokeLater(new Runnable() {
//                        public void run() {
//                            showSourceForSelectedRun();
//                        }
//                    });

                    Thread viewSourceThread = new Thread() {
                        public void run() {
                            showSourceForSelectedRun();
                        }
                    };
                    viewSourceThread.setName("ViewSourceThread-" + viewSourceThreadCounter++);
                    viewSourceThread.start();
                }
            });
            viewSourceButton.setToolTipText("Displays a read-only view of the source code for the currently selected run");
        }
        return viewSourceButton;
    }
    
    /**
     * Displays a {@link MultipleFileViewer} containing the source code for the run (submission) which is currently selected in the Runs grid.
     * 
     * If no run is selected, or more than one run is selected, prompts the user to select just one run (row) in the grid
     * and does nothing else.
     */
    private void showSourceForSelectedRun() {

        // make sure we're allowed to fetch a run
        if (!isAllowed(Permission.Type.ALLOWED_TO_FETCH_RUN)) {
            getController().getLog().log(Log.WARNING, "Account does not have the permission ALLOWED_TO_FETCH_RUN; cannot view run source.");
            showMessage("Unable to fetch run, check log");
            return;
        }

        // make sure there's exactly one run selected in the grid
        int[] selectedIndexes = runListBox.getSelectedIndexes();

        if (selectedIndexes.length < 1) {
            showMessage("Please select a run ");
            return;
        } else if (selectedIndexes.length > 1) {
            showMessage("Please select exactly ONE run in order to view source ");
            return;
        }

        // we are allowed to view source and there's exactly one run selected; try to obtain the run source and display it in a MFV 
        try {

            ElementId elementId = (ElementId) runListBox.getKeys()[selectedIndexes[0]];
            Run run = getContest().getRun(elementId);

            // make sure we found the currently selected run
            if (run != null) {

                showMessage("Preparing to display source code for run " + run.getNumber() + " at site " + run.getSiteNumber());
                getController().getLog().log(Log.INFO, "Preparing to display source code for run " + run.getNumber() + " at site " + run.getSiteNumber());

                //the following forces a (read-only) checkout from the server; it makes more sense to first see if we already have the 
                // necessary RunFiles and then if not to issue a "Fetch" request rather than a "checkout" request
//                getController().checkOutRun(run, true, false); // checkoutRun(run, isReadOnlyRequest, isComputerJudgedRequest)

                //check if we already have the RunFiles for the run
                if (!getContest().isRunFilesPresent(run)) {
                    
                    //we don't have the files; request them from the server
                    getController().fetchRun(run);

                    // wait for the server to reply (i.e., to make a callback to the run listener) -- but only for up to 30 sec
                    int waitedMS = 0;
                    serverReplied = false;
                    while (!serverReplied && waitedMS < 30000) {
                        Thread.sleep(100);
                        waitedMS += 100;
                    }
                
                    //check if we got a reply from the server
                    if (serverReplied) {
                        
                        //the server replied; see if we got some RunFiles
                        if (fetchedRunFiles!=null) {
                            
                            //we got some RunFiles from the server; put them into the contest model
                            getContest().updateRunFiles(run, fetchedRunFiles);
                            
                        } else {
                            
                            //we got a reply from the server but we didn't get any RunFiles
                            getController().getLog().log(Log.WARNING, "Server failed to return RunFiles in response to fetch run request");
                            getController().getLog().log(Log.WARNING, "Unable to fetch source files for run " + run.getNumber() + " from server");
                            showMessage("Unable to fetch selected run; check log");
                            return;
                        }
                        
                    } else {
                        
                        // the server failed to reply to the fetchRun request within the time limit
                        getController().getLog().log(Log.WARNING, "No response from server to fetch run request after " + waitedMS + "ms");
                        getController().getLog().log(Log.WARNING, "Unable to fetch run " + run.getNumber() + " from server");
                        showMessage("Unable to fetch selected run; check log");
                        return;
                    }
                }
                
                //if we get here we know there should be RunFiles in the contest model -- but let's sanity-check that
                if (!getContest().isRunFilesPresent(run)) {
                    
                    //something bad happened -- we SHOULD have RunFiles at this point!
                    getController().getLog().log(Log.SEVERE, "Unable to find RunFiles for run " + run.getNumber() + " -- server error?");
                    showMessage("Unable to fetch selected run; check log");
                    return;

                } else {
                    
                    //get the RunFiles
                    RunFiles runFiles = getContest().getRunFiles(run);

                    if (runFiles != null) {

                        // get the (serialized) source files out of the RunFiles
                        SerializedFile mainFile = runFiles.getMainFile();
                        SerializedFile[] otherFiles = runFiles.getOtherFiles();

                        // create a MultiFileViewer in which to display the runFiles
                        MultipleFileViewer mfv = new MultipleFileViewer(log, "Source files for Site " + fetchedRun.getSiteNumber() + " Run " + fetchedRun.getNumber());
                        mfv.setContestAndController(getContest(), getController());

                        // add any other files to the MFV (these are added first so that the mainFile will appear at index 0)
                        boolean otherFilesPresent = false;
                        boolean otherFilesLoadedOK = false;
                        if (otherFiles != null) {
                            otherFilesPresent = true;
                            otherFilesLoadedOK = true;
                            for (SerializedFile otherFile : otherFiles) {
                                otherFilesLoadedOK &= mfv.addFilePane(otherFile.getName(), otherFile);
                            }
                        }

                        // add the mainFile to the MFV
                        boolean mainFilePresent = false;
                        boolean mainFileLoadedOK = false;
                        if (mainFile != null) {
                            mainFilePresent = true;
                            mainFileLoadedOK = mfv.addFilePane("Main File" + " (" + mainFile.getName() + ")", mainFile);
                        }

                        // if we successfully added all files, show the MFV
                        if ((!mainFilePresent || (mainFilePresent && mainFileLoadedOK)) 
                                && (!otherFilesPresent || (otherFilesPresent && otherFilesLoadedOK))) {
                            mfv.setSelectedIndex(0);  //always make leftmost selected; normally this will be MainFile
                            mfv.setVisible(true);
                            showMessage("");
                        } else {
                            getController().getLog().log(Log.WARNING, "Unable to load run source files into MultiFileViewer");
                            showMessage("Unable to load run source files into MultiFileViewer");
                        }

                    } else {
                        // runfiles is null
                        getController().getLog().log(Log.WARNING, "Unable to obtain RunFiles for Site " + run.getSiteNumber() + " run " + run.getNumber());
                        showMessage("Unable to obtain RunFiles for selected run");
                    }
                    
                }
                
            } else {
                // getContest().getRun() returned null
                getController().getLog().log(Log.WARNING, "Selected run not found");
                showMessage("Selected run not found");
            }

        } catch (Exception e) {
            getController().getLog().log(Log.WARNING, "Exception logged ", e);
            showMessage("Unable to show run source, check log");
        }

    }

}
