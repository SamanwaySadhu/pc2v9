package edu.csus.ecs.pc2.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.KeyEvent;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.GregorianCalendar;
import java.util.Properties;
import java.util.Set;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

import edu.csus.ecs.pc2.core.CommandVariableReplacer;
import edu.csus.ecs.pc2.core.IInternalController;
import edu.csus.ecs.pc2.core.model.ContestInformation;
import edu.csus.ecs.pc2.core.model.ContestInformation.TeamDisplayMask;
import edu.csus.ecs.pc2.core.model.ContestInformationEvent;
import edu.csus.ecs.pc2.core.model.IContestInformationListener;
import edu.csus.ecs.pc2.core.model.IInternalContest;
import edu.csus.ecs.pc2.core.scoring.DefaultScoringAlgorithm;

/**
 * Contest Information edit/update Pane.
 * 
 * Update contest information.
 * 
 * @author pc2@ecs.csus.edu
 */
public class ContestInformationPane extends JPanePlugin {

    private static final long serialVersionUID = -8408469113380938482L;

    private JPanel buttonPanel = null;

    private JPanel centerPane = null;

    private JButton updateButton = null;

    private JLabel contestTitleLabel = null;

    private JTextField contestTitleTextField = null;

    private JPanel teamDisplaySettingPane = null;

    private JRadioButton displayNoneRadioButton = null;

    private JRadioButton displayNumbersOnlyRadioButton = null;

    private JRadioButton displayNameAndNumberRadioButton = null;

    private JRadioButton displayAliasNameRadioButton = null;

    private JRadioButton displayNamesOnlyRadioButton = null;

    private ButtonGroup displayNameButtonGroup = null; // @jve:decl-index=0:visual-constraint="617,62"

    private JButton cancelButton = null;

    private JTextField judgesDefaultAnswerTextField = null;

    private JLabel judgesDefaultAnswerLabel = null;

    private JCheckBox jCheckBoxShowPreliminaryOnBoard = null;

    private JCheckBox jCheckBoxShowPreliminaryOnNotifications = null;

    private JCheckBox additionalRunStatusCheckBox = null;

    private ContestInformation savedContestInformation = null; // @jve:decl-index=0:

    private JLabel labelMaxFileSize = null;

    private JTextField maxFieldSizeInKTextField = null;

    private JButton scoringPropertiesButton = null;

    private Properties savedScoringProperties = null; // @jve:decl-index=0:

    private Properties changedScoringProperties = null;

    private JCheckBox ccsTestModeCheckbox = null;

    private JTextField runSubmissionInterfaceCommandTextField = null;

    private JLabel runSubmissionInterfaceLabel = null;

    private JCheckBox autoRegistrationCheckbox = null;
    
    private JTextField startTimeTextField;

    private JLabel startTimeLabel;
    private JTextField contestFreezeLengthtextField;

    private JCheckBox chckbxUnfrozen;

    /**
     * This method initializes
     * 
     */
    public ContestInformationPane() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     * 
     */
    private void initialize() {
        this.setLayout(new BorderLayout());
        this.setSize(new Dimension(641, 570));
        this.add(getCenterPane(), java.awt.BorderLayout.CENTER);
        this.add(getButtonPanel(), java.awt.BorderLayout.SOUTH);
    }

    /**
     * This method initializes buttonPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getButtonPanel() {
        if (buttonPanel == null) {
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setHgap(35);
            buttonPanel = new JPanel();
            buttonPanel.setLayout(flowLayout);
            buttonPanel.add(getUpdateButton(), null);
            buttonPanel.add(getCancelButton(), null);
        }
        return buttonPanel;
    }

    /**
     * This method initializes centerPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getCenterPane() {
        if (centerPane == null) {
            runSubmissionInterfaceLabel = new JLabel();
            runSubmissionInterfaceLabel.setBounds(new Rectangle(21, 257, 175, 27));
            runSubmissionInterfaceLabel.setHorizontalTextPosition(SwingConstants.TRAILING);
            runSubmissionInterfaceLabel.setText("Run Submission Command");
            runSubmissionInterfaceLabel.setToolTipText("CCS Run Submission Interface Command");
            runSubmissionInterfaceLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            labelMaxFileSize = new JLabel();
            labelMaxFileSize.setBounds(new Rectangle(21, 173, 200, 27));
            labelMaxFileSize.setHorizontalAlignment(SwingConstants.RIGHT);
            labelMaxFileSize.setText("Maximum output size (in kB)");
            contestTitleLabel = new JLabel();
            contestTitleLabel.setBounds(new java.awt.Rectangle(55, 21, 134, 27));
            contestTitleLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
            contestTitleLabel.setText("Contest Title");
            centerPane = new JPanel();
            centerPane.setToolTipText("");
            centerPane.setLayout(null);
            centerPane.add(contestTitleLabel, null);
            centerPane.add(getContestTitleTextField(), null);
            centerPane.add(getTeamDisplaySettingPane(), null);
            centerPane.add(getJudgesDefaultAnswerLabel(), null);
            centerPane.add(getJudgesDefaultAnswerTextField(), null);
            centerPane.add(getJCheckBoxShowPreliminaryOnBoard(), null);
            centerPane.add(getJCheckBoxShowPreliminaryOnNotifications(), null);
            centerPane.add(getAdditionalRunStatusCheckBox(), null);
            centerPane.add(labelMaxFileSize, null);
            centerPane.add(getMaxFieldSizeInKTextField(), null);
            centerPane.add(getScoringPropertiesButton(), null);
            centerPane.add(getCcsTestModeCheckbox(), null);
            centerPane.add(getRunSubmissionInterfaceCommandTextField(), null);
            centerPane.add(runSubmissionInterfaceLabel, null);
            centerPane.add(getAutoRegistrationCheckbox(), null);
            centerPane.add(getStartTimeLabel(), null);
            centerPane.add(getStartTimeTextField(), null);
            
            
  
            centerPane.add(getContestFreezeLengthtextField());
            
            JLabel contestFreezeTimeLabel = new JLabel();
            contestFreezeTimeLabel.setText("Contest Freeze Length");
            contestFreezeTimeLabel.setSize(new Dimension(175, 27));
            contestFreezeTimeLabel.setLocation(new Point(21, 215));
            contestFreezeTimeLabel.setHorizontalTextPosition(SwingConstants.TRAILING);
            contestFreezeTimeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            contestFreezeTimeLabel.setBounds(78, 475, 175, 27);
            centerPane.add(contestFreezeTimeLabel);
            
            centerPane.add(getChckbxUnfrozen());
        }
        return centerPane;
    }

    private JCheckBox getChckbxUnfrozen() {
        if (chckbxUnfrozen == null) {
            chckbxUnfrozen = new JCheckBox("Unfrozen");
            chckbxUnfrozen.setToolTipText("Unfreezing means the final results can be released to the public via the Contest API");
            chckbxUnfrozen.setBounds(431, 474, 139, 29);
            chckbxUnfrozen.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    enableUpdateButton();
                }
            });
        }
        return chckbxUnfrozen;
    }

    private JTextField getContestFreezeLengthtextField() {
        if (contestFreezeLengthtextField == null) {
            contestFreezeLengthtextField = new JTextField();
            contestFreezeLengthtextField.setBounds(new Rectangle(233, 172, 122, 29));
            contestFreezeLengthtextField.setBounds(270, 474, 122, 29);
            contestFreezeLengthtextField.addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyReleased(java.awt.event.KeyEvent e) {
                    enableUpdateButton();
                }
            });
        }
        return contestFreezeLengthtextField;
    }

    private JTextField getStartTimeTextField() {
        if (startTimeTextField == null) {
            startTimeTextField = new JTextField();
            startTimeTextField.setBounds(270, 424, 243, 31);
            startTimeTextField.setColumns(25);
            startTimeTextField.setEditable(false);
            startTimeTextField.setToolTipText("Use Contest Times tab \"Edit Start Schedule\" button to edit Start Time");
        }
        return startTimeTextField ;
    }

    private JLabel getStartTimeLabel() {
        if (startTimeLabel == null) {
            startTimeLabel = new JLabel("Scheduled Start Time");
            startTimeLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            startTimeLabel.setBounds(95, 428, 158, 27);
        }
        return startTimeLabel ;
    }

    /**
     * This method initializes updateButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getUpdateButton() {
        if (updateButton == null) {
            updateButton = new JButton();
            updateButton.setText("Update");
            updateButton.setToolTipText("Save settings");
            updateButton.setPreferredSize(new java.awt.Dimension(100, 26));
            updateButton.setMnemonic(java.awt.event.KeyEvent.VK_U);
            updateButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    updateContestInformation();
                }
            });
        }
        return updateButton;
    }

    /**
     * This method initializes contestTitleTextField
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getContestTitleTextField() {
        if (contestTitleTextField == null) {
            contestTitleTextField = new JTextField();
            contestTitleTextField.setBounds(new java.awt.Rectangle(204, 21, 340, 27));
            contestTitleTextField.addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyReleased(java.awt.event.KeyEvent e) {
                    enableUpdateButton();
                }
            });
        }
        return contestTitleTextField;
    }

    @Override
    public String getPluginTitle() {
        return "Contest Information Pane";
    }

    public void setContestAndController(IInternalContest inContest, IInternalController inController) {
        super.setContestAndController(inContest, inController);

        savedContestInformation = getContest().getContestInformation();
        populateGUI();

        setContestInformation(savedContestInformation);

        getContest().addContestInformationListener(new ContestInformationListenerImplementation());

    }

    protected ContestInformation getFromFields() {
        ContestInformation contestInformation = new ContestInformation();
        ContestInformation currentContestInformation = getContest().getContestInformation();
        if (currentContestInformation.getContestURL() != null) {
            contestInformation.setContestURL(new String(currentContestInformation.getContestURL()));
        }
        contestInformation.setContestTitle(getContestTitleTextField().getText());
        if (getDisplayNoneRadioButton().isSelected()) {
            contestInformation.setTeamDisplayMode(TeamDisplayMask.NONE);
        } else if (getDisplayNameAndNumberRadioButton().isSelected()) {
            contestInformation.setTeamDisplayMode(TeamDisplayMask.NUMBERS_AND_NAME);
        } else if (getDisplayNumbersOnlyRadioButton().isSelected()) {
            contestInformation.setTeamDisplayMode(TeamDisplayMask.LOGIN_NAME_ONLY);
        } else if (getDisplayNamesOnlyRadioButton().isSelected()) {
            contestInformation.setTeamDisplayMode(TeamDisplayMask.DISPLAY_NAME_ONLY);
        } else if (getDisplayAliasNameRadioButton().isSelected()) {
            contestInformation.setTeamDisplayMode(TeamDisplayMask.ALIAS);
        } else {
            // DEFAULT
            contestInformation.setTeamDisplayMode(TeamDisplayMask.LOGIN_NAME_ONLY);
        }
        contestInformation.setJudgesDefaultAnswer(getJudgesDefaultAnswerTextField().getText());
        contestInformation.setPreliminaryJudgementsTriggerNotifications(getJCheckBoxShowPreliminaryOnNotifications().isSelected());
        contestInformation.setPreliminaryJudgementsUsedByBoard(getJCheckBoxShowPreliminaryOnBoard().isSelected());
        contestInformation.setSendAdditionalRunStatusInformation(getAdditionalRunStatusCheckBox().isSelected());
        
        contestInformation.setRsiCommand(getRunSubmissionInterfaceCommandTextField().getText());
        contestInformation.setCcsTestMode(getCcsTestModeCheckbox().isSelected());
        contestInformation.setEnableAutoRegistration(getAutoRegistrationCheckbox().isSelected());

        String maxFileSizeString = "0" + getMaxFieldSizeInKTextField().getText();
        long maximumFileSize = Long.parseLong(maxFileSizeString);
        contestInformation.setMaxFileSize(maximumFileSize * 1000);

        if (savedContestInformation != null) {
            contestInformation.setJudgementNotificationsList(savedContestInformation.getJudgementNotificationsList());
                
            contestInformation.setJudgeCDPBasePath(savedContestInformation.getJudgeCDPBasePath());
            contestInformation.setScheduledStartDate(savedContestInformation.getScheduledStartDate());
            
            contestInformation.setAdminCDPBasePath(savedContestInformation.getAdminCDPBasePath());
            contestInformation.setContestShortName(savedContestInformation.getContestShortName());
            contestInformation.setExternalYamlPath(savedContestInformation.getExternalYamlPath());
            
            contestInformation.setFreezeTime(savedContestInformation.getFreezeTime());
            contestInformation.setLastRunNumberSubmitted(savedContestInformation.getLastRunNumberSubmitted());
            contestInformation.setAutoStartContest(savedContestInformation.isAutoStartContest());
        }

        contestInformation.setScoringProperties(changedScoringProperties);
        
        contestInformation.setFreezeTime(contestFreezeLengthtextField.getText());

        contestInformation.setThawed(getChckbxUnfrozen().isSelected());
        return (contestInformation);
    }

    protected void dumpProperties(String comment, Properties properties) {

        System.out.println("  Properties " + comment + " " + properties);
        if (properties == null) {
            return;
        }

        Set<Object> set = properties.keySet();

        String[] keys = (String[]) set.toArray(new String[set.size()]);

        Arrays.sort(keys);

        for (String key : keys) {
            System.out.println("     " + key + "='" + properties.get(key) + "'");
        }
    }

    protected void enableUpdateButton() {
        ContestInformation newChoice = getFromFields();

        if (getContest().getContestInformation().isSameAs(newChoice)) {
            setEnableButtons(false);
        } else {
            setEnableButtons(true);
        }
    }

    void setEnableButtons(boolean isEnabled) {
        getUpdateButton().setEnabled(isEnabled);
        getCancelButton().setEnabled(isEnabled);
    }

    private void populateGUI() {

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                ContestInformation contestInformation = getContest().getContestInformation();
                getContestTitleTextField().setText(contestInformation.getContestTitle());
                selectDisplayRadioButton();
                getJudgesDefaultAnswerTextField().setText(contestInformation.getJudgesDefaultAnswer());
                getJCheckBoxShowPreliminaryOnBoard().setSelected(contestInformation.isPreliminaryJudgementsUsedByBoard());
                getJCheckBoxShowPreliminaryOnNotifications().setSelected(contestInformation.isPreliminaryJudgementsTriggerNotifications());
                getAdditionalRunStatusCheckBox().setSelected(contestInformation.isSendAdditionalRunStatusInformation());
                getCcsTestModeCheckbox().setSelected(contestInformation.isCcsTestMode());
                getMaxFieldSizeInKTextField().setText((contestInformation.getMaxFileSize() / 1000) + "");
                getContestFreezeLengthtextField().setText(contestInformation.getFreezeTime());
                getRunSubmissionInterfaceCommandTextField().setText(contestInformation.getRsiCommand());
                if (contestInformation.getRsiCommand() == null || "".equals(contestInformation.getRsiCommand().trim())) {
                    String cmd = "# /usr/local/bin/sccsrs " + CommandVariableReplacer.OPTIONS + " " + CommandVariableReplacer.FILELIST;
                    getRunSubmissionInterfaceCommandTextField().setText(cmd);
                }
                getAutoRegistrationCheckbox().setSelected(contestInformation.isEnableAutoRegistration());
                
                //add the scheduled start time to the GUI
                GregorianCalendar cal = contestInformation.getScheduledStartTime();
                getStartTimeTextField().setText(getScheduledStartTimeStr(cal));   

                getChckbxUnfrozen().setSelected(contestInformation.isUnfrozen());
                setContestInformation(contestInformation);
                setEnableButtons(false);
            }
        });

    }
    
    /**
     * Convert a GregorianCalendar date/time to a displayable string in yyyy-mm-dd:hh:mm form.
     */
    private String getScheduledStartTimeStr(GregorianCalendar cal) {
        
        String retString = "<undefined>";
        if (cal != null) {
            //extract fields from input and build string
            //TODO:  need to deal with the difference between displaying LOCAL time and storing UTC

            SimpleDateFormat fmt = new SimpleDateFormat("yyyy-MM-dd:HH:mm");
            fmt.setCalendar(cal);
            retString = fmt.format(cal.getTime());

            }

        return retString;
    }


    private void updateContestInformation() {
        ContestInformation contestInformation = getFromFields();
        getController().updateContestInformation(contestInformation);
    }

    /**
     * 
     * @author pc2@ecs.csus.edu
     * 
     */
    class ContestInformationListenerImplementation implements IContestInformationListener {

        public void contestInformationAdded(ContestInformationEvent event) {
            populateGUI();
            savedContestInformation = event.getContestInformation();
        }

        public void contestInformationChanged(ContestInformationEvent event) {
            populateGUI();
            savedContestInformation = event.getContestInformation();

        }

        public void contestInformationRemoved(ContestInformationEvent event) {
            // TODO Auto-generated method stub

        }

        public void contestInformationRefreshAll(ContestInformationEvent contestInformationEvent) {
            populateGUI();
            savedContestInformation = getContest().getContestInformation();
            
        }
        
        public void finalizeDataChanged(ContestInformationEvent contestInformationEvent) {
            // Not used
        }

    }

    /**
     * This method initializes teamDisplaySettingPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getTeamDisplaySettingPane() {
        if (teamDisplaySettingPane == null) {
            teamDisplaySettingPane = new JPanel();
            teamDisplaySettingPane.setLayout(new FlowLayout());
            teamDisplaySettingPane.setBounds(new java.awt.Rectangle(111, 59, 381, 101));
            teamDisplaySettingPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Team Information Displayed to Judges", javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION,
                    javax.swing.border.TitledBorder.DEFAULT_POSITION, null, null));
            teamDisplaySettingPane.add(getDisplayNoneRadioButton(), null);
            teamDisplaySettingPane.add(getDisplayNumbersOnlyRadioButton(), null);
            teamDisplaySettingPane.add(getDisplayNamesOnlyRadioButton(), null);
            teamDisplaySettingPane.add(getDisplayNameAndNumberRadioButton(), null);
            teamDisplaySettingPane.add(getDisplayAliasNameRadioButton(), null);

        }
        return teamDisplaySettingPane;
    }

    private void selectDisplayRadioButton() {

        ContestInformation contestInformation = getContest().getContestInformation();
        if (contestInformation == null || contestInformation.getTeamDisplayMode() == null) {
            getDisplayNameButtonGroup().setSelected(getDisplayNamesOnlyRadioButton().getModel(), true);

        } else {
            switch (contestInformation.getTeamDisplayMode()) {
                case DISPLAY_NAME_ONLY:
                    getDisplayNameButtonGroup().setSelected(getDisplayNamesOnlyRadioButton().getModel(), true);
                    break;
                case LOGIN_NAME_ONLY:
                    getDisplayNameButtonGroup().setSelected(getDisplayNumbersOnlyRadioButton().getModel(), true);
                    break;
                case NUMBERS_AND_NAME:
                    getDisplayNameButtonGroup().setSelected(getDisplayNameAndNumberRadioButton().getModel(), true);
                    break;
                case ALIAS:
                    getDisplayNameButtonGroup().setSelected(getDisplayAliasNameRadioButton().getModel(), true);
                    break;
                case NONE:
                    getDisplayNameButtonGroup().setSelected(getDisplayNoneRadioButton().getModel(), true);
                    break;
                default:
                    break;
            }
        }
        // getDisplayNameButtonGroup().setSelected(getDisplayNoneButton(), false)

    }

    /**
     * This method initializes displayNoneButton
     * 
     * @return javax.swing.JRadioButton
     */
    private JRadioButton getDisplayNoneRadioButton() {
        if (displayNoneRadioButton == null) {
            displayNoneRadioButton = new JRadioButton();
            displayNoneRadioButton.setText("None");
            displayNoneRadioButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    // getActionCommand called with text from button
                    // getSource returns the JRadioButton
                    enableUpdateButton();
                }
            });
        }
        return displayNoneRadioButton;
    }

    /**
     * This method initializes displayNumbersOnlyRadioButton
     * 
     * @return javax.swing.JRadioButton
     */
    private JRadioButton getDisplayNumbersOnlyRadioButton() {
        if (displayNumbersOnlyRadioButton == null) {
            displayNumbersOnlyRadioButton = new JRadioButton();
            displayNumbersOnlyRadioButton.setText("Show Numbers Only");
            displayNumbersOnlyRadioButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    // getActionCommand called with text from button
                    // getSource returns the JRadioButton
                    enableUpdateButton();
                }
            });
        }
        return displayNumbersOnlyRadioButton;
    }

    /**
     * This method initializes displayNameAndNumberRadioButton
     * 
     * @return javax.swing.JRadioButton
     */
    private JRadioButton getDisplayNameAndNumberRadioButton() {
        if (displayNameAndNumberRadioButton == null) {
            displayNameAndNumberRadioButton = new JRadioButton();
            displayNameAndNumberRadioButton.setText("Show Number and Name");
            displayNameAndNumberRadioButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    // getActionCommand called with text from button
                    // getSource returns the JRadioButton
                    enableUpdateButton();
                }
            });
        }
        return displayNameAndNumberRadioButton;
    }

    /**
     * This method initializes displayAliasNameRadioButton
     * 
     * @return javax.swing.JRadioButton
     */
    private JRadioButton getDisplayAliasNameRadioButton() {
        if (displayAliasNameRadioButton == null) {
            displayAliasNameRadioButton = new JRadioButton();
            displayAliasNameRadioButton.setText("Show Alias");
            displayAliasNameRadioButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    // getActionCommand called with text from button
                    // getSource returns the JRadioButton
                    enableUpdateButton();
                }
            });
        }
        return displayAliasNameRadioButton;
    }

    /**
     * This method initializes showNamesOnlyRadioButton
     * 
     * @return javax.swing.JRadioButton
     */
    private JRadioButton getDisplayNamesOnlyRadioButton() {
        if (displayNamesOnlyRadioButton == null) {
            displayNamesOnlyRadioButton = new JRadioButton();
            displayNamesOnlyRadioButton.setText("Show Names only");
            displayNamesOnlyRadioButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    // getActionCommand called with text from button
                    // getSource returns the JRadioButton
                    enableUpdateButton();
                }
            });
        }
        return displayNamesOnlyRadioButton;
    }

    /**
     * This method initializes displayNameButtonGroup
     * 
     * @return javax.swing.ButtonGroup
     */
    private ButtonGroup getDisplayNameButtonGroup() {
        if (displayNameButtonGroup == null) {
            displayNameButtonGroup = new ButtonGroup();
            displayNameButtonGroup.add(getDisplayNoneRadioButton());

            displayNameButtonGroup.add(getDisplayNamesOnlyRadioButton());
            displayNameButtonGroup.add(getDisplayNameAndNumberRadioButton());
            displayNameButtonGroup.add(getDisplayNumbersOnlyRadioButton());
            displayNameButtonGroup.add(getDisplayAliasNameRadioButton());
        }
        return displayNameButtonGroup;
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
            cancelButton.setToolTipText("Discard changes");
            cancelButton.setMnemonic(KeyEvent.VK_C);
            cancelButton.setPreferredSize(new java.awt.Dimension(100, 26));
            cancelButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    populateGUI();
                }
            });
        }
        return cancelButton;
    }

    /**
     * This method initializes JudgesDefaultAnswerTextField
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getJudgesDefaultAnswerTextField() {
        if (judgesDefaultAnswerTextField == null) {
            judgesDefaultAnswerTextField = new JTextField();
            judgesDefaultAnswerTextField.setText("");
            judgesDefaultAnswerTextField.setSize(new Dimension(280, 29));
            judgesDefaultAnswerTextField.setLocation(new Point(208, 214));
            judgesDefaultAnswerTextField.addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyReleased(java.awt.event.KeyEvent e) {
                    enableUpdateButton();
                }
            });
        }
        return judgesDefaultAnswerTextField;
    }

    /**
     * This method initializes judgesDefaultAnswerLabel
     * 
     * @return javax.swing.JLabel
     */
    private JLabel getJudgesDefaultAnswerLabel() {
        if (judgesDefaultAnswerLabel == null) {
            judgesDefaultAnswerLabel = new JLabel();
            judgesDefaultAnswerLabel.setText("Judges' Default Answer");
            judgesDefaultAnswerLabel.setHorizontalTextPosition(javax.swing.SwingConstants.TRAILING);
            judgesDefaultAnswerLabel.setLocation(new Point(21, 215));
            judgesDefaultAnswerLabel.setSize(new java.awt.Dimension(175, 27));
            judgesDefaultAnswerLabel.setHorizontalAlignment(javax.swing.SwingConstants.RIGHT);
        }
        return judgesDefaultAnswerLabel;
    }

    /**
     * This method initializes jCheckBoxShowPreliminaryOnBoard
     * 
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getJCheckBoxShowPreliminaryOnBoard() {
        if (jCheckBoxShowPreliminaryOnBoard == null) {
            jCheckBoxShowPreliminaryOnBoard = new JCheckBox();
            jCheckBoxShowPreliminaryOnBoard.setBounds(new Rectangle(57, 334, 392, 21));
            jCheckBoxShowPreliminaryOnBoard.setHorizontalAlignment(SwingConstants.LEFT);
            jCheckBoxShowPreliminaryOnBoard.setMnemonic(KeyEvent.VK_UNDEFINED);
            jCheckBoxShowPreliminaryOnBoard.setText("Include Preliminary Judgements in Scoring Algorithm");
            jCheckBoxShowPreliminaryOnBoard.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    enableUpdateButton();
                }
            });
        }
        return jCheckBoxShowPreliminaryOnBoard;
    }

    /**
     * This method initializes jCheckBoxShowPreliminaryOnNotifications
     * 
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getJCheckBoxShowPreliminaryOnNotifications() {
        if (jCheckBoxShowPreliminaryOnNotifications == null) {
            jCheckBoxShowPreliminaryOnNotifications = new JCheckBox();
            jCheckBoxShowPreliminaryOnNotifications.setBounds(new Rectangle(57, 364, 392, 21));
            jCheckBoxShowPreliminaryOnNotifications.setHorizontalAlignment(SwingConstants.LEFT);
            jCheckBoxShowPreliminaryOnNotifications.setMnemonic(KeyEvent.VK_UNDEFINED);
            jCheckBoxShowPreliminaryOnNotifications.setText("Send Balloon Notifications for Preliminary Judgements");
            jCheckBoxShowPreliminaryOnNotifications.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    enableUpdateButton();
                }
            });
        }
        return jCheckBoxShowPreliminaryOnNotifications;
    }

    /**
     * This method initializes additionalRunStatusCheckBox
     * 
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getAdditionalRunStatusCheckBox() {
        if (additionalRunStatusCheckBox == null) {
            additionalRunStatusCheckBox = new JCheckBox();
            additionalRunStatusCheckBox.setHorizontalAlignment(SwingConstants.LEFT);
            additionalRunStatusCheckBox.setSize(new Dimension(392, 21));
            additionalRunStatusCheckBox.setLocation(new Point(57, 394));
            additionalRunStatusCheckBox.setText("Send Additional Run Status Information");
            additionalRunStatusCheckBox.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    enableUpdateButton();
                }
            });
        }
        return additionalRunStatusCheckBox;
    }

    public ContestInformation getContestInformation() {
        return savedContestInformation;
    }

    public void setContestInformation(ContestInformation contestInformation) {
        this.savedContestInformation = contestInformation;
        savedScoringProperties = contestInformation.getScoringProperties();
        if (savedScoringProperties == null) {
            savedScoringProperties = DefaultScoringAlgorithm.getDefaultProperties();
        }
        changedScoringProperties = savedScoringProperties;
    }

    /**
     * This method initializes maxFieldSizeInKTextField
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getMaxFieldSizeInKTextField() {
        if (maxFieldSizeInKTextField == null) {
            maxFieldSizeInKTextField = new JTextField();
            maxFieldSizeInKTextField.setDocument(new IntegerDocument());
            maxFieldSizeInKTextField.setBounds(new Rectangle(233, 172, 122, 29));
            maxFieldSizeInKTextField.addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyReleased(java.awt.event.KeyEvent e) {
                    enableUpdateButton();
                }
            });
        }
        return maxFieldSizeInKTextField;
    }

    /**
     * This method initializes scoringPropertiesButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getScoringPropertiesButton() {
        if (scoringPropertiesButton == null) {
            scoringPropertiesButton = new JButton();
            scoringPropertiesButton.setBounds(new Rectangle(370, 173, 200, 30));
            scoringPropertiesButton.setToolTipText("Edit Scoring Properties");
            scoringPropertiesButton.setMnemonic(KeyEvent.VK_S);
            scoringPropertiesButton.setText("Edit Scoring Properties");
            scoringPropertiesButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    showContestPropertiesEditor();
                }
            });
        }
        return scoringPropertiesButton;
    }

    protected void showContestPropertiesEditor() {

        PropertiesEditFrame propertiesEditFrame = new PropertiesEditFrame();
        propertiesEditFrame.setTitle("Edit Scoring Properties");
        propertiesEditFrame.setProperties(changedScoringProperties, new UpdateScoreProperties());
        propertiesEditFrame.setVisible(true);
    }

    /**
     * Update the edited properties.
     * 
     * @author pc2@ecs.csus.edu
     * @version $Id$
     */

    // $HeadURL$
    protected class UpdateScoreProperties implements IPropertyUpdater {

        public void updateProperties(Properties properties) {
            changedScoringProperties = properties;
            enableUpdateButton();
        }
    }

    /**
     * This method initializes ccsTestModeCheckbox
     * 
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getCcsTestModeCheckbox() {
        if (ccsTestModeCheckbox == null) {
            ccsTestModeCheckbox = new JCheckBox();
            ccsTestModeCheckbox.setBounds(new Rectangle(57, 298, 253, 24));
            ccsTestModeCheckbox.setHorizontalAlignment(SwingConstants.LEFT);
            ccsTestModeCheckbox.setText("C.C.S. Test Mode");
            ccsTestModeCheckbox.setMnemonic(KeyEvent.VK_T);
            ccsTestModeCheckbox.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    enableUpdateButton();
                }
            });
        }
        return ccsTestModeCheckbox;
    }

    /**
     * This method initializes runSubmissionInterfaceCommandTextField
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getRunSubmissionInterfaceCommandTextField() {
        if (runSubmissionInterfaceCommandTextField == null) {
            runSubmissionInterfaceCommandTextField = new JTextField();
            runSubmissionInterfaceCommandTextField.setBounds(new Rectangle(208, 256, 404, 29));
            runSubmissionInterfaceCommandTextField.setText("");
            runSubmissionInterfaceCommandTextField.addKeyListener(new java.awt.event.KeyAdapter() {
                public void keyReleased(java.awt.event.KeyEvent e) {
                    enableUpdateButton();
                }
            });
        }
        return runSubmissionInterfaceCommandTextField;
    }

    /**
     * This method initializes autoRegistrationCheckbox
     * 
     * @return javax.swing.JCheckBox
     */
    private JCheckBox getAutoRegistrationCheckbox() {
        if (autoRegistrationCheckbox == null) {
            autoRegistrationCheckbox = new JCheckBox();
            autoRegistrationCheckbox.setBounds(new Rectangle(372, 300, 243, 24));
            autoRegistrationCheckbox.setHorizontalAlignment(SwingConstants.LEFT);
            autoRegistrationCheckbox.setText("Auto Registration");
            autoRegistrationCheckbox.setMnemonic(KeyEvent.VK_A);
            autoRegistrationCheckbox.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    enableUpdateButton();
                }
            });
        }
        return autoRegistrationCheckbox;
    }
} // @jve:decl-index=0:visual-constraint="10,10"
