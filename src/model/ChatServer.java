package model;

/**
 * A model object representing a chat server.
 * @author c12mkn
 *
 */
public class ChatServer {

	private byte[] address;
	private int port;
	private String name;

	/**
	 * Constructs a ChatServer object.
	 * @param address a byte address of a chat server.
	 * @param port a port of a chat server.
	 * @param name a name of a chat server.
	 */
	public ChatServer(byte[] address, int port, String name) {
		this.address = address;
		this.port = port;
		this.name = name;
	}

	/**
	 * Gets the byte address of the server.
	 * @return the byte address of the server.
	 */
	public byte[] getAddress() {
		return address;
	}

	/**
	 * Gets the port of the server.
	 * @return the port of the server.
	 */
	public int getPort() {
		return port;
	}

	/**
	 * Gets the name of the server
	 * @return the name of the server.
	 */
	public String getName() {
		return name;
	}

}
