package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;

/**
 * A listener class defining behavior when the user checks the encrypt 
 * checkbox.
 * @author c12mkn
 *
 */
public class EncryptCheckboxListener implements ActionListener {
	private SendButtonListener sendListener;

	/**
	 * Constructs a EncryptCheckboxListener.
	 * @param sendListener a SendListener object listening to the send button
	 * of the GUI.
	 */
	public EncryptCheckboxListener(SendButtonListener sendListener) {
		this.sendListener = sendListener;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		/*If the checkbox is checked, set the encrypt value in the sendListener
		 * to true, else set it to false.*/
		if (((AbstractButton) e.getSource()).isSelected()) {
			sendListener.setEncrypt(true);
		} else {
			sendListener.setEncrypt(false);
		}
	}
}
