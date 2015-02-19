package controller;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;

import model.ChatServer;
import model.OpCodes;
import model.PDU;

/**
 * An object representing a chat server connection.
 * @author c12mkn
 *
 */
public class ChatServerConnection {
	private static final int CONNECTION_TIMEOUT = 5000;

	public Socket socket;
	private DataInputStream inputStream;
	private DataOutputStream outputStream;

	/**
	 * Creates a chat server connection from the given information.
	 * @param chatServer a ChatServer object containing the server's address and
	 * port.
	 * @throws Exception if the connection couldn't be established.
	 */
	public ChatServerConnection(ChatServer chatServer) throws Exception {
		InetAddress address = InetAddress.getByAddress(chatServer.getAddress());
		socket = new Socket();
		socket.connect(new InetSocketAddress(address, chatServer.getPort()),
				CONNECTION_TIMEOUT);
		outputStream = new DataOutputStream(socket.getOutputStream());
		inputStream = new DataInputStream(socket.getInputStream());
	}

	/**
	 * Writes a byte array of data to the socket, thus sending it to the server.
	 * @param data a byte array of data.
	 */
	public void sendData(byte[] data) {
		try {
			outputStream.write(data);
		} catch (IOException e) {

			/*If data can't be written, do nothing*/
		}
	}

	/**
	 * Reads data from the socket, parses it returns a PDU object created from
	 * it. Blocks until data is available to read.
	 * @return a received PDU.
	 */
	public PDU receivePDU() {
		PDU pdu = null;
		try {

			/*Blocks until read is available.*/
			int opCode = inputStream.read();
			if (opCode == -1) {
				socket.close();
			}

			/*Determine what kind of PDU is being received and parse it
			 * accordingly.*/
			switch(opCode) {
				case OpCodes.MESSAGE:
					pdu = PDUParser.parseMessage(inputStream);
					break;
				case OpCodes.QUIT:
					pdu = PDUParser.parseQuit(inputStream);
					break;
				case OpCodes.UJOIN:
					pdu = PDUParser.parseUserJoin(inputStream);
					break;
				case OpCodes.NICKS:
					pdu = PDUParser.parseNicknames(inputStream);
					break;
				case OpCodes.ULEAVE:
					pdu = PDUParser.parseUserLeave(inputStream);
					break;
				case OpCodes.UCNICK:
					pdu = PDUParser.parseChangeNickname(inputStream);
					break;
			}
		} catch (IOException e) {
			/*If an exception was thrown, close the socket if it's open
			 * and return null;*/
			if (!socket.isClosed()) {
				close();
			}

			return null;
		}
		return pdu;
	}

	/**
	 * Simple function to determine whether the socket is closed.
	 * @return true if the socket is closed, else false.
	 */
	public boolean isClosed() {
		return socket.isClosed();
	}

	/**
	 * Simple function to close the current socket.
	 */
	public void close() {
		try {
			socket.close();
		} catch (IOException e) {

			/*If a socket can't be closed, close the application*/
			System.exit(8);
		}
	}
}
