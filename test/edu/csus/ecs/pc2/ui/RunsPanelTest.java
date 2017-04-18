package edu.csus.ecs.pc2.ui;

import edu.csus.ecs.pc2.core.InternalController;
import edu.csus.ecs.pc2.core.model.Account;
import edu.csus.ecs.pc2.core.model.ClientId;
import edu.csus.ecs.pc2.core.model.ClientType.Type;
import edu.csus.ecs.pc2.core.model.ElementId;
import edu.csus.ecs.pc2.core.model.IInternalContest;
import edu.csus.ecs.pc2.core.model.JudgementRecord;
import edu.csus.ecs.pc2.core.model.Problem;
import edu.csus.ecs.pc2.core.model.Run;
import edu.csus.ecs.pc2.core.model.Run.RunStates;
import edu.csus.ecs.pc2.core.model.SampleContest;
import edu.csus.ecs.pc2.core.util.AbstractTestCase;

/**
 * Test Runs Pane.
 * 
 * Primarily test {@link RunsPane#getJudgementResultString(Run)}
 * 
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */

// $HeadURL$
public class RunsPanelTest extends AbstractTestCase {

    /**
     * Add a run judgement and change RunStatus.
     * 
     * @param theRun
     * @param judgement
     * @param manualReview
     */
    public void updateRunJudgement(Run theRun, JudgementRecord judgement, boolean manualReview) {

        theRun.getElementId().incrementVersionNumber();

        if (theRun.getStatus().equals(RunStates.BEING_JUDGED)) {

            if ((manualReview) && (judgement.isComputerJudgement())) {
                theRun.setStatus(RunStates.MANUAL_REVIEW);
            } else {
                theRun.setStatus(RunStates.JUDGED);
            }
        } else {
            theRun.setStatus(RunStates.JUDGED);
        }

        theRun.addJudgement(judgement);

    }

    /*
     * Test method for 'edu.csus.ecs.pc2.ui.RunsPanel.getJudgementResultString(Run)'
     */
    public void testGetJudgementResultStringJudge() {
        
        if (! isGui()){
            return;
        }

        SampleContest sampleContest = new SampleContest();

        IInternalContest contest = sampleContest.createContest(2, 2, 3, 3, false);
        InternalController internalController = new InternalController(contest);

        Account account = contest.getAccounts(Type.JUDGE).firstElement();
        ClientId judgeClient = account.getClientId();
        contest.setClientId(judgeClient);

        Run[] runs = sampleContest.createRandomRuns(contest, 4, true, true, false);

        // Judge run 1

        Run theRun = runs[0];
        int noJudgementIndex = 1;

        RunsPane runsPanel = new RunsPane();
        runsPanel.setContestAndController(contest, internalController);

        String judgementString = runsPanel.getJudgementResultString(theRun);
        assertEquals(judgementString, "" + RunStates.NEW);

        theRun.setStatus(RunStates.BEING_JUDGED);
        judgementString = runsPanel.getJudgementResultString(theRun);
        assertEquals(judgementString, "" + RunStates.BEING_JUDGED);

        setRunToYes(contest, theRun, judgeClient, false, false);
        judgementString = runsPanel.getJudgementResultString(theRun);
        // XXX TODO FIXME runsPanel returned "Yes" but the 1st judgement is "Yes." so this fails
        assertEquals(judgementString, contest.getJudgements()[0].getDisplayName());

        // Judge run 2

        theRun = runs[1];
        noJudgementIndex = 1;
        setRunToNo(contest, theRun, judgeClient, noJudgementIndex, false, false);
        judgementString = runsPanel.getJudgementResultString(theRun);
        assertEquals(judgementString, contest.getJudgements()[noJudgementIndex].getDisplayName());

        theRun.setStatus(RunStates.BEING_JUDGED);
        judgementString = runsPanel.getJudgementResultString(theRun);
        assertEquals(judgementString, "" + RunStates.BEING_JUDGED);

        setRunToNo(contest, theRun, judgeClient, noJudgementIndex, true, false);
        judgementString = runsPanel.getJudgementResultString(theRun);
        assertEquals(judgementString, contest.getJudgements()[noJudgementIndex].getDisplayName());

        theRun.setStatus(RunStates.BEING_JUDGED);
        noJudgementIndex = 2;
        setRunToNo(contest, theRun, judgeClient, noJudgementIndex, true, false);
        judgementString = runsPanel.getJudgementResultString(theRun);
        assertEquals(judgementString, contest.getJudgements()[noJudgementIndex].getDisplayName());

        theRun.setStatus(RunStates.BEING_JUDGED);
        setRunToYes(contest, theRun, judgeClient, true, true);
        judgementString = runsPanel.getJudgementResultString(theRun);
        assertEquals(judgementString, "MANUAL_REVIEW (" + contest.getJudgements()[0].getDisplayName() + ")");

        theRun.setStatus(RunStates.BEING_JUDGED);
        setRunToYes(contest, theRun, judgeClient, false, false);
        judgementString = runsPanel.getJudgementResultString(theRun);
        assertEquals(judgementString, contest.getJudgements()[0].getDisplayName());

        // Test Unjudged result string

        theRun = runs[2];
        judgementString = runsPanel.getJudgementResultString(theRun);
        assertEquals(judgementString, "" + RunStates.NEW);

        theRun.setStatus(RunStates.BEING_JUDGED);
        judgementString = runsPanel.getJudgementResultString(theRun);
        assertEquals(judgementString, "" + RunStates.BEING_JUDGED);

        theRun.setStatus(RunStates.BEING_COMPUTER_JUDGED);
        judgementString = runsPanel.getJudgementResultString(theRun);
        assertEquals(judgementString, "" + RunStates.BEING_COMPUTER_JUDGED);

    }

    /*
     * Test method for 'edu.csus.ecs.pc2.ui.RunsPanel.getJudgementResultString(Run)'
     */
    public void testGetJudgementResultStringTeam() {
        
        if (! isGui()){
            return;
        }
        
        SampleContest sampleContest = new SampleContest();

        IInternalContest contest = sampleContest.createContest(2, 2, 3, 3, false);
        InternalController internalController = new InternalController(contest);

        Account account = contest.getAccounts(Type.JUDGE).firstElement();
        ClientId judgeClient = account.getClientId();

        account = contest.getAccounts(Type.TEAM).firstElement();
        ClientId teamClient = account.getClientId();
        contest.setClientId(teamClient);

        Run[] runs = sampleContest.createRandomRuns(contest, 4, true, true, false);
        Run theRun = runs[0];
        int noJudgementIndex = 1;

        RunsPane runsPanel = new RunsPane();
        runsPanel.setShowJudgesInfo(false);
        runsPanel.setContestAndController(contest, internalController);

        String judgementString = runsPanel.getJudgementResultString(theRun);
        assertEquals(judgementString, "" + RunStates.NEW);

        theRun.setStatus(RunStates.BEING_JUDGED);
        judgementString = runsPanel.getJudgementResultString(theRun);
        // assertEquals(judgementString, ""+RunStates.BEING_JUDGED);
        assertEquals(judgementString, "" + RunStates.NEW);

        setRunToYes(contest, theRun, judgeClient, false, false);
        judgementString = runsPanel.getJudgementResultString(theRun);
        assertEquals(judgementString, contest.getJudgements()[0].getDisplayName());

        // Judge run 2

        theRun = runs[1];
        noJudgementIndex = 1;
        setRunToNo(contest, theRun, judgeClient, noJudgementIndex, false, false);
        judgementString = runsPanel.getJudgementResultString(theRun);
        assertEquals(judgementString, contest.getJudgements()[noJudgementIndex].getDisplayName());

        theRun.setStatus(RunStates.BEING_JUDGED);
        judgementString = runsPanel.getJudgementResultString(theRun);
        // assertEquals(judgementString, ""+RunStates.BEING_JUDGED);
        assertEquals(judgementString, "" + RunStates.NEW);

        setRunToNo(contest, theRun, judgeClient, noJudgementIndex, true, false);
        judgementString = runsPanel.getJudgementResultString(theRun);
        assertEquals(judgementString, contest.getJudgements()[noJudgementIndex].getDisplayName());

        theRun.setStatus(RunStates.BEING_JUDGED);
        noJudgementIndex = 2;
        setRunToNo(contest, theRun, judgeClient, noJudgementIndex, true, false);
        judgementString = runsPanel.getJudgementResultString(theRun);
        assertEquals(judgementString, contest.getJudgements()[noJudgementIndex].getDisplayName());

        // change to the judge client, otherwise this run will show as NEW
        
        // the problem defaults to not send preliminaryNotifications
        ElementId problemId = theRun.getProblemId();
        Problem theProblem = contest.getProblem(problemId);
        theProblem.setPrelimaryNotification(true);
        contest.updateProblem(theProblem);
        theRun.setStatus(RunStates.BEING_JUDGED);
        // setRunToYes true true changes this to MANUAL_REVIEW (Yes)
        setRunToYes(contest, theRun, judgeClient, true, true);
        judgementString = runsPanel.getJudgementResultString(theRun);
        assertEquals(judgementString, "PRELIMINARY (" + contest.getJudgements()[0].getDisplayName() + ")");

        theRun.setStatus(RunStates.BEING_JUDGED);
        setRunToYes(contest, theRun, judgeClient, false, false);
        judgementString = runsPanel.getJudgementResultString(theRun);
        assertEquals(judgementString, contest.getJudgements()[0].getDisplayName());

        // Test Unjudged result string

        /**
         * Unlike the Judge, a team should only get a NEW judgement text if the run is not judged
         */

        theRun = runs[2];
        judgementString = runsPanel.getJudgementResultString(theRun);
        assertEquals(judgementString, "" + RunStates.NEW);

        theRun.setStatus(RunStates.BEING_JUDGED);
        judgementString = runsPanel.getJudgementResultString(theRun);
        // assertEquals(judgementString, "" + RunStates.BEING_JUDGED);
        assertEquals(judgementString, "" + RunStates.NEW);

        theRun.setStatus(RunStates.BEING_COMPUTER_JUDGED);
        judgementString = runsPanel.getJudgementResultString(theRun);
        // assertEquals(judgementString, "" + RunStates.BEING_COMPUTER_JUDGED);
        assertEquals(judgementString, "" + RunStates.NEW);
    }

    /**
     * Set Run (judgement) to Yes.
     * 
     * @param contest
     * @param theRun
     * @param judgeClient
     * @param computerJudged
     * @param manualJudged
     */
    private void setRunToYes(IInternalContest contest, Run theRun, ClientId judgeClient, boolean computerJudged, boolean manualJudged) {
        ElementId yesJudgementId = contest.getJudgements()[0].getElementId();
        JudgementRecord judgementRecord = new JudgementRecord(yesJudgementId, judgeClient, true, false, computerJudged);
        updateRunJudgement(theRun, judgementRecord, manualJudged);
    }

    /**
     * Set Run (judgement) to NO.
     * 
     * @param contest
     * @param theRun
     * @param judgeClient
     * @param judgementIndex
     * @param computerJudged
     * @param manualJudged
     */
    private void setRunToNo(IInternalContest contest, Run theRun, ClientId judgeClient, int judgementIndex, boolean computerJudged, boolean manualJudged) {
        ElementId judgementId = contest.getJudgements()[judgementIndex].getElementId();
        JudgementRecord judgementRecord = new JudgementRecord(judgementId, judgeClient, false, false, computerJudged);
        updateRunJudgement(theRun, judgementRecord, manualJudged);
    }

}
