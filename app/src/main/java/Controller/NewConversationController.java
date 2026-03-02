package Controller;

import Models.User;
import Models.Conversation;
import Services.ServiceUser;
import Services.ServiceConversation;
import Services.ServiceMessage;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class NewConversationController {

    @FXML private ListView<String> availableUsersListView;

    private final ServiceUser serviceUser = new ServiceUser();
    private final ServiceConversation serviceConversation = new ServiceConversation();

    private User currentUser;
    private ConversationsController parentController;
    private List<User> availableUsers;

    public void setCurrentUser(User user) {
        this.currentUser = user;
    }

    public void setParentController(ConversationsController controller) {
        this.parentController = controller;
        initialize();
    }

    @FXML
    public void initialize() {
        if (currentUser == null) return;

        loadAvailableUsers();
    }

    private void loadAvailableUsers() {
        availableUsersListView.getItems().clear();
        availableUsers = new ArrayList<>();

        List<User> allUsers = serviceUser.getAllUsers();
        if (allUsers != null) {
            for (User u : allUsers) {
                if (u.getIdUser() != currentUser.getIdUser()) {
                    // Check if there's already a conversation with this user
                    List<Conversation> conversations = serviceConversation.getConversationsBetweenUsers(
                            currentUser.getIdUser(), u.getIdUser());
                    if (conversations.isEmpty()) {
                        // No conversation exists with this user, add to available
                        availableUsers.add(u);
                    }
                }
            }
        }

        var userNames = FXCollections.<String>observableArrayList();
        for (User u : availableUsers) {
            userNames.add(u.getNomUser());
        }
        availableUsersListView.setItems(userNames);

        // Show message if no users available
        if (availableUsers.isEmpty()) {
            availableUsersListView.setPlaceholder(new javafx.scene.control.Label("You've already chatted with everyone!"));
        }
    }

    @FXML
    public void onStartChatClicked() {
        int selectedIdx = availableUsersListView.getSelectionModel().getSelectedIndex();
        if (selectedIdx < 0) {
            return;
        }

        User selectedUser = availableUsers.get(selectedIdx);

        // Create a new conversation
        Conversation conv = new Conversation(LocalDate.now(), "ACTIVE", false);
        serviceConversation.createConversation(conv);
        
        // Add both users as participants
        serviceConversation.addParticipant(conv.getIdConversation(), currentUser.getIdUser());
        serviceConversation.addParticipant(conv.getIdConversation(), selectedUser.getIdUser());

        // Refresh parent and close this dialog
        if (parentController != null) {
            parentController.refreshConversations();
        }

        Stage stage = (Stage) availableUsersListView.getScene().getWindow();
        stage.close();
    }

    @FXML
    public void onCancelClicked() {
        Stage stage = (Stage) availableUsersListView.getScene().getWindow();
        stage.close();
    }
}
