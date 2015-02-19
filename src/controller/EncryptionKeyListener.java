package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import view.GUI;

/**
 * A listener class defining behavior when the user selects the encryption
 * key menu item.
 * @author c12mkn
 *
 */
public class EncryptionKeyListener implements ActionListener {
	private SendButtonListener sendButtonListener;
	private Executor executor;
	private GUI gui;

	/**
	 * Constructs an EncryptionKeyItemListener.
	 * @param executor an Executor object which encryption key attribute will
	 * be altered.
	 * @param sendButtonListener a SendButtonListener object which encryption
	 * key will be altered.
	 * @param gui a GUI to display error messages at if the encryption key
	 * doesn't have a proper format.
	 */
	public EncryptionKeyListener(Executor executor, SendButtonListener
			sendButtonListener, GUI gui) {
		this.sendButtonListener = sendButtonListener;
		this.executor = executor;
		this.gui = gui;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		/*Prompt the user for an encryption key.*/
		String key = JOptionPane.showInputDialog(null,
				"Enter desired encryption key: ", "Encryption key",
				JOptionPane.QUESTION_MESSAGE);
		
		/*If the user doesn't enter anything, do nothing.*/
		if (key == null) {
			return;
			
		/*If the key is too long, display an error message.*/
		} else if (key.length() > 255) {
			JOptionPane.showMessageDialog(gui, "Encryption key too long, " +
					"please limit it to 255 characters.", "Encryption key",
					JOptionPane.ERROR_MESSAGE);
			
			/*If the key checks out, change it where needed.*/
		} else {
			sendButtonListener.setCryptKey(key);
			executor.setEncryptionKey(key);
		}
	}
}
