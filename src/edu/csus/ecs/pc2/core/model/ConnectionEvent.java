package edu.csus.ecs.pc2.core.model;

import edu.csus.ecs.pc2.core.transport.ConnectionHandlerID;

/**
 * Connection Event.
 * 
 * Events for when the transport connects or
 * disconnects from the server.
 * 
 * @author pc2@ecs.csus.edu
 * @version $Id$
 */

// $HeadURL$
public class ConnectionEvent {

    /**
     * Actions for ConnectionEvent
     * 
     * @author pc2@ecs.csus.edu
     */
    
    public enum Action {

        /**
         * Connection Established
         */
        ESTABLISHED,

        /**
         * Connection Dropped
         */
        DROPPED, 
        /**
         * Refresh all on change of profile or reset.
         */
        REFRESH_ALL,
    }

    private Action action;

    private ConnectionHandlerID connectionHandlerID;

    public ConnectionEvent(Action action, ConnectionHandlerID connectionHandlerID) {
        this.connectionHandlerID = connectionHandlerID;
        this.action = action;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public ConnectionHandlerID getConnectionHandlerID() {
        return connectionHandlerID;
    }

    public void setConnectionHandlerID(ConnectionHandlerID connectionHandlerID) {
        this.connectionHandlerID = connectionHandlerID;
    }

}
