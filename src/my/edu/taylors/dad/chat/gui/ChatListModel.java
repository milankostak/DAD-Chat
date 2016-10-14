package my.edu.taylors.dad.chat.gui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.AbstractListModel;

import my.edu.taylors.dad.chat.entity.ClientType;
import my.edu.taylors.dad.chat.entity.Message;

public class ChatListModel extends AbstractListModel<Message> {
	private static final long serialVersionUID = 1L;

	private List<Message> messages;

	public ChatListModel() {
		messages = new ArrayList<>();
		messages.add(new Message(new Date(), "asdf", ClientType.NOT_ME));
		messages.add(new Message(new Date(), "bfhng", ClientType.NOT_ME));
		messages.add(new Message(new Date(), "agfhngfhnsdf", ClientType.NOT_ME));
		messages.add(new Message(new Date(), "g", ClientType.NOT_ME));
		messages.add(new Message(new Date(), "nnnn", ClientType.NOT_ME));
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