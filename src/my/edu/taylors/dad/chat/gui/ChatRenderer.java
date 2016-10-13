package my.edu.taylors.dad.chat.gui;

import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.ListCellRenderer;
import javax.swing.SwingConstants;

import my.edu.taylors.dad.chat.entity.ClientType;
import my.edu.taylors.dad.chat.entity.Message;

public class ChatRenderer implements ListCellRenderer<Message> {

	@Override
	public Component getListCellRendererComponent(JList<? extends Message> list, Message value, int index,
			boolean isSelected, boolean cellHasFocus) {

		JLabel label = new JLabel(value.toString());

		if (value.getClientType() == ClientType.ME) {
			label.setHorizontalAlignment(SwingConstants.RIGHT);
		} else {
			label.setHorizontalAlignment(SwingConstants.LEFT);
		}

		return label;
	}

}
