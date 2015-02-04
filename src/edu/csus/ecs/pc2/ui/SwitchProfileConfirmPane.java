package edu.csus.ecs.pc2.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.SwingConstants;

import edu.csus.ecs.pc2.core.IInternalController;
import edu.csus.ecs.pc2.core.model.ClientType.Type;
import edu.csus.ecs.pc2.core.model.ClientId;
import edu.csus.ecs.pc2.core.model.IInternalContest;
import edu.csus.ecs.pc2.core.model.Profile;

/**
 * Enter contest password for profile switch.
 * 
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */

// $HeadURL$
public class SwitchProfileConfirmPane extends JPanePlugin {

    /**
     * 
     */
    private static final long serialVersionUID = 7131488143028746955L;

    private JPanel mainPane = null;

    private JPanel promptPane = null;

    private JPanel buttonPane = null;

    private JButton switchButton = null;

    private JButton cancelButton = null;

    private JLabel contestPasswordLabel = null;

    private JPasswordField contestPasswordTextField = null;

    private Profile profile = null;
    
    private SwitchProfileStatusFrame statusFrame = null;
    

    /**
     * This method initializes
     * 
     */
    public SwitchProfileConfirmPane() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     * 
     */
    private void initialize() {
        this.setLayout(new BorderLayout());
        this.setSize(new Dimension(385, 186));
        this.add(getMainPane(), BorderLayout.CENTER);

    }

    @Override
    public void setContestAndController(IInternalContest inContest, IInternalController inController) {
        super.setContestAndController(inContest, inController);
        getStatusFrame().setContestAndController(inContest, inController);
    }
    
    @Override
    public String getPluginTitle() {
        return "Switch Profile Confirm Pane";
    }

    /**
     * This method initializes mainPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getMainPane() {
        if (mainPane == null) {
            mainPane = new JPanel();
            mainPane.setLayout(new BorderLayout());
            mainPane.add(getButtonPane(), BorderLayout.SOUTH);
            mainPane.add(getPromptPane(), BorderLayout.CENTER);
        }
        return mainPane;
    }

    /**
     * This method initializes promptPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getPromptPane() {
        if (promptPane == null) {
            contestPasswordLabel = new JLabel();
            contestPasswordLabel.setBounds(new Rectangle(17, 12, 348, 31));
            contestPasswordLabel.setFont(new Font("Dialog", Font.BOLD, 14));
            contestPasswordLabel.setHorizontalAlignment(SwingConstants.CENTER);
            contestPasswordLabel.setText("Enter Contest Password");
            promptPane = new JPanel();
            promptPane.setLayout(null);
            promptPane.add(contestPasswordLabel, null);
            promptPane.add(getContestPasswordTextField(), null);
        }
        return promptPane;
    }

    /**
     * This method initializes buttonPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getButtonPane() {
        if (buttonPane == null) {
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setHgap(45);
            flowLayout.setAlignment(FlowLayout.CENTER);
            buttonPane = new JPanel();
            buttonPane.setLayout(flowLayout);
            buttonPane.add(getSwitchButton(), null);
            buttonPane.add(getCancelButton(), null);
        }
        return buttonPane;
    }

    /**
     * This method initializes switchButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getSwitchButton() {
        if (switchButton == null) {
            switchButton = new JButton();
            switchButton.setPreferredSize(new Dimension(100, 26));
            switchButton.setMnemonic(KeyEvent.VK_S);
            switchButton.setText("Switch");
            switchButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    switchProfile();
                }
            });
        }
        return switchButton;
    }

    /**
     * This method initializes cancelButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getCancelButton() {
        if (cancelButton == null) {
            cancelButton = new JButton();
            cancelButton.setPreferredSize(new Dimension(100, 26));
            cancelButton.setMnemonic(KeyEvent.VK_C);
            cancelButton.setText("Cancel");
            cancelButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    closeWindow();
                }
            });
        }
        return cancelButton;
    }

    protected void closeWindow() {
        getParentFrame().setVisible(false);
    }

    /**
     * This method initializes contestPasswordTextField
     * 
     * @return javax.swing.JPasswordField
     */
    private JPasswordField getContestPasswordTextField() {
        if (contestPasswordTextField == null) {
            contestPasswordTextField = new JPasswordField();
            contestPasswordTextField.setBounds(new Rectangle(90, 64, 202, 30));
            contestPasswordTextField.setFont(new Font("Dialog", Font.PLAIN, 14));
            contestPasswordTextField.addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyPressed(java.awt.event.KeyEvent e) {
                    if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                        switchProfile();
                    }
                }
            });
        }
        return contestPasswordTextField;
    }
    
    /**
     * Switch profile or if multi site show them the status frame.
     */
    protected void switchProfile() {

        String password = new String(getContestPasswordTextField().getPassword());

        ClientId [] servers = getContest().getAllLoggedInClients(Type.SERVER);
        
        if (servers.length == 0){
            getController().switchProfile(getContest().getProfile(), profile, password);
            closeWindow();
            
        } else {
            getStatusFrame().setProfile(profile);
            getStatusFrame().setNewContestPassword(password);
            getStatusFrame().setCurrentContestPassword(getContest().getContestPassword());
            getStatusFrame().resetProfileStatusList();
            closeWindow();
            getStatusFrame().setVisible(true);
            
            getController().switchProfile(getContest().getProfile(), profile, password);
        }
    }
    
    
    public Profile getProfile() {
        return profile;
    }
    
    public void setProfile(Profile profile) {
        getContestPasswordTextField().setText("");
        this.profile = profile;
        contestPasswordLabel.setText("Enter Contest Password for "+profile.getName());
        getContestPasswordTextField().requestFocus();
    }

    public SwitchProfileStatusFrame getStatusFrame() {
        if (statusFrame == null){
            statusFrame = new SwitchProfileStatusFrame();
        }
        return statusFrame;
    }
    
} // @jve:decl-index=0:visual-constraint="10,10"
