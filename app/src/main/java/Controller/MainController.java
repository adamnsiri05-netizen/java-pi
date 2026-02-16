package Controller;

import Models.User;
import Models.Message;
import Models.Conversation;
import Services.ServiceUser;
import Services.ServiceMessage;
import Services.ServiceConversation;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class MainController {

    @FXML private ListView<String> userListView;
    @FXML private ListView<Message> messageListView;
    @FXML private TextField inputField;
    @FXML private ComboBox<String> currentUserCombo;

    private final ServiceUser serviceUser = new ServiceUser();
    private final ServiceMessage serviceMessage = new ServiceMessage();
    private final ServiceConversation serviceConversation = new ServiceConversation();

    private List<User> users = new ArrayList<>();
    private User selectedUser = null;
    private int currentUserId = -1; 
    private int currentConversationId = -1;

    @FXML
    public void initialize() {
        loadUsers();

        // Listener for the "Acting As" user selector
        currentUserCombo.getSelectionModel().selectedIndexProperty().addListener((obs, oldVal, newVal) -> {
            int idx = newVal.intValue();
            if (idx >= 0 && idx < users.size()) {
                currentUserId = users.get(idx).getIdUser();
                // Refresh conversation if a user was already selected
                if (selectedUser != null) loadConversationWith(selectedUser);
            }
        });

        // Listener for selecting a contact to chat with
        userListView.setOnMouseClicked((MouseEvent ev) -> {
            int idx = userListView.getSelectionModel().getSelectedIndex();
            if (idx >= 0 && idx < users.size()) {
                selectedUser = users.get(idx);
                loadConversationWith(selectedUser);
            }
        });

        // --- MODERN CHAT BUBBLE CELL FACTORY ---
        messageListView.setCellFactory(lv -> new ListCell<>() {
            @Override
            protected void updateItem(Message item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    // 1. Message Text
                    Label messageText = new Label(item.getContenuMessage());
                    messageText.setWrapText(true);
                    messageText.getStyleClass().add("message-text");

                    // 2. Metadata (Date/Time)
                    Label dateLabel = new Label(item.getDateMessage().toString());
                    dateLabel.setStyle("-fx-font-size: 0.8em; -fx-text-fill: #888888;");

                    // 3. Options Button (+)
                    Button optBtn = new Button("+");
                    optBtn.getStyleClass().add("plus-button");
                    setupMessageContextMenu(optBtn, item);

                    // 4. Arrange Text and Date inside a VBox
                    VBox textContainer = new VBox(2, messageText, dateLabel);
                    
                    // 5. Create the Bubble (HBox)
                    HBox bubble = new HBox(10, textContainer, optBtn);
                    bubble.getStyleClass().add("bubble");
                    bubble.setMaxWidth(350); // Prevent bubbles from stretching too far

                    // 6. Final Alignment Container
                    HBox alignmentWrapper = new HBox(bubble);
                    alignmentWrapper.setFillHeight(true);

                    // Logic: If I sent it, move to Right + Blue. Else, Left + Gray.
                    if (item.getIdExpediteur() == currentUserId) {
                        alignmentWrapper.setAlignment(Pos.CENTER_RIGHT);
                        bubble.getStyleClass().add("bubble-me");
                    } else {
                        alignmentWrapper.setAlignment(Pos.CENTER_LEFT);
                        bubble.getStyleClass().add("bubble-other");
                    }

                    setGraphic(alignmentWrapper);
                }
            }
        });
    }

    /**
     * Helper to create the Edit/Delete menu for a specific message
     */
    private void setupMessageContextMenu(Button button, Message item) {
        button.setOnAction(e -> {
            ContextMenu cm = new ContextMenu();
            MenuItem edit = new MenuItem("Modifier");
            MenuItem del = new MenuItem("Supprimer");

            edit.setOnAction(ev -> {
                TextInputDialog dialog = new TextInputDialog(item.getContenuMessage());
                dialog.setTitle("Modifier le message");
                dialog.setHeaderText(null);
                dialog.setContentText("Nouveau message:");
                dialog.showAndWait().ifPresent(newText -> {
                    if (!newText.trim().isEmpty()) {
                        item.setContenuMessage(newText);
                        serviceMessage.updateMessage(item);
                        refreshCurrentConversation();
                    }
                });
            });

            del.setOnAction(ev -> {
                Alert a = new Alert(AlertType.CONFIRMATION, "Supprimer ce message ?", ButtonType.YES, ButtonType.NO);
                a.showAndWait().ifPresent(resp -> {
                    if (resp == ButtonType.YES) {
                        serviceMessage.deleteMessage(item.getIdMessage());
                        refreshCurrentConversation();
                    }
                });
            });

            cm.getItems().addAll(edit, del);
            cm.show(button, javafx.geometry.Side.BOTTOM, 0, 0);
        });
    }

    private void loadUsers() {
        users = serviceUser.getAllUsers();
        ObservableList<String> displayNames = FXCollections.observableArrayList();
        if (users != null) {
            for (User u : users) {
                displayNames.add(u.getNomUser());
            }
            userListView.setItems(displayNames);
            currentUserCombo.setItems(displayNames);
        }
    }

    private void loadConversationWith(User u) {
        List<Message> all = serviceMessage.getAllMessages();
        List<Message> filtered = new ArrayList<>();
        currentConversationId = -1;

        if (all != null) {
            for (Message m : all) {
                boolean involvesSelected = (m.getIdExpediteur() == u.getIdUser() || m.getIdDestinataire() == u.getIdUser());
                boolean involvesMe = (currentUserId == -1) || (m.getIdExpediteur() == currentUserId || m.getIdDestinataire() == currentUserId);
                
                if (involvesSelected && involvesMe) {
                    filtered.add(m);
                    if (currentConversationId == -1 && m.getIdConversation() > 0) {
                        currentConversationId = m.getIdConversation();
                    }
                }
            }
        }
        messageListView.setItems(FXCollections.observableArrayList(filtered));
        // Auto-scroll to bottom
        messageListView.scrollTo(filtered.size() - 1);
    }

    @FXML
    public void onSendClicked() {
        String text = inputField.getText();
        if (text == null || text.trim().isEmpty() || selectedUser == null) return;

        // Default to first user if none acting
        if (currentUserId == -1) currentUserId = 1; 

        // Handle conversation creation if it doesn't exist
        if (currentConversationId == -1) {
            Conversation conv = new Conversation(LocalDate.now(), "ACTIVE", false);
            serviceConversation.createConversation(conv);
            currentConversationId = conv.getIdConversation();
        }

        Message m = new Message(text, LocalDate.now(), false, currentUserId, selectedUser.getIdUser(), currentConversationId);
        serviceMessage.createMessage(m);
        
        inputField.clear();
        loadConversationWith(selectedUser);
    }

    @FXML
    public void refreshCurrentConversation() {
        if (selectedUser != null) loadConversationWith(selectedUser);
    }
}