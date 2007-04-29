package edu.csus.ecs.pc2.ui.board;

import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import edu.csus.ecs.pc2.VersionInfo;
import edu.csus.ecs.pc2.core.IController;
import edu.csus.ecs.pc2.core.model.IModel;
import edu.csus.ecs.pc2.ui.FrameUtilities;
import edu.csus.ecs.pc2.ui.JPanePlugin;
import edu.csus.ecs.pc2.ui.LogWindow;
import edu.csus.ecs.pc2.ui.OptionsPanel;
import edu.csus.ecs.pc2.ui.StandingsPane;
import edu.csus.ecs.pc2.ui.UIPlugin;
import javax.swing.JTabbedPane;

/**
 * This class is the default scoreboard view (frame).
 * 
 * @author pc2@ecs.csus.edu
 * 
 */
// $HeadURL$
public class ScoreboardView extends JFrame implements UIPlugin {

    /**
     * 
     */
    private static final long serialVersionUID = -8071477348056424178L;

    // TODO remove @SuppressWarnings for model
    @SuppressWarnings("unused")
    private IModel model;

    // TODO remove @SuppressWarnings for controller
    @SuppressWarnings("unused")
    private IController controller;

    private LogWindow logWindow = null;

    private JTabbedPane mainTabbedPane = null;

    /**
     * This method initializes
     * 
     */
    public ScoreboardView() {
        super();
        initialize();
    }

    /**
     * This method initializes this
     * 
     */
    private void initialize() {
        this.setSize(new java.awt.Dimension(405, 296));
        this.setContentPane(getMainTabbedPane());
        this.setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        this.setTitle("Scoreboard");

        this.addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent e) {
                promptAndExit();
            }
        });

        FrameUtilities.centerFrame(this);
    }

    protected void promptAndExit() {
        int result = FrameUtilities.yesNoCancelDialog("Are you sure you want to exit PC^2?", "Exit PC^2");

        if (result == JOptionPane.YES_OPTION) {
            System.exit(0);
        }
    }

    public void setModelAndController(IModel inModel, IController inController) {
        this.model = inModel;
        this.controller = inController;

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                setTitle("PC^2 " + model.getTitle() + " Build " + new VersionInfo().getBuildNumber());
        
                if (logWindow == null) {
                    logWindow = new LogWindow();
                }
                logWindow.setModelAndController(model, controller);
                logWindow.setTitle("Log " + model.getClientId().toString());
        
                StandingsPane standingsPane = new StandingsPane();
                addUIPlugin(getMainTabbedPane(), "Standings", standingsPane);
                
                OptionsPanel optionsPanel = new OptionsPanel();
                addUIPlugin(getMainTabbedPane(), "Options", optionsPanel);
                optionsPanel.setLogWindow(logWindow);
        
                setVisible(true);
            }
        });
    }

    public String getPluginTitle() {
        return "Scoreboard View";
    }

    protected void addUIPlugin(JTabbedPane tabbedPane, String tabTitle, JPanePlugin plugin) {
        plugin.setModelAndController(model, controller);
        tabbedPane.add(plugin, tabTitle);
    }

    /**
     * This method initializes mainTabbedPane
     * 
     * @return javax.swing.JTabbedPane
     */
    private JTabbedPane getMainTabbedPane() {
        if (mainTabbedPane == null) {
            mainTabbedPane = new JTabbedPane();
        }
        return mainTabbedPane;
    }

} // @jve:decl-index=0:visual-constraint="10,10"
