package controller;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;


public class WindowExitAdapter extends WindowAdapter {
	private ConnectionHandler connectionHandler;
	
	public WindowExitAdapter(ConnectionHandler connectionHandler) {
		this.connectionHandler = connectionHandler;
	}
	
	@Override
	public void windowClosing(WindowEvent e) {
		if (connectionHandler.hasConnection()) {
			connectionHandler.sendPDU(PDUFactory.makeQuit());
		}
	}
}
