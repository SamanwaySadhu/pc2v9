package edu.csus.ecs.pc2.core.transport;

import java.io.Serializable;

import javax.crypto.SecretKey;

import edu.csus.ecs.pc2.core.model.ElementId;

/**
 * A unique identifier for each connection.
 * 
 * @version $Id$
 * @author pc2@ecs.csus.edu
 */

// SOMEDAY should this be named something different, it contains information beyond just Id.
// $HeadURL$
public class ConnectionHandlerID implements Serializable {
    /**
     * 
     */
    private static final long serialVersionUID = -3845388028654277507L;

    /**
     * Unique identifier for each instance of a ConnectionHandler.
     */
    private ElementId elementID = null;

    /**
     * SecretKey used for Secure Transmission of data.
     */
    private SecretKey secretKey = null;

    /**
     * Flag set when the ConnectionHandler has successfuly generated a SercretKey for data transmission.
     */
    private boolean readyToCommunicate = false;

    /**
     * 
     * @param identifier
     *            some name that is helpful in identifying this Id.
     */
    public ConnectionHandlerID(String identifier) {
        super();
        elementID = new ElementId(identifier);
        setReadyToCommunicate(false);
    }

    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        } else if (obj instanceof ConnectionHandlerID) {
            ConnectionHandlerID otherId = (ConnectionHandlerID) obj;
            return otherId.elementID.equals(this.elementID);
        } else {
            return false;
        }
    }

    public SecretKey getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(SecretKey secretKey) {
        this.secretKey = secretKey;
    }

    public String toString() {
        return elementID.toString();
    }

    /**
     * All initialization has been done and this connection is ready to communicate.
     * @return
     */
    public boolean isReadyToCommunicate() {
        return readyToCommunicate;
    }

    public synchronized void setReadyToCommunicate(boolean readyToSend) {
        readyToCommunicate = readyToSend;
        notifyAll();
    }

    public int hashCode() {
        return toString().hashCode();
    }

}
