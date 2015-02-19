package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import view.GUI;

/**
 * A listener class defining behaviour when the user wants to change nickname.
 * @author c12mkn
 *
 */
public class ChangeNicknameListener implements ActionListener {
	private ConnectionHandler connectionHandler;
	private GUI gui;

	/**
	 * Constructs a ChangeNicknameListener.
	 * @param connectionHandler a ConnectionHandler maintaining the chat clients
	 *  connections.
	 * @param gui a GUI that the prompting window will bind to.
	 */
	public ChangeNicknameListener(ConnectionHandler connectionHandler,
			GUI gui) {
		this.connectionHandler = connectionHandler;
		this.gui = gui;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		/*The user enters a desired nickname.*/
		String nickname = JOptionPane.showInputDialog(null, "Enter desired " +
				"nickname: ", "Change nickname", JOptionPane.QUESTION_MESSAGE);

		/*If the used didn't enter anything, return.*/
		if (nickname == null) {
			return;
			
			/*If the nickname was too long or too short, display an error 
			 * message.*/
		} else if (nickname.length() > 255 || nickname.length() == 0) {
			JOptionPane.showMessageDialog(gui, "Nickname format not accepted",
					"Nickname format", JOptionPane.ERROR_MESSAGE);
			
			/*If all went smoothly, change the nickname. If the client has 
			 * connection at the moment, notify the chat server as well.*/
		} else {
			connectionHandler.setNickname(nickname);
			if (connectionHandler.hasConnection()) {
				connectionHandler.sendPDU(PDUFactory.makeChangeNicknameMessage(
						nickname));
			}
		}
	}
}
