package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.nio.charset.StandardCharsets;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import model.CompressAlgorithms;
import model.CryptAlgorithms;
import model.MsgTypes;
import model.PDU;
import view.GUI;

/**
 * A listener class defining behavior when the user clicks the send button.
 * @author c12mkn
 *
 */
public class SendButtonListener implements ActionListener {
	private static final int DEFAULT_CRYPTALGORITHM = CryptAlgorithms.STANDARD;
	private static final int DEFAULT_COMPRESSALGORITHM = CompressAlgorithms.
			GZIP;
	private static final String DEFAULT_CRYPTKEY = "foobar";

	private ConnectionHandler connectionHandler;
	private GUI gui;
	private String cryptKey;
	private boolean encrypt;
	private boolean compress;

	/**
	 * Constructs a SendButtonListener.
	 * @param connectionHandler a ConnectionHandler object used to send
	 * messages.
	 * @param gui a GUI to fetch the messages from.
	 */
	public SendButtonListener(ConnectionHandler connectionHandler, GUI gui) {
		cryptKey = DEFAULT_CRYPTKEY;
		this.connectionHandler = connectionHandler;
		this.gui = gui;
	}

	@Override
	public void actionPerformed(ActionEvent e) {

		/*If the connection handler has no connection, don't send the message.*/
		if (!connectionHandler.hasConnection()) {
			gui.appendTextArea("No connection available.");
			return;
		}

		/*Retrieve the message*/
		final String message = gui.getMessage();

		/*If the message length is 0, don't send it.*/
		if (message.length() == 0) {
			return;
		}

		/*If not, send the message in a new thread to prevent the GUI from
		 * locking up.*/
		new Thread() {
			@Override
			public void run() {
				if (compress && encrypt) {
					sendCompressedAndEncryptedMessage(message);
				} else if (compress) {
					sendCompressedMessage(message);
				} else if (encrypt) {
					sendEncryptedMessage(message);
				} else {
					sendPlainMessage(message);
				}
			}
		}.start();
	}

	/**
	 * Used externally to set the boolean encrypt value.
	 * @param encrypt a boolean value deciding if the sent messages are to be
	 * encrypted.
	 */
	public void setEncrypt(boolean encrypt) {
		this.encrypt = encrypt;
	}

	/**
	 * Used externally to set the boolean compress value.
	 * @param compress a boolean value deciding if the sent messages are to be
	 * compressed.
	 */
	public void setCompress(boolean compress) {
		this.compress = compress;
	}

	public void setCryptKey(String cryptKey) {
		this.cryptKey = cryptKey;
	}

	/**
	 * Compresses and encrypts a message, then sends it.
	 * @param message a message to be sent.
	 */
	private void sendCompressedAndEncryptedMessage(String message) {
		try {
			PDU pdu = PDUFactory.makeCompressedMessage(
					DEFAULT_COMPRESSALGORITHM, message.getBytes(
							StandardCharsets.UTF_8));
			pdu = PDUFactory.makeEncryptedMessage(DEFAULT_CRYPTALGORITHM,
					pdu.getBytes(), cryptKey);
			pdu = PDUFactory.makeMessage(MsgTypes.COMPCRYPT, pdu.getBytes());
			connectionHandler.sendPDU(pdu);
		} catch (Exception e) {
			
			/*If the message couldn't be compressed, display an error message.*/
			displayErrorMessageWhenPossible("Message not sent, message could " +
					"not be compressed.", "Message compression");
		}
	}

	/**
	 * Compresses and sends a message.
	 * @param message a message to be sent.
	 */
	private void sendCompressedMessage(String message) {
		try {
			PDU pdu = PDUFactory.makeCompressedMessage(
					DEFAULT_COMPRESSALGORITHM, message.getBytes(
							StandardCharsets.UTF_8));
			pdu = PDUFactory.makeMessage(MsgTypes.COMP, pdu.getBytes());
			connectionHandler.sendPDU(pdu);
		} catch (Exception e) {
			/*If the message couldn't be compressed, display an error message.*/
			displayErrorMessageWhenPossible("Message not sent, message could " +
					"not be compressed.", "Message compression");
		}
	}

	/**
	 * Encrypts and sends a message.
	 * @param message the message to be sent.
	 */
	private void sendEncryptedMessage(String message) {
		PDU pdu = PDUFactory.makeEncryptedMessage(CryptAlgorithms.STANDARD,
				message.getBytes(StandardCharsets.UTF_8), cryptKey);
		pdu = PDUFactory.makeMessage(MsgTypes.CRYPT, pdu.getBytes());
		connectionHandler.sendPDU(pdu);
	}

	/**
	 * Sends a message.
	 * @param message the message to be sent.
	 */
	private void sendPlainMessage(String message) {
		PDU pdu = PDUFactory.makeMessage(MsgTypes.TEXT, message.getBytes(
				StandardCharsets.UTF_8));
		connectionHandler.sendPDU(pdu);
	}

	/**
	 * Help function to display an error message when convenient for the Swing
	 * thread.
	 * @param message an error message to be displayed.
	 * @param title a title for the error message.
	 */
	private void displayErrorMessageWhenPossible(String message, String title) {
		SwingUtilities.invokeLater(new ErrorMessageHelperRunnable(message,
				title));
	}

	/**
	 * Help class to display an error message.
	 * @author c12mkn
	 *
	 */
	private class ErrorMessageHelperRunnable implements Runnable {
		private String message;
		private String title;

		public ErrorMessageHelperRunnable(String message, String title) {
			this.message = message;
			this.title = title;
		}

		@Override
		public void run() {
			JOptionPane.showMessageDialog(gui, message, title,
					JOptionPane.ERROR_MESSAGE);
		}
	}
}