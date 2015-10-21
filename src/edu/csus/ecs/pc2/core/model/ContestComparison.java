package edu.csus.ecs.pc2.core.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import edu.csus.ecs.pc2.core.model.ClientType.Type;

/**
 * Provides descriptions of differences between model (Contest). 
 * 
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */

// $HeadURL$
public class ContestComparison {

    public static final String NEW_LINE = System.getProperty("line.separator");

    private List<IElementObject> list = new ArrayList<>();

    public String comparisonList(IInternalContest contestOne, IInternalContest contestTwo) {

        StringBuffer buffer = new StringBuffer();

        ContestInformation ciOne = contestOne.getContestInformation();
        ContestInformation ciTwo = contestTwo.getContestInformation();

        if (!ciOne.isSameAs(ciTwo)) {
            buffer.append("Replace Contest Information" + NEW_LINE);
        }
        
        buffer.append(addComment("Site", contestOne.getSites().length, contestTwo.getSites().length));

        buffer.append(addComment("Problem", contestOne.getProblems().length, contestTwo.getProblems().length));

        
        int [] contestOneCounts = getDataFileCounts(contestOne);
        int [] contestTwoCounts = getDataFileCounts(contestTwo);
        
        String [] countTitles = {
         //  [0] total files, [1] data files, [2] answer files, [3] data file bytes, [4] answer file bytes
                "Total Files", //
                "Input files", //
                "Answer files", //
        };
        
        for (int i = 0; i < contestTwoCounts.length; i++) {
            buffer.append(addComment(countTitles[i], contestOneCounts[i], contestTwoCounts[i]));
        }
        
        buffer.append(addComment("Language", contestOne.getLanguages().length, contestTwo.getLanguages().length));
        
        for (Type type : Type.values()) {
            String accountTypeName = type.toString().toLowerCase();
            
            int countOne = contestOne.getAccounts(type).size();
            int countTwo = contestTwo.getAccounts(type).size();
            
            if (accountTypeName.equalsIgnoreCase("ALL")){
                accountTypeName = "Account";
            }
            buffer.append(addComment(accountTypeName, countOne, countTwo));
        }
        

        buffer.append(addComment("Group", contestOne.getGroups().length, contestTwo.getGroups().length));
        buffer.append(addComment("Clar. category", contestOne.getCategories().length, contestTwo.getCategories().length));
        buffer.append(addComment("Judgement", contestOne.getJudgements().length, contestTwo.getJudgements().length));
        
        
        ClientSettings[] settings = contestOne.getClientSettingsList();
        ClientSettings[] settingsTwo = contestTwo.getClientSettingsList();
        
        int autoJudgeSettingsOne = getAutoJudgeSettings(settings);
        int autoJudgeSettingsTow = getAutoJudgeSettings(settingsTwo);
        
        
        buffer.append(addComment("AJ Settings", autoJudgeSettingsOne, autoJudgeSettingsTow));

        // SOMEDAY compare CONTEST_INFORMATION,
        // SOMEDAY compare BALLOON_SETTINGS_LIST,
        // SOMEDAY compare CLIENT_SETTINGS_LIST,
        // SOMEDAY compare PROBLEM_DATA_FILES,

        return buffer.toString();
    }


    protected int getAutoJudgeSettings(ClientSettings[] settings) {
        int ajCount = 0;

        for (ClientSettings clientSettings : settings) {
            Filter filter = clientSettings.getAutoJudgeFilter();
            int numberProblems = filter.getProblemIdList().length;
            if (numberProblems > 0) {
                ajCount++;
            }
        }
        return ajCount;
    }


    /**
     * Get counts of data files.
     * 
     * @param aContest
     * @return [0] total files, [1] data files, [2] answer files
     */
    private int[] getDataFileCounts(IInternalContest aContest) {

        int[] outCounts = new int[3];

//        long totalAnswerBytes = 0;
//        long totalBytes = 0;

        Problem[] problems = aContest.getProblems();

        if (problems.length > 0) {
            int ansCount = 0;
            int datCount = 0;

            for (Problem problem : problems) {
                ProblemDataFiles pdfiles = aContest.getProblemDataFile(problem);
                if (pdfiles != null) {
                    
                    ansCount += pdfiles.getJudgesAnswerFiles().length;
                    datCount += pdfiles.getJudgesDataFiles().length;
                    
//                    for (SerializedFile serializedFile : pdfiles.getJudgesAnswerFiles()) {
//                        totalBytes += serializedFile.getBuffer().length;
//                    }
//                    for (SerializedFile serializedFile : pdfiles.getJudgesDataFiles()) {
//                        totalAnswerBytes = serializedFile.getBuffer().length;
//                    }
                    // } else { // nothing to do here, move on
                }
            }

            outCounts[1] = datCount;
            outCounts[2] = ansCount;
        } // no else

        return outCounts;
    }

    private String addComment(String name, int sizeOne, int sizeTwo) {

        String message = "";

        // replaced by the sizeOne+sizeTwo > 0 test on the equal size if
//        if (sizeTwo == 0 && sizeOne == 0) {
            // identical 
        if (sizeTwo == sizeOne && (sizeOne + sizeTwo > 0)) {
            message += "Replace all " + sizeOne + " " + Pluralize.pluralize(name, sizeOne) + NEW_LINE;
        } else if (sizeTwo < sizeOne) {
            int diff = sizeOne - sizeTwo;
            if (sizeTwo != 0){
                message += "Replace " + sizeTwo + " " + Pluralize.pluralize(name, sizeOne) + NEW_LINE;
                message += "Keep/ignore last " + diff + " " + Pluralize.pluralize(name, diff) + NEW_LINE;
            }
        } else if (sizeTwo > sizeOne) {
            int diff = sizeTwo - sizeOne;
            if (sizeOne > 0) {
                message += "Replace " + sizeOne + " " + Pluralize.pluralize(name, sizeOne) + NEW_LINE;
            }
            message += "Add " + diff + " " + Pluralize.pluralize(name, diff) + NEW_LINE;
        }

        return message;
    }

    public IElementObject[] getItems() {
        return (IElementObject[]) list.toArray(new IElementObject[list.size()]);
    }

    public void add(IElementObject element) {
        list.add(element);
    }

    public void remove(IElementObject element) {
        list.remove(element);
    }

    private  void addSummaryEntry(StringBuffer buf, String entryName) {
        buf.append(entryName);
        buf.append(NEW_LINE);
    }

    private  void addSummaryEntry(StringBuffer buf, int count, String prefix, String entryName) {
        if (count > 0) {
            buf.append(count);
            String pluralized = Pluralize.pluralize(entryName, count);
            if (prefix.length() > 0) {
                buf.append(' ');
                buf.append(prefix);
            }
            buf.append(' ');
            buf.append(pluralized);
            buf.append(NEW_LINE);
        }
    }

    private  void addSummaryEntry(StringBuffer sb, int count, String entryName) {
        addSummaryEntry(sb, count, "", entryName);
    }

    public  String getContestLoadSummaryShorter(IInternalContest newContest) throws Exception {

        Language[] languages = newContest.getLanguages();
        Problem[] problems = newContest.getProblems();
        Site[] sites = newContest.getSites();
        Category[] categories = newContest.getCategories();
        ClientSettings[] settings = newContest.getClientSettingsList();

        StringBuffer sb = new StringBuffer();

        addSummaryEntry(sb, sites.length, "site");

        addSummaryEntry(sb, problems.length, "problem");

        if (problems.length > 0) {
            int ansCount = 0;
            int datCount = 0;
            int numberExternalDataFileProblems = 0;

            for (Problem problem : problems) {
                ProblemDataFiles pdfiles = newContest.getProblemDataFile(problem);
                if (pdfiles != null) {
                    ansCount += pdfiles.getJudgesAnswerFiles().length;
                    datCount += pdfiles.getJudgesDataFiles().length;
                    // } else { // nothing to do here, move on
                }

                if (problem.isUsingExternalDataFiles()) {
                    numberExternalDataFileProblems++;
                }
            }

            if (datCount > 0) {
                addSummaryEntry(sb, datCount, "input data file");
            } else if (numberExternalDataFileProblems == problems.length) {
                sb.append("All data files are external");
            }

            if (ansCount > 0) {
                addSummaryEntry(sb, ansCount, "answer data file");
            } else if (numberExternalDataFileProblems == problems.length) {
                sb.append("All answer files are external");
            }
        }

        addSummaryEntry(sb, languages.length, "language");

        for (Type type : Type.values()) {
            Vector<Account> accounts = newContest.getAccounts(type);
            String accountTypeName = type.toString().toLowerCase();
            addSummaryEntry(sb, accounts.size(), accountTypeName, " account");
        }

        addSummaryEntry(sb, categories.length, "clar", "category");

        addSummaryEntry(sb, settings.length, "AJ setting");

        return sb.toString();
    }

    /**
     * 
     */
    public String getContestLoadSummary(IInternalContest newContest) throws Exception {

        // SOMEDAY replace getContestLoadSummary with ContestSummaryFrame
        // ContestSummaryFrame frame = new ContestSummaryFrame();
        // frame.setContestAndController(newContest, inController);
        // frame.setVisible(true);

        Language[] languages = newContest.getLanguages();
        Problem[] problems = newContest.getProblems();
        Site[] sites = newContest.getSites();
        Category[] categories = newContest.getCategories();
        ClientSettings[] settings = newContest.getClientSettingsList();

        StringBuffer sb = new StringBuffer();

        addSummaryEntry(sb, sites.length, "site");

        addSummaryEntry(sb, problems.length, "problem");

        long totalBytes = 0;
        int ansCount = 0;
        int datCount = 0;

        if (problems.length > 0) {
            
            int totalProblems = problems.length;
            int externalDataProblemCount = 0;

            for (Problem problem : problems) {
                
                if (problem.isUsingExternalDataFiles()){
                    externalDataProblemCount++;
                }
                
                ProblemDataFiles pdfiles = newContest.getProblemDataFile(problem);
                if (pdfiles != null) {
                    ansCount += pdfiles.getJudgesAnswerFiles().length;
                    datCount += pdfiles.getJudgesDataFiles().length;
                    for (SerializedFile serializedFile : pdfiles.getJudgesAnswerFiles()) {
                        totalBytes += serializedFile.getBuffer().length;
                    }
                    for (SerializedFile serializedFile : pdfiles.getJudgesDataFiles()) {
                        totalBytes += serializedFile.getBuffer().length;
                    }
                    // } else { // nothing to do here, move on
                } // for
                
                if (totalProblems == externalDataProblemCount){
                    System.out.println("All external data files ("+totalProblems+")");
                } else if (externalDataProblemCount == 0) {
                    System.out.println("All internal/loaded data files (" + totalProblems + ")");
                }
        }

            addSummaryEntry(sb, datCount, "input data file");
            addSummaryEntry(sb, ansCount, "answer data file");
        } else {
            addSummaryEntry(sb, "No problems to be added");
        }

        addSummaryEntry(sb, languages.length, "language");

        for (Type type : Type.values()) {
            Vector<Account> accounts = newContest.getAccounts(type);
            String accountTypeName = type.toString().toLowerCase();
            addSummaryEntry(sb, accounts.size(), accountTypeName, " account");
        }

        addSummaryEntry(sb, categories.length, "clar", "category");

        addSummaryEntry(sb, settings.length, "AJ setting");

        if (problems.length > 0) {
            int totalFiles  = ansCount + datCount;

            if (totalBytes == 0) {
                sb.append("NO file/data contents loaded for any Problems (external files)");
                sb.append(NEW_LINE);
            } else {
                long meg = totalBytes / 1000 / 1000;
                sb.append("Will load " + totalBytes + " bytes (" + meg + " Meg) from "+totalFiles+" files");
                sb.append(NEW_LINE);
            }
        }

        return sb.toString();
    }

}
