package edu.csus.ecs.pc2.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import com.ibm.webrunner.j2mclb.util.HeapSorter;
import com.ibm.webrunner.j2mclb.util.NumericStringComparator;

import edu.csus.ecs.pc2.core.IInternalController;
import edu.csus.ecs.pc2.core.list.ProfileChangeStatusList;
import edu.csus.ecs.pc2.core.log.Log;
import edu.csus.ecs.pc2.core.model.ClientId;
import edu.csus.ecs.pc2.core.model.ClientType;
import edu.csus.ecs.pc2.core.model.ClientType.Type;
import edu.csus.ecs.pc2.core.model.IInternalContest;
import edu.csus.ecs.pc2.core.model.ILoginListener;
import edu.csus.ecs.pc2.core.model.IMessageListener;
import edu.csus.ecs.pc2.core.model.ISiteListener;
import edu.csus.ecs.pc2.core.model.LoginEvent;
import edu.csus.ecs.pc2.core.model.MessageEvent;
import edu.csus.ecs.pc2.core.model.MessageEvent.Area;
import edu.csus.ecs.pc2.core.model.Profile;
import edu.csus.ecs.pc2.core.model.ProfileChangeStatus;
import edu.csus.ecs.pc2.core.model.ProfileChangeStatus.Status;
import edu.csus.ecs.pc2.core.model.Site;
import edu.csus.ecs.pc2.core.model.SiteEvent;
import edu.csus.ecs.pc2.core.packet.Packet;
import edu.csus.ecs.pc2.core.packet.PacketFactory;

/**
 * Pane that allows user to get profile status and switch profile.
 *  
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */

// $HeadURL$
public class SwitchProfileStatusPane extends JPanePlugin {

    /**
     * 
     */
    private static final long serialVersionUID = -1443775801105676210L;

    private JPanel buttonPanel = null;

    private MCLB siteListBox = null;

    private ProfileChangeStatusList profileStatusList = new ProfileChangeStatusList(); // @jve:decl-index=0:

    private JButton switchNowButton = null;

    private JButton refreshButton = null;

    private JButton cancelButton = null;

    private Profile targetProfile;  //  @jve:decl-index=0:

    // TODO used with revert
    @SuppressWarnings("unused")
    private String newContestPassword; // @jve:decl-index=0:

    // TODO used with revert
    @SuppressWarnings("unused")
    private String currentContestPassword; // @jve:decl-index=0:
    
    private SimpleDateFormat formatter = new SimpleDateFormat(" HH:mm:ss MM-dd");  //  @jve:decl-index=0:

    /**
     * This method initializes
     * 
     */
    public SwitchProfileStatusPane() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     * 
     */
    private void initialize() {
        this.setLayout(new BorderLayout());
        this.setSize(new Dimension(433, 206));
        this.add(getButtonPanel(), BorderLayout.SOUTH);
        this.add(getSiteListBox(), BorderLayout.CENTER);

    }

    @Override
    public void setContestAndController(IInternalContest inContest, IInternalController inController) {
        super.setContestAndController(inContest, inController);

        resetProfileStatusList();

        currentContestPassword = inContest.getContestPassword();

        Site thisSite = getContest().getSite(getContest().getSiteNumber());
        updateCurrentSiteStatus(profileStatusList.get(thisSite));
        
   

        getContest().addSiteListener(new SiteListenerImplementation());
        getContest().addLoginListener(new LoginListenerImplementation());
        getContest().addMessageListener(new MessageListenerImplementation());
    }

    private void reloadListBox() {
        siteListBox.removeAllRows();

        ProfileChangeStatus[] list = profileStatusList.getList();

        for (ProfileChangeStatus status : list) {
            updateRow(status);
        }
        
    }

    public void updateRow(final ProfileChangeStatus status) {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                updateStatusRow(status);
            }
        });
    }

    /**
     * Add or update a login row
     * 
     * @param login
     */
    private void updateStatusRow(ProfileChangeStatus status) {
        int row = siteListBox.getIndexByKey(status);
        if (row == -1) {
            Object[] objects = buildSiteStatusRow(status);
            siteListBox.addRow(objects, status);
        } else {
            Object[] objects = buildSiteStatusRow(status);
            siteListBox.replaceRow(objects, row);
        }
        siteListBox.autoSizeAllColumns();
        enableGoButton();
    }

    @Override
    public String getPluginTitle() {
        return "Switch Profile Status Pane";
    }

    /**
     * This method initializes buttonPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getButtonPanel() {
        if (buttonPanel == null) {
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setHgap(45);
            buttonPanel = new JPanel();
            buttonPanel.setLayout(flowLayout);
            buttonPanel.add(getSwitchNowButton(), null);
            buttonPanel.add(getRefreshButton(), null);
            buttonPanel.add(getCancelButton(), null);
        }
        return buttonPanel;
    }
    
    /**
     * If all sites are ready and connected enable switchNowButton button.
     * 
     */
    private void enableGoButton (){
        int numberReady = 0;
        int numberConnected = 0;

        for (ProfileChangeStatus status : profileStatusList.getList()) {
            
            if (isLocalLoggedIn(status.getSiteNumber()) || isThisSite(status.getSiteNumber())){
                numberConnected ++;
                if (status.getStatus().equals(Status.READY)){ 
                    numberReady ++;
                }
            }
        }
        getSwitchNowButton().setEnabled(numberReady == numberConnected);
        
    }

    private boolean isThisSite(int siteNumber) {
        return siteNumber == getContest().getSiteNumber();
    }

    /**
     * This method initializes siteListBox
     * 
     * @return edu.csus.ecs.pc2.ui.MCLB
     */
    private MCLB getSiteListBox() {
        if (siteListBox == null) {
            siteListBox = new MCLB();
            Object[] cols = { "Site", "Title", "Status", "Changed", "Profile", "Logged In", "Since" };

            siteListBox.addColumns(cols);

            // Sorters
            HeapSorter sorter = new HeapSorter();
            HeapSorter numericStringSorter = new HeapSorter();
            numericStringSorter.setComparator(new NumericStringComparator());
            HeapSorter accountNameSorter = new HeapSorter();
            accountNameSorter.setComparator(new AccountColumnComparator());

            int idx = 0;

            siteListBox.setColumnSorter(idx++, numericStringSorter, 1); // Site Number
            siteListBox.setColumnSorter(idx++, accountNameSorter, 2); // Site Title
            siteListBox.setColumnSorter(idx++, sorter, 3); // Status
            siteListBox.setColumnSorter(idx++, sorter, 4); // Status Change
            siteListBox.setColumnSorter(idx++, sorter, 5); // Profile
            siteListBox.setColumnSorter(idx++, sorter, 6); // Logged In
            siteListBox.setColumnSorter(idx++, sorter, 7); // Since 
            
            siteListBox.setMultipleSelections(true);
        }
        return siteListBox;
    }

    /**
     * This method initializes switchNowButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getSwitchNowButton() {
        if (switchNowButton == null) {
            switchNowButton = new JButton();
            switchNowButton.setText("Switch");
            switchNowButton.setMnemonic(KeyEvent.VK_W);
            switchNowButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    startProfileServerSync();
                }
            });
        }
        return switchNowButton;
    }

    protected void closeWindow() {
        getParentFrame().setVisible(false);
    }

    /**
     * This method initializes refreshButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getRefreshButton() {
        if (refreshButton == null) {
            refreshButton = new JButton();
            refreshButton.setText("Refresh");
            refreshButton.setMnemonic(KeyEvent.VK_R);
            refreshButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    requestStatusFromSelectedSite();
                }
            });
        }
        return refreshButton;
    }

    /**
     * This method initializes cancelButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getCancelButton() {
        if (cancelButton == null) {
            cancelButton = new JButton();
            cancelButton.setText("Cancel");
            cancelButton.setMnemonic(KeyEvent.VK_C);
            cancelButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    revertToPreviousProfile();
                }
            });
        }
        return cancelButton;
    }

    private Object[] buildSiteStatusRow(ProfileChangeStatus status) {

//        Object[] cols = { "Site", "Title", "Status", "Changed", "Profile", "Logged In", "Since" };

        Object[] obj = new Object[siteListBox.getColumnCount()];

        Site site = status.getSite();

        obj[0] = site.getSiteNumber();
        obj[1] = site.getDisplayName();

        JLabel label = new JLabel(status.getStatus().toString());

        if (status.getStatus().equals(Status.READY)) {
            label.setForeground(Color.GREEN);
        } else {
            label.setForeground(Color.RED);
        }

        obj[2] = label;

        Date date = status.getModifiedDate();
        if (date == null) {
            obj[3] = "";
        } else {
            obj[3] = formatter.format(date);
        }
        
        Profile theProfile = status.getProfile();
        if (theProfile != null) {
            obj[4] = theProfile.getName() + " (" + theProfile.getDescription() + ")";
        } else {
            obj[4] = "";
        }

        // Logged In [5]
        // Since [6]

        try {
            ClientId serverId = new ClientId(site.getSiteNumber(), Type.SERVER, 0);
            if (isThisSite(site.getSiteNumber())){
                obj[5] = "N/A";
                obj[6] = "";
            } else if (getContest().isLocalLoggedIn(serverId)) {
                obj[5] = "YES";
                obj[6] = formatter.format(getContest().getLocalLoggedInDate(serverId));
            } else if (getContest().isRemoteLoggedIn(serverId)) {
                obj[5] = "YES";
                obj[6] = "";
            }
        } catch (Exception e) {
            obj[5] = "??";
            obj[6] = "";

            getController().getLog().log(Log.WARNING, "Exception updating Contest Time for site " + site.getSiteNumber(), e);
        }

        return obj;
    }

    public void setProfile(Profile profile) {
        this.targetProfile = profile;

    }

    public void setNewContestPassword(String newContestPassword) {
        this.newContestPassword = newContestPassword;
    }

    public void setCurrentContestPassword(String currentContestPassword) {
        this.currentContestPassword = currentContestPassword;
    }

    protected void startProfileServerSync() {
        getController().syncProfileSubmissions (targetProfile);
    }

    protected void revertToPreviousProfile() {

        // FIXME code revert profile, this should be fun.
        JOptionPane.showMessageDialog(this, "Would have reverted to profile " + getContest().getProfile() + " with contest pass = " + getContest().getContestPassword());
        
        closeWindow();
    }

    protected void requestStatusFromSelectedSite() {

        int[] selectedSites = siteListBox.getSelectedIndexes();

        if (selectedSites.length == 0) {
            JOptionPane.showMessageDialog(this, "Select a site", "Must select a site to request", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        
        if (selectedSites.length == 1){
            ProfileChangeStatus profileStatus = (ProfileChangeStatus) siteListBox.getKeys()[selectedSites[0]];
            
            if (isThisSite(profileStatus.getSiteNumber())){
                updateCurrentSiteStatus (profileStatus);
                reloadListBox();
                return;
            }
            
            if (! isLocalLoggedIn (profileStatus.getSiteNumber())){
                JOptionPane.showMessageDialog(this, "Site " + profileStatus.getSiteNumber() + " not available/logged in", "Can not refresh", JOptionPane.INFORMATION_MESSAGE);
                return;
            }
        }
        
        for (int i : selectedSites) {
            
            ProfileChangeStatus profileStatus = (ProfileChangeStatus) siteListBox.getKeys()[i];

            if (isThisSite(profileStatus.getSiteNumber())){
                updateCurrentSiteStatus (profileStatus);
                reloadListBox();
            } else {
                try {
                    ClientId remoteServerId = new ClientId(profileStatus.getSiteNumber(), ClientType.Type.SERVER, 0);
                    Packet packet = PacketFactory.createRequestServerStatusPacket(getContest().getClientId(), remoteServerId, targetProfile);
                    getController().sendToLocalServer(packet);
                } catch (Exception e) {
                    getController().getLog().log(Level.WARNING, "Unable to send request status packet to server " + i, e);
                }
            }

        }
    }

    private boolean isLocalLoggedIn(int siteNumber) {
        ClientId serverId = new ClientId(siteNumber, Type.SERVER, 0);
        return getContest().isLocalLoggedIn(serverId);
    }

    private void updateCurrentSiteStatus(ProfileChangeStatus profileStatus) {
        profileStatus.setStatus(Status.NOTREADY);
        if (getContest().getProfile().equals(targetProfile)){
            profileStatus.setStatus(Status.READY);
        }
        profileStatus.setProfile(getContest().getProfile());
    }

    /**
     * Listen for site profile changes.
     * 
     * @author pc2@ecs.csus.edu
     * @version $Id$
     */
    
    // $HeadURL$
    protected class SiteListenerImplementation implements ISiteListener{

        public void siteProfileStatusChanged(SiteEvent event) {
            ProfileChangeStatus profileChangeStatus = (ProfileChangeStatus) profileStatusList.get(event.getSite());
            profileChangeStatus.setStatus(event.getProfileStatus());
            profileChangeStatus.setProfile(event.getProfile());
            profileStatusList.update(profileChangeStatus);
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    reloadListBox();
                }
            });
        }
        
        public void siteAdded(SiteEvent event) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    reloadListBox();
                }
            });
        }

        public void siteRemoved(SiteEvent event) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    reloadListBox();
                }
            });
            
        }

        public void siteChanged(SiteEvent event) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    reloadListBox();
                }
            });
        }

        public void siteLoggedOn(SiteEvent event) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    reloadListBox();
                }
            });
        }

        public void siteLoggedOff(SiteEvent event) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    reloadListBox();
                }
            });
        }

        public void sitesRefreshAll(SiteEvent siteEvent) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    reloadListBox();
                }
            });
        }
        
    }
    
    public void resetProfileStatusList() {
        profileStatusList = new ProfileChangeStatusList(); 
        for (Site site : getContest().getSites()) {
            profileStatusList.add(new ProfileChangeStatus(site));
        }
        
        Site site = getContest().getSite(getContest().getSiteNumber());
        updateCurrentSiteStatus(profileStatusList.get(site));
        
        reloadListBox();
    }
    
    /**
     * Login Listener for use by ServerView.
     * 
     * @author pc2@ecs.csus.edu
     * 
     */
    public class LoginListenerImplementation implements ILoginListener {

        public void loginAdded(LoginEvent event) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    reloadListBox();
                }
            });
        }

        public void loginRemoved(final LoginEvent event) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    reloadListBox();
                }
            });
        }

        public void loginDenied(LoginEvent event) {
            // updateLoginList(event.getClientId(), event.getConnectionHandlerID());
        }
        
        public void loginRefreshAll(LoginEvent event) {
            
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    reloadListBox();
                }
            });
            
        }
    }

    /**
     * Listener for profile messages.
     * 
     * @author pc2@ecs.csus.edu
     * @version $Id$
     */
    
    // $HeadURL$
    class MessageListenerImplementation implements IMessageListener{

        public void messageAdded(MessageEvent event) {
            if (event.getArea().equals(Area.PROFILES)){
                JOptionPane.showMessageDialog(null, event.getMessage(),"Switch Profile Message", JOptionPane.INFORMATION_MESSAGE);
            }
            getController().getLog().warning(event.getMessage());
        }

        public void messageRemoved(MessageEvent event) {
            // no action
            
        }
    }
    
} // @jve:decl-index=0:visual-constraint="10,10"
