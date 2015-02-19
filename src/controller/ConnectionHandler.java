package controller;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import model.PDU;

/**
 * A class designed to handle a TCP connection between the server and the
 * client.
 * @author c12mkn
 *
 */
public class ConnectionHandler extends Observable implements Observer {
	private String nickname;
	private ChatServerConnection connection;
	private ArrayList<ChatServerConnection> previousConnections;

	/**
	 * Constructs a ConnectionHandler.
	 * @param observer an observer object to be notified when receiving a PDU.
	 * @param nickname a nickname for the user using the client.
	 */
	public ConnectionHandler(Observer observer, String nickname) {
		addObserver(observer);
		this.nickname = nickname;
		connection = null;
		previousConnections = new ArrayList<ChatServerConnection>();
	}

	/*When a first/new chat server is chosen by the user, this piece of code
	 * runs. The received Object should be a ChatServerConnection.*/
	@Override
	public void update(Observable observable, Object object) {

		/*If a previous connection didn't exist, set the provided one as
		 * the currrent and start listening.*/
		if (connection == null) {
			connection = (ChatServerConnection) object;
			startListening();

			/*If a previous connection did exist, add it to a list of
			 * connections to be closed, send out a quit message and
			 * change the current connection to the provided one.*/
		} else {
			addToPreviousConnections(connection);
			sendPDU(PDUFactory.makeQuit());
			connection = (ChatServerConnection) object;
		}

		/*Send out a join request to the new server.*/
		sendPDU(PDUFactory.makeJoinRequest(nickname.getBytes(StandardCharsets.
				UTF_8)));
	}

	/**
	 * Used externally to abort the current connection.
	 */
	public void abortConnection() {
		addToPreviousConnections(connection);
		connection = null;
	}

	/**
	 * Starts a new thread that will always listen to the current connection
	 * and take appropriate action depending on the received PDUs.
	 */
	private void startListening() {
		new Thread() {
			@Override
			public void run() {
				while (true) {

					/*If there are previous connections, see to it that they
					 * are closed.*/
					if (previousConnections.size() > 0) {
						closePreviousConnections();
					}

					/*If the current connection has been closed, set it
					 * to null.*/

					if (connection.isClosed()) {
						connection = null;
					}

					/*If there is no current connection, kill the thread.*/
					if (!hasConnection()) {
						break;
					}

					/*If there is a current connection, receive a PDU and
					 * notify all observers (usually only an Executor object.)*/
					PDU pdu = connection.receivePDU();
					if (pdu != null) {
						setChanged();
						notifyObservers(pdu);
						clearChanged();
					}
				}
			}
		}.start();
	}

	/**
	 * Closes all previous connections if they are not already closed.
	 */
	private void closePreviousConnections() {
		synchronized (previousConnections) {
			for (ChatServerConnection connection : previousConnections) {
				if (!connection.isClosed()) {
					connection.close();
				}
			}
			previousConnections.clear();
		}
	}

	/**
	 * Sends a PDU to the current chat server.
	 * @param pdu a PDU object to be sent.
	 */
	public synchronized void sendPDU(PDU pdu) {
		connection.sendData(pdu.getBytes());
	}

	/**
	 * Used to determine whether the ConnectionHandler currently has a
	 * connection.
	 * @return true if there is a connection, else false.
	 */
	public boolean hasConnection() {
		return connection != null;
	}

	/**
	 * Sets the nickname that will be used when connecting to new servers.
	 * @param nickname a nickname.
	 */
	public void setNickname(String nickname) {
		this.nickname = nickname;
	}

	/**
	 * Adds a ChatServerConnection object to a list of previous connections
	 * to be closed.
	 * @param connection a ChatServerConnection to be closed.
	 */
	private void addToPreviousConnections(ChatServerConnection connection) {
		synchronized (previousConnections) {
			previousConnections.add(connection);
		}
	}
}
