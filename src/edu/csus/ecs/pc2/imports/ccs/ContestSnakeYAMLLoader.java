package edu.csus.ecs.pc2.imports.ccs;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Vector;

import javax.xml.bind.DatatypeConverter;

import org.yaml.snakeyaml.Yaml;
import org.yaml.snakeyaml.error.Mark;
import org.yaml.snakeyaml.error.MarkedYAMLException;

import edu.csus.ecs.pc2.core.Constants;
import edu.csus.ecs.pc2.core.StringUtilities;
import edu.csus.ecs.pc2.core.Utilities;
import edu.csus.ecs.pc2.core.exception.YamlLoadException;
import edu.csus.ecs.pc2.core.imports.LoadICPCTSVData;
import edu.csus.ecs.pc2.core.list.AccountList;
import edu.csus.ecs.pc2.core.list.AccountList.PasswordType;
import edu.csus.ecs.pc2.core.model.Account;
import edu.csus.ecs.pc2.core.model.AutoJudgeSetting;
import edu.csus.ecs.pc2.core.model.Category;
import edu.csus.ecs.pc2.core.model.ClientId;
import edu.csus.ecs.pc2.core.model.ClientSettings;
import edu.csus.ecs.pc2.core.model.ClientType;
import edu.csus.ecs.pc2.core.model.ContestInformation;
import edu.csus.ecs.pc2.core.model.ContestTime;
import edu.csus.ecs.pc2.core.model.Filter;
import edu.csus.ecs.pc2.core.model.Group;
import edu.csus.ecs.pc2.core.model.IInternalContest;
import edu.csus.ecs.pc2.core.model.InternalContest;
import edu.csus.ecs.pc2.core.model.Language;
import edu.csus.ecs.pc2.core.model.LanguageAutoFill;
import edu.csus.ecs.pc2.core.model.PlaybackInfo;
import edu.csus.ecs.pc2.core.model.Problem;
import edu.csus.ecs.pc2.core.model.Problem.VALIDATOR_TYPE;
import edu.csus.ecs.pc2.core.model.ProblemDataFiles;
import edu.csus.ecs.pc2.core.model.SerializedFile;
import edu.csus.ecs.pc2.core.model.Site;
import edu.csus.ecs.pc2.validator.clicsValidator.ClicsValidatorSettings;
import edu.csus.ecs.pc2.validator.customValidator.CustomValidatorSettings;
import edu.csus.ecs.pc2.validator.pc2Validator.PC2ValidatorSettings;

/**
 * Load contest from Yaml using SnakeYaml methods.
 * 
 * @author Douglas A. Lane, PC^2 Team, pc2@ecs.csus.edu
 */
public class ContestSnakeYAMLLoader implements IContestLoader {

    /**
     * Full content of yaml file.
     */
    private Map<String, Object> fullYamlContent = null;

    /**
     * Load Problem Data File Contents
     */
    private boolean loadProblemDataFiles = true;

    @SuppressWarnings("unchecked")
    public Map<String, Object> loadYaml(String filename) {
        try {
            Yaml yaml = new Yaml();
            return (Map<String, Object>) yaml.load(new FileInputStream(filename));
        } catch (MarkedYAMLException e) {
            throw new YamlLoadException(getSnakeParserDetails(e), e, filename);
        } catch (FileNotFoundException e) {
            throw new YamlLoadException("File not found " + filename, e, filename);
        }
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> loadYaml(String filename, String[] yamlLines) {
        try {
            Yaml yaml = new Yaml();
            String fullString = StringUtilities.join("\n", yamlLines);
            InputStream stream = new ByteArrayInputStream(fullString.getBytes(StandardCharsets.UTF_8));
            return (Map<String, Object>) yaml.load(stream);
        } catch (MarkedYAMLException e) {
            throw new YamlLoadException(getSnakeParserDetails(e), e, filename);
        }
    }

    /**
     * Create a simple string with parse info.
     * 
     * @param markedYAMLException
     * @return
     */

    String getSnakeParserDetails(MarkedYAMLException markedYAMLException) {

        Mark mark = markedYAMLException.getProblemMark();

        int lineNumber = mark.getLine() + 1; // starts at zero
        int columnNumber = mark.getColumn() + 1; // starts at zero

        return "Parse error at line=" + lineNumber + " column=" + columnNumber + " message=" + markedYAMLException.getProblem();

    }

    @Override
    public IInternalContest fromYaml(IInternalContest contest, String directoryName) {
        return fromYaml(contest, directoryName, true);
    }

    @Override
    public IInternalContest fromYaml(IInternalContest contest, String directoryName, boolean loadDataFileContents) {
        String[] contents;
        String contetYamlFilename = getContestYamlFilename(directoryName);
        try {
            // TODO would it be easier to load all yaml files instead?
            contents = loadFileWithIncludes(directoryName, contetYamlFilename);
            contetYamlFilename = DEFAULT_SYSTEM_YAML_FILENAME;
            if (new File(directoryName + File.separator + contetYamlFilename).exists()) {
                String[] lines = Utilities.loadFile(directoryName + File.separator + contetYamlFilename);
                contents = concat(contents, lines);
            }
            contetYamlFilename = DEFAULT_PROBLEM_SET_YAML_FILENAME;
            if (new File(directoryName + File.separator + contetYamlFilename).exists()) {
                String[] lines = Utilities.loadFile(directoryName + File.separator + contetYamlFilename);
                contents = concat(contents, lines);
            }
            contetYamlFilename = "system.pc2.yaml";
            if (new File(directoryName + File.separator + contetYamlFilename).exists()) {
                String[] lines = Utilities.loadFile(directoryName + File.separator + contetYamlFilename);
                contents = concat(contents, lines);
            }
        } catch (IOException e) {
            throw new YamlLoadException(e.getMessage(), e, contetYamlFilename);
        }
        return fromYaml(contest, contents, directoryName, loadDataFileContents);
    }

    private String[] concat(String[] a, String[] b) {
        int aLen = a.length;
        int bLen = b.length;
        String[] c = new String[aLen + bLen];
        System.arraycopy(a, 0, c, 0, aLen);
        System.arraycopy(b, 0, c, aLen, bLen);
        return c;
    }

    private String getContestYamlFilename(String directoryName) {
        return directoryName + File.separator + DEFAULT_CONTEST_YAML_FILENAME;
    }

    /**
     * Load files with #include.
     * 
     * @param dirname
     *            if null will ignore #include files.
     * @param filename
     *            YAML input file
     * @throws IOException
     */
    @Override
    public String[] loadFileWithIncludes(String dirname, String filename) throws IOException {

        if (!new File(filename).isFile()) {
            throw new FileNotFoundException(filename);
        }

        ArrayList<String> outs = new ArrayList<String>();

        String[] lines = Utilities.loadFile(filename);

        for (String line : lines) {

            outs.add(line);
            if (dirname != null && line.trim().startsWith("#include")) {
                String[] parts = line.split("\"");
                String includeFilename = dirname + File.separator + parts[1];
                String[] includeLines = Utilities.loadFile(includeFilename);
                for (String string : includeLines) {
                    outs.add(string);
                }
                outs.add("# end include " + includeFilename);
            }
        }

        return (String[]) outs.toArray(new String[outs.size()]);
    }

    @Override
    public String getContestTitle(String contestYamlFilename) throws IOException {
        return fetchValue(new File(contestYamlFilename), IContestLoader.CONTEST_NAME_KEY);
    }

    protected String fetchValue(File file, String key) {
        Map<String, Object> content = getContent(file.getAbsolutePath());
        return (String) content.get(key);
    }

    private Map<String, Object> getContent(String filename) {
        if (fullYamlContent == null) {
            fullYamlContent = loadYaml(filename);
        }

        return fullYamlContent;
    }

    @Override
    public String getJudgesCDPBasePath(String contestYamlFilename) throws IOException {
        return fetchFileValue(contestYamlFilename, JUDGE_CONFIG_PATH_KEY);
    }

    private String fetchFileValue(String filename, String key) {
        return fetchValue(new File(filename), key);
    }

    /**
     * Insures that contest is instantiated.
     * 
     * Creates contest if contest is null, otherwise returns contest.
     * 
     * @param contest
     * @return
     */
    private IInternalContest createContest(IInternalContest contest) {
        if (contest == null) {
            contest = new InternalContest();
            contest.setSiteNumber(1);
        }
        return contest;
    }

    private void setTitle(IInternalContest contest, String title) {
        ContestInformation contestInformation = contest.getContestInformation();
        contestInformation.setContestTitle(title);
        contest.updateContestInformation(contestInformation);

    }

    private void setCcsTestMode(IInternalContest contest, boolean ccsTestMode) {
        ContestInformation contestInformation = contest.getContestInformation();
        contestInformation.setCcsTestMode(ccsTestMode);
        contest.updateContestInformation(contestInformation);

    }

    /**
     * Parse and convert dateString to Date.
     * 
     * 
     * @param dateString
     *            date string in form: yyyy-MM-dd HH:mm or yyyy-MM-dd HH:mmZ
     * @return date for input string
     * @throws ParseException
     */
    public static Date parseSimpleDate(String dateString) throws ParseException {
        // String pattern = "yyyy-MM-dd HH:mmZ";
        String pattern = "yyyy-MM-dd HH:mm";

        String dateTime = dateString;

        if (dateString.length() == pattern.length() + 1) {
            // Strip off Z
            dateTime = dateString.substring(0, pattern.length());
        }
        SimpleDateFormat parser = new SimpleDateFormat(pattern);
        Date date = parser.parse(dateTime);

        return date;
    }

    private void setCDPPath(IInternalContest contest, String path) {
        ContestInformation contestInformation = contest.getContestInformation();
        contestInformation.setJudgeCDPBasePath(path);
        contest.updateContestInformation(contestInformation);

    }

    // private int getIntValue(String string, int defaultNumber) {
    //
    // int number = defaultNumber;
    //
    // if (string != null && string.length() > 0) {
    // number = Integer.parseInt(string.trim());
    // }
    //
    // return number;
    // }

    /**
     * Returns boolean for input string.
     * 
     * Matches (case insensitive) yes, no, true, false.
     * 
     * @param string
     * @param defaultBoolean
     * @return default if string does not match (case-insensitive) string
     */
    @Override
    public boolean getBooleanValue(String string, boolean defaultBoolean) {

        boolean value = defaultBoolean;

        if (string != null && string.length() > 0) {
            string = string.trim();
            if ("yes".equalsIgnoreCase(string)) {
                value = true;
            } else if ("no".equalsIgnoreCase(string)) {
                value = false;
            } else if ("true".equalsIgnoreCase(string)) {
                value = true;
            } else if ("false".equalsIgnoreCase(string)) {
                value = false;
            }
        }

        return value;
    }

    private void setContestStartDateTime(IInternalContest contest, Date date) {
        ContestInformation contestInformation = contest.getContestInformation();
        contestInformation.setScheduledStartDate(date);
        contestInformation.setAutoStartContest(isBeforeNow(date));
    }

    /**
     * Input date before now, aka current date/time.
     * 
     * @param date
     * @return
     */
    protected boolean isBeforeNow(Date date) {
        Date now = new Date();
        return now.before(date);
    }

    @Override
    public IInternalContest fromYaml(IInternalContest contest, String[] yamlLines, String directoryName, boolean loadDataFileContents) {

        contest = createContest(contest);

        // name: ACM-ICPC World Finals 2011

        String contestFileName = getContestYamlFilename(directoryName);

        Map<String, Object> content = loadYaml(contestFileName, yamlLines);

        if (content == null) {
            return contest;
        }

        setTitle(contest, null);

        String contestTitle = fetchValue(content, CONTEST_NAME_KEY);
        if (contestTitle != null) {
            setTitle(contest, contestTitle);
        }

        boolean ccsTestMode = fetchBooleanValue(content, CCS_TEST_MODE, false);
        if (ccsTestMode) {
            setCcsTestMode(contest, ccsTestMode);
        }

        String judgeCDPath = fetchValue(content, JUDGE_CONFIG_PATH_KEY);
        if (judgeCDPath != null) {
            setCDPPath(contest, judgeCDPath);
        } else {
            setCDPPath(contest, directoryName);
        }

        Integer defaultTimeout = fetchIntValue(content, TIMEOUT_KEY, DEFAULT_TIME_OUT);

        for (String line : yamlLines) {
            if (line.startsWith(CONTEST_NAME_KEY + DELIMIT)) {
                setTitle(contest, unquoteAll(line.substring(line.indexOf(DELIMIT) + 1).trim()));

            }
        }

        loadDataFileContents = fetchBooleanValue(content, PROBLEM_LOAD_DATA_FILES_KEY, loadDataFileContents);

        String shortContestName = fetchValue(content, SHORT_NAME_KEY);
        if (shortContestName != null) {
            setShortContestName(contest, shortContestName);
        }

        if (null != fetchValue(content, AUTO_STOP_CLOCK_AT_END_KEY)) {
            // only set value if key present

            boolean autoStopClockAtEnd = fetchBooleanValue(content, AUTO_STOP_CLOCK_AT_END_KEY, false);
            setAutoStopClockAtEnd(contest, autoStopClockAtEnd);
        }

        String contestLength = fetchValue(content, CONTEST_DURATION_KEY);
        if (contestLength != null) {
            setContestLength(contest, contestLength);
        }

        // Old yaml name
        String scoreboardFreezeTime = fetchValue(content, SCOREBOARD_FREEZE_KEY);
        if (scoreboardFreezeTime != null) {
            setScoreboardFreezeTime(contest, scoreboardFreezeTime);
        }

        // New yaml name
        scoreboardFreezeTime = fetchValue(content, SCOREBOARD_FREEZE_LENGTH_KEY);
        if (scoreboardFreezeTime != null) {
            setScoreboardFreezeTime(contest, scoreboardFreezeTime);
        }

        Object startTimeObject = fetchObjectValue(content, CONTEST_START_TIME_KEY);

        Date date = null;
        if (startTimeObject != null && startTimeObject instanceof Date) {
            setContestStartDateTime(contest, (Date) startTimeObject);
        } else {

            String startTime = fetchValue(content, CONTEST_START_TIME_KEY);

            if (startTime != null) {

                /**
                 * Support previous format yyyy-MM-dd HH:mm or yyyy-MM-dd HH:mmZ
                 */

                try {
                    date = parseSimpleDate(startTime);
                    setContestStartDateTime(contest, date);
                } catch (ParseException e) {
                    date = null;
                    /**
                     * No longer a failure, will attempt to parse using ISO 8601 format
                     */
                }

                /**
                 * Parse ISO 8601 format date.
                 */

                if (date == null) {

                    try {

                        date = parseISO8601Date(startTime);
                        setContestStartDateTime(contest, date);

                    } catch (IllegalArgumentException e) {
                        throw new YamlLoadException("Invalid start-time value '" + startTime + " expected ISO 8601 format, " + e.getMessage(), e, contestFileName);
                    }
                }
            }
        }

        Object maxOutputSize = fetchObjectValue(content, MAX_OUTPUT_SIZE_K_KEY);
        if (maxOutputSize != null) {

            if (maxOutputSize instanceof Integer) {
                int maxSizeInK = ((Integer) maxOutputSize).intValue();
                if (maxSizeInK > 0) {
                    setMaxOutputSize(contest, maxSizeInK * 1000);
                } else {
                    throw new YamlLoadException("Invalid max-output-size-K value '" + maxOutputSize + " size must be > 0 ", null, contestFileName);
                }
            } else {
                throw new YamlLoadException("Invalid max-output-size-K value '" + maxOutputSize + " size must an integer", null, contestFileName);
            }
        }

        Language[] languages = getLanguages(yamlLines);
        for (Language language : languages) {
            contest.addLanguage(language);
        }

        String defaultValidatorCommandLine = fetchValue(content, DEFAULT_VALIDATOR_KEY);

        String overrideValidatorCommandLine = fetchValue(content, OVERRIDE_VALIDATOR_KEY);
        if (overrideValidatorCommandLine == null) {

            // if no override defined, then maybe use mtsv

            File mtsvFile = new File(directoryName + File.separator + MTSV_PROGRAM_NAME);
            if (mtsvFile.exists()) {
                /**
                 * If mtsv is in the same directory as the contest.yaml, then use the mtsv command line.
                 */
                overrideValidatorCommandLine = mtsvFile.getAbsolutePath() + " " + MTSV_OVERRIDE_VALIDATOR_ARGS;
            }
        }

        boolean overrideUsePc2Validator = false;
        String usingValidator = fetchValue(content, IContestLoader.USING_PC2_VALIDATOR);

        if (usingValidator != null && usingValidator.equalsIgnoreCase("true")) {
            overrideUsePc2Validator = true;
        }

        /**
         * Manual Review global override.
         */
        boolean manualReviewOverride = false;

        @SuppressWarnings("unchecked")
        LinkedHashMap<String, Object> judgingTypeContent = (LinkedHashMap<String, Object>) content.get(JUDGING_TYPE_KEY);
        if (judgingTypeContent != null) {
            manualReviewOverride = fetchBooleanValue(judgingTypeContent, MANUAL_REVIEW_KEY, false);
        } else {
            manualReviewOverride = fetchBooleanValue(content, MANUAL_REVIEW_KEY, false);
        }

        Problem[] problems = getProblems(yamlLines, defaultTimeout, loadDataFileContents, defaultValidatorCommandLine, overrideValidatorCommandLine, overrideUsePc2Validator, manualReviewOverride);

        if (loadProblemDataFiles) {
            for (Problem problem : problems) {
                loadProblemInformationAndDataFiles(contest, directoryName, problem, overrideUsePc2Validator, manualReviewOverride);
                if (overrideValidatorCommandLine != null) {
                    problem.setValidatorCommandLine(overrideValidatorCommandLine);
                }
            }
        }

        Site[] sites = getSites(yamlLines);
        for (Site site : sites) {
            Site existingSite = contest.getSite(site.getSiteNumber());
            if (existingSite == null) {
                contest.addSite(site);
            } else {
                // updateSite works by elementId, so we have to modify the existing object
                existingSite.setDisplayName(site.getDisplayName());
                existingSite.setPassword(site.getPassword());
                Properties props = new Properties();
                props.put(Site.IP_KEY, site.getConnectionInfo().getProperty(Site.IP_KEY));
                props.put(Site.PORT_KEY, site.getConnectionInfo().getProperty(Site.PORT_KEY));
                existingSite.setConnectionInfo(props);
                contest.updateSite(existingSite);
            }
        }

        String[] categories = loadGeneralClarificationAnswers(yamlLines);
        for (String name : categories) {
            contest.addCategory(new Category(name));
        }

        // String[] answers = getGeneralAnswers(yamlLines);
        // TODO CCS load general answer catagories into contest

        Account[] accounts = getAccounts(yamlLines);
        contest.addAccounts(accounts);

        AutoJudgeSetting[] autoJudgeSettings = null;
        if (problems.length == 0) {
            /**
             * If no problems in input YAML assume problems are already defined in contest/model.
             */
            autoJudgeSettings = getAutoJudgeSettings(yamlLines, contest.getProblems());

        } else {
            autoJudgeSettings = getAutoJudgeSettings(yamlLines, problems);

        }

        for (AutoJudgeSetting auto : autoJudgeSettings) {
            addAutoJudgeSetting(contest, auto);
        }

        PlaybackInfo playbackInfo = getReplaySettings(yamlLines);

        if (playbackInfo != null) {
            contest.addPlaybackInfo(playbackInfo);
        }

        return contest;

    }

    /**
     * Find group for input string.
     * 
     * @param groups
     * @param groupInfo
     *            group external id or group name
     * @return null if no match, else the group
     */
    protected Group lookupGroupInfo(Group[] groups, String string) {

        for (Group group : groups) {
            if (group.getDisplayName().equals(string.trim())) {
                return group;
            } else {
                int id = toInt(string, -1);
                if (group.getGroupId() == id) {
                    return group;
                }
            }

        }
        return null;
    }

    /**
     * 
     * @param stop
     *            - true, auto stop at end of contest
     */
    private void setAutoStopClockAtEnd(IInternalContest contest, boolean stop) {
        ContestInformation contestInformation = contest.getContestInformation();
        contestInformation.setAutoStopContest(stop);
        contest.updateContestInformation(contestInformation);
    }

    private void setMaxOutputSize(IInternalContest contest, int maxFileSize) {
        ContestInformation contestInformation = contest.getContestInformation();
        contestInformation.setMaxFileSize(maxFileSize);
        contest.updateContestInformation(contestInformation);
    }

    public Date parseISO8601Date(String startTime) {
        Calendar cal = DatatypeConverter.parseDateTime(startTime);
        Date date = cal.getTime();
        return date;
    }

    private void setScoreboardFreezeTime(IInternalContest contest, String scoreboardFreezeTime) {
        ContestInformation contestInformation = contest.getContestInformation();
        contestInformation.setFreezeTime(scoreboardFreezeTime);
        contest.updateContestInformation(contestInformation);
    }

    private void setContestLength(IInternalContest contest, String contestLength) {
        ContestTime time = contest.getContestTime();
        if (time == null) {
            time = new ContestTime();
            time.setSiteNumber(contest.getSiteNumber());
        }
        time.setContestLengthSecs(parseTimeIntoSeconds(contestLength, Constants.DEFAULT_CONTEST_LENGTH_SECONDS));
        contest.updateContestTime(time);
    }

    /**
     * Parses input string and returns number of seconds.
     * 
     * form can be integer or HH:MM:SS
     * 
     * @param defaultLongValue
     * 
     * @param timeString
     * @param defaultLongValue
     *            - default value if parsing error or no valid time format found
     * @return
     */
    public long parseTimeIntoSeconds(String timeString, long defaultLongValue) {

        if (timeString.indexOf(':') > 0) {
            // likely form: HH:MM:SS

            // TODO TODAY CLEANUP - make Utilties.stringToLongSecs static
            // long secs = Utilities.stringToLongSecs(timeString);
            long secs = stringToLongSecs(timeString);
            return secs;
        } else {
            // is a integer or long

            try {
                long l = Long.parseLong(timeString);
                return l;
            } catch (Exception e) {
                return defaultLongValue;
            }
        }

    }

    private void setShortContestName(IInternalContest contest, String shortContestName) {
        ContestInformation contestInformation = contest.getContestInformation();
        contestInformation.setContestShortName(shortContestName);
        contest.updateContestInformation(contestInformation);

    }

    /**
     * Get boolean value for input key in map.
     * 
     * Returns defaultVaue if no entry matches key.
     * 
     * @param content
     * @param key
     * @param defaultValue
     * @return defaultValue or value from item in map.
     */
    private boolean fetchBooleanValue(Map<String, Object> content, String key, boolean defaultValue) {
        Object object = content.get(key);
        Boolean value = false;
        if (object == null) {
            return defaultValue;
        } else if (object instanceof Boolean) {
            value = (Boolean) content.get(key);
        } else if (object instanceof String) {
            value = getBooleanValue((String) content.get(key), defaultValue);
        }
        return value;
    }

    private void addAutoJudgeSetting(IInternalContest contest, AutoJudgeSetting auto) {

        Account account = contest.getAccount(auto.getClientId());
        if (account == null) {
            throw new YamlLoadException("No such account for auto judge setting, undefined account is " + auto.getClientId());
        }

        ClientSettings clientSettings = contest.getClientSettings(auto.getClientId());
        if (clientSettings == null) {
            clientSettings = new ClientSettings(auto.getClientId());
        }
        clientSettings.setAutoJudgeFilter(auto.getProblemFilter());
        clientSettings.setAutoJudging(auto.isActive());

        // dumpAJSettings(clientSettings.getClientId(), clientSettings.isAutoJudging(), clientSettings.getAutoJudgeFilter());

        contest.addClientSettings(clientSettings);
    }

    @SuppressWarnings("unchecked")
    private Account[] getAccounts(String[] yamlLines) {

        Vector<Account> accountVector = new Vector<Account>();
        AccountList accountList = new AccountList();

        Map<String, Object> yamlContent = loadYaml(null, yamlLines);
        ArrayList<Map<String, Object>> list = fetchList(yamlContent, ACCOUNTS_KEY);

        if (list != null) {
            for (Object object : list) {

                Map<String, Object> map = (Map<String, Object>) object;

                String accountType = fetchValue(map, "account");
                checkField(accountType, "Account Type");

                ClientType.Type type = ClientType.Type.valueOf(accountType.trim());
                Integer startNumber = fetchIntValue(map, "start", 1);
                Integer count = fetchIntValue(map, "count", 1);
                Integer siteNumber = fetchIntValue(map, "site", 1);

                /**
                 * <pre>
                 * 
                 * accounts:
                 *   -account: TEAM
                 *       site: 1
                 *      start: 100
                 *      count: 14
                 * 
                 *   -account: JUDGE
                 *       site: 1
                 *      count: 12
                 * </pre>
                 */

                Vector<Account> newAccounts = accountList.generateNewAccounts(type, count, startNumber, PasswordType.JOE, siteNumber, true);

                accountVector.addAll(newAccounts);

            }

        }

        return (Account[]) accountVector.toArray(new Account[accountVector.size()]);

    }

    private Object fetchObjectValue(Map<String, Object> content, String key) {
        if (content == null) {
            return null;
        }
        Object value = content.get(key);
        return value;
    }

    private String fetchValue(Map<String, Object> content, String key) {
        if (content == null) {
            return null;
        }
        Object value = content.get(key);
        if (value == null) {
            return null;
        } else if (value instanceof String) {
            return (String) content.get(key);
        } else {
            return content.get(key).toString();
        }
    }

    private boolean isValuePresent(Map<String, Object> content, String key) {
        if (content == null) {
            return false;
        }
        Object value = content.get(key);
        return value != null;
    }

    @SuppressWarnings("unchecked")
    @Override
    public PlaybackInfo getReplaySettings(String[] yamlLines) {

        PlaybackInfo info = new PlaybackInfo();

        Map<String, Object> yamlContent = loadYaml(null, yamlLines);
        ArrayList<Map<String, Object>> list = fetchList(yamlContent, REPLAY_KEY);

        if (list != null) {
            Map<String, Object> map = (Map<String, Object>) list.get(0);

            String siteTitle = fetchValue(map, "title");

            String filename = fetchValue(map, "file");

            boolean started = fetchBooleanValue(map, "auto_start", false);

            Integer waitTimeBetweenEventsMS = fetchIntValue(map, "pacingMS", 1000);

            Integer minEvents = fetchIntValue(map, "minevents", 1);

            Integer siteNumber = fetchIntValue(map, "site");

            // Site site = new Site(siteTitle, siteNumber);

            info.setDisplayName(siteTitle);
            info.setFilename(filename);
            info.setStarted(started);
            info.setWaitBetweenEventsMS(waitTimeBetweenEventsMS);
            info.setMinimumPlaybackRecords(minEvents);
            info.setSiteNumber(siteNumber);
        }

        return info;

    }

    @SuppressWarnings("unused")
    private boolean fetchBooleanValue(Map<String, Object> content, String key) {
        return fetchBooleanValue(content, key, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Site[] getSites(String[] yamlLines) {

        Map<String, Object> yamlContent = loadYaml(null, yamlLines);
        ArrayList<Map<String, Object>> list = fetchList(yamlContent, SITES_KEY);
        ArrayList<Site> sitesVector = new ArrayList<Site>();

        if (list != null) {
            for (Object object : list) {

                Map<String, Object> map = (Map<String, Object>) object;
                /*
                 * <pre> sites: - number: 1 name: Site 1 IP: localhost port: 50002 </pre>
                 */

                String siteTitle = fetchValue(map, "name");

                Integer siteNumber = fetchIntValue(map, "number");

                Site site = new Site(siteTitle, siteNumber);

                String hostName = fetchValueDefault(map, "IP", "");
                Integer portString = fetchIntValue(map, "port");

                String password = fetchValue(map, "password");
                if (password == null) {
                    password = "site" + siteNumber.toString();
                }

                site.setPassword(password.trim());

                Properties props = new Properties();
                props.put(Site.IP_KEY, hostName);
                props.put(Site.PORT_KEY, portString.toString());
                site.setConnectionInfo(props);

                sitesVector.add(site);

            }

        }

        return (Site[]) sitesVector.toArray(new Site[sitesVector.size()]);

    }

    private String fetchValueDefault(Map<String, Object> map, String key, String defaultValue) {
        String value = fetchValue(map, key);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    private Integer fetchIntValue(Map<String, Object> map, String key, int defaultValue) {
        Integer value = null;
        if (map != null) {
            value = (Integer) map.get(key);
        }
        if (value != null) {
            try {
                return value;
            } catch (Exception e) {
                syntaxError("Expecting number after " + key + ": field, found '" + value + "'");
            }
        }
        return defaultValue;
    }

    private Integer fetchIntValue(Map<String, Object> map, String key) {
        if (map == null) {
            // SOMEDAY figure out why map would every be null
            return null;
        }
        Integer value = (Integer) map.get(key);
        if (value != null) {
            try {
                return value;
            } catch (Exception e) {
                syntaxError("Expecting number after " + key + ": field, found '" + value + "'");
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> fetchMap(Map<String, Object> content, String key) {
        Object object = content.get(key);
        if (object != null) {
            if (object instanceof Map) {
                return (Map<String, Object>) content.get(key);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    @Override
    public void loadProblemInformationAndDataFiles(IInternalContest contest, String baseDirectoryName, Problem problem, boolean overrideUsePc2Validator) {
        loadProblemInformationAndDataFiles(contest, baseDirectoryName, problem, overrideUsePc2Validator, false);
    }

    @Override
    public void loadProblemInformationAndDataFiles(IInternalContest contest, String baseDirectoryName, Problem problem, boolean overrideUsePc2Validator, boolean overrideManualReview) {

        Group[] groups = contest.getGroups(); // fetch once instead of fetching for each problem (in loop)

        // TODO CCS code this: do not add problem to contest model, new new parameter flag

        String problemDirectory = baseDirectoryName + File.separator + problem.getShortName();

        problem.setExternalDataFileLocation(problemDirectory);

        String problemYamlFilename = problemDirectory + File.separator + DEFAULT_PROBLEM_YAML_FILENAME;

        Map<String, Object> content = loadYaml(problemYamlFilename);

        String problemLaTexFilename = problemDirectory + File.separator + "problem_statement" + File.separator + DEFAULT_PROBLEM_LATEX_FILENAME;

        String problemTitle = fetchValue(content, PROBLEM_NAME_KEY);

        if (new File(problemLaTexFilename).isFile()) {
            problemTitle = getProblemNameFromLaTex(problemLaTexFilename);
        }

        Map<String, Object> validatorContent = fetchMap(content, VALIDATOR_KEY);
        boolean usingCustomValidator = false;
        if (validatorContent != null) {
            usingCustomValidator = fetchBooleanValue(validatorContent, IContestLoader.USING_CUSTOM_VALIDATOR, false);
        }

        boolean pc2FormatProblemYamlFile = false;
        String usingValidator = fetchValue(validatorContent, IContestLoader.USING_PC2_VALIDATOR);

        if (usingValidator != null && usingValidator.equalsIgnoreCase("true")) {
            pc2FormatProblemYamlFile = true;
        }

        if (overrideUsePc2Validator) {
            pc2FormatProblemYamlFile = true;
        }

        if (problemTitle == null && (pc2FormatProblemYamlFile)) {
            problemTitle = fetchValue(content, "name");
        }

        if (problemTitle == null) {
            syntaxError("No problem name found for " + problem.getShortName() + " in " + problemLaTexFilename);
        }

        problem.setDisplayName(problemTitle);

        String dataFileBaseDirectory = problemDirectory + File.separator + "data" + File.separator + "secret";

        ProblemDataFiles problemDataFiles = new ProblemDataFiles(problem);

        if (pc2FormatProblemYamlFile) {
            String dataFileName = fetchValue(content, "datafile");
            String answerFileName = fetchValue(content, "answerfile");

            loadPc2ProblemFiles(contest, dataFileBaseDirectory, problem, problemDataFiles, dataFileName, answerFileName);
        } else {
            loadCCSProblemFiles(contest, dataFileBaseDirectory, problem, problemDataFiles);
        }

        //
        assignValidatorSettings(content, problem);

        Map<String, Object> limitsContent = fetchMap(content, LIMITS_KEY);
        Integer timeOut = fetchIntValue(limitsContent, TIMEOUT_KEY);
        if (timeOut != null) {
            problem.setTimeOutInSeconds(timeOut);
        }

        if (!usingCustomValidator) {
            if (!pc2FormatProblemYamlFile) {
                // TODO CCS add CCS validator derived based on build script

                /**
                 * - Use CCS build command to build validator ('build' script name) - add validator created by build command - add CCS run script ('run' script name)
                 */

                // default-validator: /home/pc2/Desktop/codequest12_problems/default_validator
                // override-validator: /usr/local/bin/mtsv {:problemletter} {:resfile} {:basename}

                // `$validate_cmd $inputfile $answerfile $feedbackfile < $teamoutput `;

                addClicsValidator(problem, problemDataFiles, baseDirectoryName);

            } else {
                addDefaultPC2Validator(problem, 1);
            }
        } else {
            // usingCustomValidor
            String validatorProg = fetchValue(validatorContent, "validatorProg");
            if (validatorProg != null) {
                Problem cleanProblem = contest.getProblem(problem.getElementId());
                ProblemDataFiles problemDataFile = contest.getProblemDataFile(problem);
                SerializedFile validatorFile = new SerializedFile(validatorProg);
                if (validatorFile.getSHA1sum() != null) {
                    problemDataFile.setOutputValidatorFile(validatorFile);
                    contest.updateProblem(cleanProblem, problemDataFile);
                } else {
                    // Halt loading and throw YamlLoadException
                    syntaxError("Error: problem " + problem.getLetter() + " - " + problem.getShortName() + " custom validator import failed: " + validatorFile.getErrorMessage());
                }
            }
        }

        boolean stopOnFirstFail = fetchBooleanValue(content, STOP_ON_FIRST_FAILED_TEST_CASE_KEY, false);
        problem.setStopOnFirstFailedTestCase(stopOnFirstFail);

        assignJudgingType(content, problem, overrideManualReview);

        boolean showOutputWindow = fetchBooleanValue(content, SHOW_OUTPUT_WINDOW, true);
        problem.setHideOutputWindow(!showOutputWindow);

        boolean showCompareWindow = fetchBooleanValue(content, SHOW_COMPARE_WINDOW, false);
        problem.setShowCompareWindow(showCompareWindow);

        boolean hideProblem = fetchBooleanValue(content, HIDE_PROBLEM, false);
        problem.setActive(!hideProblem);

        boolean showValidationResults = fetchBooleanValue(content, SHOW_VALIDATION_RESULTS, true);
        problem.setShowValidationToJudges(showValidationResults);

        @SuppressWarnings("unchecked")
        LinkedHashMap<String, Object> judgingTypeContent = (LinkedHashMap<String, Object>) content.get(JUDGING_TYPE_KEY);
        if (judgingTypeContent != null) {
            assignJudgingType(judgingTypeContent, problem, overrideManualReview);
        }

        // override CCS standard read input data from stdin
        Map<String, Object> problemInputContent = fetchMap(content, PROBLEM_INPUT_KEY);
        if (problemInputContent != null) {
            boolean readFromSTDIN = fetchBooleanValue(problemInputContent, READ_FROM_STDIN_KEY, true);
            problem.setReadInputDataFromSTDIN(readFromSTDIN);
        }

        String groupListString = fetchValue(content, GROUPS_KEY);
        if (groupListString != null) {

            if (groupListString.trim().length() == 0) {
                syntaxError("Empty group list");
            }

            String[] fields = groupListString.split(";");
            if (groupListString.indexOf(';') == -1) {
                fields = groupListString.split(",");
            }

            for (String groupInfo : fields) {
                groupInfo = groupInfo.trim();
                Group group = lookupGroupInfo(groups, groupInfo);
                if (group == null) {
                    if (groups == null || groups.length == 0) {
                        syntaxError("ERROR No groups defined. (groups.tsv not loaded?), error when trying to find group for '" + groupInfo + "' from yaml value '" + groupListString + "' ");
                    } else {
                        syntaxError("Undefined group '" + groupInfo + "' for group list '" + groupListString + "' ");
                    }
                }

                problem.addGroup(group);
            }
        }

        // TODO CCS - send preliminary - add bug - fix.
        // boolean sendPreliminary = fetchBooleanValue(content, SEND_PRELIMINARY_JUDGEMENT_KEY, false);
        // if (sendPreliminary){
        // problem.setPrelimaryNotification(true);
        // }

    }

    /**
     * Assign validator config settings.
     * 
     * @param content
     * @param problem
     * @param contest
     */
    protected void assignValidatorSettings(Map<String, Object> content, Problem problem) {

        // PC2 CLICS validator section
        // validator:
        // validatorProg: pc2.jar edu.csus.ecs.pc2.validator.Validator
        // validatorCmd: "{:validator} {:infile} {:outfile} {:ansfile} {:resfile} -pc2 1 true"
        // usingInternal: true
        // validatorOption: 1

        Object object = content.get(VALIDATOR_KEY);

        if (object instanceof LinkedHashMap) {
            // Handle validator yaml section

            @SuppressWarnings("unchecked")
            LinkedHashMap<String, Object> map = (LinkedHashMap<String, Object>) object; // fetchList(content, VALIDATOR_KEY);

            boolean customType = fetchBooleanValue(map, IContestLoader.USING_CUSTOM_VALIDATOR, false);
            // boolean pc2Type = fetchBooleanValue(map, IContestLoader.USING_PC2_VALIDATOR, false);
            String validatorProg = fetchValue(map, "validatorProg");

            String validatorCmd = fetchValue(map, "validatorCmd");
            // junit does not expect NONE to be set....
            // if (pc2Type) {
            problem.setValidatorType(VALIDATOR_TYPE.PC2VALIDATOR);
            problem.setOutputValidatorProgramName(validatorProg);
            String validatorOption = fetchValue(map, "validatorOption");

            PC2ValidatorSettings settings = new PC2ValidatorSettings();
            if (validatorOption != null) {
                settings.setWhichPC2Validator(Integer.parseInt(validatorOption));
            }

            settings.setIgnoreCaseOnValidation(true);

            if (validatorCmd != null) {
                settings.setValidatorCommandLine(validatorCmd);
            }
            problem.setPC2ValidatorSettings(settings);
            // }
            if (customType) {
                problem.setValidatorType(VALIDATOR_TYPE.CUSTOMVALIDATOR);
                problem.setOutputValidatorProgramName(validatorProg);
                CustomValidatorSettings customSettings = new CustomValidatorSettings();
                boolean clicsMode = fetchBooleanValue(map, IContestLoader.USE_CLICS_CUSTOM_VALIDATOR_INTERFACE, true);
                if (clicsMode) {
                    customSettings.setUseClicsValidatorInterface();
                } else {
                    customSettings.setUsePC2ValidatorInterface();
                }
                customSettings.setValidatorCommandLine(validatorCmd);
                customSettings.setValidatorProgramName(validatorProg);
                problem.setCustomValidatorSettings(customSettings);
            }
            // String usingInternal = fetchValue(map, "usingInternal");

            return; // =================== RETURN
        }

        // PC2 CLICS validator flags
        // validator_flags: options are: [case_sensitive] [space_change_sensitive] [float_absolute_tolerance FLOAT] [float_tolerance FLOAT]
        // ex. validator_flags: float_tolerance 1e-6

        String validatorFlags = fetchValue(content, IContestLoader.VALIDATOR_FLAGS_KEY);
        if (validatorFlags != null && validatorFlags.trim().length() > 0) {

            try {
                ClicsValidatorSettings settings = new ClicsValidatorSettings(validatorFlags);
                problem.setValidatorType(VALIDATOR_TYPE.CLICSVALIDATOR);
                problem.setCLICSValidatorSettings(settings);
            } catch (RuntimeException e) {
                throw new YamlLoadException("For problem " + problem.getShortName() + ", invalid validator flags '" + validatorFlags + "' " + e.getMessage(), e.getCause());
            }

        }

        // CLICS validator
        // validator: options are: [case_sensitive] [space_change_sensitive] [float_absolute_tolerance FLOAT] [float_tolerance FLOAT]
        // ex. validator: float_tolerance 1e-6

        String validatorParameters = fetchValue(content, IContestLoader.VALIDATOR_KEY);
        if (validatorParameters != null && validatorParameters.trim().length() > 0) {
            try {
                ClicsValidatorSettings settings = new ClicsValidatorSettings(validatorParameters);
                problem.setValidatorType(VALIDATOR_TYPE.CLICSVALIDATOR);
                problem.setCLICSValidatorSettings(settings);
            } catch (RuntimeException e) {
                throw new YamlLoadException("For problem " + problem.getShortName() + ", invalid validator flags '" + validatorParameters + "' " + e.getMessage(), e.getCause());
            }
        }

    }

    @Override
    public void dumpSerialzedFileList(Problem problem, String logPrefixId, SerializedFile[] sfList) {
        // TODO Auto-generated method stub

    }

    @Override
    public Problem addDefaultPC2Validator(Problem problem, int optionNumber) {

        problem.setValidatorType(VALIDATOR_TYPE.PC2VALIDATOR);

        PC2ValidatorSettings settings = new PC2ValidatorSettings();
        settings.setWhichPC2Validator(optionNumber);
        settings.setIgnoreCaseOnValidation(true);
        settings.setValidatorCommandLine(Constants.DEFAULT_PC2_VALIDATOR_COMMAND + " -pc2 " + settings.getWhichPC2Validator() + " " + settings.isIgnoreCaseOnValidation());
        settings.setValidatorProgramName(Constants.PC2_VALIDATOR_NAME);

        problem.setPC2ValidatorSettings(settings);
        return problem;
    }

    @Override
    public String[] loadGeneralClarificationAnswers(String[] yamlLines) {
        return fetchStringList(yamlLines, CLAR_CATEGORIES_KEY);
    }

    @SuppressWarnings({ "rawtypes", "unchecked" })
    public String[] fetchStringList(String[] yamlLines, String key) {
        Map<String, Object> content = loadYaml(null, yamlLines);
        ArrayList list = fetchList(content, key);
        if (list == null) {
            return new String[0];
        } else {
            return (String[]) list.toArray(new String[list.size()]);
        }
    }

    @Override
    public String[] getGeneralAnswers(String[] yamlLines) {
        return fetchStringList(yamlLines, DEFAULT_CLARS_KEY);
    }

    @Override
    public String[] getClarificationCategories(String[] yamlLines) {
        return fetchStringList(yamlLines, CLAR_CATEGORIES_KEY);
    }

    @SuppressWarnings("rawtypes")
    private ArrayList fetchList(Map<String, Object> content, String key) {
        return (ArrayList) content.get(key);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Language[] getLanguages(String[] yamlLines) {
        ArrayList<Language> languageList = new ArrayList<Language>();

        // System.out.println(Utilities.join("\n", yamlLines));

        Map<String, Object> yamlContent = loadYaml(null, yamlLines);
        ArrayList<Map<String, Object>> list = fetchList(yamlContent, LANGUAGE_KEY);

        if (list != null) {
            for (Object object : list) {

                Map<String, Object> map = (Map<String, Object>) object;

                String name = fetchValue(map, "name");

                if (name == null) {
                    syntaxError("Language name field missing in languages section");
                } else {
                    Language language = new Language(name);

                    Language lookedupLanguage = LanguageAutoFill.languageLookup(name);
                    String compilerName = fetchValue(map, "compilerCmd");

                    if (compilerName == null && lookedupLanguage != null) {
                        language = lookedupLanguage;
                        compilerName = language.getCompileCommandLine();
                        language.setDisplayName(name);
                    } else {

                        if (compilerName == null) {
                            compilerName = fetchValue(map, "compiler");
                        }
                        String compilerArgs = fetchValue(map, "compiler-args");
                        String interpreter = fetchValue(map, "runner");
                        String interpreterArgs = fetchValue(map, "runner-args");
                        String exeMask = fetchValue(map, "exemask");
                        // runner + runner-args, so what is execCmd for ?
                        // String execCmd = getSequenceValue(sequenceLines, "execCmd");

                        if (compilerArgs == null) {
                            language.setCompileCommandLine(compilerName);
                        } else {
                            language.setCompileCommandLine(compilerName + " " + compilerArgs);
                        }
                        language.setExecutableIdentifierMask(exeMask);

                        String programExecuteCommandLine = null;
                        if (interpreter == null) {
                            programExecuteCommandLine = "a.out";
                        } else {
                            if (interpreterArgs == null) {
                                programExecuteCommandLine = interpreter;
                            } else {
                                programExecuteCommandLine = interpreter + " " + interpreterArgs;
                            }
                        }
                        language.setProgramExecuteCommandLine(programExecuteCommandLine);
                    }

                    if (compilerName == null) {
                        throw new YamlLoadException("Language \"" + name + "\" missing compiler command line");
                    }

                    boolean active = fetchBooleanValue(map, "active", true);
                    language.setActive(active);

                    boolean useJudgeCommand = fetchBooleanValue(map, USE_JUDGE_COMMAND_KEY, false);
                    language.setUsingJudgeProgramExecuteCommandLine(useJudgeCommand);

                    boolean isInterpreted = fetchBooleanValue(map, INTERPRETED_LANGUAGE_KEY, language.isInterpreted());
                    language.setInterpreted(isInterpreted);

                    String judgeExecuteCommandLine = fetchValue(map, JUDGE_EXECUTE_COMMAND_KEY);
                    if (judgeExecuteCommandLine != null) {
                        language.setJudgeProgramExecuteCommandLine(judgeExecuteCommandLine);
                    } else {
                        language.setUsingJudgeProgramExecuteCommandLine(false);
                    }

                    // TODO handle interpreted languages, seems it should be in the export

                    // boolean

                    if (valid(language, name)) {
                        languageList.add(language);
                    }

                }
            }

        }

        return (Language[]) languageList.toArray(new Language[languageList.size()]);
    }

    @SuppressWarnings("unchecked")
    @Override
    public Problem[] getProblems(String[] yamlLines, int seconds, boolean loadDataFileContents, String defaultValidatorCommand, String overrideValidatorCommandLine, boolean overrideUsePc2Validator,
            boolean manualReviewOverride) {

        Vector<Problem> problemList = new Vector<Problem>();

        Map<String, Object> yamlContent = loadYaml(null, yamlLines);
        // use problemset yaml key
        ArrayList<Map<String, Object>> list = fetchList(yamlContent, PROBLEMS_KEY);

        if (list == null) {
            // use problems yaml key
            list = fetchList(yamlContent, PROBLEMSET_PROBLEMS_KEY);
        }

        if (list != null) {

            for (Object object : list) {

                Map<String, Object> map = (Map<String, Object>) object;

                String problemKeyName = fetchValue(map, SHORT_NAME_KEY);

                if (problemKeyName == null) {
                    syntaxError("Missing " + SHORT_NAME_KEY + " in probset section");
                }

                /**
                 * <pre>
                 *  problemset:
                 *    - letter:     A
                 *      short-name: apl
                 *      color:      yellow
                 *      rgb:        #ffff00
                 * # optional title
                 *      title:      APL Rules!
                 * </pre>
                 */

                String problemTitle = fetchValue(map, PROBLEM_NAME_KEY);
                if (problemTitle == null) {
                    problemTitle = problemKeyName;
                }

                Problem problem = new Problem(problemTitle);

                problem.setComputerJudged(true);

                int actSeconds = fetchIntValue(map, TIMEOUT_KEY, seconds);
                problem.setTimeOutInSeconds(actSeconds);

                problem.setShowCompareWindow(false);

                problem.setShortName(problemKeyName);
                if (!problem.isValidShortName()) {
                    throw new YamlLoadException("Invalid short problem name '" + problemKeyName + "'");
                }

                String problemLetter = fetchValue(map, "letter");
                String colorName = fetchValue(map, "color");
                String colorRGB = fetchValue(map, "rgb");

                // TODO CCS assign Problem variables for color and letter
                problem.setLetter(problemLetter);
                problem.setColorName(colorName);
                problem.setColorRGB(colorRGB);

                /**
                 * Assign each proble default values from contest.yaml level
                 */
                assignJudgingType(yamlContent, problem, manualReviewOverride);

                LinkedHashMap<String, Object> judgingTypeContent = (LinkedHashMap<String, Object>) yamlContent.get(JUDGING_TYPE_KEY);
                if (judgingTypeContent != null) {
                    assignJudgingType(judgingTypeContent, problem, manualReviewOverride);
                }

                assignJudgingType(map, problem, manualReviewOverride);

                boolean loadFilesFlag = fetchBooleanValue(map, PROBLEM_LOAD_DATA_FILES_KEY, loadDataFileContents);
                problem.setUsingExternalDataFiles(!loadFilesFlag);

                String validatorCommandLine = fetchValue(map, VALIDATOR_KEY);

                if (validatorCommandLine == null) {
                    validatorCommandLine = defaultValidatorCommand;
                }
                if (overrideValidatorCommandLine != null) {
                    validatorCommandLine = overrideValidatorCommandLine;
                }
                problem.setValidatorType(VALIDATOR_TYPE.PC2VALIDATOR);
                problem.setValidatorCommandLine(validatorCommandLine);

                String inputValidatorCommandLine = fetchValue(map, INPUT_VALIDATOR_COMMAND_LINE_KEY);
                if (inputValidatorCommandLine != null) {
                    problem.setInputValidatorCommandLine(inputValidatorCommandLine);
                }

                problemList.addElement(problem);
            }
        }

        return (Problem[]) problemList.toArray(new Problem[problemList.size()]);
    }

    public String getInputValidatorDir(String baseDirectoryName, Problem problem) {

        // from https://clics.ecs.baylor.edu/index.php/Problem_format
        // squares/input_format_validators/squares_input_checker1.py
        // squares/input_format_validators/squares_input_checker2/check.c
        // squares/input_format_validators/squares_input_checker2/data.h

        return baseDirectoryName + File.separator + problem.getShortName() + File.separator + "input_format_validators";
    }

    /**
     * Return list of judge account numbers for list, in ascending order.
     * 
     * @param accounts
     * @return array of judge numbers, in ascending order.
     */
    private int[] getJudgeAccountNumbers(Account[] accounts) {
        int count = 0;
        for (Account account : accounts) {
            if (account.getClientId().getClientType().equals(ClientType.Type.JUDGE)) {
                count++;
            }
        }

        int[] out = new int[count];
        count = 0;
        for (Account account : accounts) {
            if (account.getClientId().getClientType().equals(ClientType.Type.JUDGE)) {
                out[count] = account.getClientId().getClientNumber();
                count++;
            }
        }

        Arrays.sort(out);
        return out;

    }

    protected int toInt(String string, int defaultNumber) {

        try {

            if (string != null && string.length() > 0) {
                return Integer.parseInt(string.trim());
            }
        } catch (Exception e) {
            // ignore, will return default if a parsing error
        }

        return defaultNumber;

    }

    protected int getIntegerValue(String string, int defaultNumber) {

        int number = defaultNumber;

        if (string != null && string.length() > 0) {
            number = Integer.parseInt(string.trim());
        }

        return number;
    }

    protected int[] getNumberList(String numberString) {

        String[] list = numberString.split(",");
        if (list.length == 1) {
            int[] out = new int[1];
            out[0] = getIntegerValue(list[0], 0);
            // if (out[0] < 1) {
            // // TODO 669 throw invalid number in list exception
            // }
            return out;
        } else {
            int[] out = new int[list.length];
            int i = 0;
            for (String n : list) {
                out[i] = getIntegerValue(n, 0);
                // if (out[i] < 1) {
                // // TODO 669 throw invalid number in list exception
                // }
                i++;
            }
            return out;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public AutoJudgeSetting[] getAutoJudgeSettings(String[] yamlLines, Problem[] problems) {

        ArrayList<AutoJudgeSetting> ajList = new ArrayList<AutoJudgeSetting>();

        Map<String, Object> yamlContent = loadYaml(null, yamlLines);
        ArrayList<Map<String, Object>> list = fetchList(yamlContent, AUTO_JUDGE_KEY);

        if (list != null) {

            // TODO get accounts from contest too
            Account[] accounts = getAccounts(yamlLines);

            for (Object object : list) {

                Map<String, Object> map = (Map<String, Object>) object;

                String accountType = fetchValue(map, "account");
                ClientType.Type type = ClientType.Type.valueOf(accountType.trim());

                int siteNumber = fetchIntValue(map, "site", 1);

                // TODO 669 check for syntax errors
                // syntaxError(AUTO_JUDGE_KEY + " name field missing in languages section");

                String numberString = fetchValue(map, "number");
                String problemLettersString = fetchValue(map, "letters");

                boolean active = fetchBooleanValue(map, "active", true);

                int[] judgeClientNumbers = null;
                if ("all".equalsIgnoreCase(numberString)) {
                    if (accounts.length == 0) {

                        throw new YamlLoadException("'all' not allowed, no judge accounts defined in YAML");
                    }
                    judgeClientNumbers = getJudgeAccountNumbers(accounts);

                } else {
                    judgeClientNumbers = getNumberList(numberString.trim());
                }

                for (int i = 0; i < judgeClientNumbers.length; i++) {

                    int clientNumber = judgeClientNumbers[i];

                    String name = accountType.toUpperCase() + clientNumber;

                    AutoJudgeSetting autoJudgeSetting = new AutoJudgeSetting(name);
                    ClientId id = new ClientId(siteNumber, type, clientNumber);
                    autoJudgeSetting.setClientId(id);
                    autoJudgeSetting.setActive(active);

                    Filter filter = new Filter();

                    if ("all".equalsIgnoreCase(problemLettersString.trim())) {
                        for (Problem problem : problems) {
                            filter.addProblem(problem);
                        }
                    } else {
                        for (Problem problem : getProblemsFromLetters(problems, problemLettersString)) {
                            filter.addProblem(problem);
                        }
                    }

                    autoJudgeSetting.setProblemFilter(filter);
                    ajList.add(autoJudgeSetting);
                }

            }
        }

        return (AutoJudgeSetting[]) ajList.toArray(new AutoJudgeSetting[ajList.size()]);

    }

    @Override
    public Problem[] getProblems(String[] contents, int defaultTimeOut, boolean loadDataFileContents, String defaultValidatorCommandLine) {
        return getProblems(contents, defaultTimeOut, loadDataFileContents, defaultValidatorCommandLine, null, false, false);
    }

    @Override
    public IInternalContest fromYaml(IInternalContest contest, String[] yamlLines, String directoryName) {
        return fromYaml(contest, yamlLines, directoryName, false);
    }

    @Override
    public Problem[] getProblems(String[] contents, int defaultTimeOut) {
        return getProblems(contents, defaultTimeOut, true, null, null, false, false);
    }

    @Override
    public String[] getFileNames(String directoryName, String extension) {

        ArrayList<String> list = new ArrayList<String>();
        File dir = new File(directoryName);

        String[] entries = dir.list();
        if (entries == null) {
            return new String[0];
        }
        Arrays.sort(entries);

        for (String name : entries) {
            if (name.endsWith(extension)) {
                list.add(name);
            }
        }

        return (String[]) list.toArray(new String[list.size()]);
    }

    @Override
    public Problem[] getProblemsFromLetters(Problem[] problems, String problemLettersString) {

        String[] list = problemLettersString.split(",");
        Problem[] out = new Problem[list.length];

        for (int i = 0; i < list.length; i++) {

            char letter = list[i].trim().toUpperCase().charAt(0);
            int offset = letter - 'A';

            if (offset < 0 || offset >= problems.length) {
                throw new YamlLoadException("getProblemsFromLetters: There is no problem definition # " + (offset + 1) + " only " + offset + " problems defined?");
            }
            out[i] = problems[offset];
        }
        return out;
    }

    @Override
    public String getProblemNameFromLaTex(String filename) {

        String[] lines;
        try {
            lines = loadFileWithIncludes(null, filename);
        } catch (IOException e) {
            return null;
        }

        String name = null;

        String titlePattern = "\\problemtitle{";

        String titlePattern2 = "\\problemname{";

        String commentPattern = "%% plainproblemtitle:";

        for (String line : lines) {
            // Now create matcher object.

            if (line.indexOf("problemtitle") != -1) {

                // %% plainproblemtitle: Problem Name
                if (line.trim().startsWith(commentPattern)) {
                    name = line.trim().substring(commentPattern.length()).trim();
                    break;
                }

                // \problemtitle{Problem Name}

                if (line.trim().startsWith(titlePattern)) {
                    name = line.trim().substring(titlePattern.length()).trim();
                    // name = name.replace(Pattern.quote(")"), "");
                    // name = name.replace(")", "");
                    name = name.substring(0, name.length() - 1);
                    break;
                }

            }

            if (line.indexOf("problemname") != -1) {

                // \problemname{Problem Name}

                if (line.trim().startsWith(titlePattern2)) {
                    name = line.trim().substring(titlePattern2.length()).trim();
                    // name = name.replace(Pattern.quote(")"), "");
                    // name = name.replace(")", "");
                    name = name.substring(0, name.length() - 1);
                    break;
                }
            }
        }
        return name;

    }

    /**
     * Remove trailing and leading quote char
     * 
     * @param string
     * @param quoteChar
     * @return
     */
    @Override
    public String unquote(String string, String quoteChar) {
        if (string.startsWith(quoteChar)) {
            String newString = string.substring(1);
            if (newString.endsWith(quoteChar)) {
                newString = newString.substring(0, newString.length() - 1);
            }
            return newString;
        }
        return string;
    }

    /**
     * Unquotes either ' or ".
     * 
     * If the first character is a ' then will strip leading/trailing ' <br>
     * or If the first character is a " then will strip leading/trailing ". <br>
     * 
     * @param string
     * @return
     */
    protected String unquoteAll(String string) {
        if (string.startsWith("'")) {
            return unquote(string, "'");

        } else if (string.startsWith("\"")) {
            return unquote(string, "\"");

        } else {
            return string;
        }
    }

    private void syntaxError(String string) {
        YamlLoadException exception = new YamlLoadException("Syntax error: " + string);
        exception.printStackTrace();
        throw exception;
    }

    private Problem addClicsValidator(Problem problem, ProblemDataFiles problemDataFiles, String baseDirectoryName) {

        problem.setValidatorType(VALIDATOR_TYPE.CLICSVALIDATOR);

        problem.setReadInputDataFromSTDIN(true);

        // problem.setValidatorProgramName(Constants.CLICS_VALIDATOR_NAME);

        // if we use the internal Java CCS validator use this.
        // problem.setValidatorCommandLine("java -cp {:pc2jarpath} " + CCSConstants.DEFAULT_CCS_VALIDATOR_COMMAND);
        if (problem.getValidatorCommandLine() == null) {
            problem.setValidatorCommandLine(Constants.DEFAULT_CLICS_VALIDATOR_COMMAND);
        }

        String validatorName = baseDirectoryName + File.separator + problem.getValidatorProgramName();

        try {
            /**
             * If file is there load it
             */
            if (new File(validatorName).isFile()) {
                problemDataFiles.setInputValidatorFile(new SerializedFile(validatorName));
            }
        } catch (Exception e) {
            throw new YamlLoadException("Unable to load validator for problem " + problem.getShortName() + ": " + validatorName, e);
        }

        String inpuFormattValidatorName = findInputValidator(baseDirectoryName, problem);

        try {
            /**
             * If file is there load it
             */
            if (inpuFormattValidatorName != null && new File(inpuFormattValidatorName).isFile()) {
                SerializedFile serializedFile = new SerializedFile(inpuFormattValidatorName);

                problemDataFiles.setInputValidatorFile(serializedFile);

                String basename = serializedFile.getName();
                problem.setInputValidatorCommandLine(basename);
                if (basename.toLowerCase().endsWith(".bat") || basename.toLowerCase().endsWith(".cmd")) {
                    problem.setInputValidatorCommandLine("cmd /c " + basename);
                }
                if (basename.toLowerCase().endsWith(".class")) {
                    basename = basename.replaceFirst(".class", "");
                    problem.setInputValidatorCommandLine("java " + basename);
                }

                // set validator name to short filename
                problem.setInputValidatorProgramName(serializedFile.getName());
                problem.setProblemHasInputValidator(true);
            }
        } catch (Exception e) {
            throw new YamlLoadException("Unable to load input format validator for problem " + problem.getShortName() + ": " + inpuFormattValidatorName, e);
        }

        return problem;
    }

    /**
     * Get file directory entries with relative dir path
     * 
     * @param directory
     *            - directory to search and to prepend onto the matching filenames
     * @return
     */
    // SOMEDAY move this to Utilities class

    public static String[] getFileEntries(String directory) {
        ArrayList<String> list = new ArrayList<>();
        File[] files = new File(directory).listFiles();

        for (File file : files) {
            if (file.isFile()) {
                list.add(directory + File.separator + file.getName());
            }
        }
        return (String[]) list.toArray(new String[list.size()]);
    }

    protected String findInputValidator(String baseDirectoryName, Problem problem) {

        String inputValidatorDir = getInputValidatorDir(baseDirectoryName, problem);

        String inputValidatorFilename = null;

        if (new File(inputValidatorDir).isDirectory()) {
            // there is a input format validator dir

            // search for validator
            String[] filenames = getFileEntries(inputValidatorDir);

            if (filenames.length == 1) {
                // only found one file
                inputValidatorFilename = filenames[0];
            } else if (filenames.length > 1) {

                ArrayList<String> validatorList = new ArrayList<String>();

                // Loop through files looking for potential validator programs

                for (String name : filenames) {
                    if (name.toLowerCase().endsWith("build")) {
                        // Cannot be build
                        continue;
                    } else if (name.toLowerCase().endsWith("readme")) {
                        // Cannot be README
                        continue;
                    } else {

                        if (Utilities.isExecutableExtension(name)) {
                            validatorList.add(name);
                        }
                    }
                }

                if (validatorList.size() == 1) {
                    inputValidatorFilename = validatorList.get(0);

                } else if (validatorList.size() > 1) {
                    inputValidatorFilename = validatorList.get(0);

                    for (String string : validatorList) {
                        System.out.println("Warning dup input format validator " + Utilities.getCurrentDirectory() + File.separator + string);
                    }
                    System.out.println("Using: " + inputValidatorFilename);
                    // YamlLoadException yex = new YamlLoadException("Too many input format validators found for " + problem.getShortName() + " found " + validatorList.size());
                    // throw yex;
                }
            }
        }

        return inputValidatorFilename;
    }

    @Override
    public void loadPc2ProblemFiles(IInternalContest contest, String dataFileBaseDirectory, Problem problem, ProblemDataFiles problemDataFiles2, String dataFileName, String answerFileName) {

        ProblemDataFiles problemDataFiles = new ProblemDataFiles(problem);

        if (dataFileName == null) {
            syntaxError("Missing datafile for pc2 problem " + problem.getShortName());
        }

        if (answerFileName == null) {
            syntaxError("Missing answerfile for pc2 problem " + problem.getShortName());
        }

        addDataFiles(problem, problemDataFiles, dataFileBaseDirectory, dataFileName, answerFileName);
        contest.addProblem(problem, problemDataFiles);
    }

    @Override
    public String getCCSDataFileDirectory(String yamlDirectory, Problem problem) {
        return getCCSDataFileDirectory(yamlDirectory, problem.getShortName());
    }

    @Override
    public String getCCSDataFileDirectory(String yamlDirectory, String shortDirName) {
        return yamlDirectory + File.separator + shortDirName + File.separator + "data" + File.separator + "secret";
    }

    @Override
    public Problem loadCCSProblemFiles(IInternalContest contest, String dataFileBaseDirectory, Problem problem, ProblemDataFiles problemDataFiles) {

        if (problem.getShortName() == null) {
            throw new YamlLoadException("  For " + problem + " missing problem short name");
        }

        /**
         * Data files are external so data files should not be loaded into problem data files.
         */
        boolean loadExternalFile = problem.isUsingExternalDataFiles();

        String[] inputFileNames = getFileNames(dataFileBaseDirectory, ".in");

        String[] answerFileNames = getFileNames(dataFileBaseDirectory, ".ans");

        if (inputFileNames.length == 0) {
            throw new YamlLoadException("No input (.in) file names found for " + problem.getDisplayName() + " in dir " + dataFileBaseDirectory);
        }

        if (answerFileNames.length == 0) {
            throw new YamlLoadException("No answer (.ans) file names found for " + problem.getDisplayName() + " in dir " + dataFileBaseDirectory);
        }

        if (inputFileNames.length == answerFileNames.length) {

            Arrays.sort(inputFileNames);

            ArrayList<SerializedFile> dataFiles = new ArrayList<SerializedFile>();
            ArrayList<SerializedFile> answerFiles = new ArrayList<SerializedFile>();

            for (int idx = 0; idx < inputFileNames.length; idx++) {

                problem.addTestCaseFilenames(inputFileNames[idx], answerFileNames[idx]);

                String dataFileName = dataFileBaseDirectory + File.separator + inputFileNames[idx];
                String answerFileName = dataFileName.replaceAll(".in$", ".ans");

                if (idx == 0) {
                    problem.setDataFileName(Utilities.basename(dataFileName));
                    problem.setAnswerFileName(Utilities.basename(answerFileName));
                }

                String answerShortFileName = inputFileNames[idx].replaceAll(".in$", ".ans");

                checkForFile(dataFileName, "Missing " + inputFileNames[idx] + " file for " + problem.getShortName() + " in " + dataFileBaseDirectory);
                checkForFile(answerFileName, "Missing " + answerShortFileName + " file for " + problem.getShortName() + " in " + dataFileBaseDirectory);

                dataFiles.add(new SerializedFile(dataFileName, loadExternalFile));
                answerFiles.add(new SerializedFile(answerFileName, loadExternalFile));

            }

            if (dataFiles.size() > 0) {

                SerializedFile[] data = (SerializedFile[]) dataFiles.toArray(new SerializedFile[dataFiles.size()]);
                SerializedFile[] answer = (SerializedFile[]) answerFiles.toArray(new SerializedFile[answerFiles.size()]);

                // dumpSerialzedFileList (problem, "Judges data", data);
                // dumpSerialzedFileList (problem, "Judges answer", answer);

                problemDataFiles.setJudgesDataFiles(data);
                problemDataFiles.setJudgesAnswerFiles(answer);

                problem.setDataFileName(inputFileNames[0]);
                problem.setAnswerFileName(inputFileNames[0].replaceAll(".in$", ".ans"));
            } else {
                syntaxWarning("There were no data files found/loaded for " + problem.getShortName());
            }

            problem.setReadInputDataFromSTDIN(true);

        } else {
            throw new YamlLoadException("  For " + problem.getShortName() + " Missing files -  there are " + inputFileNames.length + " .in files and " + //
                    answerFileNames.length + " .ans files " + " in " + dataFileBaseDirectory);
        }

        if (inputFileNames.length == 0) {
            throw new YamlLoadException("  For " + problem.getShortName() + " Missing files -  there are " + inputFileNames.length + " .in files and " + //
                    answerFileNames.length + " .ans files " + " in " + dataFileBaseDirectory);
        }

        contest.addProblem(problem, problemDataFiles);

        validateCCSData(contest, problem);

        return problem;
    }

    private void validateCCSData(IInternalContest contest, Problem problem) {

        // TODO CCS Load Validator

        // TODO CCS somehow find validator, compile, and test.
        // the somehow is because there may be more than one validator in the validator directory

        // TODO 1. Check files (all files present as required + check problem.yaml)
        // TODO 2. Check compile (check that all programs compile)
        // TODO 3. Check input (run input validators)
        // TODO 4. Check solutions (run all solutions check that they get the expected verdicts)

    }

    private void addDataFiles(Problem problem, ProblemDataFiles problemDataFiles, String dataFileBaseDirectory, String dataFileName, String answerFileName) {

        // load judge data file
        if (dataFileName != null) {
            String dataFilePath = dataFileBaseDirectory + File.separator + dataFileName;
            if (fileNotThere(dataFilePath)) {
                throw new YamlLoadException("Missing data file " + dataFilePath);
            }

            problem.setDataFileName(dataFileName);
            problem.setReadInputDataFromSTDIN(false);

            SerializedFile serializedFile = new SerializedFile(dataFilePath, problem.isUsingExternalDataFiles());
            problemDataFiles.setJudgesDataFile(serializedFile);
        }

        // load judge answer file
        if (answerFileName != null) {
            String answerFilePath = dataFileBaseDirectory + File.separator + answerFileName;
            if (fileNotThere(answerFilePath)) {
                throw new YamlLoadException("Missing data file " + answerFilePath);
            }

            problem.setAnswerFileName(answerFileName);

            SerializedFile serializedFile = new SerializedFile(answerFilePath, problem.isUsingExternalDataFiles());
            problemDataFiles.setJudgesAnswerFile(serializedFile);
        }

    }

    private boolean fileNotThere(String name) {
        return !new File(name).isFile();
    }

    private void syntaxWarning(String message) {
        System.err.println("Warning - " + message);
    }

    /**
     * Check for existence of file, if does not exist throw exception with message.
     * 
     * @param filename
     * @param message
     * 
     */
    private void checkForFile(String filename, String message) {

        if (!(new File(filename).isFile())) {
            throw new YamlLoadException(message);
        }
    }

    /**
     * Load problem data files.
     * 
     * @param loadProblemDataFiles
     *            false means ignore problem data files
     */
    public void setLoadProblemDataFiles(boolean loadProblemDataFiles) {
        this.loadProblemDataFiles = loadProblemDataFiles;
    }

    public boolean isLoadProblemDataFiles() {
        return loadProblemDataFiles;
    }

    private boolean valid(Language language, String prefix) {
        checkField(language.getDisplayName(), prefix + " Compiler Display name");
        checkField(language.getCompileCommandLine(), prefix + " Compile Command line");
        return true;
    }

    private void checkField(String value, String fieldName) {
        if (value == null) {
            syntaxError("Missing " + fieldName);
        } else if (value.trim().length() == 0) {
            syntaxError("Missing " + fieldName);
        }
    }

    public void assignJudgingType(String[] yaml, Problem problem, boolean overrideManualReviewFlag) {
        Map<String, Object> map = loadYaml(null, yaml);
        assignJudgingType(map, problem, overrideManualReviewFlag);
    }

    /**
     * Assign individual problem judging type based on map values.
     */
    protected void assignJudgingType(Map<String, Object> map, Problem problem, boolean overrideManualReviewFlag) {

        // if (map == null || map.entrySet().isEmpty()){
        // System.out.println("debug problem "+problem.getShortName()+" has NO map");
        // } else {
        // System.out.println("debug problem "+problem.getShortName()+" "+map);
        // }

        if (isValuePresent(map, SEND_PRELIMINARY_JUDGEMENT_KEY)) {
            boolean sendPreliminary = fetchBooleanValue(map, SEND_PRELIMINARY_JUDGEMENT_KEY, false);
            problem.setPrelimaryNotification(sendPreliminary);
        }

        if (isValuePresent(map, COMPUTER_JUDGING_KEY)) {
            boolean computerJudged = fetchBooleanValue(map, COMPUTER_JUDGING_KEY, false);
            problem.setComputerJudged(computerJudged);
        }

        boolean manualReview = problem.isManualReview();

        if (isValuePresent(map, MANUAL_REVIEW_KEY)) {
            manualReview = fetchBooleanValue(map, MANUAL_REVIEW_KEY, false);
        }

        if (overrideManualReviewFlag) {
            manualReview = true;
        }

        if (manualReview) {
            problem.setManualReview(true);
        }
    }

    protected void printKeyFound(String message, Map<String, Object> map, String key) {
        Object object = map.get(key);
        String value = "MISSING";
        if (object != null) {
            value = object.toString();
        }
        System.out.println(message + " " + key + " = " + value);
    }

    protected String toStringTwo(Problem problem) {

        return "Problem " + problem.getShortName() + "  cj/man/prelim = " + problem.isComputerJudged() + //
                " / " + problem.isManualReview() + //
                " / " + problem.isPrelimaryNotification();
    }

    /**
     * Convert String to second. Expects input in form: ss or mm:ss or hh:mm:ss
     * 
     * @param s
     *            string to be converted to seconds
     * @return -1 if invalid time string, 0 or greater if valid
     */
    public long stringToLongSecs(String s) {

        // TODO TODAY CLEANUP - make Utilties.stringToLongSecs static - then remove stringToLongSecs method

        if (s == null || s.trim().length() == 0) {
            return -1;
        }

        String[] fields = s.split(":");
        long hh = 0;
        long mm = 0;
        long ss = 0;

        switch (fields.length) {
            case 3:
                hh = Utilities.stringToLong(fields[0]);
                mm = Utilities.stringToLong(fields[1]);
                ss = Utilities.stringToLong(fields[2]);
                break;
            case 2:
                mm = Utilities.stringToLong(fields[0]);
                ss = Utilities.stringToLong(fields[1]);
                break;
            case 1:
                ss = Utilities.stringToLong(fields[0]);
                break;

            default:
                break;
        }

        // System.out.println(" values "+hh+":"+mm+":"+ss);

        long totsecs = 0;
        if (hh != -1) {
            totsecs = hh;
        }
        if (mm != -1) {
            totsecs = (totsecs * 60) + mm;
        }
        if (ss != -1) {
            totsecs = (totsecs * 60) + ss;
        }

        // System.out.println(" values "+hh+":"+mm+":"+ss+" secs="+totsecs);

        if (hh == -1 || mm == -1 || ss == -1) {
            return -1;
        }

        return totsecs;
    }

    /**
     * Find sample conetst by name.
     * 
     * @param name
     * @return
     */
    protected String findSampleContestYaml(String name) {

        String conestYamleFilename = getSampleContesYaml(name);

        if (new File(conestYamleFilename).isFile()) {
            return conestYamleFilename;
        } else {
            return null;
        }
    }

    /**
     * Get sample yaml in sample config dir.
     * 
     * 
     * @param name
     * @return
     */
    protected String getSampleContesYaml(String name) {

        String sampleDir = "samps" + File.separator + "contests";
        return sampleDir + File.separator + name + File.separator + "config" + File.separator + IContestLoader.DEFAULT_CONTEST_YAML_FILENAME;
    }

    /**
     * Load groups.tsv and teams.tsv.
     * 
     * @param contest
     * @param cdpConfigDirectory
     * @throws Exception
     */
    private boolean loadCCSTSVFiles(IInternalContest contest, File cdpConfigDirectory) throws Exception {

        boolean loaded = false;

        String teamsTSVFile = cdpConfigDirectory.getAbsolutePath() + File.separator + LoadICPCTSVData.TEAMS_FILENAME;

        String groupsTSVFile = cdpConfigDirectory.getAbsolutePath() + File.separator + LoadICPCTSVData.GROUPS_FILENAME;

        // only load if both tsv files are present.

        if (new File(teamsTSVFile).isFile() && new File(groupsTSVFile).isFile()) {

            LoadICPCTSVData loadTSVData = new LoadICPCTSVData();
            loadTSVData.setContestAndController(contest, null);
            loaded = loadTSVData.loadFiles(teamsTSVFile, false, false);
        }

        return loaded;
    }

    public static Site createFirstSite(int siteNumber, String hostName, int portNumber) {
        Site site = new Site("Site " + siteNumber, siteNumber);
        Properties props = new Properties();
        props.put(Site.IP_KEY, hostName);
        props.put(Site.PORT_KEY, "" + portNumber);
        site.setConnectionInfo(props);
        site.setPassword("site" + siteNumber);
        return site;
    }

    @Override
    public File findCDPConfigDirectory(File entry) {
        File cdpConfigDirectory = null;

        if (entry.isDirectory()) {

            // found a directory
            cdpConfigDirectory = new File(entry.getAbsoluteFile() + File.separator + CONFIG_DIRNAME);

        } else if (entry.isFile()) {

            // a file

            if (IContestLoader.DEFAULT_CONTEST_YAML_FILENAME.equals(entry.getName())) {
                // found contest.yaml

                cdpConfigDirectory = entry.getParentFile();
            }

        } else {

            // A CDP in the samples directory

            String sampleContestYamlFile = findSampleContestYaml(entry.getName());

            if (sampleContestYamlFile != null) {
                File yamlFile = new File(sampleContestYamlFile);
                String configDirPath = yamlFile.getParentFile().getAbsoluteFile().toString();
                cdpConfigDirectory = new File(configDirPath);
            }

        }
        return cdpConfigDirectory;
    }

    @Override
    public IInternalContest initializeContest(IInternalContest contest, File entry) throws Exception {

        if (contest == null) {
            throw new IllegalArgumentException("contest is null");
        }

        File cdpConfigDirectory = findCDPConfigDirectory(entry);

        if (cdpConfigDirectory == null) {
            throw new Exception("Cannot find CDP for " + entry);
        } else {

            loadCCSTSVFiles(contest, cdpConfigDirectory);

            contest = fromYaml(contest, cdpConfigDirectory.getAbsolutePath());

            if (contest.getSites().length == 0) {
                // Create default site.
                Site site = createFirstSite(contest.getSiteNumber(), "localhost", Constants.DEFAULT_PC2_PORT);
                contest.addSite(site);
            }

        }

        return contest;
    }

}
