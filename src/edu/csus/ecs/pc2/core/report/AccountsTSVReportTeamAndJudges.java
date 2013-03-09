package edu.csus.ecs.pc2.core.report;

import java.util.Vector;

import edu.csus.ecs.pc2.core.IInternalController;
import edu.csus.ecs.pc2.core.model.Account;
import edu.csus.ecs.pc2.core.model.ClientType.Type;
import edu.csus.ecs.pc2.core.model.Filter;
import edu.csus.ecs.pc2.core.model.IInternalContest;

/**
 * An AccountTSVReport with only teams and judges accounts.
 * 
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */
// $HeadURL$
public class AccountsTSVReportTeamAndJudges extends AccountsTSVReport {

    /**
     * 
     */
    private static final long serialVersionUID = 921156175618527895L;

    private IInternalContest contest = null;
    
    @Override
    public void setContestAndController(IInternalContest inContest, IInternalController inController) {
        super.setContestAndController(inContest, inController);
        contest = inContest;
    }
    
    @Override
    public void setFilter(Filter filter) {
        
        super.setFilter(filter);
        
        if (! filter.isFilteringAccounts()){
            /**
             * If not already filtering on accounts, set default
             * filter to judges and teams.
             */
            
            Account[] judgeAccounts = getAccounts(Type.JUDGE);
            filter.addAccounts(judgeAccounts);
            
            Account[] teamAccounts = getAccounts(Type.TEAM);
            filter.addAccounts(teamAccounts);
        }
    }
    
    private Account[] getAccounts(Type type) {
        Vector<Account> vector = contest.getAccounts(type);
        return (Account[]) vector.toArray(new Account[vector.size()]);
    }
    
    @Override
    public String getReportTitle() {
        return "accounts.tsv (team and judges)";
    }
    
    @Override
    public String getPluginTitle() {
        return "accounts.tsv Report (team and judges)";
    }
}
