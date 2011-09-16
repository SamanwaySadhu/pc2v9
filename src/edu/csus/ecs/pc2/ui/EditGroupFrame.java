package edu.csus.ecs.pc2.ui;

import javax.swing.JFrame;

import edu.csus.ecs.pc2.core.IInternalController;
import edu.csus.ecs.pc2.core.model.IInternalContest;
import edu.csus.ecs.pc2.core.model.Group;
import java.awt.Dimension;

/**
 * 
 * @author pc2@ecs.csus.edu
 * @version $Id$
 * 
 */

// $HeadURL$
// $Id$

public class EditGroupFrame extends JFrame implements UIPlugin {

    /**
     * 
     */
    private static final long serialVersionUID = -6248957592340866836L;

    private IInternalContest contest;

    private IInternalController controller;

    private EditGroupPane groupPane = null;

    /**
     * This method initializes
     * 
     */
    public EditGroupFrame() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     * 
     */
    private void initialize() {
        this.setSize(new Dimension(549, 242));
        this.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        this.setContentPane(getGroupPane());
        this.setTitle("New Group");

        FrameUtilities.centerFrame(this);

    }

    public void setContestAndController(IInternalContest inContest, IInternalController inController) {
        this.contest = inContest;
        this.controller = inController;

        getGroupPane().setContestAndController(contest, controller);
        getGroupPane().setParentFrame(this);

    }

    public void setGroup() {
        setTitle("Add New Group");
        getGroupPane().setGroup(null);
    }

    
    public void setGroup(Group group, int groupId) {
        if (group == null) {
            setTitle("Add New Group");
        } else {
            setTitle("Edit Group " +groupId + " - " + group.getDisplayName());
        }
        getGroupPane().setGroup(group);
    }

    public String getPluginTitle() {
        return "Edit Group Frame";
    }

    /**
     * This method initializes groupPane
     * 
     * @return edu.csus.ecs.pc2.ui.GroupPane
     */
    private EditGroupPane getGroupPane() {
        if (groupPane == null) {
            groupPane = new EditGroupPane();
        }
        return groupPane;
    }

} // @jve:decl-index=0:visual-constraint="10,10"
