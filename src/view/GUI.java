package view;

import java.awt.BorderLayout;

import javax.swing.BoxLayout;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * The GUI class of the chat client, extending on the JFrame class.
 * @author c12mkn
 *
 */
public class GUI extends JFrame {
	private static final long serialVersionUID = 1L;

	private JMenuItem serverList;
	private JMenuItem changeName;
	private JMenuItem encryptionKey;
	private JMenuItem exit;

	private JButton send;
	private JTextArea textArea;
	private JTextField textField;

	private JCheckBox encrypt;
	private JCheckBox compress;
	
	private JList<String> userList;

	/**
	 * Constructs a GUI object.
	 * @param userListModel a list model for the user list.
	 */
	public GUI(DefaultListModel<String> userListModel) {
		super("Chat client");

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(true);
		setSize(800, 500);

		/*Setting up the menu objects.*/
		JMenuBar menuBar = new JMenuBar();
		JMenu menu = new JMenu("Menu");
		menu.add(serverList = new JMenuItem("List servers"));
		menu.add(changeName = new JMenuItem("Change nickname"));
		menu.add(encryptionKey = new JMenuItem("Encryption key"));
		menu.add(exit = new JMenuItem("Exit"));
		menuBar.add(menu);
		setJMenuBar(menuBar);
		
		userList = new JList<String>(userListModel);
		add(new JScrollPane(userList), BorderLayout.WEST);

		textArea = new JTextArea();
		textArea.setEditable(false);
		textArea.setLineWrap(true);
		JScrollPane scrollPane = new JScrollPane(textArea);
		scrollPane.setAutoscrolls(true);
		add(scrollPane, BorderLayout.CENTER);
		add(textField = new JTextField(), BorderLayout.SOUTH);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

		panel.add(encrypt = new JCheckBox("Encrypt"));
		panel.add(compress = new JCheckBox("Compress"));
		panel.add(send = new JButton("Send"));
		getRootPane().setDefaultButton(send);
		add(panel, BorderLayout.EAST);

	}

	/**
	 * Returns the server list menu item.
	 * @return the server list menu item.
	 */
	public JMenuItem getServerListItem() {
		return serverList;
	}

	/**
	 * Returns the change nickname menu item.
	 * @return the change nickname menu item.
	 */
	public JMenuItem getChangeNicknameItem() {
		return changeName;
	}

	/**
	 * Returns the encryption key menu item.
	 * @return the encryption key menu item.
	 */
	public JMenuItem getEncryptionKeyItem() {
		return encryptionKey;
	}

	/**
	 * Returns the exit menu item.
	 * @return the exit menu item.
	 */
	public JMenuItem getExitItem() {
		return exit;
	}

	/**
	 * Returns the send button.
	 * @return the send button.
	 */
	public JButton getSendButton() {
		return send;
	}

	/**
	 * Returns the text in the text field and clears it.
	 * @return the text in the text field.
	 */
	public String getMessage() {
		String message = textField.getText();
		textField.setText("");
		return message;
	}

	/**
	 * Returns the encrypt checkbox.
	 * @return the encrypt checkbox.
	 */
	public JCheckBox getEncryptCheckbox() {
		return encrypt;
	}

	/**
	 * Returns the compress checkbox.
	 * @return the compress checkbox.
	 */
	public JCheckBox getCompressCheckbox() {
		return compress;
	}

	/**
	 * appends the text area.
	 * @param appendage a piece of text to be appended.
	 */
	public void appendTextArea(String appendage) {
		textArea.append(appendage + "\n");
	}

	/**
	 * Clears the text area.
	 */
	public void clearTextArea() {
		textArea.setText("");
	}
}
