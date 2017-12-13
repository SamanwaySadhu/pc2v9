package edu.csus.ecs.pc2.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;

import edu.csus.ecs.pc2.core.model.ContestInformation;
import edu.csus.ecs.pc2.core.model.Profile;
import edu.csus.ecs.pc2.profile.ProfileCloneSettings;
import javax.swing.BorderFactory;
import javax.swing.border.TitledBorder;
import java.awt.Font;
import java.awt.Color;

/**
 * Save/export profile settings.
 * 
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */

// $HeadURL$
public class ProfileSavePane extends JPanePlugin {

    /**
     * 
     */
    private static final long serialVersionUID = -6767667140640583963L;
    
    public static final String CLONE_BUTTON_NAME = "Clone"; // @jve:decl-index=0:

    public static final String EXPORT_BUTTON_NAME = "Export"; // @jve:decl-index=0:

    public static final String CREATE_BUTTON_NAME = "Ok"; // @jve:decl-index=0:

    private JPanel buttonPanel = null;

    private JButton saveButton = null;

    private JButton cancelButton = null;

    private JPanel centerPane = null;

    private JCheckBox resetContestTimeCheckBox = null;

    private JLabel profileNameLabel = null;

    private JLabel contestTitleLabel = null;

    private JTextField profileNameTextField = null;

    private JTextField contestTitleTextField = null;

    private JCheckBox copyRunsCheckBox = null;

    private JCheckBox copyLanguagesCheckBox = null;

    private JCheckBox copyProblemsCheckBox = null;

    private JPasswordField contestPasswordTextField = null;

    private JLabel contestPasswordLabel = null;

    private JTextField profileDescriptionTextField = null;

    private JLabel profileDescriptonLabel = null;

    private JPasswordField contestPasswordConfirmTextField = null;

    private JLabel confirmPasswordLabel = null;

    private JCheckBox copyClarificationsCheckBox = null;

    private JCheckBox copyAccountsCheckBox = null;

    private JCheckBox copyGroupsCheckBox = null;

    private JCheckBox copyJudgementsCheckbox = null;

    private JCheckBox copyContestSettingsCheckBox = null;

    private JPanel checkBoxPane = null;

    private JCheckBox copyNotificationsCheckBox = null;

    private JCheckBox copyCategoriesCheckBox = null;

    /**
     * This method initializes
     * 
     */
    public ProfileSavePane() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     * 
     */
    private void initialize() {
        this.setLayout(new BorderLayout());
        this.setSize(new Dimension(526, 560));
        this.add(getButtonPanel(), java.awt.BorderLayout.SOUTH);

        this.add(getCenterPane(), BorderLayout.CENTER);
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
            flowLayout.setVgap(5);
            buttonPanel = new JPanel();
            buttonPanel.setLayout(flowLayout);
            buttonPanel.add(getSaveButton(), null);
            buttonPanel.add(getCancelButton(), null);
        }
        return buttonPanel;
    }

    /**
     * This method initializes saveButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getSaveButton() {
        if (saveButton == null) {
            saveButton = new JButton();
            saveButton.setText(CLONE_BUTTON_NAME);
            saveButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    processSaveButton();
                }
            });
        }
        return saveButton;
    }

    protected void processSaveButton() {
        try {
            takeProfileAction(saveButton.getText());
        } catch (InvalidFieldValue e1) {
            JOptionPane.showMessageDialog(null, e1.getMessage());
        }

    }

    public void setSaveButtonName(String buttonNameText) {
        getSaveButton().setText(buttonNameText);
        populateGUI(getContest().getProfile());
    }

    private void clearAllSettings() {

        getProfileNameTextField().setText("");
        getProfileDescriptionTextField().setText("");
        getContestTitleTextField().setText("");
        getContestPasswordTextField().setText("");
        getContestPasswordConfirmTextField().setText("");

        setCheckBoxesSelected(false);
    }

    private void setCheckBoxesSelected(boolean selected) {

        getCopyAccountsCheckBox().setSelected(selected);
        getCopyCategoriesCheckBox().setSelected(selected);
        getCopyClarificationsCheckBox().setSelected(! selected);
        getCopyContestSettingsCheckBox().setSelected(selected);
        getCopyGroupsCheckBox().setSelected(selected);

        getCopyJudgementsCheckbox().setSelected(selected);

        // temporary fix for June Release 2017
        getCopyJudgementsCheckbox().setSelected(true);
        getCopyJudgementsCheckbox().setEnabled(false);
        
        getCopyLanguagesCheckBox().setSelected(selected);
        getCopyProblemsCheckBox().setSelected(! selected);
        getCopyRunsCheckBox().setSelected(! selected);
        getCopyNotificationsCheckBox().setSelected(selected);
        getResetContestTimeCheckBox().setSelected(selected);

        getResetContestTimeCheckBox().setSelected(selected);
    }

    private void setDefaultCloneSetting() {
        setCheckBoxesSelected(true);
    }

    protected void takeProfileAction(String actionText) {

        if (actionText.equalsIgnoreCase(CLONE_BUTTON_NAME)) {
            cloneProfile();
        } else if (actionText.equalsIgnoreCase(EXPORT_BUTTON_NAME)) {
            exportProfile();
        } else if (actionText.equalsIgnoreCase(CREATE_BUTTON_NAME)) {
            createNewProfile();
        } else {
            throw new InvalidFieldValue("Unknown Save button name: " + actionText);
        }

    }

    private void createNewProfile() {
        ProfileCloneSettings settings = getProfileCloneSettingsFromFields();

        settings.setCopyLanguages(false);
        settings.setCopyProblems(false);

        settings.setResetContestTimes(false);
        settings.setCopyAccounts(false);
        settings.setCopyContestSettings(false);
        settings.setCopyGroups(false);
        settings.setCopyJudgements(false);
        settings.setCopyNotifications(false);
        settings.setCopyRuns(false);
        settings.setCopyClarifications(false);
        settings.setCopyCategories(false);
        
        getController().cloneProfile(getContest().getProfile(), settings, false);
        closeWindow();

    }

    private void exportProfile() {

        throw new InvalidFieldValue("Export not implemented");
        // TODO Export Profile
        // closeWindow();
    }

    private void cloneProfile() {
        ProfileCloneSettings settings = getProfileCloneSettingsFromFields();
        
        if (isDupProfileName(settings.getName())) {
            throw new InvalidFieldValue("Profile named " + settings.getName() + " already exists");
        }

        if (settings.isCopyRuns()) {

            requireSetting(settings.isCopyProblems(), "Must copy problems to copy runs");

            requireSetting(settings.isCopyLanguages(), "Must copy languages to copy runs");

            requireSetting(settings.isCopyAccounts(), "Must copy accounts to copy runs");

            requireSetting(settings.isCopyJudgements(), "Must copy judgements to copy runs");
        }

        if (settings.isCopyClarifications()) {

            requireSetting(settings.isCopyProblems(), "Must copy problems to copy clarifications");

            requireSetting(settings.isCopyAccounts(), "Must copy accounts to copy clarifications");
        }
        
        getController().cloneProfile(getContest().getProfile(), settings, false);
        closeWindow();
    }

    private boolean isDupProfileName(String name) {
        for (Profile profile : getContest().getProfiles()) {
            if (name.trim().equals(profile.getName().trim())) {
                return true;
            }
        }
        return false;
    }

    /**
     * If not found throw InvalidFieldValue exception with message.
     * 
     * @param found
     * @param message
     * @throws InvalidFieldValue
     */
    private void requireSetting(boolean found, String message) {
        if (!found) {
            throw new InvalidFieldValue(message);
        }
    }

    private ProfileCloneSettings getProfileCloneSettingsFromFields() {

        String name = getProfileNameTextField().getText();
        
        checkField (name,"Enter a profile name" );
        
        String description = getProfileDescriptionTextField().getText();
        
        checkField (description, "Enter a profile description");

        String title = getContestTitleTextField().getText();
        
        checkField (title, "Enter a contest title");
        
        char[] password = getContestPasswordTextField().getPassword();
        char[] confirmPassword = getContestPasswordConfirmTextField().getPassword();

        if (password.length == 0) {
            throw new InvalidFieldValue("Contest password must be set");
        }

        if (password.length != confirmPassword.length){
            throw new InvalidFieldValue("Contest password does not match");
        }
        
        if (! (new String(password).equals(new String(confirmPassword)))){
            throw new InvalidFieldValue("Contest password and Confirm password do not match.");
        }

        ProfileCloneSettings settings = new ProfileCloneSettings(name, description, password, getContest().getProfile());
        
        settings.setContestTitle(title);
        settings.setCopyAccounts(getCopyAccountsCheckBox().isSelected());
        settings.setCopyClarifications(getCopyClarificationsCheckBox().isSelected());
        settings.setCopyCategories(getCopyCategoriesCheckBox().isSelected());
        settings.setCopyContestSettings(getCopyContestSettingsCheckBox().isSelected());
        settings.setCopyGroups(getCopyGroupsCheckBox().isSelected());

        // temporary fix for June Release 2017
        getCopyJudgementsCheckbox().setSelected(true);

        settings.setCopyJudgements(getCopyJudgementsCheckbox().isSelected());
        settings.setCopyLanguages(getCopyLanguagesCheckBox().isSelected());
        settings.setCopyNotifications(getCopyNotificationsCheckBox().isSelected());
        settings.setCopyProblems(getCopyProblemsCheckBox().isSelected());
        settings.setCopyRuns(getCopyRunsCheckBox().isSelected());

        settings.setResetContestTimes(getResetContestTimeCheckBox().isSelected());

        return settings;
    }

    /**
     * Check for empty or blank string throw exception if empty string. 
     * 
     * @param string
     * @param comment
     * @if string is null or empty 
     */
    private void checkField(String string, String comment)  {
        if (string == null || string.trim().length() == 0){
            throw new InvalidFieldValue(comment);
        }
    }

    private void showMessage(String string) {
        JOptionPane.showMessageDialog(this, string);
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

    @Override
    public String getPluginTitle() {
        return "ProfileSavePane";
    }

    /**
     * This method initializes centerPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getCenterPane() {
        if (centerPane == null) {
            confirmPasswordLabel = new JLabel();
            confirmPasswordLabel.setText("Confirm Password");
            confirmPasswordLabel.setBounds(new Rectangle(19, 152, 147, 23));
            confirmPasswordLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            profileDescriptonLabel = new JLabel();
            profileDescriptonLabel.setBounds(new Rectangle(19, 47, 147, 23));
            profileDescriptonLabel.setText("Description");
            profileDescriptonLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            contestPasswordLabel = new JLabel();
            contestPasswordLabel.setBounds(new Rectangle(19, 117, 147, 23));
            contestPasswordLabel.setText("Contest Password");
            contestPasswordLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            contestTitleLabel = new JLabel();
            contestTitleLabel.setBounds(new Rectangle(19, 82, 147, 23));
            contestTitleLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            contestTitleLabel.setText("Contest Title");
            profileNameLabel = new JLabel();
            profileNameLabel.setBounds(new Rectangle(19, 12, 147, 23));
            profileNameLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            profileNameLabel.setText("Profile Name");
            centerPane = new JPanel();
            centerPane.setLayout(null);
            centerPane.add(profileNameLabel, null);
            centerPane.add(contestTitleLabel, null);
            centerPane.add(getProfileNameTextField(), null);
            centerPane.add(getContestTitleTextField(), null);
            centerPane.add(getContestPasswordTextField(), null);
            centerPane.add(contestPasswordLabel, null);
            centerPane.add(getProfileDescriptionTextField(), null);
            centerPane.add(profileDescriptonLabel, null);
            centerPane.add(getContestPasswordConfirmTextField(), null);
            centerPane.add(confirmPasswordLabel, null);
            centerPane.add(getCheckBoxPane(), null);
            centerPane.add(getResetContestTimeCheckBox(), null);
        }
        return centerPane;
    }

    /**
     * This method initializes resetContestTimeCheckBox
     * 
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getResetContestTimeCheckBox() {
        if (resetContestTimeCheckBox == null) {
            resetContestTimeCheckBox = new JCheckBox();
            resetContestTimeCheckBox.setToolTipText("Set elapsed time to zero");
            resetContestTimeCheckBox.setSelected(true);
            resetContestTimeCheckBox.setSize(new Dimension(192, 21));
            resetContestTimeCheckBox.setLocation(new Point(28, 445));
            resetContestTimeCheckBox.setText("Reset Contest Times");
        }
        return resetContestTimeCheckBox;
    }

    /**
     * This method initializes profileNameTextField
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getProfileNameTextField() {
        if (profileNameTextField == null) {
            profileNameTextField = new JTextField();
            profileNameTextField.setPreferredSize(new Dimension(6, 22));
            profileNameTextField.setSize(new Dimension(273, 25));
            profileNameTextField.setLocation(new Point(184, 11));
        }
        return profileNameTextField;
    }

    /**
     * This method initializes contestTitleTextField
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getContestTitleTextField() {
        if (contestTitleTextField == null) {
            contestTitleTextField = new JTextField();
            contestTitleTextField.setBounds(new Rectangle(184, 81, 273, 25));
        }
        return contestTitleTextField;
    }

    /**
     * This method initializes copyRunsCheckBox
     * 
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getCopyRunsCheckBox() {
        if (copyRunsCheckBox == null) {
            copyRunsCheckBox = new JCheckBox();
            copyRunsCheckBox.setSelected(true);
            copyRunsCheckBox.setBounds(new Rectangle(33, 56, 132, 21));
            copyRunsCheckBox.setText("Runs");
        }
        return copyRunsCheckBox;
    }

    /**
     * This method initializes copyLanguagesCheckBox
     * 
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getCopyLanguagesCheckBox() {
        if (copyLanguagesCheckBox == null) {
            copyLanguagesCheckBox = new JCheckBox();
            copyLanguagesCheckBox.setSelected(true);
            copyLanguagesCheckBox.setBounds(new Rectangle(264, 19, 160, 21));
            copyLanguagesCheckBox.setText("Languages");
        }
        return copyLanguagesCheckBox;
    }

    /**
     * This method initializes copyProblemsCheckBox
     * 
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getCopyProblemsCheckBox() {
        if (copyProblemsCheckBox == null) {
            copyProblemsCheckBox = new JCheckBox();
            copyProblemsCheckBox.setSelected(false);
            copyProblemsCheckBox.setBounds(new Rectangle(33, 25, 132, 21));
            copyProblemsCheckBox.setText("Problems");
        }
        return copyProblemsCheckBox;
    }

    public void populateGUI(Profile profile) {

        getProfileNameTextField().setText("Copy of "+profile.getName());
        getProfileDescriptionTextField().setText(profile.getDescription());

        ContestInformation contestInformation = getContest().getContestInformation();
        String title = contestInformation.getContestTitle();
        if (title == null) {
            title = "";
        }
        getContestTitleTextField().setText(title);

        getContestPasswordTextField().setText("");
        getContestPasswordConfirmTextField().setText("");

        getCheckBoxPane().setVisible(true);
        getResetContestTimeCheckBox().setVisible(true);

        String buttonNameText = getSaveButton().getText();

        if (buttonNameText.equalsIgnoreCase(CLONE_BUTTON_NAME)) {
            setDefaultCloneSetting();
        } else if (buttonNameText.equalsIgnoreCase(EXPORT_BUTTON_NAME)) {
            setDefaultCloneSetting();
        } else if (buttonNameText.equalsIgnoreCase(CREATE_BUTTON_NAME)) {
            clearAllSettings();
            getCheckBoxPane().setVisible(false);
            getResetContestTimeCheckBox().setVisible(false);
        } else {
            showMessage("Unable to take action: " + buttonNameText);
        }
    }

    /**
     * This method initializes contestPasswordTextField
     * 
     * @return javax.swing.JTextField
     */
    private JPasswordField getContestPasswordTextField() {
        if (contestPasswordTextField == null) {
            contestPasswordTextField = new JPasswordField();
            contestPasswordTextField.setBounds(new Rectangle(184, 116, 124, 25));
        }
        return contestPasswordTextField;
    }

    protected void changeEchoChar(boolean hideText) {
        if (hideText) {
            getContestPasswordTextField().setEchoChar('*');
        } else {
            getContestPasswordTextField().setEchoChar((char) 0);
        }
    }

    /**
     * This method initializes profileDescriptionTextField
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getProfileDescriptionTextField() {
        if (profileDescriptionTextField == null) {
            profileDescriptionTextField = new JTextField();
            profileDescriptionTextField.setBounds(new Rectangle(184, 46, 273, 25));
        }
        return profileDescriptionTextField;
    }

    /**
     * This method initializes contestPasswordConfirmTextField
     * 
     * @return javax.swing.JPasswordField
     */
    private JPasswordField getContestPasswordConfirmTextField() {
        if (contestPasswordConfirmTextField == null) {
            contestPasswordConfirmTextField = new JPasswordField();
            contestPasswordConfirmTextField.setBounds(new Rectangle(184, 151, 124, 25));
            contestPasswordConfirmTextField.addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyPressed(java.awt.event.KeyEvent e) {
                    if (e.getKeyCode() == java.awt.event.KeyEvent.VK_ENTER) {
                        processSaveButton();
                    }
                }
            });
        }
        return contestPasswordConfirmTextField;
    }

    /**
     * This method initializes copyClarificationsCheckBox
     * 
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getCopyClarificationsCheckBox() {
        if (copyClarificationsCheckBox == null) {
            copyClarificationsCheckBox = new JCheckBox();
            copyClarificationsCheckBox.setText("Clarifications");
            copyClarificationsCheckBox.setBounds(new Rectangle(33, 84, 132, 24));
            copyClarificationsCheckBox.setSelected(true);
        }
        return copyClarificationsCheckBox;
    }

    /**
     * This method initializes copyAccountsCheckBox
     * 
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getCopyAccountsCheckBox() {
        if (copyAccountsCheckBox == null) {
            copyAccountsCheckBox = new JCheckBox();
            copyAccountsCheckBox.setText("Accounts");
            copyAccountsCheckBox.setBounds(new Rectangle(264, 50, 160, 24));
            copyAccountsCheckBox.setSelected(true);
        }
        return copyAccountsCheckBox;
    }

    /**
     * This method initializes copyGroupsCheckBox
     * 
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getCopyGroupsCheckBox() {
        if (copyGroupsCheckBox == null) {
            copyGroupsCheckBox = new JCheckBox();
            copyGroupsCheckBox.setText("Groups");
            copyGroupsCheckBox.setBounds(new Rectangle(264, 84, 160, 24));
            copyGroupsCheckBox.setSelected(true);
        }
        return copyGroupsCheckBox;
    }

    /**
     * This method initializes copyJudgementsCheckbox
     * 
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getCopyJudgementsCheckbox() {
        if (copyJudgementsCheckbox == null) {
            copyJudgementsCheckbox = new JCheckBox();
            copyJudgementsCheckbox.setText("Judgements");
            copyJudgementsCheckbox.setBounds(new Rectangle(264, 118, 160, 24));
            copyJudgementsCheckbox.setSelected(true);
        }
        return copyJudgementsCheckbox;
    }

    /**
     * This method initializes copyContestSettingsCheckBox
     * 
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getCopyContestSettingsCheckBox() {
        if (copyContestSettingsCheckBox == null) {
            copyContestSettingsCheckBox = new JCheckBox();
            copyContestSettingsCheckBox.setText("Contest settings");
            copyContestSettingsCheckBox.setBounds(new Rectangle(264, 186, 160, 24));
            copyContestSettingsCheckBox.setSelected(true);
        }
        return copyContestSettingsCheckBox;
    }

    /**
     * This method initializes checkBoxPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getCheckBoxPane() {
        if (checkBoxPane == null) {
            checkBoxPane = new JPanel();
            checkBoxPane.setLayout(null);
            checkBoxPane.setBounds(new Rectangle(3, 186, 477, 255));
            checkBoxPane.setBorder(BorderFactory.createTitledBorder(null, "Copy:", TitledBorder.DEFAULT_JUSTIFICATION, TitledBorder.DEFAULT_POSITION, new Font("Dialog", Font.BOLD, 12), new Color(51,
                    51, 51)));
            checkBoxPane.add(getCopyLanguagesCheckBox(), null);
            checkBoxPane.add(getCopyProblemsCheckBox(), null);
            checkBoxPane.add(getCopyClarificationsCheckBox(), null);
            checkBoxPane.add(getCopyRunsCheckBox(), null);
            checkBoxPane.add(getCopyContestSettingsCheckBox(), null);
            checkBoxPane.add(getCopyJudgementsCheckbox(), null);
            // temporary fix for June Release 2017
            getCopyJudgementsCheckbox().setSelected(true);
            getCopyJudgementsCheckbox().setEnabled(false);

            checkBoxPane.add(getCopyGroupsCheckBox(), null);
            checkBoxPane.add(getCopyAccountsCheckBox(), null);
            checkBoxPane.add(getCopyNotificationsCheckBox(), null);
            checkBoxPane.add(getCopyCategoriesCheckBox(), null);
        }
        return checkBoxPane;
    }

    /**
     * This method initializes copyNotificationsCheckBox
     * 
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getCopyNotificationsCheckBox() {
        if (copyNotificationsCheckBox == null) {
            copyNotificationsCheckBox = new JCheckBox();
            copyNotificationsCheckBox.setBounds(new Rectangle(264, 152, 160, 24));
            copyNotificationsCheckBox.setText("Notifications");
            copyNotificationsCheckBox.setSelected(true);
        }
        return copyNotificationsCheckBox;
    }

    /**
     * This method initializes copyCategoriesCheckBox
     * 
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getCopyCategoriesCheckBox() {
        if (copyCategoriesCheckBox == null) {
            copyCategoriesCheckBox = new JCheckBox();
            copyCategoriesCheckBox.setBounds(new Rectangle(264, 220, 160, 21));
            copyCategoriesCheckBox.setSelected(true);
            copyCategoriesCheckBox.setText("Categories");
        }
        return copyCategoriesCheckBox;
    }

} // @jve:decl-index=0:visual-constraint="10,10"
