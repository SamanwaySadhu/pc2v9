package edu.csus.ecs.pc2.ui;

import java.awt.Component;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import edu.csus.ecs.pc2.VersionInfo;
import edu.csus.ecs.pc2.core.IInternalController;
import edu.csus.ecs.pc2.core.NoteList;
import edu.csus.ecs.pc2.core.NoteMessage;
import edu.csus.ecs.pc2.core.log.Log;
import edu.csus.ecs.pc2.core.model.Filter;
import edu.csus.ecs.pc2.core.model.IInternalContest;
import edu.csus.ecs.pc2.core.report.IReport;
import edu.csus.ecs.pc2.core.security.Permission;

/**
 * Base class for a JFrame (GUI) plugin.
 * 
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */

// $HeadURL$
public abstract class JFramePlugin extends JFrame implements UIPlugin {

    /**
     * 
     */
    private static final long serialVersionUID = -1059195405486851400L;

    private IInternalController controller;

    private IInternalContest contest;
    
    private JFrame parentFrame = null;

    public void setContestAndController(IInternalContest inContest, IInternalController inController) {
        this.controller = inController;
        this.contest = inContest;
    }

    public abstract String getPluginTitle();

    public IInternalController getController() {
        return controller;
    }

    public void setController(IInternalController controller) {
        this.controller = controller;
    }

    public IInternalContest getContest() {
        return contest;
    }

    public void setContest(IInternalContest contest) {
        this.contest = contest;
    }


    public JFrame getParentFrame() {
        return parentFrame;
    }


    public void setParentFrame(JFrame parentFrame) {
        this.parentFrame = parentFrame;
    }
    
    /**
     * Get filename based on the report title.
     *  
     * @param selectedReport
     * @return
     */

    /**
     * Create and view via GUI the report file.
     * 
     * @param report the IReport to be viewed
     * @param filter the Filter to be applied to the Report
     * @param log the Log to be used for logging
     * @throws IOException if the path to the Reports directory cannot be found
     */
    public void createAndViewReportFile(IReport report, Filter filter, Log log) throws IOException {

        ReportPane reportPane = new ReportPane();
        reportPane.setContestAndController(contest, controller);

        String filename = reportPane.getFileName(report, "txt");
        
        File reportDirectoryFile = new File(reportPane.getReportDirectory());
        if (reportDirectoryFile.exists()) {
            if (reportDirectoryFile.isDirectory()) {
                filename = reportDirectoryFile.getCanonicalPath() + File.separator + filename;
            }
        } else {
            if (reportDirectoryFile.mkdirs()) {
                filename = reportDirectoryFile.getCanonicalPath() + File.separator + filename;
            }
        }

        reportPane.createReportFile(report, false, filename, filter);

        String title = report.getReportTitle();

        MultipleFileViewer multipleFileViewer = new MultipleFileViewer(log);
        multipleFileViewer.addFilePane(title, filename);
        multipleFileViewer.setTitle("PC^2 Report (Build " + new VersionInfo().getBuildNumber() + ")");
        FrameUtilities.centerFrameFullScreenHeight(multipleFileViewer);
        multipleFileViewer.setVisible(true);
    }
    
    public void logException(String message, Exception ex){
        controller.getLog().log(Log.WARNING, message,ex);
    }
    
    /**
     * Output notelist to log.
     * 
     * @param noteList
     */
    public void logNoteList(NoteList noteList) {

        for (NoteMessage noteMessage : noteList.getAll()) {

            String message = noteMessage.getComment();
            String prefix = "";

            String filename = noteMessage.getFilename();
            if (filename != null && filename.equals(NoteList.NO_FILENAME)) {
                prefix = filename + " " + noteMessage.getLineNumber();
                if (noteMessage.getColumnNumber() != 0) {
                    prefix += " col " + noteMessage.getColumnNumber();
                }
                message = prefix + " " + message;
            }

            switch (noteMessage.getType()) {
                case ERROR:
                    controller.getLog().log(Log.SEVERE, message);
                    break;
                case WARNING:
                    controller.getLog().log(Log.WARNING, message);
                    break;
                default:
                    controller.getLog().log(Log.INFO, message);
                    break;
            }
        }
    }
    
    /**
     * Load permission list.
     * 
     * @see #isAllowed(edu.csus.ecs.pc2.core.security.Permission.Type)
     */
    public void initializePermissions() {
        
        // TODO 6361 remove this method  everywhere.
        
    }

    public boolean isAllowed(Permission.Type type) {
        return getContest().isAllowed(type);
    }
        
    
    /**
     * Show message to user.
     * 
     * @param component
     * @param title
     * @param message
     */
    public void showMessage(Component component, String title, String message) {
        JOptionPane.showMessageDialog(component, message, title, JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Show message to user.
     * 
     * @param message
     */
    // TODO cleanup - uncomment and use this for all messages
//    private void showMessage(String message) {
//        JOptionPane.showMessageDialog(null, message, "Information Message", JOptionPane.INFORMATION_MESSAGE);
//    }

    /**
     * Show message to user.
     * 
     * @param frame parent frame
     * @param strTitle title
     * @param message message
     */
    public void showMessage(JFrame frame, String strTitle, String message) {
        final JDialog dialog = new JDialog(frame, strTitle, true);
        final JOptionPane optionPane = new JOptionPane(message, JOptionPane.INFORMATION_MESSAGE);
        dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
        optionPane.addPropertyChangeListener(new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent e) {
                String prop = e.getPropertyName();

                if (dialog.isVisible() && (e.getSource() == optionPane) && (prop.equals(JOptionPane.VALUE_PROPERTY))) {
                    // If you were going to check something
                    // before closing the window, you'd do
                    // it here.
                    dialog.setVisible(false);
                }
            }
        });
        dialog.setContentPane(optionPane);
        dialog.pack();
        FrameUtilities.centerFrameOver(parentFrame, dialog);
        dialog.setVisible(true);
    }

}
