package controller;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Observable;
import java.util.Observer;

import javax.swing.DefaultListModel;
import javax.swing.SwingUtilities;

import model.MsgTypes;
import model.OpCodes;
import model.PDU;
import view.GUI;


/**
 * A class used to take different courses of action when receiving a PDU
 */
public class Executor implements Observer {
	/*A default encryption key to be used if no other is provided.*/
	private static final String DEFAULTENCRYPTIONKEY = "foobar";

	private GUI gui;
	private DefaultListModel<String> userListModel;
	private String encryptionKey;

	/**
	 * Constructs a new Executor.
	 * @param gui a GUI object.
	 */
	public Executor(GUI gui, DefaultListModel<String> userListModel) {
		this.gui = gui;
		this.userListModel = userListModel;
		encryptionKey = DEFAULTENCRYPTIONKEY;
	}

	/*The following code runs when an underlying ConnectionHandler object
	 * receives a PDU.*/
	@Override
	public void update(Observable observable, Object object) {
		PDU pdu = (PDU) object;

		/*Depending on the PDU, take appropriate action.*/
		switch (pdu.getByte(0)) {
			case OpCodes.UJOIN:
				processUserJoin(pdu);
				break;
			case OpCodes.NICKS:
				clearUserList();
				clearWhenPossible();
				processNicknames(pdu);
				break;
			case OpCodes.ULEAVE:
				processUserLeave(pdu);
				break;
			case OpCodes.UCNICK:
				processChangeNickname(pdu);
				break;
			case OpCodes.QUIT:
				((ConnectionHandler) observable).abortConnection();
				break;
			case OpCodes.MESSAGE:

				/*If the checksum of the message doesn't check out, disregard
				 * it.*/
				if (Checksum.calc(pdu.getBytes(), pdu.length()) == 0) {
					displayMessage(pdu);
				}
				break;
		}
	}

	/**
	 * Extracts information from a UCNIK PDU and displays the name change in
	 * the GUI.
	 * @param pdu a UCNIK PDU.
	 */
	private void processChangeNickname(PDU pdu) {
		int nickLength1 = pdu.getByte(1);
		int nickLength2 = pdu.getByte(2);

		String nickname1 = new String(pdu.getSubrange(8, nickLength1),
				StandardCharsets.UTF_8);
		String nickname2 = new String(pdu.getSubrange(8+Client.
				getPaddedLength(nickLength1), nickLength2), StandardCharsets.
				UTF_8);

		removeFromUserList(nickname1);
		addToUserList(nickname2);

		String timeStamp = getTimeStamp(pdu.getInt(4));
		appendWhenPossible(timeStamp + "User " + nickname1 + " changed " +
				"nickname to " + nickname2 + ".");
	}

	/**
	 * Extracts information from a ULEAVE PDU and displays it in the GUI.
	 * @param pdu
	 */
	private void processUserLeave(PDU pdu) {
		int nickLength = pdu.getByte(1);
		String timeStamp = getTimeStamp(pdu.getInt(4));
		String nickname = new String(pdu.getSubrange(8, nickLength),
				StandardCharsets.UTF_8);

		removeFromUserList(nickname);
		appendWhenPossible(timeStamp+nickname+" has left the server.");
	}

	/**
	 * Extracts information from a MESS PDU and shows the contained message
	 * in the GUI.
	 * @param pdu a MESS PDU.
	 */
	private void displayMessage(PDU pdu) {
		int messageLength = pdu.getShort(4);
		byte[] temp;
		String message = "";

		switch(pdu.getByte(1)) {
			case MsgTypes.TEXT:

				/*If the message is in plain text, simply extract it.*/
				message = new String(pdu.getSubrange(12, messageLength),
						StandardCharsets.UTF_8);
				break;
			case MsgTypes.COMP:

				/*If the message is compressed, decompress it.*/
				try {
					temp = decompressMessage(pdu.getSubrange(12,
							messageLength));
					if (temp == null) {
						return;
					}
					message = new String(temp, StandardCharsets.UTF_8);
					break;
				} catch (ArrayIndexOutOfBoundsException e) {
					return;
				}
			case MsgTypes.CRYPT:

				/*If the message is encrypted, decrypt it.*/
				try {
					temp = decryptMessage(pdu.getSubrange(12, messageLength));
					if (temp == null) {
						return;
					}
					message = new String(temp, StandardCharsets.UTF_8);
					break;
				} catch (ArrayIndexOutOfBoundsException e) {
					return;
				}

				/*If them essage is both compressed and encrypted, decrypt it,
				 * then decompress it.*/
			case MsgTypes.COMPCRYPT:
				try {
					temp = decryptMessage(pdu.getSubrange(12, messageLength));
					if (temp == null) {
						return;
					}
					PDU tempPDU = new PDU(temp, temp.length);
					temp = decompressMessage(tempPDU.getBytes());
					if (temp == null) {
						return;
					}
					message = new String(temp, StandardCharsets.UTF_8);
					break;
				}  catch (ArrayIndexOutOfBoundsException e) {
					return;
				}

		}

		int messagePaddedLength = Client.getPaddedLength(
				messageLength);
		int nicknameLength = pdu.getByte(2);

		/*Extract the nickname if there is one.*/
		String nickname = "";
		if (nicknameLength != 0) {
			nickname = new String(pdu.getSubrange(
					12+messagePaddedLength, nicknameLength), StandardCharsets.
					UTF_8);
		} else {

			/*If not, assume it's a server message.*/
			nickname = "Server message";
		}

		String timeStamp = getTimeStamp(pdu.getInt(8));
		appendWhenPossible(timeStamp+nickname+": "+message);
	}

	/**
	 * Extracts information from a NICKS PDU and displays it in the GUI.
	 * @param pdu a NICKS PDU.
	 */
	private void processNicknames(PDU pdu) {
		int totalLength = pdu.getShort(2);
		int nickLength = 0;
		int nickStart = 4;

		for (int i = 4; i < totalLength; i++) {
			if (pdu.getByte(i) != 0) {
				nickLength++;
			} else {
				if (nickLength > 0) {
					String nickname = new String(pdu.getSubrange(nickStart,
							nickLength), StandardCharsets.UTF_8);
					addToUserList(nickname);
				}
				nickLength = 0;
				nickStart = i+1;
			}
		}
	}

	/**
	 * Extracts informaton from a UJOIN PDU and displays it in the GUI.
	 * @param pdu a UJOIN PDU.
	 */
	private void processUserJoin(PDU pdu) {
		int nickLength = pdu.getByte(1);
		String timeStamp = getTimeStamp(pdu.getInt(4));
		String nickname = new String(pdu.getSubrange(8, nickLength),
				StandardCharsets.UTF_8);

		addToUserList(nickname);
		appendWhenPossible(timeStamp+nickname+" has joined the server.");
	}

	/**
	 * Extracts the time stamp from a MESS PDU and returns it as a formatted
	 * string.
	 * @param time a long representing the time.
	 * @return the time stamp as a string.
	 */
	private String getTimeStamp(long time) {
		Date date = new Date(time*1000);
		DateFormat formatter = new SimpleDateFormat("HH:mm:ss");
		String dateFormatted = "[" + formatter.format(date) + "] ";
		return dateFormatted;
	}

	/**
	 * Appends the text area in the GUI when possible.
	 * @param text the text to be appended.
	 */
	private void appendWhenPossible(String text) {
		SwingUtilities.invokeLater(new AppendMessageHelperRunnable(text));
	}

	/**
	 * Decompresses a message.
	 * @param message the bytes of the message to be decompressed.
	 * @return the decompressed message as a byte array. Returns null if
	 * the message could not be decompressed.
	 * @throws ArrayIndexOutOfBoundsException if the message to be decompressed
	 * had a bad format.
	 */
	private byte[] decompressMessage(byte[] message)
			throws ArrayIndexOutOfBoundsException {
		PDU pdu = new PDU(message, message.length);

		/*Check the checksum, if it doesn't check out, return null.*/
		if (Checksum.calc(pdu.getBytes(), pdu.length()) != 0) {
			return null;
		}
		byte[] temp = pdu.getSubrange(8, pdu.getShort(2));
		try {
			temp = GZIP.decompress(temp, pdu.getShort(4));
		} catch (Exception e) {
			return null;
		}

		return temp;
	}

	/**
	 * Decrypts a message.
	 * @param message the message to be decrypted.
	 * @return the decrypted message as a byte array.
	 * @throws ArrayIndexOutOfBoundsException if the message to be decompressed
	 * had a bad format.
	 */
	private byte[] decryptMessage(byte[] message)
			throws ArrayIndexOutOfBoundsException {
		PDU pdu = new PDU(message, message.length);

		/*Check the checksum, if it doesn't check out, return null.*/
		if (Checksum.calc(pdu.getBytes(), pdu.length()) != 0) {
			return null;
		}
		byte[] buffer = pdu.getSubrange(8, pdu.getShort(2));
		Crypt.decrypt(buffer, buffer.length, encryptionKey.getBytes(),
				encryptionKey.length());
		return buffer;
	}

	/**
	 * Sets the decryption key to be used.
	 * @param decryptionKey a decryption key to be used.
	 */
	public void setEncryptionKey(String decryptionKey) {
		this.encryptionKey = decryptionKey;
	}

	/**
	 * Helper class used internally to append a message to the text field
	 * in the GUI.
	 * @author c12mkn
	 *
	 */
	private class AppendMessageHelperRunnable implements Runnable {
		private String text;

		public AppendMessageHelperRunnable(String text) {
			this.text = text;
		}

		@Override
		public void run() {
			gui.appendTextArea(text);
		}
	}

	/**
	 * Clears the text area in the GUI when possible.
	 */
	private void clearWhenPossible() {
		SwingUtilities.invokeLater(new ClearHelperRunnable(gui));
	}

	/**
	 * Helper class used internally to clear the text area in the GUI.
	 * @author c12mkn
	 *
	 */
	private class ClearHelperRunnable implements Runnable {
		private GUI gui;

		public ClearHelperRunnable(GUI gui) {
			this.gui = gui;
		}

		@Override
		public void run() {
			gui.clearTextArea();
		}
	}

	/**
	 * Helper function to add a user to the user list.
	 * @param nickname the nickname of the user.
	 */
	private void addToUserList(final String nickname) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				userListModel.addElement(nickname);
			}
		});
	}

	/**
	 * Helper function to add a user to the user list.
	 * @param nickname
	 */
	private void removeFromUserList(final String nickname) {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				userListModel.removeElement(nickname);
			}
		});
	}

	/**
	 * Helper function to clear the user list.
	 */
	private void clearUserList() {
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				userListModel.clear();
			}
		});
	}
}
