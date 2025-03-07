// Copyright (C) 1989-2019 PC2 Development Team: John Clevenger, Douglas Lane, Samir Ashoo, and Troy Boudreau.
package edu.csus.ecs.pc2.core.imports;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import javax.swing.JOptionPane;

import edu.csus.ecs.pc2.core.IInternalController;
import edu.csus.ecs.pc2.core.model.Account;
import edu.csus.ecs.pc2.core.model.Group;
import edu.csus.ecs.pc2.core.model.IInternalContest;
import edu.csus.ecs.pc2.imports.ccs.ICPCTSVLoader;
import edu.csus.ecs.pc2.ui.FrameUtilities;
import edu.csus.ecs.pc2.ui.UIPlugin;

/**
 * Load ICPC TSV files into contest.
 * 
 * Read input .tsv files, validate then if valid load into contest.
 * 
 * @author pc2@ecs.csus.edu
 */

public class LoadICPCTSVData implements UIPlugin {

    public static final String TEAMS2_TSV = "teams2.tsv";

    /**
     * 
     */
    private static final long serialVersionUID = 1611218320856176033L;

    public static final String TEAMS_FILENAME = "teams.tsv";

    public static final String GROUPS_FILENAME = "groups.tsv";

    private static final String ACCOUNTS_FILENAME = "accounts.tsv";
    
    public static final String INSTITUTIONS_FILENAME = "institutions.tsv";

    private String teamsFilename = "";

    private String groupsFilename = "";

    private IInternalContest contest;

    private IInternalController controller;

    private String accountsFilename = "";

    public void setContestAndController(IInternalContest inContest, IInternalController inController) {
        contest = inContest;
        controller = inController;
    }
    
    public IInternalContest getContest() {
        return contest;
    }

    /**
     * Load both teams.tsv and groups.tsv into contest and sends results to server.
     * 
     * @param filename
     * @return
     * @throws Exception
     */
    public boolean loadFiles(String filename) throws Exception {
        return loadFiles(filename, true, true);
    }
    
    /**
     * Load both teams.tsv and groups.tsv into contest and optionally sends results to server.
     * @param filename
     * @param sendToServer if true sends to server via controller, if false just loads into contest
     * @param useConfirmGUI prompt using GUI to add accounts and groups
     * @return
     * @throws Exception
     */
    public boolean loadFiles(String filename, boolean sendToServer, boolean useConfirmGUI) throws Exception {

        if (checkFiles(filename)) {

            String instFilename = groupsFilename.replaceFirst(GROUPS_FILENAME, INSTITUTIONS_FILENAME);
            // this must be loaded before accounts, and no harm before groups
            ICPCTSVLoader.loadInstitutions(instFilename);
            Group[] groups = ICPCTSVLoader.loadGroups(groupsFilename, contest.getGroups());
            
//          for (Group group : groups) {
//              group.setSite(getContest().getSites()[0].getElementId());
//          }
            Account[] accounts = ICPCTSVLoader.loadAccounts(teamsFilename);
            
            if (!accountsFilename.equals("")) {
                HashMap<Integer, String> passwordMap = ICPCTSVLoader.loadPasswordsFromAccountsTSV(accountsFilename);
                if (!passwordMap.isEmpty()) {
                    for (int i = 0; i < accounts.length; i++) {
                        Account account = accounts[i];
                        int clientNumber = account.getClientId().getClientNumber();
                        String password = passwordMap.get(new Integer(clientNumber));
                        account.setPassword(password);
                    }
                }
            } else {
                System.err.println("accounts.tsv filename is empty still");
            }

            int result = JOptionPane.YES_OPTION;

            if (useConfirmGUI){
                
                String nl = System.getProperty("line.separator");
                String message = "Add " + nl + accounts.length + " accounts and " + nl + groups.length + " groups?";

                result = FrameUtilities.yesNoCancelDialog(null, message, "Load TSV files");
            }
            
            if (result == JOptionPane.YES_OPTION) {
                
                List<Group> groupList = Arrays.asList(groups);
                List<Account> accountList = Arrays.asList(accounts);

                /**
                 * Merge/update groups and account into existing groups and accounts, create if necessary
                 */
                updateGroupsAndAccounts (contest, groupList, accountList);
                
                if (sendToServer) {

                    /**
                     * Update Groups
                     */
                    Group[] updatedGroups = (Group[]) groupList.toArray(new Group[groupList.size()]);
                    for (Group group : updatedGroups) {
                        getController().updateGroup(group);
                    }

                    info("Sent groups from " + groupsFilename + " to server");

                    /**
                     * UpdateAccounts
                     */
                    Account[] updatedAccounts = (Account[]) accountList.toArray(new Account[accountList.size()]);
                    getController().updateAccounts(updatedAccounts);
                    info("Sent accounts from " + teamsFilename + " to server");
                } else {
                    
                    // since not sending to server, add to existing contest.
                    
                    Group[] updatedGroups = (Group[]) groupList.toArray(new Group[groupList.size()]);
                    
                    for (Group group : updatedGroups) {
                        contest.updateGroup(group);
                    }
                    
                    Account[] updatedAccounts = (Account[]) accountList.toArray(new Account[accountList.size()]);
                    for (Account account : updatedAccounts) {
                        contest.updateAccount(account);
                    }
                }

                info("Load from file " + groupsFilename);
                info("Load from file " + teamsFilename);
                
                info("Added " + accounts.length + " accounts and " + groups.length + " groups.");

                return true;
            } else {
                return false;
            }

        } else {
            return false;
        }
    }

    private void info(String message) {
        if (getController() == null) {
            System.out.println(message);
        } else {
            getController().getLog().info(message);
        }
    }

    /**
     * Merge/update groups and account into existing groups and accounts, create if necessary
     * @param contest2
     * @param groupList
     * @param accountList
     */
    protected void updateGroupsAndAccounts(IInternalContest inContest, List<Group> groupList, List<Account> accountList) {

        int i = 0;
        
        for (Group group : groupList) {
            
            Group existingGroup = lookupGroup (inContest, group.getGroupId());
            if (existingGroup != null){

                /**
                 * Update certain fields in group
                 */
                existingGroup.updateFrom(group);
                
            } else {
                existingGroup = group;
            }
            
            groupList.set(i, existingGroup);
            i++;
        }
        
        i = 0;
        
        for (Account account : accountList) {
            
            Account existingAccount = inContest.getAccount(account.getClientId());
            
            if (existingAccount != null){
                existingAccount.updateFrom(account);
            } else {
                existingAccount = account;
            }
            
            accountList.set(i, existingAccount);
            i ++;
            
            
        }
        
    }

    /**
     * Lookup group by externalId
     * @param contest2
     * @param externalId
     * @return
     */
    private Group lookupGroup(IInternalContest contest2, int externalId) {
        
        Group [] groups = contest2.getGroups();
        for (Group group : groups) {
            if (group.getGroupId() == externalId){
                return group;
            }
        }
        return null;
    }
    
    /**
     * checkFiles
     * Check the teams, groups and accounts tsv files.
     * These files should be in the same folder, so the basename is determined
     * and the other files are formed using path where the basename file resides.
     * teams2.tsv is preferred over teams.tsv since it has groups in it and is otherwise identical.
     * If teams.tsv is specified, the existence of teams2.tsv is
     * checked.  If found, it replaces teams.tsv.
     * Previously, if teams.tsv did not exist, then the existance of teams2.tsv was never
     * even considered and an error was returned.  This behavior was wrong.
     * @param filename - one of "/path-to/teams.tsv" or "/path-to/groups.tsv"
     * @return true if needed files are present
     * @throws Exception on error
     */
    protected boolean checkFiles(String filename) throws Exception {

        if (filename.endsWith(TEAMS_FILENAME)) {
            teamsFilename = filename;
            groupsFilename = filename;
            accountsFilename = filename;
            groupsFilename = groupsFilename.replaceFirst(TEAMS_FILENAME, GROUPS_FILENAME);
            accountsFilename = accountsFilename.replaceFirst(TEAMS_FILENAME, ACCOUNTS_FILENAME);
        } else if (filename.endsWith(GROUPS_FILENAME)) {
            teamsFilename = filename;
            groupsFilename = filename;
            accountsFilename = filename;
            teamsFilename = teamsFilename.replaceFirst(GROUPS_FILENAME, TEAMS_FILENAME);
            accountsFilename = accountsFilename.replaceFirst(GROUPS_FILENAME, ACCOUNTS_FILENAME);
        } else {
            throw new Exception("Must select either " + TEAMS_FILENAME + " or " + GROUPS_FILENAME);
        }

        String teams2Filename = teamsFilename.replaceFirst(TEAMS_FILENAME, TEAMS2_TSV);
        if (new File(teams2Filename).isFile()) {
            teamsFilename = teams2Filename;
        } else if (!new File(teamsFilename).isFile()) {
            /*
             * We only throw an exception if neither teams file is found.
             * TEAMS_FILENAME does not have to exist of TEAMS2_TSV does.  
             */
            throw new FileNotFoundException("File not found: " + teamsFilename);
        }

        if (!new File(groupsFilename).isFile()) {
            throw new FileNotFoundException("File not found: " + groupsFilename);
        }

        return true;

    }

    public String getPluginTitle() {
        return "Load TSV Files";
    }

    public IInternalController getController() {
        return controller;
    }

    
    public final String getGroupsFilename() {
        return groupsFilename;
    }
    
    public final String getTeamsFilename() {
        return teamsFilename;
    }
}
