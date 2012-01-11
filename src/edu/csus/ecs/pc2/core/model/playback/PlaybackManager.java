package edu.csus.ecs.pc2.core.model.playback;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Properties;
import java.util.Vector;

import edu.csus.ecs.pc2.core.IInternalController;
import edu.csus.ecs.pc2.core.Utilities;
import edu.csus.ecs.pc2.core.list.JudgementNotificationsList;
import edu.csus.ecs.pc2.core.model.ClientId;
import edu.csus.ecs.pc2.core.model.ClientType.Type;
import edu.csus.ecs.pc2.core.model.ElementId;
import edu.csus.ecs.pc2.core.model.IInternalContest;
import edu.csus.ecs.pc2.core.model.Judgement;
import edu.csus.ecs.pc2.core.model.JudgementRecord;
import edu.csus.ecs.pc2.core.model.Language;
import edu.csus.ecs.pc2.core.model.PlaybackInfo;
import edu.csus.ecs.pc2.core.model.Problem;
import edu.csus.ecs.pc2.core.model.Run;
import edu.csus.ecs.pc2.core.model.Run.RunStates;
import edu.csus.ecs.pc2.core.model.RunExecutionStatus;
import edu.csus.ecs.pc2.core.model.RunFiles;
import edu.csus.ecs.pc2.core.model.RunUtilities;
import edu.csus.ecs.pc2.core.model.SerializedFile;
import edu.csus.ecs.pc2.core.model.playback.ReplayEvent.EventType;
import edu.csus.ecs.pc2.core.packet.Packet;
import edu.csus.ecs.pc2.core.packet.PacketFactory;

/**
 * Loads and Manages playback (events) for previous contests.
 * 
 * A {@link PlaybackManager} has methods to load {@link ReplayEvent}s and {@link ReplayEventDetails} for events to be re-played/run.
 * 
 * The {@link PlaybackManager} contains methods to maage the play back of {@link ReplayEvent}s and create {@link PlaybackRecord}s that contain the state of each {@link ReplayEvent}.
 * 
 * @see ReplayEvent
 * @see PlaybackRecord
 * 
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */

// $HeadURL$
public class PlaybackManager {

    public static final String ACTION_KEY = "action";

    public static final String ID_KEY = "id";

    public static final String SITE_KEY = "site";

    public static final String PROBLEM_KEY = "problem";

    public static final String LANGUAGE_KEY = "language";

    public static final String MAINFILE_KEY = "mainfile";

    public static final String SUBMIT_CLIENT_KEY = "submitclient";

    public static final String ELAPSED_KEY = "elapsed";

    private static final String DELIMITER = "";

    public static final String IS_SOLVED = "solved";

    public static final String IS_PRELIMINARY = "preliminary";

    public static final String JUDGED_ELAPSED_TIME = "judged_elapsed_time";

    public static final String IS_COMPUTER_JUDGED = "computer_judged";

    public static final String JUDGEMENT_TEXT = "judgement";

    public static final String JUDGE_CLIENT_KEY = "judgeclient";

    public static final String JUDGE_CLIENT_SITE = "judgeclientsite";

    public static final String IS_SEND_TO_TEAMS = "senttoteams";

    private PlaybackInfo playbackInfo = new PlaybackInfo();

    private Vector<PlaybackRecord> playbackList = new Vector<PlaybackRecord>();

    private boolean playbackRunning = false;

    /**
     * 
     * @param filename
     * @param internalContest
     * @return
     * @throws Exception
     */
    private ReplayEvent[] load(String filename, IInternalContest contest) throws Exception {

        Vector<ReplayEvent> events = new Vector<ReplayEvent>();
        
        if (filename == null || "".equals(filename)) {
            throw new IllegalArgumentException("Unable to load filename, filename is null");
        }

        if (!new File(filename).exists()) {
            throw new FileNotFoundException(filename);
        }

        // for (int i = 0; i < 13; i++) {
        //
        // ClientId clientId = new ClientId(1, Type.TEAM, i + 1);
        //
        // Language language = internalContest.getLanguages()[0];
        // Problem problem = internalContest.getProblems()[0];
        // // SerializedFile file = new SerializedFile(filename);
        // Run run = new Run(clientId, language, problem);
        // run.setElapsedMins(i + 45);
        //
        // ReplayEvent playbackEvent = new ReplayEvent(EventType.RUN_SUBMIT, clientId, run);
        //
        // events.add(playbackEvent);
        // }

        String[] lines = Utilities.loadFile(filename);

        String sourceDirectory = Utilities.dirname(filename);

        int invalidLines = 0;
        int lineNumber = 0;
        Exception savedException = null;

        int eventCount = 1;

        for (String s : lines) {
            try {
                lineNumber++;

                if (s.trim().length() == 0) {
                    continue;
                }

                if (s.trim().startsWith("#")) {
                    continue;
                }

                ReplayEvent playbackEvent = createPlayBackEvent(lineNumber, contest, s, "[|]", sourceDirectory, events, eventCount);
                if (playbackEvent != null) {
                    events.add(playbackEvent);
                    eventCount++;
                } else {
                    invalidLines++;
                    System.out.println("Line " + lineNumber + ": unable to parse line: " + s);
                }

            } catch (Exception e) {
                invalidLines++;
                if (invalidLines == 1) {
                    savedException = e;
                    System.out.println("Invalid line at: " + lineNumber + " " + s);
                }
            }
        }

        if (savedException != null) {
            System.out.println("Errors on " + invalidLines + " lines, loading " + filename);
            System.err.println("Load directory: " +Utilities.getCurrentDirectory());
            savedException.printStackTrace();
            throw savedException;
        }

        return (ReplayEvent[]) events.toArray(new ReplayEvent[events.size()]);
    }

    /**
     * Return int for input string
     * 
     * @param s
     * @return zero if error, otherwise returns value.
     */
    private static int getIntegerValue(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 0;
        }
    }

    /**
     * Create playback event from input string.
     * 
     * The string is delimited by |
     * 
     * <pre>
     * eventType=submitrun|judgement##
     * site=##
     * submitClient=team##|judge##|admin##
     * id=##
     * problem=&lt;name&gt;|#
     * language=&lt;name&gt;
     * [elapsed=##]
     * mainfile=&lt;filename&gt;
     * auxfiles=&lt;filename1&gt;[,&lt;filename2&gt;
     * eventClient=judge#|admin#
     * </pre>
     * 
     * @param lineNumber
     * 
     * @param contest
     * @param sourceDir
     * @param s
     * @param delimit
     * @param events
     * @param sequenceNumber2
     * @return
     * @throws PlaybackParseException
     */
    protected ReplayEvent createPlayBackEvent(int lineNumber, IInternalContest contest, String s, String delimit, String sourceDir, Vector<ReplayEvent> events, int eventCount)
            throws PlaybackParseException {

        String[] fields = s.split(delimit);

        if (fields.length < 3) {
            throw new PlaybackParseException("line must have 3 or more fields");
        }

        ReplayEvent playbackEvent = null;

        Properties properties = mapFieldsNameValuePairs(fields);

        String command = getAndCheckValue(properties, ACTION_KEY, "'ACTION' name/value", lineNumber);

        EventType eventType = EventType.UNDEFINED;

        if (command.equalsIgnoreCase(EventType.RUN_SUBMIT.toString())) {
            eventType = ReplayEvent.EventType.RUN_SUBMIT;
            String problemName = getAndCheckValue(properties, PROBLEM_KEY, "Problem name", lineNumber);
            String languageName = getAndCheckValue(properties, LANGUAGE_KEY, "Language name", lineNumber);
            String mainfileName = getAndCheckValue(properties, MAINFILE_KEY, "Main filename", lineNumber);
            String siteId = getAndCheckValue(properties, SITE_KEY, "Site number", lineNumber);
            String submitClientName = getAndCheckValue(properties, SUBMIT_CLIENT_KEY, "Client id", lineNumber);

            String elapsedTimeStr = getAndCheckValue(properties, ELAPSED_KEY, "Elapsed time", false, lineNumber);

            Language language = findLanguage(contest, languageName);
            Problem problem = findProblem(contest, problemName);
            ClientId clientId = findClient(contest, siteId, submitClientName);

            Run run = new Run(clientId, language, problem);

            SerializedFile[] files = new SerializedFile[1];
            try {
                SerializedFile file = new SerializedFile(mainfileName);

                if (file == null || file.getBuffer() == null) {
                    throw new PlaybackParseException(lineNumber, "Could not read/find " + mainfileName);
                }
                if (file.getBuffer().length == 0) {
                    throw new PlaybackParseException(lineNumber, "No bytes for file " + mainfileName);
                }
                files[0] = file;

            } catch (Exception e) {
                e.printStackTrace();
                throw new PlaybackParseException(e);
            }

            String idStr = getAndCheckValue(properties, ID_KEY, "run/clar number", lineNumber);
            int number = Integer.parseInt(idStr);

            if (number < 1) {
                throw new PlaybackParseException(lineNumber, "invalid run/clar number: " + idStr);
            }

            run.setSiteNumber(contest.getSiteNumber());
            if (siteId != null) {
                int siteNumber = getIntegerValue(siteId);
                run.setSiteNumber(siteNumber);
            }

            run.setElapsedMins(getIntegerValue(elapsedTimeStr));
            run.setNumber(number);

            playbackEvent = new ReplayEvent(eventType, clientId, eventCount);
            ReplayEventDetails details = new ReplayEventDetails(playbackEvent, run, files);
            playbackEvent.setEventDetails(details);

            // TODO aux files, someday, maybe

        } else if (command.equalsIgnoreCase(EventType.RUN_JUDGEMENT.toString())) {

            eventType = EventType.RUN_JUDGEMENT;
            String siteId = getAndCheckValue(properties, SITE_KEY, "Site number", lineNumber);

            String idStr = getAndCheckValue(properties, ID_KEY, "run/clar number", lineNumber);
            int number = Integer.parseInt(idStr);

            if (number < 1) {
                throw new PlaybackParseException(lineNumber, "invalid run/clar number: " + idStr);
            }

            String judgeSiteString = getAndCheckValue(properties, JUDGE_CLIENT_SITE, "run/clar number", lineNumber);
            String judgeClientName = getAndCheckValue(properties, JUDGE_CLIENT_KEY, "Judge Client id", lineNumber);

            ClientId clientId = findClient(contest, judgeSiteString, judgeClientName);

            Run run = findRun(contest, siteId, number, events);
            if (run == null) {
                throw new PlaybackParseException("Could not find submitted run for judgement run site " + siteId + " run number " + number);
            }

            // writeValues(printWriter, PlaybackManager.SITE_KEY, run.getSiteNumber());
            //
            // writeValues(printWriter, PlaybackManager.JUDGED_ELAPSED_TIME, judgementRecord.getWhenJudgedTime());
            // writeValues(printWriter, PlaybackManager.IS_SOLVED, judgementRecord.isSendToTeam());
            // writeValues(printWriter, PlaybackManager.IS_SEND_TO_TEAMS, judgementRecord.isSendToTeam());

            ClientId judgeClientId = findClient(contest, siteId, judgeClientName);

            String solvedString = getAndCheckValue(properties, IS_SOLVED, "Solved flag", false, lineNumber);
            boolean solved = solvedString.equalsIgnoreCase("true");

            String judgementText = getAndCheckValue(properties, JUDGEMENT_TEXT, "Judgement", true, lineNumber);
            ElementId judgementElementId = findJudgementId(contest, solved, judgementText);
            if (judgementElementId == null) {
                throw new PlaybackParseException("Could not find judgement " + judgementText);
            }
            String computerJudgedString = getAndCheckValue(properties, IS_COMPUTER_JUDGED, "Conputer Judged flag", false, lineNumber);
            boolean computerJudged = computerJudgedString.equalsIgnoreCase("true");

            String prelimJudgedString = getAndCheckValue(properties, IS_PRELIMINARY, "Preliminary Judged", false, lineNumber);
            boolean preliminaryJudged = prelimJudgedString.equalsIgnoreCase("true");

            String sendToTeamString = getAndCheckValue(properties, IS_SEND_TO_TEAMS, "Send to teams", false, lineNumber);
            boolean sendToTeam = sendToTeamString.equalsIgnoreCase("true");

            boolean usedValidator = false;

            JudgementRecord judgementRecord = new JudgementRecord(judgementElementId, judgeClientId, solved, usedValidator, computerJudged);
            judgementRecord.setPreliminaryJudgement(preliminaryJudged);
            judgementRecord.setSendToTeam(sendToTeam);

            playbackEvent = new ReplayEvent(eventType, clientId, eventCount);
            ReplayEventDetails details = new ReplayEventDetails(playbackEvent, run, judgementRecord);
            playbackEvent.setEventDetails(details);

        } else {
            throw new PlaybackParseException(lineNumber, "Unknown event: " + command);
        }

        return playbackEvent;
    }

    private ElementId findJudgementId(IInternalContest contest, boolean solved, String judgementText) {

        Judgement[] judgements = contest.getJudgements();

        if (solved) {
            return judgements[0].getElementId();
        }

        for (Judgement judgement : judgements) {
            if (judgementText.trim().equalsIgnoreCase(judgement.getDisplayName().trim())) {
                return judgement.getElementId();
            }
        }

        return null;
    }

    /**
     * Search events for run which matches site and number.
     * 
     * @param contest
     * @param siteId
     * @param number
     * @param events
     * @return
     */
    private Run findRun(IInternalContest contest, String siteIdString, int number, Vector<ReplayEvent> events) {

        if (events == null) {
            return null;
        }

        int siteId = getIntegerValue(siteIdString);

        for (ReplayEvent event : events) {
            if (event.getEventType().equals(EventType.RUN_SUBMIT)) {
                Run run = event.getEventDetails().getRun();
                if (run.getNumber() == number && (run.getSiteNumber() == siteId)) {
                    return run;
                }
            }
        }

        return null;
    }

    private ClientId findClient(IInternalContest contest, String siteId, String loginName) throws PlaybackParseException {

        int number;

        int siteNumber = getIntegerValue(siteId);

        if (loginName.startsWith("team") && loginName.length() > 4) {
            number = getIntegerValue(loginName.substring(4));
            return new ClientId(siteNumber, Type.TEAM, number);
        } else if (loginName.startsWith("t") && loginName.length() > 1) {
            number = getIntegerValue(loginName.substring(1));
            return new ClientId(siteNumber, Type.TEAM, number);
        } else if (loginName.startsWith("judge") && loginName.length() > 5) {
            number = getIntegerValue(loginName.substring(5));
            return new ClientId(siteNumber, Type.JUDGE, number);
        }

        throw new PlaybackParseException("Could not find/match client: " + loginName);
    }

    private Problem findProblem(IInternalContest contest, String problemName) throws PlaybackParseException {

        for (Problem problem : contest.getProblems()) {
            if (problem.getDisplayName().trim().equalsIgnoreCase(problemName.trim())) {
                return problem;
            }
        }

        throw new PlaybackParseException("Could not find/match problem: " + problemName);
    }

    private Language findLanguage(IInternalContest contest, String languageName) throws PlaybackParseException {

        for (Language language : contest.getLanguages()) {
            if (language.getDisplayName().trim().equalsIgnoreCase(languageName.trim())) {
                return language;
            }
        }

        throw new PlaybackParseException("Could not find/match language: " + languageName);
    }

    private String getAndCheckValue(Properties properties, String key, String message, int lineNumber) throws PlaybackParseException {
        return getAndCheckValue(properties, key, message, true, lineNumber);
    }

    /**
     * 
     * @param properties
     * @param key
     * @param string
     * @param message
     * @param requiredOption
     *            true means throw exception if value is missing
     * @return
     * @throws PlaybackParseException
     */
    private String getAndCheckValue(Properties properties, String key, String message, boolean requiredOption, int lineNumber) throws PlaybackParseException {
        String value = properties.getProperty(key);
        if (value == null && requiredOption) {
            throw new PlaybackParseException(lineNumber, message + " value missing (key = " + key + ")");
        }
        return value;
    }

    private Properties mapFieldsNameValuePairs(String[] fields) throws PlaybackParseException {

        Properties properties = new Properties();

        int index;

        int fieldNumber = 0;

        for (String field : fields) {

            fieldNumber++;

            if (field.trim().length() == 0) {
                continue;
            }

            index = field.indexOf("=");
            if (index == -1) {
                throw new PlaybackParseException("Missing = in name/value pair, field " + fieldNumber + ": " + field);
            } else {
                String key = field.substring(0, index).trim().toLowerCase();
                String value = field.substring(index + 1);
                properties.put(key, value);
            }
        }

        return properties;
    }

    public void sendToJudgesAndOthers(IInternalController controller, Packet packet, boolean sendToServers) {

        controller.sendToAdministrators(packet);
        controller.sendToJudges(packet);
        controller.sendToScoreboards(packet);
        if (sendToServers) {
            controller.sendToServers(packet);
        }
    }

    private void writeValues(String key, long number) {
        System.out.print(key + "=" + number + DELIMITER + " ");
    }

    private void writeValues(String key, String value) {
        System.out.print(key + "=" + value + DELIMITER + " ");
    }

    private void dump(String message, PlaybackRecord playbackRecord) {

        ReplayEventDetails details = playbackRecord.getReplayEvent().getEventDetails();

        System.out.println(message);

        if (details != null && details.getRun() != null) {

            Run run = details.getRun();

            writeValues(PlaybackManager.ACTION_KEY, playbackRecord.getEventType().toString());
            writeValues(PlaybackManager.ID_KEY, run.getNumber());
            writeValues(PlaybackManager.ELAPSED_KEY, run.getElapsedMins());
            writeValues(PlaybackManager.LANGUAGE_KEY, run.getLanguageId().toString());
            writeValues(PlaybackManager.PROBLEM_KEY, run.getProblemId().toString());
            writeValues(PlaybackManager.SITE_KEY, run.getSiteNumber());
            writeValues(PlaybackManager.SUBMIT_CLIENT_KEY, run.getSubmitter().getName());
            writeValues("File size", details.getFiles()[0].getBuffer().length);

        } else {
            System.out.println(" No run details for playback event " + playbackRecord.getSequenceNumber());
        }
        System.out.println();

    }

    /**
     * Execute next Playback record.
     * 
     * Will execute next {@link EventStatus#PENDING} playback/replay record and then increment sequence number {@link #getSequenceNumber()}.
     * 
     * @param contest
     * @param controller
     * @return
     * @throws Exception
     */
    public PlaybackRecord executeNextEvent(IInternalContest contest, IInternalController controller) throws Exception {

        PlaybackRecord playbackRecord = playbackList.elementAt(playbackInfo.getSequenceNumber());

        ReplayEvent event = playbackRecord.getReplayEvent();
        ReplayEventDetails details = event.getEventDetails();

        if (Utilities.isDebugMode()) {
            dump("in executeEvent", playbackRecord);
        }

        switch (playbackRecord.getEventType()) {
            case RUN_SUBMIT:
                SerializedFile file = details.getFiles()[0];
                SerializedFile[] files = new SerializedFile[1];
                files[0] = file;

                RunFiles runFiles = new RunFiles(details.getRun(), file, new SerializedFile[0]);
                Run theRun = details.getRun();

                long savedElapsed = theRun.getElapsedMS();
                Run newRun = contest.acceptRun(theRun, runFiles);
                newRun.setPlaybackSequenceNumber(playbackRecord.getSequenceNumber());

                if (savedElapsed > 0) {
                    newRun.setElapsedMS(savedElapsed);
                }

                playbackRecord.setEventStatus(EventStatus.COMPLETED);
                
                contest.updatePlaybackInfo(playbackInfo);
                
                ClientId fromId = contest.getClientId();
                // Send to team
                Packet confirmPacket = PacketFactory.createRunSubmissionConfirm(contest.getClientId(), fromId, newRun);
                controller.sendToClient(confirmPacket);

                // Send to clients and servers
                sendToJudgesAndOthers(controller, confirmPacket, true);

                break;

            case RUN_JUDGEMENT:

                Run run = details.getRun();

                sendStatusMessge(contest, controller, run, RunExecutionStatus.COMPILING);
                sendStatusMessge(contest, controller, run, RunExecutionStatus.EXECUTING);
                sendStatusMessge(contest, controller, run, RunExecutionStatus.VALIDATING);

                JudgementRecord judgement = details.getJudgementRecord();

                Run runToUpdate = contest.getRun(run.getElementId());
                runToUpdate.addJudgement(judgement);
                ClientId whoChangedRun = judgement.getJudgerClientId();
                runToUpdate.setStatus(RunStates.JUDGED);
                contest.updateRun(runToUpdate, whoChangedRun);

                Run updatedRun = contest.getRun(run.getElementId());
                Packet runUpdatedPacket = PacketFactory.createRunUpdateNotification(contest.getClientId(), PacketFactory.ALL_SERVERS, updatedRun, whoChangedRun);
                sendToJudgesAndOthers(controller, runUpdatedPacket, true);

                /**
                 * Send Judgement Notification to Team or not.
                 */

                if (updatedRun.isJudged() && updatedRun.getJudgementRecord().isSendToTeam()) {

                    Packet notifyPacket = PacketFactory.clonePacket(contest.getClientId(), run.getSubmitter(), runUpdatedPacket);
                    sendJudgementToTeam(controller, contest, notifyPacket, updatedRun);
                }

                playbackRecord.setEventStatus(EventStatus.COMPLETED);
                contest.updatePlaybackInfo(playbackInfo);
                break;

            default:
                throw new Exception("Event " + playbackRecord.getEventType() + " not implemented, yet");
        }

        playbackInfo.incrementSequenceNumber();

        return playbackRecord;

    }
    
    /**
     * Start a playback.
     * 
     * To stop the playback {@link #setPlaybackRunning(boolean)} to false.
     * 
     * @param contest
     * @param controller
     * @param callback
     *            a runnable executed for each executed event.
     */
    public void startPlayback(final IInternalContest contest, final IInternalController controller, final Runnable callback) {

        playbackInfo.setStarted(true);
        insureMinimumPlaybackRecords(playbackInfo.getMinimumPlaybackRecords());

        new Thread(new Runnable() {

            public void run() {

                setPlaybackRunning(true);

                int waitTime = playbackInfo.getWaitBetweenEventsMS();

                while ((!allEventsExecuted()) && isPlaybackRunning()) {

                    try {
                        executeNextEvent(contest, controller);
                        
                        if (callback != null) {
                            callback.run();
                        }

                        if (waitTime > 0) {
                            Thread.sleep(waitTime);
                        }

                    } catch (Exception e) {
                        setPlaybackRunning(false);
                        if (callback != null) {
                            callback.run();
                        }
                        e.printStackTrace();
                    }
                }

                setPlaybackRunning(false);
                if (callback != null) {
                    callback.run();
                }
            }
        }).start();
    }

    private void sendStatusMessge(IInternalContest contest, IInternalController controller, Run run, RunExecutionStatus status) {

        if (contest.isSendAdditionalRunStatusMessages()) {
            Packet sendPacket = PacketFactory.createRunStatusPacket(contest.getClientId(), contest.getClientId(), run, contest.getClientId(), status);
            sendToSpectatorsAndSites(controller, sendPacket, true);
        }
    }

    /**
     * Send to spectators and servers
     * 
     * @param controller
     * 
     * @param packet
     * @param sendToServers
     */
    public void sendToSpectatorsAndSites(IInternalController controller, Packet packet, boolean sendToServers) {
        controller.sendToSpectators(packet);
        if (sendToServers) {
            controller.sendToServers(packet);
        }
    }

    private void sendJudgementToTeam(IInternalController controller, IInternalContest contest, Packet judgementPacket, Run run) {

        if (run.isJudged() && run.getJudgementRecord().isSendToTeam()) {
            JudgementNotificationsList judgementNotificationsList = contest.getContestInformation().getJudgementNotificationsList();

            if (!RunUtilities.supppressJudgement(judgementNotificationsList, run, contest.getContestTime())) {
                // Send to team who sent it, send to other server if needed.
                controller.sendToClient(judgementPacket);
            } else {
                controller.getLog().info("Notification not sent to " + run.getSubmitter() + " for run " + run);
            }
        } else {
            controller.getLog().warning("Attempted to send back unjudged run to team " + run);
        }
    }

    public int getSequenceNumber() {
        return playbackInfo.getSequenceNumber();
    }

    public void rewind() {
        playbackInfo.rewind();

    }

    public PlaybackInfo createPlaybackInfo(String inputFilename, IInternalContest contest) throws Exception {

        ReplayEvent[] list = load(inputFilename, contest);
        playbackInfo = new PlaybackInfo("Default Playback Name", list);
        playbackInfo.setSiteNumber(contest.getSiteNumber());
        playbackInfo.setFilename(inputFilename);

        int nextSequence = 1;

        PlaybackRecord[] playbackRecords = new PlaybackRecord[list.length];
        for (int i = 0; i < list.length; i++) {
            playbackRecords[i] = new PlaybackRecord(list[i], nextSequence);
            nextSequence++;
        }

        playbackList.addAll(Arrays.asList(playbackRecords));

        return playbackInfo;
    }

    /**
     * Insure up to N {@link PlaybackRecord} records.
     * 
     * @param minimumNumberOfRecords
     *            number of records that must be in the {@link PlaybackInfo}.
     * @return playback info
     */
    public PlaybackRecord[] insureMinimumPlaybackRecords(int minimumNumberOfRecords) {

        int numberRecordsToCreate = minimumNumberOfRecords - getPlaybackRecords().length;

        ReplayEvent[] replays = playbackInfo.getReplayList();
        int maxReplayRecords = replays.length;

        if (numberRecordsToCreate > 0 && maxReplayRecords > 0) {

            PlaybackRecord[] playbackRecords = new PlaybackRecord[numberRecordsToCreate];

            /*
             * In order to insure that when replay records are added we should
             * not start at the beginning of the replay list, we should start in
             * the replay list where we left off.
             * 
             * For example, if there were 8 replay records and we have added 3 replay
             * records from that list and now we want to add 2 replay records - we
             * would want to add records (zero base) offset 3 and 4, not offset 0 and 1.
             */

            int offset = getPlaybackRecords().length % maxReplayRecords;

            int nextSequence = getPlaybackRecords().length + 1;

            for (int i = 0; i < numberRecordsToCreate; i++) {

                if (offset >= maxReplayRecords) {
                    offset = 0;
                }

                playbackRecords[i] = new PlaybackRecord(replays[offset], nextSequence);

                nextSequence++;
                offset++;

            }

            playbackList.addAll(Arrays.asList(playbackRecords));

        }

        return getPlaybackRecords();
    }

    /**
     * Return complete list of playback records.
     * 
     * @return
     */
    public PlaybackRecord[] getPlaybackRecords() {
        return (PlaybackRecord[]) playbackList.toArray(new PlaybackRecord[playbackList.size()]);
    }

    public boolean allEventsExecuted() {
        return playbackInfo.getSequenceNumber() > getPlaybackRecords().length;
    }

    public PlaybackInfo getPlaybackInfo() {
        return playbackInfo;
    }
    
    public boolean isPlaybackRunning() {
        return playbackRunning;
    }

    public void setPlaybackRunning(boolean playbackRunning) {
        this.playbackRunning = playbackRunning;
    }

    public PlaybackRecord getCurrentPlaybackRecord() {
        return getPlaybackRecords()[getSequenceNumber()];
    }
}
