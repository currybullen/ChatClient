package controller;

import javax.swing.DefaultListModel;
import javax.swing.JOptionPane;

import view.GUI;

/**
 * A chat client application developed as part of the course Datakommunikation
 * och datornat at Umea University.
 * @author c12mkn
 *
 */
public class Client {

	public static void main(String[] args) {
		new Client().go();
	}

	/**
	 * Executes the starting code of the application.
	 */
	public void go() {
		/*Prompt the user for a nickname.*/
		String nickname = promptNickname();

		/*Exit if it's too long.*/
		if (nickname.length() > 255) {
			JOptionPane.showMessageDialog(null, "Nickname too long.",
					"Nickname", JOptionPane.ERROR_MESSAGE);
			System.exit(2);
		}

		/*Prompt the user for a name server hostname.*/
		String hostname = promptHostname();

		/*Prompt the user for a port.*/
		int port = promptPort();

		/*Exit if it's not within the allowed interval.*/
		if (port > 65535 || port < 0) {
			JOptionPane.showMessageDialog(null, "Port not within the allowed" +
					" interval.", "Bad port", JOptionPane.ERROR_MESSAGE);
			System.exit(6);
		}

		/*Attempt to find the name server by hostname, if not possible exit.*/
		NameServerConnection nameServerConnection = null;
		try {
			nameServerConnection =
					new NameServerConnection(hostname, port);
		} catch (Exception e) {
			JOptionPane.showMessageDialog(null, "Couldn't find the name server",
					"Name server", JOptionPane.ERROR_MESSAGE);
			System.exit(7);
		}

		/*Create a GUI, an Executor and a ConnectionHandler. The GUI and the
		 * Executor are provided with a user list model.*/
		DefaultListModel<String> userListModel = new DefaultListModel<String>();
		GUI gui = new GUI(userListModel);
		Executor executor = new Executor(gui, userListModel);
		ConnectionHandler connectionHandler = new ConnectionHandler(executor,
				nickname);

		/*Create and configure all of the listeners.*/
		gui.getExitItem().addActionListener(new ExitWindowListener(
				connectionHandler));
		gui.addWindowListener(new WindowExitAdapter(connectionHandler));
		gui.getChangeNicknameItem().addActionListener(
				new ChangeNicknameListener(connectionHandler, gui));
		gui.getServerListItem().addActionListener(new ListServersListener(gui,
				nameServerConnection, connectionHandler));
		SendButtonListener sendButtonListener = new SendButtonListener(
				connectionHandler, gui);
		gui.getEncryptCheckbox().addActionListener(new EncryptCheckboxListener(
				sendButtonListener));
		gui.getEncryptionKeyItem().addActionListener(
				new EncryptionKeyListener(executor, sendButtonListener,
						gui));
		gui.getCompressCheckbox().addActionListener(
				new CompressCheckboxListener(sendButtonListener));
		gui.getSendButton().addActionListener(sendButtonListener);

		/*Set the GUI visible.*/
		gui.setVisible(true);
	}

	/**
	 * Prompts the user for a name server port. Exits the program if the user
	 * doesn't enter anything or enters something incorrect.
	 * @return the port provided.
	 */
	private int promptPort() {
		int port = 0;
		String userInput = JOptionPane.showInputDialog(null, "Enter name " +
				"server port (in the interval 0-65535): ", "Name server",
				JOptionPane.QUESTION_MESSAGE);
		if (userInput == null || userInput.length() == 0) {
			JOptionPane.showMessageDialog(null, "You didn't enter anything.",
					"Error", JOptionPane.ERROR_MESSAGE);
			System.exit(4);
		}
		try {
			port = Integer.parseInt(userInput);
		} catch (NumberFormatException e) {
			JOptionPane.showMessageDialog(null, "Couldn't recognize number " +
					"format", "Bad number format", JOptionPane.ERROR_MESSAGE);
			System.exit(5);
		}

		return port;
	}

	/**
	 * Prompts the user for a hostname. Exits if the user doesn't enter
	 * anything.
	 * @return the hostname provided.
	 */
	private String promptHostname() {
		String hostname =  JOptionPane.showInputDialog(null, "Enter name " +
				"server hostname: ", "Name server", JOptionPane.
				QUESTION_MESSAGE);
		if (hostname == null || hostname.length() == 0) {
			JOptionPane.showMessageDialog(null, "You didn't enter anything.",
					"Name server", JOptionPane.ERROR_MESSAGE);
			System.exit(3);
		}

		return hostname;
	}

	/**
	 * Prompts the user for a nickname. Exits if the user doesn't enter
	 * anything.
	 * @return a nickname.
	 */
	private String promptNickname() {
		String nickname = JOptionPane.showInputDialog(null, "Enter a nickname" +
				" (maximum 255 characters): ", "Nickname", JOptionPane.
				QUESTION_MESSAGE);
		if (nickname == null || nickname.length() == 0) {
			JOptionPane.showMessageDialog(null, "You didn't enter anything.",
					"Nickname", JOptionPane.ERROR_MESSAGE);
			System.exit(1);
		}

		return nickname;
	}

	/**
	 * Returns the padded length of any number of lengths. For example, if
	 * 3 and 3 are sent as parameters, the function returns 8.
	 * @param lengths the lengths to calculate the padded length of.
	 * @return the padded lengths.
	 */
	public static int getPaddedLength(int... lengths) {
		int paddedLength = 0;

		for (int i = 0; i < lengths.length; i++) {
			paddedLength += lengths[i];
			if (lengths[i] % 4 != 0) {
				paddedLength += 4 -lengths[i] % 4;
			}
		}

		return paddedLength;
	}
}
