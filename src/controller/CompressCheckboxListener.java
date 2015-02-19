package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractButton;

/**
 * A listener class defining behavior when the user checks the compress
 * checkbox.
 * @author c12mkn
 *
 */
public class CompressCheckboxListener implements ActionListener {
	private SendButtonListener sendListener;

	/**
	 * Constructs a CompressCheckboxListener.
	 * @param sendListener a SendListener object listening to the send button
	 * of the GUI.
	 */
	public CompressCheckboxListener(SendButtonListener sendListener) {
		this.sendListener = sendListener;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		/*If the checkbox is checked, set the compress value in sendListener
		 * to true, else set it to false.*/
		if (((AbstractButton) e.getSource()).isSelected()) {
			sendListener.setCompress(true);
		} else {
			sendListener.setCompress(false);
		}
	}
}
