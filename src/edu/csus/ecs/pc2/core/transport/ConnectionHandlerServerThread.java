package edu.csus.ecs.pc2.core.transport;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketException;

import javax.crypto.SealedObject;

import edu.csus.ecs.pc2.core.log.Log;

/**
 * A Server Connection Handler.
 * 
 * Provide a method to listen to incoming sockets for transmissions and
 * send to other clients.
 * 
 * @version $Id$
 * @see edu.csus.ecs.pc2.core.transport.ConnectionHandler
 * @author pc2@ecs.csus.edu
 */

// $HeadURL$
public class ConnectionHandlerServerThread extends ConnectionHandlerThread {
    public static final String SVN_ID = "$Id$";

    public ConnectionHandlerServerThread(Socket socket, TransportManager tmCallBack, ConnectionHandler chCallBack, Log log) {
        super(socket, tmCallBack, chCallBack);
        chCallBack.setConnectionHandlerServerThread(this);
        setLog(log);
        getLog().info("new ConnectionhandlerServerThread on Socket " + socket.toString());
    }

    public void run() {
        try {
            setToOtherModule(new ObjectOutputStream(getMySocket().getOutputStream()));

            InputStream iss = getMySocket().getInputStream();
            setFromOtherModule(new ObjectInputStream(iss));

            getTmCallBack().registerIncomingConnectionRequest(getMyConnectionID(), this);
            // Code should ensure that the send worked!
            sendUnencrypted(getTmCallBack().getPublicKeyPacket());

            // Code should ensure that the receive worked!
            try {
                getTmCallBack().receiveUnencrypted(receiveUnencrypted(), getMyConnectionID());
            } catch (Exception e) {
                getLog().log(Log.INFO, "Exception - Could not receive unencrypted packet for: "+getMyConnectionID(), e);
                if (getMyConnectionID().getSecretKey() == null) {
                    getLog().info("Could not generate SecretKey -- Aborting connection");
                    // TODO Throw the expection to upper level
                    // throw new TransportException("Could not generate
                    // SecretKey -- Aborting connection");
                    getTmCallBack().connectionDropped(getMyConnectionID());
                    return;
                }
            }

            getMyConnectionID().setReadyToCommunicate(true);

            setStillListening(true);

            while (isStillListening()) {
                SealedObject sealedObject = null;
                try {
                    sealedObject = receive();
                    getTmCallBack().receive(sealedObject, getMyConnectionID());
                } catch (TransportException e) {
                    if (e.getMessage() == null ) {
                        getLog().throwing(getClass().getName(), "run - e.getmessage is null()", e);
                        setStillListening(false);
                    } else if (e.getMessage().equalsIgnoreCase(TransportException.CONNECTION_RESET)) {
                        setStillListening(false);
                    }
                }
            }
        } catch (SocketException e) {
            getLog().info("Lost connection to "+getMyConnectionID());
        } catch (Exception e) {
            getLog().log(Log.DEBUG, "Exception "+e.getMessage(), e);
        }

        getTmCallBack().connectionDropped(getMyConnectionID());
    }

}
