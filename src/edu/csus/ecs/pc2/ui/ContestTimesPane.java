package edu.csus.ecs.pc2.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import edu.csus.ecs.pc2.core.IController;
import edu.csus.ecs.pc2.core.model.ContestTime;
import edu.csus.ecs.pc2.core.model.ContestTimeEvent;
import edu.csus.ecs.pc2.core.model.ElementId;
import edu.csus.ecs.pc2.core.model.IContestTimeListener;
import edu.csus.ecs.pc2.core.model.IContest;
import edu.csus.ecs.pc2.core.model.ISiteListener;
import edu.csus.ecs.pc2.core.model.Site;
import edu.csus.ecs.pc2.core.model.SiteEvent;
import javax.swing.JLabel;

/**
 * Contest Times Pane.
 * 
 * Shows contest times at all sites.
 * 
 * @author pc2@ecs.csus.edu
 */

// $HeadURL$
public class ContestTimesPane extends JPanePlugin {

    /**
     * 
     */
    private static final long serialVersionUID = -8946167067842024295L;

    private JPanel contestTimeButtonPane = null;

    private MCLB contestTimeListBox = null;

    private JButton contestTimeRefreshButton = null;

    private JButton startClockButton = null;

    private JButton stopClockButton = null;

    private JPanel messagePane = null;

    private JLabel messageLabel = null;

    /**
     * This method initializes
     * 
     */
    public ContestTimesPane() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     * 
     */
    private void initialize() {
        this.setLayout(new BorderLayout());
        this.setSize(new java.awt.Dimension(564, 229));
        this.add(getContestTimeListBox(), java.awt.BorderLayout.CENTER);
        this.add(getContestTimeButtonPane(), java.awt.BorderLayout.SOUTH);

        this.add(getMessagePane(), java.awt.BorderLayout.NORTH);
    }

    @Override
    public String getPluginTitle() {
        return "Contest Times";
    }

    /**
     * This method initializes contestTimeButtonPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getContestTimeButtonPane() {
        if (contestTimeButtonPane == null) {
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setHgap(35);
            contestTimeButtonPane = new JPanel();
            contestTimeButtonPane.setLayout(flowLayout);
            contestTimeButtonPane.setPreferredSize(new java.awt.Dimension(35, 35));
            contestTimeButtonPane.add(getStartClockButton(), null);
            contestTimeButtonPane.add(getContestTimeRefreshButton(), null);
            contestTimeButtonPane.add(getStopClockButton(), null);
        }
        return contestTimeButtonPane;
    }

    /**
     * This method initializes contestTimeListBox
     * 
     * @return edu.csus.ecs.pc2.core.log.MCLB
     */
    private MCLB getContestTimeListBox() {
        if (contestTimeListBox == null) {
            contestTimeListBox = new MCLB();

            Object[] cols = { "Site", "Running", "Remaining", "Elapsed", "Length" };

            contestTimeListBox.addColumns(cols);
            contestTimeListBox.autoSizeAllColumns();

        }
        return contestTimeListBox;
    }

    public void updateContestTimeRow(final ContestTime contestTime) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                Object[] objects = buildContestTimeRow(contestTime);
                int rowNumber = contestTimeListBox.getIndexByKey(contestTime.getElementId());
                if (rowNumber == -1) {
                    contestTimeListBox.addRow(objects, contestTime.getElementId());
                } else {
                    contestTimeListBox.replaceRow(objects, rowNumber);
                }
                contestTimeListBox.autoSizeAllColumns();
                contestTimeListBox.sort();
            }
        });
    }

    protected Object[] buildContestTimeRow(ContestTime contestTime) {

        // Object[] cols = { "Site", "Running", "Remaining", "Elapsed", "Length" };

        int numberColumns = contestTimeListBox.getColumnCount();
        Object[] c = new String[numberColumns];

        c[0] = "Site " + contestTime.getSiteNumber();
        c[1] = "NO CONTACT";

        if (contestTime != null) {
            if (contestTime.isContestRunning()) {
                c[1] = "STARTED";
            } else {
                c[1] = "STOPPED";
            }

            c[2] = contestTime.getRemainingTimeStr();
            c[3] = contestTime.getElapsedTimeStr();
            c[4] = contestTime.getContestLengthStr();
        }

        return c;
    }

    private void reloadListBox() {

        showMessage("");
        contestTimeListBox.removeAllRows();
        Site[] sites = getModel().getSites();

        for (Site site : sites) {
            ContestTime contestTime = getModel().getContestTime(site.getSiteNumber());
            if (contestTime != null) {
                addContestTimeRow(contestTime);
            }
        }
    }

    private void addContestTimeRow(ContestTime contestTime) {
        Object[] objects = buildContestTimeRow(contestTime);
        if (contestTime != null) {
            contestTimeListBox.addRow(objects, contestTime.getElementId());
            contestTimeListBox.autoSizeAllColumns();
        }
    }

    public void setModelAndController(IContest inModel, IController inController) {
        super.setModelAndController(inModel, inController);

        getModel().addContestTimeListener(new ContestTimeListenerImplementation());

        getModel().addSiteListener(new SiteListenerImplementation());

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                reloadListBox();
            }
        });
    }

    /**
     * 
     * @author pc2@ecs.csus.edu
     * 
     */
    public class SiteListenerImplementation implements ISiteListener {

        protected void updateSiteInfo(int siteNumber) {
            ContestTime contestTime = getModel().getContestTime(siteNumber);
            if (contestTime != null) {
                updateContestTimeRow(contestTime);
            }

        }

        public void siteAdded(SiteEvent event) {
            int siteNumber = event.getSite().getSiteNumber();
            updateSiteInfo(siteNumber);
        }

        public void siteRemoved(SiteEvent event) {
            // TODO Auto-generated method stub

        }

        public void siteChanged(SiteEvent event) {
            int siteNumber = event.getSite().getSiteNumber();
            updateSiteInfo(siteNumber);
        }

        public void siteLoggedOn(SiteEvent event) {
            int siteNumber = event.getSite().getSiteNumber();
            updateSiteInfo(siteNumber);
        }

        public void siteLoggedOff(SiteEvent event) {
            int siteNumber = event.getSite().getSiteNumber();
            updateSiteInfo(siteNumber);
        }

    }

    /**
     * ContestTime Listener
     * 
     * @author pc2@ecs.csus.edu
     */

    // $HeadURL$
    public class ContestTimeListenerImplementation implements IContestTimeListener {

        public void contestTimeAdded(ContestTimeEvent event) {
            updateContestTimeRow(event.getContestTime());
        }

        public void contestTimeRemoved(ContestTimeEvent event) {
            // TODO Auto-generated method stub
        }

        public void contestTimeChanged(ContestTimeEvent event) {
            updateContestTimeRow(event.getContestTime());
        }

        public void contestStarted(ContestTimeEvent event) {
            updateContestTimeRow(event.getContestTime());
        }

        public void contestStopped(ContestTimeEvent event) {
            updateContestTimeRow(event.getContestTime());
        }

    }

    /**
     * This method initializes contestTimeRefreshButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getContestTimeRefreshButton() {
        if (contestTimeRefreshButton == null) {
            contestTimeRefreshButton = new JButton();
            contestTimeRefreshButton.setText("Refresh");
            contestTimeRefreshButton.setToolTipText("Refresh All Clocks");
            contestTimeRefreshButton.setMnemonic(java.awt.event.KeyEvent.VK_R);
            contestTimeRefreshButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    reloadListBox();
                }
            });
        }
        return contestTimeRefreshButton;
    }

    /**
     * This method initializes startClockButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getStartClockButton() {
        showMessage("");

        if (startClockButton == null) {
            startClockButton = new JButton();
            startClockButton.setText("Start");
            startClockButton.setMnemonic(java.awt.event.KeyEvent.VK_S);
            startClockButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    startClockTimes();
                }
            });
        }
        return startClockButton;
    }

    protected void startClockTimes() {
        int[] selectedSites = contestTimeListBox.getSelectedIndexes();
        if (selectedSites.length == 0) {
            showMessage("Please select site");
            return;
        }

        for (int i = 0; i < selectedSites.length; i++) {
            ElementId contestTimeElementId = (ElementId) contestTimeListBox.getKeys()[i];
            ContestTime contestTime = getModel().getContestTime(contestTimeElementId);
            if (contestTime != null) {
                getController().startContest(contestTime.getSiteNumber());
            }
        }

    }

    /**
     * This method initializes stopClockButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getStopClockButton() {
        showMessage("");

        if (stopClockButton == null) {
            stopClockButton = new JButton();
            stopClockButton.setText("Stop");
            stopClockButton.setMnemonic(java.awt.event.KeyEvent.VK_T);
            stopClockButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    stopClockTimes();
                }
            });
        }
        return stopClockButton;
    }

    protected void stopClockTimes() {

        int[] selectedSites = contestTimeListBox.getSelectedIndexes();
        if (selectedSites.length == 0) {
            showMessage("Please select site");
            return;
        }

        for (int i = 0; i < selectedSites.length; i++) {
            ElementId contestTimeElementId = (ElementId) contestTimeListBox.getKeys()[i];
            ContestTime contestTime = getModel().getContestTime(contestTimeElementId);
            if (contestTime != null) {
                getController().stopContest(contestTime.getSiteNumber());
            }
        }
    }

    /**
     * This method initializes messagePane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getMessagePane() {
        if (messagePane == null) {
            messageLabel = new JLabel();
            messageLabel.setText("");
            messageLabel.setFont(new java.awt.Font("Dialog", java.awt.Font.BOLD, 14));
            messageLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
            messagePane = new JPanel();
            messagePane.setLayout(new BorderLayout());
            messagePane.setPreferredSize(new java.awt.Dimension(30, 30));
            messagePane.add(messageLabel, java.awt.BorderLayout.CENTER);
        }
        return messagePane;
    }

    private void showMessage(final String string) {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                if (messageLabel != null) {
                    messageLabel.setText(string);
                    messageLabel.setToolTipText(string);
                }
            }
        });
    }

} // @jve:decl-index=0:visual-constraint="10,10"
