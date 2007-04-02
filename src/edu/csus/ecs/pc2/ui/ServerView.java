package edu.csus.ecs.pc2.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import edu.csus.ecs.pc2.VersionInfo;
import edu.csus.ecs.pc2.core.IController;
import edu.csus.ecs.pc2.core.model.Account;
import edu.csus.ecs.pc2.core.model.AccountEvent;
import edu.csus.ecs.pc2.core.model.IAccountListener;
import edu.csus.ecs.pc2.core.model.ClientType;
import edu.csus.ecs.pc2.core.model.ILoginListener;
import edu.csus.ecs.pc2.core.model.IModel;
import edu.csus.ecs.pc2.core.model.LoginEvent;
import edu.csus.ecs.pc2.core.model.RunEvent;
import edu.csus.ecs.pc2.core.model.IRunListener;

/**
 * GUI for Server.
 * 
 * @author pc2@ecs.csus.edu
 */

// $HeadURL$
public class ServerView extends JFrame implements UIPlugin {

    public static final String SVN_ID = "$Id$";

    private IModel model = null;

    // TODO remove @SuppressWarnings for serverController
    @SuppressWarnings("unused")
    private IController serverController = null;

    /**
     * 
     */
    private static final long serialVersionUID = 4547574494017009634L;

    private JPanel mainViewPane = null;

    private JPanel runPane = null;

    private JScrollPane runScrollPane = null;

    private JList runJList = null;

    private DefaultListModel runListModel = new DefaultListModel();

    private JTabbedPane mainTabbedPane = null;

    private JPanel generateAccountsPane = null;

    private JTextField judgeCountTextField = null;

    private JPanel genButtonPane = null;

    private JButton genButton = null;

    private JTextField teamCountTextField = null;

    private JTextField boardCountTextField = null;

    private JTextField adminCountTextField = null;

    private JPanel centerPane2 = null;

    private JLabel genAdminLabel = null;

    private JLabel genJudgeLabel = null;

    private JLabel genTeamLabels = null;

    private JLabel genScoreboardLabel = null;

    public ServerView(IModel model, IController serverController) {
        super();
        setModelController(model, serverController);
        model.addRunListener(new RunListenerImplementation());
        model.addAccountListener(new AccountListenerImplementation());
        model.addLoginListener(new LoginListenerImplementation());
        initialize();
        
        updateListBox (getPluginTitle()+" Build "+new VersionInfo().getBuildNumber());
    }

    /**
     * This method initializes
     * 
     */
    public ServerView() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     * 
     */
    private void initialize() {
        this.setSize(new java.awt.Dimension(518, 327));
        this.setTitle("Server View");
        this.setContentPane(getMainViewPane());
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                promptAndExit();
            }
        });
        setVisible(true);
        
        FrameUtilities.centerFrameTop(this);
    }

    protected void promptAndExit() {
        int result = FrameUtilities.yesNoCancelDialog("Are you sure you want to exit PC^2?", "Exit PC^2");

        if (result == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    private void updateListBox(final String messageString) {
        Runnable messageRunnable = new Runnable() {
            public void run() {
                runListModel.addElement(messageString);
                System.out.println("Box: " + messageString);
            }
        };
        SwingUtilities.invokeLater(messageRunnable);
    }

    /**
     * Implementation for the Run Listener.
     * 
     * @author pc2@ecs.csus.edu
     * 
     */
    private class RunListenerImplementation implements IRunListener {

        public void runAdded(RunEvent event) {
            updateListBox(event.getRun() + " ADDED ");
        }

        public void runChanged(RunEvent event) {
            updateListBox(event.getRun() + " CHANGED ");
        }

        public void runRemoved(RunEvent event) {
            updateListBox(event.getRun() + " REMOVED ");
        }
    }

    private String accountText(Account account) {
        if (account == null) {
            return "null";
        } else {
            return account.getClientId().toString();
        }
    }
    
    /**
     * 
     * @author pc2@ecs.csus.edu
     *
     */
    public class LoginListenerImplementation implements ILoginListener {

        public void loginAdded(LoginEvent event) {
            updateListBox("Login " + event.getAction() + " " + event.getClientId());
        }

        public void loginRemoved(LoginEvent event) {
            updateListBox("Login " + event.getAction() + " " + event.getClientId());
        }

    }

    /**
     * Implementation for a Account Listener.
     * 
     * @author pc2@ecs.csus.edu
     * 
     */
    public class AccountListenerImplementation implements IAccountListener {

        public void accountAdded(AccountEvent accountEvent) {
            updateListBox("Account " + accountEvent.getAction() + " " + accountText(accountEvent.getAccount()));

        }

        public void accountModified(AccountEvent accountEvent) {
            updateListBox("Account " + accountEvent.getAction() + " " + accountText(accountEvent.getAccount()));

        }

    }

    /**
     * This method initializes mainViewPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getMainViewPane() {
        if (mainViewPane == null) {
            mainViewPane = new JPanel();
            mainViewPane.setLayout(new BorderLayout());
            mainViewPane.add(getMainTabbedPane(), java.awt.BorderLayout.CENTER);
        }
        return mainViewPane;
    }

    /**
     * This method initializes runPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getRunPane() {
        if (runPane == null) {
            runPane = new JPanel();
            runPane.setLayout(new BorderLayout());
            runPane.setBorder(javax.swing.BorderFactory.createTitledBorder(null, "Runs",
                    javax.swing.border.TitledBorder.DEFAULT_JUSTIFICATION, javax.swing.border.TitledBorder.DEFAULT_POSITION, null,
                    null));
            runPane.add(getRunScrollPane(), java.awt.BorderLayout.CENTER);
        }
        return runPane;
    }

    /**
     * This method initializes runScrollPane
     * 
     * @return javax.swing.JScrollPane
     */
    private JScrollPane getRunScrollPane() {
        if (runScrollPane == null) {
            runScrollPane = new JScrollPane();
            runScrollPane.setViewportView(getRunJList());
        }
        return runScrollPane;
    }

    /**
     * This method initializes runJList
     * 
     * @return javax.swing.JList
     */
    private JList getRunJList() {
        if (runJList == null) {
            runJList = new JList(runListModel);
        }
        return runJList;
    }

    /**
     * Puts this frame to right of input frame.
     * 
     * @param sourceFrame
     */
    public void windowToRight(JFrame sourceFrame) {
        int rightX = sourceFrame.getX() + sourceFrame.getWidth();
        setLocation(rightX, getY());
    }

    /**
     * This method initializes mainTabbedPane
     * 
     * @return javax.swing.JTabbedPane
     */
    private JTabbedPane getMainTabbedPane() {
        if (mainTabbedPane == null) {
            mainTabbedPane = new JTabbedPane();
            mainTabbedPane.addTab("Runs Submitted", null, getRunPane(), null);
            mainTabbedPane.addTab("Generate Accounts", null, getGenerateAccountsPane(), null);
        }
        return mainTabbedPane;
    }

    /**
     * This method initializes generateAccountsPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getGenerateAccountsPane() {
        if (generateAccountsPane == null) {
            generateAccountsPane = new JPanel();
            generateAccountsPane.setLayout(new BorderLayout());
            generateAccountsPane.add(getGenButtonPane(), java.awt.BorderLayout.SOUTH);
            generateAccountsPane.add(getCenterPane2(), java.awt.BorderLayout.CENTER);
        }
        return generateAccountsPane;
    }

    /**
     * This method initializes adminAccountNumber
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getJudgeCountTextField() {
        if (judgeCountTextField == null) {
            judgeCountTextField = new JTextField();
            judgeCountTextField.setBounds(new java.awt.Rectangle(356, 65, 52, 31));
            judgeCountTextField.setDocument(new IntegerDocument());
        }
        return judgeCountTextField;
    }

    /**
     * This method initializes genButtonPane
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getGenButtonPane() {
        if (genButtonPane == null) {
            FlowLayout flowLayout = new FlowLayout();
            flowLayout.setAlignment(java.awt.FlowLayout.RIGHT);
            genButtonPane = new JPanel();
            genButtonPane.setLayout(flowLayout);
            genButtonPane.add(getGenButton(), null);
        }
        return genButtonPane;
    }

    /**
     * This method initializes genButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getGenButton() {
        if (genButton == null) {
            genButton = new JButton();
            genButton.setText("Generate");
            genButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {

                    generateAccounts();
                }
            });
        }
        return genButton;
    }

    private int getIntegerValue(String s) {
        try {
            return Integer.parseInt(s);
        } catch (Exception e) {
            return 0;
        }
    }

    protected void generateAccounts() {

        try {
            int count = getIntegerValue(adminCountTextField.getText());
            if (count > 0) {
                model.generateNewAccounts(ClientType.Type.ADMINISTRATOR.toString(), count, true);
            }

            count = getIntegerValue(judgeCountTextField.getText());
            if (count > 0) {
                model.generateNewAccounts(ClientType.Type.JUDGE.toString(), count, true);
            }

            count = getIntegerValue(teamCountTextField.getText());
            if (count > 0) {
                model.generateNewAccounts(ClientType.Type.TEAM.toString(), count, true);
            }

            count = getIntegerValue(boardCountTextField.getText());
            if (count > 0) {
                model.generateNewAccounts(ClientType.Type.SCOREBOARD.toString(), count, true);
            }

        } catch (Exception e) {
            // TODO: handle exception
            e.printStackTrace();
        }
        
        updateGenerateTitles();
    }

    private void updateGenerateTitles() {

        // Update the Number of accounts

        int number = model.getAccounts(ClientType.Type.SCOREBOARD).size();
        genScoreboardLabel.setText("Scoreboards (" + number + ")");

        number = model.getAccounts(ClientType.Type.TEAM).size();
        genTeamLabels.setText("Teams (" + number + ")");

        number = model.getAccounts(ClientType.Type.JUDGE).size();
        genJudgeLabel.setText("Judges (" + number + ")");

        number = model.getAccounts(ClientType.Type.ADMINISTRATOR).size();
        genAdminLabel.setText("Administrators (" + number + ")");
    }

    /**
     * This method initializes jTextField
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getTeamCountTextField() {
        if (teamCountTextField == null) {
            teamCountTextField = new JTextField();
            teamCountTextField.setBounds(new java.awt.Rectangle(356, 115, 52, 31));
            teamCountTextField.setDocument(new IntegerDocument());
        }
        return teamCountTextField;
    }

    /**
     * This method initializes jTextField1
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getBoardCountTextField() {
        if (boardCountTextField == null) {
            boardCountTextField = new JTextField();
            boardCountTextField.setBounds(new java.awt.Rectangle(356, 165, 52, 31));
            boardCountTextField.setDocument(new IntegerDocument());
        }
        return boardCountTextField;
    }

    /**
     * This method initializes jTextField3
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getAdminCountTextField() {
        if (adminCountTextField == null) {
            adminCountTextField = new JTextField();
            adminCountTextField.setBounds(new java.awt.Rectangle(356, 15, 52, 31));
            adminCountTextField.setDocument(new IntegerDocument());
        }
        return adminCountTextField;
    }

    /**
     * This method initializes centerPane2
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getCenterPane2() {
        if (centerPane2 == null) {
            genScoreboardLabel = new JLabel();
            genScoreboardLabel.setBounds(new java.awt.Rectangle(62, 165, 257, 31));
            genScoreboardLabel.setText("Scoreboards");
            genTeamLabels = new JLabel();
            genTeamLabels.setBounds(new java.awt.Rectangle(62, 115, 257, 31));
            genTeamLabels.setText("Teams");
            genJudgeLabel = new JLabel();
            genJudgeLabel.setBounds(new java.awt.Rectangle(62, 65, 257, 31));
            genJudgeLabel.setText("Judges");
            genAdminLabel = new JLabel();
            genAdminLabel.setBounds(new java.awt.Rectangle(62, 15, 257, 31));
            genAdminLabel.setText("Administrators");
            centerPane2 = new JPanel();
            centerPane2.setLayout(null);
            centerPane2.add(getAdminCountTextField(), null);
            centerPane2.add(getBoardCountTextField(), null);
            centerPane2.add(getTeamCountTextField(), null);
            centerPane2.add(getJudgeCountTextField(), null);
            centerPane2.add(genAdminLabel, null);
            centerPane2.add(genJudgeLabel, null);
            centerPane2.add(genTeamLabels, null);
            centerPane2.add(genScoreboardLabel, null);
        }
        return centerPane2;
    }

    public void setModelController(IModel inModel, IController inController) {
        this.model = inModel;
        this.serverController = inController;
        setTitle("PC^2 Server (Site "+model.getSiteNumber()+")");
        updateGenerateTitles();
    }

    public String getPluginTitle() {
        return "Server Main GUI";
    }

} // @jve:decl-index=0:visual-constraint="10,10"
