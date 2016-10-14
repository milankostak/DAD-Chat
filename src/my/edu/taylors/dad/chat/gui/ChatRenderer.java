package my.edu.taylors.dad.chat.gui;

import java.awt.Color;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;

import my.edu.taylors.dad.chat.entity.ClientType;
import my.edu.taylors.dad.chat.entity.Message;

/**
 * Custom chat window renderer to show message in nicer way
 */
public class ChatRenderer implements ListCellRenderer<Message> {

	@Override
	public Component getListCellRendererComponent(JList<? extends Message> list, Message value, int index,
			boolean isSelected, boolean cellHasFocus) {

		FlowLayout layout = new FlowLayout();
		JPanel messagePanel = new JPanel(layout);
		//int width = (int)(list.getWidth() * 0.8);
		//messagePanel.setMaximumSize(new Dimension(width, 20));
		messagePanel.setOpaque(true);
		messagePanel.setBackground(Color.WHITE);

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

		JLabel lbMessage = new JLabel(value.getMessage());
		lbMessage.setOpaque(true);
		lbMessage.setBackground(color);
		final int border = 6;
		lbMessage.setBorder(BorderFactory.createEmptyBorder(border, border, border, border));

		JLabel lbTime = new JLabel(value.getFormattedTime());
		lbTime.setOpaque(true);
		lbTime.setBackground(color2);
		lbTime.setBorder(BorderFactory.createEmptyBorder(border, border, border, border));
		lbTime.setFont(new Font(lbTime.getFont().getFontName(), Font.PLAIN, 10));

		messagePanel.add(lbTime);
		messagePanel.add(lbMessage);

		return messagePanel;
	}

}
