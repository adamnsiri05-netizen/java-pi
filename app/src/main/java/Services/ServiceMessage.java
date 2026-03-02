package Services;

import Models.Message;
import Repository.MessageRepository;
import java.util.List;
import utils.ProfanityFilter;

public class ServiceMessage implements InterfaceMessage {

    // Dependency on the Repository
    private final MessageRepository messageRepository;

    public ServiceMessage() {
        // Direct instantiation as requested
        this.messageRepository = new MessageRepository();
    }

    @Override
    public void createMessage(Message message) {
        // Sanitize content before saving
        String content = message.getContenuMessage();
        content = ProfanityFilter.sanitize(content);
        message.setContenuMessage(content);

        // Business logic: ensure content is not empty after sanitization
        if (message.getContenuMessage() != null && !message.getContenuMessage().trim().isEmpty()) {
            messageRepository.save(message);
        }
    }

    @Override
    public Message getMessageById(int id) {
        return messageRepository.findById(id);
    }

    @Override
    public List<Message> getAllMessages() {
        return messageRepository.findAll();
    }

    @Override
    public void updateMessage(Message message) {
        // Sanitize content before updating
        String content = message.getContenuMessage();
        content = ProfanityFilter.sanitize(content);
        message.setContenuMessage(content);

        messageRepository.update(message);
    }

    @Override
    public void deleteMessage(int id) {
        messageRepository.delete(id);
    }

    public List<Message> getMessagesByConversation(int conversationId) {
        return messageRepository.findByConversationId(conversationId);
    }
}