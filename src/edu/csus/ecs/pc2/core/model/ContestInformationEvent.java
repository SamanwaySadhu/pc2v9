package edu.csus.ecs.pc2.core.model;

/**
 * A contestInformation and a event state {@link edu.csus.ecs.pc2.core.model.ContestInformationEvent.Action}.
 * 
 * @version $Id$
 * @author pc2@ecs.csus.edu
 */

//$HeadURL$
public class ContestInformationEvent {

    public static final String SVN_ID = "$Id$";

    /**
     * InternalContest Information Event States(s).
     * 
     * @author pc2@ecs.csus.edu
     */
    public enum Action {

        /**
         * 
         */
        DELETED,
        /**
         * A new contestInformation.
         */
        ADDED,
        /**
         * Modified ContestInformation.
         */
        CHANGED,
        /**
         * Reload/Refresh all Contest Information.
         */
        REFRESH_ALL,
        /**
         * Update Finalize Data
         */
        CHANGED_FINALIZED,

    }

    private Action action;

    private ContestInformation contestInformation;
    
    private FinalizeData finalizeData;

    public ContestInformationEvent(Action action, ContestInformation contestInformation) {
        super();
        this.action = action;
        this.contestInformation = contestInformation;
    }

    public ContestInformationEvent(ContestInformation contestInformation, FinalizeData data){
        super();
        this.action = Action.CHANGED_FINALIZED;
        this.finalizeData = data;
        this.contestInformation = contestInformation;
    }
    
    public Action getAction() {
        return action;
    }

    public ContestInformation getContestInformation() {
        return contestInformation;
    }
    
    public FinalizeData getFinalizeData() {
        return finalizeData;
    }

}
