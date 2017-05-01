package edu.csus.ecs.pc2.core.model;

import edu.csus.ecs.pc2.core.Constants;
import edu.csus.ecs.pc2.core.util.AbstractTestCase;

/**
 * Unit Tests.
 * 
 * @author pc2@ecs.csus.edu
 */
public class ContestTimeTest extends AbstractTestCase {

    private SampleContest sampleContest = new SampleContest();

    public ContestTimeTest(String string) {
        super(string);
    }

    /**
     * Test whether contest clock start/stop sets correct elapsed time, Bug 707.
     * 
     * Using InternalContest directly.
     * 
     * @throws Exception
     */
    public void testElapsedStartStopInContest() throws Exception {
        
        if (isFastJUnitTesting()){
            return;
        }

        String testName = this.getName();
        /**
         * Number of second to wait before starting and stopping contest clock.
         */
        int secondsToWait = 10;
        int siteNumber = 2;

        long ms = secondsToWait * Constants.MS_PER_SECONDS;

        IInternalContest contest = sampleContest.createContest(siteNumber, siteNumber, 12, 12, true);

        contest.startContest(siteNumber);

        debugPrint(testName + ": sleep for " + secondsToWait + " seconds.");
        Thread.sleep(ms);

        contest.stopContest(siteNumber);
        long actualSecs = contest.getContestTime().getElapsedSecs();
        assertTrue("After stop, expecting elapsed time secs > " + secondsToWait + ", was=" + secondsToWait, actualSecs >= secondsToWait);

        long actualMS = contest.getContestTime().getElapsedMS();
        assertTrue("After stop, expecting elapsed time ms > " + ms + ", was=" + actualMS, actualMS >= ms);

        contest.startContest(siteNumber);
        actualSecs = contest.getContestTime().getElapsedSecs();
        assertTrue("After stop, expecting elapsed time secs > " + secondsToWait + ", was=" + secondsToWait, actualSecs >= secondsToWait);

        actualMS = contest.getContestTime().getElapsedMS();
        assertTrue("After start, expecting elapsed time ms > " + ms + ", was=" + actualMS, actualMS >= ms);
    }

    /**
     * Test whether contest clock start/stop sets correct elapsed time, Bug 707.
     * 
     * Using ContestTime directly.
     * 
     * @throws Exception
     */
    public void testElapsedStartStop() throws Exception {
        
        if (isFastJUnitTesting()){
            return;
        }

        String testName = this.getName();

        /**
         * Number of second to wait before starting and stopping contest clock.
         */
        int secondsToWait = 3;

        long ms = secondsToWait * Constants.MS_PER_SECONDS;

        ContestTime contestTime = new ContestTime();

        contestTime.startContestClock();

        debugPrint(testName + ": sleep for " + secondsToWait + " seconds.");
        Thread.sleep(ms);

        contestTime.stopContestClock();
        long actualSecs = contestTime.getElapsedSecs();
        assertTrue("After stop, expecting elapsed time secs > " + secondsToWait + ", was=" + secondsToWait, actualSecs >= secondsToWait);

        long actualMS = contestTime.getElapsedMS();
        assertTrue("After stop, expecting elapsed time ms > " + ms + ", was=" + actualMS, actualMS >= ms);

        contestTime.startContestClock();
        actualSecs = contestTime.getElapsedSecs();
        assertTrue("After stop, expecting elapsed time secs > " + secondsToWait + ", was=" + secondsToWait, actualSecs >= secondsToWait);

        debugPrint(testName + ": sleep for " + secondsToWait + " seconds.");
        Thread.sleep(ms);

        actualMS = contestTime.getElapsedMS();
        assertTrue("After start, expecting elapsed time ms > " + ms + ", was=" + actualMS, actualMS >= ms);
    }

    /**
     * Test various accessors.
     * 
     * @throws Exception
     */
    public void testTimeElements() throws Exception {
        
        if (isFastJUnitTesting()){
            return;
        }

        String testName = this.getName();

        /**
         * Number of second to wait before starting and stopping contest clock.
         */
        int secondsToWait = 3;

        long ms = secondsToWait * Constants.MS_PER_SECONDS;

        ContestTime contestTime = new ContestTime();

        assertEquals("Elapsed seconds ", contestTime.getElapsedMS() / Constants.MS_PER_SECONDS, contestTime.getElapsedSecs());
        assertEquals("Elapsed minutes  ", contestTime.getElapsedMS() / Constants.MS_PER_MINUTE, contestTime.getElapsedMins());

        assertEquals("Remaining seconds ", contestTime.getRemainingMS() / Constants.MS_PER_SECONDS, contestTime.getRemainingSecs());
        assertEquals("Remaining minutes  ", contestTime.getRemainingMS() / Constants.MS_PER_MINUTE, contestTime.getRemainingSecs() / Constants.SECONDS_PER_MINUTE);

        contestTime.stopContestClock();

        assertEquals("Elapsed seconds ", contestTime.getElapsedMS() / Constants.MS_PER_SECONDS, contestTime.getElapsedSecs());
        assertEquals("Elapsed minutes  ", contestTime.getElapsedMS() / Constants.MS_PER_MINUTE, contestTime.getElapsedMins());

        assertEquals("Remaining seconds ", contestTime.getRemainingMS() / Constants.MS_PER_SECONDS, contestTime.getRemainingSecs());
        assertEquals("Remaining minutes  ", contestTime.getRemainingMS() / Constants.MS_PER_MINUTE, contestTime.getRemainingSecs() / Constants.SECONDS_PER_MINUTE);

        debugPrint(testName + ": sleep for " + secondsToWait + " seconds.");
        Thread.sleep(ms);

        assertEquals("Elapsed seconds ", contestTime.getElapsedMS() / Constants.MS_PER_SECONDS, contestTime.getElapsedSecs());
        assertEquals("Elapsed minutes  ", contestTime.getElapsedMS() / Constants.MS_PER_MINUTE, contestTime.getElapsedMins());

        assertEquals("Remaining seconds ", contestTime.getRemainingMS() / Constants.MS_PER_SECONDS, contestTime.getRemainingSecs());
        assertEquals("Remaining minutes  ", contestTime.getRemainingMS() / Constants.MS_PER_MINUTE, contestTime.getRemainingSecs() / Constants.SECONDS_PER_MINUTE);
        
        contestTime.setElapsedSecs(3 * Constants.SECONDS_PER_MINUTE);
        
        assertEquals("Elapsed seconds ", contestTime.getElapsedMS() / Constants.MS_PER_SECONDS, contestTime.getElapsedSecs());
        assertEquals("Elapsed minutes  ", contestTime.getElapsedMS() / Constants.MS_PER_MINUTE, contestTime.getElapsedMins());

        assertEquals("Remaining seconds ", contestTime.getRemainingMS() / Constants.MS_PER_SECONDS, contestTime.getRemainingSecs());
        assertEquals("Remaining minutes  ", contestTime.getRemainingMS() / Constants.MS_PER_MINUTE, contestTime.getRemainingSecs() / Constants.SECONDS_PER_MINUTE);

        contestTime.resetClock();
        
        assertEquals("Elapsed seconds ", contestTime.getElapsedMS() / Constants.MS_PER_SECONDS, contestTime.getElapsedSecs());
        assertEquals("Elapsed minutes  ", contestTime.getElapsedMS() / Constants.MS_PER_MINUTE, contestTime.getElapsedMins());

        assertEquals("Remaining seconds ", contestTime.getRemainingMS() / Constants.MS_PER_SECONDS, contestTime.getRemainingSecs());
        assertEquals("Remaining minutes  ", contestTime.getRemainingMS() / Constants.MS_PER_MINUTE, contestTime.getRemainingSecs() / Constants.SECONDS_PER_MINUTE);
       
        contestTime.setElapsedMins(40);

        assertEquals("Elapsed seconds ", contestTime.getElapsedMS() / Constants.MS_PER_SECONDS, contestTime.getElapsedSecs());
        assertEquals("Elapsed minutes  ", contestTime.getElapsedMS() / Constants.MS_PER_MINUTE, contestTime.getElapsedMins());

        assertEquals("Remaining seconds ", contestTime.getRemainingMS() / Constants.MS_PER_SECONDS, contestTime.getRemainingSecs());
        assertEquals("Remaining minutes  ", contestTime.getRemainingMS() / Constants.MS_PER_MINUTE, contestTime.getRemainingSecs() / Constants.SECONDS_PER_MINUTE);
    }
}
