package my.edu.taylors.dad.chat.client;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.GridLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
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

import my.edu.taylors.dad.chat.entity.Agent;
import my.edu.taylors.dad.chat.entity.Auth;
import my.edu.taylors.dad.chat.entity.AuthResult;
import my.edu.taylors.dad.chat.entity.Customer;
import my.edu.taylors.dad.chat.entity.Flags;
import my.edu.taylors.dad.chat.entity.Ports;
import my.edu.taylors.dad.chat.gsa.GsaClient;

/**
 * Running class for clients<br>
 * Obtains server IP address and handles authentication<br>
 * Then creates either {@link ClientAgent} or {@link ClientCustomer}
 */
public class LoginMenu extends JFrame {

	private static final long serialVersionUID = 1L;

	private JTextField tfUsername;
	private JPasswordField tfPassword;
	private Socket socket = null;

	public static String serverIpAddress;

	public LoginMenu() {
		// firstly get server address
		serverIpAddress = new GsaClient().getAddress();
		// once it has the address, start other things
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

	/**
	 * Reads inputs a returns {@link Auth} object
	 * @return
	 */
	private Auth getAuth() {
		String username = tfUsername.getText();
		char[] passwordChars = tfPassword.getPassword();
		String password = new String(passwordChars);
		Auth auth = new Auth(username, password);

		return auth;
	}

	/**
	 * Sends credentials to server and waits for response
	 */
	private void authenticate() {
		Auth auth = getAuth();
		try {
			if (socket == null) socket = new Socket(serverIpAddress, Ports.MSG_SERVER);

			// send credentials to server for authentication
			ObjectOutputStream output = new ObjectOutputStream(socket.getOutputStream());
			output.writeObject(auth);

			// read the result object from server
			ObjectInputStream ois2 = new ObjectInputStream(socket.getInputStream());
			AuthResult result = (AuthResult) ois2.readObject();

			int type = Integer.parseInt(result.getResult());
			switch (type) {
				// Guest
				case Flags.CUSTOMER_AUTHENTICATED_I:
					Customer customer = (Customer) result.getAuth();
					new ClientCustomer(socket, customer);
					this.setVisible(false);
					this.dispose();
					break;

				// Agent
				case Flags.AGENT_AUTHENTICATED_I:
					Agent agent = (Agent) result.getAuth();
					new ClientAgent(socket, agent);
					this.setVisible(false);
					this.dispose();
					break;

				// too many attempts
				case Flags.AUTHENTICATICATION_ATTEMTPS_I:
					JOptionPane.showOptionDialog(null, "You made too many attempts. Try again later.", "Sorry", JOptionPane.WARNING_MESSAGE, NORMAL, null, null, null);
					break;

				// Wrong combination or error
				default:
					JOptionPane.showOptionDialog(null, "Wrong combination", "Sorry", JOptionPane.WARNING_MESSAGE, NORMAL, null, null, null);
					break;
			}

		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			if (socket != null) {
				try {
					socket.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
			JOptionPane.showOptionDialog(null, "An error eccoured when trying to authenticate you. Server is not responding. Please try again later.",
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
