package my.edu.taylors.dad.chat.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractListModel;

import my.edu.taylors.dad.chat.entity.Message;

public class ChatListModel extends AbstractListModel<Message> {
	private static final long serialVersionUID = 1L;

	private List<Message> messages;

	public ChatListModel() {
		messages = new ArrayList<>();
	}

	public List<Message> getMessages() {
		return messages;
	}

	@Override
	public int getSize() {
		return messages.size();
	}

	@Override
	public Message getElementAt(int index) {
		return messages.get(index);
	}
	
    public void update() {
        fireContentsChanged(this, 0, messages.size() - 1);
    }

}
