package my.edu.taylors.dad.chat.gui;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;

public class WaitingWindow extends JFrame {
	private static final long serialVersionUID = 1L;

	/**
	 * Basic waiting window when there is no client for an agent or no agent for a client
	 * @param message message to show depending on who is waiting
	 */
	public WaitingWindow(String message) {
		super("Please wait");
		ImageIcon loading = new ImageIcon("loader.gif");
		add(new JLabel(message, loading, JLabel.CENTER));

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setResizable(false);
		setSize(380, 150);
		setLocationRelativeTo(null);
	}
}
