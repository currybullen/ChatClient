package controller;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;

import model.ChatServer;
import view.GUI;

/**
 * A listener class defining behavior when the user selects the server list
 * menu item.
 * @author c12mkn
 *
 */
public class ListServersListener extends Observable implements ActionListener{
	private GUI gui;
	private NameServerConnection connection;

	/**
	 * Constructs a ListServerListener.
	 * @param gui a GUI to display messages at.
	 * @param connection a NameServerConnection object to fetch chat server
	 * lists via.
	 * @param connectionHandler a ConnectionHandler object to provide with
	 * a chat server connection.
	 */
	public ListServersListener(GUI gui, NameServerConnection connection,
			Observer connectionHandler) {
		this.connection = connection;
		this.gui = gui;
		addObserver(connectionHandler);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		new SwingWorker<Void, ChatServer>() {
			private String[] selectionValues;
			private ArrayList<ChatServer> chatServers;

			@Override
			protected Void doInBackground() {
				selectionValues = null;

				try {

					/*Get a list of chat servers and build from it a list
					 * of selections for the user.*/
					chatServers = connection.getServerList();
					selectionValues = makeSelectionValues(chatServers);
				} catch (Exception e) {
					e.printStackTrace();

					/*If the server list couldn't be fetched, display an
					 * error message.*/
					SwingUtilities.invokeLater(new Runnable() {
						@Override
						public void run() {
							JOptionPane.showMessageDialog(gui, "Couldn't " +
									"fetch chat server list.", "Chat server" +
											" list", JOptionPane.ERROR_MESSAGE);
						}
					});
				}
				return null;
			}

			@Override
			public void done() {
				/*If no selection values could be built, don't prompt the
				 * user.*/
				if (selectionValues == null) {
					return;
				}

				/*Prompt the user for a chat server.*/
				String userInput = promptUser(selectionValues);

				/*If the user didn't enter anything, return.*/
				if (userInput == null) {
					return;
				}

				/*If the user selected a server, proceed.*/
				ChatServer selectedServer = getSelectedServer(userInput,
						chatServers);
				new ConnectionHelperThread(selectedServer).start();
			}
		}.execute();
	}

	/**
	 * Makes an array of selection values from a provided list of servers.
	 * @param serverList an array list of ChatServer objects.
	 * @return an array of selection values.
	 */
	private String[] makeSelectionValues(ArrayList<ChatServer> serverList) {
		String[] selectionValues = new String[serverList.size()];

		for (int i = 0; i < serverList.size(); i++) {
			selectionValues[i] = serverList.get(i).getName();
		}

		return selectionValues;
	}

	/**
	 * Prompts the user to choose a server and returns the chosen one.
	 * @param selectionValues an array of selection values.
	 * @return the selected server as a string.
	 */
	private String promptUser(String[] selectionValues) {
		String userInput = (String) JOptionPane.showInputDialog(gui,
				"Choose a chat server: ", "Chat servers", JOptionPane.
				QUESTION_MESSAGE, null, selectionValues, null);
		return userInput;
	}

	/**
	 * Returns the user selected server by providing the name of the selected
	 * server.
	 * @param userInput a user selected server name.
	 * @param serverList a list of chat servers.
	 * @return the selected server as a ChatServer object.
	 */
	private ChatServer getSelectedServer(String userInput,
			ArrayList<ChatServer> serverList) {
		ChatServer returnValue = null;

		for (ChatServer server : serverList) {
			if (server.getName().equals(userInput)) {
				returnValue = server;
			}
		}

		return returnValue;
 	}

	/**
	 * Inner class used briefly when connecting to a chat server to prevent
	 * performance issues in the Swing thread.
	 * @author c12mkn
	 *
	 */
	private class ConnectionHelperThread extends Thread {
		private ChatServer chatServer;

		public ConnectionHelperThread(ChatServer chatServer) {
			this.chatServer = chatServer;
		}

		@Override
		public void run() {
			try {
				ChatServerConnection chatServerConnection =
						new ChatServerConnection(chatServer);
				setChanged();
				notifyObservers(chatServerConnection);
				clearChanged();
			} catch (Exception e) {
				SwingUtilities.invokeLater(new Runnable() {
					@Override
					public void run() {
						JOptionPane.showMessageDialog(gui, "Couldn't " +
								"connect to chat server.", "Chat server" +
										" list", JOptionPane.ERROR_MESSAGE);
					}
				});
			}
		}
	}
}
