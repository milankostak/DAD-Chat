package my.edu.taylors.dad.chat.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import my.edu.taylors.dad.chat.entity.ClientType;
import my.edu.taylors.dad.chat.entity.Message;
import my.edu.taylors.dad.chat.entity.MessageType;

/**
 * Custom chat window renderer to show message in a nicer way
 */
public class ChatRenderer implements ListCellRenderer<Message> {

	@Override
	public Component getListCellRendererComponent(JList<? extends Message> list, Message value, int index,
			boolean isSelected, boolean cellHasFocus) {

		FlowLayout layout = new FlowLayout();
		JPanel messagePanel = new JPanel(layout);
		messagePanel.setOpaque(true);
		messagePanel.setBackground(Color.WHITE);

		final int border = 6;
		Color color, color2;
		if (value.getClientType() == ClientType.ME) {
			layout.setAlignment(FlowLayout.RIGHT);
			color = Color.decode("#aaccee");
			color2 = Color.decode("#cbddff");
		} else {
			layout.setAlignment(FlowLayout.LEFT);
			color = Color.decode("#dddddd");
			color2 = Color.decode("#eeeeee");
		}
		
		JLabel lbTime = new JLabel(value.getFormattedTime());
		lbTime.setOpaque(true);
		lbTime.setBackground(color2);
		lbTime.setBorder(BorderFactory.createEmptyBorder(border, border, border, border));
		lbTime.setFont(new Font(lbTime.getFont().getFontName(), Font.PLAIN, 10));

		messagePanel.add(lbTime);

		
		if (value.getMessageType() == MessageType.TEXT) {
			JLabel lbMessage = new JLabel(value.getMessage());
			lbMessage.setOpaque(true);
			lbMessage.setBackground(color);
			lbMessage.setBorder(BorderFactory.createEmptyBorder(border, border, border, border));
			messagePanel.add(lbMessage);
		} else {
			JButton btPlay = new JButton("Play");
			btPlay.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
			messagePanel.add(btPlay);
		}

		return messagePanel;
	}

}
