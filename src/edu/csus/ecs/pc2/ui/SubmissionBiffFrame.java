package edu.csus.ecs.pc2.ui;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

import edu.csus.ecs.pc2.core.IInternalController;
import edu.csus.ecs.pc2.core.model.IInternalContest;

/**
 * Submission Biff Frame.
 * 
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */
// $HeadURL$
public class SubmissionBiffFrame extends JFrame implements UIPlugin {

    /**
     * 
     */
    private static final long serialVersionUID = 1330276750746647066L;

    private IInternalContest contest;

    private IInternalController controller;

    private SubmissionBiffPane submissionBiffPane = null;

    /**
     * This method initializes
     * 
     */
    public SubmissionBiffFrame() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     * 
     */
    private void initialize() {
        this.setSize(new java.awt.Dimension(505,159));
        this.setContentPane(getSubmissionBiffPane());
        this.setTitle("Unjudged Submissions");

    }

    public void setContestAndController(IInternalContest inContest, IInternalController inController) {
        this.contest = inContest;
        this.controller = inController;

        submissionBiffPane.setContestAndController(contest, controller);

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                setTitle("PC^2 Unjudged Submission Count Judge " + contest.getTitle());
            }
        });

    }

    public String getPluginTitle() {
        return "Submission Biff";
    }

    /**
     * This method initializes submissionBiffPane
     * 
     * @return edu.csus.ecs.pc2.ui.SubmissionBiffPane
     */
    private SubmissionBiffPane getSubmissionBiffPane() {
        if (submissionBiffPane == null) {
            submissionBiffPane = new SubmissionBiffPane();
        }
        return submissionBiffPane;
    }

    public void setFontSize (int pointSize){
        getSubmissionBiffPane().setFontSize(pointSize);
    }
    
    /**
     * If there are no runs and clars show No runs.
     * 
     * @param showNoRunsTitle if true show No Runs if not runs nor clars
     */
    public void setShowNoRunsTitle(boolean showNoRunsTitle) {
        getSubmissionBiffPane().setShowNoRunsTitle(showNoRunsTitle);
    }

} // @jve:decl-index=0:visual-constraint="10,10"
