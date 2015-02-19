package controller;

import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import model.ChatServer;
import model.PDU;

/**
 * A class representing the name server connection.
 * @author c12mkn
 *
 */
public class NameServerConnection {
	private static final int TIMEOUT_TIME = 3000;
	private static final int UDP_MAX_SIZE = 65507;
	private static final int UDP_MAX_BUFFER = 5*UDP_MAX_SIZE;

	private DatagramSocket socket;
	private InetAddress address;
	private int port;
	private byte[] serverRequest;

	/**
	 * Constructs a name server connection object from a given hostname and
	 * port.
	 * @param hostname a hostname of a name server.
	 * @param port a port of a name server.
	 * @throws Exception
	 */
	public NameServerConnection(String hostname, int port) throws Exception {
		this.port = port;
		address = InetAddress.getByName(hostname);
		socket = new DatagramSocket();
		socket.setSoTimeout(TIMEOUT_TIME);
		socket.setReceiveBufferSize(UDP_MAX_BUFFER);
		serverRequest = PDUFactory.makeServerListRequest().getBytes();
	}

	/**
	 * A function used externally to retrieve a list of chat servers.
	 * @return a list of chat servers.
	 * @throws Exception if the list could not be received.
	 */
	public ArrayList<ChatServer> getServerList() throws Exception {
		requestServerList();
		return createServerList(receivePDU());
	}

	/**
	 * Sends a UDP package requesting a chat server list in a UDP package
	 * addressed to the name server.
	 * @throws Exception
	 */
	private void requestServerList() throws Exception {
		DatagramPacket packet = new DatagramPacket(serverRequest, serverRequest.
				length, address, port);
		socket.send(packet);
	}

	/**
	 * Receives a UDP package containing a chat server list, or a part of a
	 * chat server list.
	 * @return the contained chat server list PDU.
	 * @throws Exception if the UDP package could not be received.
	 */
	private PDU receivePDU() throws Exception {
		byte[] data = new byte[UDP_MAX_SIZE];
		DatagramPacket packet = new DatagramPacket(data, data.length);
		socket.receive(packet);

		return new PDU(packet.getData(), packet.getLength());
	}

	/**
	 * Constructs a server list from one or multiple PDU's and returns it.
	 * @param pdu a PDU containing a chat server list or a part of one.
	 * @return the list of chat servesr.
	 */
	private ArrayList<ChatServer> createServerList(PDU pdu) {
		ArrayList<ChatServer> chatServers = new ArrayList<ChatServer>();
		ArrayList<Integer> receivedSequences = new ArrayList<Integer>();

		/*Read the sequence number of the received PDU and add it to a list.*/
		int sequenceNumber = pdu.getByte(1);
		receivedSequences.add(new Integer(sequenceNumber));
		int noOfServers = pdu.getShort(2);
		int offset = 4;

		for (int i = 0; i < noOfServers; i++) {

			/*If the end of the current PDU has been reached, attempt to
			 * recive another UDP package.*/
			if (offset > pdu.length()) {
				try {
					pdu = receivePDU();
					sequenceNumber = pdu.getByte(2);

					/*If the newly received PDU has a sequence number that
					 * has been previously parsed, dismiss it and be
					 * done with it.*/
					if (receivedSequences.contains(sequenceNumber)) {
						break;
					}
					offset = 0;
				} catch (Exception e) {

					/*If another UDP package couldn't be received, be done
					 * with it.*/
					break;
				}
			}

			/*Extract the information, create a chat server object from it
			 * and insert it into a list.*/
			byte[] address = pdu.getSubrange(offset, 4);
			int port = pdu.getShort(offset+4);
			int noOfClients = pdu.getByte(offset+6);
			int nameLength = pdu.getByte(offset+7);
			String serverName = getServerName(pdu, offset+8, nameLength);
			serverName += ", " + noOfClients + " connected.";
			chatServers.add(new ChatServer(address, port, serverName));
			offset += 8 + nameLength;
			if (nameLength % 4 != 0) {
				offset += 4 - (nameLength % 4);
			}
		}

		return chatServers;
	}

	/**
	 * Extracts the server name from a PDU given an index and the length of
	 * the server name.
	 * @param pdu the PDU containing the server name.
	 * @param startIndex the byte index in the PDU where the name starts.
	 * @param nameLength the length of the name.
	 * @return the server name.
	 */
	private String getServerName(PDU pdu, int startIndex, int nameLength) {
		String serverName = "";

		for (int i = startIndex; i < startIndex + nameLength; i++) {
			if (pdu.getByte(i) == 0) {
				break;
			} else {
				serverName += new String(pdu.getSubrange(i, 1),
						StandardCharsets.UTF_8);
			}
		}

		return serverName;
	}
}
