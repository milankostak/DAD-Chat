package my.edu.taylors.dad.chat;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectOutputStream;
import java.net.Socket;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import my.edu.taylors.dad.chat.entity.Auth;
import my.edu.taylors.dad.chat.entity.Flags;

public class LoginMenu extends JFrame {

	private static final long serialVersionUID = 1L;

	private JTextField tfUsername;
	private JPasswordField tfPassword;
	private Socket socket = null;

	public LoginMenu() {
		setUpGui();
	}

	private void setUpGui() {
		// general layout
		BorderLayout mainLayout = new BorderLayout(); 
		setLayout(mainLayout);
		add(getInputPanel(), BorderLayout.CENTER);
		add(getBottomPanel(), BorderLayout.PAGE_END);

		// basic settings and packing
		setTitle("Login to system");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		pack();
		setLocationRelativeTo(null);
	}

	private Component getInputPanel() {
		// labels
		JLabel lbUsername = new JLabel("Username");
		lbUsername.setHorizontalAlignment(JLabel.CENTER);
		JLabel lbPassword = new JLabel("Password");
		lbPassword.setHorizontalAlignment(JLabel.CENTER);

		// inputs
		tfUsername = new JTextField(15);
		tfUsername.addKeyListener(new AuthenticateKeyAdapter());
		tfPassword = new JPasswordField(15);
		tfPassword.addKeyListener(new AuthenticateKeyAdapter());

		GridLayout inputGridLayout = new GridLayout(2, 2);
		JPanel inputPanel = new JPanel(inputGridLayout);
		inputPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

		inputGridLayout.setHgap(10);
		inputGridLayout.setVgap(10);

		inputPanel.add(lbUsername);
		inputPanel.add(tfUsername);
		inputPanel.add(lbPassword);
		inputPanel.add(tfPassword);

		return inputPanel;
	}

	private Component getBottomPanel() {
		// button
		JButton btLogin = new JButton("Login");
		btLogin.setMnemonic(KeyEvent.VK_L);
		btLogin.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btLogin.addActionListener(e -> authenticate());

		// bottom box
		FlowLayout bottomLayout = new FlowLayout(FlowLayout.CENTER);
		JPanel bottomPanel = new JPanel(bottomLayout);
		bottomPanel.add(btLogin);

		return bottomPanel;
	}

	private Auth getAuth() {
		String username = tfUsername.getText();
		char[] passwordChars = tfPassword.getPassword();
		String password = new String(passwordChars);
		Auth auth = new Auth(username, password);

		return auth;
	}

	private void authenticate() {
		Auth auth = getAuth();
		try {
			if (socket == null) socket = new Socket("192.168.9.116", 9999);

			// send credentials to server for authentication
			ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
			output.writeObject(auth);

			// read the result message from server
			BufferedReader fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			
			int type = Integer.parseInt(fromServer.readLine());
			switch (type) {
				// Guest
				case Flags.CUSTOMER_AUTHENTICATED_I:
					new ClientCustomer(socket, auth);
					this.setVisible(false);
					break;

				// Agent
				case Flags.AGENT_AUTHENTICATED_I:
					new ClientAgent(socket, auth);
					this.setVisible(false);
					break;

				// too many attempts
				case Flags.AUTHENTICATICATION_ATTEMTPS_I:
					JOptionPane.showOptionDialog(null, "You made too many attempts. Try again later.", "Sorry", JOptionPane.WARNING_MESSAGE, NORMAL, null, null, null);
					break;

				default:
					// Wrong combination or error
					JOptionPane.showOptionDialog(null, "Wrong combination", "Sorry", JOptionPane.WARNING_MESSAGE, NORMAL, null, null, null);
					break;
			}

		} catch (IOException e) {
			e.printStackTrace();
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			JOptionPane.showOptionDialog(null, "An error eccoured when trying to auhtneticate you. Server is not responding. Please try gagin later.",
					"Authentication error", JOptionPane.WARNING_MESSAGE, NORMAL, null, null, null);
		}
	}

	public static void main(String[] args) {
		new LoginMenu().setVisible(true);
	}

	private class AuthenticateKeyAdapter extends KeyAdapter {
		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				authenticate();
			}
		}
	}

}
