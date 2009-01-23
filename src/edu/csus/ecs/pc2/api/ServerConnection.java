package edu.csus.ecs.pc2.api;

import edu.csus.ecs.pc2.api.exceptions.LoginFailureException;
import edu.csus.ecs.pc2.api.exceptions.NotLoggedInException;
import edu.csus.ecs.pc2.api.implementation.Contest;
import edu.csus.ecs.pc2.api.listener.IConnectionEventListener;
import edu.csus.ecs.pc2.core.InternalController;
import edu.csus.ecs.pc2.core.model.IInternalContest;
import edu.csus.ecs.pc2.core.model.InternalContest;

/**
 * This class represents a connection to a PC<sup>2</sup> server. Instantiating the class creates a local {@link ServerConnection} object which can then be used to connect to the PC<sup>2</sup>
 * server via the {@link ServerConnection#login(String, String)} method. The PC<sup>2</sup> server must already be running, and the local client must have a <code>pc2v9.ini</code> file specifying
 * valid server connection information, prior to invoking {@link ServerConnection#login(String, String)} method.
 * 
 * <p>
 * This documentation describes the current <I>draft</i> of the PC<sup>2</sup> API, which is subject to change. 
 * 
 * @author pc2@ecs.csus.edu
 * @version $Id$

*/

// $HeadURL$
public class ServerConnection {

    private InternalController controller;

    private IInternalContest internalContest;

    private Contest contest = null;

    /**
     * Construct a local {@link ServerConnection} object which can subsequently be used to connect to a currently-running PC<sup>2</sup> server.
     * 
     */
    public ServerConnection() {
        super();
    }

    /**
     * Login to the PC<sup>2</sup> server represented by this ServerConnection using the specified login account name and password. If the login is successful, the method returns an
     * {@link edu.csus.ecs.pc2.api.IContest} object which can then be used to obtain information about the contest being controlled by the server. If the login fails the method throws
     * {@link edu.csus.ecs.pc2.api.exceptions.LoginFailureException}, in which case the message contained in the exception can be used to determine the nature of the login failure.
     * <P>
     * Note that invoking {@link ServerConnection#login(String, String)} causes an attempt to establish a network connection to a PC<sup>2</sup> server using the connection information specified in
     * the <code>pc2v9.ini</code> file in the current directory. The PC<sup>2</sup> server must <I>already be running</i> prior to invoking {@link ServerConnection#login(String, String)}, and
     * the <code>pc2v9.ini</code> must specify legitmate server connection information; otherwise, {@link edu.csus.ecs.pc2.api.exceptions.LoginFailureException} is thrown. See the PC<sup>2</sup>
     * Contest Administrator's Guide for information regarding specifying server connection information in <code>pc2v9.ini</code> files.
     * <P>
     * The following code snippet shows typical usage for connecting to and logging in to a PC<sup>2</sup> server. 
     * <A NAME="loginsample"></A>
     * 
     * <pre>
     * String login = &quot;team4&quot;;
     * String password = &quot;team4&quot;;
     * try {
     *     ServerConnection serverConnection = new ServerConnection();
     *     IContest contest = serverConnection.login(login, password);
     *     //... code here to invoke methods in &quot;contest&quot;;
     *     serverConnection.logoff();
     * } catch (LoginFailureException e) {
     *     System.out.println(&quot;Could not login because &quot; + e.getMessage());
     * }
     * </pre>
     * 
     * @param login
     *            client login name (for example: &quot;team5&quot; or &quot;judge3&quot;)
     * @param password
     *            password for the login name
     * @throws LoginFailureException
     *             if login fails, the message contained in the exception will provide and indication of the reason for the failure.
     */
    public IContest login(String login, String password) throws LoginFailureException {

        if (contest != null){
            throw new LoginFailureException("Already logged in as: "+contest.getMyClient().getLoginName());
        }
        internalContest = new InternalContest();
        controller = new InternalController(internalContest);

        controller.setUsingMainUI(false);
        controller.setClientAutoShutdown(false);
        try {
            controller.start(new String[0]);
            internalContest = controller.clientLogin(internalContest, login, password);

            contest = new Contest(internalContest,  controller, controller.getLog());
            
            contest.addConnectionListener(new ConnectionEventListenerImplementation());

            return contest;

        } catch (Exception e) {
            throw new LoginFailureException(e.getMessage());
        }
    }
    

    /**
     * Logoff/disconnect from the PC<sup>2</sup> server.
     * 
     * @return true if logged off, else false.
     * @throws NotLoggedInException if attempt to logoff without being logged in
     */
    public boolean logoff() throws NotLoggedInException {
            
        if (contest == null) {
            throw new NotLoggedInException("Can not log off, not logged in");
        }

        try {
            controller.logoffUser(internalContest.getClientId());
            contest.setLoggedIn(false);
            contest = null;
            return true;
        } catch (Exception e) {
            throw new NotLoggedInException(e);
        }
    }

    /**
     * Returns a IContest, if not connected to a server throws a NotLoggedInException.
     * 
     * @return contest
     * @throws NotLoggedInException if attempt to invoke this method without being logged in
     */
    public Contest getContest() throws NotLoggedInException {
        if (contest != null) {
            return contest;
        } else {
            throw new NotLoggedInException("Can not get IContest, not logged in");
        }
    }

    /**
     * Is this ServerConnection connected to a server ?
     * 
     * @return true if connected to server, false if not connected to server.
     */
    public boolean isLoggedIn() {
        return contest != null && contest.isLoggedIn();
    }

    /**
     * Returns a IClient if logged into a server.
     * 
     * @see IContest#getMyClient()
     * @return Client class
     * @throws NotLoggedInException if attempt to invoke this method without being logged in
     */
    public IClient getMyClient() throws NotLoggedInException {
        if (contest != null) {
            return contest.getMyClient();
        } else {
            throw new NotLoggedInException("Not logged in");
        }
    }
    
    /**
     * A Connection Event used by ServerConnection.
     * 
     * @author pc2@ecs.csus.edu
     * @version $Id$
     */
    protected class ConnectionEventListenerImplementation implements IConnectionEventListener {

        public void connectionDropped() {
            contest = null;
        }
    }

}
