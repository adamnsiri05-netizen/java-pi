package Services;

import Models.Conversation;
import Repository.ConversationRepository;
import Repository.MessageRepository;
import java.util.List;

public class ServiceConversation implements InterfaceConversation {

    // Dependency on the Repository
    private final ConversationRepository conversationRepository;
    private final MessageRepository messageRepository;

    public ServiceConversation() {
        // Direct instantiation of the repository
        this.conversationRepository = new ConversationRepository();
        this.messageRepository = new MessageRepository();
    }

    @Override
    public void createConversation(Conversation conversation) {
        // Business logic can be added here (e.g., validation)
        conversationRepository.save(conversation);
    }

    @Override
    public Conversation getConversationById(int id) {
        // Delegate retrieval to the repository
        return conversationRepository.findById(id);
    }

    @Override
    public List<Conversation> getAllConversations() {
        // Delegate fetching the list to the repository
        return conversationRepository.findAll();
    }

    @Override
    public void updateConversation(Conversation conversation) {
        // Delegate update logic to the repository
        conversationRepository.update(conversation);
    }

    @Override
    public void deleteConversation(int id) {
        // Delete all messages in the conversation first
        messageRepository.deleteByConversationId(id);
        // Delete all participants
        conversationRepository.deleteParticipants(id);
        // Then delete the conversation
        conversationRepository.delete(id);
    }

    public List<Conversation> getConversationsBetweenUsers(int userId1, int userId2) {
        return conversationRepository.findConversationsByUserPair(userId1, userId2);
    }

    public void addParticipant(int conversationId, int userId) {
        conversationRepository.addParticipant(conversationId, userId);
    }
}