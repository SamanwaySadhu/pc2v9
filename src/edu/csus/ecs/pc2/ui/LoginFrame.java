package edu.csus.ecs.pc2.ui;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;

/**
 * Login frame for all clients.
 * 
 * @author pc2@ecs.csus.edu
 */
// $HeadURL: http://pc2.ecs.csus.edu/repos/v9wip/trunk/src/edu/csus/ecs/pc2/core/gui/LoginFrame.java $
public class LoginFrame extends JFrame {

    /**
     * 
     */
    private static final long serialVersionUID = -6389607881992853161L;

    private JPanel centerPane = null;

    private JPasswordField passwordTextField = null;

    private JTextField loginTextField = null;

    private JLabel nameTitleLabel = null;

    private JLabel versionTitleLabel = null;

    private JLabel mainTitleTopLabel = null;

    private JLabel passwordTitleLabel = null;

    private JButton loginButton = null;

    private JButton exitButton = null;

    private JLabel messageLabel = null;

    private Runnable starterRunnable;

    private JLabel mainTitleBottomLabel = null;

    /**
     * This method initializes
     * 
     */
    public LoginFrame() {
        super();
        initialize();
        FrameUtilities.centerFrame(this);
    }

    /**
     * This method initializes this
     * 
     */
    private void initialize() {
        this.setSize(new java.awt.Dimension(429, 365));
        this.setTitle("PC^2 Login");
        this.setContentPane(getPasswordTitleLabel());
        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                System.exit(0);
            }
        });
    }

    /**
     * This method initializes jPanel
     * 
     * @return javax.swing.JPanel
     */
    private JPanel getPasswordTitleLabel() {
        if (centerPane == null) {
            GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
            gridBagConstraints11.insets = new java.awt.Insets(1, 21, 3, 24);
            gridBagConstraints11.gridx = 0;
            gridBagConstraints11.gridy = 1;
            gridBagConstraints11.ipadx = 167;
            gridBagConstraints11.ipady = 6;
            gridBagConstraints11.gridwidth = 2;
            GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
            gridBagConstraints10.insets = new java.awt.Insets(4, 5, 2, 5);
            gridBagConstraints10.gridx = 0;
            gridBagConstraints10.gridy = 7;
            gridBagConstraints10.ipadx = 411;
            gridBagConstraints10.ipady = 26;
            gridBagConstraints10.gridwidth = 2;
            GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
            gridBagConstraints9.insets = new java.awt.Insets(14, 29, 3, 91);
            gridBagConstraints9.gridy = 6;
            gridBagConstraints9.ipadx = 18;
            gridBagConstraints9.gridx = 1;
            GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
            gridBagConstraints8.insets = new java.awt.Insets(14, 80, 3, 53);
            gridBagConstraints8.gridy = 6;
            gridBagConstraints8.ipadx = 30;
            gridBagConstraints8.gridx = 0;
            GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
            gridBagConstraints7.insets = new java.awt.Insets(7, 80, 6, 28);
            gridBagConstraints7.gridy = 4;
            gridBagConstraints7.ipadx = 62;
            gridBagConstraints7.gridx = 0;
            GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
            gridBagConstraints5.insets = new java.awt.Insets(15, 21, 1, 24);
            gridBagConstraints5.gridx = 0;
            gridBagConstraints5.gridy = 0;
            gridBagConstraints5.ipadx = 202;
            gridBagConstraints5.ipady = 6;
            gridBagConstraints5.gridwidth = 2;
            GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
            gridBagConstraints3.insets = new java.awt.Insets(2, 5, 39, 11);
            gridBagConstraints3.gridx = 0;
            gridBagConstraints3.gridy = 8;
            gridBagConstraints3.ipadx = 257;
            gridBagConstraints3.gridwidth = 2;
            GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
            gridBagConstraints2.insets = new java.awt.Insets(4, 80, 5, 28);
            gridBagConstraints2.gridy = 2;
            gridBagConstraints2.ipadx = 75;
            gridBagConstraints2.gridx = 0;
            GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
            gridBagConstraints1.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints1.gridwidth = 2;
            gridBagConstraints1.gridx = 0;
            gridBagConstraints1.gridy = 3;
            gridBagConstraints1.ipadx = 244;
            gridBagConstraints1.weightx = 1.0;
            gridBagConstraints1.insets = new java.awt.Insets(6, 80, 6, 93);
            GridBagConstraints gridBagConstraints = new GridBagConstraints();
            gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
            gridBagConstraints.gridwidth = 2;
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 5;
            gridBagConstraints.ipadx = 246;
            gridBagConstraints.weightx = 1.0;
            gridBagConstraints.insets = new java.awt.Insets(7, 80, 13, 91);
            mainTitleBottomLabel = new JLabel();
            mainTitleBottomLabel.setHorizontalAlignment(SwingConstants.CENTER);
            mainTitleBottomLabel.setHorizontalTextPosition(SwingConstants.CENTER);
            mainTitleBottomLabel.setText("Contest Control System");
            mainTitleBottomLabel.setFont(new Font("Dialog", Font.BOLD, 18));
            messageLabel = new JLabel();
            messageLabel.setForeground(Color.red);
            messageLabel.setText("");
            messageLabel.setHorizontalAlignment(SwingConstants.CENTER);
            GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
            gridBagConstraints6.insets = new Insets(0, 24, 5, 20);
            gridBagConstraints6.gridx = 0;
            gridBagConstraints6.gridy = 1;
            gridBagConstraints6.ipadx = 168;
            gridBagConstraints6.ipady = 9;
            gridBagConstraints6.gridwidth = 2;
            mainTitleTopLabel = new JLabel();
            mainTitleTopLabel.setFont(new Font("Dialog", Font.BOLD, 18));
            mainTitleTopLabel.setText("Contest Control System");
            mainTitleTopLabel.setHorizontalAlignment(SwingConstants.CENTER);
            passwordTitleLabel = new JLabel();
            passwordTitleLabel.setText("Password");
            GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
            gridBagConstraints4.insets = new Insets(20, 22, 0, 23);
            gridBagConstraints4.gridx = 0;
            gridBagConstraints4.gridy = 0;
            gridBagConstraints4.ipadx = 202;
            gridBagConstraints4.ipady = 6;
            gridBagConstraints4.gridwidth = 2;
            mainTitleTopLabel.setFont(new Font("Dialog", Font.BOLD, 18));
            mainTitleTopLabel.setHorizontalTextPosition(SwingConstants.CENTER);
            mainTitleTopLabel.setText("CSUS Programming");
            mainTitleTopLabel.setHorizontalAlignment(SwingConstants.CENTER);
            versionTitleLabel = new JLabel();
            versionTitleLabel.setHorizontalAlignment(SwingConstants.RIGHT);
            versionTitleLabel.setText("Version XX. XX YYYY vv 22");
            nameTitleLabel = new JLabel();
            nameTitleLabel.setText("Name");
            nameTitleLabel.setPreferredSize(new java.awt.Dimension(45, 16));
            centerPane = new JPanel();
            centerPane.setLayout(new GridBagLayout());
            centerPane.add(getPasswordTextField(), gridBagConstraints);
            centerPane.add(getLoginTextField(), gridBagConstraints1);
            centerPane.add(nameTitleLabel, gridBagConstraints2);
            centerPane.add(versionTitleLabel, gridBagConstraints3);
            centerPane.add(mainTitleTopLabel, gridBagConstraints5);
            centerPane.add(passwordTitleLabel, gridBagConstraints7);
            centerPane.add(getLoginButton(), gridBagConstraints8);
            centerPane.add(getExitButton(), gridBagConstraints9);
            centerPane.add(messageLabel, gridBagConstraints10);
            centerPane.add(mainTitleBottomLabel, gridBagConstraints11);
            centerPane.add(mainTitleBottomLabel, gridBagConstraints6);
        }
        return centerPane;
    }

    /**
     * This method initializes jPasswordField
     * 
     * @return javax.swing.JPasswordField
     */
    private JPasswordField getPasswordTextField() {
        if (passwordTextField == null) {
            passwordTextField = new JPasswordField();
        }
        return passwordTextField;
    }

    /**
     * This method initializes jTextField
     * 
     * @return javax.swing.JTextField
     */
    private JTextField getLoginTextField() {
        if (loginTextField == null) {
            loginTextField = new JTextField();
        }
        return loginTextField;
    }

    /**
     * This method initializes jButton
     * 
     * @return javax.swing.JButton
     */
    private JButton getLoginButton() {
        if (loginButton == null) {
            loginButton = new JButton();
            loginButton.setMnemonic(KeyEvent.VK_L);
            loginButton.setText("Login");
            loginButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    attemptToLogin();
                }
            });
        }
        return loginButton;
    }

    protected void attemptToLogin() {

        setStatusMessage("");
        if (getLogin() == null || getLogin().length() < 1) {
            setStatusMessage("Please enter a login");
        } else {
            starterRunnable.run();
        }
    }

    /**
     * This method initializes jButton1
     * 
     * @return javax.swing.JButton
     */
    private JButton getExitButton() {
        if (exitButton == null) {
            exitButton = new JButton();
            exitButton.setMnemonic(KeyEvent.VK_X);
            exitButton.setText("Exit");
            exitButton.addActionListener(new java.awt.event.ActionListener() {
                public void actionPerformed(java.awt.event.ActionEvent e) {
                    promptBeforeExit();
                }
            });
        }
        return exitButton;
    }

    protected void promptBeforeExit() {

        setStatusMessage("");
        int result = FrameUtilities.yesNoCancelDialog("Are you sure you want to exit?", "Exit PC^2");

        if (result == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    public static void main(String[] args) {
        new LoginFrame().setVisible(true);
    }

    public void setRunnable(Runnable runnable) {
        this.starterRunnable = runnable;
    }

    /**
     * Display a message for the user.
     * 
     * @param messageString
     *            text to show to user
     */
    public void setStatusMessage(final String messageString) {

        Runnable messageRunnable = new Runnable() {
            public void run() {
                messageLabel.setText(messageString);
            }
        };
        SwingUtilities.invokeLater(messageRunnable);
    }

    /**
     * Fetch Login name for client.
     * 
     * @return the login name
     */
    public String getLogin() {
        return loginTextField.getText();
    }

    /**
     * fetch password for client.
     * 
     * @return the password
     */
    public String getPassword() {
        return new String(passwordTextField.getPassword());
    }

} // @jve:decl-index=0:visual-constraint="10,10"
