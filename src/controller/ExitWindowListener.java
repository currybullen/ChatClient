package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


/**
 * A window adapter class used to listen for the event of a window closing.
 * @author c12mkn
 *
 */
public class ExitWindowListener implements ActionListener {
	private ConnectionHandler connectionHandler;
	
	/**
	 * Constructs a WindowExitAdapter.
	 * @param connectionHandler a ConnectionHandler object.
	 */
	public ExitWindowListener(ConnectionHandler connectionHandler) {
		this.connectionHandler = connectionHandler;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		
		/*If the window is closing, send a quit message to the server.*/
		if (connectionHandler.hasConnection()) {
			connectionHandler.sendPDU(PDUFactory.makeQuit());
		}
		System.exit(0);
	}
}
