package my.edu.taylors.dad.chat.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.BorderFactory;
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

import my.edu.taylors.dad.chat.ClientAgent;
import my.edu.taylors.dad.chat.entity.ClientType;
import my.edu.taylors.dad.chat.entity.Message;

public abstract class ChatWindow extends JFrame {
	private static final long serialVersionUID = 1L;

	// GUI components
	private JFrame mainFrame;
	private JTextField tfMainInput;
	private ChatListModel chatListModel;
	private JScrollBar vertical;
	private ClientType clientType;

	public ChatWindow(String title, ClientType clientType) {
		this.clientType = clientType;
		setUpGui(title);
	}

	protected abstract void setupWriter();

	protected abstract void logOut();
	
	protected abstract void sendMessage(String message);

	private void setUpGui(String title) {
		// general layout
		mainFrame = new JFrame();
		BorderLayout mainLayout = new BorderLayout(); 
		mainFrame.setLayout(mainLayout);
		mainFrame.add(getMessagePanel(), BorderLayout.CENTER);
		mainFrame.add(getBottomInputPanel(), BorderLayout.PAGE_END);
		mainFrame.setMinimumSize(new Dimension(500, 400));

		// basic settings and packing
		mainFrame.setTitle(title);//TODO name
		//TODO closing
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		//mainFrame.addWindowListener(new CustomExitWindowListener());

		mainFrame.pack();
		mainFrame.setLocationRelativeTo(null);
		mainFrame.setVisible(true);
	}

	private Component getMessagePanel() {
		chatListModel = new ChatListModel();

		JList<Message> chatList = new JList<Message>(chatListModel);
		chatList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		chatList.setVisibleRowCount(15); 
		DefaultListCellRenderer renderer = (DefaultListCellRenderer) chatList.getCellRenderer();
		renderer.setHorizontalAlignment(SwingConstants.CENTER);
		chatList.setCellRenderer(new ChatRenderer());
		chatList.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		JScrollPane chatListScroll = new JScrollPane(chatList);
		vertical = chatListScroll.getVerticalScrollBar();

		return chatListScroll;
	}

	private Component getBottomInputPanel() {
		tfMainInput = new JTextField();
		tfMainInput.addKeyListener(new MainInputKeyAdapter());

		JButton btSend = new JButton("Send");
		btSend.setMnemonic(KeyEvent.VK_S);
		btSend.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btSend.addActionListener(e -> showMessage());

		BorderLayout bottomLayout = new BorderLayout(); 
		bottomLayout.setHgap(5);
		JPanel bottomPanel = new JPanel(bottomLayout);
		bottomPanel.add(tfMainInput, BorderLayout.CENTER);

		JPanel btPanel = new JPanel(new FlowLayout());
		btPanel.add(btSend);
		
		if (clientType == ClientType.AGENT) {
			JButton btSendBoth = new JButton("Send both");
			btSendBoth.setMnemonic(KeyEvent.VK_B);
			btSendBoth.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			btSendBoth.addActionListener(e -> showMessageBoth());
			btPanel.add(btSendBoth);
		}

		JButton btLogOut = new JButton("Log Out");
		btLogOut.setMnemonic(KeyEvent.VK_L);
		btLogOut.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		btLogOut.addActionListener(e -> logOut());
		btPanel.add(btLogOut);

		bottomPanel.add(btPanel, BorderLayout.EAST);
		
		bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		return bottomPanel;
	}

	private void showMessageBoth() {
		String message = tfMainInput.getText();
		ClientAgent.sendBoth(message);
	}

	public void showMessage() {
		String message = tfMainInput.getText();
		showMessage(message);
	}

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

	public void addMessage(Message message) {
		chatListModel.getMessages().add(message);
		chatListModel.update();
		scrollDown();
	}

	private void scrollDown() {
		vertical.setValue(vertical.getMaximum());
	}

	private class MainInputKeyAdapter extends KeyAdapter {
		public void keyReleased(KeyEvent e) {
			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
				showMessage();
			}
		}
	}
	
	/*private class CustomExitWindowListener extends WindowAdapter {

	    @Override
	    public void windowClosing(WindowEvent event) {
	        keepReceiving = false;
	        if (socket != null) {
				try {
					socket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
	        }
	        WindowEvent wev = new WindowEvent(mainFrame, WindowEvent.WINDOW_CLOSING);
	        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
	        mainFrame.setVisible(false);
	        mainFrame.dispose();
	        System.exit(0);
	    }
	};*/
}
