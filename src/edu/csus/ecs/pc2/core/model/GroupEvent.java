package edu.csus.ecs.pc2.core.model;

/**
 * A group and a event state {@link edu.csus.ecs.pc2.core.model.GroupEvent.Action}.
 * 
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */

// $HeadURL$
public class GroupEvent {

    public static final String SVN_ID = "$Id$";

    /**
     * Run Event States(s).
     * 
     * @author pc2@ecs.csus.edu
     */
    public enum Action {

        /**
         * Group deleted.
         */
        DELETED,
        /**
         * A new group.
         */
        ADDED,
        /**
         * Modify a group.
         */
        CHANGED,
        /**
         * More then 1 group was added
         */
        ADDED_GROUPS,
        /**
         * More then 1 group was updated
         */
        CHANGED_GROUPS,
        /**
         * Reload/Refresh all groups.
         */
        REFRESH_ALL,

    }

    private Action action;

    private Group group;

    private Group[] groups;

    public GroupEvent(Action groupAction, Group group) {
        super();
        this.action = groupAction;
        this.group = group;
    }

    public GroupEvent(Action groupAction, Group[] groups) {
        super();
        this.action = groupAction;
        this.setGroups(groups);
    }

    public Action getAction() {
        return action;
    }

    public Group getGroup() {
        return group;
    }

    /**
     * @return the groups
     */
    public Group[] getGroups() {
        return groups;
    }

    /**
     * @param groups the groups to set
     */
    public void setGroups(Group[] groups) {
        this.groups = groups;
    }

}
