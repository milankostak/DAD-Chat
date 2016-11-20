package my.edu.taylors.dad.chat.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Point;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.ByteArrayOutputStream;
import java.net.InetAddress;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;

import my.edu.taylors.dad.chat.client.ClientAgent;
import my.edu.taylors.dad.chat.entity.ClientType;
import my.edu.taylors.dad.chat.entity.Message;
import my.edu.taylors.dad.chat.entity.MessageType;
import my.edu.taylors.dad.chat.voice.VoiceClient;
import my.edu.taylors.dad.chat.voice.VoicePlayThread;

public abstract class ChatWindow extends JFrame {
	private static final long serialVersionUID = 1L;

	// GUI components
	private JFrame mainFrame;
	private JTextField tfMainInput;
	private JButton btSendBoth, btSend, btLogOut, btCapture, btStop;
	private ChatListModel chatListModel;
	private JScrollBar vertical;
	private ClientType clientType;
	private VoiceClient voiceClient;

	private boolean isLoggingOut;

	public ChatWindow(String title, ClientType clientType, InetAddress IP) {
		this.clientType = clientType;
		this.isLoggingOut = false;
		setUpGui(title);
		voiceClient = new VoiceClient(IP);
	}

	/**
	 * Setup print writer for sending message from user to server
	 */
	protected abstract void setupWriter();

	/**
	 * This method invokes log out on both sides (customer, agent), called when user clicks button
	 */
	protected abstract void invokeLogOut();

	/**
	 * This method is called to finish logging out<br>
	 * Can be invoked from other side (agent, customer) to perform log out
	 */
	public abstract void logOut(Message message);

	/**
	 * Uses writer to send message to server
	 * @param message message to send
	 */
	protected abstract void sendMessage(String message);


	protected abstract void sendVoiceFinished();

	/**
	 * Main method for setting up the GUI
	 * @param title
	 */
	private void setUpGui(String title) {
		// general layout
		mainFrame = new JFrame();
		BorderLayout mainLayout = new BorderLayout(); 
		mainFrame.setLayout(mainLayout);
		mainFrame.add(getMessagePanel(), BorderLayout.CENTER);
		mainFrame.add(getBottomInputPanel(), BorderLayout.PAGE_END);
		mainFrame.setMinimumSize(new Dimension(600, 500));

		// basic settings and packing
		mainFrame.setTitle(title);
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainFrame.pack();
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setVisible(true);
	}

	/**
	 * Creates component displaying chat messages<br>
	 * Based on {@link JList} with custom cell renderer and custom model
	 * @return GUi component for showing messages
	 */
	private Component getMessagePanel() {
		chatListModel = new ChatListModel();

		JList<Message> chatList = new JList<Message>(chatListModel);
		chatList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		chatList.setVisibleRowCount(15); 
		DefaultListCellRenderer renderer = (DefaultListCellRenderer) chatList.getCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.CENTER);
		chatList.setCellRenderer(new ChatRenderer());
		chatList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		chatList.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				chatListClick(e.getPoint(), chatList);
			}
		});

		JScrollPane chatListScroll = new JScrollPane(chatList);
		vertical = chatListScroll.getVerticalScrollBar();

		return chatListScroll;
	}

	private void chatListClick(Point point, JList<Message> chatList) {
		int index = chatList.locationToIndex(point);
		Message item = (Message) chatList.getModel().getElementAt(index);
		if (item.getMessageType() == MessageType.VOICE) {
			new VoicePlayThread(item.getVoiceData());
		}
	}

	/**
	 * Creates bottom component for writing and sending new messages and for logging out
	 * @return GUI component
	 */
	private Component getBottomInputPanel() {
		tfMainInput = new JTextField();
		tfMainInput.addKeyListener(new MainInputKeyAdapter());

		btSend = new JButton("Send");
		btSend.setMnemonic(KeyEvent.VK_S);
		btSend.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btSend.addActionListener(e -> showMessage());

		BorderLayout messagingLayout = new BorderLayout(); 
		messagingLayout.setHgap(5);
		JPanel messagingPanel = new JPanel(messagingLayout);
		messagingPanel.add(tfMainInput, BorderLayout.CENTER);

		JPanel btPanel = new JPanel(new FlowLayout());
		btPanel.add(btSend);
		
		if (clientType == ClientType.AGENT) {
			btSendBoth = new JButton("Send both");
			btSendBoth.setMnemonic(KeyEvent.VK_B);
			btSendBoth.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			btSendBoth.addActionListener(e -> showMessageBoth());
			btPanel.add(btSendBoth);
		}

		messagingPanel.add(btPanel, BorderLayout.EAST);
		messagingPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));


		BorderLayout voiceLayout = new BorderLayout(); 
		voiceLayout.setHgap(5);
		JPanel voicePanel = new JPanel(voiceLayout);

		btLogOut = new JButton("Log Out");
		btLogOut.setMnemonic(KeyEvent.VK_L);
		btLogOut.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btLogOut.addActionListener(e -> invokeLogOut());
		voicePanel.add(btLogOut, BorderLayout.EAST);

		JPanel voiceLeftPanel = new JPanel(new FlowLayout());
		btCapture = new JButton("Capture voice");
		btCapture.setMnemonic(KeyEvent.VK_C);
		btCapture.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btCapture.addActionListener(e -> startCapture());
		voiceLeftPanel.add(btCapture);

		btStop = new JButton("Stop capturing");
		btStop.setMnemonic(KeyEvent.VK_P);
		btStop.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btStop.addActionListener(e -> stopCapture());
		voiceLeftPanel.add(btStop);
		
		voicePanel.add(voiceLeftPanel, BorderLayout.WEST);


		JPanel bottomPanel = new JPanel();
		bottomPanel.setLayout(new BoxLayout(bottomPanel, BoxLayout.Y_AXIS));
		bottomPanel.add(messagingPanel);
		bottomPanel.add(voicePanel);

		return bottomPanel;
	}

	/**
	 * Method for start of capturing
	 */
	private void startCapture() {
		voiceClient.captureAudio();
	}

	/**
	 * Stop capturing, add message to window
	 */
	private void stopCapture() {
		ByteArrayOutputStream voiceData = voiceClient.stopCapture();
		addMessage(new Message(voiceData, ClientType.ME));
		sendVoiceFinished();
	}

	/**
	 * In case of agent, it is possible to send one message to both customers
	 * Here it all starts
	 */
	private void showMessageBoth() {
		String message = tfMainInput.getText();
		ClientAgent.sendBoth(message);
	}

	/**
	 * Get input and call other method
	 */
	public void showMessage() {
		String message = tfMainInput.getText();
		showMessage(message);
	}
	
	/**
	 * Show given message in chat window and call method for sending it 
	 * @param message message to show and send
	 */
	public void showMessage(String message) {
		if (message.equals("")) {
			return;
		}
		tfMainInput.setText("");
		
		Message newMessage = new Message(message, ClientType.ME);
		
		// show message
		addMessage(newMessage);
		
		sendMessage(message);
	}
	
	/**
	 * Main method for adding new messages to chat window
	 * @param message message to add
	 */
	public void addMessage(Message message) {
		chatListModel.getMessages().add(message);
		chatListModel.update();
		scrollDown();
	}
	
	protected void disableControls() {
		btSend.setEnabled(false);
		btLogOut.setEnabled(false);
		if (btSendBoth != null) btSendBoth.setEnabled(false);
		tfMainInput.setEnabled(false);
		btCapture.setEnabled(false);
		btStop.setEnabled(false);
	}
	
	/**
	 * Scroll down the chat window to see the newest messages
	 */
	private void scrollDown() {
		vertical.setValue(vertical.getMaximum());
	}

	public boolean isLoggingOut() {
		return isLoggingOut;
	}

	public void setLoggingOut(boolean isLoggingOut) {
		this.isLoggingOut = isLoggingOut;
	}

	public ChatListModel getChatListModel() {
		return chatListModel;
	}

	/**
	 * Key adapter for listening for Enter key, which also sends current message
	 */
	private class MainInputKeyAdapter extends KeyAdapter {
		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				showMessage();
			}
		}
	}

}
