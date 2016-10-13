package my.edu.taylors.dad.chat.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Date;

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

import my.edu.taylors.dad.chat.entity.ClientType;
import my.edu.taylors.dad.chat.entity.Message;

public class ClientGui extends Thread {

	// GUI components
	private JFrame mainFrame;
	private JTextField tfMainInput;
	private ChatListModel chatListModel;
	private JScrollBar vertical;
	
	// communication componnets
	private Socket socket;
	private PrintWriter writer;
	private boolean keepReceiving;

	public ClientGui(Socket socket) {
		this.socket = socket;
		setupWriter();
		setUpGui();
		start();
	}
	
	@Override
	public void run() {
		try {
			BufferedReader fromServer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			keepReceiving = true;
			while (keepReceiving) {
				String message = fromServer.readLine();
				if (message != null) {
					System.out.println(message);
					Message msg = new Message(new Date(), message, ClientType.NOT_ME);
					addMessage(msg);
				}
				
			}
		} catch (SocketException e) {
			// throws when closing window, because it is waiting for server while we close the socket
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void setupWriter() {
		try {
			writer = new PrintWriter(new OutputStreamWriter(socket.getOutputStream()));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void setUpGui() {
		// general layout
		mainFrame = new JFrame();
		BorderLayout mainLayout = new BorderLayout(); 
		mainFrame.setLayout(mainLayout);
		mainFrame.add(getMessagePanel(), BorderLayout.CENTER);
		mainFrame.add(getBottomInputPanel(), BorderLayout.PAGE_END);
		mainFrame.setMinimumSize(new Dimension(500, 400));

		// basic settings and packing
		mainFrame.setTitle("System");//TODO name
		mainFrame.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		mainFrame.addWindowListener(new CustomExitWindowListener());


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
		btSend.addActionListener(e -> sendMessage());

		BorderLayout bottomLayout = new BorderLayout(); 
		bottomLayout.setHgap(5);
		JPanel bottomPanel = new JPanel(bottomLayout);
		bottomPanel.add(tfMainInput, BorderLayout.CENTER);
		bottomPanel.add(btSend, BorderLayout.EAST);
		bottomPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

		return bottomPanel;
	}

	private void sendMessage() {
		String message = tfMainInput.getText();
		if (message.equals("")) {
			return;
		}
		tfMainInput.setText("");
		
		Message newMessage = new Message(new Date(), message, ClientType.ME);
		
		// show message
		addMessage(newMessage);
		
		// send message
		writer.println(message);
		writer.flush();
	}
	
	private void addMessage(Message message) {
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
				sendMessage();
			}
		}
	}
	
	private class CustomExitWindowListener extends WindowAdapter {

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
	};
	
}


